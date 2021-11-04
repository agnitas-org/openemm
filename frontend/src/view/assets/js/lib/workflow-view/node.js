(function() {
    var DateTimeUtils = AGN.Lib.WM.DateTimeUtils,
        Template = AGN.Lib.Template,
        Def = AGN.Lib.WM.Definitions,
        Popover = AGN.Lib.Popover;

    function Node(type, data) {
        this.id = null;
        this.x = 0;
        this.y = 0;
        this.type = type;
        this.title = '';
        this.statisticTitle = '';
        this.overlayTitle = '';
        this.statistic = [];
        this.overlayImage = {image: '', title: '', visible: false};
        this.filled = false;
        this.editable = true;
        this.expandable = false;
        // This attribute will be true if recipient icon is supplemented in that sequence "mailing > deadline > recipient > … > mailing".
        this.dependent = false;  // For recipient icon only.
        // This attribute will be true if recipient icon is supplemented in that sequence " … > recipient > recipient > recipient > … ".
        this.inRecipientsChain = false; // For recipient icon only.
        this.anchors = Def.ICONS_CONFIG[type].anchors;
        this.data = data;
        this.$element = Node.create$(type);
        this.$element.data('agn:node', this);
        this.$connectionButton = this.$element.find('.node-connect-button');
        this.$titleDiv = Node.createTitleDiv$();
        this.$titleSpan = this.$titleDiv.find('.icon-title-span');
        this.$statisticSpan = this.$titleDiv.find('.icon-statistic-span');
        this.$overlayTitle = this.$element.find('.icon-overlay-title');
        this.$overlayImage = this.$element.find('.icon-overlay-image img');
        this.nodePopover = new NodePopover(this);
        this.lastTitleAnchor = Def.BOTTOM;
    }

    Node.restore = function(node) {
        var agnData = node.get$().data('agn:node');
        if (!agnData) {
            node.get$().data('agn:node', node);
        }

        return node;
    };

    Node.get = function($element) {
        if ($element instanceof HTMLElement) {
            $element = $($element);
        }

        if (!$element.is('.node')) {
            $element = $element.closest('.node');
        }

        return $element.data('agn:node');
    };

    Node.isDecisionNode = function(node) {
        return node.getType() === Def.NODE_TYPE_DECISION;
    };

    Node.isMailingNode = function(node) {
        if (!node) {
            return false;
        }

        return Def.NODE_TYPES_MAILING.includes(node.getType());
    };

    Node.isBranchingDecisionNode = function(node) {
        if (Node.isDecisionNode(node)) {
            var data = node.getData();
            if (data.decisionType === Def.constants.decisionTypeDecision) {
                return true;
            }
        }

        return false;
    };

    Node.isDecisionBranchesOrderFromPositiveToNegative = function(source, target1, target2) {
        if (target1.y <= source.y && target2.y <= source.y) {
            // All nodes above the decision node?

            if (target1.y < target2.y) {
                return true;
            } else if (target2.y < target1.y) {
                return false
            } else {
                return target1.x < target2.x;
            }
        } else if (target1.y > source.y && target2.y > source.y) {
            // All nodes below the decision node?
            return target1.x > target2.x;
        } else {
            // One of node above and another under the decision node.
            return target1.y < target2.y;
        }
    };

    Node.getDecisionBranches = function(connections) {
        if (connections.length === 2) {
            var source = Node.getPosition(connections[0].source.get$());
            var target1 = Node.getPosition(connections[0].target.get$());
            var target2 = Node.getPosition(connections[1].target.get$());

            if (this.isDecisionBranchesOrderFromPositiveToNegative(source, target1, target2)) {
                return {positive: connections[0], negative: connections[1]};
            } else {
                return {positive: connections[1], negative: connections[0]};
            }
        }

        return null;
    };

    Node.isCommented = function(node) {
        return !!_.trim(node.getComment());
    };

    Node.isSelected = function($element) {
        return $element.hasClass('ui-selected');
    };

    Node.setSelected = function($element, isSelected) {
        return $element.toggleClass('ui-selected', !!isSelected);
    };

    Node.isChainSource = function($element) {
        return $element.hasClass('chain-mode-source');
    };

    Node.setChainSource = function($element, isSource) {
        $element.toggleClass('chain-mode-source', !!isSource);
    };

    Node.getAnchors = function($node) {
        var node = Node.get($node);
        if (node) {
            return node.getAnchors();
        }

        return Def.ICONS_CONFIG[$node.data('type')].anchors;
    };

    Node.getAnchorsInUse = function(endpoints) {
        var anchorsInUse = {};

        endpoints.forEach(function(endpoint) {
            var orientation = endpoint.anchor.getOrientation();

            if (orientation[0] > 0) {
                anchorsInUse[Def.RIGHT] = true;
            } else if (orientation[0] < 0) {
                anchorsInUse[Def.LEFT] = true;
            } else if (orientation[1] > 0) {
                anchorsInUse[Def.BOTTOM] = true;
            } else if (orientation[1] < 0) {
                anchorsInUse[Def.TOP] = true;
            }
        });

        return anchorsInUse;
    };

    Node.getPosition = function($node) {
        var position = $node.position();
        return {
            x: position.left,
            y: position.top
        };
    };

    Node.getBox = function($node) {
        var position = $node.position();
        var rect = $node[0].getBoundingClientRect();
        return {
            minX: position.left,
            minY: position.top,
            maxX: position.left + rect.width,
            maxY: position.top + rect.height
        };
    };

    Node.isInRegion = function($node, regionBox) {
        var nodeBox = Node.getBox($node);
        return (nodeBox.minX >= regionBox.minX && nodeBox.minX <= regionBox.maxX ||
          nodeBox.maxX >= regionBox.minX && nodeBox.maxX <= regionBox.maxX) &&
          (nodeBox.minY >= regionBox.minY && nodeBox.minY <= regionBox.maxY ||
            nodeBox.maxY >= regionBox.minY && nodeBox.maxY <= regionBox.maxY)
    };

    Node.getCollisionBox = function($node) {
        var anchors = Node.getAnchors($node);
        var position = $node.position();
        var rect = $node[0].getBoundingClientRect();

        var paddingLeft = (anchors[0][0] - Def.NODE_MIN_MARGIN) * rect.width;
        var paddingTop = (anchors[1][1] - Def.NODE_MIN_MARGIN) * rect.height;
        var paddingRight = (1 - anchors[2][0] - Def.NODE_MIN_MARGIN) * rect.width;
        var paddingBottom = (1 - anchors[3][1] - Def.NODE_MIN_MARGIN) * rect.height;

        return {
            minX: position.left + paddingLeft,
            minY: position.top + paddingTop,
            maxX: position.left + rect.width - paddingRight,
            maxY: position.top + rect.height - paddingBottom
        };
    };

    Node.detectBoxCollision = function(box1, box2) {
        return !(box1.minX > box2.maxX || box1.maxX < box2.minX || box1.minY > box2.maxY || box1.maxY < box2.minY);
    };

    Node.create$ = (function() {
        var render = null;

        return function(type) {
            if (render == null) {
                render = Template.prepare('workflow-node');
            }

            return $(render({icons: Def.ICONS_CONFIG[type].icons}));
        };
    })();

    Node.createDraggable$ = (function() {
        var render = null;

        return function(type) {
            if (render == null) {
                render = Template.prepare('workflow-draggable-node');
            }

            return $(render({type: type, icons: Def.ICONS_CONFIG[type].icons}));
        };
    })();

    Node.createTitleDiv$ = (function() {
        var render = null;

        return function() {
            if (render == null) {
                render = Template.prepare('workflow-icon-title');
            }

            return $(render({}));
        };
    })();

    Node.create = function(type) {
        var data = {
            iconComment: ''
        };
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
                data.dateProfileField = '';
                data.dateFieldOperator = 1;
                data.dateFieldValue = '';
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
                data.skipEmptyBlocks = false;
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

    Node.isExpandableType = function(type) {
        switch (type) {
            case 'scABTest':
            case 'scBirthday':
            case 'scDOI':
            case 'ownWorkflow':
                return true;

            default:
                return false;
        }
    };

    Node.deserialize = function(object) {
        var data = $.extend({}, object);
        var node = new Node(object.type, data);

        node.setId(object.id);
        node.setCoordinates(object.x, object.y);
        node.setTitle(object.iconTitle);
        node.setComment(object.iconComment);
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
                if (icon.connections) {
                    icon.connections.forEach(function(connection) {
                        var target = nodesMap[connection.targetIconId];
                        if (target instanceof Node) {
                            connections.push({source: source, target: target});
                        }
                    });
                }
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

    Node.prototype.appendTo = function($container, $titlesContainer, animation) {
        this.$container = $container;
        this.$titlesContainer = $titlesContainer;

        if (animation) {
            this.$element.addClass(animation);
        }

        $container.append(this.$element);
        $titlesContainer.append(this.$titleDiv);

        this.$titleDiv.toggleClass('expandable', this.$titleSpan.height() > this.$titleDiv.height());
    };

    Node.prototype.isAppended = function() {
        return !!this.$container;
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

    Node.prototype.getY = function() {
        return this.y;
    };

    Node.prototype.getCoordinates = function() {
        return {
            x: this.x,
            y: this.y
        };
    };

    Node.prototype.setCoordinates = function(x, y) {
        this.x = x;
        this.y = y;

        this.$element.css('left', x * Def.CANVAS_GRID_SIZE);
        this.$element.css('top', y * Def.CANVAS_GRID_SIZE);
    };

    Node.prototype.updateCoordinates = function(scale) {
        var position = Node.getPosition(this.$element);

        this.x = Math.round(position.x / scale / Def.CANVAS_GRID_SIZE);
        this.y = Math.round(position.y / scale / Def.CANVAS_GRID_SIZE);
    };

    Node.prototype.getType = function() {
        return this.type;
    };

    Node.prototype.setType = function(type) {
        this.type = type;
    };

    Node.prototype.setStatistic = function(enabled, statistic) {
        this.statistic = statistic || [];
    }

    Node.prototype.getStatistic = function() {
        return this.statistic;
    }

    Node.prototype.isStatisticEnabled = function() {
        return !_.isEmpty(this.statistic);
    }

    Node.prototype.getTitle = function() {
        return this.title;
    };

    Node.prototype.setTitle = function(title, statisticTitle) {
        this.title = title;
        this.statisticTitle = statisticTitle;

        if (title || statisticTitle) {
            this.$titleSpan.text(title);
            this.$statisticSpan.text(statisticTitle);
            this.positionTitle(this.lastTitleAnchor);
            this.$titleDiv.toggleClass('expandable', this.$titleSpan.height() + this.$statisticSpan.height() > this.$titleDiv.height());
        }

        this.setTitleEnabled(!!title || !!statisticTitle);
    };

    Node.prototype.setTitleEnabled = function(isEnabled) {
        this.$titleDiv.toggle(!!isEnabled && (!!this.title || !!this.statisticTitle));
    };

    Node.prototype.setTitleExpanded = function(isExpanded) {
        this.$titleDiv.toggleClass('expanded', !!isExpanded);

        // When title is expanded it has be shown over all the icons so append it to common container
        // where z-index brings it to the top.

        if (this.isAppended()) {
            if (isExpanded) {
                this.$titleDiv.appendTo(this.$container);
            } else {
                this.$titleDiv.appendTo(this.$titlesContainer);
            }
        }
    };

    Node.prototype.positionTitle = function(anchor) {
        this.lastTitleAnchor = anchor;

        var minX = this.x * Def.CANVAS_GRID_SIZE;
        var maxX = minX + Def.NODE_SIZE;
        var minY = this.y * Def.CANVAS_GRID_SIZE;
        var maxY = minY + Def.NODE_SIZE;

        switch (anchor) {
            case Def.BOTTOM:
                this.$titleDiv.css({
                    top: maxY,
                    left: minX + (Def.NODE_SIZE - Def.TITLE_WIDTH) / 2,
                    transform: 'none'
                });
                break;

            case Def.LEFT:
                this.$titleDiv.css({
                    top: (minY + maxY) / 2,
                    left: minX - Def.TITLE_WIDTH,
                    transform: 'translateY(-50%)'
                });
                break;

            case Def.RIGHT:
                this.$titleDiv.css({
                    top: (minY + maxY) / 2,
                    left: maxX,
                    transform: 'translateY(-50%)'
                });
                break;

            case Def.TOP:
                this.$titleDiv.css({
                    top: minY,
                    left: minX + (Def.NODE_SIZE - Def.TITLE_WIDTH) / 2,
                    transform: 'translateY(-100%)'
                });
                break;

            default: return;
        }
    };

    Node.prototype.getOverlayTitle = function() {
        return this.overlayTitle;
    };

    Node.prototype.setOverlayTitle = function(overlayTitle) {
        this.overlayTitle = overlayTitle;
        this.$overlayTitle.text(_.trim(overlayTitle));
    };

    Node.prototype.getOverlayImage = function() {
        return this.overlayImage;
    };

    Node.prototype.setOverlayImage = function(overlayImage) {
        this.overlayImage = $.extend({image: '', title: '', visible: false}, overlayImage);

        var src = !!_.trim(this.overlayImage.image) ? Def.constants.imagePath + '/' + _.trim(this.overlayImage.image) : '';

        this.$overlayImage.prop('src', src);
        this.$overlayImage.prop('title', this.overlayImage.title);

        if (this.overlayImage.visible) {
            this.$overlayImage.show();
        } else {
            this.$overlayImage.hide();
        }
    };

    Node.prototype.getComment = function() {
        return this.comment;
    };

    Node.prototype.setComment = function(comment) {
        this.comment = this.data.iconComment = comment;
        this.$element.toggleClass('icon-commented', !!_.trim(comment))
    };

    Node.prototype.updateCommentLabels = function() {
        this.$element.toggleClass('icon-commented', !!_.trim(this.comment));
    };

    Node.prototype.setFootnote = function(footnoteId) {
        if (footnoteId > 0) {
            this.$element.attr('comment-footnote', footnoteId);
        } else {
            this.$element.removeAttr('comment-footnote');
        }
    };

    Node.prototype.isFilled = function() {
        return this.filled;
    };

    Node.prototype.setFilled = function(filled) {
        this.filled = filled;
        this.$element.toggleClass('active', filled);
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

    Node.prototype.isInRecipientsChain = function () {
        return this.inRecipientsChain;
    }

    Node.prototype.setInRecipientsChain = function (inRecipientsChain) {
        this.inRecipientsChain = inRecipientsChain;
    }

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

    Node.prototype.setInteractionEnabled = function(isEnabled) {
        this.$connectionButton.toggle(!!isEnabled);
    };

    Node.prototype.removePopover = function() {
        this.nodePopover.remove();
    };

    Node.prototype.setPopoverEnabled = function(isEnabled) {
        if (!isEnabled) {
            this.nodePopover._hidePopover(true);
        }
        this.nodePopover.setEnabled(isEnabled);
    };

    Node.prototype.setPopoverShown = function(isShown) {
        if (isShown) {
            this.nodePopover.show();
        } else {
            this.nodePopover.hide();
        }
    };

    Node.prototype.setHovered = function(isHovered) {
        this.setPopoverShown(isHovered);
        this.setTitleExpanded(isHovered);
    };

    Node.prototype.remove = function() {
        this.nodePopover.remove();
        this.$element.remove();
        this.$titleDiv.remove();
        this.$container = null;
        this.$titlesContainer = null;
    };

    Node.prototype.serialize = function() {
        var object = $.extend({}, this.data, {
            id: this.id,
            x: this.x,
            y: this.y,
            type: this.type,
            filled: this.filled,
            editable: this.editable,
            iconTitle: this.title
        });

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

    function NodePopover(node) {
        if (node instanceof Node) {
          this.node = node;
        } else {
          this.node = Node.get(node);
        }
        this.popover = undefined;
        this.timeoutId = undefined;
        this.isEnabled = true;
    }

    NodePopover.prototype.remove = function() {
        if (this.popover) {
            this.popover.hide();
        }
        Popover.remove(this.node.get$());
        this.popover = null;
    }

    NodePopover.destroyAll = function() {
        $("[rel=popover]").each(function(i, node) {
            Node.get($(node)).removePopover();
        });

        $('.popover').remove();
    };

    NodePopover.prototype.initPopover = function() {
        var self = this;
        var popover = Popover.new(self.node.get$(), {
            trigger: 'manual',
            html: true,
            template: Def.NODE_POPOVER_TEMPLATE,
            content: function () {return self.getPopoverContent()}
        });

        if (self.isEnabled) {
            popover.enable();
        } else {
            popover.disable();
        }

        var $tip = popover.tip();
        $tip.on({
            mouseenter: function() {self._showPopover();},
            mouseleave: function() {self._hidePopover(true);}
        });

        return popover;
    }

    NodePopover.prototype.show = function() {
        NodePopover.destroyAll();
        this.popover = this.initPopover();
        if (this.popover.hasContent() && this.popover.enabled) {
            this._showPopover();
        }
    };

    NodePopover.prototype.hide = function(immediate) {
        this._hidePopover(immediate);
    };

    NodePopover.prototype.getPopoverContent = function() {
        var node = this.node;

        var content = '';

        var thumbnail = NodePopover.getThumbnail(node);
        if (thumbnail && !_.isEmpty(thumbnail.src)) {
          content += '<img src="' + thumbnail.src + '" style="width: 100%; height: 100%;" />';
        }

        if (Node.isCommented(node)) {
          content += '<div>' + node.getComment() + '</div>';
        }

        return content;
    };

    NodePopover.getThumbnail = function(node) {
        if (Node.isMailingNode(node) && node.getData().mailingId > 0) {
            return NodePopover.getMailingThumbnail(node.getData().mailingId);
        }

        return '';
    };

    NodePopover.getMailingThumbnail = _.memoize(function(mailingId) {
        var img = '';
        $.ajax({
            type: "POST",
            url: AGN.url('/workflow/getMailingThumbnail.action'),
            async: false,
            data: {
              mailingId: mailingId
            },
            success: function (componentId) {
              if (componentId > 0) {
                img = new Image;
                img.src = AGN.url('/sc?compID=' + componentId);
              }
            }
        });

        return img;
    });

    NodePopover.prototype._stopHiding = function() {
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
            this.timeoutId = undefined;
        }
    };

    NodePopover.prototype._showPopover = function() {
        this._stopHiding();
        this.popover.show();
    };

    NodePopover.prototype._hidePopover = function(immediate) {
        this._stopHiding();

        if (!this.popover) {
            return;
        }

        if (immediate === true) {
            if (this.popover) {
                this.popover.hide();
            }
        } else {
            var self = this;
            this.timeoutId = setTimeout(function() {
                self._hidePopover(true);
            }, 200);
        }
    };

    NodePopover.prototype.setEnabled = function(isEnabled) {
        this.isEnabled = isEnabled;

        if (!this.popover) {
            return;
        }
        if (isEnabled) {
            this.popover.enable();
        } else {
            this.popover.disable();
        }
    };

    AGN.Lib.WM.NodePopover = NodePopover;
    AGN.Lib.WM.Node = Node;
})();
