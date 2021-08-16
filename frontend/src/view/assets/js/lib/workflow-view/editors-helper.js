(function() {
    var Def = AGN.Lib.WM.Definitions,
        Select = AGN.Lib.Select;

    var EditorsHelper = function() {
        var options = {};

        this.editors = [];
        this.curEditor;
        this.curEditingNode;

        this.getCurrentNode = function() {
            return this.curEditingNode;
        };

        this.getCurrentNodeId = function() {
            return this.curEditingNode.getId();
        };

        this.getCurrentNodeAnchorsInUse = function() {
            if (options.getNodeAnchorsInUse) {
                return options.getNodeAnchorsInUse(this.curEditingNode);
            }

            return {};
        };

        this.getCurrentEditor = function() {
            return this.curEditor;
        };

        this.assignOptions = function(newOptions) {
            $.extend(options, newOptions);
        };

        this.registerEditor = function(name, editor) {
            return this.editors[name] = editor;
        };

        this.showEditDialog = function(node, isActivatedWorkflow) {
            var self = this;
            var nodeType = node.getType();

            this.curEditingNode = node;
            this.curEditor = this.editors[nodeType];
            this.curEditor.fillEditor(node);

            this.getEditorPanel(nodeType).dialog({
                open: function(event) {
                    var $panel = $(this);
                    var title = $panel.parent().find('.ui-dialog-title');

                    title.empty();
                    title.append($('<span class="dialog-title-image">' + self.curEditor.getTitle() + '</span>'));
                    title.find('.dialog-title-image').attr('data-type', nodeType);

                    if (nodeType == Def.NODE_TYPE_RECIPIENT) {
                        // Only one mailinglist allowed per campaign. That's why we disable editing for the second and others recipient icons.
                        $panel.parent().find('.recipient-editor-select').prop('disabled', node.isDependent() || node.isInRecipientsChain());
                    }

                    // Re-init date pickers.
                    $(event.target).find('.js-datepicker').each(function(index, element) {
                        var $datepicker = $(element).pickadate('picker');
                        $datepicker.set('select', $datepicker.get('select'));
                    });

                    if (isActivatedWorkflow) {
                        disableDialogItems($(event.target));
                    }
                },
                close: function() {
                    if (self.curEditor.closeEditor) {
                        self.curEditor.closeEditor();
                    }
                },
                width: 650,
                height: 'auto',
                minHeight: 'auto',
                modal: true,
                resizable: false
            });
        };

        this.showIconCommentDialog = function(node) {
            var self = this;

            this.curEditingNode = node;
            this.curEditor = this.editors['icon-comment'];
            this.curEditor.fillEditor(node);

            this.getEditorPanel('icon-comment').dialog({
                open: function() {
                    self.getEditorPanel('icon-comment').parent().find('.ui-dialog-titlebar').hide();
                },
                width: 300,
                height: 'auto',
                minHeight: 'auto',
                modal: true,
                resizable: false,
                fluid: true,
                position: {my: 'left top', at: 'left bottom', of: node.get$()}
            });
        };

        this.saveIconComment = function(iconComment) {
            var self = this;
            options.getUndoManager().transaction(function() {
                var node = self.curEditingNode;
                options.getUndoManager().operation('nodeDataUpdated', node, _.cloneDeep(node));

                node.setComment(iconComment);
                options.onChange(node);
            });
        };

        this.saveCurrentEditorWithUndo = function(leaveOpen, mailingEditorBase) {
            var self = this;

            _.defer(function() {
                options.getUndoManager().startTransaction();

                self.saveCurrentEditor(leaveOpen, mailingEditorBase, function() {
                    options.getUndoManager().endTransaction();
                });
            });

        };

        this.saveCurrentEditor = function(leaveOpen, mailingEditorBase, undoCallback) {
            undoCallback = _.isFunction(undoCallback) ? undoCallback : _.noop;

            var self = this;
            var node = this.curEditingNode;

            options.getUndoManager().operation('nodeDataUpdated', node, _.cloneDeep(node), self.curEditor);
            var editorData = self.curEditor.saveEditor();

            // check to close editor dialog
            if (!leaveOpen) {
                self.cancelEditor();
            }

            if (self.isNodeIsMailing(node)) {
                var mailingId = editorData.mailingId;
                if (mailingEditorBase) {
                    mailingEditorBase.checkDifferentMailingLists(mailingId, function(mailingContent) {
                        self.mailingSpecificSave(leaveOpen, mailingContent, editorData, mailingEditorBase);

                        undoCallback();
                    }, function(mailingContent) {
                        self.curEditor.storedMailingContent = mailingContent;
                        self.curEditor.storedMailingId = mailingId;
                        self.curEditor.storedLeaveOpen = leaveOpen;
                        self.curEditor.storedEditorData = editorData;
                        mailingEditorBase.initOneMailinglistWarningDialog(mailingEditorBase);

                        undoCallback();
                    });
                }
            } else {
                $.extend(node.getData(), editorData);

                if (!node.isFilled()) {
                    node.setFilled(true);
                }

                if (node.isFilled() && _.isFunction(self.curEditor.isSetFilledAllowed)) {
                    if (!self.curEditor.isSetFilledAllowed()) {
                        node.setFilled(false);
                    }
                }

                if (options.supplement) {
                    options.supplement(node);
                }

                if (options.onChange) {
                    options.onChange(node);
                }

                undoCallback();
            }
        };

        this.resave = function(node) {
            var bkpNode = this.curEditingNode;
            var exEditor = this.curEditor;

            try {
                this.curEditingNode = node;
                this.curEditor = this.editors[node.getType()];
                this.curEditor.fillEditor(node);
                this.saveCurrentEditor();
            } finally {
                this.curEditingNode = bkpNode;
                this.curEditor = exEditor;
            }
        };

        this.isNodeIsMailing = function(node) {
            return Def.NODE_TYPES_MAILING.includes(node.getType());
        };

        /**
         * Save changes in mailing node of any type (normal, follow-up, action-based, date-based).
         *
         * @param leaveOpen
         * @param mailingContent
         * @param editorData
         * @param mailingEditorBase
         */
        this.mailingSpecificSave = function(leaveOpen, mailingContent, editorData, mailingEditorBase) {
            var self = this;
            var node = this.curEditingNode;
            var data = node.getData();

            $.extend(data, editorData);

            if (node.isFilled() && _.isFunction(self.curEditor.isSetFilledAllowed)) {
                if (!self.curEditor.isSetFilledAllowed()) {
                    node.setFilled(false);
                }
            }

            if (options.supplement) {
                options.supplement(node, mailingEditorBase, mailingContent);
            }

            if (options.onChange) {
                options.onChange(node);
            }
        };

        /**
         * Restore data that was stored before showing modal and save changes of mailing.
         */
        this.mailingSpecificSaveAfterMailinglistCheckModal = function(mailingEditorBase) {
            this.curEditor.storedMailingContent.mailinglistId = mailingEditorBase.configuredMailingData.mailinglistId;
            this.mailingSpecificSave(this.curEditor.storedLeaveOpen, this.curEditor.storedMailingContent, this.curEditor.storedEditorData, mailingEditorBase);
        };

        this.cancelEditor = function() {
            var nodeType = this.curEditingNode.getType();
            var $panel = this.getEditorPanel(nodeType);

            if ($panel.dialog('instance')) {
                $panel.dialog('close');
            }
        };

        this.getEditorPanel = function(type) {
            var editorType = (type == 'stop') ? 'start' : type;
            return $('#' + editorType + '-editor');
        };

        this.formToObject = function(formName) {
            var formData = $('form[name="' + formName + '"]').serializeArray();
            var result = {};

            $.each(formData, function() {
                if (this.name.indexOf('[') != -1) {
                    var arrName = this.name.substring(0, this.name.indexOf('['));
                    if (result[arrName] == undefined) {
                        result[arrName] = [];
                    }
                    var index = this.name.substring(this.name.indexOf('[') + 1, this.name.indexOf(']'));
                    if (result[arrName][index] == undefined) {
                        result[arrName][index] = {};
                    }
                    if (this.name.indexOf('.') != -1) {
                        var propName = this.name.substring(this.name.indexOf('.') + 1);
                        result[arrName][index][propName] = this.value;
                    } else {
                        result[arrName][index] = this.value;
                    }
                } else {
                    result[this.name] = this.value || '';
                }
            });

            // Handle unselected checkboxes.
            $('form[name="' + formName + '"] [type="checkbox"]').each(function() {
                result[this.name] = $(this).prop('checked');
            });

            return result;
        };

        this.fillFormFromObject = function(formName, data, namePrefix, defaultData) {
            if (typeof data === 'string') {
                this.fillFormFromObjectItem(formName, namePrefix, namePrefix, data || defaultData);
            } else {
                for (var nameKey in data) {
                    if (data.hasOwnProperty(nameKey)) {
                        var value = data[nameKey];
                        if (!value && defaultData && defaultData[nameKey]) {
                            value = defaultData[nameKey];
                        }
                        this.fillFormFromObjectItem(formName, namePrefix, nameKey, value);
                    }
                }
            }
        };

        this.fillFormFromObjectItem = function(formName, namePrefix, name, val) {
            if (Object.prototype.toString.call(val) === '[object Array]') {
                for (var i = 0; i < val.length; i++) {
                    this.fillFormFromObject(formName, val[i], name + '[' + i + ']' + '.');
                }
            } else {
                var $el = $('form[name="' + formName + '"] [name="' + namePrefix + name + '"]');
                var type = $el.attr('type');
                switch (type) {
                    case 'checkbox':
                        $el.prop('checked', Boolean(val));
                        break;
                    case 'radio':
                        $el.filter('[value="' + val + '"]').prop('checked', true);
                        break;
                    default:
                        var tagName = $el.prop('tagName');
                        switch (tagName) {
                            //since we started using select2 we should perform such initialization
                            case 'SELECT' :
                                $el.val(val);
                                this.initSelectWithValueOrChooseFirst($el, val);
                                break;
                            default:
                                $el.val(val);
                        }
                }
            }
        };

        this.processForward = function(forwardName, elementId, multiSelectors, submitFn, extraForwardParams) {
            $('#forwardName').val(forwardName);
            $('#forwardParams').val(
                'nodeId=' + this.curEditingNode.getId()
                + ';elementId=' + encodeURIComponent(elementId) + (extraForwardParams ? ';' + extraForwardParams : '')
            );

            if (this.curEditor.safeToSave) {
                this.saveCurrentEditor();
            }

            submitFn(false);
        };

        this.getFirstOptionValue = function($el) {
            return Select.get($el).getFirstValue();
        };

        this.resetSelect = function($el) {
            Select.get($el).clear();
        };

        this.initSelectWithFirstValue = function($el) {
            Select.get($el).selectFirstValue();
        };

        this.initSelectWithValueOrChooseFirst = function($el, selectedValue) {
            Select.get($el).selectValueOrSelectFirst(selectedValue)
        };

        this.getNodesByTypes = function(types) {
            if (options.getNodesByTypes) {
                return options.getNodesByTypes(types);
            }
            return [];
        };

        this.getNodesByType = function(type) {
            return this.getNodesByTypes([type]);
        };

        this.getNodesByIncomingConnections = function(node) {
            if (options.getNodesByIncomingConnections) {
                return options.getNodesByIncomingConnections(node);
            }
            return [];
        };

        this.getNodesByOutgoingConnections = function(node) {
            if (options.getNodesByOutgoingConnections) {
                return options.getNodesByOutgoingConnections(node);
            }
            return [];
        };

        this.getFirstIncomingChain = function() {
            if (this.curEditingNode && options.getFirstIncomingChain) {
                return options.getFirstIncomingChain(this.curEditingNode);
            }

            return [];
        };

        this.forEachPreviousNode = function(callback) {
            var self = this;

            if (this.curEditingNode && options.forEachPreviousNode) {
                return options.forEachPreviousNode(this.curEditingNode, function(node) {
                    return callback.call(self, node);
                });
            }

            return [];
        };

        this.deleteNode = function(nodeId) {
            if (options.deleteNodeById) {
                options.deleteNodeById(nodeId);
            }
        };

        this.openNode = function(node, needCheckActivation) {
            if (options.editNode) {
                options.editNode(node, needCheckActivation);
            }
        };
    };

    function disableDialogItems($target) {
        $target.find('.disable-for-active').each(function(index, element) {
            $(element).attr('disabled', true);
        });

        $target.find('.hide-for-active').each(function(index, element) {
            $(element).hide();
        });

        $target.find(':input:enabled[type!="hidden"]').not(':button, .select2, .js-select, .js-datepicker').each(function(index, element) {
            if ($(element).prop('type') == 'radio' || $(element).prop('type') == 'checkbox') {
                $(element).attr('disabled', true);
            } else {
                $(element).attr('readonly', true);
            }
        });

        $target.find(':input:enabled.select2, :input:enabled.js-select, .select2-container').not('.disabled, :disabled').each(function(index, element) {
            if ($(element).prop('type') == 'text' || $(element).is('input, select')) {
                $(element).attr('disabled', true);
            } else {
                $(element).select2('disable');
            }
        });

        $target.find('.js-datepicker').each(function(index, element) {
            $(element).attr('disabled', true);
        });
    }

    //TODO: rename to EditorsHelper after old code deleted
    AGN.Lib.WM.EditorsHelperNew = new EditorsHelper();
})();
