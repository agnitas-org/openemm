(function () {
  const MailingSelector = AGN.Lib.WM.MailingSelector;
  const FieldRulesTable = AGN.Lib.WM.FieldRulesTable;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Messages = AGN.Lib.Messages;
  const Def = AGN.Lib.WM.Definitions;

  class DecisionNodeEditor extends AGN.Lib.WM.NodeEditor {
    constructor(editor, config) {
      super();
      this.editor = editor;
      this.config = config;
      this.mailingSelector = new MailingSelector(config.form, config.selectName, config.noMailingOption);
    }

    get saveOnOpen () {
      return true;
    }

    get formName() {
      return 'decisionForm';
    }

    get title() {
      return t('workflow.decision');
    }

    get $rulesContainer() {
      return this.$find('#decisionProfileFieldRules');
    }
    
    get $decisionType() {
      return this.$find('select[name="decisionType"]');
    }
    
    get $decisionCriteria() {
      return this.$find('select[name="decisionCriteria"]');
    }
    
    fillEditor(node) {
      // Field is used to set correct mailingID if "Profile field" selected as decision criteria.
      this.profileFieldMailingId = 0;

      const data = node.getData();

      this.$form.submit(false);
      this.possibleToUseAutoOptimization = this.editor.isAutoOptimizationDecisionAllowed(node);

      if (data.profileField) {
        data.profileField = data.profileField.toLowerCase();
      }

      EditorsHelper.fillFormFromObject(this.formName, data, '');

      this.mailingSelector.setMailingId(data.mailingId);
      this.mailingSelector.cleanOptions(1);

      if (!data.decisionDate) {
        data.decisionDate = new Date();
      }

      const $decisionDateTime = $('#decision-date-time-inputs');
      
      if (this.checkPresentNodeOfType([Def.NODE_TYPE_ACTION_BASED_MAILING, Def.NODE_TYPE_DATE_BASED_MAILING])) {
        $decisionDateTime.hide();
      } else {
        $decisionDateTime.show();
        this.$find('#decisionDate').datepicker("setDate", this.dateAsUTC(data.decisionDate));
        this.$find('#decisionTime').val(('0' + data.decisionDate.getHours()).slice(-2) + ':' + ('0' + data.decisionDate.getMinutes()).slice(-2));
      }

      this.updateVisibility();
      this.updateDateFormatVisibility();

      // update mailing links in editor
      if (data.decisionType == Def.constants.decisionTypeDecision && data.decisionCriteria == Def.constants.decisionReaction &&
        data.reaction == Def.constants.reactionClickedLink && (data.mailingId != 0 && data.mailingId != null)) {
        this.onMailingSelectChange(data.mailingId, data.linkId);
      } else {
        EditorsHelper.resetSelect(this.$find('select[name="linkId"]'));
      }

      this.rulesTable = new FieldRulesTable(this.$rulesContainer, data.rules, EditorsHelper.isReadOnlyMode(), this.config.profileFields);
      
      this.form.cleanFieldFeedback();
    }

    saveEditor() {
      const data = EditorsHelper.formToObject(this.formName);

      //We should use ID of previous mailing if "Profile field" selected in decision criteria,
      // not mailing ID from Mailing dropdown of decision form.
      if (data.decisionCriteria == Def.constants.decisionProfileField && this.profileFieldMailingId != 0) {
        data.mailingId = this.profileFieldMailingId;
      }
      //Field reset.
      this.profileFieldMailingId = 0;

      if (data.rules == undefined) {
        data.rules = [];
      }
      if (!data.threshold) {
        data.threshold = '';
      }

      if (this.checkPresentNodeOfType([Def.NODE_TYPE_ACTION_BASED_MAILING, Def.NODE_TYPE_DATE_BASED_MAILING])) {
        data.decisionDate = null;
      } else {
        data.decisionDate = this.$find('#decisionDate').datepicker("getDate");

        const time = this.$find('#decisionTime').val();
        data.decisionDate.setHours(time.substring(0, 2));
        data.decisionDate.setMinutes(time.substring(3, 5));
      }

      data.rules = this.rulesTable.collect();
      return data;
    }

    save() {
      var decisionType = this.$decisionType.val();
      var decisionCriteria = $('#decisionCriteria').val();
      var mailingId = this.$find('select[name="mailingId"]').val();
      var linkId = this.$find('select[name="linkId"]').val();
      var reaction = this.$find('select[name="reaction"]').val();
      var threshold = this.$find('input[name="threshold"]').val();

      if (decisionType == Def.constants.decisionTypeDecision && decisionCriteria == Def.constants.decisionReaction && mailingId == 0) {
        Messages.alert('error.workflow.noMailing');
      } else if (decisionType == Def.constants.decisionTypeDecision && decisionCriteria == Def.constants.decisionReaction && reaction == Def.constants.reactionClickedLink
        && (linkId == 0 || linkId == null)) {
        Messages.alert('error.workflow.noLinkSelected');
      } else if (decisionType == Def.constants.decisionTypeDecision && decisionCriteria == Def.constants.decisionProfileField) {
        if (this.config.isMailtrackingActive == 'true') {
          const $decisionProfileField = this.$find('#decisionProfileField');
          if (!$.trim($decisionProfileField.val())) {
            this.form.showFieldError('profileField', t('fields.required.errors.missing'))
          } else {
            this.form.cleanFieldError('profileField');
            EditorsHelper.saveCurrentEditorWithUndo();
          }
        }
      } else if (decisionType == Def.constants.decisionTypeAutoOptimization) {
        if (threshold) {
          var thresholdIntValue = parseInt(threshold, 10);
          if (thresholdIntValue && thresholdIntValue > 0) {
            EditorsHelper.saveCurrentEditorWithUndo();
          } else {
            Messages.alert('error.workflow.noValidThreshold');
          }
        } else {
          // Value is omitted (input field is empty)
          EditorsHelper.saveCurrentEditorWithUndo();
        }
      } else {
        EditorsHelper.saveCurrentEditorWithUndo();
      }
    }

    onTypeChanged() {
      const $decisionTypeSelect = this.$decisionType;
      const value = $decisionTypeSelect.val();

      if (!this.possibleToUseAutoOptimization && value === Def.constants.decisionTypeAutoOptimization) {
        Messages.alert('error.workflow.autoOptimizationDecisionForbidden');
        // change selected decision type again to 'Decision'
        $decisionTypeSelect.val(Def.constants.decisionTypeDecision);
        return;
      }

      if (value == Def.constants.decisionTypeDecision) {
        this.hide('#autoOptimizationPanel');
        this.show('#decisionPanel');
      } else if (value == Def.constants.decisionTypeAutoOptimization) {
        this.hide('#decisionPanel');
        this.show('#autoOptimizationPanel');
      }
      this.updateVisibilityOfRuleMailingReceived();
    }

    onDecisionReactionChanged() {
      var value = this.$find('select[name="reaction"]').val();
      if (value == Def.constants.reactionClickedLink) {
        this.show('#reactionLinkPanel');
        var mailingId = this.$find('select[name="mailingId"]').val();
        this.onMailingSelectChange(mailingId, 0);
      } else {
        this.hide('#reactionLinkPanel');
      }
    }

    onCriteriaChanged() {
      const value = this.$decisionCriteria.val();
      if (value == Def.constants.decisionReaction) {
        this.hide('#decisionProfileFieldPanel');
        this.show('#decisionReactionPanel');
      } else if (value == Def.constants.decisionProfileField) {
        this.hide('#decisionReactionPanel');
        this.show('#decisionProfileFieldPanel');
      }
      this.updateVisibilityOfRuleMailingReceived();
    }

    onMailingSelectChange(value, selectedValue) {
      var $linkSelect = this.$find('select[name="linkId"]');
      var mailingId = parseInt(value, 10);

      if (mailingId > 0) {
        $linkSelect.attr('readonly', 'readonly');
        $.ajax({
          url: AGN.url(`/mailing/ajax/${mailingId}/links.action`),
          success: function (data) {
            // populate the drop-down list with mailing links
            $linkSelect.empty();

            $linkSelect.append($('<option></option>', {value: '', text: Def.EMPTY_OPTION_PLACEHOLDER}));
            $.each(data, function (index, itemUrl) {
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
    }
    
    onProfileFieldChanged() {
      this.updateDateFormatVisibility();
      this.rulesTable?.onProfileFieldChanged();
    }
    
    updateDateFormatVisibility() {
      const profileField = this.$find('select[name="profileField"]').val();
      const fieldType = this.config.profileFields[profileField];
      this.toggle('#decisionDateFormat', fieldType && fieldType.toLowerCase() == Def.FIELD_TYPE_DATE);
    }

    updateVisibility() {
      this.onTypeChanged();
      this.onDecisionReactionChanged();
      this.onCriteriaChanged();
    }

    updateVisibilityOfRuleMailingReceived() {
      const self = this;
      const decisionType = this.$decisionType.val();
      if (decisionType == Def.constants.decisionTypeAutoOptimization) {
        this.hide('#ruleMailingReceivedWrapper');
      } else if (decisionType == Def.constants.decisionTypeDecision) {
        var decisionCriteria = this.$decisionCriteria.val();
        if (decisionCriteria == Def.constants.decisionReaction) {
          this.hide('#ruleMailingReceivedWrapper');
        } else if (decisionCriteria == Def.constants.decisionProfileField) {
          this.hide('#ruleMailingReceivedWrapper');
          //check if mailing icon exists
          EditorsHelper.forEachPreviousNode(function(node) {
            if (Def.NODE_TYPES_MAILING.includes(node.getType())) {
              var data = node.getData();
              // Memorizes mailing ID to be set as decisions mailingId.
              this.profileFieldMailingId = data.mailingId;
              self.show('#ruleMailingReceivedWrapper');
              return false;
            }
          });
        }
      }
    }

    checkPresentNodeOfType(types) {
      return EditorsHelper.getNodesByTypes(types).length > 0;
    }
  }

  AGN.Lib.WM.DecisionNodeEditor = DecisionNodeEditor;
})();
