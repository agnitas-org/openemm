(() => {
  const Def = AGN.Lib.WM.Definitions;
  const Node = AGN.Lib.WM.Node;
  const Vertex = AGN.Lib.WM.Vertex;
  const VertexGroup = AGN.Lib.WM.VertexGroup;
  const NodeTitleHelper = AGN.Lib.WM.NodeTitleHelper;
  const Canvas = AGN.Lib.WM.Canvas;
  const GridBackground = AGN.Lib.WM.GridBackground;
  const ConnectionConstraints = AGN.Lib.WM.ConnectionConstraints;
  const DraggableButtons = AGN.Lib.WM.DraggableButtons;
  const AutoAlignment = AGN.Lib.WM.AutoAlignment;
  const UndoManager = AGN.Lib.WM.UndoManager;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Dialogs = AGN.Lib.WM.Dialogs;
  const Snippets = AGN.Lib.WM.Snippets;
  const ContextMenu = AGN.Lib.ContextMenu;
  const Helpers = AGN.Lib.Helpers;
  const Messages = AGN.Lib.Messages;
  const Utils = AGN.Lib.WM.Utils;

  const SCROLL_STEP = 50;

  const Ui = {
    blurInputFields: function () {
      if (document.activeElement && $(document.activeElement).is('input, textarea')) {
        document.activeElement.blur();
      }
    },
    setConnectionLabel: function (connection, text) {
      if (text) {
        var label = connection.origin.getOverlay('label');
        if (label) {
          label.setLabel(text);
          label.show();
        } else {
          connection.origin.addOverlay(['Label', {
            location: 0.5,
            id: 'label',
            cssClass: 'connection-label',
            label: text
          }]);
        }
      } else {
        connection.origin.hideOverlay('label');
      }
    },
    setDeleteButtonEnabled: function (isEnabled) {
      $('#deleteButton').toggleClass('disabled', !isEnabled);
      $('#deleteItem').prop('disabled', !isEnabled);
    },
    setUndoButtonEnabled: function (isEnabled) {
      $('#undoButton').toggleClass('disabled', !isEnabled);
      $('#undoItem').prop('disabled', !isEnabled);
    }
  };

  class Editor {
    constructor(isEditable, isContextMenuEnabled, isFootnotesEnabled) {
      this.$viewport = $('#viewPort');
      this.$container = $('#canvas');
      this.$titlesContainer = this.$container.find('#icon-titles-container');
      this.canvas = new Canvas(this.$container);
      this.canvasZoomHandler = null;

      this.instance = jsPlumb.getInstance(Editor.defaults(this.$container));
      this.editable = !!isEditable;
      this.lastAllocatedId = 0;
      this.drag = null;
      this.chainModeEnabled = false;
      this.multiConnectionEnabled = false;
      this.scale = 1;
      this.dragMode = Def.DEFAULT_DRAG_MODE;
      this.gridBackground = new GridBackground(this.canvas);

      // enables hover effect when drag node (required for node inserting inside the hovered connection)
      this.instance.isHoverSuspended = () => !this.editable;

      const self = this;

      this.canvas.setOnZoom(scale => {
        this.instance.setZoom(scale);
        this.gridBackground.setZoom(scale);
        if (this.canvasZoomHandler) {
          this.canvasZoomHandler.call(null, scale);
        }
      });

      this.draggableButtons = new DraggableButtons({
        enabled: !!isEditable,
        getZoom: () => self.getZoom(),
        onStart: function (type) {
          this._connections = self.getConnections();
          this._connectionsHoverAllowed = !Node.isExpandableType(type);
          this._occupiedAreas = self.getNodes$()
            .get()
            .map(node => Node.getCollisionBox($(node)));
        },
        onStop: function () {
          this._connections.forEach(c => $(c.origin.canvas).removeClass('js-connection-drop-forbidden'));
          this._connections = null;
        },
        onDrag: function ($node) {
          const newNodeBox = Node.getCollisionBox($node);
          const hasCollision = this._occupiedAreas.some(box => Node.detectBoxCollision(newNodeBox, box));

          $node.toggleClass('js-collision-detected', hasCollision);

          this._connections.forEach(c => {
            $(c.origin.canvas).toggleClass('js-connection-drop-forbidden', c.origin.isHover() && !this._connectionsHoverAllowed);
          });
        },
        onDrop: function ($node, type, position) {
          const collisionExists = $node?.hasClass('js-collision-detected');
          const hoveredConnection = self.findHoveredConnection(this._connections);

          if (!hoveredConnection && collisionExists) {
            return;
          }

          if (hoveredConnection && !this._connectionsHoverAllowed) {
            return;
          }

          self.undoManager.transaction(() => {
            if (!hoveredConnection || self.canInsertNodeIntoConnection(Node.create(type), hoveredConnection)) {
              const node = self.newNode(type, position);

              if (hoveredConnection) {
                self.insertNodeIntoConnection(node, hoveredConnection, collisionExists);
              }
            }
          });
        }
      })

      this.$viewport.selectable({
        filter: '.node',
        distance: 10,
        disabled: !this.editable,
        start: function () {
          $('#selection-backdrop').show();
        },
        stop: function () {
          $('#selection-backdrop').hide();
        },
        selected: function (event, ui) {
          self.instance.addToDragSelection(ui.selected);
          self.onSelectionChanged();
        },
        unselected: function (event, ui) {
          self.instance.removeFromDragSelection(ui.unselected);
          self.onSelectionChanged();
        }
      });

      this.$viewport.on('mousedown', function (event) {
        if (['canvas', 'viewPort', 'icon-titles-container'].includes(event.target.id) || $.contains(self.$titlesContainer[0], event.target)) {
          self.deselectAll();
        }

        Ui.blurInputFields();
      });

      this.$viewport.on('wheel', (event) => {
        if (this.canvas.mouseWheelZoomEnabled) {
          event.preventDefault();
          return;
        }
        const {x, y} = this.canvas.getPosition();
        const direction = event.originalEvent.wheelDelta > 0 ? 1 : -1;
        const scroll = SCROLL_STEP * direction;
        const [newX, newY] = event.shiftKey ? [x + scroll, y] : [x, y + scroll];
        this.canvas.moveTo(newX, newY);
      });

      // On connection created.
      this.instance.bind('connection', function (connection) {
        self.undoManager.operation('connectionCreated', connection);
        self.autofillStopNodesIfPossible();
        self.autoConvertMailingNodesTypeIfPossible();
        self.updateOverlays();
        self.positionTitles();
        self.processRecipientsChains();
      });

      this.instance.bind('connectionDetached', function (connection) {
        self.undoManager.operation('connectionDeleted', connection);
        self.autofillStopNodesIfPossible();
        self.autoConvertMailingNodesTypeIfPossible();
        self.updateOverlays();
        self.positionTitles();
        self.processRecipientsChains();
      });

      this.instance.bind('mousedown', function (connection, event) {
        if (self.editable && event.button === Def.MOUSE_BUTTON_MIDDLE) {
          _.defer(function () {
            // Use defer function to make sure jsplumb will fire connectionDetached event synchronously
            // (otherwise transaction of undo manager will be broken).

            self.undoManager.transaction(function () {
              self.deleteConnection(connection);
            });
          });
        }

        Ui.blurInputFields();
      });

      this.instance.bind('beforeDrag', function () {
        self.undoManager.startTransaction();
      });

      this.instance.bind('connectionDragStop', function () {
        self.undoManager.endTransaction();
      });

      this.instance.bind('connectionDrag', function (connection) {
        self.deselectAll();

        if (connection.source) {
          var $source = $(connection.source);
          self.setNodeSelected($source, true);

          if (self.isChainMode()) {
            Node.setChainSource($source, true);
          }
        }
      });

      this.instance.bind('beforeDrop', function (params) {
        var source = Node.get(document.getElementById(params.sourceId));
        var target = Node.get(document.getElementById(params.targetId));
        var result = self.connect(source, target, true);

        if (result) {
          return true;
        }

        if (result === false) { // Constraint violated.
          Dialogs.connectionNotAllowed();
        }

        return false;
      });

      this.$footnotesContainer = $('#footnotes-container ol');
      this.isFootnotesEnabled = isFootnotesEnabled;

      this.undoManager = new UndoManager(this, {
        onChange: function () {
          Ui.setUndoButtonEnabled(this.canUndo());
          self.processRecipientsChains();
          self.autofillStopNodesIfPossible();
          self.autoConvertMailingNodesTypeIfPossible();
        }
      });

      EditorsHelper.assignOptions({
        getUndoManager: () => self.getUndoManager(),

        getNodesByTypes: types => self.getNodesByTypes(types),

        getNodesByIncomingConnections: function (node) {
          return self.getNodeIncomingConnections(node)
            .map(function (connection) {
              return connection.source;
            });
        },

        getNodesByOutgoingConnections: function (node) {
          return self.getNodeOutgoingConnections(node)
            .map(function (connection) {
              return connection.target;
            });
        },

        getFirstIncomingChain: node => self.getFirstIncomingChain(node),

        forEachPreviousNode: (node, callback) => self.forEachPreviousNode(node, callback),

        getNodeAnchorsInUse: node => self.getAnchorsInUse(node),

        onChange: function (node) {
          self.updateNodeTitle(node, true);
          self.updateFootnotes();
          self.updateMinimap();
        },

        supplement: function (node /*, mailingEditorBase, mailingContent */) {
          self.supplement.apply(self, arguments);
        },

        editNode: function (node, needCheckActivation) {
          alert('Missing editorsHelper.editNode');
        },

        deleteNodeById: function (id) {
          alert('Missing editorsHelper.deleteNodeById');
        }
      });

      if (isContextMenuEnabled) {
        ContextMenu.create(Editor.contextMenuOptions(self));
      }
    }

    static defaults($container) {
      return {
        Container: $container,
        Anchor: [
          [0, 0.5, -1, 0],
          [0.5, 0, 0, -1],
          [1, 0.5, 1, 0],
          [0.5, 1, 0, 1]
        ],
        DragOptions: {
          cursor: 'pointer',
          zIndex: Def.Z_INDEX_JSPLUMB_DRAG_OPTIONS
        },
        Endpoint: 'Blank',
        Connector: ['FixedBezierConnector'],
        PaintStyle: {
          stroke: Def.CONNECTION_COLOR,
          strokeWidth: Def.CONNECTION_THICKNESS,
          outlineStroke: Def.CONNECTION_OUTLINE_COLOR,
          outlineWidth: Def.CONNECTION_OUTLINE_WIDTH
        },
        HoverPaintStyle: {
          stroke: Def.CONNECTION_HOVER_COLOR,
          strokeWidth: Def.CONNECTION_THICKNESS,
          outlineStroke: Def.CONNECTION_OUTLINE_COLOR,
          outlineWidth: Def.CONNECTION_OUTLINE_WIDTH
        },
        ConnectionOverlays: [['PlainArrow', {
          id: 'arrow',
          visible: true,
          location: 1,
          width: Def.CONNECTION_ARROW_SIZE,
          length: Def.CONNECTION_ARROW_SIZE
        }]]
      }
    }

    static contextMenuOptions(self) {
      return {
        '.jtk-connector': {
          shown: () => self.editable,
          items: {
            'delete': {
              name: t('workflow.defaults.delete'),
              icon: 'delete',
              disabled: function ($connection) {
                return !self.canDeleteConnection(self.getConnectionByConnector$($connection));
              },
              clicked: function ($connection) {
                self.undoManager.transaction(() => {
                  const connection = self.getConnectionByConnector$($connection);
                  self.deleteConnection(connection.origin);
                });
              }
            }
          }
        },
        '.node': {
          shown: () => true,
          items: {
            'connect': {
              name: t('workflow.connect'),
              icon: 'connect',
              shown: () => self.editable && self.getSelectionSize() > 1,
              clicked: function () {
                self.undoManager.transaction(() => {
                  if (self.getSelectionSize() == 2 && self.multiConnectionEnabled) {
                    self.connectBetweenSelected();
                  } else {
                    self.connectSelected();
                  }
                });
              }
            },
            'disconnect': {
              name: t('workflow.disconnect'),
              icon: 'disconnect',
              shown: () => self.editable && self.getSelectionSize() > 1,
              clicked: function () {
                self.undoManager.transaction(() => {
                  self.disconnectSelected();
                });
              }
            },
            'edit': {
              name: t('workflow.defaults.edit'),
              icon: 'pen',
              shown: () => self.getSelectionSize() <= 1,
              clicked: $element => self.editIcon(Node.get($element)),
              disabled: $element => !Node.get($element).isEditable()
            },
            'delete': {
              name: t('workflow.defaults.delete'),
              icon: 'trash-alt',
              clicked: function () {
                self.undoManager.transaction(() => self.deleteSelected());
              },
              disabled: () => !self.editable
            },
            'comment': {
              name: t('workflow.defaults.comment'),
              icon: 'comment-dots',
              clicked: $element => self.editIconComment(Node.get($element)),
              disabled: () => !self.editable
            }
          }
        }
      }
    }

    updateFootnotes = _.throttle(() => {
      this.forEachNode(node => node.setFootnote(-1));
      this.$footnotesContainer.html('');

      if (this.isFootnotesEnabled) {
        var self = this;
        var index = 1;
        this.forEachNode(function (node) {
          if (Node.isCommented(node)) {
            self.$footnotesContainer.append('<li id="#fn:' + index + '">' + node.getComment() + '</li>');
            node.setFootnote(index);
            index++;
          }
        });
      }
    }, 150);

    isActionBasedWorkflow() {
      return this.getNodes().some(node => node.getType() === Def.NODE_TYPE_ACTION_BASED_MAILING);
    }

    isDateBasedWorkflow() {
      return this.getNodes().some(node => node.getType() === Def.NODE_TYPE_DATE_BASED_MAILING);
    }

    isNormalWorkflow() {
      return !this.isActionBasedWorkflow() && !this.isDateBasedWorkflow();
    }

    getNodes$() {
      return this.$container.find('.node');
    }

    getNodes() {
      return this.getNodes$()
        .get()
        .map($node => Node.get($node));
    }

    getVertices() {
      return Vertex.verticesFrom(this.getNodes(), this.instance.getAllConnections());
    }

    getNodesByType(type) {
      return this.getNodesByTypes([type]);
    }

    getNodesByTypes(types) {
      return this.getNodes().filter(node => types.includes(node.type));
    }

    forEachNode$(callback) {
      const $nodes = this.getNodes$();
      const self = this;

      $nodes.each(function () {
        callback.call(self, $(this));
      });
    }

    forEachNode(callback) {
      this.forEachNode$($node => callback.call(this, Node.get($node)));
    }

    forEachPreviousNode(target, callback) {
      var connections = this.getConnections();
      var layer = connections.filter(function (connection) {
        return connection.target === target;
      }).map(connection => connection.source);

      var visitedIds = {};

      while (layer.length) {
        for (var i = 0; i < layer.length; i++) {
          var node = layer[i];
          var nodeId = node.getId();

          var result = callback.call(this, node);
          if (result === false) {
            return;
          }

          visitedIds[nodeId] = true;
        }

        layer = connections.filter(function (connection) {
          if (layer.includes(connection.target)) {
            var nodeId = connection.source.getId();
            if (visitedIds[nodeId]) {
              return false;
            } else {
              visitedIds[nodeId] = true;
              return true;
            }
          } else {
            return false;
          }
        }).map(connection => connection.source);
      }
    }

    getNodeIncomingChains(node) {
      var vertices = this.getVertices();
      var target = vertices.find(vertex => vertex.node == node);
      var chains = [];

      if (target) {
        return Vertex.getIncomingChains(target);
      }

      return chains;
    }

    getNodeOutgoingChains(node) {
      var vertices = this.getVertices();
      var target = vertices.find(vertex => vertex.node == node);
      var chains = [];

      if (target) {
        return Vertex.getOutgoingChains(target);
      }

      return chains;
    }

    getFirstOutgoingChain(node) {
      const chains = this.getNodeOutgoingChains(node);
      if (chains.length) {
        return chains[0];
      }
      return [];
    }

    getFirstIncomingChain(node) {
      var chains = this.getNodeIncomingChains(node);
      if (chains.length) {
        return chains[0];
      }
      return [];
    }

    getNodeIncomingConnections(node) {
      return this.getConnections(connection => Node.get(connection.target) === node);
    }

    getNodeOutgoingConnections(node) {
      return this.getConnections(connection => Node.get(connection.source) === node);
    }

    getNodeConnections(node) {
      return this.getConnections(connection => Node.get(connection.source) === node || Node.get(connection.target) === node);
    }

    isNodeConnected(node) {
      return this.getNodeConnections(node).length > 0
    }

    hasNodeIncomingConnections(node) {
      return this.instance.getAllConnections()
        .some(connection => Node.get(connection.target) === node);
    }

    hasNodeOutgoingConnections(node) {
      return this.instance.getAllConnections()
        .some(connection => Node.get(connection.source) === node);
    }

    hasConnection(predicate) {
      return this.instance.getAllConnections()
        .some(function (connection) {
          return predicate.call(null, {
            origin: connection,
            source: Node.get(connection.source),
            target: Node.get(connection.target)
          });
        });
    }

    getConnections(filter) {
      var connections = this.instance.getAllConnections();

      if (filter) {
        connections = connections.filter(filter);
      }

      return connections.map(function (connection) {
        return {
          origin: connection,
          source: Node.get(connection.source),
          target: Node.get(connection.target)
        }
      });
    }

    getConnectionByConnector$($connector) {
      return this.getConnections(connection => connection.canvas == $connector[0])[0];
    }

    getConnectionBetween($source, $target) {
      return this.getConnections(connection => $(connection.source).is($source) && $(connection.target).is($target))[0];
    }

    recycle() {
      this.instance.reset();
      this.$container.empty();
    }

    batch() {
      this.instance.batch.apply(this.instance, arguments);
    }

    newNode(type, position) {
      var self = this;

      if (this.editable) {
        if (Node.isExpandableType(type)) {
          const newUnchangedWorkflow = Def.workflowId === 0 && self._isInInitialState();
          switch (type) {
            case Def.NODE_TYPE_SC_BIRTHDAY:
            case Def.NODE_TYPE_SC_DOI:
            case Def.NODE_TYPE_BIRTHDAY_WITH_COUPON:
            case Def.NODE_TYPE_WELCOME_TRACK:
            case Def.NODE_TYPE_WELCOME_TRACK_WITH_INCENTIVE:
            case Def.NODE_TYPE_SHOPPING_CART_ABANDONERS_SMALL:
            case Def.NODE_TYPE_SHOPPING_CART_ABANDONERS_LARGE:
            case Def.NODE_TYPE_ANNIVERSARY_MAIL:
              if (Def.workflowId === 0 && this._isInInitialState()) {
                this.deleteAllNodes();
              }

              this.newSnippetFromSample(type, position);
              break;
            case Def.NODE_TYPE_SC_ABTEST:
              Dialogs.createAutoOpt().done(mailingsCount => {
                if (newUnchangedWorkflow) {
                  self.deleteAllNodes();
                }
                Snippets.loadAutoOptSample(mailingsCount, this.gridBackgroundShown,
                  (nodes, connections) => this.newSnippet(nodes, connections, position));
              });
              break;
            case Def.NODE_TYPE_OWN_WORKFLOW:
              Dialogs.confirmOwnWorkflowExpanding().done(function (params) {
                if (newUnchangedWorkflow) {
                  self.deleteAllNodes();
                }
                self.newSnippetFromOwnWorkflow(params.workflowId, params.copyContent, position);
              });
              break;
          }
        } else {
          var node = Node.create(type);
          var animation = Def.NODE_ANIMATION_DROPPED;

          if (!position) {
            position = this.pickNewNodePosition();
            animation = Def.NODE_ANIMATION_AUTO_DROPPED;
          }

          node.setCoordinates(position.x, position.y);

          self.add(node, animation);
          self.supplement(node);

          return node;
        }
      }
    }

    _isInInitialState() {
      return Def.intialSchema === this.serializeIcons();
    }

    newSnippetFromSample(type, position) {
      Snippets.loadSample(type, this.gridBackgroundShown,
        (nodes, connections) => this.newSnippet(nodes, connections, position));
    }

    newSnippetFromOwnWorkflow(workflowId, copyContent, position) {
      Snippets.loadOwnWorkflow(workflowId, copyContent, (nodes, connections) => {
        if (this.gridBackgroundShown) {
          nodes.forEach(node => this.convertNodeCoordinates(node));
        }
        this.newSnippet(nodes, connections, position);
      });
    }

    newSnippet(nodes, connections, position) {
      var self = this;

      var animation = position ? Def.NODE_ANIMATION_DROPPED : Def.NODE_ANIMATION_AUTO_DROPPED;
      var removeStart = this.getNodesByTypes([Def.NODE_TYPE_START]).length > 0;
      var removeStop = this.getNodesByTypes([Def.NODE_TYPE_STOP]).length > 0;

      Snippets.removeStartStop(nodes, connections, removeStart, removeStop);

      if (position) {
        Snippets.adjustPositions(nodes, position.x, position.y, true);
      } else {
        position = this.getCenterPosition();
        Snippets.adjustPositions(nodes, position.x, position.y, false);
      }

      this.undoManager.transaction(() => {
        self.batch(function () {
          var allocatedIds = self.allocateIds(nodes.length);

          self.allocateSpaceForSnippet(nodes);

          nodes.forEach(function (node, nodeIndex) {
            node.setId(allocatedIds[nodeIndex]);
            self.add(node, animation);
          });

          connections.forEach(function (connection) {
            self.connect(connection.source, connection.target);
          });
        });
      });
    }

    allocateId() {
      return this.allocateIds(1)[0];
    }

    allocateIds(count) {
      var newIds = [];
      var ids = {};

      this.forEachNode(function (node) {
        ids[node.getId()] = true;
      });

      while (count > 0) {
        do {
          this.lastAllocatedId++;
        } while (ids[this.lastAllocatedId]);

        newIds.push(this.lastAllocatedId);
        count--;
      }

      return newIds;
    }

    _findMaxNodeId() {
      var maxId = 0;

      this.forEachNode(function (node) {
        if (node.getId() > maxId) {
          maxId = node.getId();
        }
      });

      return maxId;
    }

    pickNewNodePosition() {
      var area = this.canvas.getVisibleArea();
      var areaMinY = Math.ceil(area.minY / Def.CANVAS_GRID_SIZE);
      var areaMaxY = Math.floor(area.maxY / Def.CANVAS_GRID_SIZE) - (Def.AUTO_DROP_STEP_Y >> 1);
      var positionRangesInUse = [];
      var result;

      this.forEachNode(function (node) {
        var coordinates = node.getCoordinates();

        positionRangesInUse.push({
          minX: coordinates.x - Def.AUTO_DROP_STEP_X + 1,
          minY: coordinates.y - Def.AUTO_DROP_STEP_Y + 1,
          maxX: coordinates.x + Def.AUTO_DROP_STEP_X - 1,
          maxY: coordinates.y + Def.AUTO_DROP_STEP_Y - 1
        });
      });

      var testOne = function (posX, posY) {
        if (posY < areaMinY || posY > areaMaxY) {
          return false;
        }

        var empty = !positionRangesInUse.some(function (ranges) {
          // Is within?
          return !(posX < ranges.minX || posX > ranges.maxX || posY < ranges.minY || posY > ranges.maxY);
        });

        if (empty) {
          return {x: posX, y: posY};
        }

        return false;
      }

      var test = function (/* posX1, posY1, posX2, posY2, ... */) {
        for (var index = 0; index + 1 < arguments.length; index += 2) {
          var res = testOne(arguments[index], arguments[index + 1]);
          if (res) {
            return res;
          }
        }

        return false;
      }

      var centerX = Math.round((area.minX + area.maxX - Def.NODE_SIZE) / 2 / Def.CANVAS_GRID_SIZE);
      var centerY = Math.round((area.minY + area.maxY - Def.NODE_SIZE) / 2 / Def.CANVAS_GRID_SIZE);

      result = test(centerX, centerY);
      if (result) {
        return result;
      }

      var minX = centerX;
      var maxX = centerX;
      var minY = centerY;
      var maxY = centerY;

      var step = 0;

      while (true) {
        // middles
        //    2
        //  1 X 4
        //    3
        result = test(
          minX - Def.AUTO_DROP_STEP_X, centerY,
          centerX, minY - Def.AUTO_DROP_STEP_Y,
          centerX, maxY + Def.AUTO_DROP_STEP_Y,
          maxX + Def.AUTO_DROP_STEP_X, centerY
        );

        if (result) {
          return result;
        }

        for (var n = 1; n <= step; n++) {
          var nX = n * Def.AUTO_DROP_STEP_X;
          var nY = n * Def.AUTO_DROP_STEP_Y;

          // between middles and corners
          //    3 X 4
          //  1 X X X 7
          //  X X X X X
          //  2 X X X 8
          //    5 X 6
          result = test(
            minX - Def.AUTO_DROP_STEP_X, centerY - nY,
            minX - Def.AUTO_DROP_STEP_X, centerY + nY,
            centerX - nX, minY - Def.AUTO_DROP_STEP_Y,
            centerX + nX, minY - Def.AUTO_DROP_STEP_Y,
            centerX - nX, maxY + Def.AUTO_DROP_STEP_Y,
            centerX + nX, maxY + Def.AUTO_DROP_STEP_Y,
            maxX + Def.AUTO_DROP_STEP_X, centerY - nY,
            maxX + Def.AUTO_DROP_STEP_X, centerY + nY
          );

          if (result) {
            return result;
          }
        }

        // corners
        //  1 X 3
        //  X X X
        //  2 X 4
        result = test(
          minX - Def.AUTO_DROP_STEP_X, minY - Def.AUTO_DROP_STEP_Y,
          minX - Def.AUTO_DROP_STEP_X, maxY + Def.AUTO_DROP_STEP_Y,
          maxX + Def.AUTO_DROP_STEP_X, minY - Def.AUTO_DROP_STEP_Y,
          maxX + Def.AUTO_DROP_STEP_X, maxY + Def.AUTO_DROP_STEP_Y
        );

        if (result) {
          return result;
        }

        minX -= Def.AUTO_DROP_STEP_X;
        maxX += Def.AUTO_DROP_STEP_X;
        minY -= Def.AUTO_DROP_STEP_Y;
        maxY += Def.AUTO_DROP_STEP_Y;
        step++;
      }
    }

    getCenterPosition() {
      var area = this.canvas.getVisibleArea();

      var centerX = Math.round((area.minX + area.maxX - Def.NODE_SIZE) / 2 / Def.CANVAS_GRID_SIZE);
      var centerY = Math.round((area.minY + area.maxY - Def.NODE_SIZE) / 2 / Def.CANVAS_GRID_SIZE);

      return {x: centerX, y: centerY};
    }

    // returns an array of the filled start icons connected with provided node
    getLinkedFilledStartNodes(node) {
      return _.compact(this.getNodeIncomingChains(node)
        .map(function (chain) {
          return _.find(chain, node => node.getType() === Def.NODE_TYPE_START);
        })).filter(startNode => startNode.isFilled());
    }

    isAutoOptimizationDecisionAllowed(node) {
      const filledStartNodes = this.getLinkedFilledStartNodes(node);
      if (filledStartNodes.length === 0) {
        return true;
      }

      return filledStartNodes.every(startNode => Def.constants.startTypeEvent !== startNode.data.startType);
    }

    // autofill of the stop node is possible when all linked filled start nodes has normal date start
    isAllLinkedFilledStartsHasDateType(node) {
      const filledStartNodes = this.getLinkedFilledStartNodes(node);
      return !!filledStartNodes.length && filledStartNodes.every(function (startNode) {
        return Def.constants.startTypeDate === startNode.data.startType;
      });
    }

    autofillStopNodesIfPossible() {
      const self = this;
      this.getNodesByTypes([Def.NODE_TYPE_STOP]).forEach(function (stopNode) {
        self.autofillAutomaticEndIfPossible(stopNode);
      });
    }

    autofillAutomaticEndIfPossible(stopNode) {
      if (!this.isAllLinkedFilledStartsHasDateType(stopNode)) {
        return;
      }
      stopNode.data.endType = Def.constants.endTypeAutomatic;
      stopNode.setFilled(true);
      this.updateNodeTitle(stopNode, true);
    }

    // Converting of the mailing types to action-based type is possible
    // when all filled start nodes has event start type and reaction event
    _possibleToAutoConvertMailingsTypeToActionBased(mailingNode) {
      const filledStartNodes = this.getLinkedFilledStartNodes(mailingNode);
      return !!filledStartNodes.length && filledStartNodes.every(function (startNode) {
        return Def.constants.startTypeEvent === startNode.data.startType
          && "EVENT_REACTION" === startNode.data.event;
      });
    }

    // Converting of the mailing types to action-based type is possible
    // when all filled start nodes has event start type and date event
    _possibleToAutoConvertMailingsTypeToDateBased(mailingNode) {
      const filledStartNodes = this.getLinkedFilledStartNodes(mailingNode);
      return !!filledStartNodes.length && filledStartNodes.every(function (startNode) {
        return Def.constants.startTypeEvent === startNode.data.startType
          && "EVENT_DATE" === startNode.data.event;
      });
    }

    autoConvertMailingNodesTypeIfPossible() {
      const self = this;
      const mailingNodes = [
        Def.NODE_TYPE_MAILING,
        Def.NODE_TYPE_ACTION_BASED_MAILING,
        Def.NODE_TYPE_DATE_BASED_MAILING
      ];
      this.getNodesByTypes(mailingNodes).forEach(function (mailingNode) {
        const newType = self._getAutoConvertedMailingTypeIfPossible(mailingNode);
        if (mailingNode.type === newType) {
          return; // exit if type change not possible
        }
        mailingNode.type = newType;
        if (mailingNode.isFilled()) {
          mailingNode.data.mailingId = 0;
          mailingNode.setFilled(false);
          self.updateNodeTitle(mailingNode, true);
        }
        Messages.warn('workflow.mailing.typeChanged');
      });
    }

    // Automatic converting of the mailing type is possible
    // when all linked filled start icons has same type [normal, action_based or date_based]
    _getAutoConvertedMailingTypeIfPossible(mailingNode) {
      if (this.isAllLinkedFilledStartsHasDateType(mailingNode)) {
        return Def.NODE_TYPE_MAILING;
      }
      if (this._possibleToAutoConvertMailingsTypeToActionBased(mailingNode)) {
        return Def.NODE_TYPE_ACTION_BASED_MAILING;
      }
      if (this._possibleToAutoConvertMailingsTypeToDateBased(mailingNode)) {
        return Def.NODE_TYPE_DATE_BASED_MAILING;
      }
      return mailingNode.type; // auto converting not possible
    }

    allocateSpaceForSnippet(snippetNodes) {
      const self = this;

      function detectCollision(node1, node2) {
        const a = node1.getCoordinates();
        const b = node2.getCoordinates();

        return Math.abs(a.x - b.x) < Def.AUTO_DROP_STEP_X && Math.abs(a.y - b.y) < Def.AUTO_DROP_STEP_Y;
      }

      function detectGroupsCollision(group1, group2) {
        return group1.some(node1 => group2.some(node2 => detectCollision(node1, node2)));
      }

      function getGroupCenterPosition(group) {
        let minX = 0, maxX = 0, minY = 0, maxY = 0;

        group.forEach(node => {
          minX = Math.min(minX, node.getX());
          maxX = Math.max(maxX, node.getX());
          minY = Math.min(minY, node.getY());
          maxY = Math.max(maxY, node.getY());
        });

        return {
          x: (maxX - minX) / 2,
          y: (maxY - minY) / 2
        }
      }

      function getMoveDirection(currentPosition, relativeTo) {
        if (Math.abs(currentPosition.x - relativeTo.x) >= Math.abs(currentPosition.y - relativeTo.y)) {
          // LEFT : RIGHT
          return currentPosition.x < relativeTo.x ? {x: -Def.AUTO_DROP_STEP_X, y: 0} : {x: Def.AUTO_DROP_STEP_X, y: 0};
        } else {
          // TOP : BOTTOM
          return currentPosition.y < relativeTo.y ? {x: 0, y: -Def.AUTO_DROP_STEP_Y} : {x: 0, y: Def.AUTO_DROP_STEP_Y};
        }
      }

      function moveNodes(nodes, direction) {
        nodes.forEach(node => {
          const coordinates = node.getCoordinates();
          node.setCoordinates(coordinates.x + direction.x, coordinates.y + direction.y);
        });
      }

      if (snippetNodes.length) {
        const allExistingNodes = this.getNodes();

        // First collect all (let's presume all the nodes are to be moved).
        // Then we will check what's affected and what's not.
        var nodesMovedLog = allExistingNodes.map(node => {
          return {node: node, coordinates: node.getCoordinates(), dragMode: self.getCurrentDragMode()};
        });

        const snippetCenterPosition = getGroupCenterPosition(snippetNodes);

        const allExistingGroups = VertexGroup.detectGroups(this.getVertices())
          .map(group => {
            const nodes = group.getNodes();

            return {
              nodes: nodes,
              moveDirection: getMoveDirection(getGroupCenterPosition(nodes), snippetCenterPosition)
            }
          });

        allExistingGroups.forEach(group => {
          if (detectGroupsCollision(group.nodes, snippetNodes)) {
            const direction = group.moveDirection;
            const nodesToMove = group.nodes.slice(0);
            const nodesToKeep = snippetNodes.slice(0);

            let otherGroups = allExistingGroups.filter(g => g !== group);

            do {
              moveNodes(nodesToMove, direction);

              let jointGroupDetected;

              do {
                jointGroupDetected = false;

                otherGroups = otherGroups.filter(otherGroup => {
                  if (detectGroupsCollision(nodesToMove, otherGroup.nodes)) {
                    if (otherGroup.moveDirection.x === direction.x && otherGroup.moveDirection.y === direction.y) {
                      moveNodes(otherGroup.nodes, direction);
                      Array.prototype.push.apply(nodesToMove, otherGroup.nodes);
                      jointGroupDetected = true;
                    } else {
                      Array.prototype.push.apply(nodesToKeep, otherGroup.nodes);
                    }
                    return false;
                  } else {
                    return true;
                  }
                });
              } while (jointGroupDetected);
            } while (detectGroupsCollision(nodesToMove, nodesToKeep));
          }
        });

        // Ignore nodes that haven't been actually moved.
        nodesMovedLog = nodesMovedLog.filter(entry => {
          const newCoordinates = entry.node.getCoordinates();
          const oldCoordinates = entry.coordinates;

          return newCoordinates.x !== oldCoordinates.x || newCoordinates.y !== oldCoordinates.y;
        });

        if (nodesMovedLog.length) {
          this.undoManager.operation('nodeMoved', nodesMovedLog);
        }
      }
    }

    allocateSpaceForSupplement(node, direction, occupiedPositionRanges = this.#getOccupiedPositionRanges()) {
      let deltaX = 0;
      let deltaY = 0;

      switch (direction) {
        case Def.LEFT:
        default:
          deltaX = -1;
          break;

        case Def.RIGHT:
          deltaX = +1;
          break;

        case Def.TOP:
          deltaY = -1;
          break;

        case Def.BOTTOM:
          deltaY = +1;
          break;
      }

      // Immediately make a big enough step to avoid collision with reference node.
      let newX = node.getX() + Def.AUTO_DROP_STEP_X * deltaX;
      let newY = node.getY() + Def.AUTO_DROP_STEP_Y * deltaY;

      while (!this.#canPlaceNodeAtPosition(newX, newY, occupiedPositionRanges)) {
        // Make little steps towards chosen direction until no collision is there.
        newX += deltaX;
        newY += deltaY;
      }

      return {x: newX, y: newY};
    }

    #getOccupiedPositionRanges(exceptNodes = []) {
      return this.getNodes()
        .filter(n => !exceptNodes.includes(n))
        .map(node => {
          const coordinates = node.getCoordinates();
          return {
            minX: coordinates.x - Def.AUTO_DROP_STEP_X + 1,
            minY: coordinates.y - Def.AUTO_DROP_STEP_Y + 1,
            maxX: coordinates.x + Def.AUTO_DROP_STEP_X - 1,
            maxY: coordinates.y + Def.AUTO_DROP_STEP_Y - 1
          };
        });
    }

    #canPlaceNodeAtPosition(posX, posY, occupiedPositionRanges) {
      return !occupiedPositionRanges.some(ranges => {
        // Is within?
        return !(posX < ranges.minX || posX > ranges.maxX || posY < ranges.minY || posY > ranges.maxY);
      });
    }

    allocateSpaceForIncomingSupplement(target) {
      const anchorsInUse = this.getAnchorsInUse(target);
      let direction = Def.LEFT;

      if (!anchorsInUse[Def.LEFT] && anchorsInUse[Def.TOP]) {
        direction = Def.TOP;
      }

      return this.allocateSpaceForSupplement(target, direction);
    }

    allocateSpaceForOutgoingSupplement(source) {
      const anchorsInUse = this.getAnchorsInUse(source);
      let direction = Def.RIGHT;

      if (!anchorsInUse[Def.RIGHT] && anchorsInUse[Def.BOTTOM]) {
        direction = Def.BOTTOM;
      }

      return this.allocateSpaceForSupplement(source, direction);
    }

    add(node, animation) {
      var self = this;
      var $node = node.get$();

      if (!node.getId()) {
        node.setId(this.allocateId());
      }

      node.appendTo(this.$container, this.$titlesContainer, animation);
      node.setInteractionEnabled(this.editable);
      node.updateCommentLabels();

      this.instance.makeTarget($node, {anchor: node.getAnchors()});
      this.instance.makeSource($node, {anchor: node.getAnchors(), filter: '.node-connect-button svg'});

      this.initDraggableOptions($node);

      var wasSelected;

      $node.on({
        mousedown: function (event) {
          if (event.button === Def.MOUSE_BUTTON_MIDDLE) {
            if (self.editable) {
              self.undoManager.transaction(() => self.deleteNode($node));
            }
          } else {
            node.wasDragging = false;
            wasSelected = Node.isSelected($node);

            if (!wasSelected) {
              if (self.isChainMode()) {

                if (!self.isChainSourceSelected()) {
                  //mark the node as source if any node selected as source
                  self.setNodeSelected($node, true);
                  Node.setChainSource($node, true);
                } else {
                  var source = self.getChainSource();

                  _.defer(function () {
                    // Use defer function to make sure jsplumb will fire connection event synchronously
                    // (otherwise transaction of undo manager will be broken).

                    self.undoManager.transaction(() => {
                      var connected = self.connect(Node.get(source), node);

                      //reset selection after a successful connection, otherwise source stays the same
                      if (connected) {
                        self.deselectAll();

                        self.setNodeSelected($node, true);
                        Node.setChainSource($node, true);
                      } else if (connected === false) { // Constraint violated.
                        Dialogs.connectionNotAllowed();
                      }
                    });
                  });
                }
              } else if (!event.shiftKey && !event.ctrlKey) {
                self.deselectAll();
                self.setNodeSelected($node, true);
              } else {
                self.setNodeSelected($node, true);
              }
            }
          }

          Ui.blurInputFields();
        },
        mouseup: function (event) {
          if (wasSelected && !node.wasDragging) {
            if (self.isChainMode()) {
              self.setNodeSelected($node, false);
              Node.setChainSource($node, false);
            } else if (event.shiftKey || event.ctrlKey) {
              self.setNodeSelected($node, false);
            } else {
              self.deselectAll();
              self.setNodeSelected($node, true);
            }
          }
        },
        dblclick: () => this.editIcon(node),
        mouseenter: () => node.setHovered(true),
        mouseleave: () => node.setHovered(false)
      });

      this.undoManager.operation('nodeAdded', node);
      this.updateMinimap();
      this.updateFootnotes();
      this.updateDraggableButtons();
    }

    supplement(node, mailingEditorBase, mailingContent) {
      var self = this;

      if (node.isFilled()) {
        if (Def.NODE_TYPES_MAILING.includes(node.getType()) && !Utils.isPausedWorkflow()) {
          const finalParameterNode = this._findFinalParameterNode(node);
          if (finalParameterNode) {
            this._fillOptimizationFinalMailingIfPossible(finalParameterNode);
          }

          mailingEditorBase.trySupplementChain(this.getFirstIncomingChain(node), mailingContent, function (chain) {
            self.batch(function () {
              // First delete connections between node pairs that now have new nodes inserted in between.
              var target = chain[0];  // Edited mailing.
              for (var i = 1; i < chain.length; i++) {
                var previousNode = chain[i];

                // Appended == non-new (existed before supplementing).
                if (previousNode.isAppended()) {
                  target = previousNode;
                } else {
                  // Found a new node (created during supplementing),
                  // now we have to find and disconnect closest non-new nodes around it.
                  for (var j = i; j < chain.length; j++) {
                    var sourceCandidate = chain[j];
                    if (sourceCandidate.isAppended()) {
                      self.deleteConnectionBetween(sourceCandidate.get$(), target.get$());
                      target = sourceCandidate;
                      i = j;
                      break;
                    }
                  }
                }
              }

              // Now we have to allocate appropriate positions for new nodes, add them to editor and connect.
              for (var i = 1; i < chain.length; i++) {
                var previousNode = chain[i];

                if (!previousNode.isAppended()) {
                  var nextNode = chain[i - 1];
                  var position = self.allocateSpaceForIncomingSupplement(nextNode);
                  previousNode.setCoordinates(position.x, position.y);
                  self.add(previousNode);
                  self.connect(previousNode, nextNode);

                  if (i + 1 < chain.length && chain[i + 1].isAppended()) {
                    self.connect(chain[i + 1], previousNode);
                  }

                  EditorsHelper.resave(previousNode);
                }
              }
            });
          });
          this.autoConvertMailingNodesTypeIfPossible();
        } else if (Def.NODE_TYPE_RECIPIENT == node.getType()) {
          this.processRecipientsChains();
        } else if (Def.NODE_TYPE_PARAMETER === node.getType()) {
          const finalParameterNode = self._findFinalParameterNode(node);

          if (finalParameterNode) {
            const optimizationParameterNodes = self._findTestMailingsParamsNodes(finalParameterNode);

            const parameterValue = node.getData().value;
            self._setParameterNodesValue(optimizationParameterNodes, parameterValue);

            const parametersSum = parameterValue * optimizationParameterNodes.length;

            EditorsHelper.modify(finalParameterNode, function (n) {
              n.setFilled(true);
              n.setEditable(false);
              n.getData().value = 100 - parametersSum;
            });
          }
        } else if (Def.NODE_TYPE_START == node.getType()) {
          this.autofillStopNodesIfPossible();
          this.autoConvertMailingNodesTypeIfPossible();
        }
      } else {
        // Deadline icon is required right after an import icon (import -> deadline).
        if (Def.NODE_TYPE_IMPORT == node.getType()) {
          // Let's presume that an import icon is either followed by a deadline icon or by nothing.
          var isDeadlineMissing = !this.hasNodeOutgoingConnections(node);

          if (isDeadlineMissing) {
            var deadline = Node.create(Def.NODE_TYPE_DEADLINE);
            var position = this.allocateSpaceForOutgoingSupplement(node);

            deadline.setCoordinates(position.x, position.y);

            this.add(deadline);
            this.connect(node, deadline);
          }
        }
      }
    }

    isAutoOptimizationWorkflow(startNode) {
      const finalParameterNode = this._findFinalParameterNode(startNode);
      if (!finalParameterNode) {
        return false;
      }

      const finalMailingNode = this._findOptimizationFinalMailingNode(finalParameterNode);
      if (!finalMailingNode) {
        return false;
      }

      const decisionNode = this.getFirstIncomingChain(finalParameterNode).find(function (node) {
        return node.getType() === Def.NODE_TYPE_DECISION;
      })

      return !!decisionNode && decisionNode.data.decisionType === Def.constants.decisionTypeAutoOptimization;
    }

    /**
     * Prepare list of available options for select with split value depending on count of test mailings.
     * For example:
     *  1) If A/B campaign has 2 test mailings, that max split value can be 33%
     *  2) If A/B campaign has 3 test mailings, that max split value can be 25%
     * @param node that will be configured
     * @returns list of options for select element, in format {id: value, text: value}
     */
    getParametersOptions(node) {
      const finalParameterNode = this._findFinalParameterNode(node);

      var splitValues = Def.SPLIT_DEFAULT_PARAMETERS;

      if (finalParameterNode) {
        const optimizationParameterNodes = this._findTestMailingsParamsNodes(finalParameterNode);
        const paramsNodesCount = optimizationParameterNodes.length + 1; // + final parameter node

        const maxSplitValue = Math.floor(100 / paramsNodesCount);

        splitValues = splitValues.filter(function (value) {
          return value <= maxSplitValue;
        });
      }

      return splitValues.map(function (value) {
        return {id: value, text: value};
      });
    }

    /**
     * If all of the test mailing of A/B campaign is filled, than it make final mailing icon active.
     * @param finalParameterNode - node of final parameter of A/B campaign
     */
    _fillOptimizationFinalMailingIfPossible(finalParameterNode) {
      if (!this._isAllOptimizationTestMailingsFilled(finalParameterNode)) {
        return;
      }

      const finalMailingNode = this._findOptimizationFinalMailingNode(finalParameterNode);
      const title = finalMailingNode.title;

      EditorsHelper.modify(finalMailingNode, function (n) {
        n.setFilled(true);
      });

      finalMailingNode.setTitle(title);
    }

    _findFinalParameterNode(node) {
      const outgoingChains = this.getNodeOutgoingChains(node);

      for (var i = 0; i < outgoingChains.length; i++) {
        const chain = outgoingChains[i];

        const finalParameterNode = chain.find(function (_node, index) {
          return index > 0 && Def.NODE_TYPE_PARAMETER === _node.getType();
        });

        if (finalParameterNode) {
          return finalParameterNode;
        }
      }

      return null;
    }

    /**
     * Finds all parameters nodes of test mailings of A/B campaign.
     * @param finalParameterNode - parameter node of final mailing
     */
    _findTestMailingsParamsNodes(finalParameterNode) {
      const optimizationParameterNodes = [];

      this.getNodeIncomingChains(finalParameterNode).forEach(function (incomingChain) {
        const optimizationParameterNode = incomingChain.find(function (node, index) {
          return index > 0 && Def.NODE_TYPE_PARAMETER === node.getType();
        });

        if (optimizationParameterNode) {
          optimizationParameterNodes.push(optimizationParameterNode);
        }
      });

      return optimizationParameterNodes;
    }

    _findOptimizationFinalMailingNode(finalParameterNode) {
      const outgoingChain = this.getFirstOutgoingChain(finalParameterNode);

      return outgoingChain.find(function (node) {
        return node.getType() === Def.NODE_TYPE_MAILING && !node.isEditable();
      })
    }

    /**
     * Checks if all of the test mailings of A/B campaign was filled.
     * @param finalParameterNode - parameter node of final mailing
     */
    _isAllOptimizationTestMailingsFilled(finalParameterNode) {
      const testMailingParametersNodes = this._findTestMailingsParamsNodes(finalParameterNode);

      for (var i = 0; i < testMailingParametersNodes.length; i++) {
        const parameterNode = testMailingParametersNodes[i];

        const outgoingChain = this.getFirstOutgoingChain(parameterNode);

        if (outgoingChain.length <= 1) {
          return false;
        }

        const nextNode = outgoingChain[1];
        if (nextNode.getType() !== Def.NODE_TYPE_MAILING || !nextNode.isFilled() || !nextNode.isEditable()) {
          return false;
        }
      }

      return true;
    }

    _setParameterNodesValue(parameterNodes, value) {
      parameterNodes.forEach(function (optimizationNode) {
        EditorsHelper.modify(optimizationNode, function (node) {
          node.setFilled(true);
          node.getData().value = value;
        });
      });
    }

    processRecipientsChains = _.debounce(() => {
      var initialRecipientsNodes = this._findInitialRecipientsNodes();
      var selfEditor = this;

      initialRecipientsNodes.forEach(function (initNode) {
        initNode.setInRecipientsChain(false);
        selfEditor.updateNodeTitle(initNode);
        var chains = selfEditor.getNodeOutgoingChains(initNode);
        chains.forEach(function (chain) {
          var nodeIndex = 1;
          for (; nodeIndex < chain.length; ++nodeIndex) {
            var node = chain[nodeIndex];
            if (node.type != Def.NODE_TYPE_RECIPIENT) {
              break;
            }
            node.data.mailinglistId = initNode.data.mailinglistId;
            node.setInRecipientsChain(true);
            selfEditor.updateNodeTitle(node);
          }
        });
      });
    }, 150);

    _findInitialRecipientsNodes() {
      var recipientNodes = this.getNodesByTypes([Def.NODE_TYPE_RECIPIENT]);

      var connections = this.getConnections();
      var connectionsTargetsId = [];

      connections.filter(function (connection) {
        return connection.target.type == Def.NODE_TYPE_RECIPIENT && connection.source.type == Def.NODE_TYPE_RECIPIENT;
      }).forEach(function (connection) {
        connectionsTargetsId.push(connection.target.id);
      });

      return recipientNodes.filter(function (node) {
        return !connectionsTargetsId.includes(node.id);
      });
    }

    getAnchorsInUse(node) {
      var $node = node.get$();
      return Node.getAnchorsInUse(this.instance.getEndpoints($node[0]));
    }

    getConnectionDirection(connection) {
      const anchorsInUse = Node.getAnchorsInUse([connection.endpoints[1]]);

      if (anchorsInUse[Def.TOP]) {
        return Def.BOTTOM;
      }

      if (anchorsInUse[Def.BOTTOM]) {
        return Def.TOP;
      }

      if (anchorsInUse[Def.RIGHT]) {
        return Def.LEFT;
      }

      return Def.RIGHT;
    }

    positionTitle(node) {
      NodeTitleHelper.positionTitle(node, this.getAnchorsInUse(node));
    }

    updateNodeTitle(node, forceUpdate) {
      var isNormalWorkflow = this.isNormalWorkflow();
      NodeTitleHelper.updateTitle(node, isNormalWorkflow, forceUpdate);
      this.positionTitle(node);
    }

    updateNodeTitles = _.throttle(forceUpdate => {
      var self = this;
      var isNormalWorkflow = this.isNormalWorkflow();

      this.forEachNode(function (node) {
        NodeTitleHelper.updateTitle(node, isNormalWorkflow, forceUpdate);
        self.positionTitle(node);
      });
    }, 150);

    positionTitles = _.throttle(() => {
      this.forEachNode(node => {
        this.positionTitle(node);
      });
    }, 150);

    setMinimapEnabled(isEnabled) {
      this.canvas.setMinimapEnabled(isEnabled);
    }

    updateMinimap = _.debounce(function () {
      this.canvas.updateMinimap();
    }, 100);

    isChainSourceSelected() {
      return this.$container.find('.node.chain-mode-source').exists();
    }

    getChainSource() {
      return this.$container.find('.node.chain-mode-source');
    }

    setNodeSelected($node, isSelected) {
      if (isSelected) {
        this.instance.addToDragSelection($node);
      } else {
        this.instance.removeFromDragSelection($node);
      }

      Node.setSelected($node, !!isSelected);
      this.onSelectionChanged();
    }

    isConnectionPossible(source, target) {
      if (source instanceof Node && target instanceof Node) {
        // Loops are not allowed.
        if (source === target) {
          return false;
        }

        // Multiple-way connections are not allowed.
        return !this.hasConnection(function (connection) {
          return connection.source === source && connection.target === target || connection.source === target && connection.target === source;
        });
      }

      return false;
    }

    isConnectionAllowed(sourceNode, targetNode, replaceableConnection) {
      var constraints = new ConnectionConstraints(Def.CONNECTION_CONSTRAINTS);
      var self = this;

      var source = {
        node: sourceNode,
        type: sourceNode.getType(),
        connections: {}
      }

      var target = {
        node: targetNode,
        type: targetNode.getType(),
        connections: {}
      }

      Object.defineProperty(source.connections, 'incoming', {
        get: Helpers.caching(node => self.getNodeIncomingConnections(node).filter(c => replaceableConnection?.origin !== c.origin), self, sourceNode)
      });

      Object.defineProperty(source.connections, 'outgoing', {
        get: Helpers.caching(node => self.getNodeOutgoingConnections(node).filter(c => replaceableConnection?.origin !== c.origin), self, sourceNode)
      });

      Object.defineProperty(target.connections, 'incoming', {
        get: Helpers.caching(node => self.getNodeIncomingConnections(node).filter(c => replaceableConnection?.origin !== c.origin), self, targetNode)
      });

      Object.defineProperty(target.connections, 'outgoing', {
        get: Helpers.caching(node => self.getNodeOutgoingConnections(node).filter(c => replaceableConnection?.origin !== c.origin), self, targetNode)
      });

      if (!replaceableConnection) {
        // A stop icon becomes start icon when it gets outgoing connections (unless it has incoming connections).
        if (source.type === Def.NODE_TYPE_STOP && !this.hasNodeIncomingConnections(sourceNode)) {
          source.type = Def.NODE_TYPE_START;
          source.node.setType(Def.NODE_TYPE_START);
          this.updateDraggableButtons();
        }

        // A start icon becomes stop icon when it gets incoming connections (unless it has outgoing connections).
        if (target.type === Def.NODE_TYPE_START && !this.hasNodeOutgoingConnections(targetNode)) {
          target.type = Def.NODE_TYPE_STOP;
          target.node.setType(Def.NODE_TYPE_STOP);
          this.updateDraggableButtons();
        }
      }

      return constraints.check(source, target);
    }

    connect(source, target, isUserDriven) {
      if (this.isConnectionPossible(source, target)) {
        if (!this.isConnectionAllowed(source, target)) {
          return false;
        }

        // Assign proper type to start/stop icon according to its actual role.

        if (source.getType() === Def.NODE_TYPE_STOP) {
          source.setType(Def.NODE_TYPE_START);
        }

        if (target.getType() === Def.NODE_TYPE_START) {
          target.setType(Def.NODE_TYPE_STOP);
        }

        if (isUserDriven !== true) {
          this.instance.connect({source: source.get$(), target: target.get$()});
        }

        return true;
      } else {
        return null;
      }
    }

    deselectAll() {
      this.$container.find('.node.ui-selected').removeClass('ui-selected');
      this.instance.clearDragSelection();

      this.clearSelectedSource();
      this.onSelectionChanged();
    }

    clearSelectedSource() {
      this.$container.find('.node.chain-mode-source').removeClass('chain-mode-source');
    }

    alignAll() {
      this.undoManager.transaction(() => {
        if (this.#performNodesMove(() => AutoAlignment.align(this.getVertices()))) {
          this.positionTitles();
          this.updateMinimap();
        }
      });
    }

    #performNodesMove(moveCallback) {
      const nodes = this.getNodes();

      // Coordinates snapshot before move.
      const oldCoordinatesMap = {};
      nodes.forEach(node => oldCoordinatesMap[node.getId()] = node.getCoordinates());

      this.batch(() => moveCallback());

      const changes = nodes.filter(node => {
        const oldCoordinates = oldCoordinatesMap[node.getId()];
        return oldCoordinates.x !== node.getX() || oldCoordinates.y !== node.getY();
      }).map(node => {
        return {
          node: node,
          coordinates: oldCoordinatesMap[node.getId()],
          dragMode: this.getCurrentDragMode()
        }
      });

      if (changes.length) {
        this.undoManager.operation('nodeMoved', changes);
        return true;
      }

      return false;
    }

    deleteSelected() {
      this.deleteNodes(this.getSelectedNodes());
    }

    deleteNode(nodeToDelete) {
      if (nodeToDelete instanceof Node) {
        //nothing do
      } else {
        nodeToDelete = Node.get(nodeToDelete);
      }

      // Make sure we not remove what's already removed.
      if (nodeToDelete.isAppended()) {
        // Some connected nodes must be deleted together.
        var nodesToDelete = [nodeToDelete];

        // Pair import -> deadline cannot be divorced.
        switch (nodeToDelete.getType()) {
          case Def.NODE_TYPE_IMPORT:
            this.getNodeOutgoingConnections(nodeToDelete)
              .forEach(function (connection) {
                if (Def.NODE_TYPE_DEADLINE == connection.target.getType()) {
                  nodesToDelete.push(connection.target);
                }
              });
            break;

          case Def.NODE_TYPE_DEADLINE:
            this.getNodeIncomingConnections(nodeToDelete)
              .forEach(function (connection) {
                if (Def.NODE_TYPE_IMPORT == connection.source.getType()) {
                  nodesToDelete.push(connection.source);
                }
              });
            break;
        }

        nodesToDelete.forEach(node => {
          EditorsHelper.exitNodeEditorIfActive(node);
          this.undoManager.operation('nodeDeleted', node);
          this.instance.remove(node.get$());
          node.remove();
        });

        this.updateDraggableButtons();
        this.onSelectionChanged();
        this.positionTitles();
        this.updateFootnotes();
        this.updateMinimap();
      }
    }

    deleteAllNodes() {
      this.deleteNodes(this.getNodes());
    }

    deleteNodes(nodes) {
      this.undoManager.transaction(() => {
        this.batch(() => {
          nodes.forEach(node => this.deleteNode(node));
        });
      });
    }

    disconnectSelected() {
      this.disconnectNodes(this.getSelectedNodes());
    }

    disconnectNode(node) {
      var self = this;

      this.getNodeConnections(node).forEach(function (connection) {
        self.deleteConnection(connection);
      });
    }

    disconnectNodes(nodes) {
      var self = this;

      this.batch(function () {
        nodes.forEach(function (node) {
          self.disconnectNode(node);
        });
      });
    }

    canDeleteConnection(connection) {
      var source, target;

      if (connection.origin) {
        source = connection.source;
        target = connection.target;
      } else {
        source = Node.get(connection.source);
        target = Node.get(connection.target);
      }

      return !(Def.NODE_TYPE_IMPORT == source.getType() && Def.NODE_TYPE_DEADLINE == target.getType());
    }

    deleteConnection(connection) {
      if (this.canDeleteConnection(connection)) {
        if (connection.origin) {
          this.instance.deleteConnection(connection.origin);
        } else {
          this.instance.deleteConnection(connection);
        }

        return true;
      }

      return false;
    }

    deleteConnectionBetween($source, $target) {
      const connection = this.getConnectionBetween($source, $target);
      if (connection) {
        this.deleteConnection(connection);
      }
    }

    connectSelected() {
      return this.connectNodes(this.getSelectedNodes());
    }

    connectBetweenSelected() {
      var $nodes = this.getSelectedNodes$();

      if ($nodes.length >= 2) {
        return this.connectNodes(this.getNodesBetweenSelected($nodes[0], $nodes[1]));
      } else {
        return false;
      }
    }

    getNodesBetweenSelected($source, $target) {
      var firstBox = Node.getBox($source);
      var lastBox = Node.getBox($target);

      var regionBox = {
        minX: Math.min(firstBox.minX, lastBox.minX),
        maxX: Math.max(firstBox.maxX, lastBox.maxX),
        minY: Math.min(firstBox.minY, lastBox.minY),
        maxY: Math.max(firstBox.maxY, lastBox.maxY)
      }

      return this.getNodes()
        .filter(function (node) {
          return Node.isInRegion(node.get$(), regionBox);
        });
    }

    findHoveredConnection(connections) {
      return connections?.find(c => c.origin.isHover());
    }

    insertNodeIntoConnection(node, connection, collisionExists = false) {
      if (!this.canInsertNodeIntoConnection(node, connection)) {
        return false;
      }

      const connectionDirection = this.getConnectionDirection(connection.origin);

      this.deleteConnection(connection);
      this.connect(connection.source, node);
      this.connect(node, connection.target);

      if (collisionExists) {
        this.#performNodesMove(() => {
          node.setCoordinates(connection.source.getX(), connection.source.getY());

          const chain = this.getFirstOutgoingChain(node);
          let prevNode = connection.source;

          chain.forEach(_node => {
            const occupiedPositionRanges = this.#getOccupiedPositionRanges(chain.slice(chain.indexOf(_node)));

            if (!this.#canPlaceNodeAtPosition(_node.getX(), _node.getY(), occupiedPositionRanges)) {
              const { x, y } = this.allocateSpaceForSupplement(prevNode, connectionDirection, occupiedPositionRanges);
              _node.setCoordinates(x, y);
            }
            prevNode = _node;
          });
        });
      }

      return true;
    }

    canInsertNodeIntoConnection(node, connection) {
      const allowed = this.isConnectionAllowed(connection.source, node, connection)
        && this.isConnectionAllowed(node, connection.target, connection);

      if (!allowed) {
        Dialogs.connectionNotAllowed();
      }

      return allowed;
    }

    connectNodes(nodes) {
      // sorting by node coordinates
      // (we should connect from left to right and from top to bottom)
      const sortedNodes = nodes.sort((node1, node2) => {
        return node1.getX() < node2.getX() ? -1 :
          node1.getX() > node2.getX() ? 1 :
            node1.getY() < node2.getY() ? -1 :
              node1.getY() > node2.getY() ? 1 : 0;
      });

      let connected = false;
      this.undoManager.transaction(() => {
        this.batch(() => {
          for (let i = 0; i < sortedNodes.length - 1; i++) {
            if (this.connect(sortedNodes[i], sortedNodes[i + 1])) {
              connected = true;
            }
          }
        });
      });

      return connected;
    }

    getSelectedNodes$() {
      var $elements = [];

      this.$container.find('.node.ui-selected')
        .each(function (index, element) {
          $elements.push($(element));
        });

      return $elements;
    }

    getSelectedNodes() {
      return this.getSelectedNodes$()
        .map(function ($node) {
          return Node.get($node);
        });
    }

    getSelectionSize() {
      return this.$container.find('.node.ui-selected').length;
    }

    updateDraggableButtons() {
      if (this.getNodesByType(Def.NODE_TYPE_START).length) {
        this.draggableButtons.changeType(Def.NODE_TYPE_START, Def.NODE_TYPE_STOP);
      } else {
        this.draggableButtons.changeType(Def.NODE_TYPE_STOP, Def.NODE_TYPE_START);
      }
    }

    onSelectionChanged() {
      Ui.setDeleteButtonEnabled(this.getSelectionSize() > 0);
    }

    setOnInitialized(handler) {
      const selfEditor = this;
      const _handler = function () {
        handler.apply(this);
        selfEditor.lastAllocatedId = selfEditor._findMaxNodeId();
      }

      this.instance.ready(_handler);
    }

    setOnZoom(handler) {
      this.canvasZoomHandler = handler;
    }

    setEditable(isEditable) {
      isEditable = !!isEditable;

      if (this.editable !== isEditable) {
        this.editable = isEditable;
        this.draggableButtons.setEnabled(isEditable);
        this.$viewport.selectable(isEditable ? 'enable' : 'disable');
        this.forEachNode(function (node) {
          node.setInteractionEnabled(isEditable);
        });
      }
    }

    getZoom() {
      return this.instance.getZoom();
    }

    setZoom(scale) {
      this.scale = scale;

      this.canvas.setZoom(scale);
      this.instance.setZoom(scale);
      this.gridBackground.setZoom(scale);
    }

    toggleGrid() {
      this.gridBackground.toggle();
      this.dragMode = this.gridBackgroundShown ? Def.GRID_CELL_DRAG_MODE : Def.DEFAULT_DRAG_MODE;
      this.adaptToNewDragMode();
    }

    get gridBackgroundShown() {
      return this.gridBackground.shown;
    }

    convertNodeCoordinates(node) {
      const coordinates = node.getCoordinates();
      this.convertCoordinates(coordinates);
      node.setCoordinates(coordinates.x, coordinates.y);
    }

    /**
     * Converts coordinates from one system to another according the current drag mode
     * @param coordinates - object, that looks like {x: 1, y: 1}
     */
    convertCoordinates(coordinates) {
      const scale = Def.NODE_SIZE / Def.CANVAS_DEFAULT_GRID_SIZE;

      if (this.getCurrentDragMode() === Def.GRID_CELL_DRAG_MODE) {
        coordinates.x = coordinates.x / scale;
        coordinates.y = coordinates.y / scale;
      } else {
        coordinates.x = coordinates.x * scale;
        coordinates.y = coordinates.y * scale;
      }
    }

    adaptToNewDragMode() {
      this.adaptPropertiesToDragMode();
      const self = this;

      this.getNodes$().each(function () {
        self.instance.destroyDraggable(this);
        self.initDraggableOptions($(this));
      });

      this.forEachNode(function (node) {
        self.convertNodeCoordinates(node);
      });
    }

    /**
     * Changes the properties needed to move icons in the editor depending on the current drag mode.
     */
    adaptPropertiesToDragMode() {
      if (this.getCurrentDragMode() === Def.GRID_CELL_DRAG_MODE) {
        Def.CANVAS_GRID_SIZE = Def.NODE_SIZE;
        Def.AUTO_ALIGN_STEP_X = Def.GRID_AUTO_ALIGN_STEP_X;
        Def.AUTO_ALIGN_STEP_Y = Def.GRID_AUTO_ALIGN_STEP_Y;
      } else {
        Def.CANVAS_GRID_SIZE = Def.CANVAS_DEFAULT_GRID_SIZE;
        Def.AUTO_ALIGN_STEP_X = Def.DEFAULT_AUTO_ALIGN_STEP_X;
        Def.AUTO_ALIGN_STEP_Y = Def.DEFAULT_AUTO_ALIGN_STEP_Y;
      }
    }

    initDraggableOptions($node) {
      const self = this;
      const node = Node.get($($node));

      let connections;

      this.instance.draggable($node, {
        distance: Def.CANVAS_GRID_SIZE / 2,
        grid: [Def.CANVAS_GRID_SIZE, Def.CANVAS_GRID_SIZE],
        rightButtonCanDrag: false,
        canDrag: function () {
          return self.editable;
        },
        start: function () {
          node.wasDragging = true;

          if (self.drag == null) {
            const selected = [];
            const unselected = [];

            node.setPopoverEnabled(false);

            self.forEachNode$(function ($n) {
              // Selected nodes are draggable so collision boxes have to be recalculated every time.
              if (Node.isSelected($n)) {
                selected.push($n);
                Node.get($n).setTitleEnabled(false);
              } else {
                unselected.push(Node.getCollisionBox($n));
              }
            });

            connections = self.getConnections();
            self.drag = {
              lead: $node,
              selected: selected,
              unselected: unselected,
              connectionsHoverAllowed: selected.length === 1 && !self.isNodeConnected(node)
            };
          }
        },
        stop: function () {
          self.drag.selected.forEach($n => {
            Node.get($n).setTitleEnabled(true);
            $n.removeClass('js-collision-detected');
          });

          connections.forEach(c => $(c.origin.canvas).removeClass('js-connection-drop-forbidden'));

          node.setPopoverEnabled(true);
        },
        drag: function () {
          if (self.drag && $node === self.drag.lead) {
            self.drag.selected.forEach($n => {
              const thisBox = Node.getCollisionBox($n);
              const hasCollision = self.drag.unselected
                .some(box => Node.detectBoxCollision(box, thisBox));

              $n.toggleClass('js-collision-detected', hasCollision);
            });

            connections.forEach(c => {
              $(c.origin.canvas).toggleClass('js-connection-drop-forbidden', c.origin.isHover() && !self.drag.connectionsHoverAllowed);
            });
          }
        },
        revert: function () {
          if (!self.drag) {
            return false;
          }

          let revert = false;

          self.undoManager.transaction(() => {
            const scale = self.getZoom();
            const collisionExists = self.drag.selected.some($n => {
              const thisBox = Node.getCollisionBox($n);
              return self.drag.unselected.some(box => Node.detectBoxCollision(box, thisBox));
            });
            const hoveredConnection = self.findHoveredConnection(connections);

            if (!hoveredConnection && collisionExists) {
              revert = true;
            }

            if (hoveredConnection && !self.drag.connectionsHoverAllowed) {
              revert = true;
            }

            if (!revert && hoveredConnection) {
              revert = !self.insertNodeIntoConnection(node, hoveredConnection, collisionExists);
            }

            if (!revert && !hoveredConnection) {
              self.undoManager.operation('nodeMoved',
                self.drag.selected.map($n => {
                  const _node = Node.get($n);
                  return {
                    node: _node,
                    coordinates: _node.getCoordinates(),
                    dragMode: self.getCurrentDragMode()
                  }
                })
              );

              self.drag.selected.forEach($n => {
                Node.get($n).updateCoordinates(scale);
              });
            }

            self.drag = null;
            self.updateOverlays();
            self.positionTitles();
            self.updateMinimap();
          });

          return revert;
        }
      });
    }

    getCurrentDragMode() {
      return this.dragMode;
    }

    setPanningEnabled(isEnabled) {
      this.canvas.setPanningEnabled(isEnabled);
      this.$viewport.toggleClass('js-panning', !!isEnabled);

      if (isEnabled) {
        this.$viewport.selectable('disable');
      } else if (this.editable) {
        this.$viewport.selectable('enable');
      }
    }

    setMouseWheelZoomEnabled(isEnabled) {
      this.canvas.setMouseWheelZoomEnabled(isEnabled);
    }

    setMultiConnectionEnabled(isEnabled) {
      this.multiConnectionEnabled = isEnabled;
    }

    setChainModeEnabled(isEnabled) {
      this.chainModeEnabled = isEnabled;
      this.clearSelectedSource();

      if (isEnabled) {
        var source = this.getSelectedNodes$().shift();
        if (source) {
          this.deselectAll();
          this.setNodeSelected(source, true);
          Node.setChainSource(source, true);
        }
      }
    }

    isChainMode() {
      return this.chainModeEnabled === true;
    }

    updateOverlays = _.debounce(() => {
      this.forEachNode(function (node) {
        if (Node.isDecisionNode(node)) {
          this.updateDecisionBranchesOverlays(node);
        }
      });
    }, 100);

    updateDecisionBranchesOverlays(node) {
      var connections = this.getNodeOutgoingConnections(node);

      if (Node.isBranchingDecisionNode(node)) {
        var branches = Node.getDecisionBranches(connections);

        if (branches) {
          var titles = NodeTitleHelper.getDecisionBranchesLabels();
          Ui.setConnectionLabel(branches.positive, titles.positive);
          Ui.setConnectionLabel(branches.negative, titles.negative);
        } else {
          connections.forEach(function (connection) {
            Ui.setConnectionLabel(connection, '?');
          });
        }
      } else {
        connections.forEach(function (connection) {
          Ui.setConnectionLabel(connection, false);
        });
      }
    }

    editIcon(node) {
      if (this.drag == null) {
        if (node) {
          if (node.editable) {
            this.deselectAll();
            EditorsHelper.showEditDialog(node);
          }
        } else {
          var $nodes = this.getSelectedNodes$();
          if ($nodes.length == 1) {
            this.deselectAll();
            const node = Node.get($nodes[0]);
            EditorsHelper.showEditDialog(node);
          }
        }
      }
    }

    editIconComment(node) {
      EditorsHelper.showIconCommentDialog(node);
    }

    serializeIcons() {
      var icons = this.getNodes().map(function (node) {
        return node.serialize();
      });

      var connectionsBySourceId = {};

      this.getConnections().forEach(function (connection) {
        var nodeConnections = connectionsBySourceId[connection.source.id];
        if (!nodeConnections) {
          nodeConnections = [];
        }
        nodeConnections.push(connection);
        connectionsBySourceId[connection.source.id] = nodeConnections;
      });

      icons.forEach(function (icon) {
        var nodeConnections = connectionsBySourceId[icon.id];
        if (nodeConnections) {
          icon.connections = nodeConnections.map(function (connection) {
            return {targetIconId: connection.target.id};
          });
        } else {
          icon.connections = [];
        }
      });

      return JSON.stringify(icons);
    }

    undo() {
      this.undoManager.undo();
    }

    getUndoManager() {
      return this.undoManager;
    }

    hasUnsavedChanges() {
      return this.getUndoManager().canUndo();
    }

    fitPdfPage() {
      this.canvas.fitPdfPage(Def.PDF_SIZE, Def.PDF_ORIENTATION);
    }
  }

  AGN.Lib.WM.Editor = Editor;
})();
