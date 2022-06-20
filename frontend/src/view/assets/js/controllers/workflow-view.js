AGN.Lib.Controller.new('workflow-view', function() {
    var Def = AGN.Lib.WM.Definitions,
        Editor = AGN.Lib.WM.Editor,
        Node = AGN.Lib.WM.Node,
        EditorsHelper = AGN.Lib.WM.EditorsHelper,
        MailingEditorHelper = AGN.Lib.WM.MailingEditorHelper,
        Dialogs = AGN.Lib.WM.Dialogs,
        MailingSelector = AGN.Lib.WM.MailingSelector,
        Confirm = AGN.Lib.Confirm,
        Template = AGN.Lib.Template,
        Utils = AGN.Lib.WM.Utils,
        Select = AGN.Lib.Select,
        DateTimeUtils = AGN.Lib.WM.DateTimeUtils;

    var controller = this;
    var editor;

    // Register custom connector type.
    if (!jsPlumb.Connectors['FixedBezierConnector']) {
        jsPlumbUtil.extend(AGN.Opt.Components.JsPlumbFixedBezierConnector, jsPlumb.Connectors.AbstractConnector);
        jsPlumb.Connectors['FixedBezierConnector'] = AGN.Opt.Components.JsPlumbFixedBezierConnector;
    }

    this.addDomInitializer('workflow-view', function() {
        var config = this.config;

        Def.constants = config.constants;
        Def.workflowId = config.workflowId;
        Def.shortname = config.shortname;

        var nodes = config.icons.map(Node.deserialize);
        var connections = Node.deserializeConnections(config.icons, Node.toMap(nodes));

        if (editor) {
            editor.recycle();
        }

        editor = new Editor(config.isEditable !== false, config.isContextMenuEnabled !== false, config.isFootnotesEnabled === true);

        editor.setOnInitialized(function() {
            editor.batch(function() {
                // Add all the nodes to the editor.
                nodes.forEach(function(node) {
                    editor.add(node);
                });

                // Establish all the connections.
                connections.forEach(function(connection) {
                    editor.connect(connection.source, connection.target);
                });
            });

            editor.updateNodeTitles();
            editor.updateFootnotes();
        });

        editor.setMinimapEnabled(config.isMinimapEnabled !== false);
        editor.setStatisticEnabled(config.isStatisticEnabled === true);

        var slider = $('#slider');
        if (slider.exists()) {
            slider.slider({
                min: Def.MIN_ZOOM,
                max: Def.MAX_ZOOM,
                value: Def.DEFAULT_ZOOM,
                step: Def.ZOOM_STEP,
                create: function() {
                    var slider = $(this).slider('instance');

                    editor.setZoom(Def.DEFAULT_ZOOM);
                    editor.setOnZoom(function(scale) {
                        slider.value(scale);
                    });
                },
                slide: function(event, ui) {
                    editor.setZoom(ui.value);
                }
            });
        }

        if (config.fitPdfPage === true) {
            editor.fitPdfPage();
        }

        window.status = config.initializerFinishStatus || '';
    });

    this.addDomInitializer('mailing-data-transfer-modal', function($e) {
        var $inputsPerParam = $e.find('input[data-mailing-param]');
        var $all = $e.find('#transferAllSettings');
        var checkedParamsMap = {};
        var checkedParams = [];

        $inputsPerParam.on('change', function() {
            var $checkbox = $(this);
            var param = $checkbox.data('mailing-param');

            if ($checkbox.prop('checked')) {
                checkedParamsMap[param] = true;
            } else {
                delete checkedParamsMap[param];
            }

            checkedParams = Object.keys(checkedParamsMap);

            $all.prop('checked', checkedParams.length == $inputsPerParam.length);
        });

        $all.on('change', function() {
            var isChecked = $all.prop('checked');

            $inputsPerParam.prop('checked', isChecked);

            if (isChecked) {
                $inputsPerParam.each(function() {
                    checkedParamsMap[$(this).data('mailing-param')] = true;
                });
                checkedParams = Object.keys(checkedParamsMap);
            } else {
                checkedParamsMap = {};
                checkedParams = [];
            }
        });

        this.addAction({click: 'transfer-mailing-data'}, function() {
            Confirm.get($e).positive(checkedParams);
        });
    });

    this.addDomInitializer('own-workflow-expanding-modal', function($e) {
        var $select = $e.find('#workflow-select');
        var $radios = $e.find('input[name="copyContent"]');

        this.addAction({click: 'expand-own-workflow'}, function() {
            Confirm.get($e).positive({
                workflowId: $select.val(),
                copyContent: $radios.filter(':checked').val() == 'true'
            });
        });
    });

    this.addDomInitializer('icon-comment-editor-initializer', function() {
        var nodeEditor = EditorsHelper.registerEditor('icon-comment', {
            formName: 'iconCommentForm',
            safeToSave: true,

            fillEditor: function(node) {
                $('#iconComment').val(node.getComment());
            },

            saveEditor: function() {
                EditorsHelper.saveIconComment($('#iconComment').val());
                this.cancelEditor();
            },

            cancelEditor: function() {
                $('#icon-comment-editor').dialog('close');
                return false;
            }
        });

        controller.addAction({click: 'icon-comment-editor-save'}, function() {
            nodeEditor.saveEditor();
        });

        controller.addAction({click: 'icon-comment-editor-cancel'}, function() {
            nodeEditor.cancelEditor();
        });
    });

    this.addDomInitializer('parameter-editor-initializer', function() {
        EditorsHelper.registerEditor('parameter', {
            formName: 'parameterForm',

            getTitle: function() {
                return t('workflow.parameter');
            },

            fillEditor: function(node) {
                var $form = $('form[name="' + this.formName + '"]');
                var data = node.getData();

                $form.submit(false);
                $form.get(0).reset();

                EditorsHelper.fillFormFromObject(this.formName, data, '');
            },

            saveEditor: function() {
                return EditorsHelper.formToObject(this.formName);
            }
        });

        controller.addAction({click: 'parameter-editor-save'}, function() {
            EditorsHelper.saveCurrentEditorWithUndo();
        });
    });

    this.addDomInitializer('deadline-editor-initializer', function() {
        var nodeEditor = EditorsHelper.registerEditor('deadline', {
            panelIds: ['fixedDeadlinePanel', 'delayDeadlinePanel'],
            timeUnitIds: [
                'deadlineTimeUnitMinute',
                'deadlineTimeUnitHour',
                'deadlineTimeUnitDay',
                'deadlineTimeUnitWeek',
                'deadlineTimeUnitMonth'
            ],
            formName: 'deadlineForm',

            getDefaultDelayData: function(deadlineType) {
                var defaultData = {};
                if (deadlineType === Def.constants.deadlineTypeDelay && this.isPrecededByImportIcon) {
                    defaultData = {
                        timeUnit: Def.constants.deadlineTimeUnitHour,
                        delayValue: Def.constants.defaultImportDelayLimit
                    };
                }
                return defaultData;
            },

            getTitle: function() {
                return t('workflow.deadline.title');
            },

            fillEditor: function(node) {
                var data = node.getData();

                $('#deadline-editor').find('.editor-error-messages').css('display', 'none');
                this.isPrecededByImportIcon = this.checkPrecededByImportIcon(node);

                var $form = $('form[name="' + this.formName + '"]');

                $form.submit(false);
                $('form[name="' + this.formName + '"] #deadlineDate').pickadate('picker').set('select', this.dateAsUTC(data.date || new Date()));

                this.fillDelayData($form, data);

                $('form[name="' + this.formName + '"] #time').val(('0' + data.hour).slice(-2) + ':' + ('0' + data.minute).slice(-2));
            },

            fillDelayData: function($form, data) {
                $form.get(0).reset();
                var type = $('form[name="' + this.formName + '"] input[name="deadlineType"]:checked').val();
                var defaultValues = this.getDefaultDelayData(type);

                EditorsHelper.fillFormFromObject(this.formName, data, '', defaultValues);
                this.updateVisibility();

                var delayValue = data.delayValue || defaultValues.delayValue;
                EditorsHelper.initSelectWithValueOrChooseFirst($('form[name="' + this.formName + '"] select[name="delayValue"]'), delayValue);
            },

            saveEditor: function() {
                var data = EditorsHelper.formToObject(this.formName);
                var time = $('form[name="' + this.formName + '"] #time').val();

                data.date = $('form[name="' + this.formName + '"] #deadlineDate').pickadate('picker').get('select').obj;
                data.hour = time.substring(0, 2);
                data.minute = time.substring(3, 5);

                return data;
            },

            // functions handling visibility of different parts of dialog according to selected settings
            updateVisibility: function() {
                var $form = $('form[name="' + this.formName + '"]');
                var type = $form.find('input[name="deadlineType"]:checked').val();

                if (this.checkPresentNodeOfType([Def.NODE_TYPE_ACTION_BASED_MAILING, Def.NODE_TYPE_DATE_BASED_MAILING])) {
                    $form.find('input#typeFixedDeadline').parent().parent().parent().hide();
                    $form.find('input[value="' + Def.constants.deadlineTypeDelay + '"]').prop('checked', true);
                    type = Def.constants.deadlineTypeDelay;
                } else {
                    $form.find('input#typeFixedDeadline').parent().parent().parent().show();
                }

                if (type === Def.constants.deadlineTypeDelay) {
                    this.switchPanelsVisibility('delayDeadlinePanel');
                    this.switchTimeContainerVisibility(false);

                    var timeUnit = $form.find('select[name="timeUnit"]').val();
                    switch (timeUnit) {
                        case Def.constants.deadlineTimeUnitMinute:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitMinute');
                            break;

                        case Def.constants.deadlineTimeUnitHour:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitHour');
                            break;

                        case Def.constants.deadlineTimeUnitDay:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitDay');
                            this.switchTimeContainerVisibility(true);
                            $('#deadlineTimeHelp').removeAttr('style');
                            break;

                        case Def.constants.deadlineTimeUnitWeek:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitWeek');
                            break;

                        case Def.constants.deadlineTimeUnitMonth:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitMonth');
                            break;
                    }
                    $form.find('input[name="useTime"]').css('display', 'inline');
                } else if (type === Def.constants.deadlineTypeFixedDeadline) {
                    this.switchPanelsVisibility('fixedDeadlinePanel');
                    this.switchTimeContainerVisibility(true);
                    this.hideDiv('deadlineTimeHelp');
                    $form.find('input[name="useTime"]').css('display', 'none');
                }
            },

            switchPanelsVisibility: function(selectedPanelId) {
                var self = this;
                this.panelIds.forEach(function(itemId) {
                    if (itemId !== selectedPanelId) {
                        self.hideDiv(itemId);
                    }
                });
                this.showDiv(selectedPanelId);
            },

            switchTimeUnitsVisibility: function(selectedTimeUnitId) {
                var self = this;
                this.timeUnitIds.forEach(function(itemId) {
                    if (itemId !== selectedTimeUnitId) {
                        self.hideDiv(itemId);
                        $('#' + itemId + ' select').removeAttr('name');
                    }
                });
                this.showDiv(selectedTimeUnitId);
                $('#' + selectedTimeUnitId + ' select').attr('name', 'delayValue');
            },

            switchTimeContainerVisibility: function(isVisible) {
                var self = this;
                if (isVisible) {
                    self.showDiv('deadlineTimeContainer');
                } else {
                    self.hideDiv('deadlineTimeContainer');
                }
            },

            hideAllItems: function(itemIds) {
                var self = this;
                itemIds.forEach(function(itemId) {
                    self.hideDiv(itemId);
                });
            },

            validateEditor: function() {
                var valid = true;
                var hourLimit = Def.constants.defaultImportDelayLimit;

                if (this.isPrecededByImportIcon) {
                    var $form = $('form[name="' + this.formName + '"]');
                    var type = $form.find('input[name="deadlineType"]:checked').val();

                    if (type === Def.constants.deadlineTypeDelay) {
                        var timeUnit = $form.find('select[name="timeUnit"]').val();

                        if (timeUnit === Def.constants.deadlineTimeUnitMinute) {
                            valid = false;
                        } else if (timeUnit === Def.constants.deadlineTimeUnitHour) {
                            if ($('#deadlineTimeUnitHour select').val() < hourLimit) {
                                valid = false;
                            }
                        }
                    }
                }

                if (valid) {
                    EditorsHelper.saveCurrentEditorWithUndo();
                } else {
                    var $message = $('#deadline-editor').find('.editor-error-messages');
                    $message.html(t('error.workflow.deadlineIsTooShortForImport', hourLimit));
                    $message.css('display', 'block');
                }
            },

            onTimeChanged: function() {
                var $form = $('form[name="' + this.formName + '"]');
                var type = $form.find('input[name="deadlineType"]:checked').val();
                var timeUnit = $form.find('select[name="timeUnit"]').val();

                if (type == Def.constants.deadlineTypeDelay && timeUnit == Def.constants.deadlineTimeUnitDay) {
                    $form.find('input[name="useTime"]').prop('checked', true);
                }
            },

            hideDiv: function(id) {
                $('form[name="' + this.formName + '"] #' + id).css('display', 'none');
            },

            showDiv: function(id) {
                $('form[name="' + this.formName + '"] #' + id).css('display', 'block');
            },

            // Date picker uses a date in UTC but JS uses a local timezone so we need to workaround that
            dateAsUTC: function(date) {
                if (date) {
                    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
                } else {
                    return null;
                }
            },

            checkPresentNodeOfType: function(types) {
                return EditorsHelper.getNodesByTypes(types).length > 0;
            },

            checkPrecededByImportIcon: function(node) {
                return EditorsHelper.getNodesByIncomingConnections(node)
                    .some(function(n) {
                        return Def.NODE_TYPE_IMPORT == n.getType();
                    });
            }
        });

        controller.addAction({'change': 'deadline-editor-update'}, function() {
            nodeEditor.updateVisibility();
        });

        controller.addAction({'change': 'deadline-editor-time-change'}, function() {
            nodeEditor.onTimeChanged();
        });

        controller.addAction({click: 'deadline-editor-save'}, function() {
            nodeEditor.validateEditor();
        });
    });

    this.addDomInitializer('archive-editor-initializer', function() {
        var nodeEditor = EditorsHelper.registerEditor('archive', {
            formName: 'archiveForm',
            safeToSave: true,

            getTitle: function() {
                return t('workflow.mailing.archive');
            },

            createNewArchive: function() {
                EditorsHelper.processForward(Def.constants.forwardArchiveCreate, '#settings_general_campaign', submitWorkflowForm);
            },

            fillEditor: function(node) {
                var $form = $('form[name="' + this.formName + '"]');
                var data = node.getData();

                $form.submit(false);
                $form.get(0).reset();

                EditorsHelper.fillFormFromObject(this.formName, data, '');
            },

            saveEditor: function() {
                return EditorsHelper.formToObject(this.formName);
            }
        });

        controller.addAction({click: 'archive-editor-new'}, function() {
            nodeEditor.createNewArchive();
        });
    });

    this.addDomInitializer('report-editor-initializer', function() {
        var nodeEditor = EditorsHelper.registerEditor('report', {
            safeToSave: true,

            getTitle: function() {
                return t('report');
            },

            createNewReport: function() {
                EditorsHelper.processForward(Def.constants.forwardReportCreate, '#reportSelector', submitWorkflowForm);
            },

            editReport: function(reportId) {
                $('#forwardTargetItemId').val(reportId);
                EditorsHelper.processForward(Def.constants.forwardReportEdit, '#reportSelector', submitWorkflowForm);
            },

            fillEditor: function(node) {
                var data = node.getData();
                var $select = $('#reportSelect');

                //set option selected by default
                EditorsHelper.initSelectWithFirstValue($select);

                $('#report-editor .editor-error-messages').css('display', 'none');
                $select.select2('val', data.reports);

                $select.select2({
                    formatSelection: function(report) {
                        return '<a href="#" class="btn-link-light" data-action="report-editor-change" data-report-id="' + report.id + '">' + report.text + '</a>';
                    }
                });
            },

            validateEditor: function() {
                var selectedReports = $('#reportSelect').val();
                if (selectedReports && selectedReports.length > 0) {
                    EditorsHelper.saveCurrentEditorWithUndo();
                } else {
                    var $message = $('#report-editor').find('.editor-error-messages');
                    $message.html(t('workflow.report.error.no_report'));
                    $message.css('display', 'block');
                }
            },

            saveEditor: function() {
                return {
                    reports: $('#reportSelect').val()
                };
            },

            isSetFilledAllowed: function() {
                var selectedReports = $('#reportSelect').val();
                return selectedReports && selectedReports.length > 0;
            }
        });

        controller.addAction({click: 'report-editor-change'}, function() {
            nodeEditor.editReport(this.el.data('report-id'));
        });

        controller.addAction({click: 'report-editor-new'}, function() {
            nodeEditor.createNewReport();
        });

        controller.addAction({click: 'report-editor-save'}, function() {
            nodeEditor.validateEditor();
        });
    });

    this.addDomInitializer('recipient-editor-initializer', function() {
        var isAltgExtended = Def.constants.isAltgExtended;
        var nodeEditor = EditorsHelper.registerEditor('recipient', {
            formName: 'recipientForm',
            targetSelector: '#recipientTargetSelect',
            altgSelector: '#recipientAltgSelect',
            safeToSave: true,

            getTargetFieldSelect: function() {
                return new Select(
                    $(this.targetSelector).select2({
                        formatSelection: function(target) {
                            if ($(target.element).data('editable')) {
                                return '<a href="#" class="btn-link-light" ' +
                                    'data-action="recipient-editor-target-edit" data-target-id="' + target.id + '">' +
                                    target.text +
                                    '</a>';
                            } else {
                                return target.text;
                            }
                        }
                    })
                );
            },
            getAltgFieldSelect: function() {
                return new Select(
                    $(this.altgSelector).select2({
                        formatSelection: function(target) { return target.text; }
                    })
                );
            },

            createNewTarget: function() {
                EditorsHelper.processForward(Def.constants.forwardTargetGroupCreate, this.targetSelector, submitWorkflowForm);
            },

            editTarget: function(targetId) {
                $('#forwardTargetItemId').val(targetId);
                EditorsHelper.processForward(Def.constants.forwardTargetGroupEdit, this.targetSelector, submitWorkflowForm);
            },

            getTitle: function() {
                return t('workflow.recipient');
            },

            fillEditor: function(node) {
                var data = node.getData();
                if (!isAltgExtended) {
                  if (Def.constants.accessLimitTargetId > 0) {
                      data.targets = _.union([Def.constants.accessLimitTargetId], data.targets);
                      data.targetsOption = 'ALL_TARGETS_REQUIRED';
                  }
                }

                var $form = $('form[name="' + this.formName + '"]');
                $form.submit(false);
                $form.get(0).reset();
                EditorsHelper.fillFormFromObject(this.formName, data, '');

                this.getTargetFieldSelect().selectValue(data.targets);
                if (isAltgExtended) {
                  this.getAltgFieldSelect().selectValue(data.altgs);
                }
            },

            saveEditor: function() {
                var data = EditorsHelper.formToObject(this.formName);
                data.targets = this.getTargetFieldSelect().getSelectedValue();
                if (isAltgExtended) {
                  data.altgs = this.getAltgFieldSelect().getSelectedValue();
                }
                return data;
            }
        });

        controller.addAction({click: 'recipient-editor-create-new-target'}, function() {
            nodeEditor.createNewTarget();
        });

        controller.addAction({click: 'recipient-editor-save'}, function() {
            if (isAltgExtended) {
              var form = AGN.Lib.Form.get($('#' + nodeEditor.formName));
              if (form.valid({})) {
                form.cleanErrors();
                EditorsHelper.saveCurrentEditorWithUndo();
              } else {
                form.handleErrors();
              }
            } else {
              EditorsHelper.saveCurrentEditorWithUndo();
            }
        });

        controller.addAction({click: 'recipient-editor-target-edit'}, function() {
            nodeEditor.editTarget(this.el.data('target-id'));
        });
    });

    this.addDomInitializer('export-editor-initializer', function() {
        var exportData = this.config;

        var nodeEditor = EditorsHelper.registerEditor('export', {
            formName: 'exportForm',
            safeToSave: true,

            getTitle: function() {
                return t('auto_export');
            },

            createNewAutoExport: function() {
                EditorsHelper.processForward(Def.constants.forwardAutoExportCreate, 'form[name="' + this.formName + '"] select[name=importexportId]', submitWorkflowForm);
            },

            editAutoExport: function() {
                var autoExportSelector = $('form[name="' + this.formName + '"] select[name=importexportId]');
                var autoExportIdVal = autoExportSelector.val();
                $('#forwardTargetItemId').val(autoExportIdVal);
                EditorsHelper.processForward(Def.constants.forwardAutoExportEdit, 'form[name="' + this.formName + '"] select[name=importexportId]', submitWorkflowForm);
            },

            fillEditor: function(node) {
                var $messages = $('#export-editor .editor-error-messages');
                var $form = $('form[name="' + this.formName + '"]');
                var data = node.getData();

                $form.submit(false);
                $form.get(0).reset();
                EditorsHelper.fillFormFromObject(this.formName, data, '');
                this.addLinks();

                if (exportData.isDisabled == 'true') {
                    $messages.html(t('error.workflow.autoExportPermission'));
                    $messages.css('display', 'block');
                } else {
                    $messages.css('display', 'none');
                }
            },

            validateEditor: function() {
                var $messages = $('#export-editor .editor-error-messages');
                var autoExportSelector = $('form[name="' + this.formName + '"] select[name=importexportId]');
                if (autoExportSelector.val() > 0) {
                    $.ajax({
                        type: 'POST',
                        url: AGN.url('/workflow/validateDependency.action'),
                        data: {
                            workflowId: Def.workflowId || 0,
                            type: Def.DEPENDENCY_TYPE_AUTO_EXPORT,
                            entityId: autoExportSelector.val()
                        }
                    }).done(function(data) {
                        if (data.valid === true) {
                            EditorsHelper.saveCurrentEditorWithUndo();
                        } else {
                            $messages.html(t('error.workflow.autoExportInUse'));
                            $messages.css('display', 'block');
                        }
                    }).fail(function() {
                        $messages.html(t('Error'));
                        $messages.css('display', 'block');
                    });
                } else {
                    $messages.html(t('error.workflow.noExport'));
                    $messages.css('display', 'block');
                }
            },

            onChange: function() {
                this.addLinks();
            },

            addLinks: function() {
                var formSelector = $('form[name="' + this.formName + '"] select[name=importexportId]');
                var formIdVal = formSelector.val();
                if (formIdVal > 0) {
                    $('#export-editor #editAutoExportLink').css('display', 'block');
                    $('#export-editor #createAutoExportLink').css('display', 'none');
                } else {
                    $('#export-editor #createAutoExportLink').css('display', 'block');
                    $('#export-editor #editAutoExportLink').css('display', 'none');
                }
            },

            saveEditor: function() {
                return EditorsHelper.formToObject(this.formName);
            },

            isSetFilledAllowed: function() {
                var $select = $('form[name="' + this.formName + '"] select[name=importexportId]');
                var $option = $select.find(':selected');

                return !($select.val() == 0 || $option.data('is-available') != true);
            }
        });

        controller.addAction({change: 'export-editor-change'}, function() {
            nodeEditor.onChange();
        });

        controller.addAction({click: 'export-editor-new'}, function() {
            nodeEditor.createNewAutoExport();
        });

        controller.addAction({click: 'export-editor-autoexport'}, function() {
            nodeEditor.editAutoExport();
        });

        controller.addAction({click: 'export-editor-save'}, function() {
            nodeEditor.validateEditor();
        });
    });

    this.addDomInitializer('import-editor-initializer', function() {
        var importData = this.config;

        var nodeEditor = EditorsHelper.registerEditor('import', {
            formName: 'importForm',
            safeToSave: true,

            getTitle: function() {
                return t('auto_import');
            },

            createNewAutoImport: function() {
                EditorsHelper.processForward(Def.constants.forwardAutoImportCreate, '#importexportId', submitWorkflowForm);
            },

            editAutoImport: function() {
                var autoImportSelector = $('form[name="' + this.formName + '"] select[name=importexportId]');
                var autoImportIdVal = autoImportSelector.val();
                $('#forwardTargetItemId').val(autoImportIdVal);
                EditorsHelper.processForward(Def.constants.forwardAutoImportEdit, 'form[name="' + this.formName + '"] select[name=importexportId]', submitWorkflowForm);
            },

            fillEditor: function(node) {
                var $messages = $('#import-editor .editor-error-messages');
                var $form = $('form[name="' + this.formName + '"]');
                var data = node.getData();

                $form.submit(false);
                $form.get(0).reset();
                EditorsHelper.fillFormFromObject(this.formName, data, '');
                this.addLinks();

                if (importData.isDisabled == 'true') {
                    $messages.html(t('error.workflow.autoImportPermission'));
                    $messages.css('display', 'block');
                } else {
                    $messages.css('display', 'none');
                }
            },

            validateEditor: function() {
                var $messages = $('#import-editor .editor-error-messages');
                var autoImportSelector = $('form[name="' + this.formName + '"] select[name=importexportId]');
                if (autoImportSelector.val() > 0) {
                    $.ajax({
                        type: 'POST',
                        url: AGN.url('/workflow/validateDependency.action'),
                        data: {
                            workflowId: Def.workflowId || 0,
                            type: Def.DEPENDENCY_TYPE_AUTO_IMPORT,
                            entityId: autoImportSelector.val()
                        }
                    }).done(function(data) {
                        if (data.valid === true) {
                            EditorsHelper.saveCurrentEditorWithUndo();
                        } else {
                            $messages.html(t('error.workflow.autoImportInUse'));
                            $messages.css('display', 'block');
                        }
                    }).fail(function() {
                        $messages.html(t('Error'));
                        $messages.css('display', 'block');
                    });
                } else {
                    $messages.html(t('error.workflow.noImport'));
                    $messages.css('display', 'block');
                }
            },

            onChange: function() {
                this.addLinks();
            },

            addLinks: function() {
                var formSelector = $('form[name="' + this.formName + '"] select[name=importexportId]');
                var formIdVal = formSelector.val();
                if (formIdVal > 0) {
                    $('#import-editor #editAutoImportLink').css('display', 'block');
                    $('#import-editor #createAutoImportLink').css('display', 'none');
                } else {
                    $('#import-editor #createAutoImportLink').css('display', 'block');
                    $('#import-editor #editAutoImportLink').css('display', 'none');
                }
            },

            saveEditor: function() {
                return EditorsHelper.formToObject(this.formName);
            },

            isSetFilledAllowed: function() {
                var $select = $('form[name="' + this.formName + '"] select[name=importexportId]');
                var $option = $select.find(':selected');

                return !($select.val() == 0 || $option.data('is-available') != true);
            }
        });

        controller.addAction({change: 'import-editor-change'}, function() {
            nodeEditor.onChange();
        });

        controller.addAction({click: 'import-editor-new'}, function() {
            nodeEditor.createNewAutoImport();
        });

        controller.addAction({click: 'import-editor-update'}, function() {
            nodeEditor.editAutoImport();
        });

        controller.addAction({click: 'import-editor-save'}, function() {
            nodeEditor.validateEditor();
        });
    });

    this.addDomInitializer('start-editor-initializer', function() {
        var startData = this.config;
        var startMailingSelector = new MailingSelector(startData.form, startData.container, startData.selectedName, startData.noMailingOption);

        var startProfileFieldsTypes = startData.profileFields;
        var isBigData = startData.isBigData;

        var nodeEditor = {
            rulesNumber: 0,
            isStartEditor: false,
            timePattern: /^(\d{2}):(\d{2})$/,

            getTitle: function() {
                return this.isStartEditor ? t('workflow.start.title') : t('workflow.stop.title');
            },

            getStartStopDate: function(data) {
                data = data || EditorsHelper.formToObject('startForm');

                var $form = $('form[name="startForm"]');

                var picker = null;
                if (this.isStartEditor) {
                    switch (data.startType) {
                        case Def.constants.startTypeDate:
                            picker = $form.find('#startDate').pickadate('picker');
                            break;
                        case Def.constants.startTypeEvent:
                            picker = $form.find('#executionDate').pickadate('picker');
                            break;
                    }
                } else if (data.endType == Def.constants.endTypeDate) {
                    picker = $form.find('#startDate').pickadate('picker');
                }

                if (picker) {
                    var select = picker.get('select');
                    if (select) {
                        return select.obj;
                    }
                }

                return null;
            },

            generateReminderComment: function() {
                var name = $('#workflowForm input[name="workflow.shortname"]').val();
                var dateString = DateTimeUtils.getDateStr(this.getStartStopDate(), startData.localeDatePattern);

                return (this.isStartEditor ? t('workflow.start.reminder_text') : t('workflow.stop.reminder_text'))
                    .replace(/:campaignName/g, name)
                    .replace(/:startDate/g, dateString)
                    .replace(/:endDate/g, dateString);
            },

            fillEditor: function(node) {
                var data = node.getData();
                var nodeType = node.getType();

                this.isStartEditor = (nodeType == 'start');
                this.updateEditorType(this.isStartEditor);

                var editorForm = $('form[name="startForm"]');

                editorForm.submit(false);
                editorForm.find('.rule-row').each(function() {
                    $(this).remove();
                });
                editorForm.find('#remindCalendarDateTitle')
                    .text(this.isStartEditor ? t('workflow.start.start_date') : t('workflow.stop.end_date'));

                if (data.rules != undefined) {
                    for (var i = 0; i < data.rules.length; i++) {
                        var rule = data.rules[i];
                        this.createRuleRow(i, rule);
                    }
                    this.rulesNumber = data.rules.length;
                }
                editorForm.get(0).reset();

                //this time zone will be used during sending reminders
                data.adminTimezone = startData.adminTimezone;
                data.sendReminder = data.scheduleReminder;

                EditorsHelper.fillFormFromObject('startForm', data, '');
                startMailingSelector.setMailingId(data.mailingId);
                startMailingSelector.cleanOptions(1);

                //init datepickers
                var currentDate = new Date();
                editorForm.find('#startDate').pickadate('picker').set('select', this.dateAsUTC(data.date != undefined ? data.date : currentDate));
                editorForm.find('#executionDate').pickadate('picker').set('select', this.dateAsUTC(data.date != undefined ? data.date : currentDate));
                editorForm.find('#remindDate').pickadate('picker').set('select', this.dateAsUTC(data.remindDate != undefined ? data.remindDate : currentDate));

                //init timepickers
                if (data.date != undefined) {
                    editorForm.find('#startTime').val(this.formatTime(data.hour, data.minute));
                    editorForm.find('#remindTime').val(this.formatTime(data.remindHour, data.remindMinute));
                } else {
                    this.setCurrentTime();
                }

                this.updateChainFieldsVisibility();
                this.updateVisibility();
                this.updateOperatorsAvailability();

                // update mailing links in editor
                if (data.startType == Def.constants.startTypeEvent && data.event == Def.constants.startEventReaction && data.reaction == Def.constants.reactionClickedLink
                    && data.mailingId != 0 && data.mailingId != null) {
                    this.onMailingSelectChange(data.mailingId, data.linkId);
                }

                // init reminder comment
                if (!data.comment) {
                    editorForm.find('#reminderComment').val(this.generateReminderComment());
                }

                // init reminder user type
                var userType;
                if (data.recipients != '') {
                    userType = 2;
                } else {
                    userType = 1;
                }
                $('#start-editor input[name="userType"][value=' + userType + ']').prop('checked', true).trigger('change');

                $('#start-editor .editor-error-messages').css('display', 'none');
            },

            saveEditor: function() {
                var data = EditorsHelper.formToObject('startForm');

                data.date = this.getStartStopDate(data);

                if (data.date == null) {
                    data.date = new Date();
                    data.date.setHours(0);
                    data.date.setMinutes(0);
                    data.date.setSeconds(0);
                    data.date.setMilliseconds(0);
                }

                var match;

                var startTime = $('form[name="startForm"] #startTime').val();
                if (match = this.timePattern.exec(startTime)) {
                    data.hour = match[1];
                    data.minute = match[2];
                } else {
                    data.hour = 0;
                    data.minute = 0;
                }

                data.scheduleReminder = data.sendReminder;

                var remindTime = $('form[name="startForm"] #remindTime').val();
                if (match = this.timePattern.exec(remindTime)) {
                    data.remindHour = match[1];
                    data.remindMinute = match[2];
                } else {
                    data.remindHour = 0;
                    data.remindMinute = 0;
                }

                if ($('input[name="remindSpecificDate"]:checked').val() == 'true') {
                    data.remindDate = $('form[name="startForm"] #remindDate').pickadate('picker').get('select').obj;
                } else {
                    data.remindDate = data.date;
                    data.remindHour = 0;
                    data.remindMinute = 0;
                }

                if (data.rules == undefined) {
                    data.rules = [];
                }

                if (data.linkId == undefined || data.linkId == null) {
                    data.linkId = 0;
                }

                if (data.dateFieldOperator != startData.equelsOperatorCode) {
                    data.dateFieldOperator = startData.equelsOperatorCode;
                }

                if (data.userType == 2) {
                    data.remindAdminId = 0;
                    data.recipients = $.trim(data.recipients)
                        .toLowerCase()
                        .split(/[,;\s\n\r]+/)
                        .filter(function(address) {
                            return !!address;
                        })
                        .join(', ');
                } else {
                    data.recipients = '';
                }
                delete data.userType;

                return data;
            },

            validateEditorInternal: function(showErrors) {
                var self = this;
                var valid = true;

                var messageView = $('#start-editor .editor-error-messages');
                var editorForm = $('form[name="startForm"]');
                var type = this.isStartEditor ?
                    editorForm.find('input[name="startType"]:checked').val() :
                    editorForm.find('input[name="endType"]:checked').val();

                if (this.isStartEditor && type == Def.constants.startTypeEvent) {
                    validateStartEvent();
                    validateStartDate();
                }

                if ((this.isStartEditor && type == Def.constants.startTypeDate)
                    || (!this.isStartEditor && type == Def.constants.endTypeDate)) {
                    validateStartDate();
                }

                if (valid && editorForm.find('#sendReminder').is(':checked')) {
                    var userType = editorForm.find('input[name="userType"]:checked').val();
                    if (userType == 2) {
                        validateReminderRecipients();
                    }
                }

                return valid;

                function validateStartEvent() {
                    var event = editorForm.find('select[name="event"]').val();
                    switch (event) {
                        case Def.constants.startTypeDate:
                            var executionDate = editorForm.find('#executionDate').pickadate('picker').get('select').obj;
                            if (executionDate) {
                                var startTime = editorForm.find('#startTime').val();
                                if (!self.timePattern.test(startTime)) {
                                    valid = false;
                                }
                            } else {
                                valid = false;
                            }
                            var dateFieldValue = editorForm.find('#dateFieldValue');
                            if (!$.trim(dateFieldValue.val())) {
                                valid = false;
                                if (showErrors) {
                                    messageView.html(t('error.workflow.startDateOmitted'));
                                    messageView.css('display', 'block');
                                }
                            }
                            break;

                        case Def.constants.startEventReaction:
                            var reaction = editorForm.find('select[name="reaction"]').val();
                            switch (reaction) {
                                case Def.constants.reactionClickedLink:
                                    var linkId = editorForm.find('select[name="linkId"]').val();
                                    if (parseInt(linkId, 10) <= 0) {
                                        valid = false;
                                        if (showErrors) {
                                            messageView.html(t('error.workflow.noLinkSelected'));
                                            messageView.css('display', 'block');
                                        }
                                    }
                                // Fall-through
                                case Def.constants.reactionOpened:
                                case Def.constants.reactionClicked:
                                    var mailingId = editorForm.find('select[name="mailingId"]').val();
                                    if (parseInt(mailingId, 10) <= 0) {
                                        valid = false;
                                    }
                                    break;
                                case Def.constants.reactionChangeOfProfile:
                                    if (typeof (isBigData) != 'undefined' && isBigData == true) {
                                        var profileField = editorForm.find('select[name="profileField"]').val();
                                        if (!profileField) {
                                            valid = false;
                                        }
                                    }
                                    break;

                                case Def.constants.reactionWaitingForConfirm:
                                case Def.constants.reactionOptIn:
                                case Def.constants.reactionOptOut:
                                    // Nothing to validate
                                    break;

                                default:
                                    // Unknown reaction
                                    valid = false;
                                    break;
                            }
                            break;
                    }
                }

                function validateStartDate() {
                    var startDate = editorForm.find('#startDate').pickadate('picker').get('select').obj;
                    if (startDate) {
                        var startTime = editorForm.find('#startTime').val();
                        if (!self.timePattern.test(startTime)) {
                            valid = false;
                        }
                    } else {
                        valid = false;
                    }
                }

                function validateReminderRecipients() {
                    var emails = $.trim(editorForm.find('textarea[name="recipients"]').val())
                        .split(/[,;\s\n\r]+/)
                        .filter(function(address) {
                            return !!address
                        });

                    if (!emails.length) {
                        if (showErrors) {
                            valid = false;
                            messageView.html(t('error.workflow.emptyRecipientList'));
                            messageView.css('display', 'block');
                        }
                    }
                }
            },

            validateEditor: function() {
                if (this.validateEditorInternal(true)) {
                    EditorsHelper.saveCurrentEditorWithUndo();
                }
            },

            isSetFilledAllowed: function() {
                return this.validateEditorInternal(false);
            },

            setCurrentTime: function() {
                var self = this;
                $.ajax({
                    type: 'GET',
                    url: AGN.url('/workflow/getCurrentAdminTime.action'),
                    success: function(data) {
                        $('form[name="startForm"] #startTime').val(self.formatTime(data.hour, data.minute));
                        $('form[name="startForm"] #remindTime').val(self.formatTime(data.remindHour, data.remindMinute));
                    }
                });
            },

            updateEditorType: function(isStartEditor) {
                var $type = $('#startStopType');

                $type.empty();

                if (isStartEditor) {
                    $type.append(Template.text('start-types'));
                    $('#eventReaction').html(t('workflow.start.reaction_based'));
                    $('#eventDate').html(t('workflow.start.date_based'));
                } else {
                    $type.append(Template.text('stop-types'));
                    if (this.checkPresentNodeOfType([Def.NODE_TYPE_ACTION_BASED_MAILING, Def.NODE_TYPE_DATE_BASED_MAILING])) {
                        $('#endTypeActiomaticLabel').html(t('workflow.stop.open_end'));
                    } else {
                        $('#endTypeActiomaticLabel').html(t('workflow.stop.automatic_end'));
                    }
                }
            },

            onRulesChanged: function() {
                var ruleIndex = 0;
                $('form[name="startForm"] #profileFieldAddedRules tr').each(function() {
                    var $this = $(this);

                    var ruleId = $this.attr('id');
                    var oldIndex = ruleId.substring(5);
                    var $rule = $('#' + ruleId);

                    $rule.find('.rule-field').attr('name', function(i, name) {
                        return name ? name.replace(oldIndex, ruleIndex + '') : '';
                    });

                    $rule.find('.delete-rule').attr('data-rule-index', function(i, value) {
                        return value == oldIndex ? ruleIndex : value;
                    });

                    $this.attr('id', 'rule_' + ruleIndex);
                    ruleIndex++;
                });
                this.updateChainFieldsVisibility();
            },

            updateChainFieldsVisibility: function() {
                if (this.rulesNumber > 0) {
                    $('form[name="startForm"] [name="rules[0].chainOperator"]').css('visibility', 'hidden');
                    $('form[name="startForm"] #newRule_chainOperator').css('visibility', 'visible');
                } else {
                    $('form[name="startForm"] #newRule_chainOperator').css('visibility', 'hidden');
                }
            },

            updateOperatorsAvailability: function() {
                var profileField = $('form[name="startForm"] select[name="profileField"]').val();
                var fieldType = startProfileFieldsTypes[profileField];

                $('form[name="startForm"] select.primary-operator').each(function() {
                    var $select = $(this);

                    $select.children('option').prop('disabled', function() {
                        var types = $(this).data('types');

                        if (types == '*' || !types) {
                            return false;
                        }

                        return types.split(/[\s,]+/).indexOf(fieldType) == -1;
                    });

                    AGN.Lib.CoreInitializer.run('select', $select);

                    if ($select.val() == null) {
                        EditorsHelper.initSelectWithFirstValue($select);
                    }
                });
            },

            addRule: function() {
                var $form = $('form[name="startForm"]');
                var rule = {
                    chainOperator: $form.find('#newRule_chainOperator').val(),
                    parenthesisOpened: $form.find('#newRule_parenthesisOpened').val(),
                    primaryOperator: $form.find('#newRule_primaryOperator').val(),
                    primaryValue: $form.find('#newRule_primaryValue').val(),
                    parenthesisClosed: $form.find('#newRule_parenthesisClosed').val()
                };
                this.createRuleRow(this.rulesNumber, rule);
                EditorsHelper.fillFormFromObject('startForm', rule, 'rules[' + this.rulesNumber + '].');
                this.rulesNumber++;

                if ($('#newRule_primaryOperator').val() == Def.constants.operatorIs) {
                    $('#newRule_primaryValue').parents('td').replaceWith(this.createSimpleNewRuleValueField());
                }
                this.updateChainFieldsVisibility();
                this.updateOperatorsAvailability();
                $form.find('#profileFieldRuleAdd select').each(function() {
                    EditorsHelper.initSelectWithFirstValue($(this));
                });
                $form.find('#profileFieldRuleAdd input').each(function() {
                    $(this).val('');
                });
            },

            removeRuleRow: function(index) {
                $('form[name="startForm"] #rule_' + index).remove();
                this.rulesNumber--;
                this.onRulesChanged();
            },

            createRuleRow: function(ruleIndex, rule) {
                var $rules = $('form[name="startForm"] #profileFieldAddedRules');
                var valueElement = this.createSimpleRuleValueField(ruleIndex);
                var options = Utils.arrayAsOptionsHtml(Def.constants.operators);
                if (this.curProfileField && this.curProfileField.toLowerCase() == Def.GENDER_PROFILE_FIELD) {
                    valueElement = this.createGenderSelect();
                } else if (rule.primaryOperator == Def.constants.operatorIs) {
                    valueElement = this.createIsOperatorSelect();
                }
                $rules.append(
                    '<tr class="rule-row" id="rule_' + ruleIndex + '">' +
                    '<td><select name="rules[' + ruleIndex + '].chainOperator" class="rule-field">' +
                    '<option value="' + Def.constants.chainOperatorAnd + '">' + t('workflow.defaults.and') + '</option>' +
                    '<option value="' + Def.constants.chainOperatorOr + '">' + t('workflow.defaults.or') + '</option>' +
                    '</select></td>' +
                    '<td><select name="rules[' + ruleIndex + '].parenthesisOpened" class="parentheses-opened rule-field">' +
                    '<option value="0">&nbsp</option>' +
                    '<option value="1">(</option>' +
                    '</select></td>' +
                    '<td><select name="rules[' + ruleIndex + '].primaryOperator" class="primary-operator rule-field" data-action="start-rule-operator-change">' + options +
                    '</select></td>' +
                    valueElement +
                    '<td><select name="rules[' + ruleIndex + '].parenthesisClosed" class="parenthesis-closed rule-field">' +
                    '<option value="0">&nbsp</option>' +
                    '<option value="1">)</option>' +
                    '</select></td>' +
                    '<td><a class="mailing_delete delete-rule btn btn-regular btn-alert" href="#" data-action="start-editor-rule-remove" data-rule-index="' + ruleIndex + '"><i class="icon icon-minus-circle"></i></a></td>' +
                    '</tr>');
                this.addRuleProperties($('#rule_' + ruleIndex + ' .primary-value'), ruleIndex);

                AGN.runAll($rules);
            },

            // functions handling visibility of different parts of dialog according to selected settings

            onStartTypeChanged: function() {
                var value = this.isStartEditor ?
                    $('form[name="startForm"] input[name="startType"]:checked').val() :
                    $('form[name="startForm"] input[name="endType"]:checked').val();
                if ((this.isStartEditor && value == Def.constants.startTypeDate)
                    || (!this.isStartEditor && value == Def.constants.endTypeDate)) {
                    this.toggleFormat();
                    this.hideDiv('startEventPanel');
                    this.showDiv('startDatePanel');
                    this.showDiv('startIconTime');
                    this.showDiv('startRemindAdmin');
                } else if (this.isStartEditor && value == Def.constants.startTypeEvent) {
                    this.hideDiv('startDatePanel');
                    this.showDiv('startEventPanel');
                    this.showDiv('startIconTime');
                    this.showDiv('startRemindAdmin');
                    this.onStartEventChanged();
                } else if ((this.isStartEditor && value == Def.constants.startTypeOpen)
                    || (!this.isStartEditor && value == Def.constants.endTypeAutomatic)) {
                    this.hideDiv('startDatePanel');
                    this.hideDiv('startEventPanel');
                    this.hideDiv('startIconTime');
                    this.hideDiv('startRemindAdmin');
                }
            },

            onStartEventChanged: function() {
                var event = $('#startEvent').val();

                if (event == Def.constants.startEventReaction) {
                    this.toggleFormat();
                    this.hideDiv('dateStartPanel');
                    this.showDiv('reactionStartPanel');
                    this.onExecutionChanged();
                    this.onProfileFieldChanged();
                } else if (event == Def.constants.startEventDate) {
                    this.toggleFormat();
                    this.hideDiv('reactionStartPanel');
                    this.showDiv('dateStartPanel');
                    this.hideDiv('executionDateLabel');
                    this.showDiv('firstExecutionDateLabel');
                    $('#startIconDateFormat').insertAfter('#startIconDateFieldOperator');
                    this.showDiv('startIconDateFormat');
                }
            },

            toggleFormat: function() {
                var timeInput, typeDateInput,
                    startEventInput, checked, isDateBased;
                return function() {
                    startEventInput = startEventInput || $('select#startEvent');
                    typeDateInput = typeDateInput || $('input#typeEvent');
                    timeInput = timeInput || $('input#startTime');
                    checked = typeDateInput.prop('checked');
                    isDateBased = startEventInput.val() == Def.constants.startEventDate;
                    (typeDateInput.prop('checked') && isDateBased) ? timeInput.inputmask('h:00') : timeInput.inputmask('h:s');
                }
            }(),

            onRuleModeChanged: function() {
                var value = $('input[name="useRules"]:checked').val();
                if (value == 'false') {
                    this.hideDiv('profileFieldRules');
                } else {
                    this.showDiv('profileFieldRules');
                }
            },

            onProfileFieldChanged: function() {
                var profileField = $('form[name="startForm"] select[name="profileField"]').val();
                var fieldType = startProfileFieldsTypes[profileField];
                if (fieldType && fieldType.toLowerCase() == Def.FIELD_TYPE_DATE) {
                    $('#startIconDateFormat').insertAfter('#reactionProfileField');
                    this.showDiv('startIconDateFormat');
                } else {
                    this.hideDiv('startIconDateFormat');
                }
                this.updateOperatorsAvailability();
            },

            getRuleIndex: function($element) {
                return $element.attr('id').substring(5);
            },

            onRuleOperatorChanged: function($element) {
                if (this.curProfileField && this.curProfileField.toLowerCase() == Def.GENDER_PROFILE_FIELD) {
                    return;
                }

                var $parent = $element.closest('tr');
                var index = $parent.attr('id') ? this.getRuleIndex($parent) : null;

                if ($element.val() == Def.constants.operatorIs) {
                    var $select = $(this.createIsOperatorSelect());
                    $parent.find('.primary-value').parents('td').replaceWith($select);
                    AGN.Lib.CoreInitializer.run('select', $select);
                } else if ($parent.find('.primary-value').hasClass('null-select')) {
                    var html;
                    if (index == null) {
                        html = this.createSimpleNewRuleValueField();
                    } else {
                        html = this.createSimpleRuleValueField(index);
                    }
                    $parent.find('.primary-value').parents('td').replaceWith(html);
                }

                if (index == null) {
                    this.addNewRuleProperties($parent.find('.primary-value'));
                } else {
                    this.addRuleProperties($parent.find('.primary-value'), index);
                }
            },

            createGenderSelect: function() {
                var options = Utils.mapAsOptionsHtml(Def.constants.genderOptions);
                return '<td><select class="primary-value form-control">' + options + '</select></td>';
            },

            createIsOperatorSelect: function() {
                return '<td><select class="primary-value null-select form-control">' +
                    '<option value="NULL">NULL</option>' +
                    '<option value="NOT_NULL">NOT NULL</option>' +
                    '</select></td>';
            },

            createSimpleRuleValueField: function(ruleIndex) {
                return '<td><input name="rules[' + ruleIndex + '].primaryValue" type="text" class="primary-value rule-field form-control"/></td>';
            },

            createSimpleNewRuleValueField: function() {
                return '<td><input id="newRule_primaryValue" type="text" class="primary-value form-control"/></td>';
            },


            addRuleProperties: function($element, ruleIndex) {
                $element.filter('select, input')
                    .attr('name', 'rules[' + ruleIndex + '].primaryValue')
                    .addClass('rule-field');
            },

            addNewRuleProperties: function($element) {
                $element.filter('select, input')
                    .attr('id', 'newRule_primaryValue');
            },

            onExecutionChanged: function() {
                var value = $('input[name="executeOnce"]:checked').val();
                if (value == 'false') {
                    this.hideDiv('executionDateLabel');
                    this.showDiv('firstExecutionDateLabel');
                } else {
                    this.hideDiv('firstExecutionDateLabel');
                    this.showDiv('executionDateLabel');
                }
            },

            onReactionChanged: function() {
                var selectedReaction = $('form[name="startForm"] #startReaction').val();
                if (selectedReaction == Def.constants.reactionOpened
                    || selectedReaction == Def.constants.reactionNotOpened
                    || selectedReaction == Def.constants.reactionClicked
                    || selectedReaction == Def.constants.reactionNotClicked
                    || selectedReaction == Def.constants.reactionBought
                    || selectedReaction == Def.constants.reactionNotBought
                    || selectedReaction == Def.constants.reactionClickedLink) {
                    this.hideDiv('reactionStartProfile');
                    this.showDiv('reactionStartMailing');
                } else if (selectedReaction == Def.constants.reactionChangeOfProfile) {
                    this.hideDiv('reactionStartMailing');
                    this.showDiv('reactionStartProfile');
                } else {
                    this.hideDiv('reactionStartMailing');
                    this.hideDiv('reactionStartProfile');
                }
                if (selectedReaction == Def.constants.reactionClickedLink) {
                    this.showDiv('reactionStartMailingLink');
                } else {
                    this.hideDiv('reactionStartMailingLink');
                }
            },

            onReminderChanged: function() {
                if ($('input[name="sendReminder"]:checked').val()) {
                    $('form[name="startForm"]')
                        .find('#reminderComment')
                        .val(this.generateReminderComment());

                    this.showDiv('reminderDetails');
                } else {
                    this.hideDiv('reminderDetails');
                }
                return false;
            },

            onScheduleReminderDateChanged: function() {
                if ($('form[name="startForm"] input[name="remindSpecificDate"]:checked').val() == 'true') {
                    this.showDiv('dateTimePicker');
                } else {
                    this.hideDiv('dateTimePicker');
                }
                return false;
            },

            onMailingSelectChange: function(value, selectedValue) {
                var $linkSelect = $('form[name="startForm"] select[name="linkId"]');
                var mailingId = parseInt(value, 10);

                if (mailingId > 0) {
                    $linkSelect.attr('readonly', 'readonly');
                    $.ajax({
                        type: 'POST',
                        url: AGN.url('/workflow/getMailingLinks.action'),
                        data: {
                            mailingId: mailingId
                        },
                        success: function(data) {
                            // populate the drop-down list with mailing links
                            $linkSelect.empty();

                            $.each(data, function(index, itemUrl) {
                                $linkSelect.append($('<option></option>', {value: itemUrl.id, text: itemUrl.url}));
                            });

                            $linkSelect.removeAttr('readonly');

                            if (selectedValue != null && selectedValue != undefined) {
                                $linkSelect.val(selectedValue);
                                EditorsHelper.initSelectWithValueOrChooseFirst($linkSelect, selectedValue);
                            } else {
                                EditorsHelper.initSelectWithFirstValue($linkSelect);
                            }
                        }
                    });
                } else {
                    $linkSelect.empty();
                }
            },

            updateVisibility: function() {
                this.onStartTypeChanged();
                this.onStartEventChanged();
                this.onReactionChanged();
                this.onRuleModeChanged();
                this.onExecutionChanged();
                this.onReminderChanged();
                this.onScheduleReminderDateChanged();
            },

            hideDiv: function(id) {
                $('form[name="startForm"] #' + id).css('display', 'none');
            },

            showDiv: function(id) {
                $('form[name="startForm"] #' + id).css('display', '');
            },

            // Date picker uses a date in UTC but JS uses a local timezone so we need to workaround that
            dateAsUTC: function(date) {
                if (date) {
                    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
                } else {
                    return null;
                }
            },

            formatTime: function(hours, minutes) {
                var h = 0;
                var m = 0;

                hours = parseInt(hours, 10);
                if (hours >= 0) {
                    h = hours;
                }

                minutes = parseInt(minutes, 10);
                if (minutes >= 0) {
                    m = minutes;
                }

                return (h < 10 ? '0' + h : h) + ':' + (m < 10 ? '0' + m : m);
            },

            checkPresentNodeOfType: function(types) {
                return EditorsHelper.getNodesByTypes(types).length > 0;
            },
        };

        EditorsHelper.registerEditor('start', nodeEditor);
        EditorsHelper.registerEditor('stop', nodeEditor);

        controller.addAction({change: 'start-editor-execution-changed'}, function() {
            nodeEditor.onExecutionChanged();
        });

        controller.addAction({change: 'start-editor-event-changed'}, function() {
            nodeEditor.onStartEventChanged();
        });

        controller.addAction({change: 'start-editor-type-changed'}, function() {
            nodeEditor.onStartTypeChanged()
        });

        controller.addAction({change: 'start-editor-rule-changed'}, function() {
            nodeEditor.onRuleModeChanged();
        });

        controller.addAction({change: 'start-editor-reaction-changed'}, function() {
            nodeEditor.onReactionChanged();
        });

        controller.addAction({change: 'start-editor-profile-field-changed'}, function() {
            nodeEditor.onProfileFieldChanged();
        });

        controller.addAction({click: 'start-editor-add-rule'}, function() {
            nodeEditor.addRule();
        });

        controller.addAction({click: 'start-editor-reminder-changed'}, function() {
            nodeEditor.onReminderChanged();
        });

        controller.addAction({click: 'start-editor-schedule-reminder-date-changed'}, function() {
            nodeEditor.onScheduleReminderDateChanged();
        });

        controller.addAction({click: 'start-editor-validate'}, function() {
            nodeEditor.validateEditor();
        });

        controller.addAction({click: 'start-editor-mailing-sort-date'}, function() {
            startMailingSelector.onMailingSortClick('date');
        });

        controller.addAction({change: 'start-editor-mailing-select'}, function() {
            nodeEditor.onMailingSelectChange(this.el.val());
        });

        controller.addAction({click: 'start-editor-mailing-sort-shortname'}, function() {
            startMailingSelector.onMailingSortClick('shortname');
        });
    });

    this.addDomInitializer('decision-editor-initializer', function() {
        var decisionData = this.config;
        var decisionProfileFieldsTypes = decisionData.profileFields;
        var decisionMailingSelector = new MailingSelector(decisionData.form, decisionData.container, decisionData.selectName, decisionData.noMailingOption);

        // Field is used to set correct mailingID if "Profile field" selected as decision criteria.
        var profileFieldMailingId;

        var nodeEditor = EditorsHelper.registerEditor('decision', {
            rulesNumber: 0,
            rulePrefix: 'decision_',
            prevProfileField: '',
            curProfileField: '',

            getTitle: function() {
                return t('workflow.decision');
            },

            fillEditor: function(node) {
                var $form = $('form[name="decisionForm"]');
                // Field is used to set correct mailingID if "Profile field" selected as decision criteria.
                profileFieldMailingId = 0;

                var data = node.getData();

                $form.submit(false);
                $form.find('.rule-row').each(function() {
                    $(this).remove();
                });
                this.curProfileField = data.profileField;
                if (data.rules != undefined) {
                    for (var i = 0; i < data.rules.length; i++) {
                        var rule = data.rules[i];
                        this.createRuleRow(i, rule);
                    }
                    this.rulesNumber = data.rules.length;
                }
                $form.get(0).reset();
                EditorsHelper.fillFormFromObject('decisionForm', data, '');

                decisionMailingSelector.setMailingId(data.mailingId);
                decisionMailingSelector.cleanOptions(1);

                if (data.decisionDate == undefined) {
                    data.decisionDate = new Date();
                }

                var decisionDatePicker = $form.find('#decisionDate');
                var decisionTimePicker = $form.find('#decisionTime');

                if (this.checkPresentNodeOfType([Def.NODE_TYPE_ACTION_BASED_MAILING, Def.NODE_TYPE_DATE_BASED_MAILING])) {
                    decisionDatePicker.parent().parent().hide();
                    decisionTimePicker.parent().parent().parent().parent().hide();
                } else {
                    decisionDatePicker.parent().parent().show();
                    decisionTimePicker.parent().parent().parent().parent().show();

                    // init date and time picker
                    decisionDatePicker.pickadate('picker').set('select', this.dateAsUTC(data.decisionDate));
                    decisionTimePicker.val(('0' + data.decisionDate.getHours()).slice(-2) + ':' + ('0' + data.decisionDate.getMinutes()).slice(-2));
                }

                this.updateChainFieldsVisibility();
                this.updateVisibility();
                this.updateDateFormatVisibility();
                this.updateOperatorsAvailability();

                // update mailing links in editor
                if (data.decisionType == Def.constants.decisionTypeDecision && data.decisionCriteria == Def.constants.decisionReaction &&
                    data.reaction == Def.constants.reactionClickedLink && (data.mailingId != 0 && data.mailingId != null)) {
                    this.onMailingSelectChange(data.mailingId, data.linkId);
                } else {
                    EditorsHelper.resetSelect($form.find('select[name="linkId"]'));
                }

                $('#decision-editor .editor-error-messages').css('display', 'none');
            },

            saveEditor: function() {
                var data = EditorsHelper.formToObject('decisionForm');

                //We should use ID of previous mailing if "Profile field" selected in decision criteria,
                // not mailing ID from Mailing dropdown of decision form.
                if (data.decisionCriteria == Def.constants.decisionProfileField && profileFieldMailingId != 0) {
                    data.mailingId = profileFieldMailingId;
                }
                //Field reset.
                profileFieldMailingId = 0;

                if (data.rules == undefined) {
                    data.rules = [];
                }
                if (!data.threshold) {
                    data.threshold = '';
                }

                if (this.checkPresentNodeOfType([Def.NODE_TYPE_ACTION_BASED_MAILING, Def.NODE_TYPE_DATE_BASED_MAILING])) {
                    data.decisionDate = null;
                } else {
                    var $form = $('form[name="decisionForm"]');
                    data.decisionDate = $form.find('#decisionDate').pickadate('picker').get('select').obj;

                    var time = $form.find('#decisionTime').val();
                    data.decisionDate.setHours(time.substring(0, 2));
                    data.decisionDate.setMinutes(time.substring(3, 5));
                }
                return data;
            },

            validateEditor: function() {
                var $form = $('form[name="decisionForm"]');
                var $messages = $('#decision-editor .editor-error-messages');

                var decisionType = $form.find('input[name="decisionType"]:checked').val();
                var decisionCriteria = $('#decisionCriteria').val();
                var mailingId = $form.find('select[name="mailingId"]').val();
                var linkId = $form.find('select[name="linkId"]').val();
                var reaction = $form.find('select[name="reaction"]').val();
                var threshold = $form.find('input[name="threshold"]').val();

                if (decisionType == Def.constants.decisionTypeDecision && decisionCriteria == Def.constants.decisionReaction && mailingId == 0) {
                    $messages.html(t('error.workflow.noMailing'));
                    $messages.css('display', 'block');
                } else if (decisionType == Def.constants.decisionTypeDecision && decisionCriteria == Def.constants.decisionReaction && reaction == Def.constants.reactionClickedLink
                    && (linkId == 0 || linkId == null)) {
                    $messages.html(t('error.workflow.noLinkSelected'));
                    $messages.css('display', 'block');
                } else if (decisionType == Def.constants.decisionTypeDecision && decisionCriteria == Def.constants.decisionProfileField && decisionData.isMailtrackingActive == false) {
                    // Do nothing
                } else if (decisionType == Def.constants.decisionTypeAutoOptimization) {
                    if (threshold) {
                        var thresholdIntValue = parseInt(threshold, 10);
                        if (thresholdIntValue && thresholdIntValue > 0) {
                            EditorsHelper.saveCurrentEditorWithUndo();
                        } else {
                            $messages.html(t('error.workflow.noValidThreshold'));
                            $messages.css('display', 'block');
                        }
                    } else {
                        // Value is omitted (input field is empty)
                        EditorsHelper.saveCurrentEditorWithUndo();
                    }
                } else {
                    EditorsHelper.saveCurrentEditorWithUndo();
                }
            },

            onTypeChanged: function() {
                var value = $('form[name="decisionForm"] input[name="decisionType"]:checked').val();
                if (value == Def.constants.decisionTypeDecision) {
                    this.hideDecisionFormDiv('autoOptimizationPanel');
                    this.showDecisionFormDiv('decisionPanel');
                } else if (value == Def.constants.decisionTypeAutoOptimization) {
                    this.hideDecisionFormDiv('decisionPanel');
                    this.showDecisionFormDiv('autoOptimizationPanel');
                }
                this.updateVisibilityOfRuleMailingReceived();
            },

            onDecisionReactionChanged: function() {
                var value = $('form[name="decisionForm"] select[name="reaction"]').val();
                if (value == Def.constants.reactionClickedLink) {
                    this.showDecisionFormDiv('reactionLinkPanel');
                    var mailingId = $('form[name="decisionForm"] select[name="mailingId"]').val();
                    this.onMailingSelectChange(mailingId, 0);
                } else {
                    this.hideDecisionFormDiv('reactionLinkPanel');
                }
            },

            onCriteriaChanged: function() {
                var value = $('form[name="decisionForm"] select[name="decisionCriteria"]').val();
                if (value == Def.constants.decisionReaction) {
                    this.hideDecisionFormDiv('decisionProfileFieldPanel');
                    this.showDecisionFormDiv('decisionReactionPanel');
                } else if (value == Def.constants.decisionProfileField) {
                    this.hideDecisionFormDiv('decisionReactionPanel');
                    this.showDecisionFormDiv('decisionProfileFieldPanel');
                }
                this.updateVisibilityOfRuleMailingReceived();
            },

            onMailingSelectChange: function(value, selectedValue) {
                var $linkSelect = $('form[name="decisionForm"] select[name="linkId"]');
                var mailingId = parseInt(value, 10);

                if (mailingId > 0) {
                    $linkSelect.attr('readonly', 'readonly');
                    $.ajax({
                        type: 'POST',
                        url: AGN.url('/workflow/getMailingLinks.action'),
                        data: {
                            mailingId: mailingId
                        },
                        success: function(data) {
                            // populate the drop-down list with mailing links
                            $linkSelect.empty();

                            $.each(data, function(index, itemUrl) {
                                $linkSelect.append($('<option></option>', {value: itemUrl.id, text: itemUrl.url}));
                            });

                            $linkSelect.removeAttr('readonly');

                            if (selectedValue != null && selectedValue != undefined) {
                                $linkSelect.val(selectedValue);
                                EditorsHelper.initSelectWithValueOrChooseFirst($linkSelect, selectedValue);
                            } else {
                                EditorsHelper.initSelectWithFirstValue($linkSelect);
                            }
                        }
                    });
                } else {
                    $linkSelect.empty();
                }
            },

            onRuleOperatorChanged: function($element) {
                if (this.curProfileField && this.curProfileField.toLowerCase() == Def.GENDER_PROFILE_FIELD) {
                    return;
                }

                var $parent = $element.closest('tr');
                var index = $parent.attr('id') ? this.getRuleIndex($parent) : null;

                if ($element.val() == Def.constants.operatorIs) {
                    var $select = $(this.createIsOperatorSelect());
                    $parent.find('.primary-value').parents('td').replaceWith($select);
                    AGN.Lib.CoreInitializer.run('select', $select);
                } else if ($parent.find('.primary-value').hasClass('null-select')) {
                    var html;
                    if (index == null) {
                        html = this.createSimpleNewRuleValueField();
                    } else {
                        html = this.createSimpleRuleValueField(index);
                    }
                    $parent.find('.primary-value').parents('td').replaceWith(html);
                }

                if (index == null) {
                    this.addNewRuleProperties($parent.find('.primary-value'));
                } else {
                    this.addRuleProperties($parent.find('.primary-value'), index);
                }
            },

            getRuleIndex: function($element) {
                return $element.attr('id').substring(14);
            },

            onProfileFieldChanged: function() {
                var $form = $('form[name="decisionForm"]');
                var profileField = $form.find('select[name="profileField"]').val();
                this.curProfileField = profileField;
                var self = this;

                // if the field gender is selected - we need to replace all value fields with gender dropdown
                if (profileField && profileField.toLowerCase() == Def.GENDER_PROFILE_FIELD) {
                    // remove all primary-value controls
                    $('#decisionProfileFieldRules .primary-value').each(function() {
                        $(this).parents('td').remove();
                    });
                    // add gender-select for existing rules
                    $form.find('.rule-row').each(function() {
                        var index = self.getRuleIndex($(this));
                        $(this).find('.decision-rule-operator').parents('td').after(self.createGenderSelect());
                        self.addRuleProperties($(this).find('.primary-value'), index);
                    });
                    // add gender-select for new-rule control
                    $('#decision_newRule_primaryOperator').parents('td').after(this.createGenderSelect());
                    this.addNewRuleProperties($('#decisionProfileFieldRuleAdd .primary-value'));
                }

                // if it's not gender selected and previous selection was gender - we need to replace gender drop-downs with fields
                else if (this.prevProfileField && this.prevProfileField.toLowerCase() == Def.GENDER_PROFILE_FIELD) {
                    $('#decisionProfileFieldRules .primary-value').each(function() {
                        $(this).parents('td').remove();
                    });
                    $form.find('.rule-row').each(function() {
                        var index = self.getRuleIndex($(this));
                        if ($(this).find('.decision-rule-operator').val() == Def.constants.operatorIs) {
                            $(this).find('.primary-value').parents('td').remove();
                            $(this).find('select').parents('td').after(self.createIsOperatorSelect());
                            self.addRuleProperties($(this).find('.primary-value'), index);
                        } else {
                            $(this).find('.decision-rule-operator').parents('td').after(self.createSimpleRuleValueField(index))
                        }
                    });

                    if ($('#decision_newRule_primaryOperator').val() == Def.constants.operatorIs) {
                        $('#decisionProfileFieldRuleAdd').find('select').parents('td').after(this.createIsOperatorSelect());
                        this.addNewRuleProperties($('#decisionProfileFieldRuleAdd .primaryValue'));
                    } else {
                        $('#decision_newRule_primaryOperator').parents('td').after(this.createSimpleNewRuleValueField());
                    }
                }

                this.updateDateFormatVisibility();
                this.updateOperatorsAvailability();

                this.prevProfileField = profileField;
            },

            updateDateFormatVisibility: function() {
                var profileField = $('form[name="decisionForm"] select[name="profileField"]').val();
                var fieldType = decisionProfileFieldsTypes[profileField];
                if (fieldType && fieldType.toLowerCase() == Def.FIELD_TYPE_DATE) {
                    this.showDecisionFormDiv('decisionDateFormat');
                } else {
                    this.hideDecisionFormDiv('decisionDateFormat');
                }
            },

            updateOperatorsAvailability: function() {
                var $form = $('form[name="decisionForm"]');
                var profileField = $form.find('select[name="profileField"]').val();
                var fieldType = decisionProfileFieldsTypes[profileField];

                $form.find('select.decision-rule-operator').each(function() {
                    var $select = $(this);

                    $select.children('option').prop('disabled', function() {
                        var types = $(this).data('types');

                        if (profileField && profileField.toLowerCase() == Def.GENDER_PROFILE_FIELD) {
                            return this.text != '=' && this.text != '!=';
                        }

                        if (types == '*' || !types) {
                            return false;
                        }

                        return types.split(/[\s,]+/).indexOf(fieldType) == -1;
                    });

                    AGN.Lib.CoreInitializer.run('select', $select);

                    if ($select.val() == null) {
                        EditorsHelper.initSelectWithFirstValue($select);
                    }
                });
            },

            onRulesChanged: function() {
                var ruleIndex = 0;
                var self = this;
                $('form[name="decisionForm"] #decisionProfileFieldAddedRules .rule-row').each(function() {
                    var $this = $(this);

                    var ruleId = $this.attr('id');
                    var oldIndex = self.getRuleIndex($this);
                    var $rule = $('#' + ruleId);

                    $rule.find('.rule-field').attr('name', function(i, name) {
                        return name ? name.replace(oldIndex, ruleIndex + '') : '';
                    });

                    $rule.find('.delete-rule').attr('data-rule-index', function(i, value) {
                        return value == oldIndex ? ruleIndex : value;
                    });

                    $this.attr('id', 'decision_rule_' + ruleIndex);
                    ruleIndex++;
                });
                this.updateChainFieldsVisibility();
            },

            updateChainFieldsVisibility: function() {
                var $form = $('form[name="decisionForm"]');
                if (this.rulesNumber > 0) {
                    $form.find('[name="rules[0].chainOperator"]').css('visibility', 'hidden');
                    $form.find('#decision_newRule_chainOperator').css('visibility', 'visible');
                } else {
                    $form.find('#decision_newRule_chainOperator').css('visibility', 'hidden');
                }
            },

            addRule: function() {
                var $form = $('form[name="decisionForm"]');
                var rule = {
                    chainOperator: $form.find('#decision_newRule_chainOperator').val(),
                    parenthesisOpened: $form.find('#decision_newRule_parenthesisOpened').val(),
                    primaryOperator: $form.find('#decision_newRule_primaryOperator').val(),
                    primaryValue: $form.find('#decision_newRule_primaryValue').val(),
                    parenthesisClosed: $form.find('#decision_newRule_parenthesisClosed').val()
                };
                this.createRuleRow(this.rulesNumber, rule);
                EditorsHelper.fillFormFromObject('decisionForm', rule, 'rules[' + this.rulesNumber + '].');
                this.rulesNumber++;

                if ($('#decision_newRule_primaryOperator').val() == Def.constants.operatorIs) {
                    $('#decision_newRule_primaryValue').parents('td').replaceWith(this.createSimpleNewRuleValueField());
                }
                this.updateChainFieldsVisibility();
                this.updateOperatorsAvailability();
                $form.find('#decisionProfileFieldRuleAdd select').each(function() {
                    EditorsHelper.initSelectWithFirstValue($(this));
                });
                $form.find('#decisionProfileFieldRuleAdd input').each(function() {
                    $(this).val('');
                });
            },

            removeRuleRow: function(index) {
                $('form[name="decisionForm"] #decision_rule_' + index).remove();
                this.rulesNumber--;
                this.onRulesChanged();
            },

            createRuleRow: function(ruleIndex, rule) {
                var $form = $('form[name="decisionForm"]');
                var valueElement = this.createSimpleRuleValueField(ruleIndex);
                var options = Utils.arrayAsOptionsHtml(Def.constants.operators);
                if (this.curProfileField && this.curProfileField.toLowerCase() == Def.GENDER_PROFILE_FIELD) {
                    valueElement = this.createGenderSelect();
                } else if (rule.primaryOperator == Def.constants.operatorIs) {
                    valueElement = this.createIsOperatorSelect();
                }
                $form.find('#decisionProfileFieldAddedRules').append(
                    '<tr class="rule-row" id="decision_rule_' + ruleIndex + '">' +
                    '<td><select name="rules[' + ruleIndex + '].chainOperator" class="rule-field">' +
                    '<option value="' + Def.constants.chainOperatorAnd + '">' + t('workflow.defaults.and') + '</option>' +
                    '<option value="' + Def.constants.chainOperatorOr + '">' + t('workflow.defaults.or') + '</option>' +
                    '</select></td>' +
                    '<td><select name="rules[' + ruleIndex + '].parenthesisOpened" class="parentheses-opened rule-field">' +
                    '<option value="0">&nbsp</option>' +
                    '<option value="1">(</option>' +
                    '</select></td>' +
                    '<td><select name="rules[' + ruleIndex + '].primaryOperator" class="decision-rule-operator rule-field" data-action="decision-rule-operator-change">' + options +
                    '</select></td>' +
                    valueElement +
                    '<td><select name="rules[' + ruleIndex + '].parenthesisClosed" class="parenthesis-closed rule-field">' +
                    '<option value="0">&nbsp</option>' +
                    '<option value="1">)</option>' +
                    '</select></td>' +
                    '<td><a class="mailing_delete delete-rule btn btn-regular btn-alert" href="#" data-action="decision-editor-rule-remove" data-rule-index="' + ruleIndex + '"><i class="icon icon-minus-circle"></i></a></td>' +
                    '</tr>');
                this.addRuleProperties($('#decision_rule_' + ruleIndex + ' .primary-value'), ruleIndex);

                AGN.runAll($form.find('#decisionProfileFieldAddedRules'));
            },

            createGenderSelect: function() {
                var options = Utils.mapAsOptionsHtml(Def.constants.genderOptions);
                return '<td><select class="primary-value form-control">' + options + '</select></td>';
            },

            createIsOperatorSelect: function() {
                return '<td><select class="primary-value null-select form-control">' +
                    '<option value="NULL">NULL</option>' +
                    '<option value="NOT_NULL">NOT NULL</option>' +
                    '</select></td>';
            },

            createSimpleRuleValueField: function(ruleIndex) {
                return '<td><input name="rules[' + ruleIndex + '].primaryValue" type="text" class="primary-value rule-field form-control"/></td>';
            },

            createSimpleNewRuleValueField: function() {
                return '<td><input id="decision_newRule_primaryValue" type="text" class="primary-value form-control"/></td>';
            },

            addRuleProperties: function($element, ruleIndex) {
                $element.filter('select, input')
                    .attr('name', 'rules[' + ruleIndex + '].primaryValue')
                    .addClass('rule-field');
            },

            addNewRuleProperties: function($element) {
                $element.filter('select, input')
                    .attr('id', 'decision_newRule_primaryValue');
            },

            updateVisibility: function() {
                this.onTypeChanged();
                this.onDecisionReactionChanged();
                this.onCriteriaChanged();
            },

            hideDecisionFormDiv: function(id) {
                $('form[name="decisionForm"] #' + id).css('display', 'none');
            },

            showDecisionFormDiv: function(id) {
                $('form[name="decisionForm"] #' + id).css('display', 'block');
            },

            updateVisibilityOfRuleMailingReceived: function() {
                var decisionType = $('form[name="decisionForm"] input[name="decisionType"]:checked').val();
                if (decisionType == Def.constants.decisionTypeAutoOptimization) {
                    this.hideDecisionFormDiv('ruleMailingReceivedWrapper');
                } else if (decisionType == Def.constants.decisionTypeDecision) {
                    var decisionCriteria = $('form[name="decisionForm"] select[name="decisionCriteria"]').val();
                    if (decisionCriteria == Def.constants.decisionReaction) {
                        this.hideDecisionFormDiv('ruleMailingReceivedWrapper');
                    } else if (decisionCriteria == Def.constants.decisionProfileField) {
                        this.hideDecisionFormDiv('ruleMailingReceivedWrapper');
                        var localShowDecisionFormDiv = this.showDecisionFormDiv;
                        //check if mailing icon exists
                        EditorsHelper.forEachPreviousNode(function(node) {
                            if (Def.NODE_TYPES_MAILING.includes(node.getType())) {
                                var data = node.getData();
                                // Memorizes mailing ID to be set as decisions mailingId.
                                profileFieldMailingId = data.mailingId;
                                localShowDecisionFormDiv('ruleMailingReceivedWrapper');
                                return false;
                            }
                        });
                    }
                }
            },

            // Date picker uses a date in UTC but JS uses a local timezone so we need to workaround that
            dateAsUTC: function(date) {
                if (date) {
                    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
                } else {
                    return null;
                }
            },

            checkPresentNodeOfType: function(types) {
                return EditorsHelper.getNodesByTypes(types).length > 0;
            },
        });

        controller.addAction({click: 'decision-editor-mailing-sort-shortname'}, function() {
            decisionMailingSelector.onMailingSortClick('shortname');
        });

        controller.addAction({click: 'decision-editor-mailing-sort-date'}, function() {
            decisionMailingSelector.onMailingSortClick('date');
        });

        controller.addAction({change: 'decision-editor-mailing-select'}, function() {
            nodeEditor.onMailingSelectChange(this.el.val());
        });

        controller.addAction({change: 'decision-rule-operator-change'}, function() {
            nodeEditor.onRuleOperatorChanged(this.el);
        });

        controller.addAction({click: 'decision-editor-rule-remove'}, function() {
            nodeEditor.removeRuleRow(this.el.data('rule-index'));
        });

        controller.addAction({click: 'decision-editor-rule-add'}, function() {
            nodeEditor.addRule();
        });

        controller.addAction({change: 'decision-editor-type-change'}, function() {
            nodeEditor.onTypeChanged();
        });

        controller.addAction({change: 'decision-editor-criteria-change'}, function() {
            nodeEditor.onCriteriaChanged();
        });

        controller.addAction({change: 'decision-editor-reaction-change'}, function() {
            nodeEditor.onDecisionReactionChanged();
        });

        controller.addAction({click: 'decision-editor-save'}, function() {
            nodeEditor.validateEditor();
        });

        controller.addAction({change: 'decision-editor-profile-field-change'}, function() {
            nodeEditor.onProfileFieldChanged();
        });
    });

    this.addDomInitializer('mailing-editor-initializer', function() {
        var mailingData = this.config;
        var mailingEditorBase = new MailingEditorHelper(mailingData, submitWorkflowForm);

        var nodeEditor = EditorsHelper.registerEditor('mailing', {
            formName: 'mailingForm',
            safeToSave: true,

            getTitle: function() {
                return t('workflow.defaults.mailing');
            },

            fillEditor: function(node) {
                var $form = $('form[name="' + this.formName + '"]');
                var data = node.getData();

                $form.submit(false);
                $form.get(0).reset();

                EditorsHelper.fillFormFromObject(this.formName, data, '');
                mailingEditorBase.fillEditorBase(node);
                $('#mailing-editor .editor-error-messages').css('display', 'none');
            },

            createNewMailing: function() {
                mailingEditorBase.createNewMailing(Def.constants.forwardMailingCreate);
            },

            editMailing: function() {
                mailingEditorBase.editMailing(Def.constants.forwardMailingEdit);
            },

            copyMailing: function() {
                mailingEditorBase.copyMailing(Def.constants.forwardMailingCopy);
            },

            saveEditor: function() {
                mailingEditorBase.setNodeFields();
                mailingEditorBase.disableInputs();
                return EditorsHelper.formToObject(this.formName);
            },

            saveWithCheckStatus: function() {
                var isNotSent = mailingEditorBase.showSecurityQuestion();
                if (isNotSent) {
                    EditorsHelper.saveCurrentEditorWithUndo(false, mailingEditorBase);
                }
            }
        });

        controller.addAction({click: 'mailing-editor-save'}, function() {
            mailingEditorBase.validateEditor(nodeEditor.saveWithCheckStatus);
        });

        controller.addAction({click: 'mailing-editor-base-close'}, function() {
            mailingEditorBase.closeOneMailinglistWarningDialog();
        });

        controller.addAction({click: 'mailing-editor-base-accept'}, function() {
            mailingEditorBase.acceptOneMailinglistPerCampaign();
        });

        controller.addAction({click: 'mailing-editor-base-sort-shortname'}, function() {
            mailingEditorBase.onMailingSortClick('shortname', 'shortname');
        });

        controller.addAction({click: 'mailing-editor-base-sort-data'}, function() {
            var config = Utils.getConfigData(this.el);
            mailingEditorBase.onMailingSortClick('shortname', config.sortByDate);
        });

        controller.addAction({change: 'mailing-editor-base-status-change'}, function() {
            mailingEditorBase.onMailingsStatusChange(this.el.val());
        });

        controller.addAction({change: 'mailing-editor-base-select-change'}, function() {
            mailingEditorBase.onMailingSelectChange(this.el.val());
        });

        controller.addAction({click: 'mailing-editor-base-secure-cancel'}, function() {
            mailingEditorBase.cancelSecurityDialog();
        });

        controller.addAction({click: 'mailing-editor-base-toggle-settings'}, function() {
            var config = Utils.getConfigData(this.el);
            mailingEditorBase.toggleSendSettings(config.editorId);
        });
    });

    this.addDomInitializer('followup-mailing-editor-initializer', function() {
        var baseMailingData = this.config.baseMailingData;
        var followupMailingData = this.config.followupMailingData;

        baseMailingData.$dropdown = $('#followup-m-editor');

        var baseMailingEditorBase = new MailingEditorHelper(baseMailingData, submitWorkflowForm);
        var followupMailingEditorBase = new MailingEditorHelper(followupMailingData, submitWorkflowForm);

        baseMailingEditorBase.selectedFollowUpMailingId = 0;

        baseMailingEditorBase.findPreviousMailingIds = function() {
            var previousMailingIds = [];

            EditorsHelper.forEachPreviousNode(function(node) {
                var type = node.getType();

                if (Def.NODE_TYPES_MAILING.includes(type) && Def.NODE_TYPE_FOLLOWUP_MAILING != type) {
                    previousMailingIds.push(node.getId());
                }
            });

            return previousMailingIds;
        };

        baseMailingEditorBase.getMailingsByWorkStatus = function(status, sort, order, selectedMailValue) {
            var mailingEditorBase = this;
            var $mailingsList = $(this.formNameJId + ' ' + this.selectNameJId);
            var previousMailingIds = _.uniq(this.findPreviousMailingIds()).toString();
            $mailingsList.attr('readonly', 'readonly');

            $.ajax({
                type: 'POST',
                url: AGN.url('/workflow/getMailingsByWorkStatus.action'),
                data: {
                    mailingTypes: this.mailingTypesForLoading.join(','),
                    status: status,
                    sort: sort,
                    order: order,
                    mailingId: this.selectedFollowUpMailingId,
                    parentMailingId: $(this.formNameJId + ' ' + this.selectNameJId).val(),
                    mailingStatus: 'W',
                    takeMailsForPeriod: true,
                    mailingsInCampaign: previousMailingIds
                },
                success: function(data) {
                    //populate the drop-down list with mailings
                    $mailingsList.html('');
                    $mailingsList.append('<option value="0">' + t('workflow.defaults.no_mailing') + '</option>');
                    for (var i = 0; i < data.length; i++) {
                        var obj = data[i];
                        var mailingFontColor = '';
                        var selected = '';

                        if (obj.WORK_STATUS == 'mailing.status.sent' || obj.WORK_STATUS == 'mailing.status.norecipients') {
                            mailingFontColor = 'style=\'color: #808080;\'';
                        }

                        if (selectedMailValue == obj.MAILING_ID) {
                            selected = 'selected';
                        }

                        $mailingsList.append('<option ' + mailingFontColor + ' value=\'' + obj.MAILING_ID + '\' status=\'' + obj.WORK_STATUS + '\' senddate=\'' + obj.SENDDATE + '\' ' + selected + '>' + obj.SHORTNAME + '</option>');
                    }
                    $mailingsList.removeAttr('readonly');
                    mailingEditorBase.mailingsStatus = status;
                    mailingEditorBase.mailingsSort = sort;
                    mailingEditorBase.mailingsOrder = order;
                    $mailingsList.val(mailingEditorBase.mailingId);

                    EditorsHelper.initSelectWithValueOrChooseFirst($mailingsList, mailingEditorBase.mailingId);
                }
            });
        };

        followupMailingEditorBase.onMailingSelectChange = function(val) {
            baseMailingEditorBase.selectedFollowUpMailingId = val;
            this.setSelectMailingOptions(val);
        };

        var nodeEditor = EditorsHelper.registerEditor('followup_mailing', {
            formName: 'followupMailingForm',
            safeToSave: false,

            getTitle: function() {
                return t('workflow.mailing.followup');
            },

            fillEditor: function(node) {
                var $form = $('form[name="' + this.formName + '"]');
                var $messages = $('#followup_mailing-editor .editor-error-messages');
                followupMailingEditorBase.fillEditorBase(node);
                baseMailingEditorBase.fillEditorBase(node);

                $form.submit(false);
                $form.get(0).reset();

                EditorsHelper.fillFormFromObject(this.formName, node.getData(), '');

                if (followupMailingData.disableFollowup == 'true') {
                    $messages.html(t('error.workflow.followupPermission'));
                    $messages.css('display', 'block');
                    baseMailingEditorBase.disableInputs();
                } else {
                    $messages.css('display', 'none');
                }
            },

            createNewMailing: function() {
                var data = baseMailingEditorBase.node.getData();

                //store base mailing data
                data.baseMailingId = baseMailingEditorBase.getSelectedMailingOption().val();
                data.decisionCriterion = baseMailingEditorBase.getSelectedDecisionOption().val();

                followupMailingEditorBase.createNewMailing(Def.constants.forwardMailingCreate);
            },

            editMailing: function() {
                followupMailingEditorBase.editMailing(Def.constants.forwardMailingEdit);
            },

            copyMailing: function() {
                var data = baseMailingEditorBase.node.getData();
                data.baseMailingId = baseMailingEditorBase.getSelectedMailingOption().val();
                data.decisionCriterion = baseMailingEditorBase.getSelectedDecisionOption().val();
                followupMailingEditorBase.copyMailing(Def.constants.forwardMailingCopy);
            },

            saveEditor: function() {
                baseMailingEditorBase.disableInputs();
                followupMailingEditorBase.disableInputs();
                this.setNodeFields();
                return EditorsHelper.formToObject(this.formName);
            },

            saveWithCheckStatus: function() {
                var isNotSent = followupMailingEditorBase.showSecurityQuestion();
                if (isNotSent) {
                    EditorsHelper.saveCurrentEditorWithUndo(false, followupMailingEditorBase);
                }
            },

            validateEditor: function(save) {
                var baseMailingSelector = $(baseMailingEditorBase.formNameJId + ' ' + baseMailingEditorBase.selectNameJId);
                var followupMailingSelector = $(followupMailingEditorBase.formNameJId + ' ' + followupMailingEditorBase.selectNameJId);
                if (baseMailingSelector.val() > 0 && followupMailingSelector.val() > 0) {
                    if (save) {
                        save();
                    } else {
                        EditorsHelper.saveCurrentEditorWithUndo();
                    }
                } else {
                    var $message = $('#followup_mailing-editor').find('.editor-error-messages');
                    $message.html(t('error.workflow.noMailing'));
                    $message.css('display', 'block');
                }
            },

            setNodeFields: function() {
                baseMailingEditorBase.node.setFilled(parseInt(baseMailingEditorBase.mailingId, 10) > 0 && parseInt(followupMailingEditorBase.mailingId, 10) > 0);
            },
        });

        controller.addAction({click: 'followup-mailing-editor-base-sort-shortname'}, function() {
            followupMailingEditorBase.onMailingSortClick('shortname', 'shortname');
        });

        controller.addAction({click: 'followup-mailing-editor-base-sort-data'}, function() {
            var config = Utils.getConfigData(this.el);
            followupMailingEditorBase.onMailingSortClick('shortname', config.sortByDate);
        });

        controller.addAction({change: 'followup-mailing-editor-base-status-change'}, function() {
            followupMailingEditorBase.onMailingsStatusChange(this.el.val());
        });

        controller.addAction({change: 'followup-mailing-editor-base-select-change'}, function() {
            followupMailingEditorBase.onMailingSelectChange(this.el.val());
        });

        controller.addAction({click: 'followup-base-mailing-editor-base-sort-shortname'}, function() {
            baseMailingEditorBase.onMailingSortClick('shortname', 'shortname');
        });

        controller.addAction({click: 'followup-base-mailing-editor-base-sort-data'}, function() {
            var config = Utils.getConfigData(this.el);
            baseMailingEditorBase.onMailingSortClick('shortname', config.sortByDate);
        });

        controller.addAction({change: 'followup-base-mailing-editor-base-status-change'}, function() {
            baseMailingEditorBase.onMailingsStatusChange(this.el.val());
        });

        controller.addAction({change: 'followup-base-mailing-editor-base-select-change'}, function() {
            baseMailingEditorBase.onMailingSelectChange(this.el.val());
        });

        controller.addAction({click: 'followup-base-mailing-editor-base-secure-cancel'}, function() {
            followupMailingEditorBase.cancelSecurityDialog();
        });

        controller.addAction({click: 'followup-mailing-editor-save'}, function() {
            nodeEditor.validateEditor(nodeEditor.saveWithCheckStatus);
        });
    });

    this.addDomInitializer('action-mailing-editor-initializer', function() {
        var actionMailingData = this.config;
        var actionbasedMailingEditorBase = new MailingEditorHelper(actionMailingData, submitWorkflowForm);

        var nodeEditor = EditorsHelper.registerEditor('actionbased_mailing', {
            formName: 'actionbasedMailingForm',
            safeToSave: false,

            getTitle: function() {
                return t('workflow.mailing.action_based');
            },

            fillEditor: function(node) {
                var $form = $('form[name="' + this.formName + '"]');

                $form.submit(false);
                $form.get(0).reset();

                EditorsHelper.fillFormFromObject(this.formName, node.getData(), '');
                actionbasedMailingEditorBase.fillEditorBase(node);
                $('#actionbased_mailing-editor .editor-error-messages').css('display', 'none');
            },

            createNewMailing: function() {
                actionbasedMailingEditorBase.createNewMailing(Def.constants.forwardMailingCreate);
            },

            editMailing: function() {
                actionbasedMailingEditorBase.editMailing(Def.constants.forwardMailingEdit);
            },

            saveEditor: function() {
                actionbasedMailingEditorBase.setNodeFields();
                actionbasedMailingEditorBase.disableInputs();
                return EditorsHelper.formToObject(this.formName);
            },

            saveWithCheckStatus: function() {
                var isNotSent = actionbasedMailingEditorBase.showSecurityQuestion();
                if (isNotSent) {
                    EditorsHelper.saveCurrentEditorWithUndo(false, actionbasedMailingEditorBase);
                }
            },

            copyMailing: function() {
                actionbasedMailingEditorBase.copyMailing(Def.constants.forwardMailingCopy);
            }
        });

        controller.addAction({click: 'action-mailing-editor-save'}, function() {
            actionbasedMailingEditorBase.validateEditor(nodeEditor.saveWithCheckStatus);
        });

        controller.addAction({click: 'action-mailing-editor-base-sort-shortname'}, function() {
            actionbasedMailingEditorBase.onMailingSortClick('shortname', 'shortname');
        });

        controller.addAction({click: 'action-mailing-editor-base-sort-data'}, function() {
            var config = Utils.getConfigData(this.el);
            actionbasedMailingEditorBase.onMailingSortClick('shortname', config.sortByDate);
        });

        controller.addAction({change: 'action-mailing-editor-base-status-change'}, function() {
            actionbasedMailingEditorBase.onMailingsStatusChange(this.el.val());
        });

        controller.addAction({change: 'action-mailing-editor-base-select-change'}, function() {
            actionbasedMailingEditorBase.onMailingSelectChange(this.el.val());
        });

        controller.addAction({click: 'action-mailing-editor-base-secure-cancel'}, function() {
            actionbasedMailingEditorBase.cancelSecurityDialog();
        });
    });

    this.addDomInitializer('date-mailing-initializer', function() {
        var dateMailingData = this.config;
        var datebasedMailingEditorBase = new MailingEditorHelper(dateMailingData, submitWorkflowForm);

        var nodeEditor = EditorsHelper.registerEditor('datebased_mailing', {
            formName: 'datebasedMailingForm',
            safeToSave: false,

            getTitle: function() {
                return t('workflow.mailing.date_based');
            },

            fillEditor: function(node) {
                var $form = $('form[name="' + this.formName + '"]');

                $form.submit(false);
                $form.get(0).reset();

                EditorsHelper.fillFormFromObject(this.formName, node.getData(), '');
                datebasedMailingEditorBase.fillEditorBase(node);
                $('#datebased_mailing-editor .editor-error-messages').css('display', 'none');
            },

            saveEditor: function() {
                datebasedMailingEditorBase.setNodeFields();
                datebasedMailingEditorBase.disableInputs();
                return EditorsHelper.formToObject(this.formName);
            },

            createNewMailing: function() {
                datebasedMailingEditorBase.createNewMailing(Def.constants.forwardMailingCreate);
            },

            editMailing: function() {
                datebasedMailingEditorBase.editMailing(Def.constants.forwardMailingEdit);
            },

            saveWithCheckStatus: function() {
                var isNotSent = datebasedMailingEditorBase.showSecurityQuestion();
                if (isNotSent) {
                    EditorsHelper.saveCurrentEditorWithUndo(false, datebasedMailingEditorBase);
                }
            },

            copyMailing: function() {
                datebasedMailingEditorBase.copyMailing(Def.constants.forwardMailingCopy);
            }
        });

        controller.addAction({click: 'date-mailing-editor-base-sort-shortname'}, function() {
            datebasedMailingEditorBase.onMailingSortClick('shortname', 'shortname');
        });

        controller.addAction({click: 'date-mailing-editor-base-sort-data'}, function() {
            var config = Utils.getConfigData(this.el);
            datebasedMailingEditorBase.onMailingSortClick('shortname', config.sortByDate);
        });

        controller.addAction({change: 'date-mailing-editor-base-status-change'}, function() {
            datebasedMailingEditorBase.onMailingsStatusChange(this.el.val());
        });

        controller.addAction({change: 'date-mailing-editor-base-select-change'}, function() {
            datebasedMailingEditorBase.onMailingSelectChange(this.el.val());
        });

        controller.addAction({click: 'date-mailing-editor-base-secure-cancel'}, function() {
            datebasedMailingEditorBase.cancelSecurityDialog();
        });

        controller.addAction({click: 'date-mailing-editor-save'}, function() {
            datebasedMailingEditorBase.validateEditor(nodeEditor.saveWithCheckStatus);
        });
    });

    this.addAction({click: 'mailing-editor-copy'}, function () {
        EditorsHelper.getCurrentEditor().copyMailing();
    });

    this.addAction({click: 'mailing-editor-new'}, function () {
        EditorsHelper.getCurrentEditor().createNewMailing();
    });

    this.addAction({click: 'mailing-editor-edit'}, function () {
        EditorsHelper.getCurrentEditor().editMailing();
    });

    this.addAction({click: 'editor-save-current'}, function() {
        EditorsHelper.saveCurrentEditorWithUndo();
    });

    this.addAction({click: 'editor-cancel'}, function() {
        EditorsHelper.cancelEditor();
    });

    this.addAction({click: 'chain-mode'}, function() {
        if (editor.isChainMode() || editor.connectSelected()) {
            editor.setChainModeEnabled(false);
        } else {
            editor.setChainModeEnabled(true);
        }

        $(this.el).toggleClass('button-selected', editor.isChainMode());
    });

    this.addAction({click: 'delete-selected'}, function() {
        editor.deleteSelected();
    });

    this.addAction({click: 'align-all'}, function() {
        editor.alignAll();
    });

    this.addAction({click: 'zoom-in'}, function() {
        var slider = $('#slider').slider('instance');
        slider.value(slider.value() + Def.ZOOM_STEP);
        editor.setZoom(slider.value());
    });

    this.addAction({click: 'zoom-out'}, function() {
        var slider = $('#slider').slider('instance');
        slider.value(slider.value() - Def.ZOOM_STEP);
        editor.setZoom(slider.value());
    });

    this.addAction({click: 'reset-zoom'}, function() {
        editor.setZoom(Def.DEFAULT_ZOOM);
        $('#slider').slider('value', Def.DEFAULT_ZOOM);
    });

    this.addAction({click: 'workflow-save'}, function() {
        submitWorkflowForm(true);
    });

    this.addAction({click: 'workflow-copy'}, function () {
        var workflowId = Def.workflowId;

        //checks whether at least one node has content
        var someNonEmptyNode = editor.getNodes().some(function(node) {return node.isFilled()});

        Dialogs.confirmCopy(someNonEmptyNode)
          .done(function(response) {
            window.location.href = AGN.url('/workflow/copy.action' + '?workflowId=' + workflowId
                    + '&isWithContent=' + (response == true));
            return false;
          });
    });

    this.addAction({change: 'workflow-view-change-status'}, function() {
        if ($('#workflow_active').is(':checked')) {
            $('#workflow-status').val(Def.constants.statusActive)
        } else {
            $('#workflow-status').val(Def.constants.statusInactive)
        }
    });

    this.addAction({click: 'undo'}, function() {
        editor.undo();
    });

    this.addAction({click: 'create-workflow-enlarged-editor-modal'}, function () {
        editor.enlargeEditor(function() {submitWorkflowForm(true)});
    });

    this.addAction({click: 'workflow-generate-pdf'}, function() {
        var isNewWorkflow = Def.workflowId <= 0 || !Def.shortname;
        var hasUnsavedChanges = editor.hasUnsavedChanges();

        if (isNewWorkflow || hasUnsavedChanges) {
            var message = '';
            if (isNewWorkflow) {
                message = t('workflow.pdf.save_new_campaign');
            } else if (hasUnsavedChanges) {
                message = t('workflow.pdf.save_modified_campaign');
            }

            Dialogs.confirmSaveBeforePdfGenerating(function () {
                submitWorkflowForm(true);
            }, message);
        } else {
            window.location.href =
              AGN.url('/workflow/' + Def.workflowId + '/generatePDF.action?showStatistics=' + editor.isStatisticEnabled());
            AGN.Lib.Loader.hide();
        }
    });

    this.addAction({click: 'workflow-dry-run'}, function () {
        var isStartTesting = false;
        var newStatus = Def.constants.statusOpen;
        if (Def.constants.initialWorkflowStatus != Def.constants.statusTesting) {
            isStartTesting = true;
            newStatus = Def.constants.statusTesting;
        }

        Dialogs.confirmTestingStartStop(isStartTesting)
          .done(function() {
            // Un-check "active" checkbox
            var activationCheckbox = $('#workflow_active');
            activationCheckbox.prop('disabled', true);
            saveWorkflowFormData(true, {'status': newStatus});
          });
    });

    this.addAction({click: 'toggle-statistic'}, function () {
        if (Def.constants.initialWorkflowStatus == Def.constants.statusOpen) {
            workflowNoStatisticsDialogHandler.showDialog();
        } else {
            editor.toggleStatistic();
        }
    });

    $(window).on('resize viewportChanged', function() {
        if (editor) {
            editor.updateMinimap();
        }
    });

    $(document).on('keydown', function(e) {
        if (e.target && !$(e.target).is('input, textarea')) {
            switch (e.keyCode) {
                case Def.KEY_SPACE:
                    editor.setPanningEnabled(true);
                    break;

                case Def.KEY_SHIFT:
                    editor.setMultiConnectionEnabled(true);
                    break;

                case Def.KEY_CTRL:
                    editor.setMouseWheelZoomEnabled(true);
                    break;
            }
        }
    });

    $(document).on('keyup', function(e) {
        if (e.target && !$(e.target).is('input, textarea')) {
            switch (e.keyCode) {
                case Def.KEY_DELETE:
                    editor.deleteSelected();
                    break;

                case Def.KEY_SPACE:
                    editor.setPanningEnabled(false);
                    break;

                case Def.KEY_SHIFT:
                    editor.setMultiConnectionEnabled(false);
                    break;

                case Def.KEY_ENTER:
                    editor.editIcon();
                    break;

                case Def.KEY_CTRL:
                    editor.setMouseWheelZoomEnabled(false);
                    break;
            }
        }
    });

    function submitWorkflowForm(validate, options) {
        var isActiveChecked = $('#workflow_active').is(':checked');
        var initialWorkflowStatus = Def.constants.initialWorkflowStatus;

        var inactivating = initialWorkflowStatus === Def.constants.statusActive && !isActiveChecked;
        var activating = initialWorkflowStatus !== Def.constants.statusActive && isActiveChecked;

        if (inactivating || !Utils.checkActivation()) {
            if (activating) {
                activate(validate, options);
            } else if (inactivating) {
                deactivate(validate, options);
            } else {
                saveWorkflowFormData(validate, options);
            }
        }
    }

    function activate(validate, options) {
        Dialogs.Activation(
            function() {
                saveWorkflowFormData(validate, options);
            },
            getMailingNames().join('<br/>'));
    }

    function deactivate(validate, options) {
        Dialogs.Deactivation(function() {
            saveWorkflowFormData(validate, options);
        });
    }

    function saveWorkflowFormData(validate, options) {
        var save = function() {
            var form = AGN.Lib.Form.get($('form#workflowForm'));
            options = $.extend({}, options);

            Object.keys(options).forEach(function(key) {
                form.setValueOnce(key, options[key]);
            })

            form.setValueOnce('workflowSchema', editor.serializeIcons());
            form.setValueOnce('editorPositionLeft', -1); //todo: remove if not needed
            form.setValueOnce('editorPositionTop', -1); //todo: remove if not needed
            //form.setValueOnce("workflowUndoHistoryData", campaignManager.getUndoHistoryDataForSubmission());//todo: add after implementation

            form.submit('static');
        };

        if (validate) {
            validateWorkflowBaseData()
                .done(save);
        } else {
            save();
        }
    }

    function getMailingNames() {
        return editor.getNodesByTypes(Def.NODE_TYPES_MAILING).map(function(node) {
            return node.getTitle();
        });
    }

    function validateWorkflowBaseData() {
        var deferred = $.Deferred();

        if ($('#name').val().length < 3) {
            AGN.Lib.Messages(t('workflow.defaults.error'), t('error.workflow.shortName'), 'alert');
            deferred.reject();
        } else {
            if (checkMailingTypesConvertingRequired()) {
                return Confirm.create(Template.text('mailing-types-replace-modal'));
            } else {
                deferred.resolve();
            }
        }

        return deferred.promise();
    }

    function checkMailingTypesConvertingRequired() {
        var icons = editor.getNodes();
        var startType = 'UNKNOWN';
        var mailingStartTypes = {};

        for (var i = 0; i < icons.length; i++) {
            var icon = icons[i];

            if (icon.type === Def.NODE_TYPE_START && icon.filled) {
                var type = 'UNKNOWN';

                switch (icon.startType) {
                    case Def.constants.startTypeDate:
                        type = 'DATE';
                        break;

                    case Def.constants.startTypeEvent:
                        switch (icon.event) {
                            case Def.constants.startEventDate:
                                type = 'RULE';
                                break;

                            case Def.constants.startEventReaction:
                                type = 'REACTION';
                                break;
                        }
                        break;
                }

                if (startType !== type) {
                    if (startType === 'UNKNOWN') {
                        startType = type;
                    } else {
                        // Multiple start icons and mixed start types we can't handle.
                        // Let's fallback to server-side validation.
                        return false;
                    }
                }
            }

            switch (icon.type) {
                case Def.NODE_TYPE_MAILING:
                case Def.NODE_TYPE_FOLLOWUP_MAILING:
                    mailingStartTypes['DATE'] = true;
                    break;

                case Def.NODE_TYPE_ACTION_BASED_MAILING:
                    mailingStartTypes['REACTION'] = true;
                    break;

                case Def.NODE_TYPE_DATE_BASED_MAILING:
                    mailingStartTypes['RULE'] = true;
                    break;
            }
        }

        if (startType !== 'UNKNOWN') {
            mailingStartTypes = Object.keys(mailingStartTypes);

            if (mailingStartTypes.length === 1) {
                if (startType !== mailingStartTypes[0]) {
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
    }
});
