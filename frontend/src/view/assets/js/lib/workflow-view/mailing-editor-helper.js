(function() {
    var Def = AGN.Lib.WM.Definitions,
        Node = AGN.Lib.WM.Node,
        EditorsHelper = AGN.Lib.WM.EditorsHelper,
        Dialogs = AGN.Lib.WM.Dialogs;

    var MailingEditorHelper = function(data, submitWorkflowForm) {
        this.MAILING_TYPE_ACTIONBASED = 1;
        this.MAILING_TYPE_DATEBASED = 2;
        this.MAILING_TYPE_FOLLOWUP = 3;

        this.node = {};
        this.mailingsStatus = 'all';
        this.defaultMailingsSort = data.defaultMailingsSort;
        this.defaultMailingsOrder = data.defaultMailingsOrder;
        this.mailingsSort = this.defaultMailingsSort;
        this.mailingsOrder = this.defaultMailingsOrder;
        this.formNameJId = 'form[name="' + data.form + '"]';
        this.containerId = data.container;
        this.mailingType = data.mailingType;
        this.mailingTypesForLoading = data.mailingTypesForLoading;
        this.statusName = data.mailingStatus;
        this.selectName = data.selectName;
        this.selectNameJId = 'select[name=' + data.selectName + ']';
        this.mailingId = 0;
        this.showCreateEditLinks = data.showCreateEditLinks;
        this.$dropdown = null;
        if (data.$dropdown) {
            this.$dropdown = data.$dropdown;
        }
        //follow up base mailing settings for aadvertising content type
        this.isChangeDecisionOptions = data.isFollowUpBaseMailing;
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
            $(this.containerId + ' #' + this.statusName + '_all').attr('checked', 'checked');
            this.activateInputs();
            this.setSelectMailingOptions(this.node.data[this.selectName]);
            this.getMailingsByWorkStatus(this.mailingsStatus, this.mailingsSort, this.mailingsOrder, this.node.data.mailingId);
        };

        this.setDefaultSortOptions = function() {
            this.mailingsSort = this.defaultMailingsSort;
            this.mailingsOrder = this.defaultMailingsOrder;
            $(this.containerId + ' .arrowUp').hide();
            $(this.containerId + ' .arrowDown').hide();
            $(this.containerId + ' #shortname_sort').attr('press', 1);
            $(this.containerId + ' #change_date_sort').attr('press', 2);
            if ((this.mailingType == this.MAILING_TYPE_ACTIONBASED) || (this.mailingType == this.MAILING_TYPE_DATEBASED)) {
                $(this.containerId + ' #shortname_sort .arrowUp').show();
            } else {
                $(this.containerId + ' #change_date_sort .arrowDown').show();
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
            if (isChangeDecisionOptions) {
                var selectedMailingId = this.mailingId;
                var followUpSelect = $(scope.find(this.followUpContainer));
                var additionalOptions = this.advertisingAdditionalOptions;
                var advertisingUrl = this.advertisingUrl;
                $.ajax({
                    action: 'POST',
                    url: advertisingUrl,
                    data: {
                        mailingId: selectedMailingId
                    },
                    success: function(result) {
                        $(followUpSelect.find('.advertisingOption')).remove();
                        if (result.isAdvertisingContentType) {
                            var options = AGN.Lib.Template.text('followupAdvertisingOptions', {items: additionalOptions});
                            followUpSelect.append(options);
                        }
                    }
                });
            }
        };

        this.onMailingSortClick = function(id, val) {
            $(this.containerId + ' .arrowUp').hide();
            $(this.containerId + ' .arrowDown').hide();
            var sortElements = $(this.containerId + ' .sort');
            var sortId = this.containerId + ' #' + id + '_sort';
            var sortEl = $(sortId);
            var upSortEl = $(sortId + ' .arrowUp');
            var downSortEl = $(sortId + ' .arrowDown');
            var press = sortEl.attr('press');
            var selectedMailValue = $(this.formNameJId + ' ' + this.selectNameJId).val();
            if (!press) {
                sortElements.attr('press', 2);
                sortEl.attr('press', 2);
                upSortEl.show();
                this.mailingsOrder = 'ASC';
            } else {
                switch (press) {
                    case '1' :
                        sortEl.attr('press', 2);
                        downSortEl.show();
                        this.mailingsOrder = 'DESC';
                        break;
                    case '2' :
                        sortEl.attr('press', 1);
                        upSortEl.show();
                        this.mailingsOrder = 'ASC';
                        break;
                }
            }
            this.getMailingsByWorkStatus(this.mailingsStatus, val, this.mailingsOrder, selectedMailValue);
        };

        this.setMailingId = function(mailingId) {
            this.mailingId = mailingId;

            if (this.$dropdown) {
                if (this.mailingId > 0) {
                    this.$dropdown.show();
                } else {
                    this.$dropdown.hide();
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
            if (mailingId != '0') {
                mailLink = '<span style="font-weight: bold;">' + t('workflow.mailing.tip') + ': &nbsp;</span>' +
                    '<a href="#" data-action="mailing-editor-edit">' + t('workflow.mailing.edit_mailing_link') + '</a> ';
            } else {
                mailLink = '' +
                    '<a href="#" class="setting-mailing-left" data-action="mailing-editor-new"> ' +
                    t('workflow.mailing.new') +
                    '</a>';
            }

            var mailingLinkContainer = $(this.containerId + ' #mailing_create_edit_link');
            mailingLinkContainer.html('');
            mailingLinkContainer.append(mailLink);
        };

        this.showSecurityQuestion = function() {
            this.setSecurityQuestion();
            this.setIsUsedInCMAttr();

            if (this.isSentMailing(this.mailingIsSent) || this.mailingIsUsedInCM) {
                var selectedOption = this.getSelectedMailingOption();

                $(this.containerId + '-security-dialog').dialog({
                    title: '<span class="dialog-fat-title">' + selectedOption.html() + '</span>',
                    dialogClass: 'no-close',
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
            return status == 'mailing.status.sent' || status == 'mailing.status.scheduled' || status == 'mailing.status.norecipients';
        };

        this.cancelSecurityDialog = function() {
            $(this.containerId + '-security-dialog').dialog('close');
        };

        this.getMailingsByWorkStatus = function(status, sort, order, selectedMailingValue) {
            var mailingEditorBase = this;
            var $mailings = $(this.formNameJId + ' ' + this.selectNameJId);

            $mailings.attr('readonly', 'readonly');

            $.ajax({
                type: 'POST',
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

                    EditorsHelper.initSelectWithValueOrChooseFirst($mailings, mailingEditorBase.mailingId);
                }
            });
        };

        this.disableInputs = function() {
            $(this.containerId + ' [name="' + this.statusName + '"]').attr('disabled', 'disabled');
        };

        this.activateInputs = function() {
            $(this.containerId + ' [name="' + this.statusName + '"]').removeAttr('disabled');
        };

        this.setNodeFields = function() {
            this.node.setFilled(parseInt(this.mailingId, 10) > 0);
        };

        this.setSecurityQuestion = function() {
            var selectedMailingOption = this.getSelectedMailingOption();
            this.mailingIsSent = '';
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
                var workflowId = Def.workflowId || 0;
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
            return $(this.formNameJId + ' ' + this.selectNameJId + ' option[value=' + this.mailingId + ']');
        };

        this.getSelectedDecisionOption = function() {
            var selector = 'decisionCriterion';
            var val = $(this.formNameJId + ' select[name="' + selector + '"]').val();
            return $(this.formNameJId + ' select[name="' + selector + '"] option[value=' + val + ']');
        };

        this.createNewMailing = function(forwardName) {
            hasMailingIconMailingList({
                node: this.node,
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
                $(self.containerId + ' .editor-error-messages').html(t('error.workflow.notAddedMailingList'));
                $(self.containerId + ' .editor-error-messages').css('display', 'block');
            } else {
                var additionalParams = [];
                additionalParams.push('mailingType=' + self.mailingType);
                if (self.mailingType == self.MAILING_TYPE_FOLLOWUP) {
                    additionalParams.push('workflowFollowUpParentMailing=' + self.node.data.baseMailingId);
                    additionalParams.push('workflowFollowUpDecisionCriterion=' + self.node.data.decisionCriterion);
                }
                EditorsHelper.processForward(forwardName, self.formNameJId + ' ' + self.selectNameJId, submitWorkflowForm, additionalParams.join(';'));
            }
        };

        this.processCopyAndEditMailingForward = function(mailingId, forwardName, formNameJId, selectNameJId) {
            $('#forwardTargetItemId').val(mailingId);
            var additionalParams = [];
            if (this.mailingType == this.MAILING_TYPE_FOLLOWUP) {
                additionalParams.push('workflowFollowUpParentMailing=' + this.node.data.baseMailingId);
                additionalParams.push('workflowFollowUpDecisionCriterion=' + this.node.data.decisionCriterion);
            }
            if (additionalParams.length > 0) {
                EditorsHelper.processForward(forwardName, formNameJId + ' ' + selectNameJId, submitWorkflowForm, additionalParams.join(';'));
            } else {
                EditorsHelper.processForward(forwardName, formNameJId + ' ' + selectNameJId, submitWorkflowForm);
            }
        };

        this.editMailing = function(forwardName) {
            this.processCopyAndEditMailingForward(this.mailingId, forwardName, this.formNameJId, this.selectNameJId);
        };

        this.copyMailing = function(forwardName) {
            this.processCopyAndEditMailingForward(this.mailingId, forwardName, this.formNameJId, this.selectNameJId);
        };

        this.trySupplementChain = function(chain, mailingContent, successCallback) {
            if (chain.length > 0) {
                this.configuredMailingData = collectIncomingMailingData(chain);
                this.nodesChain = chain;
                this.mailingData = mailingContent;

                var paramsToAsk = getMailingParams(self, true);

                if (paramsToAsk.length) {
                    Dialogs.confirmMailingDataTransfer(paramsToAsk)
                        .done(function(checkedParams) {
                            self.transferMailingData(checkedParams);
                            self.supplementChain(successCallback);
                        })
                        .fail(function() {
                            self.transferMailingData();
                            self.supplementChain(successCallback);
                        });
                } else {
                    self.supplementChain(successCallback);
                }
            }
        };

        /**
         * Load mailing content for mailing with id = {@code mailingId} and check its mailinglistId.
         * If we have several mailings and theirs mailinglists are different executes {@code failedCallback},
         * otherwise executes {@code successCallback}
         */
        this.checkDifferentMailingLists = function(mailingId, successCallback, failedCallback) {
            var mailingEditorBase = this;
            getMailingContent(mailingId)
              .then(function(mailingContent) {
                  var chain = EditorsHelper.getFirstIncomingChain();
                  mailingEditorBase.configuredMailingData = collectIncomingMailingData(chain);
                  mailingEditorBase.nodesChain = chain;
                  mailingEditorBase.mailingData = mailingContent;

                  // How much mailings contains in the chain.
                  var mailingIconsCount = 0;
                  chain.forEach(function(node) {
                      if (node.getType() == Def.NODE_TYPE_MAILING) {
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

        var collectIncomingMailingData = function(chain) {
            var data = {
                parameterValue: 0,
                campaignId: 0,
                mailinglistId: 0,
                targets: [],
                date: null
            };

            for (var nodeIndex = chain.length - 1; nodeIndex >= 0; nodeIndex--) {
                var node = chain[nodeIndex];
                var nodeData = node.getData();

                switch (node.getType()) {
                    case Def.NODE_TYPE_PARAMETER:
                        data.parameterValue = nodeData.value;
                        break;

                    case Def.NODE_TYPE_ARCHIVE:
                        data.campaignId = nodeData.campaignId;
                        break;

                    case Def.NODE_TYPE_RECIPIENT:
                        if (data.mailinglistId == 0) {
                            data.mailinglistId = nodeData.mailinglistId;
                        }

                        // //add only unique target group id
                        if (nodeData.targets !== null) {
                            nodeData.targets.forEach(function(target) {
                                if (!data.targets.includes(target)) {
                                    data.targets.push(parseInt(target));
                                }
                            });
                        }
                        break;


                    case Def.NODE_TYPE_START:
                        if (nodeData.date) {
                            data.date = excludeTime(nodeData.date);
                            data.date.setHours(nodeData.hour);
                            data.date.setMinutes(nodeData.minute);
                        } else {
                            data.date = null;
                        }
                        break;

                    case Def.NODE_TYPE_DEADLINE:
                        if (nodeData.deadlineType == Def.constants.deadlineTypeFixedDeadline) {
                            if (nodeData.date) {
                                data.date = excludeTime(nodeData.date);
                                data.date.setHours(nodeData.hour);
                                data.date.setMinutes(nodeData.minute);
                            } else {
                                data.date = null;
                            }
                        } else if (nodeData.deadlineType == Def.constants.deadlineTypeDelay) {
                            // add delay
                            if (data.date != null) {
                                if (nodeData.timeUnit == Def.constants.deadlineTimeUnitMinute) {
                                    data.date.setMinutes(data.date.getMinutes() + parseInt(nodeData.delayValue));
                                } else if (nodeData.timeUnit == Def.constants.deadlineTimeUnitHour) {
                                    data.date.setHours(data.date.getHours() + parseInt(nodeData.delayValue));
                                } else if (nodeData.timeUnit == Def.constants.deadlineTimeUnitDay) {
                                    data.date.setDate(data.date.getDate() + parseInt(nodeData.delayValue));
                                    if (nodeData.useTime || nodeData.useTime == 'true') {
                                        data.date.setMinutes(data.date.getMinutes() + parseInt(nodeData.minute));
                                        data.date.setHours(data.date.getHours() + parseInt(nodeData.hour));
                                    }
                                }
                            }
                        }
                        break;
                }
            }

            return data;
        };

        var getMailingParams = function(mailingEditorBase, askAmbiguous) {
            var configuredMailingData = mailingEditorBase.configuredMailingData;
            var mailingData = mailingEditorBase.mailingData;

            var paramsToBeTransferred = [];
            var paramsToBeAsked = [];
            var paramsToBeOverridden = [];

            if (mailingData.mailinglistId == 0) {
                paramsToBeOverridden.push(Def.MAILING_PARAM_MAILING_LIST);
            } else {
                if (configuredMailingData.mailinglistId == 0) {
                    // Transfer
                    paramsToBeTransferred.push(Def.MAILING_PARAM_MAILING_LIST);
                } else {
                    if (configuredMailingData.mailinglistId != mailingData.mailinglistId) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(Def.MAILING_PARAM_MAILING_LIST);
                        } else {
                            paramsToBeTransferred.push(Def.MAILING_PARAM_MAILING_LIST);
                        }
                    }
                }
            }

            var configuredTargets = configuredMailingData.targets;
            var mailingTargets = mailingData.targetGroupIds;

            if (mailingTargets == null || mailingTargets.length == 0) {
                paramsToBeOverridden.push(Def.MAILING_PARAM_TARGET_GROUPS);
            } else {
                if (configuredTargets == null || configuredTargets.length == 0) {
                    // Transfer
                    paramsToBeTransferred.push(Def.MAILING_PARAM_TARGET_GROUPS);
                } else {
                    if (!equalTargetGroups(configuredMailingData, mailingData)) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(Def.MAILING_PARAM_TARGET_GROUPS);
                        } else {
                            paramsToBeTransferred.push(Def.MAILING_PARAM_TARGET_GROUPS);
                        }
                    }
                }
            }
            
            if (Def.constants.isAltgExtended) {
              var configuredAltgs = configuredMailingData.altgs;
              var mailingAltgs = mailingData.altgIds;

              if (mailingAltgs == null || mailingAltgs.length == 0) {
                paramsToBeOverridden.push(Def.MAILING_PARAM_ALTGS);
              } else {
                if (configuredAltgs == null || configuredAltgs.length == 0) {
                  // Transfer
                  paramsToBeTransferred.push(Def.MAILING_PARAM_ALTGS);
                } else {
                  if (!equalAltgs(configuredMailingData, mailingData)) {
                    if (askAmbiguous) {
                      // Dialog
                      paramsToBeAsked.push(Def.MAILING_PARAM_TARGET_GROUPS);
                    } else {
                      paramsToBeTransferred.push(Def.MAILING_PARAM_TARGET_GROUPS);
                    }
                  }
                }
              }
            }

            var parameterValue = mailingEditorBase.getParameterValue(mailingData.splitBase, mailingData.splitPart);

            if (parameterValue == null) {
                paramsToBeOverridden.push(Def.MAILING_PARAM_LIST_SPLIT);
            } else {
                if (configuredMailingData.parameterValue == 0) {
                    // Transfer
                    paramsToBeTransferred.push(Def.MAILING_PARAM_LIST_SPLIT);
                } else {
                    if (configuredMailingData.parameterValue != parameterValue) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(Def.MAILING_PARAM_LIST_SPLIT);
                        } else {
                            paramsToBeTransferred.push(Def.MAILING_PARAM_LIST_SPLIT);
                        }
                    }
                }
            }

            if (mailingData.campaignId == 0) {
                paramsToBeOverridden.push(Def.MAILING_PARAM_ARCHIVE);
            } else {
                if (configuredMailingData.campaignId == 0) {
                    // Transfer
                    paramsToBeTransferred.push(Def.MAILING_PARAM_ARCHIVE);
                } else {
                    if (configuredMailingData.campaignId != mailingData.campaignId) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(Def.MAILING_PARAM_ARCHIVE);
                        } else {
                            paramsToBeTransferred.push(Def.MAILING_PARAM_ARCHIVE);
                        }
                    }
                }
            }

            var configuredDate = configuredMailingData.date;
            var mailingPlannedDate = extractMailingPlannedDate(mailingData);
            var mailingSendDate = extractMailingSendDate(mailingData);

            if (mailingPlannedDate == null && mailingSendDate == null) {
                paramsToBeOverridden.push(Def.MAILING_PARAM_PLANNED_DATE);
            } else {
                if (configuredDate == null) {
                    // Transfer
                    if (mailingPlannedDate != null) {
                        paramsToBeTransferred.push(Def.MAILING_PARAM_PLANNED_DATE);
                    } else {
                        paramsToBeTransferred.push(Def.MAILING_PARAM_SEND_DATE);
                    }
                } else {
                    if (excludeTime(mailingPlannedDate) > excludeTime(configuredDate)) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(Def.MAILING_PARAM_PLANNED_DATE);
                        } else {
                            paramsToBeTransferred.push(Def.MAILING_PARAM_PLANNED_DATE);
                        }
                    } else if (mailingPlannedDate && excludeTime(mailingPlannedDate) < excludeTime(configuredDate)) {
                        paramsToBeOverridden.push(Def.MAILING_PARAM_PLANNED_DATE);
                    }

                    if (mailingSendDate > configuredDate) {
                        if (askAmbiguous) {
                            // Dialog
                            paramsToBeAsked.push(Def.MAILING_PARAM_SEND_DATE);
                        } else {
                            paramsToBeTransferred.push(Def.MAILING_PARAM_SEND_DATE);
                        }
                    } else if (mailingSendDate && mailingSendDate < configuredDate) {
                        paramsToBeOverridden.push(Def.MAILING_PARAM_SEND_DATE);
                    }
                }
            }

            overrideMailingData(mailingEditorBase, paramsToBeOverridden);

            return askAmbiguous ? paramsToBeAsked : paramsToBeTransferred;
        };

        var overrideMailingData = function(mailingEditorBase, paramsToBeOverridden) {
            var configuredMailingData = mailingEditorBase.configuredMailingData;
            var mailingData = mailingEditorBase.mailingData;

            for (var i = 0; i < paramsToBeOverridden.length; i++) {
                switch (paramsToBeOverridden[i]) {
                    case Def.MAILING_PARAM_MAILING_LIST:
                        mailingData.mailinglistId = configuredMailingData.mailinglistId;
                        break;

                    case Def.MAILING_PARAM_TARGET_GROUPS:
                        mailingData.targetGroupIds = configuredMailingData.targets;
                        break;
                    case Def.MAILING_PARAM_ALTGS:
                        mailingData.altgIds = configuredMailingData.altgs;
                        break;

                    case Def.MAILING_PARAM_ARCHIVE:
                        mailingData.campaignId = configuredMailingData.campaignId;
                        break;

                    case Def.MAILING_PARAM_LIST_SPLIT:
                        // TODO: find out how can I override parameter
                        mailingData.splitBase = '';
                        mailingData.splitPart = '';
                        break;

                    case Def.MAILING_PARAM_PLANNED_DATE:
                        var campaignDate = excludeTime(configuredMailingData.date);
                        if (campaignDate) {
                            mailingData.planDate = campaignDate.getTime();
                        } else {
                            mailingData.planDate = 0;
                        }
                        break;

                    case Def.MAILING_PARAM_SEND_DATE:
                        mailingData.sendDate = excludeTime(configuredMailingData.date);
                        if (configuredMailingData.date) {
                            mailingData.sendHour = configuredMailingData.date.getHours();
                            mailingData.sendMinute = configuredMailingData.date.getMinutes();
                        }
                        break;
                }
            }
        };

        var getMailingContent = function(mailingId) {
            return new Promise(function (resolve) {
                _.defer(function() {
                    $.ajax({
                        type: 'POST',
                        url: AGN.url('/workflow/getMailingContent.action'),
                        data: {
                            mailingId: mailingId
                        },
                        success: function(data) {
                            resolve(data);
                        }
                    });
                });
            });
        };

        // Method get true if recipient icon has mailing list for mailing with iconId
        var hasMailingIconMailingList = function(options) {
            var node = options.node || EditorsHelper.curEditingNode;
            var successCallback = options.successCallback;
            var forwardName = options.forwardName;

            function checkHasMailinglist(node) {
                var mailinglistId = 0;
                EditorsHelper.curEditingNode = node;
                EditorsHelper.forEachPreviousNode(function(prevNode) {
                    if (prevNode.type === Def.NODE_TYPE_RECIPIENT) {
                        mailinglistId = prevNode.data.mailinglistId;
                        return false;
                    }
                })
                return mailinglistId > 0;
            }

            successCallback({hasMailingList: checkHasMailinglist(node)}, forwardName, options.mailingEditorBase);
        };

        this.transferMailingData = function(checkedParams) {
            if (checkedParams) {
                overrideMailingData(self, checkedParams);
            } else {
                overrideMailingData(self, getMailingParams(self, true));
            }
        };

        this.supplementChain = function(successCallback) {
            var paramsToTransfer = getMailingParams(this, false);
            var mailingData = this.mailingData;
            var configuredMailingData = this.configuredMailingData;
            var chain = this.nodesChain;

            // Try to find another mailing in incoming chain.
            var isStartNodeSupplemented = false;
            var isBetweenMailings = false;

            for (var i = 1; i < chain.length; i++) {
                if (Def.NODE_TYPES_MAILING.includes(chain[i].getType())) {
                    chain.splice(i + 1);
                    isBetweenMailings = true;
                    break;
                }
            }

            // Start (if allowed).
            if (!isBetweenMailings) {
                if (paramsToTransfer.includes(Def.MAILING_PARAM_PLANNED_DATE) || paramsToTransfer.includes(Def.MAILING_PARAM_SEND_DATE)) {
                    if (Def.NODE_TYPE_START == chain[chain.length - 1].getType()) {
                        isStartNodeSupplemented = true;
                    } else {
                        var allStartNodes = EditorsHelper.getNodesByType(Def.NODE_TYPE_START);
                        if (allStartNodes.length == 0 && mailingData.mailingType != this.MAILING_TYPE_ACTIONBASED && mailingData.mailingType != this.MAILING_TYPE_DATEBASED) {
                            isStartNodeSupplemented = true;
                        }
                    }

                    if (isStartNodeSupplemented) {
                        this.supplementSingleNode(Def.NODE_TYPE_START, function(node) {
                            var data = node.getData();

                            if (paramsToTransfer.includes(Def.MAILING_PARAM_PLANNED_DATE)) {
                                data.date = extractMailingPlannedDate(mailingData);
                                var configuredDate = configuredMailingData.date;
                                if (configuredDate) {
                                    data.hour = configuredDate.getHours();
                                    data.minute = configuredDate.getMinutes();
                                }
                                EditorsHelper.resave(node);
                            } else if (paramsToTransfer.includes(Def.MAILING_PARAM_SEND_DATE)) {
                                data.date = extractMailingSendDate(mailingData);
                                data.hour = data.date.getHours();
                                data.minute = data.date.getMinutes();
                            }
                        });
                    }
                }
            }

            // Parameter
            if (paramsToTransfer.includes(Def.MAILING_PARAM_LIST_SPLIT)) {
                this.supplementSingleNode(Def.NODE_TYPE_PARAMETER, function(node) {
                    var data = node.getData();
                    data.value = self.getParameterValue(mailingData.splitBase, mailingData.splitPart);
                });
            }

            // Archive
            if (paramsToTransfer.includes(Def.MAILING_PARAM_ARCHIVE)) {
                this.supplementSingleNode(Def.NODE_TYPE_ARCHIVE, function(node) {
                    var data = node.getData();
                    data.campaignId = mailingData.campaignId;
                });
            }

            // Recipient
            if (paramsToTransfer.includes(Def.MAILING_PARAM_MAILING_LIST) || paramsToTransfer.includes(Def.MAILING_PARAM_TARGET_GROUPS)) {
                this.supplementSingleNode(Def.NODE_TYPE_RECIPIENT, function(node) {
                    var data = node.getData();

                    if (paramsToTransfer.includes(Def.MAILING_PARAM_MAILING_LIST)) {
                        data.mailinglistId = mailingData.mailinglistId;
                        node.setDependent(true);
                    }

                    if (paramsToTransfer.includes(Def.MAILING_PARAM_TARGET_GROUPS)) {
                        data.targets = mailingData.targetGroupIds;
                        if (Def.constants.isAltgExtended) {
                          data.targetsOption = 'ONE_TARGET_REQUIRED';
                        } else {
                          data.targetsOption = Def.constants.accessLimitTargetId > 0 ? 'ALL_TARGETS_REQUIRED' : 'ONE_TARGET_REQUIRED';
                        }
                    }
                    if (Def.constants.isAltgExtended) {
                      if (paramsToTransfer.includes(Def.MAILING_PARAM_ALTGS)) {
                        data.altgs = mailingData.altgIds;
                      }
                    }
                });
            }

            // Deadline
            if (!isStartNodeSupplemented && (paramsToTransfer.includes(Def.MAILING_PARAM_PLANNED_DATE) || paramsToTransfer.includes(Def.MAILING_PARAM_SEND_DATE))) {
                this.supplementSingleNode(Def.NODE_TYPE_DEADLINE, function(node) {
                    var data = node.getData();

                    if (paramsToTransfer.includes(Def.MAILING_PARAM_PLANNED_DATE)) {
                        data.date = extractMailingPlannedDate(mailingData);
                        var configuredDate = configuredMailingData.date;
                        if (configuredDate) {
                            data.date.setHours(configuredDate.getHours());
                            data.date.setMinutes(configuredDate.getMinutes());
                        }
                    } else if (paramsToTransfer.includes(Def.MAILING_PARAM_SEND_DATE)) {
                        data.date = extractMailingSendDate(mailingData);
                    }

                    data.deadlineType = Def.constants.deadlineTypeFixedDeadline;
                    data.hour = data.date.getHours();
                    data.minute = data.date.getMinutes();
                });
            }

            successCallback(chain);
        };

        this.supplementSingleNode = function(nodeType, callback) {
            var availableTypesAfter;

            switch (nodeType) {
                case Def.NODE_TYPE_START:
                    // Must be the very first node in a chain.
                    availableTypesAfter = false;
                    break;

                case Def.NODE_TYPE_PARAMETER:
                    availableTypesAfter = [];
                    break;

                case Def.NODE_TYPE_ARCHIVE:
                    availableTypesAfter = [Def.NODE_TYPE_PARAMETER];
                    break;

                case Def.NODE_TYPE_RECIPIENT:
                    availableTypesAfter = [Def.NODE_TYPE_PARAMETER, Def.NODE_TYPE_ARCHIVE];
                    break;

                case Def.NODE_TYPE_DEADLINE:
                    availableTypesAfter = [Def.NODE_TYPE_PARAMETER, Def.NODE_TYPE_ARCHIVE, Def.NODE_TYPE_RECIPIENT];
                    break;

                default:
                    console.error('Unexpected supplement node type: ' + nodeType);
                    return;
            }

            if (!supplementExistingNode(nodeType, callback)) {
                supplementAddNode(nodeType, availableTypesAfter, callback);
            }
        };

        var supplementExistingNode = function(nodeType, callback) {
            var chain = self.nodesChain;

            for (var i = 1; i < chain.length; i++) {
                var node = chain[i];

                if (nodeType == node.getType()) {
                    callback(node);
                    EditorsHelper.resave(node);
                    return true;
                }
            }

            return false;
        };

        var supplementAddNode = function(nodeType, availableTypesAfter, callback) {
            var chain = self.nodesChain;
            var newNode = Node.create(nodeType);

            callback(newNode);

            if (availableTypesAfter === false) {
                chain.push(newNode);
            } else {
                for (var index = 1; index < chain.length; index++) {
                    var previousNode = chain[index];

                    if (!availableTypesAfter.includes(previousNode.getType())) {
                        chain.splice(index, 0, newNode);
                        return;
                    }
                }

                chain.push(newNode);
            }
        };

        this.closeOneMailinglistWarningDialog = function() {
            $(this.containerId + '-oneMailinglistWarning-dialog').dialog('close');
        };

        this.acceptOneMailinglistPerCampaign = function() {
            this.closeOneMailinglistWarningDialog();
            EditorsHelper.mailingSpecificSaveAfterMailinglistCheckModal(self);
        };

        this.validateEditor = function(save) {
            var $errors = $(this.containerId + ' .editor-error-messages');

            $errors.html('');
            $errors.css('display', 'none');

            var errorsFound = false;
            // validate delivery settings
            if ($(this.containerId + ' .delivery-settings').length) {
                var maxRecipients = $(this.containerId + ' [name="maxRecipients"]').val();
                if (parseInt(maxRecipients) != maxRecipients) {
                    $errors.html(t('error.workflow.wrongMaxRecipientsFormat'));
                    errorsFound = true;
                } else if (parseInt(maxRecipients) < 0) {
                    $errors.html(t('error.workflow.maxRecipientsLessThanZero'));
                    errorsFound = true;
                } else if (parseInt(maxRecipients) > 1000000000) {
                    $errors.html(t('error.workflow.maxRecipientsTooBig'));
                    errorsFound = true;
                }
            }
            // validate that mailing is selected
            var mailingSelector = $(this.formNameJId + ' ' + this.selectNameJId);
            if (mailingSelector.val() <= 0) {
                if ($errors.html() != '') {
                    $errors.html($errors.html() + '<br>');
                }
                $errors.html($errors.html() + t('error.workflow.noMailing'));
                errorsFound = true;
            }

            if (!errorsFound) {
                if (save) {
                    save();
                } else {
                    EditorsHelper.saveCurrentEditor();
                }
            } else {
                $errors.css('display', 'block');
            }
        };

        this.toggleSendSettings = function(editorId) {
            if ($('#sendSettings_' + editorId).css('display') == 'none') {
                $('#sendSettingsToggle_' + editorId).removeClass('toggle_closed');
                $('#sendSettingsToggle_' + editorId).addClass('toggle_open');
            } else {
                $('#sendSettingsToggle_' + editorId).removeClass('toggle_open');
                $('#sendSettingsToggle_' + editorId).addClass('toggle_closed');
            }
            $('#sendSettings_' + editorId).toggle('blind', 200);
        };

        this.hideSendSettings = function() {
            $('.wm-mailing-send-settings-link').removeClass('toggle_open');
            $('.wm-mailing-send-settings-link').addClass('toggle_closed');
            $('.wm-mailing-send-settings').css('display', 'none');
        };

        this.getParameterValue = function(splitBase, splitPart) {
            if (splitBase.length > 4) { //__listsplit_1010101060_5
                for (var i = 0; i < splitBase.length / 2; i++) {
                    if (i + 1 == splitPart) {
                        return splitBase.substring(i * 2, (i + 1) * 2);
                    }
                }
            } else { //__listsplit_1090_90
                for (var i = 0; i < splitBase.length / 2; i++) {
                    if (splitBase.substring(i * 2, (i + 1) * 2) == splitPart) {
                        return splitBase.substring(i * 2, (i + 1) * 2);
                    }
                }
            }
            return null;
        };

        this.initOneMailinglistWarningDialog = function(self) {
            $(self.containerId + '-oneMailinglistWarning-dialog').dialog({
                title: '<span class="dialog-fat-title">' + self.getSelectedMailingOption().html() + '</span>',
                dialogClass: 'no-close',
                width: 350,
                modal: true,
                resizable: false
            });
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

            for (var i = 0; i < mailingTargets.length; i++) {
                if (!contains(configuredTargets, mailingTargets[i])) {
                    return false;
                }
            }
            return true;
        };
        
      var equalAltgs = function(configuredMailingData, mailingData) {
          var configuredAltgs = configuredMailingData.altgs;
          var mailingAltgs = mailingData.altgIds;

          if (configuredAltgs == null && mailingAltgs == null) {
              return true;
          }

          if (configuredAltgs.length != mailingAltgs.length) {
              return false;
          }

          for (var i = 0; i < mailingAltgs.length; i++) {
              if (!contains(configuredAltgs, mailingAltgs[i])) {
                  return false;
              }
          }
          return true;
      };

        var contains = function(array, item) {
            return array.indexOf(item) != -1;
        };
    };

    AGN.Lib.WM.MailingEditorHelper = MailingEditorHelper;

})();
