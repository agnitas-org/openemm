(function ($) {
    var Helpers = AGN.Lib.Helpers;

    //
    //  Constrains
    //

    function Constraints(rules) {
        this.rules = rules;
    }

    Constraints.prototype.isConstraint = function (value) {
        return _.isBoolean(value) || _.isFunction(value);
    };

    Constraints.prototype.evaluate = function (constraint, source, target, nodesMap) {
        if (constraint === true || constraint === false) {
            return constraint;
        }

        if (_.isFunction(constraint)) {
            return constraint(source, target, nodesMap);
        }

        return undefined;
    };

    Constraints.prototype.getRulesToCheck = function (source, target) {
        return [{from: source.type, to: [target.type, '*']}, {from: '*', to: [target.type, '*']}];
    };

    Constraints.prototype.getRelevantConstraints = function (source, target) {
        var isConstraint = this.isConstraint;
        var rulesToCheck = this.getRulesToCheck(source, target);

        var rules = this.rules;
        var constraints = [];

        rulesToCheck.forEach(function (rule) {
            var isRuleMissing = true;

            if (_.has(rules, rule.from)) {
                var value = rules[rule.from];

                if (isConstraint(value)) {
                    constraints.push(value);
                    isRuleMissing = false;
                } else {
                    rule.to.forEach(function (to) {
                        if (_.has(rules[rule.from], to)) {
                            value = rules[rule.from][to];

                            if (isConstraint(value)) {
                                constraints.push(value);
                                isRuleMissing = false;
                            }
                        }
                    });
                }
            }

            if (isRuleMissing && rule.from != '*') {
                constraints.push(false);
            }
        });

        return constraints;
    };

    Constraints.prototype.check = function (source, target, nodesMap) {
        var constraints = this.getRelevantConstraints(source, target);
        var result = false;

        for (var i = 0; i < constraints.length; i++) {
            if (this.evaluate(constraints[i], source, target, nodesMap)) {
                result = true;
            } else {
                return false;
            }
        }

        return result;
    };

    //
    //  CampaignManager
    //

    var CampaignManager = function CampaignManager(data) {

        /*******************************************************************************************************************
         * "Constants"
         ******************************************************************************************************************/

            // --- get open or automatic flag for Stop Icon --- //
        var nodeFactory = AGN.Lib.WM.NodeFactory;
        this.IS_ACTION_BASED = false;
        this.IS_DATE_BASED = false;


        // --- stage states --- //

        this.STATE_WAITING = 0;
        this.STATE_DRAGGING_STAGE_WAIT_MOUSE = 1;
        this.STATE_DRAGGING_STAGE = 2;
        this.STATE_SELECTING_NODES = 3;
        this.STATE_CREATING_NODE = 4;
        this.STATE_CREATING_CONNECTION = 5;
        this.STATE_SPACE_RESTORED = 6;
        this.STATE_EXPANDING_ICONS_SET = 7;
        this.STATE_AUTOMATICALLY_CREATING_CONNECTION = 8; //use this state for process of connecting auto supplemented nodes

        // --- EndOf stage states --- //

        // --- z-indexes --- //
        // some are not used and are here just to clarify and group together all z-index related logic
        // modifications in this section should take reflection inside CSS

        // page level
        this.Z_INDEX_VIEW_PORT = 10;
        this.Z_INDEX_ICON_PANEL = 50; // icon panel above the view port

        // panel level
        this.Z_INDEX_DRAGGABLE_BUTTON = 60;

        // stage level
        this.Z_INDEX_ICON_NODE = 10; // lowest object on the stage
        this.Z_INDEX_CONNECT_RAPID_BUTTON = 20;
        this.Z_INDEX_JSPLUMB_DRAG_OPTIONS = 25;
        this.Z_INDEX_NAVIGATOR_GLOBE = 30;
        this.Z_INDEX_NAVIGATOR_ARROWS = 31;
        this.Z_INDEX_CONTEXT_MENU = 40; // highest object on the stage

        // --- EndOf Setup z-indexes --- //

        this.CONNECTION_CONSTRAINTS = {
            '*': {
                deadline: function (source, target, map) {
                    // Make sure that there's no incoming connection from import icon.
                    return !target.connections.incoming.some(function (connection) {
                        return connection.source in map && map[connection.source].type == 'import';
                    });
                },

                mailing: function (source, target /*, map */) {
                    // The second incoming connection to mailing icon is not allowed.
                    return target.connections.incoming.length == 0;
                },

                datebased_mailing: function (source, target /*, map */) {
                    // The second incoming connection to mailing icon is not allowed.
                    return target.connections.incoming.length == 0;
                }
            },
            start: {
                recipient: true,
                form: true,
                import: true,
                export: true
            },
            stop: false,
            decision: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                form: true,
                archive: true,
                actionbased_mailing: true,
                datebased_mailing: true,
                followup_mailing: true,
                mailing: true
            },
            parameter: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                form: true,
                archive: true,
                actionbased_mailing: true,
                datebased_mailing: true,
                followup_mailing: true,
                mailing: true
            },
            deadline: {
                decision: true,
                parameter: true,
                report: true,
                recipient: true,
                archive: true,
                actionbased_mailing: true,
                datebased_mailing: true,
                followup_mailing: true,
                mailing: true,
                import: true,
                export: true
            },
            report: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                archive: true,
                actionbased_mailing: true,
                datebased_mailing: true,
                followup_mailing: true,
                mailing: true,
                import: true,
                export: true
            },
            recipient: {
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                form: true,
                archive: true,
                actionbased_mailing: true,
                datebased_mailing: true,
                followup_mailing: true,
                mailing: true,
                import: true,
                export: true
            },
            actionbased_mailing: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                form: true,
                archive: true,
                import: true,
                export: true
            },
            datebased_mailing: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                form: true,
                archive: true,
                import: true,
                export: true
            },
            followup_mailing: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                form: true,
                archive: true,
                import: true,
                export: true
            },
            mailing: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                form: true,
                archive: true,
                import: true,
                export: true
            },
            archive: {
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                actionbased_mailing: true,
                datebased_mailing: true,
                followup_mailing: true,
                mailing: true,
                import: true,
                export: true
            },
            form: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                form: true,
                archive: true,
                actionbased_mailing: true,
                datebased_mailing: true,
                followup_mailing: true,
                mailing: true,
                import: true,
                export: true
            },
            import: {
                deadline: function (source /*, target, map */) {
                    return source.connections.outgoing.length == 0;
                }
            },
            export: {
                stop: true,
                decision: true,
                parameter: true,
                deadline: true,
                report: true,
                recipient: true,
                actionbased_mailing: true,
                datebased_mailing: true,
                followup_mailing: true,
                mailing: true,
                import: true,
                export: true,
                archive: true,
                form: true
            }
        };



        /*******************************************************************************************************************
         * Private members
         ******************************************************************************************************************/

            // closure-safe "this" reference
        var self = this;
        this.workflowId = data.workflowId;
        this.pageContextSessionId = data.pageContextSessionId;
        this.isActivated = data.isActivated;

        this.isPdf = data.isPdfGenerating;
        this.historyWasOverloaded = false;
        this.ignoreChangesThisTime = false;

        // setup viewport variables
        var viewPortDOMId = "viewPort";
        var viewPortJQ = jQuery("#" + viewPortDOMId);
        var viewPortWidth = viewPortJQ.get(0).clientWidth;
        var viewPortHeight = viewPortJQ.get(0).clientHeight;

        // setup canvas variables
        var editorCanvasDOMId = "editorCanvas";
        var editorCanvasLeft = 0;
        var editorCanvasTop = 0;
        var editorCanvasWidth = viewPortWidth;
        var editorCanvasHeight = viewPortHeight;


        var editorCanvasJQ = $('<div></div>')
          .attr('id', editorCanvasDOMId)
          .css({
                position: "absolute",
                left: editorCanvasLeft,
                top: editorCanvasTop,
                width: editorCanvasWidth,
                height: editorCanvasHeight
                // to see what's going on inside
                //, background: "#eee"
            })
          .appendTo($('#' + viewPortDOMId));

        var editorCanvasWidthGlobalIncrease = 0;
        var editorCanvasHeightGlobalIncrease = 0;

        // setup bounding rectangle variables
        var boundNodesDOMId = "bounder";
        var boundNodesLeft = data.editorPositionLeft;
        var boundNodesTop = data.editorPositionTop;
        var localeDateTimePattern = data.localeDateTimePattern;
        var boundNodesWidth = false;
        var boundNodesHeight = false;
        var noContextMenu = data.noContextMenu;
        var boundNodesJQ = $('<div></div>')
          .attr('id', boundNodesDOMId)
          .css({
              position: "absolute"
              // to see what's going on inside
              // , background: "#aaaa
               })
          .appendTo($('#' + editorCanvasDOMId));

        // setup navigator
        var navigatorJQ = $('#navigator.js-navigation');

        // does not require initiation (yet?)
        var campaignManagerSettings = data.campaignManagerSettings;


        var restoreSpaceFields = data.restoreSpaceFields;
        var campaignManagerScale = data.campaignManagerScale;
        var allUsedEntity = data.allUsedEntity;

        var campaignManagerNodes = new AGN.Lib.WM.CampaignManagerNodes({
            nodesContainerDOMId: boundNodesDOMId,
            localeDateTimePattern: localeDateTimePattern,
            campaignManagerScale: campaignManagerScale,
            campaignManager: self,
            nodeFactory: nodeFactory,
            allUsedEntity: allUsedEntity,
            autoOptData: data.autoOptData
        });

        var editorsHelper = new AGN.Lib.WM.EditorsHelper(self, nodeFactory, campaignManagerSettings);

        var autoLayout = new AGN.Lib.WM.AutoLayout(campaignManagerSettings);

        var chainProcessor = new AGN.Lib.WM.ChainProcessor({
            campaignManager: self,
            pageContextSessionId: this.pageContextSessionId,
            editorsHelper: editorsHelper,
            nodeFactory: nodeFactory
        });


        // keeps state of whole stage (see above constants)
        var currentState = this.STATE_WAITING;

        // keeps connections between nodes separately from jsPlumb to be able to
        // restore them once stage is rearranged and new instance of jsPlumb is created
        var nodeConnections = [];

        var currentScaleSliderPosition = campaignManagerScale.getCurrentScaleSliderPosition();

        var jsPlumbInstance = false;

        //This callback function will call after some changes
        var workflowManagerStateChangedCallback = "";

        var undoHistory = [];
        var historyStackChangedCallback = null;

        var connectionNotAllowedCallback = null;

        var editingNotAllowedCallback = null;

        var shiftDown = false;

        var draggedElements = null;

        var commentControls = new AGN.Lib.WM.CommentControls(self, data);

        /**
         * Updates all nodes DOM positions and boundNodes div's position/size (not DOM, variables only)
         */
        updateBoundNodesVariables = function () {

            // if (campaignManagerNodes.count()) {

            var result = campaignManagerNodes.normalizeNodes();
            if (result.collapsedX) {
                boundNodesLeft -= result.collapsedX * campaignManagerScale.getScaledGridSize();
            }
            if (result.collapsedY) {
                boundNodesTop -= result.collapsedY * campaignManagerScale.getScaledGridSize();
            }

            if (boundNodesLeft < 0) {
                boundNodesLeft = 0;
            }

            var nodes = campaignManagerNodes.getNodes();

            for (var i in nodes) {
                campaignManagerNodes.updateNodeView(nodes[i], campaignManagerScale, allUsedEntity.allMailings);
            }

            var right;
            var bottom;
            var top;
            var maxRight = false;
            var maxTop = false;
            var maxBottom = false;

            for (i in nodes) {
                top = parseInt(nodes[i].elementJQ.find("div.icon-extra-info").css("top"));
                right = parseInt(nodes[i].element.style.left) + parseInt(nodes[i].element.style.width);
                bottom = parseInt(nodes[i].element.style.top) + parseInt(nodes[i].element.style.height);
                if (maxRight === false || maxRight < right) {
                    maxRight = right;
                }
                if (maxTop === false || maxTop < top) {
                    maxTop = top;
                }

                if (maxBottom === false || maxBottom < bottom) {
                    maxBottom = bottom;
                }
            }
            if (boundNodesTop < 0) {
                boundNodesTop = maxTop ? maxTop : 0;
            }
            boundNodesWidth = maxRight;
            boundNodesHeight = maxBottom;
            // }
        };

        /**
         * Updates boundNodes div's DOM position
         */
        updateBoundNodesDOM = function () {
            boundNodesJQ.css({
                left: boundNodesLeft,
                top: boundNodesTop,
                width: boundNodesWidth + campaignManagerScale.getScaledGridSize(), // create margin on right = 1 grid cell
                height: boundNodesHeight + campaignManagerScale.getScaledGridSize() // create margin on bottom = 1 grid cell
            });
        };

        /**
         * Updates editorCanvas variables and DOM position/size
         *
         * Calls updateBoundNodesVariables and updateBoundNodesDOM so there is no necessity to call
         * them when updateEditorCanvas called
         */
        updateEditorCanvas = function () {
            updateBoundNodesVariables();

            var localIncreaseEditorCanvasWidth = 0;
            var localIncreaseEditorCanvasHeight = 0;

            var deltaX = parseInt(editorCanvasJQ.css("left"));
            var deltaY = parseInt(editorCanvasJQ.css("top"));

            boundNodesLeft = parseInt(boundNodesLeft) + deltaX;
            if (boundNodesLeft <= 0) {
                localIncreaseEditorCanvasWidth += -boundNodesLeft;
                editorCanvasWidthGlobalIncrease += -boundNodesLeft;
                boundNodesLeft = 0;
            }
            boundNodesTop = parseInt(boundNodesTop) + deltaY;
            if (boundNodesTop <= 0) {
                localIncreaseEditorCanvasHeight += -boundNodesTop;
                editorCanvasHeightGlobalIncrease += -boundNodesTop;
                boundNodesTop = 0;
            }
            // right and bottom edges of canvas are not prolonged since
            // "bound" div works for scrollbars in this case as necessary

            editorCanvasJQ.css({
                left: 0, //left,
                top: 0, //top,
                width: viewPortWidth + editorCanvasWidthGlobalIncrease,
                height: viewPortHeight + editorCanvasHeightGlobalIncrease
            });

            viewPortJQ.scrollLeft(viewPortJQ.scrollLeft() + localIncreaseEditorCanvasWidth);
            viewPortJQ.scrollTop(viewPortJQ.scrollTop() + localIncreaseEditorCanvasHeight);

            // --- fix left and top excessive canvas fields --- //

            var cutFromTheLeft = 0;
            if (boundNodesLeft - viewPortJQ.scrollLeft() > 0) {
                // if boundNodes left side inside visible area
                cutFromTheLeft = viewPortJQ.scrollLeft();
                boundNodesLeft -= viewPortJQ.scrollLeft();
            }
            else if (viewPortJQ.scrollLeft() - boundNodesLeft > campaignManagerScale.getScaledGridSize()) {
                // if boundNodes left side outside visible area
                cutFromTheLeft = boundNodesLeft;
                boundNodesLeft = 0;
            }
            editorCanvasWidthGlobalIncrease -= cutFromTheLeft;

            var cutFromTheTop = 0;
            if (boundNodesTop - viewPortJQ.scrollTop() > 0) {
                cutFromTheTop = viewPortJQ.scrollTop();
                boundNodesTop -= viewPortJQ.scrollTop();
            }
            else if (viewPortJQ.scrollTop() - boundNodesTop > campaignManagerScale.getScaledGridSize()) {
                cutFromTheTop = boundNodesTop;
                boundNodesTop = 0;
            }
            editorCanvasHeightGlobalIncrease -= cutFromTheTop;

            var keepScrollLeft = viewPortJQ.scrollLeft();
            var keepScrollTop = viewPortJQ.scrollTop();
            editorCanvasJQ.css({
                width: viewPortWidth + editorCanvasWidthGlobalIncrease,
                height: viewPortHeight + editorCanvasHeightGlobalIncrease
            });

            viewPortJQ.scrollLeft(keepScrollLeft - cutFromTheLeft);
            viewPortJQ.scrollTop(keepScrollTop - cutFromTheTop);

            // --- EndOf fix left and top excessive canvas fields --- //

            // --- fix right and bottom excessive canvas fields --- //

            var cutFromTheRight;
            if (viewPortJQ.scrollLeft() + viewPortWidth < boundNodesWidth) {
                // if boundNodes right side outside visible area
                cutFromTheRight = viewPortWidth + editorCanvasWidthGlobalIncrease - boundNodesWidth;
            }
            else {
                // if boundNodes right side inside visible area
                cutFromTheRight = viewPortWidth + editorCanvasWidthGlobalIncrease - viewPortJQ.scrollLeft() - viewPortWidth;
            }
            editorCanvasWidthGlobalIncrease -= cutFromTheRight;
            var cutFromTheBottom;
            if (viewPortJQ.scrollTop() + viewPortHeight < boundNodesHeight) {
                cutFromTheBottom = viewPortHeight + editorCanvasHeightGlobalIncrease - boundNodesHeight;
            }
            else {
                cutFromTheBottom = viewPortHeight + editorCanvasHeightGlobalIncrease - viewPortJQ.scrollTop() - viewPortHeight;
            }
            editorCanvasHeightGlobalIncrease -= cutFromTheBottom;
            editorCanvasJQ.css({
                width: viewPortWidth + editorCanvasWidthGlobalIncrease,
                height: viewPortHeight + editorCanvasHeightGlobalIncrease
            });

            // --- EndOf fix right and bottom excessive canvas fields --- //
            updateBoundNodesDOM();
        };

        /**
         * (Re)creates jsPlumber and recreate all connections
         */
        resetJsPlumber = function () {
            // cleanup existing instance
            if (jsPlumbInstance) {
                jsPlumbInstance.reset();
            } else {
                // get new instance
                jsPlumbInstance = jsPlumb.getInstance();

                jsPlumbInstance.bind("beforeDrop",
                    function (params) {
                        if (self.checkActivation()) {
                            return;
                        }

                        var source = params.sourceId;
                        var target = params.targetId;
                        if (self.connectIntermediateNodes(source, target)) {
                            return false;
                        } else {
                            return self.isConnectionAllowed(source, target)
                        }
                    }
                );
            }

            jsPlumbInstance.bind("ready", function () {
                var anchors = [
                        [0.5, 0.6, 0, 1],
                        [0.5, 0.4, 0, -1],
                        [0.6, 0.5, 1, 0],
                        [0.4, 0.5, -1, 0]
                ];
                jsPlumbInstance.setRenderMode(campaignManagerSettings.renderMode);
                jsPlumbInstance.importDefaults({
                    Anchors: [anchors, anchors],
                    DragOptions: {cursor: "pointer", zIndex: self.Z_INDEX_JSPLUMB_DRAG_OPTIONS},
                    Endpoints: ["Blank", "Blank"],
                    Connector: ["Flowchart", {stub: 5, gap: 0, midpoint: 0.5}],
                    PaintStyle: {
                        strokeStyle: campaignManagerSettings.lineColor,
                        lineWidth: campaignManagerScale.getLineWidth()
                    },
                    ConnectionOverlays: [["PlainArrow", {
                        width: campaignManagerScale.getArrowWidth(),
                        length: campaignManagerScale.getArrowLength(),
                        location: 0.52,
                        id: "arrow"
                    }]]
                });

                jsPlumbInstance.draggable(jQuery(campaignManagerNodes.getIconNodesSelector()), getJSPlumbDraggableProperties());

                campaignManagerNodes.resetUsedAnchors();

                for (var i = 0; i < nodeConnections.length; i++) {
                    var jsPlumbConnection = jsPlumbInstance.connect(nodeConnections[i]);
                    processJSPlumbConnection(jsPlumbConnection);
                }

                jsPlumbInstance.bind("jsPlumbConnection", function (data) {
                    if (!self.connectNodes(data.sourceId, data.targetId, false)) {
                        // cancel connection
                        resetJsPlumber();
                    }

                    //we should not to do any view updates during connecting new nodes in auto mode
                    if (self.getCurrentState() != self.STATE_AUTOMATICALLY_CREATING_CONNECTION) {
                        updateEditorCanvas();
                    }

                    updateNodeAdditionalViewElements();

                    //we should not to do any view updates during connecting new nodes in auto mode
                    if (self.getCurrentState() != self.STATE_AUTOMATICALLY_CREATING_CONNECTION) {
                        updateEditorCanvas();
                    }
                });
            });
        };

        processJSPlumbConnection = function (jsPlumbConnection) {
            var usedAnchorsOfNode = {};

            for (var i = 0; i < jsPlumbConnection.endpoints.length; i++) {
                var endPoint = jsPlumbConnection.endpoints[i];
                if (!usedAnchorsOfNode.hasOwnProperty(endPoint.elementId)) {
                    usedAnchorsOfNode[endPoint.elementId] = [];
                }

                if (endPoint.anchor.x == 0.5 && endPoint.anchor.y == 0.6) {
                    if (jQuery.inArray(campaignManagerNodes.BOTTOM, usedAnchorsOfNode[endPoint.elementId]) == -1) {
                        usedAnchorsOfNode[endPoint.elementId].push(campaignManagerNodes.BOTTOM);
                    }
                } else if (endPoint.anchor.x == 0.5 && endPoint.anchor.y == 0.4) {
                    if (jQuery.inArray(campaignManagerNodes.TOP, usedAnchorsOfNode[endPoint.elementId]) == -1) {
                        usedAnchorsOfNode[endPoint.elementId].push(campaignManagerNodes.TOP);
                    }
                } else if (endPoint.anchor.x == 0.6 && endPoint.anchor.y == 0.5) {
                    if (jQuery.inArray(campaignManagerNodes.RIGHT, usedAnchorsOfNode[endPoint.elementId]) == -1) {
                        usedAnchorsOfNode[endPoint.elementId].push(campaignManagerNodes.RIGHT);
                    }
                } else if (endPoint.anchor.x == 0.4 && endPoint.anchor.y == 0.5) {
                    if (jQuery.inArray(campaignManagerNodes.LEFT, usedAnchorsOfNode[endPoint.elementId]) == -1) {
                        usedAnchorsOfNode[endPoint.elementId].push(campaignManagerNodes.LEFT);
                    }
                }
            }

            var nodes = campaignManagerNodes.getNodes();
            for (var i in nodes) {
                if (usedAnchorsOfNode.hasOwnProperty(nodes[i].elementJQ.attr("id"))) {
                    nodes[i].usedAnchors = nodes[i].usedAnchors.concat(usedAnchorsOfNode[nodes[i].elementJQ.attr("id")]);
                    jQuery.uniqueSort(nodes[i].usedAnchors);
                }
            }
        };

        /**
         * Rearrange the whole stage
         */
        rearrangeStage = function () {
            jQuery("#connectRapidButton").width(campaignManagerSettings.rapidButtonSize * campaignManagerScale.getCurrentScale());
            jQuery("#connectRapidButton").height(campaignManagerSettings.rapidButtonSize * campaignManagerScale.getCurrentScale());

            updateEditorCanvas();
            resetJsPlumber();
            updateNodeAdditionalViewElements();
        };

        updateNodeAdditionalViewElements = function () {
            var nodes = campaignManagerNodes.getNodes();
            for (var i in nodes) {
                campaignManagerNodes.updateNodeAdditionalViewElements(nodes[i], campaignManagerScale);
            }
        };

        connectSelectedNodes = function () {
            // turn off save snapshots during adding connections
            var prevNeedSaveSnapshot = self.needSaveSnapshot;
            self.needSaveSnapshot = false;
            // create one snapshot for all connections
            self.saveSnapshot();
            var selected = AGN.Lib.WM.CampaignManagerSelection.getSelected();
            var prev = null;
            var connected = false;
            // sorting if ids according to coordinates
            // (we should connect from left to rigth and from top to bottom)
            var idsToConnect = selected.slice(0);
            idsToConnect.sort(function (id1, id2) {
                var node1 = campaignManagerNodes.getNodeById(id1);
                var node2 = campaignManagerNodes.getNodeById(id2);
                if (node1.x < node2.x) {
                    return -1;
                }
                else if (node1.x > node2.x) {
                    return 1;
                }
                else if (node1.y < node2.y) {
                    return -1;
                }
                else if (node1.y > node2.y) {
                    return 1;
                } else {
                    return 0;
                }
            });
            jQuery.each(idsToConnect, function (index, current) {
                if (prev != null) {
                    self.connectNodes(prev, current, true);
                    connected = true;
                }
                prev = current;
            });
            // TODO: implement as necessary
            AGN.Lib.WM.CampaignManagerSelection.prevSelected = null;
            self.needSaveSnapshot = prevNeedSaveSnapshot;
            return connected;
        };

        getCanvasOffsetX = function () {
            return jQuery("#toolbarLeft").offset().left + jQuery("#toolbarLeft").width() - viewPortJQ.scrollLeft();
        };

        getCanvasOffsetY = function () {
            return jQuery("#toolbarTop").offset().top + jQuery("#toolbarTop").height() - viewPortJQ.scrollTop();
        };

        justifyNodeElement = function (node) {
            jsPlumbInstance.draggable(node.element, getJSPlumbDraggableProperties());

            commentControls.addCommentEllipsis(node);

            jQuery(node.elementJQ)
              .on("click", function (e) {
                    if (currentState == self.STATE_WAITING || self.STATE_SPACE_RESTORED) {
                        var node = jQuery(this);
                        if (node.hasClass("preventClickAfterDrop")) {
                            node.removeClass("preventClickAfterDrop");
                        }
                        else {
                            AGN.Lib.WM.CampaignManagerSelection.handleClick(e, jQuery(this));
                        }
                    }
                })
                .mouseover(nodeMouseOver)
                .mouseout(nodeMouseLeave)
                .mousemove(checkRapidButton)
                .dblclick(function (e) {
                    if (node.data.editable !== false) {
                        self.editNode(node, true);
                    } else {
                        if (typeof(editingNotAllowedCallback) == "function") {
                            editingNotAllowedCallback();
                        }
                    }
                });
        };


        /**
         * Initializations of node D&D
         */
        getJSPlumbDraggableProperties = function () {
            var selected;
            var selectedCache;

            var timeout = false;
            var queuedJob = false;

            return {
                distance: (jQuery.browser.msie && jQuery.browser.version < 9) ? 5 : 10,

                // when the drag is started - select dragged element
                start: function (e, ui) {
                    // does current node exist in "selected" list?
                    var exists = false;
                    selected = AGN.Lib.WM.CampaignManagerSelection.getSelected();
                    draggedElements = selectedCache;
                    for (var i = 0; i < selected.length; i++) {
                        if (selected[i] == ui.helper.attr("id")) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        // we are not going to mass move
                        AGN.Lib.WM.CampaignManagerSelection.clear();
                        AGN.Lib.WM.CampaignManagerSelection.select(ui.helper);
                    }

                    // cache selected nodes to speedup "drag" event as much as possible
                    selected = AGN.Lib.WM.CampaignManagerSelection.getSelected();
                    selectedCache = [];
                    for (i = 0; i < selected.length; i++) {
                        var element = document.getElementById(selected[i]);
                        selectedCache[i] = {
                            DOMElement: element,
                            left: parseInt(element.style.left),
                            top: parseInt(element.style.top)
                        };
                    }
                },

                drag: function (e, ui) {
                    commentControls.hidePopover(true);
                    var deltaX = ui.position.left - ui.originalPosition.left;
                    var deltaY = ui.position.top - ui.originalPosition.top;
                    for (var i = 0; i < selectedCache.length; i++) {
                        if (ui.helper.get(0) != selectedCache[i].DOMElement) {
                            selectedCache[i].DOMElement.style.left = (selectedCache[i].left + deltaX) + "px";
                            selectedCache[i].DOMElement.style.top = (selectedCache[i].top + deltaY) + "px";
                        }
                    }

                    // comment if dragging is too slow
                    if (AGN.Lib.WM.CampaignManagerSelection.selected.length > 1) {
                        var repaintJob = function () {
                            try {
                                jsPlumbInstance.repaint(AGN.Lib.WM.CampaignManagerSelection.selected);
                            } finally {
                                if (queuedJob) {
                                    var nextJob = queuedJob;
                                    queuedJob = false;
                                    timeout = setTimeout(nextJob, 50);
                                } else {
                                    timeout = false;
                                }
                            }
                        };

                        queuedJob = repaintJob;
                        if (!timeout) {
                            queuedJob = false;
                            timeout = setTimeout(repaintJob, 50);
                        }
                    }
                },

                stop: function (e, ui) {
                    draggedElements = null;
                    var intersected = false;
                    for (var i = 0; i < selectedCache.length; i++) {
                        selectedCache[i].x = campaignManagerScale.pxToCoordinate(parseInt(selectedCache[i].DOMElement.style.left));
                        selectedCache[i].y = campaignManagerScale.pxToCoordinate(parseInt(selectedCache[i].DOMElement.style.top));
                        if (!campaignManagerNodes.isEmptyCell(selectedCache[i].x, selectedCache[i].y, selectedCache)) {
                            intersected = true;
                            break;
                        }
                    }

                    if (!intersected) {
                        self.saveSnapshot();
                        for (i = 0; i < selectedCache.length; i++) {
                            var node = campaignManagerNodes.getNodes()[jQuery(selectedCache[i].DOMElement).data("index")];
                            campaignManagerNodes.updateNode(
                                node.id,
                                {
                                    x: selectedCache[i].x,
                                    y: selectedCache[i].y
                                }
                            );
                        }
                    }
                    rearrangeStage();

                    ui.helper.addClass("preventClickAfterDrop");
                }
            };
        };

        var savedSelectableStopEvent = undefined;

        var isTheSamePosition = function (first, second) {
            if (first === second) {
                return true;
            }

            return first.screenX == second.screenX && first.screenY == second.screenY;
        };

        /**
         * Initialization of rubber band selector
         */
        var getSelectableProperties = function () {
            var startXPx;
            var startYPx;
            return {
                distance: 10,
                start: function (e, ui) {
                    startXPx = e.pageX;
                    startYPx = e.pageY;
                    blurFieldsWithSpaces();
                    currentState = self.STATE_SELECTING_NODES;
                },
                stop: function (e, ui) {
                    var endXPx = e.pageX;
                    var endYPx = e.pageY;
                    if (endXPx < startXPx) {
                        var tmp = startXPx;
                        startXPx = endXPx;
                        endXPx = tmp;
                    }
                    if (endYPx < startYPx) {
                        tmp = startYPx;
                        startYPx = endYPx;
                        endYPx = tmp;
                    }
                    var halfGridSize = campaignManagerScale.getScaledGridSize() / 2;
                    var foundNodes = campaignManagerNodes.getNodesFromRegion(
                        campaignManagerScale.pxToCoordinate(startXPx - halfGridSize - getCanvasOffsetX() - boundNodesLeft),
                        campaignManagerScale.pxToCoordinate(startYPx - halfGridSize - getCanvasOffsetY() - boundNodesTop),
                        campaignManagerScale.pxToCoordinate(endXPx - halfGridSize - getCanvasOffsetX() - boundNodesLeft),
                        campaignManagerScale.pxToCoordinate(endYPx - halfGridSize - getCanvasOffsetY() - boundNodesTop),
                        0
                    );
                    AGN.Lib.WM.CampaignManagerSelection.validateMultiselection(e);
                    for (i in foundNodes) {
                        AGN.Lib.WM.CampaignManagerSelection.select(jQuery(foundNodes[i].element));
                    }
                    currentState = self.STATE_WAITING;
                    savedSelectableStopEvent = e.originalEvent.originalEvent;
                }
            };
        };

        var nodeMouseOver = function() {
            if (currentState != self.STATE_WAITING) return;
            var element = $(this);
            var node = campaignManagerNodes.getNodeById(element.attr("id"));
            showRapidButton(element, node);
            commentControls.showCommentPopover(node);
        };

        var nodeMouseLeave = function() {
            if (currentState != self.STATE_WAITING) return;
            hideRapidButton();
            commentControls.hideCommentPopover();
        };

        /* Rapid button functionality */

        var showRapidButton = function (element, node) {
            jsPlumbInstance.unmakeEverySource();
            jQuery(element).append(jQuery("#connectRapidButton"));
            jQuery("#connectRapidButton").offset({
                top: jQuery(element).offset().top,
                left: jQuery(element).offset().left + jQuery(element).width() - jQuery("#connectRapidButton").width()
            });
            jQuery("#connectRapidButton").css("visibility", "visible");
            jsPlumbInstance.makeSource(jQuery("#connectRapidButton"), {
                parent: jQuery(element),
                dragOptions: {
                    start: function (e, ui) {
                        blurFieldsWithSpaces();
                        currentState = self.STATE_CREATING_CONNECTION;
                    },
                    stop: function (e, ui) {
                        currentState = self.STATE_WAITING;
                        hideRapidButton();
                    }
                }
            });
            jsPlumbInstance.makeTarget(jQuery(campaignManagerNodes.getIconNodesSelector()), {
                dropOptions: {hoverClass: "hover"}
            });
        };

        var hideRapidButton = function () {
            jsPlumbInstance.unmakeEverySource();
            jsPlumbInstance.unmakeEveryTarget();
            jQuery("#connectRapidButton").css("visibility", "hidden");
        };

        var checkRapidButton = function () {
            if (currentState != self.STATE_WAITING) return;
            if (jQuery(this).attr("id") != jQuery("#connectRapidButton").parent().attr("id")) {
                jQuery(this).append(jQuery("#connectRapidButton"));
                jQuery("#connectRapidButton").css("visibility", "visible");
            }
        };

        var detachRapidButton = function () {
            $('#invisible').append($('#connectRapidButton'));
        };

        /*******************************************************************************************************************
         * Public members
         ******************************************************************************************************************/
        this.needSaveSnapshot = true;

        this.extendAllUsedEntities = function (data) {
            $.extend(true, allUsedEntity, data);
        };

        this.noSnapshot = function (code) {
            var value = self.needSaveSnapshot;
            try {
                self.needSaveSnapshot = false;
                code();
            } finally {
                self.needSaveSnapshot = value;
            }
        };

        this.getBoundNodesDOM = function () {
            return {
                left: boundNodesJQ.css("left"),
                top: boundNodesJQ.css("top"),
                width: boundNodesJQ.css("width"),
                height: boundNodesJQ.css("height")
            };
        };

        this.getCurrentState = function () {
            return currentState;
        };

        this.setCurrentState = function (newCurrentState) {
            currentState = newCurrentState;
        };

        this.isConnectionAllowed = function (sourceNodeId, targetNodeId) {
            if (!sourceNodeId || !targetNodeId || sourceNodeId == targetNodeId) {
                return false;
            }

            var constraints = new Constraints(self.CONNECTION_CONSTRAINTS);
            var nodesMap = campaignManagerNodes.getNodesMap();

            var sourceNode = nodesMap[sourceNodeId];
            var targetNode = nodesMap[targetNodeId];

            var source = {
                node: sourceNode,
                type: sourceNode.type,
                connections: {}
            };

            var target = {
                node: targetNode,
                type: targetNode.type,
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
            if (source.type == nodeFactory.NODE_TYPE_STOP && !this.nodeHasIncomingConnections(source.node)) {
                source.type = nodeFactory.NODE_TYPE_START;
            }

            // A start icon becomes stop icon when it gets incoming connections (unless it has outgoing connections).
            if (target.type == nodeFactory.NODE_TYPE_START && !this.nodeHasOutgoingConnections(target.node)) {
                target.type = nodeFactory.NODE_TYPE_STOP;
            }

            if (constraints.check(source, target, nodesMap)) {
                // The new node type should only be assigned if constraints check passes.
                if (sourceNode.type == nodeFactory.NODE_TYPE_STOP) {
                    sourceNode.type = nodeFactory.NODE_TYPE_START;
                }

                if (targetNode.type == nodeFactory.NODE_TYPE_START) {
                    targetNode.type = nodeFactory.NODE_TYPE_STOP;
                }

                return true;
            }

            if (typeof(connectionNotAllowedCallback) == "function") {
                connectionNotAllowedCallback();
            }

            return false;
        };

        this.connectNodes = function (sourceId, targetId, connectJsPlumb) {
            var isConnected = false;

            for (var i = 0; i < nodeConnections.length; i++) {
                var c = nodeConnections[i];
                if (c.source == sourceId && c.target == targetId || c.source == targetId && c.target == sourceId) {
                    isConnected = true;
                    break;
                }
            }

            if (!isConnected && self.isConnectionAllowed(sourceId, targetId)) {
                var connection = {source: sourceId, target: targetId};
                if (self.needSaveSnapshot) {
                    this.saveSnapshot();
                }

                nodeConnections.push(connection);
                if (connectJsPlumb) {
                    processJSPlumbConnection(jsPlumbInstance.connect(connection));
                }

                chainProcessor.updateParameterValueAfterDecision();
                chainProcessor.updateRecipientNodesChains();
            }
        };

        this.connectIntermediateNodes = function (sourceId, targetId) {
            if (shiftDown) {
                var nodeSize = campaignManagerScale.pxToCoordinate(campaignManagerScale.getScaledNodeSize());
                var startNode = campaignManagerNodes.getNodeById(sourceId);
                var finishNode = campaignManagerNodes.getNodeById(targetId);
                var dX = Math.abs(finishNode.x - startNode.x);
                var dY = Math.abs(finishNode.y - startNode.y);
                var region = {x1: 0, y1: 0, x2: 0, y2: 0};
                //define region to catch nodes
                if (dX >= dY) {
                    region.x1 = Math.min(startNode.x, finishNode.x) + nodeSize;
                    region.y1 = Math.min(startNode.y, finishNode.y);
                    region.x2 = Math.max(startNode.x, finishNode.x);
                    region.y2 = Math.max(startNode.y, finishNode.y) + nodeSize;
                } else {
                    region.x1 = Math.min(startNode.x, finishNode.x);
                    region.y1 = Math.min(startNode.y, finishNode.y) + nodeSize;
                    region.x2 = Math.max(startNode.x, finishNode.x) + nodeSize;
                    region.y2 = Math.max(startNode.y, finishNode.y);
                }
                var intermediateNodes = campaignManagerNodes.getNodesFromRegion(region.x1, region.y1, region.x2, region.y2, 0);
                //added start/finish nodes
                intermediateNodes[startNode.id] = startNode;
                intermediateNodes[finishNode.id] = finishNode;
                //prepare nodes to sort
                var nodes = [];
                for (var index in intermediateNodes) {
                    nodes.push(intermediateNodes[index]);
                }
                //sort nodes
                nodes.sort(function (n1, n2) {
                    //by ascending X-coordinates
                    if ((dX >= dY) && (startNode.x <= finishNode.x)) {
                        return n1.x - n2.x;
                        //by desscending X-coordinates
                    } else if ((dX >= dY) && (startNode.x > finishNode.x)) {
                        return n2.x - n1.x;
                        //by ascending Y-coordinates
                    } else if ((dX <= dY) && (startNode.y <= finishNode.y)) {
                        return n1.y - n2.y;
                        //by desscending Y-coordinates
                    } else if ((dX <= dY) && (startNode.y > finishNode.y)) {
                        return n2.y - n1.y;
                    }
                });
                var curCallback = self.getConnectionNotAllowedCallback();
                //replace callback
                if (nodes.length > 2) {
                    self.setConnectionNotAllowedCallback(function () {
                        //TODO: there is no workflwNotAllowedSeveralConnectionsDialogHandler
                        //workflowNotAllowedSeveralConnectionsDialogHandler.showDialog();
                    });
                }
                var prev = null;
                //connect nodes
                jQuery.each(nodes, function (index, current) {
                    if (prev != null) {
                        self.connectNodes(campaignManagerNodes.getNodeIdPrefix() + prev.id,
                            campaignManagerNodes.getNodeIdPrefix() + current.id, true);
                    }
                    prev = current;
                });
                //return callback function
                self.setConnectionNotAllowedCallback(curCallback);
                return true;
            } else {
                return false;
            }
        };

        this.deleteSelectedNode = function () {
            var needSaveSnapshot = true;
            var selected = AGN.Lib.WM.CampaignManagerSelection.getSelected();
            jQuery.each(selected, function (index, nodeId) {
                var nodeToDelete = campaignManagerNodes.getNodeById(nodeId);
                if (nodeToDelete != null) {
                    if (needSaveSnapshot) {
                        self.saveSnapshot();
                        needSaveSnapshot = false;
                    }
                    self.deleteNode(nodeId, false, true);
                }
            });
            AGN.Lib.WM.CampaignManagerSelection.clear();
            rearrangeStage();
            self.restoreCampaignActionsType();

            chainProcessor.updateParameterValueAfterDecision();
            chainProcessor.updateRecipientNodesChains();

            this.callWorkflowManagerStateChangedCallback();
        };

        this.deleteNode = function (nodeId, relayout, needCheckActivation) {
            if (needCheckActivation == true && self.checkActivation()) {
                return;
            }
            detachRapidButton();

            var nodeMap = campaignManagerNodes.getNodesMap();
            var nodesToDelete = [nodeId];

            if (nodeMap[nodeId]) {
                switch (nodeMap[nodeId].type) {
                    case nodeFactory.NODE_TYPE_DEADLINE:
                        // Check there's an incoming connection from import icon (import -> deadline).
                        self.getIncomingConnectionsById(nodeId)
                            .forEach(function (connection) {
                                var node = nodeMap[connection.source];
                                if (node && node.type == nodeFactory.NODE_TYPE_IMPORT) {
                                    nodesToDelete.push(node.element.id);
                                }
                            });
                        break;

                    case nodeFactory.NODE_TYPE_IMPORT:
                        // Check there's an outgoing connection to deadline icon (import -> deadline).
                        self.getOutgoingConnectionsById(nodeId)
                            .forEach(function (connection) {
                                var node = nodeMap[connection.target];
                                if (node && node.type == nodeFactory.NODE_TYPE_DEADLINE) {
                                    nodesToDelete.push(node.element.id);
                                }
                            });
                        break;
                }
            }

            nodeConnections = nodeConnections.filter(function (connection) {
                // Remove all incoming and outgoing connections of each deleted icon.
                return nodesToDelete.indexOf(connection.source) < 0 && nodesToDelete.indexOf(connection.target) < 0;
            });

            nodesToDelete.forEach(function (id) {
                campaignManagerNodes.deleteNode(id);
            });

            if (relayout === true) {
                rearrangeStage();
            }
        };

        this.deleteConnection = function (connection) {
            if (self.checkActivation()) {
                return;
            }

            var sourceId = connection.source.attr('id');
            var targetId = connection.target.attr('id');

            var source = campaignManagerNodes.getNodeById(sourceId);
            var target = campaignManagerNodes.getNodeById(targetId);

            if (source.type == nodeFactory.NODE_TYPE_IMPORT && target.type == nodeFactory.NODE_TYPE_DEADLINE) {
                return;
            }

            this.saveSnapshot();

            nodeConnections = nodeConnections.filter(function (c) {
                return (c.source != sourceId || c.target != targetId) && (c.source != targetId || c.target != sourceId);
            });

            rearrangeStage();
            chainProcessor.updateParameterValueAfterDecision();
            chainProcessor.updateRecipientNodesChains();
        };

        this.deleteConnectionByHtmlElement = function (htmlElement) {
            var connections = jsPlumbInstance.getConnections();
            for (var i = 0; i < connections.length; i++) {
                if (connections[i].canvas == htmlElement) {
                    self.deleteConnection(connections[i]);
                    break;
                }
            }
        };

        this.deleteConnectionByIdsOfElements = function (connection, needRearrange) {
            var keepNodeConnections = [];
            for (var i = 0; i < nodeConnections.length; i++) {
                if ((nodeConnections[i].source != connection.source || nodeConnections[i].target != connection.target) &&
                    (nodeConnections[i].source != connection.target || nodeConnections[i].target != connection.source)
                ) {
                    keepNodeConnections.push(nodeConnections[i]);
                }
            }
            nodeConnections = keepNodeConnections;
            if (needRearrange == true) {
                rearrangeStage();
            }
        };

        // get uniq connection from selected nodes
        this.getConnectionsFromSelected = function (nodeIds) {
            var connections = [];
            jQuery.each(nodeIds, function (index, nodeId) {
                jQuery.each(self.getIncomingConnectionsById(nodeId), function (index, connection) {
                    if (jQuery.inArray(connection, connections) == -1) {
                        connections.push(connection);
                    }
                });
                jQuery.each(self.getOutgoingConnectionsById(nodeId), function (index, connection) {
                    if (jQuery.inArray(connection, connections) == -1) {
                        connections.push(connection);
                    }
                });
            });
            return connections;
        };

        this.getNodesFromSelected = function (nodeIds) {
            return jQuery.map(nodeIds, function (nodeId) {
                return campaignManagerNodes.getNodeById(nodeId);
            });
        };

        this.deleteConnections = function (connections) {
            jQuery.each(connections, function (index, connection) {
                self.deleteConnectionByIdsOfElements(connection, false);
            });
            rearrangeStage();
        };

        this.deleteAllNodes = function (relayout) {
            var nodes = campaignManagerNodes.getNodes();
            for (var i in nodes) {
                this.deleteNode(nodes[i].elementJQ.attr('id'), relayout, true);
            }
        };

        this.getSessionId = function () {
            return this.pageContextSessionId;
        };

        this.getEditorsHelper = function () {
            return editorsHelper;
        };

        this.setCampaignActionsType = function (node) {
            if (node.type == "actionbased_mailing") {
                self.IS_ACTION_BASED = true;
            }
            if (node.type == "datebased_mailing") {
                self.IS_DATE_BASED = true;
            }
        };

        this.restoreCampaignActionsType = function () {
            var nodes = campaignManagerNodes.getNodes();
            self.IS_ACTION_BASED = false;
            self.IS_DATE_BASED = false;
            for (var i in nodes) {
                self.setCampaignActionsType(nodes[i]);
            }
            self.restoreStopsTitles();
        };

        this.isNormalCampaignActionsType = function () {
            return !self.IS_ACTION_BASED && !self.IS_DATE_BASED;
        };

        this.restoreStopsTitles = function () {
            var nodes = campaignManagerNodes.getNodes();
            var stopTitle = self.isNormalCampaignActionsType() ? t('workflow.stop.automatic_end') : t('workflow.stop.open_end');
            for (var i in nodes) {
                var node = nodes[i];
                if (node.data.endType == constants.endTypeAutomatic) {
                    node.elementJQ.find(".icon-extra-info").remove();
                    node.elementJQ.append($("<div class='icon-extra-info'>" + stopTitle + "</div>"));
                    var fontSize = campaignManagerScale.getCurrentScale() * 90;
                    var position = campaignManagerNodes.getIconExtraInfoPosition(node, campaignManagerScale);
                    var textTop = position.top;
                    var textLeft = position.left;
                    node.elementJQ.find("div.icon-extra-info").css("font-size", fontSize + "%");
                    node.elementJQ.find("div.icon-extra-info").css("top", textTop + "px");
                    node.elementJQ.find("div.icon-extra-info").css("left", textLeft + "px");
                }
            }
        };

        this.getIncomingConnectionsById = function (nodeId) {
            var connections = [];
            for (var i = 0; i < nodeConnections.length; i++) {
                if (nodeConnections[i].target == nodeId) {
                    connections.push(nodeConnections[i]);
                }
            }
            return connections;
        };

        this.getNodeIncomingConnections = function (node) {
            return self.getIncomingConnectionsById(node.elementJQ.attr("id"));
        };

        this.getOutgoingConnectionsById = function (nodeId) {
            var connections = [];
            for (var i = 0; i < nodeConnections.length; i++) {
                if (nodeConnections[i].source == nodeId) {
                    connections.push(nodeConnections[i]);
                }
            }
            return connections;
        };

        this.getNodeOutgoingConnections = function (node) {
            return self.getOutgoingConnectionsById(node.elementJQ.attr("id"));
        };

        this.clearLabelConnection = function (node) {
            var outgoingConnections = self.getNodeOutgoingConnections(node);
            for (var i = 0; i < outgoingConnections.length; i++) {
                var allConnections = jsPlumbInstance.getConnections();
                for (var j = 0; j < allConnections.length; j++) {
                    if ((allConnections[j].source[0].id == outgoingConnections[i].source) &&
                        (allConnections[j].target[0].id == outgoingConnections[i].target)) {
                        var labelOverlay = allConnections[j].getOverlay('label');
                        if (labelOverlay != null) {
                            jQuery("#" + labelOverlay.canvas.id).remove();
                            allConnections[j].removeOverlay('label');
                        }
                        break;
                    }
                }
            }
        };

        this.updateLabelConnection = function (node) {
            if ((node.data.decisionType == constants.decisionTypeDecision) ||
                (node.data.decisionType == constants.decisionTypeAutoOptimization)) {
                self.clearLabelConnection(node);
                var connections = self.getNodeOutgoingConnections(node);
                if ((node.data.decisionType == constants.decisionTypeDecision) && (connections.length == 2)) {
                    var yesLabel = t('workflow.defaults.yes');
                    var noLabel = t('workflow.defaults.no');
                    if (node.statisticsList != undefined) {
                        yesLabel += ": <span class='node-stats'>" + node.statisticsList[0] + "</span>";
                        noLabel += ": <span class='node-stats'>" + node.statisticsList[1] + "</span>";
                    }
                    var conn1 = self.getConnectionObject(connections[0]);
                    if (conn1 == null) {
                        return;
                    }
                    var conn2 = self.getConnectionObject(connections[1]);

                    var firstNode = campaignManagerNodes.getNodeById(connections[0].target);
                    var secondNode = campaignManagerNodes.getNodeById(connections[1].target);
                    var dX = firstNode.x - secondNode.x;
                    var dY = firstNode.y - secondNode.y;

                    var yesPlacement = (Math.abs(dX) < Math.abs(dY)) ? "top" : "right";
                    var noPlacement = (Math.abs(dX) < Math.abs(dY)) ? "bottom" : "left";

                    //all nodes above the decision node
                    if (node.y >= firstNode.y && node.y >= secondNode.y) {
                        //the highest node must have YES label
                        if (firstNode.y < secondNode.y) {
                            self.setLabelToConnection(conn1, yesLabel, yesPlacement);
                            self.setLabelToConnection(conn2, noLabel, noPlacement);
                        }
                        else if (firstNode.y > secondNode.y) {
                            self.setLabelToConnection(conn1, noLabel, noPlacement);
                            self.setLabelToConnection(conn2, yesLabel, yesPlacement);
                        }
                        else {
                            //left node must have YES label
                            if (firstNode.x < secondNode.x) {
                                self.setLabelToConnection(conn1, yesLabel, yesPlacement);
                                self.setLabelToConnection(conn2, noLabel, noPlacement);
                            }
                            else {
                                self.setLabelToConnection(conn1, noLabel, noPlacement);
                                self.setLabelToConnection(conn2, yesLabel, yesPlacement);
                            }
                        }
                    }
                    //all nodes under the decision node
                    else if (node.y < firstNode.y && node.y < secondNode.y) {
                        //left node always must have NO label
                        if (firstNode.x < secondNode.x) {
                            self.setLabelToConnection(conn1, noLabel, noPlacement);
                            self.setLabelToConnection(conn2, yesLabel, yesPlacement);
                        }
                        else {
                            self.setLabelToConnection(conn1, yesLabel, yesPlacement);
                            self.setLabelToConnection(conn2, noLabel, noPlacement);
                        }
                    }
                    //one of node above and another under the decision node
                    else {
                        if (firstNode.y < secondNode.y) {
                            self.setLabelToConnection(conn1, yesLabel, yesPlacement);
                            self.setLabelToConnection(conn2, noLabel, noPlacement);
                        }
                        else {
                            self.setLabelToConnection(conn1, noLabel, noPlacement);
                            self.setLabelToConnection(conn2, yesLabel, yesPlacement);
                        }
                    }

                    var labelStyle = jQuery("._jsPlumb_overlay");
                    if (labelStyle.length !== 0) {
                        labelStyle.css("font-size", campaignManagerScale.getLabelFontSize());
                    }
                }
            }
        };

        this.getConnectionObject = function (connection) {
            var connections = jsPlumbInstance.getConnections();
            for (var i = 0; i < connections.length; i++) {
                if ((connections[i].source[0].id == connection.source) &&
                    (connections[i].target[0].id == connection.target)) {
                    return connections[i];
                }
            }
            return null;
        };

        this.setLabelToConnection = function (conn, text, nodePlacement) {
            if (conn != null) {
                conn.setLabel({id: 'label', label: text, location: 0.65, cssClass: "connection-label"});

                // by default the label is placed on connection which is not ok for us
                // this code moves the label not to overlap the connection
                var label = jQuery(conn.getOverlay("label").canvas);
                var dx = 6 + label.width() / 2;
                var dy = 4 + label.height() / 2;
                if (nodePlacement == "bottom") {
                    dy = -dy;
                }
                if (nodePlacement == "right") {
                    dx = -dx;
                }
                jQuery(conn.getOverlay("label").canvas).css({
                    "margin-top": dy + "px",
                    "margin-left": dx + "px"
                });
            }
        };

        this.nodeHasIncomingConnections = function (node) {
            var elementId = node.elementJQ.attr('id');
            return nodeConnections.some(function (connection) {
                return connection.target == elementId;
            });
        };

        this.nodeHasOutgoingConnections = function (node) {
            var elementId = node.elementJQ.attr('id');
            return nodeConnections.some(function (connection) {
                return connection.source == elementId;
            });
        };

        this.editNode = function (node, needCheckActivation) {
            if (node == undefined) {
                node = campaignManagerNodes.getNodeById(AGN.Lib.WM.CampaignManagerSelection.getSelected()[0])
            }
            if (node.isExpandable && (node.filled || isSampleNode(node))) {
                if (!node.data.created && node.data.copyContent && !isSampleNode(node)) {
                    // if it is the expanding of node (examples or another workflow) - we need to ask if user wants to use copy or original
                    self.expandNodeUseOriginal(node.data.copyContent, editorsHelper.curEditingNode.id);
                } else {
                    expandNode(node, node.data.created);
                }
            } else {
                AGN.Lib.WM.CampaignManagerSelection.clear();
                editorsHelper.showEditDialog(node, self.checkActivation());
            }
        };

        var isSampleNode = function (node) {
            return node.isExpandable && node.type != "ownWorkflow";
        };

        var expandNode = function (node, useOriginal) {
            new AGN.Lib.WM.IconsSetLoader(self).loadIconsSetAJAX(node, useOriginal);
            node.data.created = true;
        };

        this.expandNodeUseOriginal = function (copy, id) {
            var node = campaignManagerNodes.getNodes()[id];
            expandNode(node, !copy);
        };

        this.isNewNode = function (node) {
            return (node.id + '').indexOf('new') != -1;
        };

        this.updateNode = function (node) {
            campaignManagerNodes.updateNodeView(node, campaignManagerScale);
        };

        this.restoreWorkflow = function (nodesData) {
            var restoredNodes = self.restoreNodes(nodesData);

            self.restoreConnections(nodesData);
            self.relayout();

            return restoredNodes;
        };

        this.restoreNodes = function (nodesData) {
            var restoredNodes = [];

            nodesData.forEach(function (nodeData) {
                var node = campaignManagerNodes.restoreNode(nodeData);
                restoredNodes.push(node);
                justifyNodeElement(node);
            });

            return restoredNodes;
        };

        this.restoreConnections = function (nodesData) {
            var nodes = campaignManagerNodes.getNodes();

            self.noSnapshot(function () {
                nodesData.forEach(function (nodeData) {
                    if (nodeData.connections) {
                        var sourceId = nodeData.id;

                        nodeData.connections.forEach(function (connection) {
                            var targetId = connection.targetIconId;
                            if (nodes[sourceId] && nodes[targetId]) {
                                self.restoreConnection(sourceId, targetId);
                            }
                        });
                    }
                });
            });
        };

        this.restoreConnection = function (sourceIconId, targetIconId) {
            var nodeIdPrefix = campaignManagerNodes.getNodeIdPrefix();
            this.connectNodes(nodeIdPrefix + sourceIconId, nodeIdPrefix + targetIconId, false);
        };

        this.relayout = function () {
            rearrangeStage();
            currentState = self.STATE_WAITING;
        };

        this.getCMNodes = function () {
            return campaignManagerNodes;
        };

        this.isNodesFilled = function () {
            var nodes = campaignManagerNodes.getNodes();
            for (var i in nodes) {
                if (!nodes[i].filled) {
                    return false;
                }
            }
            return true;
        };

        this.justifyNodeElement = function (node) {
            justifyNodeElement(node);
        };

        this.saveSnapshot = function () {
            if (undoHistory.length == campaignManagerSettings.MAX_UNDO_HISTORY) {
                undoHistory.shift();
                self.historyWasOverloaded = true;
            }

            undoHistory.push({
                icons: self.getIconsForSubmission(),
                workflowBound: self.getBoundNodesDOM()
            });
            if (typeof(historyStackChangedCallback) == "function") {
                historyStackChangedCallback(true);
            }
        };

        this.undo = function () {
            if (undoHistory.length > 0) {
                self.noSnapshot(function () {
                    var state = undoHistory.pop();
                    self.deleteAllNodes(false);
                    boundNodesLeft = parseInt(state.workflowBound.left);
                    boundNodesTop = parseInt(state.workflowBound.top);
                    boundNodesWidth = parseInt(state.workflowBound.width);
                    boundNodesHeight = parseInt(state.workflowBound.height);
                    updateBoundNodesDOM();

                    self.restoreWorkflow(state.icons);
                    historyStackChangedCallback(undoHistory.length > 0);
                });
            }
        };

        this.canUndo = function () {
            return undoHistory.length > 0;
        };

        this.getUndoHistoryDataForSubmission = function () {
            return JSON.stringify(undoHistory);
        };

        this.setUndoHistoryDataForSubmission = function (newUndoHistory) {
            // undoHistory = JSON.parse(newUndoHistory);

            //call callback for changing UI
            historyStackChangedCallback(undoHistory.length > 0);
        };

        this.setHistoryStackChangedCallback = function (callback) {
            historyStackChangedCallback = callback;
        };

        this.setConnectionNotAllowedCallback = function (callback) {
            connectionNotAllowedCallback = callback;
        };

        this.getConnectionNotAllowedCallback = function () {
            return connectionNotAllowedCallback;
        };

        this.setEditingNotAllowedCallback = function (callback) {
            editingNotAllowedCallback = callback;
        };

        this.setEditorCanvasWidth = function (width) {
            editorCanvasWidth = width;
        };

        this.setEditorCanvasHeight = function (height) {
            editorCanvasHeight = height;
        };

        this.setViewPortWidth = function (width) {
            viewPortWidth = width;
        };

        this.setViewPortHeight = function (height) {
            viewPortHeight = height;
        };

        this.updateWorkflowForPdf = function() {
            viewPortJQ.css({
                "overflow": "inherit",
                "float": "none",
                "border": "none",
                "sceneHeight": "100%",
                "width": "100%",
                "min-height": viewPortJQ.css("height"),
                "height": "100%"
            });

            if (navigatorJQ.exists()) {
                navigatorJQ.addClass('hidden');
            }

            var sceneHeight = boundNodesJQ.height();
            var sceneWidth = boundNodesJQ.width();

            // max width and height which can fit into PDF single page
            var maxWidth = 1360; // TODO: figure out from what was gotten such restriction
            var maxHeight = 800;
            if (sceneHeight > maxHeight || sceneWidth > maxWidth) {
                var newScale = 1.0;
                if (sceneWidth / sceneHeight < maxWidth / maxHeight) {
                    newScale = maxHeight / sceneHeight;
                } else {
                    newScale = maxWidth / sceneWidth;
                }
                self.setScale(newScale);
                self.relayout();
            }

            // reset all scrolling, we don't need it for PDF
            self.resetScrolling();
        };

        this.resetScrolling = function() {
            boundNodesJQ.css({
                "position": "relative",
                "top": "0",
                "left": "0",
                "margin": $("div.icon-extra-info").css("top") + " 15px"
            });

            editorCanvasJQ.css({
                "position": "initial",
                "height": "100%",
                "min-height": editorCanvasJQ.css("height")
            });
        };

        /*******************************************************************************************************************
         * Server side stuff
         ******************************************************************************************************************/

        this.omitKeys = function (obj, keys) {
            var dup = {};
            for (key in obj) {
                if (keys.indexOf(key) == -1) {
                    dup[key] = obj[key];
                }
            }
            return dup;
        };

        this.getIconsForSubmission = function (resolveFakeIds) {
            var icons = [];
            var nodes = campaignManagerNodes.getNodes();
            var idsMap = {};
            var keys = Object.keys(nodes);

            if (resolveFakeIds === true) {
                // Get max existing identifier value.
                var maxId = 0;
                keys.forEach(function (key) {
                    var id = parseInt(key, 10);
                    if (id > maxId) {
                        maxId = id;
                    }
                });

                // Generate values for missing ids (if any) and collect a map.
                keys.forEach(function (key) {
                    var id = parseInt(key, 10);

                    if (id > 0) {
                        idsMap[key] = id;
                    } else {
                        maxId++;
                        idsMap[key] = maxId;
                    }
                });
            } else {
                // Keep existing ids without changes.
                keys.forEach(function (key) {
                    idsMap[key] = key;
                });
            }

            // Collect icons and assign ids.
            keys.forEach(function (key) {
                var icon = self.omitKeys(nodeFactory.prepareNodeForSubmission(nodes[key]), ['isExpandable']);

                icon.id = idsMap[key];
                icons.push(icon);
            });

            var connectionMap = {};  // source -> [target]

            // Collect connection map (source icon id to array of target icon ids).
            nodeConnections.forEach(function (connection) {
                // Get and resolve ids of connected icons.
                var sourceId = idsMap[self.extractIdFromConnectionEnd(connection.source)];
                var targetId = idsMap[self.extractIdFromConnectionEnd(connection.target)];

                if (sourceId in connectionMap) {
                    connectionMap[sourceId].push(targetId);
                } else {
                    connectionMap[sourceId] = [targetId];
                }
            });

            // Store connections to icons.
            icons.forEach(function (icon) {
                var connections = connectionMap[icon.id];
                if (connections) {
                    icon.connections = $.map(connections, function (id) {
                        return {targetIconId: id};
                    });
                } else {
                    icon.connections = [];
                }
            });

            return icons;
        };

        this.getIconsForSubmissionJson = function () {
            return JSON.stringify(this.getIconsForSubmission(true));
        };

        this.getLeftPosition = function () {
            return boundNodesJQ.offset().left - viewPortJQ.offset().left;
        };

        this.getTopPosition = function () {
            return boundNodesJQ.offset().top - viewPortJQ.offset().top;
        };

        this.extractIdFromConnectionEnd = function (endElementClientId) {
            return endElementClientId.substring(endElementClientId.indexOf(campaignManagerNodes.getNodeIdPrefix()) + campaignManagerNodes.getNodeIdPrefix().length, endElementClientId.length);
        };

        this.updateParameterValueAfterDecision = function () {
            chainProcessor.updateParameterValueAfterDecision();
        };

        this.updateRecipientNodesChains = function () {
            chainProcessor.updateRecipientNodesChains();
        };

        /**
         * Returns 2d array of possible incoming connections for specified icon
         */
        this.getIncomingChainsForIcon = function () {
            var PREFIX = self.getCMNodes().getNodeIdPrefix();

            var nodes = self.getCMNodes();
            var chains = [];

            chainProcessor.getIncomingChainsForIcon().forEach(function (icons) {
                var chain = [];

                icons.forEach(function (icon) {
                    chain.push(nodes.getNodeById(PREFIX + icon.id));
                });

                chains.push(chain);
            });

            return chains;
        };

        this.setWorkflowManagerStateChangedCallback = function (fnCallback) {
            workflowManagerStateChangedCallback = fnCallback;
        };

        this.callWorkflowManagerStateChangedCallback = function () {
            if (typeof(workflowManagerStateChangedCallback) == "function") {
                workflowManagerStateChangedCallback();
            }
        };

        this.getCampaignManagerNodes = function () {
            return campaignManagerNodes;
        };

        this.getNodesFactory = function () {
            return nodeFactory;
        };

        this.getMailingNames = function () {
            var mailingNames = "";
            var nodes = self.getCMNodes().getNodesByTypeList([nodeFactory.NODE_TYPE_MAILING,
                nodeFactory.NODE_TYPE_ACTION_BASED_MAILING, nodeFactory.NODE_TYPE_DATE_BASED_MAILING,
                nodeFactory.NODE_TYPE_FOLLOWUP_MAILING]);
            for (var i = 0; i < nodes.length; i++) {
                mailingNames = mailingNames + nodes[i].iconTitle + '<br/>';
            }
            return mailingNames;
        };

        this.showError = function (msg) {
            AGN.Lib.Messages(t('workflow.defaults.error'), msg, 'alert');
        };

        //procedure to check workflow to allow or not saving
        this.checkWorkflowBeforeSave = function () {
            var checkActivated = self.checkActivation();
            var checkUsingActivated = self.checkUsingActivatedWorkflow();
            var checkPartOfActivated = self.checkPartOfActivatedWorkflow();
            return checkActivated || checkUsingActivated || checkPartOfActivated;
        };

        this.checkActivation = function () {
            var status = $("input[name='status']").val();

            if (this.isActivated && (status === "STATUS_ACTIVE" || status === "STATUS_TESTING")) {
                self.showError(t('error.workflow.saveActivatedWorkflow'));
                return true;
            } else {
                return false;
            }
        };

        this.checkUsingActivatedWorkflow = function () {
            //verify that workflow uses activated workflow
            var msg_key = $("input[name='usingActivatedWorkflow']").val();
            if ("" != msg_key) {
                var name = $("input[name='usingActivatedWorkflowName']").val();
                self.showError(t('error.workflow.' + msg_key).replace("%s", name));
                return true;
            } else {
                return false;
            }
        };

        this.checkPartOfActivatedWorkflow = function () {
            //verify that workflow is used in the activated workflow
            var msg_key = $("input[name='partOfActivatedWorkflow']").val();
            if ("" != msg_key) {
                var name = $("input[name='partOfActivatedWorkflowName']").val();
                self.showError(t('error.workflow.' + msg_key).replace("%s", name));
                return true;
            } else {
                return false;
            }
        };

        this.checkMailingTypesConvertingRequired = function () {
            var icons = this.getIconsForSubmission(false);
            var startType = 'UNKNOWN';
            var mailingStartTypes = {};

            for (var i = 0; i < icons.length; i++) {
                var icon = icons[i];

                if (icon.type == nodeFactory.NODE_TYPE_START && icon.filled) {
                    var type = 'UNKNOWN';

                    switch (icon.startType) {
                        case constants.startTypeDate:
                            type = 'DATE';
                            break;

                        case constants.startTypeEvent:
                            switch (icon.event) {
                                case constants.startEventDate:
                                    type = 'RULE';
                                    break;

                                case constants.startEventReaction:
                                    type = 'REACTION';
                                    break;
                            }
                            break;
                    }

                    if (startType != type) {
                        if (startType == 'UNKNOWN') {
                            startType = type;
                        } else {
                            // Multiple start icons and mixed start types we can't handle.
                            // Let's fallback to server-side validation.
                            return false;
                        }
                    }
                }

                switch (icon.type) {
                    case nodeFactory.NODE_TYPE_MAILING:
                    case nodeFactory.NODE_TYPE_FOLLOWUP_MAILING:
                        mailingStartTypes['DATE'] = true;
                        break;

                    case nodeFactory.NODE_TYPE_ACTION_BASED_MAILING:
                        mailingStartTypes['REACTION'] = true;
                        break;

                    case nodeFactory.NODE_TYPE_DATE_BASED_MAILING:
                        mailingStartTypes['RULE'] = true;
                        break;
                }
            }

            if (startType != 'UNKNOWN') {
                mailingStartTypes = Object.keys(mailingStartTypes);

                if (mailingStartTypes.length == 1) {
                    if (startType != mailingStartTypes[0]) {
                        // Used mailing type conflicts with start icon so converting is required.
                        return true;
                    }
                } else {
                    // Keep in mind that normal and follow-up mailings are represented as equals here.
                    if (mailingStartTypes.length > 1) {
                        // Mixed mailing types are here (some require to be converted).
                        return true;
                    }
                }
            }

            return false;
        };

        this.setScale = function (scale) {
            campaignManagerScale.setExternalScale(scale);
        };

        this.getCommentControls = function() {
            return commentControls;
        };

        this.getDraggedElements = function() {
            return draggedElements;
        };

        this.activateIgnoreChangesThisTime = function() {
            self.ignoreChangesThisTime = true;
        };

        this.hasUnsavedChanges = function() {
            if(self.ignoreChangesThisTime){
                self.ignoreChangesThisTime = false;
                return false;
            }
            return self.canUndo() || self.historyWasOverloaded;
        };


        /*******************************************************************************************************************
         * Main routine
         ******************************************************************************************************************/

        // --- Setup navigator --- //

        if (navigatorJQ.exists()) {
            var navigatorArrowTop = $('<div class="unselectable-text"></div>').addClass('js-navigation-top');
            var navigatorArrowRight = $('<div class="unselectable-text"></div>').addClass('js-navigation-right');
            var navigatorArrowBottom = $('<div class="unselectable-text"></div>').addClass('js-navigation-bottom');
            var navigatorArrowLeft = $('<div class="unselectable-text"></div>').addClass('js-navigation-left');

            navigatorJQ.append(navigatorArrowTop);
            navigatorJQ.append(navigatorArrowRight);
            navigatorJQ.append(navigatorArrowBottom);
            navigatorJQ.append(navigatorArrowLeft);

            navigatorJQ.removeClass('hidden');

            navigatorArrowTop.on("click", function() {
                editorCanvasJQ.animate({
                    top: campaignManagerSettings.navigatorStep
                }, "fast", function() {
                    rearrangeStage();
                });
            });

            navigatorArrowRight.on("click", function () {
                var keepScrollLeft = viewPortJQ.scrollLeft();
                editorCanvasJQ.css({width: editorCanvasWidth + editorCanvasWidthGlobalIncrease + campaignManagerSettings.navigatorStep});
                viewPortJQ.scrollLeft(keepScrollLeft);
                editorCanvasJQ.animate(
                    {
                        left: -campaignManagerSettings.navigatorStep
                    },
                    "fast",
                    function () {
                        rearrangeStage();
                    }
                )
            });
            navigatorArrowBottom.on("click", function () {
                var keepScrollTop = viewPortJQ.scrollTop();
                editorCanvasJQ.css({height: editorCanvasHeight + editorCanvasHeightGlobalIncrease + campaignManagerSettings.navigatorStep});
                viewPortJQ.scrollTop(keepScrollTop);
                editorCanvasJQ.animate(
                    {
                        top: -campaignManagerSettings.navigatorStep
                    },
                    "fast",
                    function () {
                        rearrangeStage();
                    }
                )
            });
            navigatorArrowLeft.on("click", function () {
                editorCanvasJQ.animate(
                    {
                        left: campaignManagerSettings.navigatorStep
                    },
                    "fast",
                    function () {
                        rearrangeStage();
                    }
                )
            });
        }

        // --- EndOf Setup navigator --- //

        // --- Enable/disable space interception routine --- //

        var restoreSpaceFieldsSelector = "";
        for (var i = 0; i < restoreSpaceFields.length; i++) {
            if (restoreSpaceFieldsSelector) {
                restoreSpaceFieldsSelector += ", ";
            }
            restoreSpaceFieldsSelector += "#" + restoreSpaceFields[i];
        }

        var blurFieldsWithSpaces = function () {
            jQuery(restoreSpaceFieldsSelector).filter(":focus").blur();
        };

        jQuery(restoreSpaceFieldsSelector)
            .on("focus", function () {
                currentState = self.STATE_SPACE_RESTORED;
            })
            .on("blur", function () {
                currentState = self.STATE_WAITING;
            });

        jQuery("#slider").slider({
            value: currentScaleSliderPosition,
            step: campaignManagerSettings.zoomStep,
            change: function () {
                campaignManagerScale.setCurrentScaleSliderPosition(jQuery(this).slider("value"));

                // rearrange whole stage
                rearrangeStage();
            }
        });

        // --- EndOf Enable/disable space interception routine --- //

        // chrome fix.
        document.onselectstart = function () {
            return false;
        };

        // initialize the stage
        rearrangeStage();

        // event for draggable buttons from the toolbars (marked with "js-draggable-button" class)
        $('.js-draggable-button')
            .css({zIndex: self.Z_INDEX_DRAGGABLE_BUTTON})
            .draggable({
                revert: true,
                revertDuration: 0,
                start: function (e, ui) {
                    blurFieldsWithSpaces();
                    currentState = self.STATE_CREATING_NODE;
                    ui.helper.css({zIndex: self.Z_INDEX_DRAGGABLE_BUTTON + 1});
                    ui.helper.parent().css({zIndex: self.Z_INDEX_ICON_PANEL + 1});
                },
                stop: function (e, ui) {
                    if (self.checkActivation()) {
                        return;
                    }
                    ui.helper.css({zIndex: self.Z_INDEX_DRAGGABLE_BUTTON});
                    ui.helper.parent().css({zIndex: self.Z_INDEX_ICON_PANEL});
                    var stagePosition = viewPortJQ.offset();
                    var nodeType = ui.helper.data('type');
                    var factor = 1;


                    switch (nodeType) {
                        case nodeFactory.NODE_TYPE_OWN_WORKFLOW:
                        case nodeFactory.NODE_TYPE_SC_BIRTHDAY:
                        case nodeFactory.NODE_TYPE_SC_DOI:
                        case nodeFactory.NODE_TYPE_SC_ABTEST:
                            factor = 4;
                            break;
                    }

                    var halfWidth = ui.helper.width() / 2;
                    var halfHeight = ui.helper.height() / 2;
                    if (ui.offset.left + halfWidth >= stagePosition.left &&
                        ui.offset.top + halfHeight >= stagePosition.top &&
                        ui.offset.left + halfWidth <= stagePosition.left + viewPortWidth &&
                        ui.offset.top + halfHeight <= stagePosition.top + viewPortHeight) {

                        self.saveSnapshot();
                        var node, nodes;
                        if (campaignManagerNodes.count()) {
                            var leftOnViewPort = ui.offset.left - halfWidth / factor - getCanvasOffsetX();
                            var topOnViewPort = ui.offset.top - halfHeight / factor - getCanvasOffsetY();
                            var coordinateX = Math.round((leftOnViewPort - boundNodesLeft) / campaignManagerScale.getScaledGridSize());
                            var coordinateY = Math.round((topOnViewPort - boundNodesTop) / campaignManagerScale.getScaledGridSize());
                            if (nodeType == nodeFactory.NODE_TYPE_IMPORT) {
                                nodes = campaignManagerNodes.addNodeSequence([nodeType, nodeFactory.NODE_TYPE_DEADLINE], coordinateX, coordinateY);
                            } else {
                                node = campaignManagerNodes.addNode(nodeType, coordinateX, coordinateY);
                            }
                        } else {
                            if (nodeType == nodeFactory.NODE_TYPE_IMPORT) {
                                nodes = campaignManagerNodes.addNodeSequence([nodeType, nodeFactory.NODE_TYPE_DEADLINE], 0, 0);
                            } else {
                                node = campaignManagerNodes.addNode(nodeType, 0, 0);
                            }
                            boundNodesLeft = ui.offset.left - halfWidth / factor - getCanvasOffsetX();
                            boundNodesTop = ui.offset.top - halfHeight / factor - getCanvasOffsetY();
                        }

                        if (nodes) {
                            nodes.forEach(function (node) {
                                justifyNodeElement(node);
                            });

                            nodes.reduce(function (n1, n2) {
                                self.connectNodes(n1.element.id, n2.element.id, true);
                                return n2;
                            });
                        } else if (node) {
                            switch (nodeType) {
                                case nodeFactory.NODE_TYPE_OWN_WORKFLOW:
                                case nodeFactory.NODE_TYPE_SC_BIRTHDAY:
                                case nodeFactory.NODE_TYPE_SC_DOI:
                                case nodeFactory.NODE_TYPE_SC_ABTEST: {
                                    campaignManagerNodes.normalizeNodes();
                                    self.editNode(node, true);
                                }
                                    break;

                                // This is a dirty trick to hide unstable feature for release
                                case nodeFactory.NODE_TYPE_ACTION_BASED_MAILING:
                                case nodeFactory.NODE_TYPE_DATE_BASED_MAILING: {
                                    var deadlineNodes = self.getCMNodes().getNodesByType(nodeFactory.NODE_TYPE_DEADLINE);
                                    if (deadlineNodes.length > 0) {
                                        for (var i = 0; i < deadlineNodes.length; i++) {
                                            var deadline = deadlineNodes[i];
                                            if (deadline.filled) {
                                                deadline.data.deadlineType = constants.deadlineTypeDelay;
                                                self.updateNode(deadline);
                                            }
                                        }
                                    }
                                    justifyNodeElement(node);
                                }
                                    break;

                                default: {
                                    justifyNodeElement(node);
                                }
                                    break;
                            }
                        }

                        if (nodes || node) {
                            rearrangeStage();
                        }

                        self.callWorkflowManagerStateChangedCallback();
                    }
                    currentState = self.STATE_WAITING;
                }
            })
            .dblclick(function (e) {
                var type = $(this).data('type');
                var nodes;

                function place(x, y) {
                    if (type == nodeFactory.NODE_TYPE_IMPORT) {
                        return campaignManagerNodes.addNodeSequence([type, nodeFactory.NODE_TYPE_DEADLINE], x, y);
                    } else {
                        var node = campaignManagerNodes.addNode(type, x, y);
                        if (node) {
                            switch (type) {
                                case nodeFactory.NODE_TYPE_OWN_WORKFLOW:
                                case nodeFactory.NODE_TYPE_SC_BIRTHDAY:
                                case nodeFactory.NODE_TYPE_SC_DOI:
                                case nodeFactory.NODE_TYPE_SC_ABTEST:
                                    self.editNode(node, true);
                                    break;
                            }
                            return [node];
                        }
                        return false;
                    }
                }

                self.saveSnapshot();
                if (campaignManagerNodes.count()) {
                    var nodeSize = campaignManagerScale.pxToCoordinate(campaignManagerScale.getScaledNodeSize());

                    for (var posY = 0; posY <= boundNodesHeight; posY += nodeSize) {
                        for (var posX = 0; posX <= boundNodesWidth; posX += nodeSize) {
                            nodes = place(posX, posY);
                            if (nodes) {
                                break;
                            }
                        }
                        if (nodes) {
                            break;
                        }
                    }
                } else {
                    nodes = place(0, 0);
                    boundNodesLeft = boundNodesWidth / 2;
                    boundNodesTop = boundNodesHeight / 2;
                }

                if (nodes) {
                    nodes.forEach(function (node) {
                        justifyNodeElement(node);
                    });

                    nodes.reduce(function (n1, n2) {
                        self.connectNodes(n1.element.id, n2.element.id, true);
                        return n2;
                    });

                    rearrangeStage();
                    $(nodes[0].elementJQ).trigger("click");
                }

                self.callWorkflowManagerStateChangedCallback();
            });


        // --- Drag stage when "dragStageKeyCode" key is pressed initialization --- //

        $(document).keydown(function (e) {
            if (e.keyCode == campaignManagerSettings.dragStageKeyCode) {
                if (e.target && !$(e.target).is('input, textarea')) {
                    if (currentState != self.STATE_SPACE_RESTORED) {
                        if (currentState == self.STATE_WAITING) {
                            $("#editorCanvas").selectable("destroy");
                            $(campaignManagerNodes.getIconNodesSelector()).draggable("disable");
                            currentState = self.STATE_DRAGGING_STAGE_WAIT_MOUSE;
                        }
                        e.preventDefault();
                        e.stopPropagation();
                    }
                }
            }
            else if (e.keyCode == campaignManagerSettings.multiConnectionKeyCode) {
                shiftDown = true;
            }
        });

        $(document).keyup(function (e) {
            if (e.keyCode == campaignManagerSettings.dragStageKeyCode) {
                if (e.target && !$(e.target).is('input, textarea')) {
                    if (currentState == self.STATE_DRAGGING_STAGE_WAIT_MOUSE || currentState == self.STATE_DRAGGING_STAGE) {
                        $("#editorCanvas").selectable(getSelectableProperties());
                        $(campaignManagerNodes.getIconNodesSelector()).draggable("enable");
                        e.stopPropagation();
                        currentState = self.STATE_WAITING;
                    }
                }
            }
            else if (e.keyCode == campaignManagerSettings.multiConnectionKeyCode) {
                shiftDown = false;
            }
            else if (campaignManagerSettings.deleteKeyCode == e.keyCode) {
                self.deleteSelectedNode();
            }
        });

        var originalPageX = false;
        var originalPageY = false;
        var keepEditorCanvasWidth = false;
        var keepEditorCanvasHeight = false;
        var keepEditorCanvasLeft = false;
        var keepEditorCanvasTop = false;
        viewPortJQ.mousedown(function (e) {
            if (currentState == self.STATE_DRAGGING_STAGE_WAIT_MOUSE) {
                originalPageX = e.pageX;
                originalPageY = e.pageY;
                currentState = self.STATE_DRAGGING_STAGE;
                keepEditorCanvasWidth = parseInt(editorCanvasJQ.css("width"));
                keepEditorCanvasHeight = parseInt(editorCanvasJQ.css("height"));
                keepEditorCanvasLeft = parseInt(editorCanvasJQ.css("left"));
                keepEditorCanvasTop = parseInt(editorCanvasJQ.css("top"));

                e.preventDefault();
                e.stopPropagation();
            }
        });
        jQuery(document).mousemove(function (e) {
            if (currentState == self.STATE_DRAGGING_STAGE) {
                editorCanvasJQ.css({
                    left: keepEditorCanvasLeft + e.pageX - originalPageX,
                    top: keepEditorCanvasTop + e.pageY - originalPageY,
                    width: keepEditorCanvasWidth - e.pageX + originalPageX,
                    height: keepEditorCanvasHeight - e.pageY + originalPageY
                });

                e.preventDefault();
                e.stopPropagation();
            }
        });
        $(document).mouseup(function (e) {
            if (currentState == self.STATE_DRAGGING_STAGE) {
                currentState = self.STATE_DRAGGING_STAGE_WAIT_MOUSE;

                updateNodeAdditionalViewElements();
                updateEditorCanvas();
                resetJsPlumber();

                e.preventDefault();
                e.stopPropagation();
            }
        });

        // --- EndOf Drag stage when "dragStageKeyCode" key is pressed initialization --- //

        // clear selection if user clicks anywhere with LMB
        jQuery(document).on("click", function (e) {
            if (currentState == self.STATE_WAITING || currentState == self.STATE_SPACE_RESTORED) {
                if ((!jQuery.browser.msie && e.which == 1) || jQuery.browser.msie) {
                    if (savedSelectableStopEvent == undefined || !isTheSamePosition(savedSelectableStopEvent, e.originalEvent)) {
                        AGN.Lib.WM.CampaignManagerSelection.clear();
                    }
                    savedSelectableStopEvent = undefined;
                }
            }
        });

        AGN.Lib.WM.CampaignManagerToolbar.init({
            deleteSelected: function () {
                self.deleteSelectedNode();
            },
            connectSelected: function () {
                return connectSelectedNodes();
            },
            undoSelected: function () {
                self.undo();
            },
            zoomMinSelected: function () {
                var slider = jQuery("#slider");
                var minValue = slider.slider("option", "min");
                var value = slider.slider("option", "value");
                if (value > minValue) {
                    var newValue = slider.slider("option", "value") - slider.slider("option", "step");
                    slider.slider("option", "value", (newValue < minValue) ? minValue : newValue);
                }
            },
            zoomMiddleSelected: function () {
                var slider = jQuery("#slider");
                slider.slider("option", "value", Math.round(slider.slider("option", "max") / 2));
            },
            zoomMaxSelected: function () {
                var slider = jQuery("#slider");
                var maxValue = slider.slider("option", "max");
                var value = slider.slider("option", "value");
                if (value < maxValue) {
                    var newValue = slider.slider("option", "value") + slider.slider("option", "step");
                    slider.slider("option", "value", (newValue > maxValue) ? maxValue : newValue);
                }
            },
            doAutoLayout: function () {
                autoLayout.layoutWorkflow(campaignManagerNodes.getNodes(), nodeConnections);
                boundNodesLeft = 15;
                boundNodesTop = 15;
                rearrangeStage();
            }
        });

        AGN.Lib.WM.CampaignManagerSelection.init({
            nodeSelector: campaignManagerNodes.getIconNodesSelector(),
            onSelectionChanged: function () {
                var selection = this.getSelected();

                if (selection && selection.length) {
                    AGN.Lib.WM.CampaignManagerToolbar.setDeletionAvailable(true);

                    if (AGN.Lib.WM.CampaignManagerToolbar.arrowMode && this.prevSelected != null) {
                        self.connectNodes(this.prevSelected, this.lastSelected, true);
                    }
                } else {
                    AGN.Lib.WM.CampaignManagerToolbar.setDeletionAvailable(false);
                }
            }
        });

        if (!noContextMenu) {
            AGN.Lib.WM.CampaignManagerContextMenu.init({
                nodeSelector: campaignManagerNodes.getIconNodesSelector(),
                campaignManager: self,
                campaignManagerSelection: AGN.Lib.WM.CampaignManagerSelection
            });
        }
        editorCanvasJQ.selectable(getSelectableProperties());
    };

    AGN.Lib.WM.CampaignManager = CampaignManager;
})(jQuery);
