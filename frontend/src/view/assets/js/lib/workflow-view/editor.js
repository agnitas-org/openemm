(function() {
    var Def = AGN.Lib.WM.Definitions,
        Node = AGN.Lib.WM.Node,
        Vertex = AGN.Lib.WM.Vertex,
        VertexGroup = AGN.Lib.WM.VertexGroup,
        NodeTitleHelper = AGN.Lib.WM.NodeTitleHelper,
        Canvas = AGN.Lib.WM.Canvas,
        ConnectionConstraints = AGN.Lib.WM.ConnectionConstraints,
        DraggableButtons = AGN.Lib.WM.DraggableButtons,
        AutoAlignment = AGN.Lib.WM.AutoAlignment,
        UndoManager = AGN.Lib.WM.UndoManager,
        EditorsHelper = AGN.Lib.WM.EditorsHelperNew,
        Dialogs = AGN.Lib.WM.Dialogs,
        Snippets = AGN.Lib.WM.Snippets,
        ContextMenu = AGN.Lib.ContextMenu,
        Helpers = AGN.Lib.Helpers,
        Utils = AGN.Lib.WM.Utils;

    var Ui = {
        blurInputFields: function() {
            if (document.activeElement && $(document.activeElement).is('input, textarea')) {
                document.activeElement.blur();
            }
        },
        setConnectionLabel: function(connection, text) {
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
        setDeleteButtonEnabled: function(isEnabled) {
            $('#deleteButton').toggleClass('disabled', !isEnabled);
            $('#deleteItem').prop('disabled', !isEnabled);
        },
        setUndoButtonEnabled: function(isEnabled) {
            $('#undoButton').toggleClass('disabled', !isEnabled);
            $('#undoItem').prop('disabled', !isEnabled);
        },
        updateStatisticButton: function(isEnabled) {
            $('#toggle-statistic-btn .text').html(isEnabled ? t('workflow.fade_out_statistics') : t('workflow.fade_in_statistics'));
        }
    };

    function Editor(isEditable, isContextMenuEnabled, isFootnotesEnabled) {
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
        this.statisticEnabled = false;

        var self = this;

        this.canvas.setOnZoom(function(scale) {
            self.instance.setZoom(scale);
            if (self.canvasZoomHandler) {
                self.canvasZoomHandler.call(null, scale);
            }
        });

        this.draggableButtons = new DraggableButtons({
            enabled: !!isEditable,
            getZoom: function() {
                return self.getZoom();
            },
            getOccupiesAreas: function() {
                return self.getNodes$()
                    .get()
                    .map(function(node) { return Node.getCollisionBox($(node)); });
            },
            onStart: function() {
                self.canvas.setMouseWheelZoomEnabled(false);
            },
            onStop: function() {
                self.canvas.setMouseWheelZoomEnabled(true);
            },
            onDrop: function(type, position) {
                self.newNode(type, position);
            }
        });

        this.$viewport.selectable({
            filter: '.node',
            distance: 10,
            disabled: !this.editable,
            start: function() {
                $('#selection-backdrop').show();
            },
            stop: function() {
                $('#selection-backdrop').hide();
            },
            selected: function(event, ui) {
                self.instance.addToDragSelection(ui.selected);
                self.onSelectionChanged();
            },
            unselected: function(event, ui) {
                self.instance.removeFromDragSelection(ui.unselected);
                self.onSelectionChanged();
            }
        });

        this.$viewport.on('mousedown', function(event) {
            if (['canvas', 'viewPort', 'icon-titles-container'].includes(event.target.id) || $.contains(self.$titlesContainer[0], event.target)) {
                self.deselectAll();
            }

            Ui.blurInputFields();
        });

        // On connection created.
        this.instance.bind('connection', function(connection) {
            self.undoManager.operation('connectionCreated', connection);
            self.updateOverlays();
            self.positionTitles();
            self.processRecipientsChains();
        });

        this.instance.bind('connectionDetached', function(connection) {
            self.undoManager.operation('connectionDeleted', connection);
            self.updateOverlays();
            self.positionTitles();
            self.processRecipientsChains();
        });

        this.instance.bind('mousedown', function(connection, event) {
            if (self.editable && event.button === Def.MOUSE_BUTTON_MIDDLE) {
                _.defer(function() {
                    // Use defer function to make sure jsplumb will fire connectionDetached event synchronously
                    // (otherwise transaction of undo manager will be broken).

                    self.undoManager.transaction(function() {
                        self.deleteConnection(connection);
                    });
                });
            }

            Ui.blurInputFields();
        });

        this.instance.bind('beforeDrag', function() {
            self.undoManager.startTransaction();
        });

        this.instance.bind('connectionDragStop', function() {
            self.undoManager.endTransaction();
        });

        this.instance.bind('connectionDrag', function(connection) {
            self.deselectAll();

            if (connection.source) {
                var $source = $(connection.source);
                self.setNodeSelected($source, true);

                if (self.isChainMode()) {
                    Node.setChainSource($source, true);
                }
            }
        });

        this.instance.bind('beforeDrop', function(params) {
            var source = Node.get(document.getElementById(params.sourceId));
            var target = Node.get(document.getElementById(params.targetId));
            var result = self.connect(source, target, true);

            if (result) {
                return true;
            }

            if (result === false) {
                // Constraint violated.
                workflowNotAllowedConnectionDialogHandler.showDialog();
            }

            return false;
        });

        this.$footnotesContainer = $('#footnotes-container ol');
        this.isFootnotesEnabled = isFootnotesEnabled;

        this.undoManager = new UndoManager(this, {
            onChange: function() {
                Ui.setUndoButtonEnabled(this.canUndo());
                self.processRecipientsChains();
            }
        });

        EditorsHelper.assignOptions({
            getUndoManager: function() {
                return self.getUndoManager();
            },

            getNodesByTypes: function(types) {
                return self.getNodesByTypes(types);
            },

            getNodesByIncomingConnections: function(node) {
                return self.getNodeIncomingConnections(node)
                    .map(function(connection) { return connection.source; });
            },

            getNodesByOutgoingConnections: function(node) {
                return self.getNodeOutgoingConnections(node)
                    .map(function(connection) { return connection.target; });
            },

            getFirstIncomingChain: function(node) {
                return self.getFirstIncomingChain(node);
            },

            forEachPreviousNode: function(node, callback) {
                return self.forEachPreviousNode(node, callback);
            },

            getNodeAnchorsInUse: function(node) {
                return self.getAnchorsInUse(node);
            },

            onChange: function(node) {
                self.updateNodeTitle(node, true);
                self.updateFootnotes();
                self.updateMinimap();
            },

            supplement: function(node /*, mailingEditorBase, mailingContent */) {
                self.supplement.apply(self, arguments);
            },

            editNode: function(node, needCheckActivation) {
                alert('Missing editorsHelper.editNode');
            },

            deleteNodeById: function(id) {
                alert('Missing editorsHelper.deleteNodeById');
            }
        });

        if (isContextMenuEnabled) {
            ContextMenu.create(Editor.contextMenuOptions(self));
        }
    }

    Editor.defaults = function($container) {
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
            ConnectionOverlays: [['Arrow', {
                id: 'arrow',
                visible: true,
                location: 1,
                width: Def.CONNECTION_ARROW_SIZE,
                length: Def.CONNECTION_ARROW_SIZE
            }]]
        };
    };

    Editor.contextMenuOptions = function(self) {
        return {
            '.jtk-connector': {
                shown: function() {
                    return self.editable;
                },
                items: {
                    'delete': {
                        name: t('workflow.defaults.delete'),
                        icon: 'delete',
                        disabled: function($connection) {
                            return !self.canDeleteConnection(self.getConnectionByConnector$($connection));
                        },
                        clicked: function($connection) {
                            self.undoManager.transaction(function() {
                                var connection = self.getConnectionByConnector$($connection);
                                self.deleteConnection(connection.origin);
                            });
                        }
                    }
                }
            },
            '.node': {
                shown: function() {
                    return true;
                },
                items: {
                    'connect': {
                        name: t('workflow.connect'),
                        icon: 'connect',
                        shown: function() {
                            return self.editable && self.getSelectionSize() > 1;
                        },
                        clicked: function() {
                            self.undoManager.transaction(function() {
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
                        shown: function() {
                            return self.editable && self.getSelectionSize() > 1;
                        },
                        clicked: function() {
                            self.undoManager.transaction(function() {
                                self.disconnectSelected();
                            });
                        }
                    },
                    'edit': {
                        name: t('workflow.defaults.edit'),
                        icon: 'edit',
                        shown: function() {
                            return self.getSelectionSize() <= 1;
                        },
                        clicked: function($element) {
                            self.editIcon(Node.get($element));
                        },
                        disabled: function($element) {
                            return !Node.get($element).isEditable();
                        }
                    },
                    'delete': {
                        name: t('workflow.defaults.delete'),
                        icon: 'delete',
                        clicked: function() {
                            self.undoManager.transaction(function() {
                                self.deleteSelected();
                            });
                        },
                        disabled: function() {
                            return !self.editable;
                        }
                    },
                    'comment': {
                        name: t('workflow.defaults.comment'),
                        icon: 'comment',
                        clicked: function($element) {
                            self.editIconComment(Node.get($element));
                        },
                        disabled: function() {
                            return !self.editable;
                        }
                    }
                }
            }
        };
    };

    Editor.prototype.updateFootnotes = _.throttle(function() {
        this.forEachNode(function(node) {
            node.setFootnote(-1);
        });
        this.$footnotesContainer.html('');

        if (this.isFootnotesEnabled) {
            var self = this;
            var index = 1;
            this.forEachNode(function(node) {
                if (Node.isCommented(node)) {
                    self.$footnotesContainer.append('<li id="#fn:' + index + '">' + node.getComment() + '</li>');
                    node.setFootnote(index);
                    index++;
                }
            });
        }
    }, 150);

    Editor.prototype.isActionBasedWorkflow = function () {
        return this.getNodes().some(function(node) {
            return node.getType() === Def.NODE_TYPE_ACTION_BASED_MAILING;
        });
    };

    Editor.prototype.isDateBasedWorkflow = function () {
        return this.getNodes().some(function(node) {
            return node.getType() === Def.NODE_TYPE_DATE_BASED_MAILING;
        });
    };

    Editor.prototype.isNormalWorkflow = function() {
        return !this.isActionBasedWorkflow() && !this.isDateBasedWorkflow();
    };

    Editor.prototype.getNodes$ = function() {
        return this.$container.find('.node');
    };

    Editor.prototype.getNodes = function() {
        return this.getNodes$()
            .get()
            .map(function($node) { return Node.get($node); });
    };

    Editor.prototype.getVertices = function() {
        return Vertex.verticesFrom(this.getNodes(), this.instance.getAllConnections());
    };

    Editor.prototype.getNodesByTypes = function(types) {
        return this.getNodes().filter(function(node) {
            return types.includes(node.type);
        });
    };

    Editor.prototype.forEachNode$ = function(callback) {
        var $nodes = this.getNodes$();
        var self = this;

        $nodes.each(function() {
            callback.call(self, $(this));
        });
    };

    Editor.prototype.forEachNode = function(callback) {
        var self = this;

        this.forEachNode$(function($node) {
            callback.call(self, Node.get($node));
        });
    };

    Editor.prototype.forEachPreviousNode = function(target, callback) {
        var connections = this.getConnections();
        var layer = connections.filter(function(connection) {
            return connection.target === target;
        }).map(function(connection) {
            return connection.source;
        });

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

            layer = connections.filter(function(connection) {
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
            }).map(function(connection) {
                return connection.source;
            });
        }
    };

    Editor.prototype.getNodeIncomingChains = function(node) {
        var vertices = this.getVertices();
        var target = vertices.find(function(vertex) { return vertex.node == node; });
        var chains = [];

        if (target) {
            return Vertex.getIncomingChains(target);
        }

        return chains;
    };

    Editor.prototype.getNodeOutgoingChains = function(node) {
        var vertices = this.getVertices();
        var target = vertices.find(function(vertex) { return vertex.node == node; });
        var chains = [];

        if (target) {
            return Vertex.getOutgoingChains(target);
        }

        return chains;
    };

    Editor.prototype.getFirstIncomingChain = function(node) {
        var chains = this.getNodeIncomingChains(node);
        if (chains.length) {
            return chains[0];
        }
        return [];
    };

    Editor.prototype.getNodeIncomingConnections = function(node) {
        return this.getConnections(function(connection) {
            return Node.get(connection.target) === node;
        });
    };

    Editor.prototype.getNodeOutgoingConnections = function(node) {
        return this.getConnections(function(connection) {
            return Node.get(connection.source) === node;
        });
    };

    Editor.prototype.getNodeConnections = function(node) {
        return this.getConnections(function(connection) {
            return Node.get(connection.source) === node || Node.get(connection.target) === node;
        });
    };

    Editor.prototype.hasNodeIncomingConnections = function(node) {
        return this.instance.getAllConnections()
            .some(function(connection) {
                return Node.get(connection.target) === node;
            });
    };

    Editor.prototype.hasNodeOutgoingConnections = function(node) {
        return this.instance.getAllConnections()
            .some(function(connection) {
                return Node.get(connection.source) === node;
            });
    };

    Editor.prototype.hasConnection = function(predicate) {
        return this.instance.getAllConnections()
            .some(function(connection) {
                return predicate.call(null, {
                    origin: connection,
                    source: Node.get(connection.source),
                    target: Node.get(connection.target)
                });
            });
    };

    Editor.prototype.getConnections = function(filter) {
        var connections = this.instance.getAllConnections();

        if (filter) {
            connections = connections.filter(filter);
        }

        return connections.map(function(connection) {
            return {
                origin: connection,
                source: Node.get(connection.source),
                target: Node.get(connection.target)
            };
        });
    };

    Editor.prototype.getConnectionByConnector$ = function($connector) {
        return this.getConnections(function(connection) {
            return connection.canvas == $connector[0];
        })[0];
    };

    Editor.prototype.getConnectionBetween = function($source, $target) {
        return this.getConnections(function(connection) {
            return $(connection.source).is($source) && $(connection.target).is($target);
        })[0];
    };

    Editor.prototype.recycle = function() {
        this.instance.reset();
        this.$container.empty();
    };

    Editor.prototype.batch = function() {
        this.instance.batch.apply(this.instance, arguments);
    };

    Editor.prototype.newNode = function(type, position) {
        var self = this;

        if (this.editable) {
            if (Node.isExpandableType(type)) {
                switch (type) {
                    case Def.NODE_TYPE_SC_ABTEST:
                    case Def.NODE_TYPE_SC_BIRTHDAY:
                    case Def.NODE_TYPE_SC_DOI:
                        this.newSnippetFromSample(type, position);
                        break;

                    case Def.NODE_TYPE_OWN_WORKFLOW:
                        Dialogs.confirmOwnWorkflowExpanding()
                            .done(function(params) {
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

                this.undoManager.transaction(function() {
                    self.add(node, animation);
                    self.supplement(node);
                });
            }
        }
    };

    Editor.prototype.newSnippetFromSample = function(type, position) {
        var self = this;

        Snippets.loadSample(type, function(nodes, connections) {
            self.newSnippet(nodes, connections, position);
        });
    };

    Editor.prototype.newSnippetFromOwnWorkflow = function(workflowId, copyContent, position) {
        var self = this;

        Snippets.loadOwnWorkflow(workflowId, copyContent, function(nodes, connections) {
            self.newSnippet(nodes, connections, position);
        });
    };

    Editor.prototype.newSnippet = function(nodes, connections, position) {
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

        this.undoManager.transaction(function() {
            self.batch(function() {
                var allocatedIds = self.allocateIds(nodes.length);

                self.allocateSpaceForSnippet(nodes);

                nodes.forEach(function(node, nodeIndex) {
                    node.setId(allocatedIds[nodeIndex]);
                    self.add(node, animation);
                });

                connections.forEach(function(connection) {
                    self.connect(connection.source, connection.target);
                });
            });
        });
    };

    Editor.prototype.allocateId = function() {
        return this.allocateIds(1)[0];
    };

    Editor.prototype.allocateIds = function(count) {
        var newIds = [];
        var ids = {};

        this.forEachNode(function(node) {
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
    };

    Editor.prototype.pickNewNodePosition = function() {
        var area = this.canvas.getVisibleArea();
        var areaMinY = Math.ceil(area.minY / Def.CANVAS_GRID_SIZE);
        var areaMaxY = Math.floor(area.maxY / Def.CANVAS_GRID_SIZE) - (Def.AUTO_DROP_STEP_Y >> 1);
        var positionRangesInUse = [];
        var result;

        this.forEachNode(function(node) {
            var coordinates = node.getCoordinates();

            positionRangesInUse.push({
                minX: coordinates.x - Def.AUTO_DROP_STEP_X + 1,
                minY: coordinates.y - Def.AUTO_DROP_STEP_Y + 1,
                maxX: coordinates.x + Def.AUTO_DROP_STEP_X - 1,
                maxY: coordinates.y + Def.AUTO_DROP_STEP_Y - 1
            });
        });

        var testOne = function(posX, posY) {
            if (posY < areaMinY || posY > areaMaxY) {
                return false;
            }

            var empty = !positionRangesInUse.some(function(ranges) {
                // Is within?
                return !(posX < ranges.minX || posX > ranges.maxX || posY < ranges.minY || posY > ranges.maxY);
            });

            if (empty) {
                return {x: posX, y: posY};
            }

            return false;
        };

        var test = function(/* posX1, posY1, posX2, posY2, ... */) {
            for (var index = 0; index + 1 < arguments.length; index += 2) {
                var res = testOne(arguments[index], arguments[index + 1]);
                if (res) {
                    return res;
                }
            }

            return false;
        };

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
    };

    Editor.prototype.getCenterPosition = function() {
        var area = this.canvas.getVisibleArea();

        var centerX = Math.round((area.minX + area.maxX - Def.NODE_SIZE) / 2 / Def.CANVAS_GRID_SIZE);
        var centerY = Math.round((area.minY + area.maxY - Def.NODE_SIZE) / 2 / Def.CANVAS_GRID_SIZE);

        return {x: centerX, y: centerY};
    };

    Editor.prototype.allocateSpaceForSnippet = function(snippetNodes) {
        var self = this;

        function detectCollision(node1, node2) {
            var a = node1.getCoordinates();
            var b = node2.getCoordinates();

            return Math.abs(a.x - b.x) < Def.AUTO_DROP_STEP_X && Math.abs(a.y - b.y) < Def.AUTO_DROP_STEP_Y;
        }

        function detectGroupsCollision(group1, group2) {
            return group1.some(function(node1) {
                return group2.some(function(node2) {
                    return detectCollision(node1, node2);
                });
            });
        }

        function getGroupCenterPosition(group) {
            var minX = 0, maxX = 0, minY = 0, maxY = 0;

            group.forEach(function(node) {
                minX = Math.min(minX, node.getX());
                maxX = Math.max(maxX, node.getX());
                minY = Math.min(minY, node.getY());
                maxY = Math.max(maxY, node.getY());
            });

            return {
                x: (maxX - minX) / 2,
                y: (maxY - minY) / 2
            };
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
            nodes.forEach(function(node) {
                var coordinates = node.getCoordinates();
                node.setCoordinates(coordinates.x + direction.x, coordinates.y + direction.y);
            });
        }

        if (snippetNodes.length) {
            var allExistingNodes = this.getNodes();

            // First collect all (let's presume all the nodes are to be moved).
            // Then we will check what's affected and what's not.
            var nodesMovedLog = allExistingNodes.map(function(node) {
                return {node: node, coordinates: node.getCoordinates()};
            });

            var snippetCenterPosition = getGroupCenterPosition(snippetNodes);

            var allExistingGroups = VertexGroup.detectGroups(this.getVertices())
                .map(function(group) {
                    var nodes = group.getNodes();

                    return {
                        nodes: nodes,
                        moveDirection: getMoveDirection(getGroupCenterPosition(nodes), snippetCenterPosition)
                    };
                });

            allExistingGroups.forEach(function(group) {
                if (detectGroupsCollision(group.nodes, snippetNodes)) {
                    var direction = group.moveDirection;
                    var nodesToMove = group.nodes.slice(0);
                    var nodesToKeep = snippetNodes.slice(0);

                    var otherGroups = allExistingGroups.filter(function(g) { return g !== group });

                    do {
                        moveNodes(nodesToMove, direction);

                        var jointGroupDetected;

                        do {
                            jointGroupDetected = false;

                            otherGroups = otherGroups.filter(function(otherGroup) {
                                if (detectGroupsCollision(nodesToMove, otherGroup.nodes)) {
                                    if (otherGroup.moveDirection.x == direction.x && otherGroup.moveDirection.y == direction.y) {
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
            nodesMovedLog = nodesMovedLog.filter(function(entry) {
                var newCoordinates = entry.node.getCoordinates();
                var oldCoordinates = entry.coordinates;

                return newCoordinates.x != oldCoordinates.x || newCoordinates.y != oldCoordinates.y;
            });

            if (nodesMovedLog.length) {
                this.undoManager.operation('nodeMoved', nodesMovedLog);
            }
        }
    };

    Editor.prototype.allocateSpaceForSupplement = function(node, direction) {
        var positionRangesInUse = [];
        var test = function(posX, posY) {
            return !positionRangesInUse.some(function(ranges) {
                // Is within?
                return !(posX < ranges.minX || posX > ranges.maxX || posY < ranges.minY || posY > ranges.maxY);
            });
        };

        this.forEachNode(function(node) {
            var coordinates = node.getCoordinates();

            positionRangesInUse.push({
                minX: coordinates.x - Def.AUTO_DROP_STEP_X + 1,
                minY: coordinates.y - Def.AUTO_DROP_STEP_Y + 1,
                maxX: coordinates.x + Def.AUTO_DROP_STEP_X - 1,
                maxY: coordinates.y + Def.AUTO_DROP_STEP_Y - 1
            });
        });

        var deltaX = 0;
        var deltaY = 0;

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
        var newX = node.getX() + Def.AUTO_DROP_STEP_X * deltaX;
        var newY = node.getY() + Def.AUTO_DROP_STEP_Y * deltaY;

        while (!test(newX, newY)) {
            // Make little steps towards chosen direction until no collision is there.
            newX += deltaX;
            newY += deltaY;
        }

        return {x: newX, y: newY};
    };

    Editor.prototype.allocateSpaceForIncomingSupplement = function(target) {
        var anchorsInUse = this.getAnchorsInUse(target);
        var direction = Def.LEFT;

        if (!anchorsInUse[Def.LEFT] && anchorsInUse[Def.TOP]) {
            direction = Def.TOP;
        }

        return this.allocateSpaceForSupplement(target, direction);
    };

    Editor.prototype.allocateSpaceForOutgoingSupplement = function(source) {
        var anchorsInUse = this.getAnchorsInUse(source);
        var direction = Def.RIGHT;

        if (!anchorsInUse[Def.RIGHT] && anchorsInUse[Def.BOTTOM]) {
            direction = Def.BOTTOM;
        }

        return this.allocateSpaceForSupplement(source, direction);
    };

    Editor.prototype.add = function(node, animation) {
        var self = this;
        var $node = node.get$();

        if (!node.getId()) {
            node.setId(this.allocateId());
        }

        node.appendTo(this.$container, this.$titlesContainer, animation);
        node.setInteractionEnabled(this.editable);
        node.updateCommentLabels();

        this.instance.makeTarget($node, {anchor: node.getAnchors()});
        this.instance.makeSource($node, {anchor: node.getAnchors(), filter: '.node-connect-button img'});

        var wasDragging;
        var wasSelected;

        this.instance.draggable($node, {
            distance: Def.CANVAS_GRID_SIZE / 2,
            grid: [Def.CANVAS_GRID_SIZE, Def.CANVAS_GRID_SIZE],
            rightButtonCanDrag: false,
            canDrag: function() {
                return self.editable;
            },
            start: function() {
                wasDragging = true;

                if (self.drag == null) {
                    var selected = [];
                    var unselected = [];

                    node.setPopoverEnabled(false);

                    self.forEachNode$(function($n) {
                        // Selected nodes are draggable so collision boxes have to be recalculated every time.
                        if (Node.isSelected($n)) {
                            selected.push($n);
                            Node.get($n).setTitleEnabled(false);
                        } else {
                            unselected.push(Node.getCollisionBox($n));
                        }
                    });

                    self.drag = {lead: $node, selected: selected, unselected: unselected};
                }

                self.canvas.setMouseWheelZoomEnabled(false);
            },
            stop: function() {
                self.canvas.setMouseWheelZoomEnabled(true);
                self.drag.lead.removeClass('js-no-drop');
                self.drag.selected.forEach(function($n) {
                    Node.get($n).setTitleEnabled(true);
                    $n.removeClass('js-collision-detected');
                });

                node.setPopoverEnabled(true);
            },
            drag: function() {
                if (self.drag && $node == self.drag.lead) {
                    var hasAnyCollision = false;

                    self.drag.selected.forEach(function($n) {
                        var thisBox = Node.getCollisionBox($n);
                        var hasCollision = self.drag.unselected.some(function(box) {
                            return Node.detectBoxCollision(box, thisBox);
                        });

                        $n.toggleClass('js-collision-detected', hasCollision);

                        if (hasCollision) {
                            hasAnyCollision = true;
                        }
                    });

                    self.drag.lead.toggleClass('js-no-drop', hasAnyCollision);
                }
            },
            revert: function() {
                if (self.drag) {
                    var hasCollision = false;
                    self.undoManager.transaction(function() {
                        var scale = self.getZoom();
                        hasCollision = self.drag.selected.some(function($n) {
                            var thisBox = Node.getCollisionBox($n);
                            return self.drag.unselected.some(function(box) {
                                return Node.detectBoxCollision(box, thisBox);
                            });
                        });

                        if (!hasCollision) {
                            self.undoManager.operation('nodeMoved',
                              self.drag.selected.map(function($n) {
                                  var _node = Node.get($n);
                                  return {
                                      node: _node,
                                      coordinates: _node.getCoordinates()
                                  };
                              })
                            );

                            self.drag.selected.forEach(function($n) {
                                Node.get($n).updateCoordinates(scale);
                            });
                        }

                        self.drag = null;
                        self.updateOverlays();
                        self.positionTitles();
                        self.updateMinimap();
                    });

                    return hasCollision;
                } else {
                    return false;
                }
            }
        });

        $node.on({
            mousedown: function(event) {
                if (event.button === Def.MOUSE_BUTTON_MIDDLE) {
                    if (self.editable) {
                        self.undoManager.transaction(function() {
                            self.deleteNode($node);
                        });
                    }
                } else {
                    wasDragging = false;
                    wasSelected = Node.isSelected($node);

                    if (!wasSelected) {
                        if (self.isChainMode()) {

                            if (!self.isChainSourceSelected()) {
                                //mark the node as source if any node selected as source
                                self.setNodeSelected($node, true);
                                Node.setChainSource($node, true);
                            } else {
                                var source = self.getChainSource();

                                _.defer(function() {
                                    // Use defer function to make sure jsplumb will fire connection event synchronously
                                    // (otherwise transaction of undo manager will be broken).

                                    self.undoManager.transaction(function() {
                                        var connected = self.connect(Node.get(source), node);

                                        //reset selection after a successful connection, otherwise source stays the same
                                        if (connected) {
                                            self.deselectAll();

                                            self.setNodeSelected($node, true);
                                            Node.setChainSource($node, true);
                                        } else if (connected === false) {
                                            // Constraint violated.
                                            workflowNotAllowedConnectionDialogHandler.showDialog();
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
            mouseup: function(event) {
                if (wasSelected && !wasDragging) {
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
            dblclick: function() {
                self.editIcon(node);
            },
            mouseover: function() {
                node.setHovered(true);
            },
            mouseout: function() {
                node.setHovered(false);
            }
        });

        this.undoManager.operation('nodeAdded', node);
        this.updateMinimap();
        this.updateFootnotes();
    };

    Editor.prototype.supplement = function(node, mailingEditorBase, mailingContent) {
        var self = this;

        if (node.isFilled()) {
            if (Def.NODE_TYPES_MAILING.includes(node.getType())) {
                // TODO: move supplement code here from mailing-editor-helper.js
                mailingEditorBase.trySupplementChain(this.getFirstIncomingChain(node), mailingContent, function(chain) {
                    self.batch(function() {
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
            } else if (Def.NODE_TYPE_RECIPIENT == node.getType()) {
                this.processRecipientsChains();
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
    };

    Editor.prototype.processRecipientsChains = _.debounce(function() {
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

    Editor.prototype._findInitialRecipientsNodes = function() {
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
    };

    Editor.prototype.getAnchorsInUse = function(node) {
        var $node = node.get$();
        return Node.getAnchorsInUse(this.instance.getEndpoints($node[0]));
    };

    Editor.prototype.positionTitle = function(node) {
        NodeTitleHelper.positionTitle(node, this.getAnchorsInUse(node));
    };

    Editor.prototype.updateNodeTitle = function(node, forceUpdate) {
        var isNormalWorkflow = this.isNormalWorkflow();
        NodeTitleHelper.updateTitle(node, isNormalWorkflow, forceUpdate);
        this.positionTitle(node);
    };

    Editor.prototype.updateNodeTitles = _.throttle(function(forceUpdate) {
        var self = this;
        var isNormalWorkflow = this.isNormalWorkflow();

        this.forEachNode(function(node) {
            NodeTitleHelper.updateTitle(node, isNormalWorkflow, forceUpdate);
            self.positionTitle(node);
        });
    }, 150);

    Editor.prototype.positionTitles = _.throttle(function() {
        var self = this;

        this.forEachNode(function(node) {
            self.positionTitle(node);
        });
    }, 150);

    Editor.prototype.setMinimapEnabled = function(isEnabled) {
        this.canvas.setMinimapEnabled(isEnabled);
    };

    Editor.prototype.updateMinimap = _.debounce(function() {
        this.canvas.updateMinimap();
    }, 100);

    Editor.prototype.isChainSourceSelected = function() {
        return this.$container.find('.node.chain-mode-source').exists();
    };

    Editor.prototype.getChainSource = function() {
        return this.$container.find('.node.chain-mode-source');
    };

    Editor.prototype.setNodeSelected = function($node, isSelected) {
        if (isSelected) {
            this.instance.addToDragSelection($node);
        } else {
            this.instance.removeFromDragSelection($node);
        }

        Node.setSelected($node, !!isSelected);
        this.onSelectionChanged();
    };

    Editor.prototype.isConnectionPossible = function(source, target) {
        if (source instanceof Node && target instanceof Node) {
            // Loops are not allowed.
            if (source === target) {
                return false;
            }

            // Multiple-way connections are not allowed.
            return !this.hasConnection(function(connection) {
                return connection.source === source && connection.target === target || connection.source === target && connection.target === source;
            });
        }

        return false;
    };

    Editor.prototype.isConnectionAllowed = function(sourceNode, targetNode) {
        var constraints = new ConnectionConstraints(Def.CONNECTION_CONSTRAINTS);
        var self = this;

        var source = {
            node: sourceNode,
            type: sourceNode.getType(),
            connections: {}
        };

        var target = {
            node: targetNode,
            type: targetNode.getType(),
            connections: {}
        };

        Object.defineProperty(source.connections, 'incoming', {
            get: Helpers.caching(self.getNodeIncomingConnections, self, sourceNode)
        });

        Object.defineProperty(source.connections, 'outgoing', {
            get: Helpers.caching(self.getNodeOutgoingConnections, self, sourceNode)
        });

        Object.defineProperty(target.connections, 'incoming', {
            get: Helpers.caching(self.getNodeIncomingConnections, self, targetNode)
        });

        Object.defineProperty(target.connections, 'outgoing', {
            get: Helpers.caching(self.getNodeOutgoingConnections, self, targetNode)
        });

        // A stop icon becomes start icon when it gets outgoing connections (unless it has incoming connections).
        if (source.type == Def.NODE_TYPE_STOP && !this.hasNodeIncomingConnections(sourceNode)) {
            source.type = Def.NODE_TYPE_START;
        }

        // A start icon becomes stop icon when it gets incoming connections (unless it has outgoing connections).
        if (target.type == Def.NODE_TYPE_START && !this.hasNodeOutgoingConnections(targetNode)) {
            target.type = Def.NODE_TYPE_STOP;
        }

        return constraints.check(source, target);
    };

    Editor.prototype.connect = function(source, target, isUserDriven) {
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
    };

    Editor.prototype.deselectAll = function() {
        this.$container.find('.node.ui-selected').removeClass('ui-selected');
        this.instance.clearDragSelection();

        this.clearSelectedSource();
        this.onSelectionChanged();
    };

    Editor.prototype.clearSelectedSource = function() {
        this.$container.find('.node.chain-mode-source').removeClass('chain-mode-source');
    };

    Editor.prototype.alignAll = function() {
        var self = this;

        var nodes = this.getNodes();

        // Coordinates snapshot before auto alignment.
        var oldCoordinatesMap = {};

        nodes.forEach(function(node) {
            oldCoordinatesMap[node.getId()] = node.getCoordinates();
        });

        this.batch(function() {
            AutoAlignment.align(self.getVertices());
        });

        var changes = nodes.filter(function(node) {
            var oldCoordinates = oldCoordinatesMap[node.getId()];
            return oldCoordinates.x != node.getX() || oldCoordinates.y != node.getY();
        }).map(function(node) {
            return {
                node: node,
                coordinates: oldCoordinatesMap[node.getId()]
            };
        });

        if (changes.length) {
            self.undoManager.transaction(function() {
                self.undoManager.operation('nodeMoved', changes);
            });

            this.positionTitles();
            this.updateMinimap();
        }
    };

    Editor.prototype.deleteSelected = function() {
        this.deleteNodes(this.getSelectedNodes());
    };

    Editor.prototype.deleteNode = function(nodeToDelete) {
        var self = this;

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
                        .forEach(function(connection) {
                            if (Def.NODE_TYPE_DEADLINE == connection.target.getType()) {
                                nodesToDelete.push(connection.target);
                            }
                        });
                    break;

                case Def.NODE_TYPE_DEADLINE:
                    this.getNodeIncomingConnections(nodeToDelete)
                        .forEach(function(connection) {
                            if (Def.NODE_TYPE_IMPORT == connection.source.getType()) {
                                nodesToDelete.push(connection.source);
                            }
                        });
                    break;
            }

            nodesToDelete.forEach(function(node) {
                self.undoManager.operation('nodeDeleted', node);
                self.instance.remove(node.get$());
                node.remove();
            });

            this.onSelectionChanged();
            this.positionTitles();
            this.updateFootnotes();
            this.updateMinimap();
        }
    };

    Editor.prototype.deleteNodes = function(nodes) {
        var self = this;

        this.undoManager.transaction(function() {
            self.batch(function() {
                nodes.forEach(function(node) {
                    self.deleteNode(node);
                });
            });
        });
    };

    Editor.prototype.disconnectSelected = function() {
        this.disconnectNodes(this.getSelectedNodes());
    };

    Editor.prototype.disconnectNode = function(node) {
        var self = this;

        this.getNodeConnections(node).forEach(function(connection) {
            self.deleteConnection(connection);
        });
    };

    Editor.prototype.disconnectNodes = function(nodes) {
        var self = this;

        this.batch(function() {
            nodes.forEach(function(node) {
                self.disconnectNode(node);
            });
        });
    };

    Editor.prototype.canDeleteConnection = function(connection) {
        var source, target;

        if (connection.origin) {
            source = connection.source;
            target = connection.target;
        } else {
            source = Node.get(connection.source);
            target = Node.get(connection.target);
        }

        return !(Def.NODE_TYPE_IMPORT == source.getType() && Def.NODE_TYPE_DEADLINE == target.getType());
    };

    Editor.prototype.deleteConnection = function(connection) {
        if (this.canDeleteConnection(connection)) {
            if (connection.origin) {
                this.instance.deleteConnection(connection.origin);
            } else {
                this.instance.deleteConnection(connection);
            }

            return true;
        }

        return false;
    };

    Editor.prototype.deleteConnectionBetween = function($source, $target) {
        var connection = this.getConnectionBetween($source, $target);
        if (connection) {
            this.deleteConnection(connection);
        }
    };

    Editor.prototype.connectSelected = function() {
        return this.connectNodes(this.getSelectedNodes());
    };

    Editor.prototype.connectBetweenSelected = function() {
        var $nodes = this.getSelectedNodes$();

        if ($nodes.length >= 2) {
            return this.connectNodes(this.getNodesBetweenSelected($nodes[0], $nodes[1]));
        } else {
            return false;
        }
    };

    Editor.prototype.getNodesBetweenSelected = function($source, $target) {
        var firstBox = Node.getBox($source);
        var lastBox = Node.getBox($target);

        var regionBox = {
            minX: Math.min(firstBox.minX, lastBox.minX),
            maxX: Math.max(firstBox.maxX, lastBox.maxX),
            minY: Math.min(firstBox.minY, lastBox.minY),
            maxY: Math.max(firstBox.maxY, lastBox.maxY)
        };

        return this.getNodes()
          .filter(function(node) {
              return Node.isInRegion(node.get$(), regionBox);
          });
    };

    Editor.prototype.connectNodes = function(nodes) {
        // sorting by node coordinates
        // (we should connect from left to right and from top to bottom)
        var self = this;

        var sortedNodes = nodes.sort(function(node1, node2) {
            return node1.getX() < node2.getX() ? -1 :
                    node1.getX() > node2.getX() ? 1 :
                    node1.getY() < node2.getY() ? -1 :
                    node1.getY() > node2.getY() ? 1 : 0;
        });

        var connected = false;
        this.undoManager.transaction(function() {
            self.batch(function() {
                for (var i = 0; i < sortedNodes.length - 1; i++) {
                    if (self.connect(sortedNodes[i], sortedNodes[i + 1])) {
                        connected = true;
                    }
                }
            });
        });

        return connected;
    };

    Editor.prototype.getSelectedNodes$ = function() {
        var $elements = [];

        this.$container.find('.node.ui-selected')
            .each(function(index, element) {
                $elements.push($(element));
            });

        return $elements;
    };

    Editor.prototype.getSelectedNodes = function() {
        return this.getSelectedNodes$()
            .map(function($node) {
                return Node.get($node);
            });
    };

    Editor.prototype.getSelectionSize = function() {
        return this.$container.find('.node.ui-selected').length;
    };

    Editor.prototype.onSelectionChanged = function() {
        Ui.setDeleteButtonEnabled(this.getSelectionSize() > 0);
    };

    Editor.prototype.setOnInitialized = function(handler) {
        this.instance.ready(handler);
    };

    Editor.prototype.setOnZoom = function(handler) {
        this.canvasZoomHandler = handler;
    };

    Editor.prototype.setEditable = function(isEditable) {
        isEditable = !!isEditable;

        if (this.editable !== isEditable) {
            this.editable = isEditable;
            this.draggableButtons.setEnabled(isEditable);
            this.$viewport.selectable(isEditable ? 'enable' : 'disable');
            this.forEachNode(function(node) {
                node.setInteractionEnabled(isEditable);
            });
        }
    };

    Editor.prototype.getZoom = function() {
        return this.instance.getZoom();
    };

    Editor.prototype.setZoom = function(scale) {
        this.canvas.setZoom(scale);
        this.instance.setZoom(scale);
    };

    Editor.prototype.setPanningEnabled = function(isEnabled) {
        this.canvas.setPanningEnabled(isEnabled);
        this.$viewport.toggleClass('js-panning', !!isEnabled);

        if (isEnabled) {
            this.$viewport.selectable('disable');
        } else if (this.editable) {
            this.$viewport.selectable('enable');
        }
    };

    Editor.prototype.setMultiConnectionEnabled = function(isEnabled) {
        this.multiConnectionEnabled = isEnabled;
    };

    Editor.prototype.setChainModeEnabled = function(isEnabled) {
        this.chainModeEnabled = isEnabled;
        this.clearSelectedSource();

        if (isEnabled) {
            var source = this.getSelection().shift();
            if (source) {
                this.deselectAll();
                this.setNodeSelected(source, true);
                Node.setChainSource(source, true);
            }
        }
    };

    Editor.prototype.isChainMode = function() {
        return this.chainModeEnabled === true;
    };

    Editor.prototype.updateOverlays = _.debounce(function() {
        this.forEachNode(function(node) {
            if (Node.isDecisionNode(node)) {
                this.updateDecisionBranchesOverlays(node);
            }
        });
    }, 100);

    Editor.prototype.updateDecisionBranchesOverlays = function(node) {
        var connections = this.getNodeOutgoingConnections(node);

        if (Node.isBranchingDecisionNode(node)) {
            var branches = Node.getDecisionBranches(connections);

            if (branches) {
                var titles = NodeTitleHelper.getDecisionBranchesLabels(node);
                Ui.setConnectionLabel(branches.positive, titles.positive);
                Ui.setConnectionLabel(branches.negative, titles.negative);
            } else {
                connections.forEach(function(connection) {
                    Ui.setConnectionLabel(connection, '?');
                });
            }
        } else {
            connections.forEach(function(connection) {
                Ui.setConnectionLabel(connection, false);
            });
        }
    };

    Editor.prototype.editIcon = function(node) {
        if (this.drag == null) {
            if (node) {
                this.deselectAll();
                EditorsHelper.showEditDialog(node, Utils.checkActivation());
            } else {
                var $nodes = this.getSelection();
                if ($nodes.length == 1) {
                    this.deselectAll();
                    EditorsHelper.showEditDialog(Node.get($nodes[0]), Utils.checkActivation());
                }
            }
        }
    };

    Editor.prototype.editIconComment = function(node) {
        EditorsHelper.showIconCommentDialog(node);
    };

    Editor.prototype.serializeIcons = function() {
        var icons = this.getNodes().map(function (node) {
            return node.serialize();
        });

        var connectionsBySourceId = {};

        this.getConnections().forEach(function (connection) {
            var nodeConnections = connectionsBySourceId[connection.source.id];
            if(!nodeConnections) {
                nodeConnections = [];
            }
            nodeConnections.push(connection);
            connectionsBySourceId[connection.source.id] = nodeConnections;
        });

        icons.forEach(function (icon) {
            var nodeConnections = connectionsBySourceId[icon.id];
            if(nodeConnections) {
                icon.connections = nodeConnections.map(function (connection) {
                    return { targetIconId: connection.target.id };
                });
            } else {
                icon.connections = [];
            }
        });

        return JSON.stringify(icons);
    };

    Editor.prototype.undo = function() {
      this.undoManager.undo();
    };

    Editor.prototype.getUndoManager = function() {
        return this.undoManager;
    };

    Editor.prototype.enlargeEditor = function(positiveCallback, negativeCallback) {
        var self = this;
        var done = _.isFunction(positiveCallback) ? positiveCallback : _.noop;
        var fail = _.isFunction(negativeCallback) ? negativeCallback : _.noop;

        var promise = AGN.Lib.Confirm.createFromTemplate({}, 'enlarged-editor-template');
        $('#modalCampaignEditorContainer').append($('#campaignEditorBody'));
        self.$viewport.selectable('option', 'appendTo', '#modalCampaignEditorContainer');

        promise
          .done(done)
          .fail(fail)
          .always(function() {
            $('#pageCampaignEditorContainer').append($('#campaignEditorBody'));
            self.$viewport.selectable('option', 'appendTo', 'body');
        });
    };

    Editor.prototype.hasUnsavedChanges = function() {
        return this.getUndoManager().canUndo();
    };

    Editor.prototype.isStatisticEnabled = function() {
        return this.statisticEnabled;
    };

    Editor.prototype.toggleStatistic = function() {
        this.setStatisticEnabled(!this.statisticEnabled);
    };

    Editor.prototype.setStatisticEnabled = function(enable) {
        var self = this;
        var deferred = $.Deferred();

        this.updateStatistic(deferred, enable);
        deferred.done(function () {
            self.statisticEnabled = enable;
            Ui.updateStatisticButton(enable);
        }).always(function() {
            self.updateNodeTitles(true);
            self.updateOverlays();
        });
    };

    Editor.prototype.updateStatistic = function(deferred, enable) {
        var self = this;

        if (enable) {
            $.post(AGN.url("/workflow/loadStatistics.action"), {workflowId: Def.workflowId})
              .done(function(data){
                self.forEachNode(function(node) {
                    var statistic = data[node.id];
                    if (statistic) {
                        node.setStatistic(true, statistic);
                    }
                })

                deferred.resolve();
            }).fail(function() {
                deferred.reject();
                AGN.Lib.Messages(t("Error"), t("defaults.error"), "alert");
            });

        } else {
            self.forEachNode(function(node) {
               node.setStatistic(false);
            });

            deferred.resolve();
        }

        return deferred.promise();
    };

    AGN.Lib.WM.Editor = Editor;
})();
