
var constants;

AGN.Lib.Controller.new('workflow-view', function () {
    var Action = AGN.Lib.Action,
        Template = AGN.Lib.Template,
        Confirm = AGN.Lib.Confirm;

    var WorkflowManagerStatistics;
    var nodeFactory = AGN.Lib.WM.NodeFactory;

    var GENDER_PROFILE_FIELD = 'gender';

    // Make sure to keep in sync with WorkflowDependencyType enum.
    var DEPENDENCY_TYPE_ARCHIVE = 1;
    var DEPENDENCY_TYPE_AUTO_EXPORT = 2;
    var DEPENDENCY_TYPE_AUTO_IMPORT = 3;
    var DEPENDENCY_TYPE_MAILING_DELIVERY = 4;
    var DEPENDENCY_TYPE_MAILING_LINK = 5;
    var DEPENDENCY_TYPE_MAILING_REFERENCE = 6;
    var DEPENDENCY_TYPE_MAILINGLIST = 7;
    var DEPENDENCY_TYPE_PROFILE_FIELD = 8;
    var DEPENDENCY_TYPE_PROFILE_FIELD_HISTORY = 9;
    var DEPENDENCY_TYPE_REPORT = 10;
    var DEPENDENCY_TYPE_TARGET_GROUP = 11;
    var DEPENDENCY_TYPE_USER_FORM = 12;

    var campaignManager;
    var data;
    var startEditor;
    var startMailingSelector;
    var decisionMailingSelector;
    var decisionEditor;
    var recipientEditor;
    var ownWorkflowCopyDialogHandler;
    var ownWorkflowEditor;
    var mailingEditor;
    var mailingEditorBase;
    var formsEditor;
    var reportEditor;
    var exportEditor;
    var deadlineEditor;
    var importEditor;
    var archiveEditor;
    var actionbasedMailingEditorBase;
    var actionbasedMailingEditor;
    var datebasedMailingEditor;
    var datebasedMailingEditorBase;
    var baseMailingEditorBase;
    var followupMailingEditorBase;
    var followupMailingEditor;
    var workflowCopyDialogHandler;
    var editorsHelper;
    var workflowSaveBeforePdfHandler;
    var isActivated;
    var workflowStatus;
    var allUsedEntity;
    var iconCommentEditor;



    function setAllEntity(data) {
        return {
            allMailinglists: data.allMailingLists,
            allMailings: data.allMailings,
            allTargets: data.allTargets,
            allReports: data.allReports,
            allUserForms: data.allUserForms,
            allAutoExports: data.allAutoExports,
            allAutoImports: data.allAutoImports,
            allCampaigns: data.allCampaigns
        };
    }

    function mapAsOptionsHtml(options) {
        var html = '';

        Object.keys(options).forEach(function (key){
            var attributes = 'value="' + key + '"';

            html += '<option ' + attributes + '>' + options[key]; + '</option>';
        });

        return html;
    }

    function arrayAsOptionsHtml(options) {
        var html = '';

        options.forEach(function(option) {
            var attributes = 'value="' + option.id + '"';

            var extras = option.data;
            if (extras) {
                Object.keys(extras).forEach(function(k) {
                    attributes += ' data-' + k + '="' + extras[k].replace('"', '&quot;') + '"';
                });
            }

            var text = option.text
                .replace('<', '&lt;')
                .replace('>', '&gt;');

            html += '<option ' + attributes + '>' + text + '</option>';
        });

        return html;
    }

    function getConfigData($e) {
        return AGN.Lib.Helpers.objFromString($e.data('config'));
    }

    this.addDomInitializer('workflow-view-constants', function() {
        if (!constants) {
            constants = new BeanConstants();
        }
        constants.setup(this.config);
    });

    function changeViewProperties() {
        var $viewPort = $("#viewPort");
        $viewPort.css("overflow", "inherit");
        $viewPort.css("float", "none");
        $viewPort.css("border", "none");
        $viewPort.css("sceneHeight", "100%");
        $viewPort.css("width", "100%");
        $viewPort.css("height", "100%");
        $("#navigatorBody").remove();
    }

    function updateWideAndHigh(campaignManager) {

        var sceneHeight = $("#bounder").height();
        var sceneWidth = $("#bounder").width();
        // max width and height which can fit into PDF single page
        var maxWidth = 1360; // TODO: figure out from what was gotten such restriction
        var maxHeight = 800;
        if (sceneHeight > maxHeight || sceneWidth > maxWidth) {
            var newScale = 1.0;
            if (sceneWidth / sceneHeight < maxWidth / maxHeight) {
                newScale = maxHeight / sceneHeight;
            }
            else {
                newScale = maxWidth / sceneWidth;
            }
            campaignManager.setScale(newScale);
            campaignManager.relayout();
        }
    }

    function resetScrolling() {
        var $bounder = $("#bounder");
        var $editorCanvas = $("#editorCanvas");

        // reset all scrolling, we don't need it for PDF
        $bounder.css("left", "15px");
        $bounder.css("top", $("div.icon-extra-info").css("top"));
        $editorCanvas.css("left", "0");
        $editorCanvas.css("top", "0");
    }

    this.addDomInitializer('workflow-pdf-initialize', function () {
        var data = this.config,
            WorkflowManagerStatistics,
            campaignManagerScale,
            sessionId = data.sessionId,
            locale = data.locale;
        allUsedEntity = setAllEntity(data);
        campaignManagerSettings.imagePath = data.imageUrl + "/campaignManager/";
        campaignManagerScale = new AGN.Lib.WM.CampaignManagerScale({campaignManagerSettings: campaignManagerSettings});

        AGN.Lib.CampaignManagerService = {
            icons: data.icons,
            resizeTimeoutId: false
        };

        campaignManager = new AGN.Lib.WM.CampaignManager({
            campaignManagerScale: campaignManagerScale,
            campaignManagerSettings: campaignManagerSettings,
            restoreSpaceFields: ["name", "workflow_description"],
            editorPositionLeft: parseInt(data.editorPositionLeft),
            editorPositionTop: parseInt(data.editorPositionTop),
            localeDateNTimePattern: data.localeDateNTimePattern,
            noContextMenu: data.noContextMenu,
            pageContextSessionId: sessionId,
            allUsedEntity:allUsedEntity,
            isPdfGenerating:true,
            workflowURL: constants.workflowURL,
            componentURL: constants.componentURL
        });
        editorsHelper = campaignManager.getEditorsHelper();
        WorkflowManagerStatistics = new AGN.Lib.WM.WorkflowManagerStatistics(campaignManager);

        jQuery.datepicker.setDefaults(jQuery.datepicker.regional[locale]);

        campaignManager.restoreWorkflow(AGN.Lib.CampaignManagerService.icons);
        campaignManager.updateWorkflowForPdf();

        if (data.showStatistics) {
            WorkflowManagerStatistics.toggleStatistics(data.workflowId);
        }
        window.status = 'initializerFinished';
    });


    this.addDomInitializer('campaign-manager-init', function ($e) {
        data = this.config;
        isActivated = data.isActivated;
        workflowStatus = data.workflowStatus;
        allUsedEntity = setAllEntity(data);

        AGN.Lib.CampaignManagerService = {
            icons: data.icons,
            resizeTimeoutId: false
        };

        // create campaign manager object
        campaignManagerSettings.imagePath = data.imageUrl + "/campaignManager/";
        var campaignManagerScale = new AGN.Lib.WM.CampaignManagerScale({campaignManagerSettings: campaignManagerSettings});

        campaignManager = new AGN.Lib.WM.CampaignManager({
            workflowId: data.workflowId,
            isActivated: data.isActivated,
            autoOptData: data.workflowAutoOptData,
            restoreSpaceFields: ["name", "workflow_description"],
            editorPositionLeft: parseInt(data.editorPositionLeft),
            editorPositionTop: parseInt(data.editorPositionTop),
            localeDateNTimePattern: data.localeDateNTimePattern,
            pageContextSessionId: data.pageContextSessionId,
            campaignManagerScale: campaignManagerScale,
            campaignManagerSettings: campaignManagerSettings,
            allUsedEntity: allUsedEntity,
            workflowURL: constants.workflowURL,
            componentURL: constants.componentURL
        });

        editorsHelper = campaignManager.getEditorsHelper();

        WorkflowManagerStatistics = new AGN.Lib.WM.WorkflowManagerStatistics(campaignManager);

        // set localization for datepickers
        jQuery.datepicker.setDefaults(jQuery.datepicker.regional[data.emmLocal]);

        if (data.newStatus === constants.statusActive) {
            campaignManager.setWorkflowManagerStateChangedCallback(function () {
                $('#workflow_active').prop('disabled', !campaignManager.isNodesFilled());
            });
        }

        // Load workflow schema (icons and connections).
        var icons = AGN.Lib.CampaignManagerService.icons;

        if (icons.length != undefined) {
            campaignManager.restoreWorkflow(icons);
        }

        //update UI elements according to the state of undo history stack
        campaignManager.setHistoryStackChangedCallback(function (isHistoryNotEmpty) {
            AGN.Lib.WM.CampaignManagerToolbar.setUndoAvailable(isHistoryNotEmpty);
        });

        campaignManager.setConnectionNotAllowedCallback(function () {
            workflowNotAllowedConnectionDialogHandler.showDialog();
        });

        campaignManager.setEditingNotAllowedCallback(function () {
            workflowNotAllowedEditingDialogHandler.showDialog();
        });

        //restore undo history
        campaignManager.setUndoHistoryDataForSubmission(data.workflowUndoHistoryData);


        jQuery("#legend-button-wrapper").on({
            mouseover: function () {
                if (campaignManager.getCurrentState() == campaignManager.STATE_WAITING) {
                    jQuery("#legend-dopdown").show();
                }
            },
            mouseout: function () {
                jQuery("#legend-dopdown").hide();
            }
        });

        for (var key in nodeFactory.reactionRegistry) {
            if (nodeFactory.reactionRegistry.hasOwnProperty(key)) {
                var item = "<div class='legend-dopdown-item'>"
                    + "<img src='" + data.imageUrl + "/campaignManager/" + nodeFactory.reactionRegistry[key].image + "'/></div>"
                    + "<div class='legend-dopdown-item-name'>"
                    + nodeFactory.reactionRegistry[key].name + "</div>";
                jQuery("#legend-dopdown").append(item);
            }
        }

        //we didn't find any possibility for implementing flexible size of CM only with CSS
        //that's why we decided to handle resize event and perform resizing manually
        jQuery(window).resize(function () {
            handleResizeCampaignEditor();
        });

        window.onbeforeunload = function(e) {
          if (campaignManager && campaignManager.hasUnsavedChanges()) {
            var message = t('grid.layout.leaveQuestion');
            e = e || window.event;
            if (e) {
              e.returnValue = message;
            }

            // For Safari
            return message;
          }
          AGN.Lib.Loader.show();
        };

    });

    this.addDomInitializer('start-editor-initializer', function ($e) {
        var startData = $e.find("#start-editor-data").json();

        startMailingSelector = new AGN.Lib.WM.MailingSelectorBase(startData.form, startData.container, startData.sessionId, startData.selectedName, startData.noMailingOption, editorsHelper);

        var startProfileFieldsTypes = startData.profileFields;
        var isBigData = startData.isBigData;

        editorsHelper.editors["start"] = {

            rulesNumber: 0,
            isStartEditor: false,
            timePattern: /^(\d{2}):(\d{2})$/,

            getTitle: function () {
                return this.isStartEditor ? t('workflow.start.title') : t('workflow.stop.title');
            },

            getStartStopDate: function(data) {
                data = data || editorsHelper.formToObject("startForm");

                var $form = $('form[name="startForm"]');

                var picker = null;
                if (this.isStartEditor) {
                    switch (data.startType) {
                        case constants.startTypeDate:
                            picker = $form.find('#startDate').pickadate('picker');
                            break;
                        case constants.startTypeEvent:
                            picker = $form.find('#executionDate').pickadate('picker');
                            break;
                    }
                } else if (data.endType == constants.endTypeDate) {
                    picker = $form.find('#startDate').pickadate('picker');
                }

                if (picker) {
                    var select = picker.get("select");
                    if (select) {
                        return select.obj;
                    }
                }

                return null;
            },

            generateReminderComment: function() {
                var name = $("#workflowForm input[name='workflow.shortname']").val();
                var dateString = AGN.Lib.WM.DateTimeUtils.getDateStr(this.getStartStopDate(), startData.localeDatePattern);

                return (this.isStartEditor ? t('workflow.start.reminder_text') : t('workflow.stop.reminder_text'))
                    .replace(/:campaignName/g, name)
                    .replace(/:startDate/g, dateString)
                    .replace(/:endDate/g, dateString);
            },

            fillEditor: function (node) {
                var data = node.data;
                this.isStartEditor = (node.type == "start");
                this.updateEditorType(this.isStartEditor);

                var editorForm = $("form[name='startForm']");

                editorForm.submit(false);
                editorForm.find(".rule-row").each(function () {
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

                editorsHelper.fillFormFromObject("startForm", data, "");
                startMailingSelector.setMailingId(data.mailingId);
                startMailingSelector.cleanOptions(1);

                //init datepickers
                var currentDate = new Date();
                editorForm.find("#startDate").pickadate('picker').set('select', this.dateAsUTC(data.date != undefined ? data.date : currentDate));
                editorForm.find("#executionDate").pickadate('picker').set('select', this.dateAsUTC(data.date != undefined ? data.date : currentDate));
                editorForm.find("#remindDate").pickadate('picker').set('select', this.dateAsUTC(data.remindDate != undefined ? data.remindDate : currentDate));

                //init timepickers
                if (data.date != undefined) {
                    editorForm.find("#startTime").val(this.formatTime(data.hour, data.minute));
                    editorForm.find("#remindTime").val(this.formatTime(data.remindHour, data.remindMinute));
                } else {
                    this.setCurrentTime();
                }

                this.updateChainFieldsVisibility();
                this.updateVisibility();
                this.updateOperatorsAvailability();

                // update mailing links in editor
                if (data.startType == constants.startTypeEvent && data.event == constants.startEventReaction && data.reaction == constants.reactionClickedLink
                    && data.mailingId != 0 && data.mailingId != null) {
                    this.onMailingSelectChange(data.mailingId, data.linkId);
                }

                // init reminder comment
                if (!data.comment) {
                    editorForm.find('#reminderComment').val(this.generateReminderComment());
                }

                // init reminder user type
                var userType;
                if (data.recipients != "") {
                    userType = 2;
                } else {
                    userType = 1;
                }
                $("#start-editor input[name='userType'][value=" + userType + "]").prop("checked", true).trigger("change");

                $("#start-editor .editor-error-messages").css("display", "none");
            },

            saveEditor: function () {
                var data = editorsHelper.formToObject("startForm");

                data.date = this.getStartStopDate(data);

                if (data.date == null) {
                    data.date = new Date();
                    data.date.setHours(0);
                    data.date.setMinutes(0);
                    data.date.setSeconds(0);
                    data.date.setMilliseconds(0);
                }

                var match;

                var startTime = jQuery("form[name='startForm'] #startTime").val();
                if (match = this.timePattern.exec(startTime)) {
                    data.hour = match[1];
                    data.minute = match[2];
                } else {
                    data.hour = 0;
                    data.minute = 0;
                }

                data.scheduleReminder = data.sendReminder;

                var remindTime = jQuery("form[name='startForm'] #remindTime").val();
                if (match = this.timePattern.exec(remindTime)) {
                    data.remindHour = match[1];
                    data.remindMinute = match[2];
                } else {
                    data.remindHour = 0;
                    data.remindMinute = 0;
                }

                if (jQuery("input[name='remindSpecificDate']:checked").val() == "true") {
                    data.remindDate = jQuery("form[name='startForm'] #remindDate").pickadate('picker').get('select').obj;
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
                        .filter(function(address) { return !!address; })
                        .join(', ');
                } else {
                    data.recipients = "";
                }
                delete data.userType;

                return data;
            },

            validateEditorInternal: function (showErrors) {
                var self = this;
                var valid = true;

                var messageView = $("#start-editor .editor-error-messages");
                var editorForm = $("form[name='startForm']");
                var type = this.isStartEditor ?
                    editorForm.find("input[name='startType']:checked").val() :
                    editorForm.find("input[name='endType']:checked").val();

                if (this.isStartEditor && type == constants.startTypeEvent) {
                    validateStartEvent();
                    validateStartDate();
                }

                if ((this.isStartEditor && type == constants.startTypeDate)
                    || (!this.isStartEditor && type == constants.endTypeDate)) {
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
                    var event = editorForm.find("select[name='event']").val();
                    switch (event) {
                        case constants.startTypeDate:
                            var executionDate = editorForm.find("#executionDate").pickadate('picker').get('select').obj;
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
                                    messageView.css("display", "block");
                                }
                            }
                            break;

                        case constants.startEventReaction:
                            var reaction = editorForm.find("select[name='reaction']").val();
                            switch (reaction) {
                                case constants.reactionClickedLink:
                                    var linkId = editorForm.find("select[name='linkId']").val();
                                    if (parseInt(linkId, 10) <= 0) {
                                        valid = false;
                                        if (showErrors) {
                                            messageView.html(t('error.workflow.noLinkSelected'));
                                            messageView.css("display", "block");
                                        }
                                    }
                                // Fall-through
                                case constants.reactionOpened:
                                case constants.reactionClicked:
                                    var mailingId = editorForm.find("select[name='mailingId']").val();
                                    if (parseInt(mailingId, 10) <= 0) {
                                        valid = false;
                                    }
                                    break;
                                case constants.reactionChangeOfProfile:
                                    if (typeof(isBigData) != "undefined" && isBigData == true) {

                                        var profileField = editorForm.find("select[name='profileField']").val();
                                        if (!profileField) {
                                            valid = false;
                                        }

                                    }
                                    break;

                                case constants.reactionWaitingForConfirm:
                                case constants.reactionOptIn:
                                case constants.reactionOptOut:
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
                    var startDate = editorForm.find("#startDate").pickadate('picker').get('select').obj;
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
                        .filter(function(address) { return !!address });

                    if (!emails.length) {
                        if (showErrors) {
                            valid = false;
                            messageView.html(t('error.workflow.emptyRecipientList'));
                            messageView.css('display', 'block');
                        }
                    }
                }
            },

            validateEditor: function () {
                if (this.validateEditorInternal(true)) {
                    editorsHelper.saveCurrentEditor();
                }
            },

            isSetFilledAllowed: function () {
                return this.validateEditorInternal(false);
            },

            setCurrentTime: function () {
                var self = this;
                jQuery.ajax({
                    type: "GET",
                    url: AGN.url('/workflow/getCurrentAdminTime.action'),
                    success: function (data) {
                        jQuery("form[name='startForm'] #startTime").val(self.formatTime(data.hour, data.minute));
                        jQuery("form[name='startForm'] #remindTime").val(self.formatTime(data.remindHour, data.remindMinute));
                    }
                });
            },

            updateEditorType: function (isStartEditor) {
                $('#startStopType').empty();
                if (isStartEditor) {
                    $('#startStopType').append(AGN.Lib.Template.text('start-types'));
                    jQuery("#eventReaction").html(t('workflow.start.reaction_based'));
                    jQuery("#eventDate").html(t('workflow.start.date_based'));
                }
                else {
                    $('#startStopType').append(AGN.Lib.Template.text('stop-types'));
                    if (campaignManager.isNormalCampaignActionsType()) {
                        jQuery("#endTypeActiomaticLabel").html(t('workflow.stop.automatic_end'));
                    } else {
                        jQuery("#endTypeActiomaticLabel").html(t('workflow.stop.open_end'));
                    }
                }
            },

            onRulesChanged: function () {
                var ruleIndex = 0;
                $("form[name='startForm'] #profileFieldAddedRules tr").each(function() {
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

            updateChainFieldsVisibility: function () {
                if (this.rulesNumber > 0) {
                    jQuery('form[name="startForm"] [name="rules[0].chainOperator"]').css("visibility", "hidden");
                    jQuery('form[name="startForm"] #newRule_chainOperator').css("visibility", "visible");
                }
                else {
                    jQuery("form[name='startForm'] #newRule_chainOperator").css("visibility", "hidden");
                }
            },

            updateOperatorsAvailability: function () {
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
                        editorsHelper.initSelectWithFirstValue($select);
                    }
                });
            },

            addRule: function () {
                var rule = {
                    chainOperator: jQuery("form[name='startForm'] #newRule_chainOperator").val(),
                    parenthesisOpened: jQuery("form[name='startForm'] #newRule_parenthesisOpened").val(),
                    primaryOperator: jQuery("form[name='startForm'] #newRule_primaryOperator").val(),
                    primaryValue: jQuery("form[name='startForm'] #newRule_primaryValue").val(),
                    parenthesisClosed: jQuery("form[name='startForm'] #newRule_parenthesisClosed").val()
                };
                this.createRuleRow(this.rulesNumber, rule);
                editorsHelper.fillFormFromObject("startForm", rule, "rules[" + this.rulesNumber + "].");
                this.rulesNumber++;

                if ($('#newRule_primaryOperator').val() == constants.operatorIs) {
                    $('#newRule_primaryValue').parents('td').replaceWith(this.createSimpleNewRuleValueField());
                }
                this.updateChainFieldsVisibility();
                this.updateOperatorsAvailability();
                $("form[name='startForm'] #profileFieldRuleAdd select").each(function () {
                    editorsHelper.initSelectWithFirstValue($(this));
                });
                $("form[name='startForm'] #profileFieldRuleAdd input").each(function () {
                    $(this).val("");
                });
            },

            removeRuleRow: function (index) {
                jQuery("form[name='startForm'] #rule_" + index).remove();
                this.rulesNumber--;
                this.onRulesChanged();
            },

            createRuleRow: function (ruleIndex, rule) {
                var valueElement = this.createSimpleRuleValueField(ruleIndex);
                var options = arrayAsOptionsHtml(constants.operators);
                if (this.curProfileField == GENDER_PROFILE_FIELD) {
                    valueElement = this.createGenderSelect();
                }
                else if (rule.primaryOperator == constants.operatorIs) {
                    valueElement = this.createIsOperatorSelect();
                }
                jQuery("form[name='startForm'] #profileFieldAddedRules").append(
                    '<tr class="rule-row" id="rule_' + ruleIndex + '">' +
                    '<td><select name="rules[' + ruleIndex + '].chainOperator" class="rule-field">' +
                    '<option value="' + constants.chainOperatorAnd + '">' + t('workflow.defaults.and') + '</option>' +
                    '<option value="' + constants.chainOperatorOr + '">' + t('workflow.defaults.or') + '</option>' +
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
                this.addRuleProperties(jQuery("#rule_" + ruleIndex + " .primary-value"), ruleIndex);

                AGN.runAll($("form[name='startForm'] #profileFieldAddedRules"));
            },

            // functions handling visibility of different parts of dialog according to selected settings

            onStartTypeChanged: function () {
                var value = this.isStartEditor ?
                    jQuery("form[name='startForm'] input[name='startType']:checked").val() :
                    jQuery("form[name='startForm'] input[name='endType']:checked").val();
                if ((this.isStartEditor && value == constants.startTypeDate)
                    || (!this.isStartEditor && value == constants.endTypeDate)) {
                    this.toggleFormat();
                    this.hideDiv("startEventPanel");
                    this.showDiv("startDatePanel");
                    this.showDiv("startIconTime");
                    this.showDiv("startRemindAdmin");
                }
                else if (this.isStartEditor && value == constants.startTypeEvent) {
                    this.hideDiv("startDatePanel");
                    this.showDiv("startEventPanel");
                    this.showDiv("startIconTime");
                    this.showDiv("startRemindAdmin");
                    this.onStartEventChanged();
                }
                else if ((this.isStartEditor && value == constants.startTypeOpen)
                    || (!this.isStartEditor && value == constants.endTypeAutomatic)) {
                    this.hideDiv("startDatePanel");
                    this.hideDiv("startEventPanel");
                    this.hideDiv("startIconTime");
                    this.hideDiv("startRemindAdmin");
                }
            },

            onStartEventChanged: function () {
                if (jQuery("#startEvent").val() == constants.startEventReaction) {
                    this.toggleFormat();
                    this.hideDiv("dateStartPanel");
                    this.showDiv("reactionStartPanel");
                    this.onExecutionChanged();
                    this.onProfileFieldChanged();
                }
                else if (jQuery("#startEvent").val() == constants.startEventDate) {
                    this.toggleFormat();
                    this.hideDiv("reactionStartPanel");
                    this.showDiv("dateStartPanel");
                    this.hideDiv("executionDateLabel");
                    this.showDiv("firstExecutionDateLabel");
                    jQuery("#startIconDateFormat").insertAfter("#startIconDateFieldOperator");
                    this.showDiv("startIconDateFormat");
                }
            },

            toggleFormat: function () {
                var timeInput, typeDateInput,
                    startEventInput, checked, isDateBased;
                return function () {
                    startEventInput = startEventInput || $('select#startEvent');
                    typeDateInput = typeDateInput || $('input#typeEvent');
                    timeInput = timeInput || $('input#startTime');
                    checked = typeDateInput.prop('checked');
                    isDateBased = startEventInput.val() == constants.startEventDate;
                    (typeDateInput.prop('checked') && isDateBased) ? timeInput.inputmask('h:00') : timeInput.inputmask('h:s');
                }
            }(),

            onRuleModeChanged: function () {
                var value = $("input[name='useRules']:checked").val();
                if (value == "false") {
                    this.hideDiv("profileFieldRules");
                }
                else {
                    this.showDiv("profileFieldRules");
                }
            },

            onProfileFieldChanged: function () {
                var profileField = $("form[name='startForm'] select[name='profileField']").val();
                var fieldType = startProfileFieldsTypes[profileField];
                if (fieldType == "DATE") {
                    $("#startIconDateFormat").insertAfter("#reactionProfileField");
                    this.showDiv("startIconDateFormat");
                }
                else {
                    this.hideDiv("startIconDateFormat");
                }
                this.updateOperatorsAvailability();
            },

            getRuleIndex: function ($element) {
                return $element.attr('id').substring(5);
            },

            onRuleOperatorChanged: function ($element) {
                if (this.curProfileField == GENDER_PROFILE_FIELD) {
                    return;
                }

                var $parent = $element.closest('tr');
                var index = $parent.attr('id') ? this.getRuleIndex($parent) : null;

                if ($element.val() == constants.operatorIs) {
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
                    this.addNewRuleProperties($parent.find(".primary-value"));
                } else {
                    this.addRuleProperties($parent.find(".primary-value"), index);
                }
            },

            createGenderSelect: function () {
                var options = mapAsOptionsHtml(constants.genderOptions);
                return '<td><select class="primary-value form-control">' + options + '</select></td>';
            },

            createIsOperatorSelect: function () {
                return '<td><select class="primary-value null-select form-control">' +
                    '<option value="NULL">NULL</option>' +
                    '<option value="NOT_NULL">NOT NULL</option>' +
                    '</select></td>';
            },

            createSimpleRuleValueField: function (ruleIndex) {
                return '<td><input name="rules[' + ruleIndex + '].primaryValue" type="text" class="primary-value rule-field form-control"/></td>';
            },

            createSimpleNewRuleValueField: function () {
                return '<td><input id="newRule_primaryValue" type="text" class="primary-value form-control"/></td>';
            },


            addRuleProperties: function ($element, ruleIndex) {
                $element.filter('select, input')
                    .attr('name', 'rules[' + ruleIndex + '].primaryValue')
                    .addClass('rule-field');
            },

            addNewRuleProperties: function ($element) {
                $element.filter('select, input')
                    .attr("id", "newRule_primaryValue");
            },

            onExecutionChanged: function () {
                var value = $("input[name='executeOnce']:checked").val();
                if (value == "false") {
                    this.hideDiv("executionDateLabel");
                    this.showDiv("firstExecutionDateLabel");
                }
                else {
                    this.hideDiv("firstExecutionDateLabel");
                    this.showDiv("executionDateLabel");
                }
            },

            onReactionChanged: function () {
                var selectedReaction = $("form[name='startForm'] #startReaction").val();
                if (selectedReaction == constants.reactionOpened
                    || selectedReaction == constants.reactionNotOpened
                    || selectedReaction == constants.reactionClicked
                    || selectedReaction == constants.reactionNotClicked
                    || selectedReaction == constants.reactionBought
                    || selectedReaction == constants.reactionNotBought
                    || selectedReaction == constants.reactionClickedLink) {
                    this.hideDiv("reactionStartProfile");
                    this.showDiv("reactionStartMailing");
                }
                else if (selectedReaction == constants.reactionChangeOfProfile) {
                    this.hideDiv("reactionStartMailing");
                    this.showDiv("reactionStartProfile");
                }
                else {
                    this.hideDiv("reactionStartMailing");
                    this.hideDiv("reactionStartProfile");
                }
                if (selectedReaction == constants.reactionClickedLink) {
                    this.showDiv("reactionStartMailingLink");
                }
                else {
                    this.hideDiv("reactionStartMailingLink");
                }
            },

            onReminderChanged: function () {
                if ($("input[name='sendReminder']:checked").val()) {
                    $('form[name="startForm"]')
                        .find('#reminderComment')
                        .val(this.generateReminderComment());

                    this.showDiv("reminderDetails");
                } else {
                    this.hideDiv("reminderDetails");
                }
                return false;
            },

            onScheduleReminderDateChanged: function () {
                if ($("form[name='startForm'] input[name='remindSpecificDate']:checked").val() == "true") {
                    this.showDiv("dateTimePicker");
                }
                else {
                    this.hideDiv("dateTimePicker");
                }
                return false;
            },

            onMailingSelectChange: function (value, selectedValue) {
                var $linkSelect = $("form[name='startForm'] select[name='linkId']");
                var mailingId = parseInt(value, 10);

                if (mailingId > 0) {
                    $linkSelect.attr("readonly", "readonly");
                    jQuery.ajax({
                        type: "POST",
                        url: AGN.url('/workflow/getMailingLinks.action'),
                        data: {
                            mailingId: mailingId
                        },
                        success: function (data) {
                            // populate the drop-down list with mailing links
                            $linkSelect.empty();

                            jQuery.each(data, function (index, itemUrl) {
                                $linkSelect.append(jQuery('<option></option>', {value: itemUrl.id, text: itemUrl.url}));
                            });

                            $linkSelect.removeAttr("readonly");

                            if (selectedValue != null && selectedValue != undefined) {
                                $linkSelect.val(selectedValue);
                                editorsHelper.initSelectWithValueOrChooseFirst($linkSelect, selectedValue);
                            } else {
                                editorsHelper.initSelectWithFirstValue($linkSelect);
                            }
                        }
                    });
                } else {
                    $linkSelect.empty();
                }
            },

            updateVisibility: function () {
                this.onStartTypeChanged();
                this.onStartEventChanged();
                this.onReactionChanged();
                this.onRuleModeChanged();
                this.onExecutionChanged();
                this.onReminderChanged();
                this.onScheduleReminderDateChanged();
            },

            hideDiv: function (id) {
                jQuery("form[name='startForm'] #" + id).css("display", "none");
            },

            showDiv: function (id) {
                jQuery("form[name='startForm'] #" + id).css("display", "");
            },

            // Date picker uses a date in UTC but JS uses a local timezone so we need to workaround that
            dateAsUTC: function (date) {
                if (date) {
                    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
                } else {
                    return null;
                }
            },

            formatTime: function (hours, minutes) {
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

                return (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m);
            }
        };

        startEditor = editorsHelper.editors["start"];
        editorsHelper.editors["stop"] = editorsHelper.editors["start"];
    });

    this.addDomInitializer('decision-editor-initializer', function ($e) {
        var decisionData = $e.find("#decision-editor-data").json();
        var decisionProfileFieldsTypes = decisionData.profileFields;
        decisionMailingSelector = new AGN.Lib.WM.MailingSelectorBase(decisionData.form, decisionData.container, decisionData.sessionId, decisionData.selectName, decisionData.noMailingOption, editorsHelper);

        // Field is used to set correct mailingID if "Profile field" selected as decision criteria.
        var profileFieldMailingId;


        editorsHelper.editors["decision"] = {

            rulesNumber: 0,
            rulePrefix: "decision_",
            prevProfileField: "",
            curProfileField: "",

            getTitle: function () {
                return t('workflow.decision');
            },

            fillEditor: function (node) {
                // Field is used to set correct mailingID if "Profile field" selected as decision criteria.
                profileFieldMailingId = 0;

                var data = node.data;

                jQuery("form[name='decisionForm']").submit(false);
                jQuery("form[name='decisionForm'] .rule-row").each(function () {
                    jQuery(this).remove();
                });
                this.curProfileField = data.profileField;
                if (data.rules != undefined) {
                    for (var i = 0; i < data.rules.length; i++) {
                        var rule = data.rules[i];
                        this.createRuleRow(i, rule);
                    }
                    this.rulesNumber = data.rules.length;
                }
                jQuery("form[name='decisionForm']").get(0).reset();
                editorsHelper.fillFormFromObject("decisionForm", data, "");

                decisionMailingSelector.setMailingId(data.mailingId);
                decisionMailingSelector.cleanOptions(1);

                if (data.decisionDate == undefined) {
                    data.decisionDate = new Date();
                }

                var decisionDatePicker = jQuery("form[name='decisionForm'] #decisionDate");
                var decisionTimePicker = jQuery("form[name='decisionForm'] #decisionTime");

                if (this.checkPresentNodesByTypeList([nodeFactory.NODE_TYPE_ACTION_BASED_MAILING, nodeFactory.NODE_TYPE_DATE_BASED_MAILING])) {
                    decisionDatePicker.parent().parent().hide();
                    decisionTimePicker.parent().parent().parent().parent().hide();
                } else {
                    decisionDatePicker.parent().parent().show();
                    decisionTimePicker.parent().parent().parent().parent().show();

                    // init date and time picker
                    decisionDatePicker.pickadate('picker').set('select', this.dateAsUTC(data.decisionDate));
                    decisionTimePicker.val(("0" + data.decisionDate.getHours()).slice(-2) + ":" + ("0" + data.decisionDate.getMinutes()).slice(-2));
                }

                this.updateChainFieldsVisibility();
                this.updateVisibility();
                this.updateDateFormatVisibility();
                this.updateOperatorsAvailability();

                // update mailing links in editor
                if (data.decisionType == constants.decisionTypeDecision && data.decisionCriteria == constants.decisionReaction &&
                    data.reaction == constants.reactionClickedLink && (data.mailingId != 0 && data.mailingId != null)) {
                    this.onMailingSelectChange(data.mailingId, data.linkId);
                } else {
                    editorsHelper.resetSelect(jQuery("form[name='decisionForm'] select[name='linkId']"));
                }

                jQuery("#decision-editor .editor-error-messages").css("display", "none");
            },

            saveEditor: function () {
                var data = editorsHelper.formToObject("decisionForm");

                //We should use ID of previous mailing if "Profile field" selected in decision criteria,
                // not mailing ID from Mailing dropdown of decision form.
                if (data.decisionCriteria == constants.decisionProfileField && profileFieldMailingId != 0) {
                    data.mailingId = profileFieldMailingId;
                }
                //Field reset.
                profileFieldMailingId = 0;

                if (data.rules == undefined) {
                    data.rules = [];
                }
                if (!data.threshold) {
                    data.threshold = "";
                }

                if (this.checkPresentNodesByTypeList([nodeFactory.NODE_TYPE_ACTION_BASED_MAILING, nodeFactory.NODE_TYPE_DATE_BASED_MAILING])) {
                    data.decisionDate = null;
                } else {
                    data.decisionDate = jQuery("form[name='decisionForm'] #decisionDate").pickadate('picker').get('select').obj;

                    var time = jQuery("form[name='decisionForm'] #decisionTime").val();
                    data.decisionDate.setHours(time.substring(0, 2));
                    data.decisionDate.setMinutes(time.substring(3, 5));
                }
                return data;
            },

            validateEditor: function () {
                var decisionType = jQuery("form[name='decisionForm'] input[name='decisionType']:checked").val();
                var decisionCriteria = jQuery("#decisionCriteria").val();
                var mailingId = jQuery("form[name='decisionForm'] select[name='mailingId']").val();
                var linkId = jQuery("form[name='decisionForm'] select[name='linkId']").val();
                var reaction = jQuery("form[name='decisionForm'] select[name='reaction']").val();
                var threshold = jQuery("form[name='decisionForm'] input[name='threshold']").val();

                if (decisionType == constants.decisionTypeDecision && decisionCriteria == constants.decisionReaction && mailingId == 0) {
                    jQuery("#decision-editor .editor-error-messages").html(t('error.workflow.noMailing'));
                    jQuery("#decision-editor .editor-error-messages").css("display", "block");
                }
                else if (decisionType == constants.decisionTypeDecision && decisionCriteria == constants.decisionReaction && reaction == constants.reactionClickedLink
                    && (linkId == 0 || linkId == null)) {
                    jQuery("#decision-editor .editor-error-messages").html(t('error.workflow.noLinkSelected'));
                    jQuery("#decision-editor .editor-error-messages").css("display", "block");
                }
                else if (decisionType == constants.decisionTypeDecision && decisionCriteria == constants.decisionProfileField && decisionData.isMailtrackingActive == false) {
                    // Do nothing
                }
                else if (decisionType == constants.decisionTypeAutoOptimization) {
                    if (threshold) {
                        var thresholdIntValue = parseInt(threshold, 10);
                        if (thresholdIntValue && thresholdIntValue > 0) {
                            editorsHelper.saveCurrentEditor();
                        } else {
                            jQuery("#decision-editor .editor-error-messages").html(t('error.workflow.noValidThreshold'));
                            jQuery("#decision-editor .editor-error-messages").css("display", "block");
                        }
                    } else {
                        // Value is omitted (input field is empty)
                        editorsHelper.saveCurrentEditor();
                    }
                }
                else {
                    editorsHelper.saveCurrentEditor();
                }
            },

            onTypeChanged: function () {
                var value = jQuery("form[name='decisionForm'] input[name='decisionType']:checked").val();
                if (value == constants.decisionTypeDecision) {
                    this.hideDecisionFormDiv("autoOptimizationPanel");
                    this.showDecisionFormDiv("decisionPanel");
                }
                else if (value == constants.decisionTypeAutoOptimization) {
                    this.hideDecisionFormDiv("decisionPanel");
                    this.showDecisionFormDiv("autoOptimizationPanel");
                }
                this.updateVisibilityOfRuleMailingReceived();
            },

            onDecisionReactionChanged: function () {
                var value = jQuery("form[name='decisionForm'] select[name='reaction']").val();
                if (value == constants.reactionClickedLink) {
                    this.showDecisionFormDiv("reactionLinkPanel");
                    var mailingId = jQuery("form[name='decisionForm'] select[name='mailingId']").val();
                    this.onMailingSelectChange(mailingId, 0);
                }
                else {
                    this.hideDecisionFormDiv("reactionLinkPanel");
                }
            },

            onCriteriaChanged: function () {
                var value = jQuery("form[name='decisionForm'] select[name='decisionCriteria']").val();
                if (value == constants.decisionReaction) {
                    this.hideDecisionFormDiv("decisionProfileFieldPanel");
                    this.showDecisionFormDiv("decisionReactionPanel");
                }
                else if (value == constants.decisionProfileField) {
                    this.hideDecisionFormDiv("decisionReactionPanel");
                    this.showDecisionFormDiv("decisionProfileFieldPanel");
                }
                this.updateVisibilityOfRuleMailingReceived();
            },

            onMailingSelectChange: function (value, selectedValue) {
                var $linkSelect = $("form[name='decisionForm'] select[name='linkId']");
                var mailingId = parseInt(value, 10);

                if (mailingId > 0) {
                    $linkSelect.attr("readonly", "readonly");
                    jQuery.ajax({
                        type: "POST",
                        url: AGN.url('/workflow/getMailingLinks.action'),
                        data: {
                            mailingId: mailingId
                        },
                        success: function (data) {
                            // populate the drop-down list with mailing links
                            $linkSelect.empty();

                            jQuery.each(data, function (index, itemUrl) {
                                $linkSelect.append(jQuery('<option></option>', {value: itemUrl.id, text: itemUrl.url}));
                            });

                            $linkSelect.removeAttr("readonly");

                            if (selectedValue != null && selectedValue != undefined) {
                                $linkSelect.val(selectedValue);
                                editorsHelper.initSelectWithValueOrChooseFirst($linkSelect, selectedValue);
                            } else {
                                editorsHelper.initSelectWithFirstValue($linkSelect);
                            }
                        }
                    });
                } else {
                    $linkSelect.empty();
                }
            },

            onRuleOperatorChanged: function ($element) {
                if (this.curProfileField == GENDER_PROFILE_FIELD) {
                    return;
                }

                var $parent = $element.closest('tr');
                var index = $parent.attr('id') ? this.getRuleIndex($parent) : null;

                if ($element.val() == constants.operatorIs) {
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
                    this.addNewRuleProperties($parent.find(".primary-value"));
                } else {
                    this.addRuleProperties($parent.find(".primary-value"), index);
                }
            },

            getRuleIndex: function ($element) {
                return $element.attr('id').substring(14);
            },

            onProfileFieldChanged: function () {
                var profileField = jQuery("form[name='decisionForm'] select[name='profileField']").val();
                this.curProfileField = profileField;
                var self = this;

                // if the field gender is selected - we need to replace all value fields with gender dropdown
                if (profileField == GENDER_PROFILE_FIELD) {
                    // remove all primary-value controls
                    jQuery("#decisionProfileFieldRules .primary-value").each(function () {
                        jQuery(this).parents('td').remove();
                    });
                    // add gender-select for existing rules
                    jQuery("form[name='decisionForm'] .rule-row").each(function () {
                        var index = self.getRuleIndex(jQuery(this));
                        jQuery(this).find(".decision-rule-operator").parents('td').after(self.createGenderSelect());
                        self.addRuleProperties(jQuery(this).find(".primary-value"), index);
                    });
                    // add gender-select for new-rule control
                    jQuery("#decision_newRule_primaryOperator").parents('td').after(this.createGenderSelect());
                    this.addNewRuleProperties(jQuery("#decisionProfileFieldRuleAdd .primary-value"));
                }

                // if it's not gender selected and previous selection was gender - we need to replace gender drop-downs with fields
                else if (this.prevProfileField == GENDER_PROFILE_FIELD) {
                    jQuery("#decisionProfileFieldRules .primary-value").each(function () {
                        jQuery(this).parents('td').remove();
                    });
                    jQuery("form[name='decisionForm'] .rule-row").each(function () {
                        var index = self.getRuleIndex(jQuery(this));
                        if (jQuery(this).find(".decision-rule-operator").val() == constants.operatorIs) {
                            jQuery(this).find(".primary-value").parents('td').remove();
                            jQuery(this).find("select").parents('td').after(self.createIsOperatorSelect());
                            self.addRuleProperties(jQuery(this).find(".primary-value"), index);
                        }
                        else {
                            jQuery(this).find(".decision-rule-operator").parents('td').after(self.createSimpleRuleValueField(index))
                        }
                    });

                    if (jQuery("#decision_newRule_primaryOperator").val() == constants.operatorIs) {
                        jQuery("#decisionProfileFieldRuleAdd").find("select").parents('td').after(this.createIsOperatorSelect());
                        this.addNewRuleProperties(jQuery("#decisionProfileFieldRuleAdd .primaryValue"));
                    }
                    else {
                        jQuery("#decision_newRule_primaryOperator").parents('td').after(this.createSimpleNewRuleValueField());
                    }
                }

                this.updateDateFormatVisibility();
                this.updateOperatorsAvailability();

                this.prevProfileField = profileField;
            },

            updateDateFormatVisibility: function () {
                var profileField = jQuery("form[name='decisionForm'] select[name='profileField']").val();
                var fieldType = decisionProfileFieldsTypes[profileField];
                if (fieldType == "DATE") {
                    this.showDecisionFormDiv("decisionDateFormat");
                }
                else {
                    this.hideDecisionFormDiv("decisionDateFormat");
                }
            },

            updateOperatorsAvailability: function () {
                var profileField = $('form[name="decisionForm"] select[name="profileField"]').val();
                var fieldType = decisionProfileFieldsTypes[profileField];

                $('form[name="decisionForm"] select.decision-rule-operator').each(function() {
                    var $select = $(this);

                    $select.children('option').prop('disabled', function() {
                        var types = $(this).data('types');

                        if(profileField == GENDER_PROFILE_FIELD) {
                            return this.text != '=' && this.text != '!=';
                        }

                        if (types == '*' || !types) {
                            return false;
                        }

                        return types.split(/[\s,]+/).indexOf(fieldType) == -1;
                    });

                    AGN.Lib.CoreInitializer.run('select', $select);

                    if ($select.val() == null) {
                        editorsHelper.initSelectWithFirstValue($select);
                    }
                });
            },

            onRulesChanged: function () {
                var ruleIndex = 0;
                var self = this;
                $("form[name='decisionForm'] #decisionProfileFieldAddedRules .rule-row").each(function() {
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

            updateChainFieldsVisibility: function () {
                if (this.rulesNumber > 0) {
                    jQuery('form[name="decisionForm"] [name="rules[0].chainOperator"]').css("visibility", "hidden");
                    jQuery('form[name="decisionForm"] #decision_newRule_chainOperator').css("visibility", "visible");
                }
                else {
                    jQuery("form[name='decisionForm'] #decision_newRule_chainOperator").css("visibility", "hidden");
                }
            },

            addRule: function () {
                var rule = {
                    chainOperator: $("form[name='decisionForm'] #decision_newRule_chainOperator").val(),
                    parenthesisOpened: $("form[name='decisionForm'] #decision_newRule_parenthesisOpened").val(),
                    primaryOperator: $("form[name='decisionForm'] #decision_newRule_primaryOperator").val(),
                    primaryValue: $("form[name='decisionForm'] #decision_newRule_primaryValue").val(),
                    parenthesisClosed: $("form[name='decisionForm'] #decision_newRule_parenthesisClosed").val()
                };
                this.createRuleRow(this.rulesNumber, rule);
                editorsHelper.fillFormFromObject("decisionForm", rule, "rules[" + this.rulesNumber + "].");
                this.rulesNumber++;

                if ($('#decision_newRule_primaryOperator').val() == constants.operatorIs) {
                    $('#decision_newRule_primaryValue').parents('td').replaceWith(this.createSimpleNewRuleValueField());
                }
                this.updateChainFieldsVisibility();
                this.updateOperatorsAvailability();
                $("form[name='decisionForm'] #decisionProfileFieldRuleAdd select").each(function () {
                    editorsHelper.initSelectWithFirstValue($(this));
                });
                $("form[name='decisionForm'] #decisionProfileFieldRuleAdd input").each(function () {
                    $(this).val("");
                });
            },

            removeRuleRow: function (index) {
                jQuery("form[name='decisionForm'] #decision_rule_" + index).remove();
                this.rulesNumber--;
                this.onRulesChanged();
            },

            createRuleRow: function (ruleIndex, rule) {
                var valueElement = this.createSimpleRuleValueField(ruleIndex);
                var options = arrayAsOptionsHtml(constants.operators);
                if (this.curProfileField == GENDER_PROFILE_FIELD) {
                    valueElement = this.createGenderSelect();
                }
                else if (rule.primaryOperator == constants.operatorIs) {
                    valueElement = this.createIsOperatorSelect();
                }
                jQuery("form[name='decisionForm'] #decisionProfileFieldAddedRules").append(
                    '<tr class="rule-row" id="decision_rule_' + ruleIndex + '">' +
                    '<td><select name="rules[' + ruleIndex + '].chainOperator" class="rule-field">' +
                    '<option value="' + constants.chainOperatorAnd + '">' + t('workflow.defaults.and') + '</option>' +
                    '<option value="' + constants.chainOperatorOr + '">' + t('workflow.defaults.or') + '</option>' +
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
                this.addRuleProperties(jQuery("#decision_rule_" + ruleIndex + " .primary-value"), ruleIndex);

                AGN.runAll($("form[name='decisionForm'] #decisionProfileFieldAddedRules"));
            },

            createGenderSelect: function () {
                var options = mapAsOptionsHtml(constants.genderOptions);
                return '<td><select class="primary-value form-control">' + options + '</select></td>';
            },

            createIsOperatorSelect: function () {
                return '<td><select class="primary-value null-select form-control">' +
                    '<option value="NULL">NULL</option>' +
                    '<option value="NOT_NULL">NOT NULL</option>' +
                    '</select></td>';
            },

            createSimpleRuleValueField: function (ruleIndex) {
                return '<td><input name="rules[' + ruleIndex + '].primaryValue" type="text" class="primary-value rule-field form-control"/></td>';
            },

            createSimpleNewRuleValueField: function () {
                return '<td><input id="decision_newRule_primaryValue" type="text" class="primary-value form-control"/></td>';
            },

            addRuleProperties: function ($element, ruleIndex) {
                $element.filter('select, input')
                    .attr('name', 'rules[' + ruleIndex + '].primaryValue')
                    .addClass('rule-field');
            },

            addNewRuleProperties: function ($element) {
                $element.filter('select, input')
                    .attr('id', 'decision_newRule_primaryValue');
            },

            updateVisibility: function () {
                this.onTypeChanged();
                this.onDecisionReactionChanged();
                this.onCriteriaChanged();
            },

            hideDecisionFormDiv: function (id) {
                jQuery("form[name='decisionForm'] #" + id).css("display", "none");
            },

            showDecisionFormDiv: function (id) {
                jQuery("form[name='decisionForm'] #" + id).css("display", "block");
            },

            updateVisibilityOfRuleMailingReceived: function () {
                var decisionType = jQuery("form[name='decisionForm'] input[name='decisionType']:checked").val();
                if (decisionType == constants.decisionTypeAutoOptimization) {
                    this.hideDecisionFormDiv("ruleMailingReceivedWrapper");
                }
                else if (decisionType == constants.decisionTypeDecision) {
                    var decisionCriteria = jQuery("form[name='decisionForm'] select[name='decisionCriteria']").val();
                    if (decisionCriteria == constants.decisionReaction) {
                        this.hideDecisionFormDiv("ruleMailingReceivedWrapper");
                    }
                    else if (decisionCriteria == constants.decisionProfileField) {
                        this.hideDecisionFormDiv("ruleMailingReceivedWrapper");
                        var localShowDecisionFormDiv = this.showDecisionFormDiv;
                        //check if mailing icon exists
                        campaignManager.getIncomingChainsForIcon()
                            .forEach(function(nodes) {
                                for (var i = nodes.length - 1; i >= 0; i--) {
                                    if (nodes[i].type == nodeFactory.NODE_TYPE_MAILING
                                        || nodes[i].type == nodeFactory.NODE_TYPE_ACTION_BASED_MAILING
                                        || nodes[i].type == nodeFactory.NODE_TYPE_DATE_BASED_MAILING
                                        || nodes[i].type == nodeFactory.NODE_TYPE_FOLLOWUP_MAILING
                                    ) {
                                        // Memorizes mailing ID to be set as decisions mailingId.
                                        profileFieldMailingId = nodes[i].data.mailingId;
                                        localShowDecisionFormDiv("ruleMailingReceivedWrapper");
                                    }
                                }
                            });
                    }
                }
            },

            // Date picker uses a date in UTC but JS uses a local timezone so we need to workaround that
            dateAsUTC: function (date) {
                if (date) {
                    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
                } else {
                    return null;
                }
            },

            checkPresentNodesByTypeList: function (typeList) {
                var nodes = campaignManager.getCMNodes().getNodesByTypeList(typeList);
                return nodes && nodes.length > 0;
            }
        };

        decisionEditor = editorsHelper.editors["decision"];
    });

    this.addDomInitializer('recipient-editor-initializer', function ($e) {

        editorsHelper.editors["recipient"] = {

            formName: "recipientForm",
            safeToSave: true,

            createNewTarget: function () {
                editorsHelper.processForward(constants.forwardTargetGroupCreate, "#recipientTargetSelector", [], submitWorkflowForm);
            },

            editTarget: function (targetId) {
                jQuery("#forwardTargetItemId").val(targetId);
                editorsHelper.processForward(constants.forwardTargetGroupEdit, "#recipientTargetSelector", [], submitWorkflowForm);
            },

            getTitle: function () {
                return t('workflow.recipient');
            },

            fillEditor: function (node) {
                var data = node.data;

                var $form = $("form[name='" + this.formName + "']");
                $form.submit(false);
                $form.get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
                $('#recipientTargetSelect').select2('val', data.targets);

                $('#recipientTargetSelect').select2({
                    formatSelection: function (target) {
                        return '<a href="#" class="btn-link-light" data-action="recipient-editor-target-edit" data-config="targetId: ' + target.id + '">' + target.text + '</a>';
                    }
                });
            },

            saveEditor: function () {
                var data = editorsHelper.formToObject(this.formName);
                data.targets = $('#recipientTargetSelect').val();
                return data;
            }
        };

        recipientEditor = editorsHelper.editors["recipient"];
    });

    this.addDomInitializer('parameter-editor-initializer', function () {
        editorsHelper.editors["parameter"] = {

            formName: "parameterForm",

            getTitle: function () {
                return t('workflow.parameter');
            },

            fillEditor: function (node) {
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
            },

            saveEditor: function () {
                return editorsHelper.formToObject(this.formName);
            }
        };
    });

    this.addDomInitializer('ownworkflow-copy-dialog-initializer', function () {
        ownWorkflowCopyDialogHandler = {

            workflowId: 0,

            ownWorkflowCopyDialogResponse: function (result) {
                jQuery('#ownworkflow-use-original-dialog').dialog('close');
                campaignManager.expandNodeUseOriginal(result, this.workflowId);
                return false;
            },

            showDialog: function (workflowId) {
                this.workflowId = workflowId;
                jQuery('#ownworkflow-use-original-dialog').dialog({
                    title: t('workflow.ownWorkflow.copy_title'),
                    dialogClass: "no-close",
                    open: function (event, ui) {
                        var title = jQuery("#ui-dialog-title-ownworkflow-use-original-dialog");
                        title.css("padding-top", 8);
                        title.css("padding-bottom", 8);
                    },
                    width: "auto",
                    modal: true,
                    resizable: false
                });
            }
        }
    });

    this.addDomInitializer('ownworkflow-editor-initializer', function () {

        editorsHelper.editors["ownWorkflow"] = {

            formName: "ownWorkflowForm",
            node: null,

            getTitle: function () {
                return t('workflow.ownWorkflow.title');
            },

            fillEditor: function (node) {
                this.node = node;
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
            },

            saveEditor: function () {
                var newData = editorsHelper.formToObject(this.formName);
                newData.copyContent = (newData.copyContent == 'true');
                return newData;
            },

            saveWithCheck: function () {
                // clear error message
                jQuery("#ownWorkflow-editor .editor-error-messages").css("display", "none");
                // check status for edit orignal option
                jQuery("#ownWorkflow-editor").css("display", "none");
                editorsHelper.saveCurrentEditor(true);
                editorsHelper.openNode(this.node, true);
                editorsHelper.cancelEditor();
            },

            cancelEditor: function () {
                // clear error message
                jQuery("#ownWorkflow-editor .editor-error-messages").css("display", "none");
                editorsHelper.cancelEditor();
            },

            closeEditor: function () {
                editorsHelper.deleteNode(this.node.id, false, false);
            }
        };

        ownWorkflowEditor = editorsHelper.editors["ownWorkflow"];
    });

    this.addDomInitializer('mailing-editor-initializer', function ($e) {

        var mailingData = $e.find("#mailing-editor-data").json();

        mailingEditorBase = new AGN.Lib.WM.MailingEditorBase(mailingData, campaignManager, submitWorkflowForm);

        editorsHelper.editors["mailing"] = {

            formName: "mailingForm",
            safeToSave: true,

            getTitle: function () {
                return t('workflow.defaults.mailing');
            },

            fillEditor: function (node) {
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
                mailingEditorBase.fillEditorBase(node);
                jQuery("#mailing-editor .editor-error-messages").css("display", "none");
            },

            createNewMailing: function () {
                mailingEditorBase.createNewMailing(constants.forwardMailingCreate);
            },

            editMailing: function () {
                mailingEditorBase.editMailing(constants.forwardMailingEdit);
            },

            copyMailing: function () {
                mailingEditorBase.copyMailing(constants.forwardMailingCopy);
            },

            saveEditor: function () {
                mailingEditorBase.setNodeFields();
                mailingEditorBase.disableInputs();
                return editorsHelper.formToObject(this.formName);
            },

            saveWithCheckStatus: function () {
                var isNotSent = mailingEditorBase.showSecurityQuestion();
                if (isNotSent) {
                    editorsHelper.saveCurrentEditor(false, mailingEditorBase);
                }
            }

        };

        mailingEditor = editorsHelper.editors["mailing"];
    });

    this.addDomInitializer('form-editor-initializer', function ($e) {
        editorsHelper.editors["form"] = {

            formName: "formsForm",
            safeToSave: true,

            getTitle: function () {
                return t('forms');
            },

            createNewUserForm: function () {
                editorsHelper.processForward(constants.forwardUserFormCreate, "#formsEditorUserFormId", [], submitWorkflowForm);
            },

            editForm: function () {
                var formSelector = jQuery("form[name='" + this.formName + "'] select[name=userFormId]");
                var formIdVal = formSelector.val();
                jQuery("#forwardTargetItemId").val(formIdVal);
                editorsHelper.processForward(constants.forwardUserFormEdit, "form[name='" + this.formName + "'] select[name=userFormId]", [], submitWorkflowForm);
            },

            fillEditor: function (node) {
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
                this.addLinks();
                jQuery("#form-editor .editor-error-messages").css("display", "none");
            },

            validateEditor: function () {
                var formSelector = jQuery("form[name='" + this.formName + "'] select[name=userFormId]");
                if (formSelector.val() > 0) {
                    editorsHelper.saveCurrentEditor();
                }
                else {
                    jQuery("#form-editor .editor-error-messages").html(t('error.workflow.noForm'));
                    jQuery("#form-editor .editor-error-messages").css("display", "block");
                }
            },

            onChange: function () {
                this.addLinks();
            },

            addLinks: function () {
                var formSelector = jQuery("form[name='" + this.formName + "'] select[name=userFormId]");
                var formIdVal = formSelector.val();
                if (formIdVal > 0) {
                    jQuery("#form-editor #editFormLink").css("display", "block");
                    jQuery("#form-editor #createFormLink").css("display", "none");
                } else {
                    jQuery("#form-editor #createFormLink").css("display", "block");
                    jQuery("#form-editor #editFormLink").css("display", "none");
                }
            },

            saveEditor: function () {
                return editorsHelper.formToObject(this.formName);
            },

            isSetFilledAllowed: function () {
                var allowed = false;
                if (jQuery("#formsEditorUserFormId").val() > 0) {
                    allowed = true;
                }
                return allowed;
            }
        };
        formsEditor = editorsHelper.editors["form"];

    });

    this.addDomInitializer('report-editor-initializer', function ($e) {
        editorsHelper.editors["report"] = {

            safeToSave: true,

            getTitle: function () {
                return t('report');
            },

            createNewReport: function () {
                editorsHelper.processForward(constants.forwardReportCreate, "#reportSelector", [], submitWorkflowForm);
            },

            editReport: function (reportId) {
                jQuery("#forwardTargetItemId").val(reportId);
                editorsHelper.processForward(constants.forwardReportEdit, "#reportSelector", [], submitWorkflowForm);
            },

            fillEditor: function (node) {
                var data = node.data;
                //set option selected by default
                editorsHelper.initSelectWithFirstValue(jQuery("#reportSelect"));

                jQuery("#report-editor .editor-error-messages").css("display", "none");
                jQuery('#reportSelect').select2('val', data.reports);

                jQuery('#reportSelect').select2({
                    formatSelection: function (report) {
                        return '<a href="#" class="btn-link-light" data-action="report-editor-change" data-config="reportId:' + report.id + '">' + report.text + '</a>';
                    }
                });
            },

            validateEditor: function () {
                var selectedReports = $('#reportSelect').val();
                if (selectedReports && selectedReports.length > 0) {
                    editorsHelper.saveCurrentEditor();
                }
                else {
                    var $message = $('#report-editor').find('.editor-error-messages');
                    $message.html(t('workflow.report.error.no_report'));
                    $message.css("display", "block");
                }
            },

            saveEditor: function () {
                var data = {
                    reports: jQuery('#reportSelect').val()
                };
                return data;
            },

            isSetFilledAllowed: function () {
                var allowed = false;
                var selectedReports = jQuery('#reportSelect').val();
                if (selectedReports && selectedReports.length > 0) {
                    allowed = true;
                }
                return allowed;
            }
        };

        reportEditor = editorsHelper.editors["report"];
    });

    this.addDomInitializer('icon-comment-editor-initializer', function ($e) {
        editorsHelper.editors["icon-comment"] = {

            formName: "iconCommentForm",
            safeToSave: true,

            fillEditor: function (node) {
                var data = node.data;
                $('#iconComment').val(data.iconComment);
            },

            saveEditor: function () {
                var commentValue = $('#iconComment').val();
                editorsHelper.saveIconComment(commentValue);
                this.cancelEditor();
            },

            cancelEditor: function() {
                $('#icon-comment-editor').dialog('close');
                return false;
            }
        };

        iconCommentEditor = editorsHelper.editors["icon-comment"];
    });

    this.addDomInitializer('export-editor-initializer', function ($e) {

        var exportData = $e.find("#export-editor-data").json();

        editorsHelper.editors["export"] = {

            formName: "exportForm",
            safeToSave: true,

            getTitle: function () {
                return t('auto_export');
            },

            createNewAutoExport: function () {
                editorsHelper.processForward(constants.forwardAutoExportCreate, "form[name='" + this.formName + "'] select[name=importexportId]", [], submitWorkflowForm);
            },

            editAutoExport: function () {
                var autoExportSelector = jQuery("form[name='" + this.formName + "'] select[name=importexportId]");
                var autoExportIdVal = autoExportSelector.val();
                jQuery("#forwardTargetItemId").val(autoExportIdVal);
                editorsHelper.processForward(constants.forwardAutoExportEdit, "form[name='" + this.formName + "'] select[name=importexportId]", [], submitWorkflowForm);
            },

            fillEditor: function (node) {
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
                this.addLinks();
                jQuery("#export-editor .editor-error-messages").css("display", "none");
                if (exportData.isDisabled == "true") {
                    jQuery("#import-editor .editor-error-messages").html(t('error.workflow.autoExportPermission'));
                    jQuery("#import-editor .editor-error-messages").css("display", "block");
                }
            },

            validateEditor: function () {
                var $messages = $("#export-editor .editor-error-messages");
                var autoExportSelector = jQuery("form[name='" + this.formName + "'] select[name=importexportId]");
                if (autoExportSelector.val() > 0) {
                    jQuery.ajax({
                        type: 'POST',
                        url: AGN.url('/workflow/validateDependency.action'),
                        data: {
                            workflowId: campaignManager.workflowId || 0,
                            type: DEPENDENCY_TYPE_AUTO_EXPORT,
                            entityId: autoExportSelector.val()
                        }
                    }).done(function(data) {
                        if (data.valid === true) {
                            editorsHelper.saveCurrentEditor();
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

            onChange: function () {
                this.addLinks();
            },

            addLinks: function () {
                var formSelector = jQuery("form[name='" + this.formName + "'] select[name=importexportId]");
                var formIdVal = formSelector.val();
                if (formIdVal > 0) {
                    jQuery("#export-editor #editAutoExportLink").css("display", "block");
                    jQuery("#export-editor #createAutoExportLink").css("display", "none");
                } else {
                    jQuery("#export-editor #createAutoExportLink").css("display", "block");
                    jQuery("#export-editor #editAutoExportLink").css("display", "none");
                }
            },

            saveEditor: function () {
                return editorsHelper.formToObject(this.formName);
            },

            isSetFilledAllowed: function () {
                var $select = $("form[name='" + this.formName + "'] select[name=importexportId]");
                var $option = $select.find(':selected');

                return !($select.val() == 0 || $option.data('is-available') != true);
            }
        };

        exportEditor = editorsHelper.editors["export"];
    });

    this.addDomInitializer('deadline-editor-initializer', function ($e) {

        editorsHelper.editors["deadline"] = {
            panelIds: ['fixedDeadlinePanel', 'delayDeadlinePanel'],
            timeUnitIds: ['deadlineTimeUnitMinute',
                'deadlineTimeUnitHour',
                'deadlineTimeUnitDay',
                'deadlineTimeUnitWeek',
                'deadlineTimeUnitMonth'],
            formName: "deadlineForm",

            getDefaultDelayData: function(type){
                var defaultData = {};
                if(type === constants.deadlineTypeDelay && this.isPrecededByImportIcon) {
                    defaultData = {
                        timeUnit: constants.deadlineTimeUnitHour,
                        delayValue: constants.defaultImportDelayLimit
                    };
                }
                return defaultData;
            },
            getTitle: function () {
                return t('workflow.deadline.title');
            },

            fillEditor: function (node) {
                var data = node.data;

                $('#deadline-editor').find('.editor-error-messages').css('display', 'none');
                this.isPrecededByImportIcon = this.checkPrecededByImportIcon(node);

                var $form = $("form[name='" + this.formName + "']");

                $form.submit(false);
                $("form[name='" + this.formName + "'] #deadlineDate").pickadate('picker').set('select', this.dateAsUTC(data.date || new Date()));

                this.fillDelayData($form, data);

                $("form[name='" + this.formName + "'] #time").val(("0" + data.hour).slice(-2) + ":" + ("0" + data.minute).slice(-2));
            },

            fillDelayData: function($form, data) {
                $form.get(0).reset();
                var type = $("form[name='" + this.formName + "'] input[name='deadlineType']:checked").val();
                var defaultValues  = this.getDefaultDelayData(type);

                editorsHelper.fillFormFromObject(this.formName, data, "", defaultValues);
                this.updateVisibility();

                var delayValue = data.delayValue || defaultValues.delayValue;
                editorsHelper.initSelectWithValueOrChooseFirst($("form[name='" + this.formName + "'] select[name='delayValue']"), delayValue);
            },

            saveEditor: function () {
                var data = editorsHelper.formToObject(this.formName);
                data.date = $("form[name='" + this.formName + "'] #deadlineDate").pickadate('picker').get('select').obj;

                var time = $("form[name='" + this.formName + "'] #time").val();
                data.hour = time.substring(0, 2);
                data.minute = time.substring(3, 5);

                return data;
            },

            // functions handling visibility of different parts of dialog according to selected settings
            updateVisibility: function () {
                var type = $("form[name='" + this.formName + "'] input[name='deadlineType']:checked").val();

                if (this.checkPresentNodesByTypeList([nodeFactory.NODE_TYPE_ACTION_BASED_MAILING, nodeFactory.NODE_TYPE_DATE_BASED_MAILING])) {
                    $("form[name='" + this.formName + "'] input#typeFixedDeadline").parent().parent().parent().hide();
                    $("form[name='" + this.formName + "'] input[value='" + constants.deadlineTypeDelay + "']").prop('checked', true);
                    type = constants.deadlineTypeDelay;
                } else {
                    $("form[name='" + this.formName + "'] input#typeFixedDeadline").parent().parent().parent().show();
                }

                if (type === constants.deadlineTypeDelay) {
                    this.switchPanelsVisibility('delayDeadlinePanel');
                    this.switchTimeContainerVisibility(false);

                    var timeUnit = $("form[name='" + this.formName + "'] select[name='timeUnit']").val();
                    switch (timeUnit) {
                        case constants.deadlineTimeUnitMinute:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitMinute');
                            break;

                        case constants.deadlineTimeUnitHour:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitHour');
                            break;

                        case constants.deadlineTimeUnitDay:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitDay');
                            this.switchTimeContainerVisibility(true);
                            $('#deadlineTimeHelp').removeAttr("style");
                            break;

                        case constants.deadlineTimeUnitWeek:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitWeek');
                            break;

                        case constants.deadlineTimeUnitMonth:
                            this.switchTimeUnitsVisibility('deadlineTimeUnitMonth');
                            break;
                    }
                    $("form[name='" + this.formName + "'] input[name='useTime']").css("display", "inline");
                } else if (type === constants.deadlineTypeFixedDeadline) {
                    this.switchPanelsVisibility('fixedDeadlinePanel');
                    this.switchTimeContainerVisibility(true);
                    this.hideDiv("deadlineTimeHelp");
                    $("form[name='" + this.formName + "'] input[name='useTime']").css("display", "none");
                }
            },

            switchPanelsVisibility: function(selectedPanelId) {
                var self = this;
                this.panelIds.forEach(function (itemId) {
                    if (itemId !== selectedPanelId) {
                        self.hideDiv(itemId);
                    }
                });
                this.showDiv(selectedPanelId);
            },

            switchTimeUnitsVisibility: function(selectedTimeUnitId) {
                var self = this;
                this.timeUnitIds.forEach(function (itemId) {
                    if (itemId !== selectedTimeUnitId) {
                        self.hideDiv(itemId);
                        $('#' + itemId + ' select').removeAttr("name");
                    }
                });
                this.showDiv(selectedTimeUnitId);
                $('#' + selectedTimeUnitId + ' select').attr("name", "delayValue");
            },

            switchTimeContainerVisibility: function(isVisible) {
                var self = this;
                if (isVisible) {
                    self.showDiv("deadlineTimeContainer");
                } else {
                    self.hideDiv("deadlineTimeContainer");
                }
            },

            hideAllItems: function(itemIds) {
                var self = this;
                itemIds.forEach(function (itemId) {
                    self.hideDiv(itemId);
                });
            },

            validateEditor: function () {
                var valid = true;
                var hourLimit = constants.defaultImportDelayLimit;

                if (this.isPrecededByImportIcon) {
                    var type = $("form[name='" + this.formName + "'] input[name='deadlineType']:checked").val();

                    if (type === constants.deadlineTypeDelay) {
                        var timeUnit = $("form[name='" + this.formName + "'] select[name='timeUnit']").val();

                        if (timeUnit === constants.deadlineTimeUnitMinute) {
                            valid = false;
                        } else if (timeUnit === constants.deadlineTimeUnitHour) {
                            if ($("#deadlineTimeUnitHour select").val() < hourLimit) {
                                valid = false;
                            }
                        }
                    }
                }

                if (valid) {
                    editorsHelper.saveCurrentEditor();
                } else {
                    var $message = $('#deadline-editor').find('.editor-error-messages');
                    $message.html(t('error.workflow.deadlineIsTooShortForImport', hourLimit));
                    $message.css('display', 'block');
                }
            },

            onTimeChanged: function () {
                var type = $("form[name='" + this.formName + "'] input[name='deadlineType']:checked").val();
                var timeUnit = $("form[name='" + this.formName + "'] select[name='timeUnit']").val();
                if (type == constants.deadlineTypeDelay && timeUnit == constants.deadlineTimeUnitDay) {
                    $("form[name='" + this.formName + "'] input[name='useTime']").prop('checked', true);
                }
            },

            hideDiv: function (id) {
                $("form[name='" + this.formName + "'] #" + id).css("display", "none");
            },

            showDiv: function (id) {
                $("form[name='" + this.formName + "'] #" + id).css("display", "block");
            },

            // Date picker uses a date in UTC but JS uses a local timezone so we need to workaround that
            dateAsUTC: function (date) {
                if (date) {
                    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
                } else {
                    return null;
                }
            },

            checkPresentNodesByTypeList: function (typeList) {
                var nodes = campaignManager.getCMNodes().getNodesByTypeList(typeList);
                return nodes && nodes.length > 0;
            },

            checkPrecededByImportIcon: function(node) {
                var campaignManagerNodes = campaignManager.getCMNodes();
                return campaignManager.getNodeIncomingConnections(node).some(function(connection) {
                    var node = campaignManagerNodes.getNodeById(connection.source);
                    return node && node.type == nodeFactory.NODE_TYPE_IMPORT;
                });
            }
        };

        deadlineEditor = editorsHelper.editors["deadline"];
    });

    this.addDomInitializer('import-editor-initializer', function ($e) {

        var importData = $e.find("#import-editor-data").json();

        editorsHelper.editors["import"] = {

            formName: "importForm",
            safeToSave: true,

            getTitle: function () {
                return t('auto_import');
            },

            createNewAutoImport: function () {
                editorsHelper.processForward(constants.forwardAutoImportCreate, "#importexportId", [], submitWorkflowForm);
            },

            editAutoImport: function () {
                var autoImportSelector = jQuery("form[name='" + this.formName + "'] select[name=importexportId]");
                var autoImportIdVal = autoImportSelector.val();
                jQuery("#forwardTargetItemId").val(autoImportIdVal);
                editorsHelper.processForward(constants.forwardAutoImportEdit, "form[name='" + this.formName + "'] select[name=importexportId]", [], submitWorkflowForm);
            },

            fillEditor: function (node) {
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
                this.addLinks();
                jQuery("#import-editor .editor-error-messages").css("display", "none");
                if (importData.isDisabled == "true") {
                    jQuery("#import-editor .editor-error-messages").html(t('error.workflow.autoImportPermission'));
                    jQuery("#import-editor .editor-error-messages").css("display", "block");
                }
            },

            validateEditor: function () {
                var $messages = $("#import-editor .editor-error-messages");
                var autoImportSelector = jQuery("form[name='" + this.formName + "'] select[name=importexportId]");
                if (autoImportSelector.val() > 0) {
                    jQuery.ajax({
                        type: 'POST',
                        url: AGN.url('/workflow/validateDependency.action'),
                        data: {
                            workflowId: campaignManager.workflowId || 0,
                            type: DEPENDENCY_TYPE_AUTO_IMPORT,
                            entityId: autoImportSelector.val()
                        }
                    }).done(function(data) {
                        if (data.valid === true) {
                            editorsHelper.saveCurrentEditor();
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

            onChange: function () {
                this.addLinks();
            },

            addLinks: function () {
                var formSelector = jQuery("form[name='" + this.formName + "'] select[name=importexportId]");
                var formIdVal = formSelector.val();
                if (formIdVal > 0) {
                    jQuery("#import-editor #editAutoImportLink").css("display", "block");
                    jQuery("#import-editor #createAutoImportLink").css("display", "none");
                } else {
                    jQuery("#import-editor #createAutoImportLink").css("display", "block");
                    jQuery("#import-editor #editAutoImportLink").css("display", "none");
                }
            },

            saveEditor: function () {
                return editorsHelper.formToObject(this.formName);
            },

            isSetFilledAllowed: function () {
                var $select = $("form[name='" + this.formName + "'] select[name=importexportId]");
                var $option = $select.find(':selected');

                return !($select.val() == 0 || $option.data('is-available') != true);
            }
        };

        importEditor = editorsHelper.editors["import"];
    });

    this.addDomInitializer('archive-editor-initializer', function ($e) {
        editorsHelper.editors["archive"] = {

            formName: "archiveForm",
            safeToSave: true,

            getTitle: function () {
                return t('workflow.mailing.archive');
            },

            createNewArchive: function () {
                editorsHelper.processForward(constants.forwardArchiveCreate, "#settings_general_campaign", [], submitWorkflowForm);
            },

            fillEditor: function (node) {
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
            },

            saveEditor: function () {
                return editorsHelper.formToObject(this.formName);
            }
        };

        archiveEditor = editorsHelper.editors["archive"];

    });

    this.addDomInitializer('action-mailing-editor-initializer', function ($e) {

        var actionMailingData = $e.find("#action-mailing-editor-data").json();

        actionbasedMailingEditorBase = new AGN.Lib.WM.MailingEditorBase(actionMailingData, campaignManager, submitWorkflowForm);

        editorsHelper.editors["actionbased_mailing"] = {

            formName: "actionbasedMailingForm",
            safeToSave: false,

            getTitle: function () {
                return t('workflow.mailing.action_based');
            },

            fillEditor: function (node) {
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
                actionbasedMailingEditorBase.fillEditorBase(node);
                jQuery("#actionbased_mailing-editor .editor-error-messages").css("display", "none");
            },

            createNewMailing: function () {
                actionbasedMailingEditorBase.createNewMailing(constants.forwardMailingCreate);
            },

            editMailing: function (mailingId) {
                actionbasedMailingEditorBase.editMailing(constants.forwardMailingEdit);
            },

            saveEditor: function () {
                actionbasedMailingEditorBase.setNodeFields();
                actionbasedMailingEditorBase.disableInputs();
                return editorsHelper.formToObject(this.formName);
            },

            saveWithCheckStatus: function () {
                var isNotSent = actionbasedMailingEditorBase.showSecurityQuestion();
                if (isNotSent) {
                    editorsHelper.saveCurrentEditor(false, actionbasedMailingEditorBase);
                }
            },

            copyMailing: function () {
                actionbasedMailingEditorBase.copyMailing(constants.forwardMailingCopy);
            }
        };

        actionbasedMailingEditor = editorsHelper.editors["actionbased_mailing"];
    });

    this.addDomInitializer('date-mailing-initializer', function ($e) {

        var dateMailingData = $e.find("#date-mailing-editor-data").json();

        datebasedMailingEditorBase = new AGN.Lib.WM.MailingEditorBase(dateMailingData, campaignManager, submitWorkflowForm);

        editorsHelper.editors["datebased_mailing"] = {

            formName: "datebasedMailingForm",
            safeToSave: false,

            getTitle: function () {
                return t('workflow.mailing.date_based');
            },

            fillEditor: function (node) {
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
                datebasedMailingEditorBase.fillEditorBase(node);
                jQuery("#datebased_mailing-editor .editor-error-messages").css("display", "none");
            },

            saveEditor: function () {
                datebasedMailingEditorBase.setNodeFields();
                datebasedMailingEditorBase.disableInputs();
                return editorsHelper.formToObject(this.formName);
            },

            createNewMailing: function () {
                datebasedMailingEditorBase.createNewMailing(constants.forwardMailingCreate);
            },

            editMailing: function () {
                datebasedMailingEditorBase.editMailing(constants.forwardMailingEdit);
            },

            saveWithCheckStatus: function () {
                var isNotSent = datebasedMailingEditorBase.showSecurityQuestion();
                if (isNotSent) {
                    editorsHelper.saveCurrentEditor(false, datebasedMailingEditorBase);
                }
            },

            copyMailing: function () {
                datebasedMailingEditorBase.copyMailing(constants.forwardMailingCopy);
            }
        };

        datebasedMailingEditor = editorsHelper.editors["datebased_mailing"];
    });

    this.addDomInitializer('followup-mailing-editor-initializer', function ($e) {
        var baseMailingData = $e.find("#followup-mailing-base-data").json();
        var followupMailingData = $e.find("#followup-mailing-data").json();
        baseMailingData.dropDownEl = jQuery("#followup-m-editor");

        baseMailingEditorBase = new AGN.Lib.WM.MailingEditorBase(baseMailingData, campaignManager, submitWorkflowForm);

        followupMailingEditorBase = new AGN.Lib.WM.MailingEditorBase(followupMailingData, campaignManager, submitWorkflowForm);

        baseMailingEditorBase.selectedFollowUpMailingId = 0;
        baseMailingEditorBase.mailingsInCampaign = [];

        baseMailingEditorBase.findMailingIconsBefore = function (node) {
            var nodes = campaignManager.getCMNodes().getNodes(),
                incomingConn = campaignManager.getNodeIncomingConnections(node),
                nodeKey,
                i;
            if (node.type === "mailing" ||
                node.type === "datebased_mailing" ||
                node.type === "actionbased_mailing"
            ) {
                this.mailingsInCampaign.push(node.data.mailingId);
            }
            for (i = 0; i < incomingConn.length; i++) {
                nodeKey = _.findKey(nodes, function (chr) {
                    return chr.element.id === incomingConn[i].source;
                });
                this.findMailingIconsBefore(nodes[nodeKey]);
            }
        };

        baseMailingEditorBase.getMailingsByWorkStatus = function (status, sort, order, selectedMailValue) {
            var mailingEditorBase = this;
            var mailingsList = jQuery(this.formNameJId + " " + this.selectNameJId);
            this.mailingsInCampaign = [];
            this.findMailingIconsBefore(this.node);
            var mailingsBefore = (_.uniq(this.mailingsInCampaign)).toString();
            mailingsList.attr("readonly", "readonly");

            jQuery.ajax({
                type: "POST",
                url: AGN.url('/workflow/getMailingsByWorkStatus.action'),
                data: {
                    mailingTypes: this.mailingTypesForLoading.join(','),
                    status: status,
                    sort: sort,
                    order: order,
                    mailingId: this.selectedFollowUpMailingId,
                    parentMailingId: jQuery(this.formNameJId + " " + this.selectNameJId).val(),
                    mailingStatus: "W",
                    takeMailsForPeriod: true,
                    mailingsInCampaign: mailingsBefore
                },
                success: function (data) {
                    //populate the drop-down list with mailings
                    mailingsList.html("");
                    mailingsList.append('<option value="0">' + t('workflow.defaults.no_mailing') + '</option>');
                    for (var i = 0; i < data.length; i++) {
                        var obj = data[i];
                        var mailingFontColor = "";
                        var selected = "";

                        if (obj.WORK_STATUS == "mailing.status.sent" || obj.WORK_STATUS == "mailing.status.norecipients") {
                            mailingFontColor = "style='color: #808080;'";
                        }

                        if (selectedMailValue == obj.MAILING_ID) {
                            selected = "selected";
                        }

                        mailingsList.append("<option " + mailingFontColor + " value='" + obj.MAILING_ID + "' status='" + obj.WORK_STATUS + "' senddate='" + obj.SENDDATE + "' " + selected + ">" + obj.SHORTNAME + "</option>");
                    }
                    mailingsList.removeAttr("readonly");
                    mailingEditorBase.mailingsStatus = status;
                    mailingEditorBase.mailingsSort = sort;
                    mailingEditorBase.mailingsOrder = order;
                    mailingsList.val(mailingEditorBase.mailingId);

                    editorsHelper.initSelectWithValueOrChooseFirst(mailingsList, mailingEditorBase.mailingId);
                }
            });
        };

        followupMailingEditorBase.onMailingSelectChange = function (val) {
            baseMailingEditorBase.selectedFollowUpMailingId = val;
            this.setSelectMailingOptions(val);
        };

        editorsHelper.editors["followup_mailing"] = {

            formName: "followupMailingForm",
            safeToSave: false,

            getTitle: function () {
                return t('workflow.mailing.followup');
            },

            fillEditor: function (node) {
                followupMailingEditorBase.fillEditorBase(node);
                baseMailingEditorBase.fillEditorBase(node);
                var data = node.data;
                jQuery("form[name='" + this.formName + "']").submit(false);
                jQuery("form[name='" + this.formName + "']").get(0).reset();
                editorsHelper.fillFormFromObject(this.formName, data, "");
                jQuery("#followup_mailing-editor .editor-error-messages").css("display", "none");
                if (followupMailingData.disableFollowup == "true") {
                    jQuery("#followup_mailing-editor .editor-error-messages").html(t('error.workflow.followupPermission'));
                    jQuery("#followup_mailing-editor .editor-error-messages").css("display", "block");
                    baseMailingEditorBase.disableInputs();
                }
            },

            createNewMailing: function () {
                //store base mailing data
                baseMailingEditorBase.node.data.baseMailingId = baseMailingEditorBase.getSelectedMailingOption().val();
                baseMailingEditorBase.node.data.decisionCriterion = baseMailingEditorBase.getSelectedDecisionOption().val();

                followupMailingEditorBase.createNewMailing(constants.forwardMailingCreate);
            },

            editMailing: function () {
                followupMailingEditorBase.editMailing(constants.forwardMailingEdit);
            },

            copyMailing: function () {
                baseMailingEditorBase.node.data.baseMailingId = baseMailingEditorBase.getSelectedMailingOption().val();
                baseMailingEditorBase.node.data.decisionCriterion = baseMailingEditorBase.getSelectedDecisionOption().val();
                followupMailingEditorBase.copyMailing(constants.forwardMailingCopy);
            },

            saveEditor: function () {
                baseMailingEditorBase.disableInputs();
                followupMailingEditorBase.disableInputs();
                this.setNodeFields();
                return editorsHelper.formToObject(this.formName);
            },

            saveWithCheckStatus: function () {
                var isNotSent = followupMailingEditorBase.showSecurityQuestion();
                if (isNotSent) {
                    editorsHelper.saveCurrentEditor(false, followupMailingEditorBase);
                }
            },

            validateEditor: function (save) {
                var baseMailingSelector = jQuery(baseMailingEditorBase.formNameJId + " " + baseMailingEditorBase.selectNameJId);
                var followupMailingSelector = jQuery(followupMailingEditorBase.formNameJId + " " + followupMailingEditorBase.selectNameJId);
                if (baseMailingSelector.val() > 0 && followupMailingSelector.val() > 0) {
                    if (save) {
                        save();
                    } else {
                        editorsHelper.saveCurrentEditor();
                    }
                } else {
                    var $message = $('#followup_mailing-editor').find('.editor-error-messages');
                    $message.html(t('error.workflow.noMailing'));
                    $message.css('display', 'block');
                }
            },

            setNodeFields: function () {
                this.setNodeFilled(baseMailingEditorBase, followupMailingEditorBase);
                this.setNodeIconTitle(baseMailingEditorBase, followupMailingEditorBase);
            },

            setNodeFilled: function (baseEditor, followupEditor) {
                baseEditor.node.filled = (parseInt(baseEditor.mailingId, 10) > 0 && parseInt(followupEditor.mailingId, 10) > 0);
            },

            setNodeIconTitle: function (baseEditor, followupEditor) {
                baseEditor.node.iconTitle = "";

                // Check if mailing is selected.
                if (parseInt(baseEditor.mailingId, 10) > 0) {
                    if (parseInt(followupEditor.mailingId, 10) > 0) {
                        baseEditor.node.iconTitle = baseEditor.getSelectedMailingOption().html() + ":/" +
                            followupEditor.getSelectedMailingOption().html();
                    } else {
                        baseEditor.node.iconTitle = baseEditor.getSelectedMailingOption().html();
                    }
                } else {
                    baseEditor.node.iconTitle = " : " + followupEditor.getSelectedMailingOption().html();
                }
            }

        };

        followupMailingEditor = editorsHelper.editors["followup_mailing"];
    });

    this.addDomInitializer('copy-dialog-initializer', function ($e) {

        var copyData = $e.find("#copy-editor-data").json();

        workflowCopyDialogHandler = {
            workflowId: {},
            workflowCopyDialogClose: function () {
                jQuery('#workflow-copy-dialog').dialog('close');
                return false;
            },
            workflowCopyDialogResponse: function (result) {
                jQuery('#workflow-copy-dialog').dialog('close');
                window.location = copyData.url + '?workflowId=' + this.workflowId
                    + '&isWithContent=' + (result ? "true" : "false");
                return false;
            },
            showDialog: function (workflowId) {
                this.workflowId = workflowId;

                //check if all nodes are empty
                var isAllNodesAreEmpty = true;
                var nodes = campaignManager.getCMNodes().getNodes();
                for (var i in nodes) {
                    if (nodes.hasOwnProperty(i)) {
                        var node = nodes[i];
                        if (node.filled) {
                            isAllNodesAreEmpty = false;
                            break;
                        }
                    }
                }

                if (isAllNodesAreEmpty) {
                    jQuery("#workflow-copy-dialog div.form-group div.col-xs-12 div.well").html(t('workflow.copy.question'));
                    jQuery("#workflowBtnWithContent").hide();
                    jQuery("#workflowBtnOnlyChain").hide();
                    jQuery("#workflowBtnCopy").show();
                } else {
                    jQuery("#workflow-copy-dialog div.form-group div.col-xs-12 div.well").html(t('workflow.copy.question_with_content'));
                    jQuery("#workflowBtnCopy").hide();
                    jQuery("#workflowBtnWithContent").show();
                    jQuery("#workflowBtnOnlyChain").show();
                }

                jQuery('#workflow-copy-dialog').dialog({
                    title: '<span class="dialog-fat-title">' + t('workflow.ownWorkflow.copy_title') + '</span>',
                    dialogClass: "no-close",
                    width: 650,
                    modal: true,
                    resizable: false
                });
            }
        }
    });

    this.addDomInitializer('save-before-pdf-initializer', function (){
        workflowSaveBeforePdfHandler = {

            submitDialog: function (result) {
                if (result) {
                    submitWorkflowForm(true);
                }
                jQuery('#workflow-save-before-pdf-dialog').dialog('close');
                return false;
            },

            showDialog: function (newCampaign, hasUnsavedChanges) {
                var message = '';
                if (newCampaign) {
                    message = t('workflow.pdf.save_new_campaign');
                }
                else if (hasUnsavedChanges) {
                    message = t('workflow.pdf.save_modified_campaign');
                }
                jQuery('#workflow-save-before-pdf-dialog').find('.well').html(message);
                jQuery('#workflow-save-before-pdf-dialog').dialog({
                    title: '<span class="dialog-fat-title">' + t('workflow.pdf.save_campaign') + '</span>',
                    dialogClass: "no-close",
                    width: 650,
                    modal: true,
                    resizable: false
                });
            }
        }
    });

    this.addDomInitializer('open-edit-icon-initializer', function ($e) {
        var data = $e.json();

        //open edit for icon
        var nodeId = data.nodeId;
        if (nodeId) {
            var campaignManagerNodes = campaignManager.getCampaignManagerNodes();
            campaignManagerNodes.setOpenNode(nodeId, data.elementValue);
            campaignManagerNodes.openNodeIcon();
        }
    });

    this.addDomInitializer('testing-dialog-initializer', function ($elem) {

        var workflowTestingDialogHandler = {};

        var configString = $elem.data('config');
        var config = AGN.Lib.Helpers.objFromString(configString);

        var callback = function () {
        };

        workflowTestingDialogHandler = {
            showDialog: function (positiveCallback) {
                if (positiveCallback instanceof Function) {
                    callback = positiveCallback;
                }

                jQuery('#workflow-testing-dialog').dialog({
                    title: '<span class="dialog-fat-title">' + t('workflow.single') + ':&nbsp;' + config.shortname + '</span>',
                    dialogClass: "no-close",
                    width: 650,
                    modal: true,
                    resizable: false
                });
            },

            closeDialog: function () {
                $('#workflow-testing-dialog').dialog('close');
                return false;
            },

            acceptDialog: function () {
                $('#workflow-testing-dialog').dialog('close');
                callback(workflowTestingDialogHandler);
            }
        };

        data.workflowTestingDialogHandler = workflowTestingDialogHandler;

    });

    this.addAction({'click': 'workflowTestingDialogSubmitButton'}, function () {
        data.workflowTestingDialogHandler.acceptDialog();
    });

    this.addAction({'click': 'workflowTestingDialogCancelButton'}, function () {
        data.workflowTestingDialogHandler.closeDialog();
    });

    this.addAction({'click': 'workflowTestBtn'}, function () {
        data.workflowTestingDialogHandler.showDialog(function () {
            // Un-check "active" checkbox
            var checkbox = $('input#workflow_active');
            var disabled = checkbox.prop('disabled');
            checkbox.prop('disabled', true);
            saveWorkflowFormData(true);
            checkbox.prop('disabled', disabled);
        });
    });

    this.addAction({'click': 'editor-save'}, function () {
        editorsHelper.saveCurrentEditor();
        return false;
    });

    this.addAction({'click': 'mailing-editor-save'}, function () {
        mailingEditorBase.validateEditor(mailingEditor.saveWithCheckStatus);
        return false;
    });

    this.addAction({'click': 'mailing-editor-base-no'}, function () {
        mailingEditorBase.transferRequiredMailingData();
        return false;
    });

    this.addAction({'click': 'mailing-editor-base-transfer-data'}, function () {
        mailingEditorBase.transferMailingData();
        return false;
    });

    this.addAction({'click': 'mailing-editor-base-close'}, function () {
        mailingEditorBase.closeOneMailinglistWarningDialog();
        return false;
    });

    this.addAction({'click': 'mailing-editor-base-accept'}, function () {
        mailingEditorBase.acceptOneMailinglistPerCampaign();
        return false;
    });

    this.addAction({'click': 'mailing-editor-base-sort-shortname'}, function () {
        mailingEditorBase.onMailingSortClick('shortname', 'shortname', editorsHelper);
        return false;
    });

    this.addAction({'click': 'mailing-editor-base-sort-data'}, function () {
        var config = getConfigData(this.el);
        mailingEditorBase.onMailingSortClick('shortname', config.sortByDate, editorsHelper);
        return false;
    });

    this.addAction({'change': 'mailing-editor-base-status-change'}, function () {
        mailingEditorBase.onMailingsStatusChange(this.el.val());
        return false;
    });

    this.addAction({'change': 'mailing-editor-base-select-change'}, function () {
        mailingEditorBase.onMailingSelectChange(this.el.val());
        return false;
    });

    this.addAction({'click': 'mailing-editor-base-secure-cancel'}, function () {
        mailingEditorBase.cancelSecurityDialog();
        return false;
    });

    this.addAction({'click': 'mailing-editor-copy'}, function () {
        editorsHelper.curEditor.copyMailing();
        return false;
    });

    this.addAction({'click': 'mailing-editor-new'}, function () {
        editorsHelper.curEditor.createNewMailing();
        return false;
    });

    this.addAction({'click': 'mailing-editor-edit'}, function () {
        editorsHelper.curEditor.editMailing();
        return false;
    });

    this.addAction({'click': 'mailing-editor-base-toggle-settings'}, function () {
        var config = getConfigData(this.el);
        mailingEditorBase.toggleSendSettings(config.editorId);
        return false;
    });

    this.addAction({'change': 'form-editor-change'}, function () {
        formsEditor.onChange();
        return false;
    });

    this.addAction({'click': 'form-editor-new'}, function () {
        formsEditor.createNewUserForm();
        return false;
    });

    this.addAction({'click': 'form-editor-form-edit'}, function () {
        formsEditor.editForm();
        return false;
    });

    this.addAction({'click': 'form-editor-save'}, function () {
        formsEditor.validateEditor();
        return false;
    });

    this.addAction({'click': 'report-editor-change'}, function () {
        var config = getConfigData(this.el);
        reportEditor.editReport(config.reportId);
        return false;
    });

    this.addAction({'click': 'report-editor-new'}, function () {
        reportEditor.createNewReport();
        return false;
    });

    this.addAction({'click': 'report-editor-save'}, function () {
        reportEditor.validateEditor();
        return false;
    });

    this.addAction({'change': 'export-editor-change'}, function () {
        exportEditor.onChange();
        return false;
    });

    this.addAction({'click': 'export-editor-new'}, function () {
        exportEditor.createNewAutoExport();
        return false;
    });

    this.addAction({'click': 'export-editor-autoexport'}, function () {
        exportEditor.editAutoExport();
        return false;
    });

    this.addAction({'click': 'export-editor-save'}, function () {
        exportEditor.validateEditor();
        return false;
    });

    this.addAction({'change': 'deadline-editor-update'}, function () {
        deadlineEditor.updateVisibility();
        return false;
    });

    this.addAction({'change': 'deadline-editor-time-change'}, function () {
        deadlineEditor.onTimeChanged();
        return false;
    });

    this.addAction({'click': 'editor-cancel'}, function () {
        editorsHelper.cancelEditor();
        return false;
    });

    this.addAction({'change': 'import-editor-change'}, function () {
        importEditor.onChange();
        return false;
    });

    this.addAction({'click': 'import-editor-new'}, function () {
        importEditor.createNewAutoImport();
        return false;
    });

    this.addAction({'click': 'import-editor-update'}, function () {
        importEditor.editAutoImport();
        return false;
    });

    this.addAction({'click': 'import-editor-save'}, function () {
        importEditor.validateEditor();
        return false;
    });

    this.addAction({'click': 'icon-comment-editor-save'}, function () {
        iconCommentEditor.saveEditor();
        return false;
    });

    this.addAction({'click': 'icon-comment-editor-cancel'}, function () {
        iconCommentEditor.cancelEditor();
        return false;
    });

    this.addAction({'click': 'archive-editor-new'}, function () {
        archiveEditor.createNewArchive();
        return false;
    });

    this.addAction({'click': 'action-mailing-editor-save'}, function () {
        actionbasedMailingEditorBase.validateEditor(actionbasedMailingEditor.saveWithCheckStatus);
        return false;
    });

    this.addAction({'click': 'action-mailing-editor-base-sort-shortname'}, function () {
        actionbasedMailingEditorBase.onMailingSortClick('shortname', 'shortname', editorsHelper);
        return false;
    });

    this.addAction({'click': 'action-mailing-editor-base-sort-data'}, function () {
        var config = getConfigData(this.el);
        actionbasedMailingEditorBase.onMailingSortClick('shortname', config.sortByDate, editorsHelper);
        return false;
    });

    this.addAction({'change': 'action-mailing-editor-base-status-change'}, function () {
        actionbasedMailingEditorBase.onMailingsStatusChange(this.el.val());
        return false;
    });

    this.addAction({'change': 'action-mailing-editor-base-select-change'}, function () {
        actionbasedMailingEditorBase.onMailingSelectChange(this.el.val());
        return false;
    });

    this.addAction({'click': 'action-mailing-editor-base-no'}, function () {
        actionbasedMailingEditorBase.transferRequiredMailingData();
        return false;
    });

    this.addAction({'click': 'action-mailing-editor-base-secure-cancel'}, function () {
        actionbasedMailingEditorBase.cancelSecurityDialog();
        return false;

    });

    this.addAction({'click': 'action-mailing-editor-base-transfer-data'}, function () {
        actionbasedMailingEditorBase.transferMailingData();
        return false;
    });

    this.addAction({'click': 'date-mailing-editor-base-sort-shortname'}, function () {
        datebasedMailingEditorBase.onMailingSortClick('shortname', 'shortname', editorsHelper);
        return false;
    });

    this.addAction({'click': 'date-mailing-editor-base-sort-data'}, function () {
        var config = getConfigData(this.el);
        datebasedMailingEditorBase.onMailingSortClick('shortname', config.sortByDate, editorsHelper);
        return false;
    });

    this.addAction({'change': 'date-mailing-editor-base-status-change'}, function () {
        datebasedMailingEditorBase.onMailingsStatusChange(this.el.val());
        return false;
    });

    this.addAction({'change': 'date-mailing-editor-base-select-change'}, function () {
        datebasedMailingEditorBase.onMailingSelectChange(this.el.val());
        return false;
    });

    this.addAction({'click': 'date-mailing-editor-base-no'}, function () {
        datebasedMailingEditorBase.transferRequiredMailingData();
        return false;
    });

    this.addAction({'click': 'date-mailing-editor-base-transfer-data'}, function () {
        datebasedMailingEditorBase.transferMailingData();
        return false;
    });

    this.addAction({'click': 'followup-mailing-editor-base-sort-shortname'}, function () {
        followupMailingEditorBase.onMailingSortClick('shortname', 'shortname', editorsHelper);
        return false;
    });

    this.addAction({'click': 'followup-mailing-editor-base-sort-data'}, function () {
        var config = getConfigData(this.el);
        followupMailingEditorBase.onMailingSortClick('shortname', config.sortByDate, editorsHelper);
        return false;
    });

    this.addAction({'change': 'followup-mailing-editor-base-status-change'}, function () {
        followupMailingEditorBase.onMailingsStatusChange(this.el.val());
        return false;
    });

    this.addAction({'change': 'followup-mailing-editor-base-select-change'}, function () {
        followupMailingEditorBase.onMailingSelectChange(this.el.val());
        return false;
    });

    this.addAction({'click': 'followup-base-mailing-editor-base-sort-shortname'}, function () {
        baseMailingEditorBase.onMailingSortClick('shortname', 'shortname');
        return false;
    });

    this.addAction({'click': 'followup-base-mailing-editor-base-sort-data'}, function () {
        var config = getConfigData(this.el);
        baseMailingEditorBase.onMailingSortClick('shortname', config.sortByDate);
        return false;
    });

    this.addAction({'change': 'followup-base-mailing-editor-base-status-change'}, function () {
        baseMailingEditorBase.onMailingsStatusChange(this.el.val());
        return false;
    });

    this.addAction({'change': 'followup-base-mailing-editor-base-select-change'}, function () {
        baseMailingEditorBase.onMailingSelectChange(this.el.val());
        return false;
    });

    this.addAction({'click': 'followup-base-mailing-editor-base-no'}, function () {
        baseMailingEditorBase.transferRequiredMailingData();
        return false;
    });

    this.addAction({'click': 'followup-base-mailing-editor-base-transfer-data'}, function () {
        baseMailingEditorBase.transferMailingData();
        return false;
    });

    this.addAction({'click': 'save-before-pdf-btn-save'}, function () {
        workflowSaveBeforePdfHandler.submitDialog(true);
        return false;
    });

    this.addAction({'click': 'save-before-pdf-btn-cancel'}, function () {
        workflowSaveBeforePdfHandler.submitDialog(false);
        return false;
    });

    this.addAction({'click': 'workflowSaveBtn'}, function () {
        submitWorkflowForm(true);
    });

    this.addAction({'click': 'workflowSaveBtnModal'}, function () {
        submitWorkflowForm(true);
    });

    this.addAction({'click': 'campaignEditorEnlarge'}, function () {
        moveCampaignEditorBodyToModal();
    });

    Action.new({'click': '[data-close-campaign-editor-modal]'}, function () {
        moveCampaignEditorBodyToPage();
    });

    this.addAction({'change': 'workflow-view-change-status'}, function () {
        if ($('#workflow_active').is(':checked')) {
            $('#workflow-status').val(constants.statusActive)
        } else {
            $('#workflow-status').val(constants.statusInactive)
        }
    });

    this.addAction({'click': 'workflowCopyBtn'}, function () {
        workflowCopyDialogHandler.showDialog(data.workflowId);
    });

    this.addAction({'click': 'workflowStatsBtn'}, function () {
        toggleStatistics(data.workflowId, this.el);
    });

    this.addAction({'change': 'start-editor-execution-changed'}, function () {
        startEditor.onExecutionChanged();
    });

    this.addAction({'change': 'start-editor-event-changed'}, function () {
        startEditor.onStartEventChanged();
    });

    this.addAction({'change': 'start-editor-type-changed'}, function () {
        startEditor.onStartTypeChanged()
    });

    this.addAction({'change': 'start-editor-rule-changed'}, function () {
        startEditor.onRuleModeChanged();
    });

    this.addAction({'change': 'start-editor-reaction-changed'}, function () {
        startEditor.onReactionChanged();
    });

    this.addAction({'change': 'start-editor-profile-field-changed'}, function () {
        startEditor.onProfileFieldChanged();
        return false;
    });

    this.addAction({'click': 'start-editor-add-rule'}, function () {
        startEditor.addRule();
        return false;
    });

    this.addAction({'click': 'start-editor-reminder-changed'}, function () {
        startEditor.onReminderChanged();
    });

    this.addAction({'click': 'start-editor-schedule-reminder-date-changed'}, function () {
        startEditor.onScheduleReminderDateChanged();
    });

    this.addAction({'click': 'start-editor-validate'}, function () {
        startEditor.validateEditor();
        return false;
    });

    this.addAction({'click': 'start-editor-mailing-sort-date'}, function () {
        startMailingSelector.onMailingSortClick('date');
        return false;
    });

    this.addAction({'change': 'start-editor-mailing-select'}, function () {
        startEditor.onMailingSelectChange(this.el.val());
        return false;
    });

    this.addAction({'click': 'start-editor-mailing-sort-shortname'}, function () {
        startMailingSelector.onMailingSortClick('shortname');
        return false;
    });

    this.addAction({'click': 'decision-editor-mailing-sort-shortname'}, function () {
        decisionMailingSelector.onMailingSortClick('shortname');
        return false;
    });

    this.addAction({'click': 'decision-editor-mailing-sort-date'}, function () {
        decisionMailingSelector.onMailingSortClick('date');
        return false;
    });

    this.addAction({'change': 'decision-editor-mailing-select'}, function () {
        decisionEditor.onMailingSelectChange(this.el.val());
        return false;
    });

    this.addAction({'click': 'start-editor-rule-remove'}, function () {
        startEditor.removeRuleRow(this.el.data('rule-index'));
        return false;
    });

    this.addAction({'change': 'start-rule-operator-change'}, function () {
        startEditor.onRuleOperatorChanged(this.el);
        return false;
    });

    this.addAction({'change': 'decision-rule-operator-change'}, function () {
        decisionEditor.onRuleOperatorChanged(this.el);
        return false;
    });

    this.addAction({'click': 'decision-editor-rule-remove'}, function () {
        decisionEditor.removeRuleRow(this.el.data('rule-index'));
        return false;
    });

    this.addAction({'click': 'decision-editor-rule-add'}, function () {
        decisionEditor.addRule();
        return false;
    });

    this.addAction({'change': 'decision-editor-type-change'}, function () {
        decisionEditor.onTypeChanged();
    });

    this.addAction({'change': 'decision-editor-criteria-change'}, function () {
        decisionEditor.onCriteriaChanged();
        return false;
    });

    this.addAction({'change': 'decision-editor-reaction-change'}, function () {
        decisionEditor.onDecisionReactionChanged();
        return false;
    });

    this.addAction({'click': 'decision-editor-save'}, function () {
        decisionEditor.validateEditor();
        return false;
    });

    this.addAction({'change': 'decision-editor-profile-field-change'}, function () {
        decisionEditor.onProfileFieldChanged();
        return false;
    });

    this.addAction({'click': 'recipient-editor-create-new-target'}, function () {
        recipientEditor.createNewTarget();
        return false;
    });

    this.addAction({'click': 'recipient-editor-save'}, function () {
        editorsHelper.saveCurrentEditor();
        return false;
    });

    this.addAction({'click': 'recipient-editor-target-edit'}, function () {
        var config = getConfigData(this.el);
        recipientEditor.editTarget(config.targetId);
        return false;
    });

    this.addAction({'click': 'parameter-editor-save'}, function () {
        editorsHelper.saveCurrentEditor();
        return false;
    });

    this.addAction({'click': 'ownworkflow-copy-dialog'}, function () {
        var config = getConfigData(this.el);
        ownWorkflowCopyDialogHandler.ownWorkflowCopyDialogResponse(config.useOriginal);
        return false;
    });

    this.addAction({'click': 'ownworkflow-editor-cancel'}, function () {
        ownWorkflowEditor.cancelEditor();
        return false;
    });

    this.addAction({'click': 'ownworkflow-editor-save'}, function () {
        ownWorkflowEditor.saveWithCheck();
        return false;
    });

    this.addAction({'click': 'editor-save-current'}, function () {
        editorsHelper.saveCurrentEditor();
        return false;
    });

    this.addAction({'click': 'deadline-editor-save'}, function () {
        deadlineEditor.validateEditor();
        return false;
    });

    this.addAction({'click': 'date-mailing-editor-save'}, function () {
        datebasedMailingEditorBase.validateEditor(datebasedMailingEditor.saveWithCheckStatus);
        return false;
    });

    this.addAction({'click': 'followup-mailing-editor-save'}, function () {
        followupMailingEditor.validateEditor(followupMailingEditor.saveWithCheckStatus);
        return false;
    });

    this.addAction({'click': 'copy-dialog-copy'}, function () {
        workflowCopyDialogHandler.workflowCopyDialogResponse(true);
        return false;
    });

    this.addAction({'click': 'copy-dialog-chain-copy'}, function () {
        workflowCopyDialogHandler.workflowCopyDialogResponse(false);
        return false;
    });

    this.addAction({'click': 'copy-dialog-cancel'}, function () {
        workflowCopyDialogHandler.workflowCopyDialogClose();
        return false;
    });

    this.addAction({'click': 'workflow-generate-pdf'}, function () {
        generatePDF();
        return false;
    });

    function BeanConstants() {

    }

    BeanConstants.prototype.setup = function (data) {
        this.startTypeOpen = data.startTypeOpen;
        this.startTypeDate = data.startTypeDate;
        this.startTypeEvent = data.startTypeEvent;
        this.startEventReaction = data.startEventReaction;
        this.startEventDate = data.startEventDate;
        this.endTypeAutomatic = data.endTypeAutomatic;
        this.endTypeDate = data.endTypeDate;

        this.deadlineTypeDelay = data.deadlineTypeDelay;
        this.deadlineTypeFixedDeadline = data.deadlineTypeFixedDeadline;
        this.deadlineTimeUnitMinute = data.deadlineTimeUnitMinute;
        this.deadlineTimeUnitHour = data.deadlineTimeUnitHour;
        this.deadlineTimeUnitDay = data.deadlineTimeUnitDay;
        this.deadlineTimeUnitWeek = data.deadlineTimeUnitWeek;
        this.deadlineTimeUnitMonth = data.deadlineTimeUnitMonth;

        // general constants
        this.reactionOpened = data.reactionOpened;
        this.reactionNotOpened = data.reactionNotOpened;
        this.reactionClicked = data.reactionClicked;
        this.reactionNotClicked = data.reactionNotClicked;
        this.reactionBought = data.reactionBought;
        this.reactionNotBought = data.reactionNotBought;
        this.reactionDownload = data.reactionDownload;
        this.reactionChangeOfProfile = data.reactionChangeOfProfile;
        this.reactionWaitingForConfirm = data.reactionWaitingForConfirm;
        this.reactionOptIn = data.reactionOptIn;
        this.reactionOptOut = data.reactionOptOut;
        this.reactionClickedLink = data.reactionClickedLink;
        this.reactionOpenedAndClicked = data.reactionOpenedAndClicked;
        this.reactionOpenedOrClicked = data.reactionOpenedOrClicked;
        this.reactionConfirmedOptIn = data.reactionConfirmedOptIn;

        constants.reactionRegistry();

        this.decisionTypeDecision = data.decisionTypeDecision;
        this.decisionTypeAutoOptimization = data.decisionTypeAutoOptimization;
        this.decisionReaction = data.decisionReaction;
        this.decisionProfileField = data.decisionProfileField;
        this.decisionAOCriteriaClickRate = data.decisionAOCriteriaClickRate;
        this.decisionAOCriteriaOpenrate = data.decisionAOCriteriaOpenrate;
        this.decisionAOCriteriaTurnover = data.decisionAOCriteriaTurnover;

        this.chainOperatorAnd = data.chainOperatorAnd;
        this.chainOperatorOr = data.chainOperatorOr;
        this.operatorIs = data.operatorIs;

        this.forwardTargetGroupCreate = data.forwardTargetGroupCreate;
        this.forwardTargetGroupEdit = data.forwardTargetGroupEdit;

        this.forwardMailingCreate = data.forwardMailingCreate;
        this.forwardMailingEdit = data.forwardMailingEdit;
        this.forwardMailingCopy = data.forwardMailingCopy;

        this.forwardUserFormCreate = data.forwardUserFormCreate;
        this.forwardUserFormEdit = data.forwardUserFormEdit;

        this.forwardReportCreate = data.forwardReportCreate;
        this.forwardReportEdit = data.forwardReportEdit;

        this.forwardAutoExportCreate = data.forwardAutoExportCreate;
        this.forwardAutoExportEdit = data.forwardAutoExportEdit;

        this.forwardAutoImportCreate = data.forwardAutoImportCreate;
        this.forwardAutoImportEdit = data.forwardAutoImportEdit;

        this.forwardArchiveCreate = data.forwardArchiveCreate;

        this.genderOptions = data.genderOptions;

        this.chainOperatorOptions = data.chainOperatorOptions;

        this.operators = data.operators;
        this.operatorsMap = data.operatorsMap;

        this.statusActive = data.statusActive;
        this.statusInactive = data.statusInactive;

        this.defaultImportDelayLimit = data.defaultImportDelayLimit;

        this.workflowURL = data.workflowURL;
        this.componentURL = data.componentURL;
    };

    BeanConstants.prototype.reactionRegistry = function () {
        nodeFactory.reactionRegistry[constants.reactionOpened] = {
            image: "reaction_opened.png",
            name: t('workflow.reaction.opened')
        };
        nodeFactory.reactionRegistry[constants.reactionNotOpened] = {
            image: "reaction_notopened.png",
            name: t('workflow.reaction.not_opened')
        };
        nodeFactory.reactionRegistry[constants.reactionClicked] = {
            image: "reaction_clicked.png",
            name: t('workflow.reaction.clicked')
        };
        nodeFactory.reactionRegistry[constants.reactionNotClicked] = {
            image: "reaction_notclicked.png",
            name: t('workflow.reaction.not_clicked')
        };
        nodeFactory.reactionRegistry[constants.reactionBought] = {
            image: "reaction_bought.png",
            name: t('workflow.reaction.bought')
        };
        nodeFactory.reactionRegistry[constants.reactionNotBought] = {
            image: "reaction_notbought.png",
            name: t('workflow.reaction.not_bought')
        };
        nodeFactory.reactionRegistry[constants.reactionChangeOfProfile] = {
            image: "reaction_profilechange.png",
            name: t('workflow.reaction.change_of_profile')
        };
        nodeFactory.reactionRegistry[constants.reactionWaitingForConfirm] = {
            image: "reaction_wfc.png",
            name: t('workflow.reaction.waiting_for_confirm')
        };
        nodeFactory.reactionRegistry[constants.reactionOptIn] = {
            image: "reaction_optin.png",
            name: t('workflow.reaction.opt_in')
        };
        nodeFactory.reactionRegistry[constants.reactionOptOut] = {
            image: "reaction_optout.png",
            name: t('workflow.reaction.opt_out')
        };
        nodeFactory.reactionRegistry[constants.reactionClickedLink] = {
            image: "reaction_clicked.png",
            name: t('workflow.reaction.clicked_on_link')
        };
        nodeFactory.reactionRegistry[constants.reactionOpenedAndClicked] = {
            image: "reaction_profilechange.png",
            name: t('workflow.reaction.opened_and_clicked')
        };
        nodeFactory.reactionRegistry[constants.reactionOpenedOrClicked] = {
            image: "reaction_profilechange.png",
            name: t('workflow.reaction.opened_or_clicked')
        };
    };

    function handleResizeCampaignEditor() {
        //we want to prevent a lot of calculation by adding timeout
        AGN.Lib.CampaignManagerService.resizeTimeoutId && clearTimeout(AGN.Lib.CampaignManagerService.resizeTimeoutId);
        AGN.Lib.CampaignManagerService.resizeTimeoutId = setTimeout(function () {
            var isModal = $('#modalCampaignEditorContainer').find($('#campaignEditorBody')).length != 0;

            //elements
            var viewPortSelector = $("#viewPort");
            var editorCanvasSelector = $("#editorCanvas");
            var toolbarLeftSelector = $("#toolbarLeft");
            //min heights for elements
            var viewPortHeight = 505;

            var width = viewPortSelector[0].clientWidth;
            var height = isModal ? $('#modalCampaignEditorContainer').height() - $('#toolbarTop').height() : viewPortSelector[0].clientHeight;
            editorCanvasSelector.width(width);
            editorCanvasSelector.height(height);
            campaignManager.setEditorCanvasWidth(width);
            campaignManager.setEditorCanvasHeight(height);
            campaignManager.setViewPortWidth(width);
            campaignManager.setViewPortHeight(height);

            //If editor isn`t in full screen mode
            if (!isModal) {
                //all this constants depend on current design
                //minimal height which needs to display collapsed tile with campaign information and tile with editor
                var minHeightOfCollapsedTiles = 808;
                //calculate minimal height for tiles according to the state of campaign information tile (collapsed/expanded)
                var minHeightOfTiles = minHeightOfCollapsedTiles + jQuery("#tile-campaignInformation").outerHeight();

                //get the current vertical position of the scroll bar for the whole document
                var scrollTop = jQuery("html").scrollTop();
                //get the current computed height for the whole document
                var documentHeight = jQuery("html").height();

                //check if we should increase min values of height for elements
                //if we have enough space we have to increase heights
                if ((documentHeight + scrollTop) > minHeightOfTiles) {
                    viewPortHeight += documentHeight + scrollTop - minHeightOfTiles;
                }

                //set heights to the elements
                toolbarLeftSelector.height(viewPortHeight);
                editorCanvasSelector.height(viewPortHeight);
                viewPortSelector.height(viewPortHeight);
            } else {
                if (height > viewPortHeight) {
                    //increase height to maximum
                    toolbarLeftSelector.height(height);
                    editorCanvasSelector.height(height);
                    viewPortSelector.height(height);
                }
            }
        }, 100);
    }

    function validateWorkflowBaseData() {
        var deferred = $.Deferred();

        if ($('#name').val().length < 3) {
            AGN.Lib.Messages(t('workflow.defaults.error'), t('error.workflow.shortName'), 'alert');
            deferred.reject();
        } else {
            if (campaignManager.checkMailingTypesConvertingRequired()) {
                return Confirm.create(Template.text('mailing-types-replace-modal'));
            } else {
                deferred.resolve();
            }
        }

        return deferred.promise();
    }

    function saveWorkflowFormData(validateNeeded) {
        var save = function() {
            var form = AGN.Lib.Form.get($("form#workflowForm"));

            form.setValueOnce("workflowSchema", campaignManager.getIconsForSubmissionJson());
            form.setValueOnce("editorPositionLeft", parseInt(campaignManager.getLeftPosition()));
            form.setValueOnce("editorPositionTop", parseInt(campaignManager.getTopPosition()));
            form.setValueOnce("workflowUndoHistoryData", campaignManager.getUndoHistoryDataForSubmission());

            campaignManager.activateIgnoreChangesThisTime();
            form.submit('static');
        };

        if (validateNeeded) {
            validateWorkflowBaseData()
                .done(save);
        } else {
            save();
        }
    }

    function toggleStatistics(workflowId, $element) {
        if (data.isStatusOpen === 'true') {
            workflownoStatisticsDialogHandler.showDialog();
        } else {
            WorkflowManagerStatistics.toggleStatistics(workflowId);
            updateStatisticsButtonLabel($element);
        }
    }

    function generatePDF() {
        var newCampaign = data.workflowId <= 0 || !data.shortName;

        var hasUnsavedChanges = campaignManager.hasUnsavedChanges();
        if (newCampaign || hasUnsavedChanges) {
            workflowSaveBeforePdfHandler.showDialog(newCampaign, hasUnsavedChanges);
        }
        else {
            window.location.href = data.pdfGenerationUrl
              .replace('{workflow-ID}', data.workflowId)
              .replace('{show-statistic}', WorkflowManagerStatistics.statisticsVisible);
            AGN.Lib.Loader.hide();
        }
    }

    function updateStatisticsButtonLabel($element) {
        var showStatistics = t('workflow.fade_in_statistics');
        var hideStatistics = t('workflow.fade_out_statistics');

        var buttonLabel = $element.html();

        if (WorkflowManagerStatistics.statisticsVisible) {
            buttonLabel = buttonLabel.replace(showStatistics, hideStatistics);
        } else {
            buttonLabel = buttonLabel.replace(hideStatistics, showStatistics);
        }

        $element.html(buttonLabel);
    }

    function submitWorkflowForm(validateNeeded) {
        //show error message for active workflow
        //if new status is not inactive
        var isActiveChecked = $('#workflow_active').is(':checked');

        var inactivating = workflowStatus === constants.statusActive && !isActiveChecked;
        var activating = workflowStatus !== constants.statusActive && isActiveChecked;

        if (inactivating || !campaignManager.checkWorkflowBeforeSave()) {
            //prepare data to show confirm dialog before workflow activation
            if (activating) {
                $("input[name='status']").val(constants.statusActive);
                var mailings = campaignManager.getMailingNames();
                $('#activating-campaign-mailings').html(mailings);
                $('#activating-campaign-dialog').css('visibility', 'visible');
                $('#activating-campaign-dialog').show();
                $('#activating-campaign-activate-button').on("click", function () {
                    saveWorkflowFormData(validateNeeded);
                    return false;
                });
                $('#activating-campaign-dialog').dialog({
                    open: function (event, ui) {
                        var title = $('#activating-campaign-dialog').parent().find('.ui-dialog-title');
                        title.empty();
                        title.append('<span class="dialog-title-image">' + t('workflow.activating.title') + '</span>');
                        title.find('.dialog-title-image').css("padding-left", "0px");
                    },
                    modal: true,
                    resizable: false,
                    width: 650,
                    minHeight: 0,
                    close: function (event, ui) {
                        return false;
                    }
                });
            } else if (inactivating) {
                $("input[name='workflow.statusString']").val(constants.statusInactive);
                $('#inactivating-campaign-dialog').css('visibility', 'visible');
                $('#inactivating-campaign-dialog').show();
                $('#inactivating-campaign-inactivate-button').on("click", function () {
                    saveWorkflowFormData(validateNeeded);
                    $('#inactivating-campaign-dialog').dialog('close');
                    return false;
                });
                $('#inactivating-campaign-dialog').dialog({
                    open: function (event, ui) {
                        var title = $('#inactivating-campaign-dialog').parent().find('.ui-dialog-title');
                        title.empty();
                        title.append('<span class="dialog-title-image">' + t('workflow.inactivating.title') + '</span>');
                        title.find('.dialog-title-image').css("padding-left", "0px");
                    },
                    modal: true,
                    resizable: false,
                    width: 650,
                    minHeight: 0,
                    close: function (event, ui) {
                        return false;
                    }
                });
            } else {
                saveWorkflowFormData(validateNeeded);
            }
        }
    }

    function moveCampaignEditorBodyToPage() {
        $('#pageCampaignEditorContainer').append($('#campaignEditorBody'));
        handleResizeCampaignEditor();
    }

    function moveCampaignEditorBodyToModal() {
        $('#modalCampaignEditorContainer').append($('#campaignEditorBody'));
        handleResizeCampaignEditor();
    }
});
