(function() {
    const Def = AGN.Lib.WM.Definitions;
    const Utils = AGN.Lib.WM.Utils;
    const Node = AGN.Lib.WM.Node;
    const Select = AGN.Lib.Select;

    var EditorsHelper = function() {
        var options = {};

        this.editors = [];
        this.curEditor;
        this.curEditingNode;

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

        this._notAllowedToChangeDuringPause = function(node) {
            return Utils.isPausedWorkflow()
                && !_.union(Def.NODE_TYPES_MAILING, [Def.NODE_TYPE_STOP]).includes(node.getType());
        }
        
        this.hideNodeEditors = function () {
          $('#node-editor > [id$="-editor"]').hide();
        }
        
        this.activeNodeIcon = function (node) {
          $('.node').removeClass('under-edit');
          node.$element.addClass('under-edit');
        }
        
        this.toggleSelectNodeInfoMsg = function (show) {
          $('#select-node-notification').toggle(show);
        }

        this.exitNodeEditorIfActive = function (node) {
          if (this.curEditingNode !== node) {
            return;
          }
          this.hideNodeEditors();
          this.toggleSelectNodeInfoMsg(true);
        }

        this.showEditDialog = function(node) {
          const nodeType = node.getType();
          this.curEditingNode = node;
          this.curEditor = this.editors[nodeType];
          this.curEditor.fillEditor(node);

          this.activeNodeIcon(node);
          this.toggleSelectNodeInfoMsg(false);
          this.hideNodeEditors();
          const $editor = this.getEditorPanel(nodeType);
          AGN.runAll($editor.find('form'));
          $('#node-editor').scrollTop(0)
          $editor.show();
          
          if (nodeType === Def.NODE_TYPE_RECIPIENT) {
              // Only one mailinglist allowed per campaign. That's why we disable editing for the second and others recipient icons.
            $editor.parent().find('.recipient-editor-select').prop('disabled', node.isDependent() || node.isInRecipientsChain());
          }
          if (this.isReadOnlyMode() || this.curEditor.alwaysReadonly(node)) {
              disableDialogItems($editor);
          }
          if (this.curEditor.saveOnOpen && !this.isReadOnlyMode() && !this.curEditor.alwaysReadonly(node)) {
            this.curEditor.save();
          }
        };
        
        this.isReadOnlyMode = function () {
          return Utils.checkActivation(!Node.isMailingNode(this.curEditingNode))
            || this._notAllowedToChangeDuringPause(this.curEditingNode);
        }

        this.showIconCommentDialog = function(node) {
            const self = this;

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
            const node = this.curEditingNode;
            options.getUndoManager().transaction(function() {
                options.getUndoManager().operation('nodeDataUpdated', node, _.cloneDeep(node));
                node.setComment(iconComment);
                options.onChange(node);
            });
            node.nodePopover?.update();
        };

        this.saveCurrentEditorWithUndo = function(mailingEditorBase) {
            _.defer(() => {
                options.getUndoManager().startTransaction();
                this.saveCurrentEditor(mailingEditorBase, () => options.getUndoManager().endTransaction());
            });
        };

        this.saveCurrentEditor = function(mailingEditorBase, undoCallback) {
            undoCallback = _.isFunction(undoCallback) ? undoCallback : _.noop;

            var self = this;
            var node = this.curEditingNode;

            options.getUndoManager().operation('nodeDataUpdated', node, _.cloneDeep(node), self.curEditor);
            var editorData = self.curEditor.saveEditor();

            if (self.isNodeIsMailing(node)) {
                var mailingId = editorData.mailingId;
                if (mailingEditorBase) {
                    mailingEditorBase.checkDifferentMailingLists(mailingId,
                      function(mailingContent) {
                        self.mailingSpecificSave(mailingContent, editorData, mailingEditorBase);

                        undoCallback();
                    },
                      function(mailingContent) {
                        self.curEditor.storedMailingContent = mailingContent;
                        self.curEditor.storedMailingId = mailingId;
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

        this.modify = function(node, callback) {
            options.getUndoManager().operation('nodeDataUpdated', node, _.cloneDeep(node));
            callback(node);

            if (options.onChange) {
                options.onChange(node);
            }
        };

        this.isNodeIsMailing = function(node) {
            return Def.NODE_TYPES_MAILING.includes(node.getType());
        };

        /**
         * Save changes in mailing node of any type (normal, follow-up, action-based, date-based).
         *
         * @param mailingContent
         * @param editorData
         * @param mailingEditorBase
         */
        this.mailingSpecificSave = function(mailingContent, editorData, mailingEditorBase) {
            const node = this.curEditingNode;
            const data = node.getData();

            $.extend(data, editorData);

            if (node.isFilled() && _.isFunction(this.curEditor.isSetFilledAllowed)) {
                if (!this.curEditor.isSetFilledAllowed()) {
                    node.setFilled(false);
                }
            }

            if (options.supplement) {
                options.supplement(node, mailingEditorBase, mailingContent);
            }

            if (options.onChange) {
                options.onChange(node);
            }
            node.nodePopover.update();
            node.toggleInUseBadge();
        };

        /**
         * Restore data that was stored before showing modal and save changes of mailing.
         */
        this.mailingSpecificSaveAfterMailinglistCheckModal = function(mailingEditorBase) {
            this.curEditor.storedMailingContent.mailinglistId = mailingEditorBase.configuredMailingData.mailinglistId;
            this.mailingSpecificSave(this.curEditor.storedMailingContent, this.curEditor.storedEditorData, mailingEditorBase);
        };

        this.getEditorPanel = function(type) {
            const editorType = (type === 'stop') ? 'start' : type;
            return $(`#${editorType}-editor`);
        };

        this.formToObject = function(formName) {
            const $form = $(`form[name="${formName}"]`);
            const groupedFormData = _.groupBy($form.serializeArray(), 'name');

            const multiValueNames = new Set(
              $form.find('select.dynamic-tags')
                .map((_, el) => el.name)
                .get()
            );

            /*
               Explanation: Only fields associated with <select class="dynamic-tags"> are allowed
               to have multiple values. This precaution prevents unexpected behavior caused by
               other repeated input fields (e.g., checkboxes or hidden fields with the same name).
               It ensures that only intentionally multi-valued fields are grouped into arrays.
             */
            const formData = Object.entries(groupedFormData)
              .map(([name, items]) => ({
                  name,
                  value: multiValueNames.has(name) ? items.map(i => i.value) : items[items.length - 1].value
              }));

            let result = {};

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
            $(`form[name="${formName}"] [type="checkbox"]`).each(function() {
                result[this.name] = $(this).prop('checked');
            });

            // Handle multi-selects without selected options
            $(`form[name="${formName}"] select.dynamic-tags`).each(function() {
                if (!result[this.name]) {
                    result[this.name] = [];
                }
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
            const $el = $(`form[name="${formName}"] [name="${namePrefix}${name}"]`);

            if (Object.prototype.toString.call(val) === '[object Array]' && !$el.is('select.dynamic-tags')) {
                for (let i = 0; i < val.length; i++) {
                    this.fillFormFromObject(formName, val[i], `${name}[${i}].`);
                }
            } else {
                switch ($el.attr('type')) {
                    case 'checkbox':
                        $el.prop('checked', Boolean(val));
                        break;
                    case 'radio':
                        $el.filter(`[value="${val}"]`).prop('checked', true);
                        break;
                    default:
                        switch ($el.prop('tagName')) {
                          //since we started using select2 we should perform such initialization
                            case 'SELECT':
                                if ($el.hasClass('dynamic-tags')) {
                                    if (val instanceof Array) {
                                        const select = Select.get($el);

                                        val.forEach(_val => {
                                            select.addOptionIfMissing(_val);
                                            select.selectOption(_val);
                                        })
                                    }
                                } else {
                                    $el.val(val);
                                    this.initSelectWithValueOrChooseFirst($el, val);
                                }
                                break;
                            default:
                                $el.val(val);
                        }
                }
            }
        };

        this.processForward = function (forwardName, elementId, submitFn, extraForwardParams) {
            var forwardParams = [];
            forwardParams.push('nodeId=' + this.curEditingNode.getId());
            forwardParams.push('elementId=' + encodeURIComponent(elementId));
            if (!!extraForwardParams) {
                forwardParams.push(extraForwardParams);
            }
            const options = {
                forwardName: forwardName,
                forwardParams: forwardParams.join(';')
            }

            if (this.curEditor.safeToSave) {
                this.saveCurrentEditor();
            }

            submitFn(false, options);
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
        $target.find('.disable-for-active').each((i, element) => $(element).attr('disabled', true));

        $target.find('.hide-for-active').each((index, element) => $(element).hide());

        $target.find(':input:enabled[type!="hidden"]').not(':button, .select2, .js-select, .js-datepicker').each((i, element) => {
            $(element).attr(['radio', 'checkbox'].includes($(element).prop('type')) ? 'disabled' : 'readonly', true);
        });

        $target.find(':input:enabled.select2, .select2 .btn, :input:enabled.js-select, select').not('.disabled, :disabled').each(function(index, element) {
            if ($(element).prop('type') === 'text' || $(element).is('input, select')) {
                $(element).attr('disabled', true);
            } else {
                $(element).prop('disabled', true);
            }
        });

        $target.find('.js-datepicker').each(function(index, element) {
            $(element).attr('disabled', true);
        });
    }

    AGN.Lib.WM.EditorsHelper = new EditorsHelper();
})();
