(function($) {

    var MailingEditorBase = function(data, campaignManager, submitWorkflowForm) {

        this.MAILING_TYPE_ACTIONBASED = 1;
        this.MAILING_TYPE_DATEBASED = 2;
        this.MAILING_TYPE_FOLLOWUP = 3;

        this.PARAM_MAILING_LIST = 1;
        this.PARAM_TARGET_GROUPS = 2;
        this.PARAM_ARCHIVE = 3;
        this.PARAM_LIST_SPLIT = 4;
        this.PARAM_PLANNED_DATE = 5;
        this.PARAM_SEND_DATE = 6;

        this.node = {};
        this.mailingsStatus = "all";
        this.defaultMailingsSort = data.defaultMailingsSort;
        this.defaultMailingsOrder = data.defaultMailingsOrder;
        this.mailingsSort = this.defaultMailingsSort;
        this.mailingsOrder = this.defaultMailingsOrder;
        this.formNameJId = "form[name='" + data.form + "']";
        this.containerId = data.container;
        this.mailingType = data.mailingType;
        this.mailingTypesForLoading = data.mailingTypesForLoading;
        this.statusName = data.mailingStatus;
        this.selectName = data.selectName;
        this.selectNameJId = "select[name=" + data.selectName + "]";
        this.mailingId = 0;
        this.showCreateEditLinks = data.showCreateEditLinks;
        this.dropDownEl = null;
        this.campaignManager = campaignManager;
        if (data.dropDownEl) {
            this.dropDownEl = data.dropDownEl;
        }
        //follow up base mailing settings for aadvertising content type
        this.isChangeDecisionOptions = data.isEnableTrackingVeto && data.isFollowUpBaseMailing;
        this.followUpContainer = data.followUpContainer;
        this.advertisingAdditionalOptions = data.advertisingAdditionalOptions;
        this.advertisingUrl = data.advertisingUrl;


        //chain of nodes which should be filled with mailing data
        this.nodesChain = [];
        //mailing data which we received
        this.mailingData = {};

        //collected information based on incoming nodes for mailing node
        this.configuredMailingData = null;

        var self = this;
        var editorsHelper = campaignManager.getEditorsHelper()

        this.fillEditorBase = function(node) {
            this.node = node;
            this.mailingsStatus = $(this.formNameJId).find('input[checked=checked]').val() || 'all';
            this.mailingsSort = this.defaultMailingsSort;
            this.mailingsOrder = this.defaultMailingsOrder;
            this.cleanOptions();
            this.hideSendSettings();
            this.changeDecisionOptionsSet($(this.containerId), this.isChangeDecisionOptions);
        };

        this.cleanOptions = function() {
            this.setDefaultSortOptions();
            jQuery(this.containerId + " #" + this.statusName + "_all").attr("checked", "checked");
            this.activateInputs();
            this.setSelectMailingOptions(this.node.data[this.selectName]);
            this.getMailingsByWorkStatus(this.mailingsStatus, this.mailingsSort, this.mailingsOrder, this.node.data.mailingId);
        };

        this.setDefaultSortOptions = function() {
            this.mailingsSort = this.defaultMailingsSort;
            this.mailingsOrder = this.defaultMailingsOrder;
            jQuery(this.containerId + " .arrowUp").hide();
            jQuery(this.containerId + " .arrowDown").hide();
            jQuery(this.containerId + " #shortname_sort").attr("press", 1);
            jQuery(this.containerId + " #change_date_sort").attr("press", 2);
            if ((this.mailingType == this.MAILING_TYPE_ACTIONBASED) || (this.mailingType == this.MAILING_TYPE_DATEBASED)) {
                jQuery(this.containerId + " #shortname_sort .arrowUp").show();
            } else {
                jQuery(this.containerId + " #change_date_sort .arrowDown").show();
            }
        };

        this.onMailingsStatusChange = function(val) {
            this.setSelectMailingOptions(0);
            this.getMailingsByWorkStatus(val, this.mailingsSort, this.mailingsOrder);
            this.changeDecisionOptionsSet($(this.containerId), this.isChangeDecisionOptions);
        };

        this.onMailingSelectChange = function(val) {
            this.setSelectMailingOptions(val);
            this.changeDecisionOptionsSet($(this.containerId), this.isChangeDecisionOptions);
        };

        this.changeDecisionOptionsSet = function(scope, isChangeDecisionOptions) {
            if(isChangeDecisionOptions) {
                var selectedMailingId = this.mailingId;
                var followUpSelect = $(scope.find(this.followUpContainer));
                var additionalOptions = this.advertisingAdditionalOptions;
                var advertisingUrl = this.advertisingUrl;
                jQuery.ajax({
                    action: "POST",
                    url: advertisingUrl,
                    data: {
                        mailingId: selectedMailingId
                    },
                    success: function (result) {
                        $(followUpSelect.find(".advertisingOption")).remove();
                        if (result.isAdvertisingContentType) {
                            var options = AGN.Lib.Template.text("followupAdvertisingOptions", {items: additionalOptions});
                            followUpSelect.append(options);
                        }
                    }
                });
            }
        };

        this.onMailingSortClick = function(id, val) {
            jQuery(this.containerId + " .arrowUp").hide();
            jQuery(this.containerId + " .arrowDown").hide();
            var sortElements = jQuery(this.containerId + " .sort");
            var sortId = this.containerId + " #" + id + "_sort";
            var sortEl = jQuery(sortId);
            var upSortEl = jQuery(sortId + " .arrowUp");
            var downSortEl = jQuery(sortId + " .arrowDown");
            var press = sortEl.attr("press");
            var selectedMailValue = jQuery(this.formNameJId + " " + this.selectNameJId).val();
            if (!press) {
                sortElements.attr("press", 2);
                sortEl.attr("press", 2);
                upSortEl.show();
                this.mailingsOrder = "ASC";
            } else {
                switch (press) {
                    case "1" :
                        sortEl.attr("press", 2);
                        downSortEl.show();
                        this.mailingsOrder = "DESC";
                        break;
                    case "2" :
                        sortEl.attr("press", 1);
                        upSortEl.show();
                        this.mailingsOrder = "ASC";
                        break;
                }
            }
            this.getMailingsByWorkStatus(this.mailingsStatus, val, this.mailingsOrder, selectedMailValue);
        };

        this.setMailingId = function(mailingId) {
            this.mailingId = mailingId;

            if (this.dropDownEl) {
                if (this.mailingId > 0) {
                    this.dropDownEl.show();
                } else {
                    this.dropDownEl.hide();
                }
            }

        };

        this.setSelectMailingOptions = function(mailingId) {
            this.setMailingId(mailingId);
            if (this.showCreateEditLinks) {
                this.setMailingLinks(mailingId);
            }

        };

        this.setMailingLinks = function(mailingId) {
            var mailLink = '';
            if (mailingId != "0") {
                mailLink = '<span style="font-weight: bold;">' + t('workflow.mailing.tip') + ': &nbsp;</span>' +
                    '<a href="#" data-action="mailing-editor-edit">' + t('workflow.mailing.edit_mailing_link') + '</a> ';
            } else {
                mailLink = '' +
                    '<a href="#" class="setting-mailing-left" data-action="mailing-editor-new"> ' +
                    t('workflow.mailing.new') +
                    '</a>';
            }

            var mailingLinkContainer = jQuery(this.containerId + " #mailing_create_edit_link");
            mailingLinkContainer.html("");
            mailingLinkContainer.append(mailLink);
        };

        this.showSecurityQuestion = function() {
            this.setSecurityQuestion();
            this.setIsUsedInCMAttr();

            if (this.isSentMailing(this.mailingIsSent) || this.mailingIsUsedInCM) {
                var selectedOption = this.getSelectedMailingOption();

                $(this.containerId + "-security-dialog").dialog({
                    title: '<span class="dialog-fat-title">' + selectedOption.html() + '</span>',
                    dialogClass: "no-close",
                    open: function(event, ui) {
                    },
                    width: 650,
                    modal: true,
                    resizable: false
                });
                return false;
            }

            return true;
        };

        this.isSentMailing = function(status) {
            return status == "mailing.status.sent" || status == "mailing.status.scheduled" || status == "mailing.status.norecipients";
        };

        this.cancelSecurityDialog = function() {
            jQuery(this.containerId + "-security-dialog").dialog("close");
        };

        this.getMailingsByWorkStatus = function(status, sort, order, selectedMailingValue) {
            var mailingEditorBase = this;
            var $mailings = $(this.formNameJId + ' ' + this.selectNameJId);

            $mailings.attr('readonly', 'readonly');

            jQuery.ajax({
                type: "POST",
                url: AGN.url('/workflow/getMailingsByWorkStatus.action'),
                data: {
                    mailingTypes: this.mailingTypesForLoading.join(','),
                    status: status,
                    sort: sort,
                    order: order
                },
                success: function(data) {
                    //populate the drop-down list with mailings
                    $mailings.empty();
                    $mailings.append($('<option></option>', {value: 0, text: t('workflow.defaults.no_mailing')}));

                    data.forEach(function(d) {
                        var $option = $('<option></option>', {
                            value: d.MAILING_ID,
                            text: d.SHORTNAME,
                            selected: (selectedMailingValue == d.MAILING_ID)
                        });

                        if (mailingEditorBase.isSentMailing(d.WORK_STATUS)) {
                            $option.css('color', '#808080');
                        }

                        // For back compatibility
                        $option.attr({
                            status: d.WORK_STATUS,
                            senddate: d.SENDDATE
                        });

                        $option.data({
                            'status': d.WORK_STATUS,
                            'send-date': d.SENDDATE,
                            'dependent-campaigns': d.USEDINWORKFLOWS || ''
                        });

                        $mailings.append($option);
                    });

                    $mailings.removeAttr('readonly');
                    mailingEditorBase.mailingsStatus = status;
                    mailingEditorBase.mailingsSort = sort;
                    mailingEditorBase.mailingsOrder = order;
                    $mailings.val(mailingEditorBase.mailingId);

                    editorsHelper.initSelectWithValueOrChooseFirst($mailings, mailingEditorBase.mailingId);
                }
            });
        };

        this.disableInputs = function() {
            jQuery(this.containerId + " [name='" + this.statusName + "']").attr("disabled", "disabled");
        };

        this.activateInputs = function() {
            jQuery(this.containerId + " [name='" + this.statusName + "']").removeAttr("disabled");
        };

        this.setNodeFields = function() {
            var selectedMailOption = this.getSelectedMailingOption();
            this.setNodeFilled(selectedMailOption);
            this.setNodeIconTitle(selectedMailOption);
        };

        this.setNodeFilled = function(selectedMailOption) {
            this.node.filled = false;
            if (parseInt(this.mailingId, 10) > 0) {
                this.node.filled = true;
            }
        };

        this.setNodeIconTitle = function(selectedMailOption) {
            this.node.iconTitle = "";
            if (parseInt(this.mailingId, 10) > 0) {
                this.node.iconTitle = selectedMailOption.html();
            }
        };

        this.setSecurityQuestion = function() {
            var selectedMailingOption = this.getSelectedMailingOption();
            this.mailingIsSent = "";
            if (parseInt(this.mailingId, 10) > 0) {
                this.mailingIsSent = selectedMailingOption.data('status');
            }
        };

        this.setIsUsedInCMAttr = function() {
            var selectedMailingOption = this.getSelectedMailingOption();
            var dependentCampaigns = selectedMailingOption.data('dependent-campaigns');
            var mailingId = selectedMailingOption.val();

            this.mailingIsUsedInCM = false;

            if (parseInt(this.mailingId, 10) > 0 && mailingId == this.mailingId && dependentCampaigns) {
                var workflowId = this.campaignManager.workflowId || 0;
                var campaignIds = dependentCampaigns.split(',');

                if (campaignIds.length > 0) {
                    if (workflowId > 0) {
                        this.mailingIsUsedInCM = campaignIds.indexOf(workflowId.toString()) < 0;
                    } else {
                        this.mailingIsUsedInCM = true;
                    }
                }
            }
        };

        this.getSelectedMailingOption = function() {
            return jQuery(this.formNameJId + " " + this.selectNameJId + " option[value=" + this.mailingId + "]");
        };

        this.getSelectedDecisionOption = function() {
            var selector = 'decisionCriterion';
            var val = jQuery(this.formNameJId + " select[name='" + selector + "']").val();
            return jQuery(this.formNameJId + " select[name='" + selector + "'] option[value=" + val + "]");
        };

        this.createNewMailing = function(forwardName) {
            hasMailingIconMailingList({
                iconId: this.node.id,
                successCallback: checkMailingListAndDoProcessForward,
                forwardName: forwardName,
                mailingEditorBase: this
            });
        };

        /**
         * function is called from  createNewMailing method for processing data have been got from hasMailingIconMailingList method
         *
         * @param data data have been got from hasMailingIconMailingList method
         * @param forwardName the forward action (create new mailing action and etc)
         * @param self mailingEditorBase object contains info about mailing content
         */
        var checkMailingListAndDoProcessForward = function(data, forwardName, self) {
            if (!data.hasMailingList) {
                jQuery(self.containerId + " .editor-error-messages").html(t('error.workflow.notAddedMailingList'));
                jQuery(self.containerId + " .editor-error-messages").css("display", "block");
            } else {
                var additionalParams = [];
                additionalParams.push("mailingType=" + self.mailingType);
                if (self.mailingType == self.MAILING_TYPE_FOLLOWUP) {
                    additionalParams.push("workflowFollowUpParentMailing=" + self.node.data.baseMailingId);
                    additionalParams.push("workflowFollowUpDecisionCriterion=" + self.node.data.decisionCriterion);
                }
                editorsHelper.processForward(forwardName, self.formNameJId + " " + self.selectNameJId, [],
                    submitWorkflowForm, additionalParams.join(';'));
            }
        };

        this.processCopyAndEditMailingForward = function(mailingId, forwardName, formNameJId, selectNameJId) {
            jQuery("#forwardTargetItemId").val(mailingId);
            var additionalParams = [];
            if (this.mailingType == this.MAILING_TYPE_FOLLOWUP) {
                additionalParams.push("workflowFollowUpParentMailing=" + this.node.data.baseMailingId);
                additionalParams.push("workflowFollowUpDecisionCriterion=" + this.node.data.decisionCriterion);
            }
            if (additionalParams.length > 0) {
                editorsHelper.processForward(forwardName, formNameJId + " " + selectNameJId, [], submitWorkflowForm, additionalParams.join(';'));
            } else {
                editorsHelper.processForward(forwardName, formNameJId + " " + selectNameJId, [], submitWorkflowForm);
            }
        };

        this.editMailing = function(forwardName) {
            this.processCopyAndEditMailingForward(this.mailingId, forwardName, this.formNameJId, this.selectNameJId);
        };

        this.copyMailing = function(forwardName) {
            this.processCopyAndEditMailingForward(this.mailingId, forwardName, this.formNameJId, this.selectNameJId);
        };

        this.trySupplementNodes = function(mailingContent) {
            var chains = campaignManager.getIncomingChainsForIcon();

            if (chains.length > 0) {
                var chain = chains[0];  // Take first chain.
                this.configuredMailingData = collectIncomingMailingData(chain);
                this.nodesChain = chain;
                this.mailingData = mailingContent;
                checkInconsistentMailingParams(this);
            }
        };

        /**
         * Load mailing content for mailing with id = {@code mailingId} and check its mailinglistId.
         * If we have several mailings and theirs mailinglists are different executes {@code failedCallback},
         * otherwise executes {@code successCallback}
         */
        this.checkDifferentMailingLists = function(mailingId, successCallback, failedCallback) {
            var mailingEditorBase = this;
            getMailingContent(mailingId, function(mailingContent) {
                var chain = campaignManager.getIncomingChainsForIcon()[0];

                mailingEditorBase.configuredMailingData = collectIncomingMailingData(chain);
                mailingEditorBase.nodesChain = chain;
                mailingEditorBase.mailingData = mailingContent;

                // How much mailings contains in the chain.
                var mailingIconsCount = 0;
                chain.forEach(function(icon) {
                    if (icon.type == AGN.Lib.WM.NodeFactory.NODE_TYPE_MAILING) {
                        mailingIconsCount++;
                    }
                });

                var mailinglistId = mailingEditorBase.configuredMailingData.mailinglistId;
                // If we have several mailings and their mailinglists are different we should show warning.
                if (mailingIconsCount > 1 && mailinglistId != 0 && mailinglistId != mailingContent.mailinglistId) {
                    failedCallback(mailingContent);
                } else {
                    successCallback(mailingContent);
                }
            });
        };

        var collectIncomingMailingData = function(nodesChain) {
            var configuredMailingData = {
                parameterValue: 0,
                campaignId: 0,
                mailinglistId: 0,
                targets: [],
                date: null
            };

            for(var itemNodeIndex = nodesChain.length - 1; itemNodeIndex >= 0; itemNodeIndex--) {
                var nodeData = nodesChain[itemNodeIndex].data;

                switch (nodesChain[itemNodeIndex].type) {
                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER:
                        configuredMailingData.parameterValue = nodeData.value;
                        break;

                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE:
                        configuredMailingData.campaignId = nodeData.campaignId;
                        break;

                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT:
                        if (configuredMailingData.mailinglistId == 0) {
                            configuredMailingData.mailinglistId = nodeData.mailinglistId;
                        }

                        // //add only unique target group id
                        if (nodeData.targets !== null) {
                            for(var i = 0; i < nodeData.targets.length; i++) {
                                if (!contains(configuredMailingData.targets, nodeData.targets[i])) {
                                    configuredMailingData.targets.push(parseInt(nodeData.targets[i]));
                                }
                            }
                        }
                        break;


                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_START:
                        if (nodeData.date) {
                            configuredMailingData.date = excludeTime(nodeData.date);
                            configuredMailingData.date.setHours(nodeData.hour);
                            configuredMailingData.date.setMinutes(nodeData.minute);
                        } else {
                            configuredMailingData.date = null;
                        }
                        break;

                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_DEADLINE:
                        if (nodeData.deadlineType == constants.deadlineTypeFixedDeadline) {
                            if (nodeData.date) {
                                configuredMailingData.date = excludeTime(nodeData.date);
                                configuredMailingData.date.setHours(nodeData.hour);
                                configuredMailingData.date.setMinutes(nodeData.minute);
                            } else {
                                configuredMailingData.date = null;
                            }
                        } else if (nodeData.deadlineType == constants.deadlineTypeDelay) {
                            // add delay
                            if (configuredMailingData.date != null) {
                                if (nodeData.timeUnit == constants.deadlineTimeUnitMinute) {
                                    configuredMailingData.date.setMinutes(configuredMailingData.date.getMinutes() + parseInt(nodeData.delayValue));
                                } else if (nodeData.timeUnit == constants.deadlineTimeUnitHour) {
                                    configuredMailingData.date.setHours(configuredMailingData.date.getHours() + parseInt(nodeData.delayValue));
                                } else if (nodeData.timeUnit == constants.deadlineTimeUnitDay) {
                                    configuredMailingData.date.setDate(configuredMailingData.date.getDate() + parseInt(nodeData.delayValue));
                                    if (nodeData.useTime || nodeData.useTime == 'true') {
                                        configuredMailingData.date.setMinutes(configuredMailingData.date.getMinutes() + parseInt(nodeData.minute));
                                        configuredMailingData.date.setHours(configuredMailingData.date.getHours() + parseInt(nodeData.hour));
                                    }
                                }
                            }
                        }
                        break;
                }
            }

            return configuredMailingData;
        };

        var checkInconsistentMailingParams = function() {
            var paramsToBeAsked = getMailingParams(self, true);

            if (paramsToBeAsked.length > 0) {
                // Dialog with question
                initTransferDialog(self);
            } else {
                // Populate node with necessary data
                self.supplementNewOrUpdateExistingNodes();
            }
        };

        var getMailingParams = function(mailingEditorBase, askAmbiguous) {
            var configuredMailingData = mailingEditorBase.configuredMailingData;
            var mailingData = mailingEditorBase.mailingData;

            var paramsToBeTransferred = [];
            var paramsToBeAsked = [];
            var paramsToBeOverridden = [];

            if (mailingData.mailinglistId == 0) {
                paramsToBeOverridden.push(mailingEditorBase.PARAM_MAILING_LIST);
            } else {
                if (configuredMailingData.mailinglistId == 0) {
                    // Transfer
                    paramsToBeTransferred.push(mailingEditorBase.PARAM_MAILING_LIST);
                } else {
                    if (configuredMailingData.mailinglistId != mailingData.mailinglistId) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(mailingEditorBase.PARAM_MAILING_LIST);
                        } else {
                            paramsToBeTransferred.push(mailingEditorBase.PARAM_MAILING_LIST);
                        }
                    }
                }
            }

            var configuredTargets = configuredMailingData.targets;
            var mailingTargets = mailingData.targetGroupIds;

            if (mailingTargets == null || mailingTargets.length == 0) {
                paramsToBeOverridden.push(mailingEditorBase.PARAM_TARGET_GROUPS);
            } else {
                if (configuredTargets == null || configuredTargets.length == 0) {
                    // Transfer
                    paramsToBeTransferred.push(mailingEditorBase.PARAM_TARGET_GROUPS);
                } else {
                    if (!equalTargetGroups(configuredMailingData, mailingData)) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(mailingEditorBase.PARAM_TARGET_GROUPS);
                        } else {
                            paramsToBeTransferred.push(mailingEditorBase.PARAM_TARGET_GROUPS);
                        }
                    }
                }
            }

            var parameterValue = mailingEditorBase.getParameterValue(mailingData.splitBase, mailingData.splitPart);

            if (parameterValue == null) {
                paramsToBeOverridden.push(mailingEditorBase.PARAM_LIST_SPLIT);
            } else {
                if (configuredMailingData.parameterValue == 0) {
                    // Transfer
                    paramsToBeTransferred.push(mailingEditorBase.PARAM_LIST_SPLIT);
                } else {
                    if (configuredMailingData.parameterValue != parameterValue) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(mailingEditorBase.PARAM_LIST_SPLIT);
                        } else {
                            paramsToBeTransferred.push(mailingEditorBase.PARAM_LIST_SPLIT);
                        }
                    }
                }
            }

            if (mailingData.campaignId == 0) {
                paramsToBeOverridden.push(mailingEditorBase.PARAM_ARCHIVE);
            } else {
                if (configuredMailingData.campaignId == 0) {
                    // Transfer
                    paramsToBeTransferred.push(mailingEditorBase.PARAM_ARCHIVE);
                } else {
                    if (configuredMailingData.campaignId != mailingData.campaignId) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(mailingEditorBase.PARAM_ARCHIVE);
                        } else {
                            paramsToBeTransferred.push(mailingEditorBase.PARAM_ARCHIVE);
                        }
                    }
                }
            }

            var configuredDate = configuredMailingData.date;
            var mailingPlannedDate = extractMailingPlannedDate(mailingData);
            var mailingSendDate = extractMailingSendDate(mailingData);

            if (mailingPlannedDate == null && mailingSendDate == null) {
                paramsToBeOverridden.push(mailingEditorBase.PARAM_PLANNED_DATE);
            } else {
                if (configuredDate == null) {
                    // Transfer
                    if (mailingPlannedDate != null) {
                        paramsToBeTransferred.push(mailingEditorBase.PARAM_PLANNED_DATE);
                    } else {
                        paramsToBeTransferred.push(mailingEditorBase.PARAM_SEND_DATE);
                    }
                } else {
                    if (excludeTime(mailingPlannedDate) > excludeTime(configuredDate)) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(mailingEditorBase.PARAM_PLANNED_DATE);
                        } else {
                            paramsToBeTransferred.push(mailingEditorBase.PARAM_PLANNED_DATE);
                        }
                    } else if (mailingPlannedDate && excludeTime(mailingPlannedDate) < excludeTime(configuredDate)) {
                        paramsToBeOverridden.push(mailingEditorBase.PARAM_PLANNED_DATE);
                    }

                    if (mailingSendDate > configuredDate) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(mailingEditorBase.PARAM_SEND_DATE);
                        } else {
                            paramsToBeTransferred.push(mailingEditorBase.PARAM_SEND_DATE);
                        }
                    } else if (mailingSendDate && mailingSendDate < configuredDate) {
                        paramsToBeOverridden.push(mailingEditorBase.PARAM_SEND_DATE);
                    }
                }
            }

            overrideMailingData(mailingEditorBase, paramsToBeOverridden);

            return askAmbiguous ? paramsToBeAsked : paramsToBeTransferred;
        };

        var overrideMailingData = function(mailingEditorBase, paramsToBeOverridden) {
            var configuredMailingData = mailingEditorBase.configuredMailingData;
            var mailingData = mailingEditorBase.mailingData;

            for(var i = 0; i < paramsToBeOverridden.length; i++) {
                switch (paramsToBeOverridden[i]) {
                    case mailingEditorBase.PARAM_MAILING_LIST:
                        mailingData.mailinglistId = configuredMailingData.mailinglistId;
                        break;

                    case mailingEditorBase.PARAM_TARGET_GROUPS:
                        mailingData.targetGroupIds = configuredMailingData.targets;
                        break;

                    case mailingEditorBase.PARAM_ARCHIVE:
                        mailingData.campaignId = configuredMailingData.campaignId;
                        break;

                    case mailingEditorBase.PARAM_LIST_SPLIT:
                        // TODO: find out how can I override parameter
                        mailingData.splitBase = "";
                        mailingData.splitPart = "";
                        break;

                    case mailingEditorBase.PARAM_PLANNED_DATE:
                        var campaignDate = excludeTime(configuredMailingData.date);
                        if (campaignDate) {
                            mailingData.planDate = campaignDate.getTime();
                        } else {
                            mailingData.planDate = 0;
                        }
                        break;

                    case mailingEditorBase.PARAM_SEND_DATE:
                        mailingData.sendDate = excludeTime(configuredMailingData.date);
                        if (configuredMailingData.date) {
                            mailingData.sendHour = configuredMailingData.date.getHours();
                            mailingData.sendMinute = configuredMailingData.date.getMinutes();
                        }
                        break;
                }
            }
        };

        var getMailingContent = function(mailingId, successCallback) {
            jQuery.ajax({
                type: "POST",
                url: AGN.url('/workflow/getMailingContent.action'),
                data: {
                    mailingId: mailingId
                },
                success: function(data) {
                    successCallback(data);
                }
            });
        };

        // Method get true if recipient icon has mailing list for mailing with iconId
        var hasMailingIconMailingList = function(options) {
            var forIconId = options.iconId || editorsHelper.curEditingNode.id;
            var successCallback = options.successCallback;
            var forwardName = options.forwardName;

            var icons = campaignManager.getIconsForSubmission();
            var iconMap = {};  // iconId -> icon
            var connectionBackMap = {};  // target -> {source...}

            icons.forEach(function(icon) {
                iconMap[icon.id] = icon;

                // Generate reversed connection map (target icon id to object having source icon ids as keys).
                if (icon.connections) {
                    icon.connections.forEach(function(connection) {
                        var sourceId = icon.id;
                        var targetId = connection.targetIconId;

                        if (!connectionBackMap[targetId]) {
                            connectionBackMap[targetId] = {};
                        }

                        connectionBackMap[targetId][sourceId] = true;
                    });
                }
            });

            function checkHasMailinglist(iconId) {
                var previousIds = Object.keys(connectionBackMap[iconId] || {});
                var visitedIds = {};

                while (previousIds.length > 0) {
                    var ids = previousIds;

                    previousIds = [];

                    for(var i = 0; i < ids.length; i++) {
                        var id = ids[i];
                        var icon = iconMap[id];

                        if (icon.type === AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT) {
                            return (icon.mailinglistId > 0);
                        }

                        visitedIds[id] = true;

                        Object.keys(connectionBackMap[id] || {}).forEach(function(previousId) {
                            if (!visitedIds[previousId]) {
                                previousIds.push(previousId);
                            }
                        });
                    }
                }

                return false;
            }

            successCallback({hasMailingList: checkHasMailinglist(forIconId)}, forwardName, options.mailingEditorBase);
        };

        this.closeTransferDialog = function() {
            jQuery(this.containerId + "-transfer-dialog").dialog("close");
        };

        this.transferMailingData = function() {

            var paramsToBeAsked = getMailingParams(self, true);
            var paramsToBeOverridden = [];
            var mailingEditorBase = self;

            jQuery(this.containerId + "-transfer-dialog input[name^=transfer]").each(function() {
                var element = jQuery(this);

                if (!element[0].checked) {
                    var param = null;

                    switch (element.attr("name")) {
                        case "transferPlanedFor":
                            param = mailingEditorBase.PARAM_PLANNED_DATE;
                            break;
                        case "transferMailingList":
                            param = mailingEditorBase.PARAM_MAILING_LIST;
                            break;
                        case "transferTargetGroups":
                            param = mailingEditorBase.PARAM_TARGET_GROUPS;
                            break;
                        case "transferArchive":
                            param = mailingEditorBase.PARAM_ARCHIVE;
                            break;
                        case "transferListSplit":
                            param = mailingEditorBase.PARAM_LIST_SPLIT;
                            break;
                        case "transferDeliveryTime":
                            param = mailingEditorBase.PARAM_SEND_DATE;
                            break;
                    }

                    if (param && contains(paramsToBeAsked, param)) {
                        paramsToBeOverridden.push(param);
                    }
                }
            });

            overrideMailingData(self, paramsToBeOverridden);
            self.supplementNewOrUpdateExistingNodes();

            this.closeTransferDialog();
        };

        this.transferRequiredMailingData = function() {
            var paramsToBeAsked = getMailingParams(this, true);

            overrideMailingData(this, paramsToBeAsked);
            this.supplementNewOrUpdateExistingNodes();

            this.closeTransferDialog();
        };

        this.supplementNewOrUpdateExistingNodes = function() {
            var mailingEditorBase = this;
            var direction = getDirectionByUsedAnchors(editorsHelper.curEditingNode.usedAnchors, false);
            if (this.nodesChain.length > 1) { //first node is mailing
                direction = getDirectionByPosition(editorsHelper.curEditingNode, this.nodesChain[1]);
            }

            var mailingTypes = [
                AGN.Lib.WM.NodeFactory.NODE_TYPE_MAILING,
                AGN.Lib.WM.NodeFactory.NODE_TYPE_ACTION_BASED_MAILING,
                AGN.Lib.WM.NodeFactory.NODE_TYPE_DATE_BASED_MAILING,
                AGN.Lib.WM.NodeFactory.NODE_TYPE_FOLLOWUP_MAILING
            ];

            //try to find mailing in incoming chain
            var firstMailingNodeIndex = 0;
            for(var i = 1; i < mailingEditorBase.nodesChain.length; i++) {
                if (mailingTypes.indexOf(mailingEditorBase.nodesChain[i].type) >= 0) {
                    firstMailingNodeIndex = i;
                    break;
                }
            }

            var paramsToBeTransferred = getMailingParams(this, false);

            //check if we have more than one mailing
            if (firstMailingNodeIndex > 0) {
                this.supplementNodesBetweenMailings(paramsToBeTransferred, firstMailingNodeIndex, direction);
            } else {
                this.supplementNodesBeforeMailing(paramsToBeTransferred, direction);
            }
        };

        this.supplementNodesBetweenMailings = function(params, firstMailingNodeIndex, direction) {
            var mailingEditorBase = self;
            var coordinate = {
                x: editorsHelper.curEditingNode.x,
                y: editorsHelper.curEditingNode.y
            };

            var addedNodeInfos = [];
            var mailingData = mailingEditorBase.mailingData;

            // Parameter
            if (contains(params, this.PARAM_LIST_SPLIT)) {
                var nodeType = AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER;
                var stopIndex = firstMailingNodeIndex > 0 ? firstMailingNodeIndex : mailingEditorBase.nodesChain.length;
                var availableTypesAfter = [];
                this.updateExistingOrAddNewNodeForThanOneValue(nodeType, stopIndex, coordinate, direction, availableTypesAfter, addedNodeInfos,
                    function(node) {
                        node.data.value = mailingEditorBase.getParameterValue(mailingData.splitBase, mailingData.splitPart);
                    }
                );
            }

            // Archive
            if (contains(params, this.PARAM_ARCHIVE)) {
                var nodeType = AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE;
                var stopIndex = firstMailingNodeIndex > 0 ? firstMailingNodeIndex : mailingEditorBase.nodesChain.length;
                var availableTypesAfter = [AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER];
                this.updateExistingOrAddNewNodeForThanOneValue(nodeType, stopIndex, coordinate, direction, availableTypesAfter, addedNodeInfos,
                    function(node) {
                        node.data.campaignId = mailingData.campaignId;
                    }
                );
            }

            // Recipient
            if (contains(params, this.PARAM_MAILING_LIST) || contains(params, this.PARAM_TARGET_GROUPS)) {
                var nodeType = AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT;
                var stopIndex = firstMailingNodeIndex > 0 ? firstMailingNodeIndex : mailingEditorBase.nodesChain.length;
                var availableTypesAfter = [AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER, AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE];
                this.updateExistingOrAddNewNodeForThanOneValue(nodeType, stopIndex, coordinate, direction, availableTypesAfter, addedNodeInfos,
                    function(node) {
                        if (contains(params, mailingEditorBase.PARAM_MAILING_LIST)) {
                            node.data.mailinglistId = mailingData.mailinglistId;
                            node.isDependent = true;
                        }

                        if (contains(params, mailingEditorBase.PARAM_TARGET_GROUPS)) {
                            node.data.targets = mailingData.targetGroupIds;
                            node.data.targetsOption = 'ONE_TARGET_REQUIRED';
                        }
                    }
                );
            }

            // Deadline
            if (contains(params, this.PARAM_PLANNED_DATE) || contains(params, this.PARAM_SEND_DATE)) {
                var nodeType = AGN.Lib.WM.NodeFactory.NODE_TYPE_DEADLINE;
                var stopIndex = firstMailingNodeIndex > 0 ? firstMailingNodeIndex : mailingEditorBase.nodesChain.length;
                var availableTypesAfter = [AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER, AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE, AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT];
                this.updateExistingOrAddNewNodeForThanOneValue(nodeType, stopIndex, coordinate, direction, availableTypesAfter, addedNodeInfos,
                    function(node) {
                        if (contains(params, mailingEditorBase.PARAM_PLANNED_DATE)) {
                            node.data.date = extractMailingPlannedDate(mailingEditorBase.mailingData);
                            var configuredDate = mailingEditorBase.configuredMailingData.date;
                            if (configuredDate) {
                                node.data.date.setHours(configuredDate.getHours());
                                node.data.date.setMinutes(configuredDate.getMinutes());
                            }
                        } else if (contains(params, mailingEditorBase.PARAM_SEND_DATE)) {
                            node.data.date = extractMailingSendDate(mailingEditorBase.mailingData);
                        }

                        node.data.deadlineType = constants.deadlineTypeFixedDeadline;
                        node.data.hour = node.data.date.getHours();
                        node.data.minute = node.data.date.getMinutes();
                    }
                );
            }

            var nodeIdPrefix = campaignManager.getCMNodes().getNodeIdPrefix();
            if (addedNodeInfos.length > 0) {
                var prevState = campaignManager.getCurrentState();
                campaignManager.setCurrentState(campaignManager.STATE_AUTOMATICALLY_CREATING_CONNECTION);
                for(var i = 0; i < addedNodeInfos.length; i++) {
                    connectNodes(addedNodeInfos[i].newNode, addedNodeInfos[i].nextNodeId);
                    connectNodes(campaignManager.getCMNodes().getNodeById(nodeIdPrefix + addedNodeInfos[i].prevNodeId), addedNodeInfos[i].newNode.id);

                    refreshNode(addedNodeInfos[i].newNode);
                }
                campaignManager.setCurrentState(prevState);
            }
        };

        this.supplementNodesBeforeMailing = function(params, direction) {
            var mailingEditorBase = self;
            var previousNodes = getSupplementedNodeChain(this.nodesChain, params);

            if (this.nodesChain.length - 1 < previousNodes.length) {
                // delete existing nodes in chain without first node (mailing)
                for(var j = 1; j < this.nodesChain.length; j++) {
                    campaignManager.deleteNode(campaignManager.getCMNodes().getNodeIdPrefix() + this.nodesChain[j].id, true, true);
                }
                // after deletion of nodes, coordinate of editing node will change
            }

            var coordinate = {
                x: editorsHelper.curEditingNode.x,
                y: editorsHelper.curEditingNode.y
            };

            var neighbours = {nextNode: editorsHelper.curEditingNode};
            var isNodeExists = false;
            var addedNodeInfos = [];

            //iterate through available nodes before mailing and add or update if it is need
            for(var nodeIndex = 0; nodeIndex < previousNodes.length; nodeIndex++) {
                isNodeExists = false;
                switch (previousNodes[nodeIndex]) {
                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER:
                        for(var i = 0; i < this.nodesChain.length; i++) {
                            if (this.nodesChain[i].type == AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER) {
                                isNodeExists = true;
                                break;
                            }
                        }
                        if (contains(params, this.PARAM_LIST_SPLIT) || isNodeExists) {
                            var newNodeInfo = this.updateExistingOrAddNewNode(neighbours, coordinate, direction, AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER,
                                function(node, nodeToReAdd) {
                                    if (nodeToReAdd) {
                                        node.data = nodeToReAdd.data;
                                    }

                                    if (contains(params, mailingEditorBase.PARAM_LIST_SPLIT)) {
                                        node.data.value = mailingEditorBase.getParameterValue(mailingEditorBase.mailingData.splitBase, mailingEditorBase.mailingData.splitPart);
                                        refreshNode(node);
                                    }
                                }
                            );
                            if (newNodeInfo) {
                                addedNodeInfos.push(newNodeInfo);
                            }
                        }
                        break;

                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE:
                        for(var i = 0; i < this.nodesChain.length; i++) {
                            if (this.nodesChain[i].type == AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE) {
                                isNodeExists = true;
                                break;
                            }
                        }
                        if (contains(params, this.PARAM_ARCHIVE) || isNodeExists) {
                            var newNodeInfo = this.updateExistingOrAddNewNode(neighbours, coordinate, direction, AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE,
                                function(node, nodeToReAdd) {
                                    if (nodeToReAdd) {
                                        node.data = nodeToReAdd.data;
                                    }

                                    if (contains(params, mailingEditorBase.PARAM_ARCHIVE)) {
                                        node.data.campaignId = mailingEditorBase.mailingData.campaignId;
                                        refreshNode(node);
                                    }
                                }
                            );
                            if (newNodeInfo) {
                                addedNodeInfos.push(newNodeInfo);
                            }
                        }
                        break;

                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT:
                        for(var i = 0; i < this.nodesChain.length; i++) {
                            if (this.nodesChain[i].type == AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT) {
                                isNodeExists = true;
                                break;
                            }
                        }
                        if (contains(params, this.PARAM_MAILING_LIST) || contains(params, this.PARAM_TARGET_GROUPS) || isNodeExists) {
                            var newNodeInfo = this.updateExistingOrAddNewNode(neighbours, coordinate, direction, AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT,
                                function(node, nodeToReAdd) {
                                    if (nodeToReAdd) {
                                        node.data = nodeToReAdd.data;
                                    }

                                    if (contains(params, mailingEditorBase.PARAM_MAILING_LIST)) {
                                        node.data.mailinglistId = mailingEditorBase.mailingData.mailinglistId;
                                        refreshNode(node);
                                    }

                                    if (contains(params, mailingEditorBase.PARAM_TARGET_GROUPS)) {
                                        node.data.targets = mailingEditorBase.mailingData.targetGroupIds;
                                        node.data.targetsOption = 'ONE_TARGET_REQUIRED';
                                        refreshNode(node);
                                    }
                                }
                            );
                            if (newNodeInfo) {
                                addedNodeInfos.push(newNodeInfo);
                            }
                        }
                        break;

                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_START:
                        for(var i = 0; i < this.nodesChain.length; i++) {
                            if (this.nodesChain[i].type == AGN.Lib.WM.NodeFactory.NODE_TYPE_START) {
                                isNodeExists = true;
                                break;
                            }
                        }
                        var startNodes = campaignManager.getCMNodes().getNodesByType(AGN.Lib.WM.NodeFactory.NODE_TYPE_START);
                        if (startNodes.length == 0
                            && this.mailingData.mailingType != this.MAILING_TYPE_ACTIONBASED
                            && this.mailingData.mailingType != this.MAILING_TYPE_DATEBASED
                            && (contains(params, this.PARAM_PLANNED_DATE) || contains(params, this.PARAM_SEND_DATE))
                            || isNodeExists
                        ) {
                            var newNodeInfo = this.updateExistingOrAddNewNode(neighbours, coordinate, direction, AGN.Lib.WM.NodeFactory.NODE_TYPE_START,
                                function(node, nodeToReAdd) {
                                    if (nodeToReAdd) {
                                        node.filled = nodeToReAdd.filled;
                                        node.data = nodeToReAdd.data;
                                    }

                                    if (contains(params, mailingEditorBase.PARAM_PLANNED_DATE)) {
                                        node.data.date = extractMailingPlannedDate(mailingEditorBase.mailingData);
                                        var configuredDate = mailingEditorBase.configuredMailingData.date;
                                        if (configuredDate) {
                                            node.data.hour = configuredDate.getHours();
                                            node.data.minute = configuredDate.getMinutes();
                                        }
                                        refreshNode(node);
                                    } else if (contains(params, mailingEditorBase.PARAM_SEND_DATE)) {
                                        node.data.date = extractMailingSendDate(mailingEditorBase.mailingData);
                                        node.data.hour = node.data.date.getHours();
                                        node.data.minute = node.data.date.getMinutes();
                                        refreshNode(node);
                                    }
                                }
                            );
                            if (newNodeInfo) {
                                addedNodeInfos.push(newNodeInfo);
                            }
                        }
                        break;

                    default:
                        for(var i = 0; i < this.nodesChain.length; i++) {
                            if (this.nodesChain[i].type == previousNodes[nodeIndex]) {
                                isNodeExists = true;
                                break;
                            }
                        }

                        if (isNodeExists) {
                            var newNodeInfo = this.updateExistingOrAddNewNode(neighbours, coordinate, direction, previousNodes[nodeIndex],
                                function(node, nodeToReAdd) {
                                    if (nodeToReAdd) {
                                        node.data = nodeToReAdd.data;
                                        refreshNode(node);
                                    }
                                }
                            );
                            if (newNodeInfo) {
                                addedNodeInfos.push(newNodeInfo);
                            }
                        }
                        break;
                }
            }

            if (addedNodeInfos.length > 0) {
                //set special state for preventing unnecessary calling updateEditorCanvas
                var prevState = campaignManager.getCurrentState();
                campaignManager.setCurrentState(campaignManager.STATE_AUTOMATICALLY_CREATING_CONNECTION);
                for(var i = 0; i < addedNodeInfos.length; i++) {
                    connectNodes(addedNodeInfos[i].node, addedNodeInfos[i].nextNode.id);
                    var isNeedRefreshNode = false;
                    switch (addedNodeInfos[i].node.type) {
                        case AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER:
                            if (addedNodeInfos[i].node.data.value != 0) {
                                isNeedRefreshNode = true;
                            }
                            break;
                        case AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE:
                            if (addedNodeInfos[i].node.data.campaignId > 0) {
                                isNeedRefreshNode = true;
                            }
                            break;
                        case AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT:
                            if (addedNodeInfos[i].node.data.mailinglistId > 0
                                || addedNodeInfos[i].node.data.targets.length > 0
                            ) {
                                isNeedRefreshNode = true;
                            }
                            break;
                        case AGN.Lib.WM.NodeFactory.NODE_TYPE_START:
                            if (addedNodeInfos[i].node.data.date != null) {
                                isNeedRefreshNode = true;
                            }
                            break;
                    }
                    if (isNeedRefreshNode) {
                        refreshNode(addedNodeInfos[i].node);
                    } else {
                        campaignManager.updateNode(addedNodeInfos[i].node);
                    }
                }

                //restore previous state
                campaignManager.setCurrentState(prevState);
            }
        };

        this.updateExistingOrAddNewNode = function(neighbours, coordinate, direction, nodeType, updateNodeDataFunction) {
            var isNodeExists = false;
            var nodeToReAdd = false;
            var nodeIdPrefix = campaignManager.getCMNodes().getNodeIdPrefix();
            for(var index = 0; index < this.nodesChain.length; index++) {
                //if node exists in chain we should just update data
                if (this.nodesChain[index].type == nodeType) {
                    if (campaignManager.getCMNodes().getNodeById(nodeIdPrefix + this.nodesChain[index].id)) {
                        updateNodeDataFunction(this.nodesChain[index], false);
                        isNodeExists = true;
                        neighbours.nextNode = this.nodesChain[index];
                        return null;
                    } else { //we have deleted existing node and have to add it
                        nodeToReAdd = this.nodesChain[index];
                    }
                }
            }

            if (!isNodeExists) {
                var node = addNewNode(nodeType, coordinate, direction, true);

                if (nodeToReAdd) {
                    updateNodeDataFunction(node, nodeToReAdd);
                } else {
                    updateNodeDataFunction(node, false);
                }

                var nodeInfo = {
                    nextNode: neighbours.nextNode,
                    node: node
                };
                neighbours.nextNode = node;
            }
            return nodeInfo;
        };

        this.updateExistingOrAddNewNodeForThanOneValue = function(nodeType, stopIndex, coordinate, direction, availableTypesAfter, addedNodeInfos, updateDataFunction) {
            var mailingEditorBase = self;
            var nodeIdPrefix = campaignManager.getCMNodes().getNodeIdPrefix();
            var isNodeUpdated = false;
            for(var i = 1; i < stopIndex; i++) {
                if (mailingEditorBase.nodesChain[i].type == nodeType) {
                    updateDataFunction(mailingEditorBase.nodesChain[i]);
                    refreshNode(mailingEditorBase.nodesChain[i]);
                    isNodeUpdated = true;
                    break;
                }
            }

            if (!isNodeUpdated) {
                var node = addNewNode(nodeType, coordinate, direction, true);
                updateDataFunction(node);
                for(var i = 1; i < stopIndex; i++) {
                    if (availableTypesAfter.indexOf(mailingEditorBase.nodesChain[i].type) == -1 || i == (stopIndex - 1)) {
                        if (addedNodeInfos.length == 0) {
                            addedNodeInfos.push({
                                'newNode': node,
                                'nextNodeId': mailingEditorBase.nodesChain[i - 1].id,
                                'prevNodeId': mailingEditorBase.nodesChain[i].id
                            });

                            //delete connection between
                            var connections = campaignManager.getNodeOutgoingConnections(mailingEditorBase.nodesChain[i]);
                            for(var j = 0, n = connections.length; j < n; j++) {
                                if (connections[j].target == nodeIdPrefix + mailingEditorBase.nodesChain[i - 1].id) {
                                    campaignManager.deleteConnectionByIdsOfElements(connections[j], true);
                                }
                            }
                        } else {
                            var lastAddedNodeInfo = addedNodeInfos[addedNodeInfos.length - 1];
                            addedNodeInfos.push({
                                'newNode': node,
                                'nextNodeId': lastAddedNodeInfo.newNode.id,
                                'prevNodeId': lastAddedNodeInfo.prevNodeId
                            });
                            lastAddedNodeInfo.prevNodeId = node.id;
                        }
                    }
                }
            }
        };

        this.closeOneMailinglistWarningDialog = function() {
            jQuery(this.containerId + "-oneMailinglistWarning-dialog").dialog("close");
        };

        this.acceptOneMailinglistPerCampaign = function() {
            this.closeOneMailinglistWarningDialog();
            editorsHelper.mailingSpecificSaveAfterMailinglistCheckModal(self);
        };

        this.validateEditor = function(save) {
            var errorContainer = jQuery(this.containerId + " .editor-error-messages");
            errorContainer.html("");
            errorContainer.css("display", "none");
            var errorsFound = false;
            // validate delivery settings
            if (jQuery(this.containerId + " .delivery-settings").length) {
                var maxRecipients = jQuery(this.containerId + " [name='maxRecipients']").val();
                if (parseInt(maxRecipients) != maxRecipients) {
                    errorContainer.html(t('error.workflow.wrongMaxRecipientsFormat'));
                    errorsFound = true;
                }
                else if (parseInt(maxRecipients) < 0) {
                    errorContainer.html(t('error.workflow.maxRecipientsLessThanZero'));
                    errorsFound = true;
                }
                else if (parseInt(maxRecipients) > 1000000000) {
                    errorContainer.html(t('error.workflow.maxRecipientsTooBig'));
                    errorsFound = true;
                }
            }
            // validate that mailing is selected
            var mailingSelector = jQuery(this.formNameJId + " " + this.selectNameJId);
            if (mailingSelector.val() <= 0) {
                if (errorContainer.html() != "") {
                    errorContainer.html(errorContainer.html() + "<br>");
                }
                errorContainer.html(errorContainer.html() + t('error.workflow.noMailing'));
                errorsFound = true;
            }

            if (!errorsFound) {
                if (save) {
                    save();
                } else {
                    editorsHelper.saveCurrentEditor();
                }
            }
            else {
                errorContainer.css("display", "block");
            }
        };

        this.toggleSendSettings = function(editorId) {
            if (jQuery("#sendSettings_" + editorId).css("display") == "none") {
                jQuery("#sendSettingsToggle_" + editorId).removeClass("toggle_closed");
                jQuery("#sendSettingsToggle_" + editorId).addClass("toggle_open");
            }
            else {
                jQuery("#sendSettingsToggle_" + editorId).removeClass("toggle_open");
                jQuery("#sendSettingsToggle_" + editorId).addClass("toggle_closed");
            }
            jQuery("#sendSettings_" + editorId).toggle("blind", 200);
        };

        this.hideSendSettings = function() {
            jQuery(".wm-mailing-send-settings-link").removeClass("toggle_open");
            jQuery(".wm-mailing-send-settings-link").addClass("toggle_closed");
            jQuery(".wm-mailing-send-settings").css("display", "none");
        };

        this.getParameterValue = function(splitBase, splitPart) {
            if (splitBase.length > 4) { //__listsplit_1010101060_5
                for(var i = 0; i < splitBase.length / 2; i++) {
                    if (i + 1 == splitPart) {
                        return splitBase.substring(i * 2, (i + 1) * 2);
                    }
                }
            } else { //__listsplit_1090_90
                for(var i = 0; i < splitBase.length / 2; i++) {
                    if (splitBase.substring(i * 2, (i + 1) * 2) == splitPart) {
                        return splitBase.substring(i * 2, (i + 1) * 2);
                    }
                }
            }
            return null;
        };

        /**
         *
         * @param type type of node to add
         * @param coordinate object contained x and y coordinate
         * @param direction (left, top, right, bottom) see CampaignManagerNodes
         * @param force boolean should we change coordinate and add new node to the nearest free place
         * @return new node
         */
        var addNewNode = function(type, coordinate, direction, force) {
            var node = null;
            do {
                coordinate = getCoordinateByDirection(coordinate, direction);
                node = campaignManager.getCMNodes().addNode(
                    type,
                    coordinate.x,
                    coordinate.y
                );
            } while (node === false && force);
            return node;
        };

        var getCoordinateByDirection = function(coordinate, direction) {
            if (direction == campaignManager.getCMNodes().LEFT) {
                coordinate.x--;
            } else if (direction == campaignManager.getCMNodes().RIGHT) {
                coordinate.x++;
            } else if (direction == campaignManager.getCMNodes().TOP) {
                coordinate.y--;
            } else if (direction == campaignManager.getCMNodes().BOTTOM) {
                coordinate.y++;
            }
            return coordinate;
        };

        var connectNodes = function(node, targetNodeId) {
            var nodeIdPrefix = campaignManager.getCMNodes().getNodeIdPrefix();
            if (node) {
                campaignManager.connectNodes(nodeIdPrefix + node.id, nodeIdPrefix + targetNodeId, true);
                campaignManager.justifyNodeElement(node);
            }
        };

        var refreshNode = function(node) {
            var realCurEditingNode = editorsHelper.curEditingNode;
            editorsHelper.curEditingNode = node;
            editorsHelper.curEditor = editorsHelper.editors[node.type];
            editorsHelper.curEditor.fillEditor(node);
            editorsHelper.saveCurrentEditor();
            editorsHelper.curEditingNode = realCurEditingNode;
        };

        var getDirectionByUsedAnchors = function(usedAnchors, isOutgoingArrow) {
            var verticalDirection = campaignManager.getCMNodes().TOP;
            var horizontalDirection = campaignManager.getCMNodes().LEFT;
            if (isOutgoingArrow) {
                verticalDirection = campaignManager.getCMNodes().BOTTOM;
                horizontalDirection = campaignManager.getCMNodes().RIGHT;
            }

            if (usedAnchors.indexOf(horizontalDirection) == -1) {
                return horizontalDirection;
            } else if (usedAnchors.indexOf(verticalDirection) == -1) {
                return verticalDirection;
            } else {
                return horizontalDirection;
            }
        };

        var getDirectionByPosition = function(node, secondNode) {
            var x = secondNode.x - node.x;
            var y = secondNode.y - node.y;

            if (
                (x > 0 && y >= 0 && x >= y)
                ||
                (x > 0 && y < 0 && x >= y)
            ) {
                return campaignManager.getCMNodes().RIGHT;
            }

            if (
                (x <= 0 && y <= 0 && x <= y)
                ||
                (x < 0 && y > 0 && x <= y)
            ) {
                return campaignManager.getCMNodes().LEFT;
            }

            if (
                (x >= 0 && y > 0 && x < y)
                ||
                (x < 0 && y > 0 && x < y)
            ) {
                return campaignManager.getCMNodes().BOTTOM;
            }

            if (
                (x >= 0 && y < 0 && x > y)
                ||
                (x < 0 && y < 0 && x > y)
            ) {
                return campaignManager.getCMNodes().TOP;
            }
        };

        var initTransferDialog = function(self) {
            jQuery(self.containerId + "-transfer-dialog").dialog({
                title: '<span class="dialog-fat-title">' + t('workflow.defaults.title') + '</span>',
                dialogClass: "no-close",
                open: function(event, ui) {
                    openTransferDialogHandler(self);
                },
                width: 350,
                modal: true,
                resizable: false
            });
        };

        this.initOneMailinglistWarningDialog = function(self) {
            jQuery(self.containerId + "-oneMailinglistWarning-dialog").dialog({
                title: '<span class="dialog-fat-title">' + self.getSelectedMailingOption().html() + '</span>',
                dialogClass: "no-close",
                width: 350,
                modal: true,
                resizable: false
            });
        };

        var openTransferDialogHandler = function(self) {
            var mailingEditorBase = self;
            jQuery(self.containerId + "-transfer-dialog input[name^=transfer]").each(function() {
                var element = jQuery(this);
                element.parent().parent().hide();
            });

            var paramsToBeAsked = getMailingParams(self, true);

            var isAllSettings = false;
            for(var i = 0; i < paramsToBeAsked.length; i++) {
                switch (paramsToBeAsked[i]) {
                    case self.PARAM_MAILING_LIST:
                        jQuery(self.containerId + "-transferMailingList").parent().parent().show();
                        isAllSettings = true;
                        break;
                    case self.PARAM_TARGET_GROUPS:
                        jQuery(self.containerId + "-transferTargetGroups").parent().parent().show();
                        isAllSettings = true;
                        break;
                    case self.PARAM_ARCHIVE:
                        jQuery(self.containerId + "-transferArchive").parent().parent().show();
                        isAllSettings = true;
                        break;
                    case self.PARAM_LIST_SPLIT:
                        jQuery(self.containerId + "-transferListSplit").parent().parent().show();
                        isAllSettings = true;
                        break;
                    case self.PARAM_PLANNED_DATE:
                        jQuery(self.containerId + "-transferPlanedFor").parent().parent().show();
                        isAllSettings = true;
                        break;
                    case self.PARAM_SEND_DATE:
                        jQuery(self.containerId + "-transferDeliveryTime").parent().parent().show();
                        isAllSettings = true;
                        break;
                }
            }

            if (isAllSettings == true) {
                jQuery(self.containerId + "-transferAllSettings").parent().parent().show();
            }

            jQuery(self.containerId + "-transferAllSettings").off().on("click", function() {
                var isChecked = jQuery(this).prop('checked');
                jQuery(self.containerId + "-transferArchive").prop('checked', isChecked);
                jQuery(self.containerId + "-transferMailingList").prop('checked', isChecked);
                jQuery(self.containerId + "-transferPlanedFor").prop('checked', isChecked);
                jQuery(self.containerId + "-transferDeliveryTime").prop('checked', isChecked);
                jQuery(self.containerId + "-transferListSplit").prop('checked', isChecked);
                jQuery(self.containerId + "-transferTargetGroups").prop('checked', isChecked);
            });
        };

        var getSupplementedNodeChain = function(nodesChain, params) {
            var mailingEditorBase = self;
            var existingPreviousNodes = [];
            var previousNodes = [];

            // add nodes which already exists
            for(var i = 1; i < nodesChain.length; i++) { // first node is mailing so we don't need to re-add it
                var type = nodesChain[i].type;

                existingPreviousNodes.push(type);

                switch (type) {
                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER:
                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE:
                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT:
                    case AGN.Lib.WM.NodeFactory.NODE_TYPE_START:
                        // Do nothing
                        break;

                    default:
                        previousNodes.push(type);
                        break;
                }
            }

            if (contains(existingPreviousNodes, AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER) ||
                contains(params, mailingEditorBase.PARAM_LIST_SPLIT)) {
                previousNodes.push(AGN.Lib.WM.NodeFactory.NODE_TYPE_PARAMETER);
            }

            if (contains(existingPreviousNodes, AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE) ||
                contains(params, mailingEditorBase.PARAM_ARCHIVE)) {
                previousNodes.push(AGN.Lib.WM.NodeFactory.NODE_TYPE_ARCHIVE);
            }

            if (contains(existingPreviousNodes, AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT) ||
                contains(params, mailingEditorBase.PARAM_MAILING_LIST) ||
                contains(params, mailingEditorBase.PARAM_TARGET_GROUPS)) {
                previousNodes.push(AGN.Lib.WM.NodeFactory.NODE_TYPE_RECIPIENT);
            }

            if (contains(existingPreviousNodes, AGN.Lib.WM.NodeFactory.NODE_TYPE_START) ||
                contains(params, mailingEditorBase.PARAM_PLANNED_DATE) ||
                contains(params, mailingEditorBase.PARAM_SEND_DATE)) {
                previousNodes.push(AGN.Lib.WM.NodeFactory.NODE_TYPE_START);
            }

            return previousNodes;
        };

        var containsMailingSendDate = function(mailingData) {
            return mailingData.sendDate != undefined && mailingData.sendDate != null && mailingData.sendHour != null && mailingData.sendMinute != null;
        };

        var containsMailingPlannedDate = function(mailingData) {
            return mailingData.planDate > 0;
        };

        var extractMailingSendDate = function(mailingData) {
            if (containsMailingSendDate(mailingData)) {
                return new Date(mailingData.sendDate + 'T' + mailingData.sendHour + ':' + mailingData.sendMinute + ':00');
            }
            return null;
        };

        var extractMailingPlannedDate = function(mailingData) {
            if (containsMailingPlannedDate(mailingData)) {
                return new Date(mailingData.planDate);
            }
            return null;
        };

        var excludeTime = function(date) {
            if (date == null) {
                return null;
            } else {
                // We should avoid influence of hours and minutes at the comparison
                return new Date(date.getFullYear(), date.getMonth(), date.getDate());
            }
        };

        // TODO: Should be improved in order to support different target options (and, or, not in)
        var equalTargetGroups = function(configuredMailingData, mailingData) {
            var configuredTargets = configuredMailingData.targets;
            var mailingTargets = mailingData.targetGroupIds;

            if (configuredTargets == null && mailingTargets == null) {
                return true;
            }

            if (configuredTargets.length != mailingTargets.length) {
                return false;
            }

            for(var i = 0; i < mailingTargets.length; i++) {
                if (!contains(configuredTargets, mailingTargets[i])) {
                    return false;
                }
            }
            return true;
        };

        var contains = function(array, item) {
            return array.indexOf(item) != -1;
        };
    };

    AGN.Lib.WM.MailingEditorBase = MailingEditorBase;

})(jQuery);
