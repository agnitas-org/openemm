(function () {
  const Def = AGN.Lib.WM.Definitions;
  const Node = AGN.Lib.WM.Node;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Utils = AGN.Lib.WM.Utils;
  const Messages = AGN.Lib.Messages;
  const Dialogs = AGN.Lib.WM.Dialogs;
  const Select = AGN.Lib.Select;

  const MailingEditorHelper = function (data, submitWorkflowForm) {
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
    this.mailingType = parseInt(data.mailingType);
    this.mediaType = data.mediaType;
    this.mediatypes = data.mediatypes;
    this.mailingTypesForLoading = data.mailingTypesForLoading;
    this.statusName = data.mailingStatus;
    this.selectName = data.selectName;
    this.selectNameJId = 'select[name=' + data.selectName + ']';
    this.mailingId = 0;
    this.prevMailingId = 0;
    this.showCreateEditLinks = data.showCreateEditLinks;
    this.$dropdown = null;
    if (data.$dropdown) {
      this.$dropdown = data.$dropdown;
    }
    //follow up base mailing settings for aadvertising content type
    this.isChangeDecisionOptions = data.isFollowUpBaseMailing;
    this.followUpContainer = data.followUpContainer;
    this.advertisingAdditionalOptions = data.advertisingAdditionalOptions;

    //chain of nodes which should be filled with mailing data
    this.nodesChain = [];
    //mailing data which we received
    this.mailingData = {};

    //collected information based on incoming nodes for mailing node
    this.configuredMailingData = null;

    var self = this;

    this.fillEditorBase = function (node) {
      this.node = node;
      if (Utils.checkActivation()) {
        this.mailingsStatus = 'all';
      } else {
        this.mailingsStatus = $(this.formNameJId).find('input[checked=checked]').val() || 'all';
      }
      this.mailingsSort = this.defaultMailingsSort;
      this.mailingsOrder = this.defaultMailingsOrder;
      this.cleanOptions();
      this.hideSendSettings();
      this.changeDecisionOptionsSet($(this.containerId), this.isChangeDecisionOptions);
    };

    this.cleanOptions = function () {
      $(this.containerId + ' #' + this.statusName + '_all').attr('checked', 'checked');
      this.activateInputs();
      this.setSelectMailingOptions(this.node.data[this.selectName]);
      this.getMailingsByWorkStatus(this.mailingsStatus, this.mailingsSort, this.mailingsOrder, this.node.data.mailingId);
    };

    this.onMailingsStatusChange = function (val) {
      this.setSelectMailingOptions(0);
      this.getMailingsByWorkStatus(val, this.mailingsSort, this.mailingsOrder);
      this.changeDecisionOptionsSet($(this.containerId), this.isChangeDecisionOptions);
    };

    this.onMailingSelectChange = function (val) {
      this.setSelectMailingOptions(val);
      this.changeDecisionOptionsSet($(this.containerId), this.isChangeDecisionOptions);
    };

    this.changeDecisionOptionsSet = function (scope, isChangeDecisionOptions) {
      if (isChangeDecisionOptions) {
        var selectedMailingId = this.mailingId;
        var followUpSelect = $(scope.find(this.followUpContainer));
        var additionalOptions = this.advertisingAdditionalOptions;
        $(followUpSelect.find('.advertisingOption')).remove();
        if (selectedMailingId) {
          $.ajax({
            type: 'POST',
            url: AGN.url("/mailing/ajax/" + selectedMailingId + "/isAdvertisingContentType.action")
          }).done(function (resp) {
            if (resp && resp.success) {
              var options = AGN.Lib.Template.text('followupAdvertisingOptions', {items: additionalOptions});
              followUpSelect.append(options);
            }
          });
        }
      }
    };

    this.setMailingId = function (mailingId) {
      this.prevMailingId = this.mailingId;
      this.mailingId = mailingId;

      if (this.$dropdown) {
        if (this.mailingId > 0) {
          this.$dropdown.show();
        } else {
          this.$dropdown.hide();
        }
      }

    };

    this.setSelectMailingOptions = function (mailingId) {
      this.setMailingId(mailingId);
      if (this.showCreateEditLinks) {
        this.setMailingLinks(mailingId);
      }
    };

    this.setMailingLinks = function (mailingId) {
      const $link = $(this.getMailingLink(mailingId));
      $link.addClass('btn btn-icon btn-primary');
      $(this.containerId + ' #mailing_create_edit_link').html($link);
    };

    this.getMailingLink = function (mailingId) {
      if (!mailingId || mailingId === '0') {
        return '<a href="#" data-action="mailing-editor-new"><i class="icon icon-plus"></i></a>';
      }
      if (!Utils.checkActivation()) {
        return '<a href="#" data-action="mailing-editor-edit"><i class="icon icon-pen"></i></a>';
      }
      return `<a href="${AGN.url('/mailing/' + mailingId + '/settings.action')}"><i class="icon icon-pen"></i></a>`;
    }

    this.showSecurityQuestion = function () {
      this.setSecurityQuestion();
      this.setIsUsedInCMAttr();

      if (this.isSentMailing(this.mailingIsSent) || this.mailingIsUsedInCM) {
        const selectedOption = this.getSelectedMailingOption();
        Dialogs.mailingInUseDialog(selectedOption.html())
          .then(() => EditorsHelper.getCurrentEditor().copyMailing())
          .fail(() => {
            this.mailingId = this.prevMailingId || 0;
            this.mailingSelect().selectValue(this.prevMailingId);
          });
        
        const $modal = $('.modal');
        const confirm = AGN.Lib.Confirm.get($modal);
        $modal.on('modal:close', () => confirm.negative());
        
        return false;
      }
      return true;
    };

    this.isSentMailing = function (status) {
      return status == 'mailing.status.sent' || status == 'mailing.status.scheduled' || status == 'mailing.status.norecipients';
    };

    this.getMailingsByWorkStatus = function (status, sort, order, selectedMailingValue) {
      const mailingEditorBase = this;
      const $mailings = this.$mailingSelect();
      const mailingsSelect = Select.get($mailings);
      mailingsSelect.setReadonly(true);

      $.ajax({
        url: AGN.url('/workflow/getMailingsByWorkStatus.action'),
        data: {
          mailingTypes: this.mailingTypesForLoading.join(','),
          mediatypes: this.mediatypes ? this.mediatypes.join(',') : '',
          status: status,
          sort: sort,
          order: order
        },
        success: function (data) { //populate the drop-down list with mailings
          mailingsSelect.resetOptions();
          mailingsSelect.addOption(0, t('workflow.defaults.no_mailing'));

          data.forEach(function (d) {
            const $option = $('<option></option>', {
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

          mailingsSelect.setReadonly(false);
          mailingEditorBase.mailingsStatus = status;
          EditorsHelper.initSelectWithValueOrChooseFirst($mailings, mailingEditorBase.mailingId);
        }
      });
    };

    this.disableInputs = function () {
      $(this.containerId + ' [name="' + this.statusName + '"]').attr('disabled', 'disabled');
    };

    this.activateInputs = function () {
      $(this.containerId + ' [name="' + this.statusName + '"]').removeAttr('disabled');
    };

    this.setNodeFields = function () {
      this.node.setFilled(parseInt(this.mailingId, 10) > 0);
    };

    this.setSecurityQuestion = function () {
      var selectedMailingOption = this.getSelectedMailingOption();
      this.mailingIsSent = '';
      if (parseInt(this.mailingId, 10) > 0) {
        this.mailingIsSent = selectedMailingOption.data('status');
      }
    };

    this.setIsUsedInCMAttr = function () {
      var selectedMailingOption = this.getSelectedMailingOption();
      var dependentCampaign = selectedMailingOption.data('dependent-campaigns');
      var mailingId = selectedMailingOption.val();

      this.mailingIsUsedInCM = false;

      if (parseInt(this.mailingId, 10) > 0 && mailingId == this.mailingId && dependentCampaign) {
        var workflowId = Def.workflowId || 0;

        if (dependentCampaign > 0) {
          if (workflowId > 0) {
            this.mailingIsUsedInCM = workflowId !== dependentCampaign;
          } else {
            this.mailingIsUsedInCM = true;
          }
        }
      }
    };

    this.getSelectedMailingOption = function () {
      return $(this.formNameJId + ' ' + this.selectNameJId + ' option[value=' + this.mailingId + ']');
    };

    this.getSelectedDecisionOption = function () {
      var selector = 'decisionCriterion';
      var val = $(this.formNameJId + ' select[name="' + selector + '"]').val();
      return $(this.formNameJId + ' select[name="' + selector + '"] option[value=' + val + ']');
    };

    this.createNewMailing = function () {
      if (!checkHasMailinglist(this.node)) {
        Messages.warn('error.workflow.notAddedMailingList');
        return;
      }
      if ([Def.NODE_TYPE_MAILING_MEDIATYPE_POST, Def.NODE_TYPE_MAILING_MEDIATYPE_SMS].includes(this.node.type)) {
        this.openSelectTemplateToCreateMailingView(Def.constants.forwards.MAILING_CREATE_STANDARD.url);
      } else {
        Dialogs.createMailing().done(url => this.openSelectTemplateToCreateMailingView(url));
      }
    };

    this.openSelectTemplateToCreateMailingView = function (url) {
      return $.get(AGN.url(url), {
        workflowId: Def.workflowId,
        workflowForwardParams: this._getCreateMailingForwardParams()
      }).done(resp => AGN.Lib.Page.render(resp));
    }

    function checkHasMailinglist(node) {
      let mailinglistId = 0;
      EditorsHelper.curEditingNode = node;
      EditorsHelper.forEachPreviousNode(function (prevNode) {
        if (prevNode.type === Def.NODE_TYPE_RECIPIENT) {
          mailinglistId = prevNode.data.mailinglistId;
          return false;
        }
      })
      return mailinglistId > 0;
    }

    this._getCreateMailingForwardParams = function () {
      const elemSelector = this.formNameJId + ' ' + this.selectNameJId;
      const params = {
        nodeId: EditorsHelper.curEditingNode.getId(),
        elementId: encodeURIComponent(elemSelector),
        mailingType: this.mailingType,
      }
      if (this.mediaType) {
        params.mediaType = this.mediaType;
      }
      if (this.mailingType === this.MAILING_TYPE_FOLLOWUP) {
        params.workflowFollowUpParentMailing = this.node.data.baseMailingId;
        params.workflowFollowUpDecisionCriterion = this.node.data.decisionCriterion;
      }
      if (EditorsHelper.curEditor.safeToSave) {
        EditorsHelper.saveCurrentEditor();
      }
      return Object.entries(params)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join(';')
    }

    this.processCopyAndEditMailingForward = function (mailingId, forwardName, formNameJId, selectNameJId) {
      $('#forwardTargetItemId').val(mailingId);
      const additionalParams = ['mailingType=' + self.mailingType];
      if (this.mailingType === this.MAILING_TYPE_FOLLOWUP) {
        additionalParams.push('workflowFollowUpParentMailing=' + this.node.data.baseMailingId);
        additionalParams.push('workflowFollowUpDecisionCriterion=' + this.node.data.decisionCriterion);
      }
      if (additionalParams.length > 0) {
        EditorsHelper.processForward(forwardName, formNameJId + ' ' + selectNameJId, submitWorkflowForm, additionalParams.join(';'));
      } else {
        EditorsHelper.processForward(forwardName, formNameJId + ' ' + selectNameJId, submitWorkflowForm);
      }
    };

    this.editMailing = function (forwardName) {
      this.processCopyAndEditMailingForward(this.mailingId, forwardName, this.formNameJId, this.selectNameJId);
    };

    this.copyMailing = function (forwardName) {
      this.processCopyAndEditMailingForward(this.mailingId, forwardName, this.formNameJId, this.selectNameJId);
    };

    this.trySupplementChain = function (chain, mailingContent, successCallback) {
      if (chain.length > 0) {
        this.configuredMailingData = collectIncomingMailingData(chain);
        this.nodesChain = chain;
        this.mailingData = mailingContent;

        var paramsToAsk = getMailingParams(self, true);

        if (paramsToAsk.length) {
          Dialogs.confirmMailingDataTransfer(paramsToAsk)
            .done(function (checkedParams) {
              self.transferMailingData(checkedParams);
              self.supplementChain(successCallback);
            })
            .fail(function () {
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
    this.checkDifferentMailingLists = function (mailingId, successCallback, failedCallback) {
      $
        .get(AGN.url(`/workflow/mailing/${mailingId}/info.action`))
        .done(mailingContent => {
          var chain = EditorsHelper.getFirstIncomingChain();
          this.configuredMailingData = collectIncomingMailingData(chain);
          this.nodesChain = chain;
          this.mailingData = mailingContent;

          // How much mailings contains in the chain.
          const mailingIconsCount = chain.filter(node => node.getType() === Def.NODE_TYPE_MAILING).length;
          const mailinglistId = this.configuredMailingData.mailinglistId;
          // If we have several mailings and their mailinglists are different we should show warning.
          if (mailingIconsCount > 1 && mailinglistId != 0 && mailinglistId != mailingContent.mailinglistId) {
            failedCallback(mailingContent);
          } else {
            successCallback(mailingContent);
          }
        });
    };

    var collectIncomingMailingData = function (chain) {
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
              nodeData.targets.forEach(function (target) {
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

    var getMailingParams = function (mailingEditorBase, askAmbiguous) {
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

    var overrideMailingData = function (mailingEditorBase, paramsToBeOverridden) {
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

    this.transferMailingData = function (checkedParams) {
      if (checkedParams) {
        overrideMailingData(self, checkedParams);
      } else {
        overrideMailingData(self, getMailingParams(self, true));
      }
    };

    this.supplementChain = function (successCallback) {
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
            if (allStartNodes.length == 0 && mailingData.mailingType !== this.MAILING_TYPE_ACTIONBASED && mailingData.mailingType !== this.MAILING_TYPE_DATEBASED) {
              isStartNodeSupplemented = true;
            }
          }

          if (isStartNodeSupplemented) {
            this.supplementSingleNode(Def.NODE_TYPE_START, function (node) {
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
        this.supplementSingleNode(Def.NODE_TYPE_PARAMETER, function (node) {
          var data = node.getData();
          data.value = self.getParameterValue(mailingData.splitBase, mailingData.splitPart);
        });
      }

      // Archive
      if (paramsToTransfer.includes(Def.MAILING_PARAM_ARCHIVE)) {
        this.supplementSingleNode(Def.NODE_TYPE_ARCHIVE, function (node) {
          var data = node.getData();
          data.campaignId = mailingData.campaignId;
        });
      }

      // Recipient
      if (paramsToTransfer.includes(Def.MAILING_PARAM_MAILING_LIST) || paramsToTransfer.includes(Def.MAILING_PARAM_TARGET_GROUPS)) {
        this.supplementSingleNode(Def.NODE_TYPE_RECIPIENT, function (node) {
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
        this.supplementSingleNode(Def.NODE_TYPE_DEADLINE, function (node) {
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

    this.supplementSingleNode = function (nodeType, callback) {
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

    var supplementExistingNode = function (nodeType, callback) {
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

    var supplementAddNode = function (nodeType, availableTypesAfter, callback) {
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

    this.acceptOneMailinglistPerCampaign = function () {
      EditorsHelper.mailingSpecificSaveAfterMailinglistCheckModal(self);
    };

    this.$mailingSelect = function () {
      return $(this.formNameJId + ' ' + this.selectNameJId);
    }
    
    this.mailingSelect = function () {
      return Select.get(this.$mailingSelect());
    }
    
    this.validateEditor = function (save) {
      var errorsFound = false;
      // validate delivery settings
      if ($(this.containerId + ' .delivery-settings').length) {
        var maxRecipients = $(this.containerId + ' [name="maxRecipients"]').val();
        if (parseInt(maxRecipients) != maxRecipients) {
          Messages.warn('error.workflow.wrongMaxRecipientsFormat');
          errorsFound = true;
        } else if (parseInt(maxRecipients) < 0) {
          Messages.warn('error.workflow.maxRecipientsLessThanZero');
          errorsFound = true;
        } else if (parseInt(maxRecipients) > 1000000000) {
          Messages.warn('error.workflow.maxRecipientsTooBig');
          errorsFound = true;
        }
      }
      // validate that mailing is selected
      var mailingSelector = $(this.formNameJId + ' ' + this.selectNameJId);
      if (mailingSelector.val() <= 0) {
        Messages.warn('error.workflow.noMailing');
        errorsFound = true;
      }

      if (!errorsFound) {
        if (save) {
          save();
        } else {
          EditorsHelper.saveCurrentEditor();
        }
      }
    };

    this.toggleSendSettings = function (editorId) {
      if ($('#sendSettings_' + editorId).css('display') == 'none') {
        $('#sendSettingsToggle_' + editorId).removeClass('toggle_closed');
        $('#sendSettingsToggle_' + editorId).addClass('toggle_open');
      } else {
        $('#sendSettingsToggle_' + editorId).removeClass('toggle_open');
        $('#sendSettingsToggle_' + editorId).addClass('toggle_closed');
      }
      $('#sendSettings_' + editorId).toggle('blind', 200);
    };

    this.hideSendSettings = function () {
      $('.wm-mailing-send-settings-link').removeClass('toggle_open');
      $('.wm-mailing-send-settings-link').addClass('toggle_closed');
      $('.wm-mailing-send-settings').css('display', 'none');
    };

    this.getParameterValue = function (splitBase, splitPart) {
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

    this.initOneMailinglistWarningDialog = function (self) {
      Dialogs
        .simpleDialog(self.getSelectedMailingOption().html(), t('workflow.mailinglist.onlyOne'))
        .then(() => self.acceptOneMailinglistPerCampaign())
        .fail(() => {
          this.mailingId = this.prevMailingId || 0;
          this.mailingSelect().selectValue(this.prevMailingId);
        });
      const $modal = $('.modal');
      const confirm = AGN.Lib.Confirm.get($modal);
      $modal.on('modal:close', () => confirm.negative());
    };

    var containsMailingSendDate = function (mailingData) {
      return mailingData.sendDate != undefined && mailingData.sendDate != null && mailingData.sendHour != null && mailingData.sendMinute != null;
    };

    var containsMailingPlannedDate = function (mailingData) {
      return mailingData.planDate > 0;
    };

    var extractMailingSendDate = function (mailingData) {
      if (containsMailingSendDate(mailingData)) {
        return new Date(mailingData.sendDate + 'T' + mailingData.sendHour + ':' + mailingData.sendMinute + ':00');
      }
      return null;
    };

    var extractMailingPlannedDate = function (mailingData) {
      if (containsMailingPlannedDate(mailingData)) {
        return new Date(mailingData.planDate);
      }
      return null;
    };

    var excludeTime = function (date) {
      if (date == null) {
        return null;
      } else {
        // We should avoid influence of hours and minutes at the comparison
        return new Date(date.getFullYear(), date.getMonth(), date.getDate());
      }
    };

    // TODO: Should be improved in order to support different target options (and, or, not in)
    var equalTargetGroups = function (configuredMailingData, mailingData) {
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

    var equalAltgs = function (configuredMailingData, mailingData) {
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

    var contains = function (array, item) {
      return array.indexOf(item) != -1;
    };
  };

  AGN.Lib.WM.MailingEditorHelper = MailingEditorHelper;

})();
