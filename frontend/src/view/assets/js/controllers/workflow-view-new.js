AGN.Lib.Controller.new('workflow-view-new', function() {
  var DateTimeUtils = AGN.Lib.WM.DateTimeUtils,
    Template = AGN.Lib.Template;

  var CONNECTION_THICKNESS = 3;
  var CONNECTION_OUTLINE_WIDTH = 4;
  var CONNECTION_COLOR = '#61B7CF';
  var CONNECTION_HOVER_COLOR = '#216477';
  var CONNECTION_OUTLINE_COLOR = 'transparent';
  var CONNECTION_ARROW_SIZE = 10;
  var CONNECTION_CORNER_RADIUS = 7;
  var CANVAS_GRID_SIZE = 20;
  var NODE_SNAP_GRID_SIZE = 10;

  // Page level.
  var Z_INDEX_VIEW_PORT = 10;
  var Z_INDEX_ICON_PANEL = 50;  // Icon panel above the view port.

  // Panel level.
  var Z_INDEX_DRAGGABLE_BUTTON = 60;

  // Stage level.
  var Z_INDEX_ICON_NODE = 10;  // Lowest object on the stage.
  var Z_INDEX_CONNECT_RAPID_BUTTON = 20;
  var Z_INDEX_JSPLUMB_DRAG_OPTIONS = 25;
  var Z_INDEX_NAVIGATOR_GLOBE = 30;
  var Z_INDEX_NAVIGATOR_ARROWS = 31;
  var Z_INDEX_CONTEXT_MENU = 40;  // Highest object on the stage.

  var ICONS_CONFIG = {
    actionbased_mailing: {
      icons: {
        inactive: 'icon_actionbased_mailing_g.png',
        active: 'icon_actionbased_mailing_l.png',
        dialog: 'icon_actionbased_mailing_s.png'
      },
      anchors: [
        [0.07, 0.5, -1, 0],
        [0.5, 0.22, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.78, 0, 1]
      ]
    },
    archive: {
      icons: {
        inactive: 'icon_archive_g.png',
        active: 'icon_archive_l.png',
        dialog: 'icon_archive_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.22, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.8, 0, 1]
      ]
    },
    datebased_mailing: {
      icons: {
        inactive: 'icon_datebased_mailing_g.png',
        active: 'icon_datebased_mailing_l.png',
        dialog: 'icon_datebased_mailing_s.png'
      },
      anchors: [
        [0.07, 0.5, -1, 0],
        [0.5, 0.22, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.78, 0, 1]
      ]
    },
    deadline: {
      icons: {
        inactive: 'icon_deadline_g.png',
        active: 'icon_deadline_l.png',
        dialog: 'icon_deadline_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.05, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.9, 0, 1]
      ]
    },
    decision: {
      icons: {
        inactive: 'icon_decision_g.png',
        active: 'icon_decision_l.png',
        dialog: 'icon_decision_s.png'
      },
      anchors: [
        [0.06, 0.5, -1, 0],
        [0.5, 0.06, 0, -1],
        [0.94, 0.5, 1, 0],
        [0.5, 0.94, 0, 1]
      ]
    },
    export: {
      icons: {
        inactive: 'icon_export_g.png',
        active: 'icon_export_l.png',
        dialog: 'icon_export_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.1, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.9, 0, 1]
      ]
    },
    followup_mailing: {
      icons: {
        inactive: 'icon_followup_mailing_g.png',
        active: 'icon_followup_mailing_l.png',
        dialog: 'icon_followup_mailing_s.png'
      },
      anchors: [
        [0.04, 0.5, -1, 0],
        [0.5, 0.20, 0, -1],
        [0.96, 0.5, 1, 0],
        [0.5, 0.8, 0, 1]
      ]
    },
    form: {
      icons: {
        inactive: 'icon_form_g.png',
        active: 'icon_form_l.png',
        dialog: 'icon_form_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.22, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.78, 0, 1]
      ]
    },
    import: {
      icons: {
        inactive: 'icon_import_g.png',
        active: 'icon_import_l.png',
        dialog: 'icon_import_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.1, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.9, 0, 1]
      ]
    },
    mailing: {
      icons: {
        inactive: 'icon_mailing_g.png',
        active: 'icon_mailing_l.png',
        dialog: 'icon_mailing_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.22, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.78, 0, 1]
      ]
    },
    ownWorkflow: {
      icons: {
        inactive: 'icon_ownWorkflow_g.png',
        active: 'icon_ownWorkflow_l.png',
        dialog: 'icon_ownWorkflow_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.22, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.78, 0, 1]
      ]
    },
    parameter: {
      icons: {
        inactive: 'icon_parameter_g.png',
        active: 'icon_parameter_l.png',
        dialog: 'icon_parameter_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.1, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.9, 0, 1]
      ]
    },
    recipient: {
      icons: {
        inactive: 'icon_recipient_g.png',
        active: 'icon_recipient_l.png',
        dialog: 'icon_recipient_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.1, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.9, 0, 1]
      ]
    },
    report: {
      icons: {
        inactive: 'icon_report_g.png',
        active: 'icon_report_l.png',
        dialog: 'icon_report_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.22, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.78, 0, 1]
      ]
    },
    scABTest: {
      icons: {
        inactive: 'icon_scABTest_g.png',
        active: 'icon_scABTest_l.png',
        dialog: 'icon_scABTest_s.png'
      },
      anchors: [
        [0.1, 0.5, -1, 0],
        [0.5, 0.15, 0, -1],
        [0.9, 0.5, 1, 0],
        [0.5, 0.85, 0, 1]
      ]
    },
    scBirthday: {
      icons: {
        inactive: 'icon_scBirthday_g.png',
        active: 'icon_scBirthday_l.png',
        dialog: 'icon_scBirthday_s.png'
      },
      anchors: [
        [0.1, 0.64, -1, 0],
        [0.5, 0.1, 0, -1],
        [0.9, 0.64, 1, 0],
        [0.5, 0.85, 0, 1]
      ]
    },
    scDOI: {
      icons: {
        inactive: 'icon_scDOI_g.png',
        active: 'icon_scDOI_l.png',
        dialog: 'icon_scDOI_s.png'
      },
      anchors: [
        [0.3, 0.46, -1, 0],
        [0.5, 0.04, 0, -1],
        [0.72, 0.46, 1, 0],
        [0.5, 0.96, 0, 1]
      ]
    },
    start: {
      icons: {
        inactive: 'icon_start_g.png',
        active: 'icon_start_l.png',
        dialog: 'icon_start_s.png'
      },
      anchors: [
        [0, 0.5, -1, 0],
        [0.5, 0.15, 0, -1],
        [1, 0.5, 1, 0],
        [0.5, 0.85, 0, 1]
      ]
    },
    stop: {
      icons: {
        inactive: 'icon_start_g.png',
        active: 'icon_start_l.png',
        dialog: 'icon_start_s.png'
      },
      anchors: [
        [0, 0.5, -1, 0],
        [0.5, 0.15, 0, -1],
        [1, 0.5, 1, 0],
        [0.5, 0.85, 0, 1]
      ]
    }
  };

  var canvas;

  // Register custom connector type.
  if (!jsPlumb.Connectors['FixedBezierConnector']) {
    jsPlumbUtil.extend(AGN.Opt.Components.JsPlumbFixedBezierConnector, jsPlumb.Connectors.AbstractConnector);
    jsPlumb.Connectors['FixedBezierConnector'] = AGN.Opt.Components.JsPlumbFixedBezierConnector;
  }

  function Canvas(editable) {
    this.$container = $('#viewPort');
    this.instance = jsPlumb.getInstance(Canvas.defaults(this.$container));
    this.editable = !!editable;
    this.nodes = [];
    this.connections = [];

    var self = this;

    this.$container.selectable({
      filter: '.iconNode',
      distance: 10,
      selected: function(event, ui) {
        self.instance.addToDragSelection(ui.selected);
      },
      unselected: function(event, ui) {
        self.instance.removeFromDragSelection(ui.unselected);
      }
    });

    this.$container.on('mousedown', function(event) {
      if (event.target.id === 'viewPort') {
        self.deselectAll();
      }
    });
  }

  Canvas.defaults = function($container) {
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
        zIndex: Z_INDEX_JSPLUMB_DRAG_OPTIONS
      },
      Endpoint: 'Blank',
      Connector: ['FixedBezierConnector'],
      PaintStyle: {
        stroke: CONNECTION_COLOR,
        strokeWidth: CONNECTION_THICKNESS,
        outlineStroke: CONNECTION_OUTLINE_COLOR,
        outlineWidth: CONNECTION_OUTLINE_WIDTH
      },
      HoverPaintStyle: {
        stroke: CONNECTION_HOVER_COLOR,
        strokeWidth: CONNECTION_THICKNESS,
        outlineStroke: CONNECTION_OUTLINE_COLOR,
        outlineWidth: CONNECTION_OUTLINE_WIDTH
      },
      ConnectionOverlays: [['Arrow', {
        id: 'arrow',
        visible: true,
        location: 1,
        width: CONNECTION_ARROW_SIZE,
        length: CONNECTION_ARROW_SIZE
      }]]
    };
  };

  Canvas.prototype.recycle = function() {
    this.instance.reset();
    this.$container.empty();
  };

  Canvas.prototype.batch = function() {
    this.instance.batch.apply(this.instance, arguments);
  };

  Canvas.prototype.add = function(node) {
    if (!this.nodes.includes(node)) {
      var self = this;
      var $node = node.get$();

      $node.css('left', node.getX() * CANVAS_GRID_SIZE);
      $node.css('top', node.getY() * CANVAS_GRID_SIZE);

      this.$container.append($node);
      this.instance.makeTarget($node, {anchor: node.getAnchors()});

      var wasDragging;
      var wasSelected;
      var snapCorrections = null;

      this.instance.draggable($node, {
        distance: 100,
        rightButtonCanDrag: false,
        start: function(event) {
          wasDragging = true;

          // When dragging a group of nodes then MouseEvent is only supplied for the node which is a holder.
          if (event.e) {
            snapCorrections = Node.getGridSnapCorrections(node);
          } else {
            snapCorrections = null;
          }
        },
        revert: function() {
          var selectedNodes = [];
          var unselectedNodes = [];

          self.$container.find('.iconNode')
              .each(function() {
                var $n = $(this);
                if (Node.isSelected($n)) {
                  selectedNodes.push($n);
                } else {
                  unselectedNodes.push($n);
                }
              });

          for (var i = 0; i < selectedNodes.length; i++) {
            for (var j = 0; j < unselectedNodes.length; j++) {
              var $node1 = selectedNodes[i];
              var $node2 = unselectedNodes[j];

              if (Node.detectCollision($node1, $node2)) {
                return true;
              }
            }
          }

          return false;
        },
        grid: function(x, y) {
          // Snapping is only applied to the node which is a holder, the rest of nodes (if any) out of a group are simply following.
          if (snapCorrections) {
            return [
              Math.round((x + snapCorrections.x) / NODE_SNAP_GRID_SIZE) * NODE_SNAP_GRID_SIZE - snapCorrections.x,
              Math.round((y + snapCorrections.y) / NODE_SNAP_GRID_SIZE) * NODE_SNAP_GRID_SIZE - snapCorrections.y
            ];
          } else {
            return [x, y];
          }
        }
      });

      $node.on({
        mousedown: function() {
          wasDragging = false;
          wasSelected = Node.isSelected($node);

          if (!wasSelected) {
            self.deselectAll();
            Node.setSelected($node, true);
          }
        },
        mouseup: function() {
          if (wasSelected && !wasDragging) {
            self.deselectAll();
            Node.setSelected($node, true);
          }
        }
      });

      this.nodes.push(node);
    }
  };

  Canvas.prototype.connect = function(source, target) {
    if (this.nodes.includes(source) && this.nodes.includes(target)) {
      var isConnected = this.connections.some(function(connection) {
        return connection.source == source && connection.target == target;
      });

      if (!isConnected) {
        this.instance.makeSource(source.get$(), {anchor: source.getAnchors()});

        this.instance.connect({
          source: source.get$(),
          target: target.get$()
        });

        this.instance.unmakeSource(source.get$());

        this.connections.push({source: source, target: target});
      }
    }
  };

  Canvas.prototype.deselectAll = function() {
    this.$container.find('.iconNode.ui-selected').removeClass('ui-selected');
    this.instance.clearDragSelection();
  };

  Canvas.prototype.setOnInitialized = function(callback) {
    this.instance.ready(callback);
  };

  function Node(type, data) {
    this.id = null;
    this.x = 0;
    this.y = 0;
    this.type = type;
    this.title = '';
    this.filled = false;
    this.editable = true;
    this.expandable = false;
    // This attribute will be true if recipient icon is supplemented in that sequence "mailing > deadline > recipient > … > mailing".
    this.dependent = false;  // For recipient icon only.
    this.anchors = ICONS_CONFIG[type].anchors;
    this.anchorsInUse = [];
    this.data = data;
    this.$element = Node.create$(type, false);
    this.$element.data('agn:node', this);
  }

  Node.get = function($element) {
    return $element.data('agn:node');
  };

  Node.isSelected = function($element) {
    return $element.hasClass('ui-selected');
  };

  Node.setSelected = function($element, isSelected) {
    return $element.toggleClass('ui-selected', !!isSelected);
  };

  // Grid snapping while dragging must be relative to connection "pipes", not icons themselves.
  Node.getGridSnapCorrections = function(node) {
    var anchors = node.getAnchors();
    // Presume that left and right anchors have the same Y positioning
    // also that top and bottom anchors have the same X positioning.

    var $node = node.get$();

    var connectionAnchorX = 0;
    var connectionAnchorY = 0;

    anchors.forEach(function(anchor) {
      if (anchor[2] === -1 && anchor[3] === 0) { // Left anchor.
        connectionAnchorY = anchor[1];
      } else if (anchor[2] === 0 && anchor[3] === -1) { // Top anchor.
        connectionAnchorX = anchor[0];
      }
    });

    return {
      x: $node.outerWidth() * connectionAnchorX,
      y: $node.outerHeight() * connectionAnchorY
    };
  };

  Node.getBox = function($node) {
    var position = $node.position();
    return {
      minX: position.left,
      minY: position.top,
      maxX: position.left + $node.outerWidth(),
      maxY: position.top + $node.outerHeight()
    };
  };

  Node.detectCollision = function($node1, $node2) {
    var box1 = Node.getBox($node1);
    var box2 = Node.getBox($node2);

    return !(box1.minX > box2.maxX || box1.maxX < box2.minX || box1.minY > box2.maxY || box1.maxY < box2.minY);
  };

  Node.create$ = (function() {
    var render = null;

    return function(type, isActive) {
      if (render == null) {
        render = Template.prepare('workflow-node');
      }

      return $(render({isActive: isActive, icons: ICONS_CONFIG[type].icons}));
    };
  })();

  Node.create = function(type) {
    var data = {};
    var node = new Node(type, data);

    switch (type) {
      case 'scABTest':
      case 'scBirthday':
      case 'scDOI':
        node.setFilled(true);
        node.setExpandable(true);
        data.copyContent = true;
        data.created = false;
        break;

      case 'ownWorkflow':
        node.setExpandable(true);
        data.ownWorkflowId = 0;
        data.copyContent = true;
        data.created = false;
        break;

      case 'start':
        data.startType = null;
        data.date = null;
        data.hour = 0;
        data.minute = 0;
        data.sendReminder = false;
        data.remindAdminId = 0;
        data.remindAtOnce = false;
        data.scheduleReminder = false;
        data.remindSpecificDate = false;
        data.remindDate = new Date();
        data.remindHour = 0;
        data.remindMinute = 0;
        data.event = null;
        data.reaction = null;
        data.mailingId = 0;
        data.profileField = '';
        data.useRules = false;
        data.rules = [];
        data.executeOnce = true;
        data.comment = '';
        data.recipients = '';
        data.adminTimezone = '';
        break;

      case 'stop':
        data.endType = null;
        data.date = null;
        data.hour = 0;
        data.minute = 0;
        data.sendReminder = false;
        data.remindAdminId = 0;
        data.remindAtOnce = false;
        data.scheduleReminder = false;
        data.remindSpecificDate = false;
        data.remindDate = new Date();
        data.remindHour = 0;
        data.remindMinute = 0;
        data.event = null;
        data.reaction = null;
        data.mailingId = 0;
        data.profileField = '';
        data.useRules = false;
        data.rules = [];
        data.executeOnce = true;
        data.comment = '';
        data.recipients = '';
        data.adminTimezone = '';
        break;

      case 'decision':
        data.decisionType = null;
        data.decisionCriteria = null;
        data.reaction = null;
        data.mailingId = 0;
        data.linkId = 0;
        data.profileField = '';
        data.aoDecisionCriteria = null;
        data.threshold = '';
        data.decisionDate = new Date();
        data.rules = [];
        data.includeVetoed = true;
        break;

      case 'deadline':
        data.deadlineType = null;
        data.date = new Date();
        data.timeUnit = null;
        data.delayValue = 0;
        data.hour = 0;
        data.minute = 0;
        data.useTime = false;
        break;

      case 'parameter':
        data.value = 0;
        break;

      case 'report':
        data.reports = [];
        break;

      case 'recipient':
        node.setDependent(false);
        data.mailinglistId = 0;
        data.targets = [];
        data.targetsOption = null;
        break;

      case 'archive':
        data.campaignId = 0;
        data.archived = false;
        break;

      case 'form':
        data.userFormId = 0;
        data.formType = 'form';
        break;

      case 'mailing':
        data.mailingId = 0;
        data.skipEmptyBlocks = true;
        data.doubleCheck = true;
        break;

      case 'actionbased_mailing':
      case 'datebased_mailing':
        data.mailingId = 0;
        break;

      case 'followup_mailing':
        data.baseMailingId = 0;
        data.mailingId = 0;
        data.decisionCriterion = 'OPENED';
        break;

      case 'import':
      case 'export':
        data.importexportId = 0;
        data.errorTolerant = false;
        break;
    }

    return node;
  };

  Node.deserialize = function(object) {
    if (object.type == 'archive') {
      object.type = 'scDOI';
    }
    var data = $.extend({}, object);
    var node = new Node(object.type, data);

    node.setId(object.id);
    node.setX(object.x);
    node.setY(object.y);
    node.setTitle(object.iconTitle);
    node.setFilled(object.filled);
    node.setEditable(object.editable);

    if (['ownWorkflow', 'scBirthday', 'scDOI', 'scABTest'].includes(object.type)) {
      node.setExpandable(true);
    }

    delete data['id'];
    delete data['x'];
    delete data['y'];
    delete data['type'];
    delete data['iconTitle'];
    delete data['filled'];
    delete data['editable'];

    ['date', 'remindDate', 'decisionDate'].forEach(function(k) {
      if (data[k]) {
        var string = data[k].toString();
        if (string.includes('-')) {
          var parts = string.split('-');
          data[k] = new Date(parts[0], parts[1] - 1, parts[2]);
        } else {
          data[k] = new Date(data[k]);
        }
      }
    });

    return node;
  };

  Node.deserializeConnections = function(icons, nodesMap) {
    var connections = [];

    icons.forEach(function(icon) {
      var source = nodesMap[icon.id];

      if (source instanceof Node) {
        icon.connections.forEach(function(connection) {
          var target = nodesMap[connection.targetIconId];
          if (target instanceof Node) {
            connections.push({source: source, target: target});
          }
        });
      }
    });

    return connections;
  };

  Node.toMap = function(nodes) {
    var map = {};

    // Map nodeId -> node.
    nodes.forEach(function(node) {
      map[node.getId()] = node;
    });

    return map;
  };

  Node.prototype.get$ = function() {
    return this.$element;
  };

  Node.prototype.getId = function() {
    return this.id;
  };

  Node.prototype.setId = function(id) {
    this.id = id;
    this.$element.prop('id', id);
  };

  Node.prototype.getX = function() {
    return this.x;
  };

  Node.prototype.setX = function(x) {
    this.x = x;
  };

  Node.prototype.getY = function() {
    return this.y;
  };

  Node.prototype.setY = function(y) {
    this.y = y;
  };

  Node.prototype.getType = function() {
    return this.type;
  };

  Node.prototype.getTitle = function() {
    return this.title;
  };

  Node.prototype.setTitle = function(title) {
    this.title = title;
  };

  Node.prototype.isFilled = function() {
    return this.filled;
  };

  Node.prototype.setFilled = function(filled) {
    this.filled = filled;
  };

  Node.prototype.isEditable = function() {
    return this.editable;
  };

  Node.prototype.setEditable = function(editable) {
    this.editable = editable;
  };

  Node.prototype.isExpandable = function() {
    return this.expandable;
  };

  Node.prototype.setExpandable = function(expandable) {
    this.expandable = expandable;
  };

  Node.prototype.isDependent = function() {
    return this.dependent;
  };

  Node.prototype.setDependent = function(dependent) {
    this.dependent = dependent;
  };

  Node.prototype.getAnchors = function() {
    return this.anchors;
  };

  Node.prototype.setAnchors = function(anchors) {
    this.anchors = anchors;
  };

  Node.prototype.getData = function() {
    return this.data;
  };

  Node.prototype.setData = function(data) {
    this.data = data;
  };

  Node.prototype.serialize = function() {
    var object = {
      id: this.id,
      x: this.x,
      y: this.y,
      type: this.type,
      filled: this.filled,
      editable: this.editable,
      iconTitle: this.title
    };

    $.extend({}, this.data, object);

    if ('date' in object && 'hour' in object && 'minute' in object) {
      object.date = DateTimeUtils.getDateTimeValue(object.date, object.hour, object.minute);
    }

    if ('remindDate' in object && 'remindHour' in object && 'remindMinute' in object) {
      object.remindDate = DateTimeUtils.getDateTimeValue(object.remindDate, object.remindHour, object.remindMinute);
    }

    if ('decisionDate' in object) {
      object.decisionDate = DateTimeUtils.getDateTimeValue(object.decisionDate);
    }

    return object;
  };

  this.addDomInitializer('workflow-view-new', function() {
    var config = this.config;
    var nodes = config.icons.map(Node.deserialize);
    var connections = Node.deserializeConnections(config.icons, Node.toMap(nodes));

    if (canvas) {
      canvas.recycle();
    }

    canvas = new Canvas(true);

    canvas.setOnInitialized(function() {
      canvas.batch(function() {
        // Add all the nodes to the canvas.
        nodes.forEach(function(node) {
          canvas.add(node);
        });

        // Establish all the connections.
        connections.forEach(function(connection) {
          canvas.connect(connection.source, connection.target);
        });
      });
    });
  });
});
