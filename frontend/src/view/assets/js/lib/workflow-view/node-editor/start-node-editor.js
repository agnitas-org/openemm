(() => {

  const MailingSelector = AGN.Lib.WM.MailingSelector;
  const FieldRulesTable = AGN.Lib.WM.FieldRulesTable;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const DateTimeUtils = AGN.Lib.WM.DateTimeUtils;
  const Template = AGN.Lib.Template;
  const Messages = AGN.Lib.Messages;
  const Select = AGN.Lib.Select;
  const Def = AGN.Lib.WM.Definitions;

  class StartStopNodeEditor extends AGN.Lib.WM.NodeEditor {
    constructor(editor, config, submitWorkflowForm) {
      super();
      this.editor = editor;
      this.config = config;
      this.submitWorkflowForm = submitWorkflowForm;
      this.startMailingSelector = new MailingSelector(config.form, config.selectedName, config.noMailingOption);
      this.isStartEditor = false;
      this.timePattern = /^(\d{2}):(\d{2})$/;
    }

    get saveOnOpen () {
      return true;
    }
    
    get formName() {
      return 'startForm';
    }

    get title() {
      return this.isStartEditor ? t('workflow.start.title') : t('workflow.stop.title');
    }
    
    get $endTypeSelect() {
      return this.$find('[name="endType"]');
    }
    
    get endTypeSelect() {
      return Select.get(this.$endTypeSelect);
    }

    get $rulesContainer() {
      return this.$find('#profileFieldRules');
    }

    get requiredMsg() {
      return t('fields.required.errors.missing');
    }
    
    getStartStopDate(data) {
      data = data || EditorsHelper.formToObject(this.formName);

      if (this.isStartEditor) {
        switch (data.startType) {
          case Def.constants.startTypeDate:
            return this.$find('#startDate').datepicker("getDate");
          case Def.constants.startTypeEvent:
            return this.$find('#executionDate').datepicker("getDate");
        }
      } else if (data.endType == Def.constants.endTypeDate) {
        return this.$find('#startDate').datepicker('getDate');
      }
      return null;
    }

    generateReminderComment() {
      const name = $('#workflowForm input[name="shortname"]').val();
      const dateString = DateTimeUtils.getDateStr(this.getStartStopDate(), window.adminDateFormat);

      return (this.isStartEditor ? t('workflow.start.reminder_text') : t('workflow.stop.reminder_text'))
        .replace(/:campaignName/g, name)
        .replace(/:startDate/g, dateString)
        .replace(/:endDate/g, dateString);
    }

    fillEditor(node) {
      var data = node.getData();

      this.isStartEditor = node.getType() === 'start';
      const showStartEventOption = this.isStartEditor &&
        (node.data.startType === Def.constants.startTypeEvent || !this.editor.isAutoOptimizationWorkflow(node));

      this.updateEditorType(this.isStartEditor, showStartEventOption);
      
      this.$form.submit(false);

      this.$find('#remindCalendarDateTitle').text(t(this.isStartEditor ? 'workflow.start.start_date' : 'workflow.stop.end_date'));

      //this time zone will be used during sending reminders
      data.adminTimezone = this.config.adminTimezone;
      data.sendReminder = data.scheduleReminder;

      EditorsHelper.fillFormFromObject(this.formName, data, '');
      this.startMailingSelector.setMailingId(data.mailingId);
      this.startMailingSelector.cleanOptions();
      this.#updateMailingEditBtn(data.mailingId);

      //init datepickers
      const currentDate = new Date();
      this.$find('#startDate').datepicker("setDate", this.dateAsUTC(data.date ?? currentDate));
      this.$find('#executionDate').datepicker("setDate", this.dateAsUTC(data.date ?? currentDate));
      this.$find('#remindDate').datepicker("setDate", this.dateAsUTC(data.remindDate ?? currentDate));

      //init timepickers
      const timeData = data.date ? data : DateTimeUtils.getCurrentAdminTime();
      this.$find('#startTime').val(this.formatTime(timeData.hour, timeData.minute));
      this.$find('#remindTime').val(this.formatTime(timeData.remindHour, timeData.remindMinute));

      this.updateVisibility();

      // update mailing links in editor
      if (data.startType == Def.constants.startTypeEvent && data.event == Def.constants.startEventReaction && data.reaction == Def.constants.reactionClickedLink
        && data.mailingId != 0 && data.mailingId != null) {
        this.onMailingSelectChange(data.mailingId, data.linkId);
      }

      if (!data.comment) { // init reminder comment
        this.$find('#reminderComment').val(this.generateReminderComment());
      }
      Select.get($('#start-editor [name="userType"]')).selectOption(data.recipients != '' ? 2 : 1); // init reminder user type

      this.form.cleanFieldFeedback();

      const forceAutomaticEndType = !this.isStartEditor && this.editor.isAllLinkedFilledStartsHasDateType(node);
      if (forceAutomaticEndType) {
        this.endTypeSelect.selectOption(Def.constants.endTypeAutomatic)
      }
      this.endTypeSelect.setReadonly(forceAutomaticEndType);

      this.rulesTable = new FieldRulesTable(this.$rulesContainer, data.rules, EditorsHelper.isReadOnlyMode(), this.config.profileFields);
    }

    #updateMailingEditBtn(mailingId) {
      const $btn = $('#edit-start-editor-mailing-btn');
      $btn.toggleClass('hidden', !mailingId);

      if (mailingId) {
        $btn.attr('href', AGN.url(`/mailing/${mailingId}/settings.action`));
      }
    }

    editMailing() {
      const elementId = `form[name="${this.formName}"] select[name=mailingId]`;
      $('#forwardTargetItemId').val($(elementId).val());
      EditorsHelper.processForward(Def.constants.forwards.MAILING_EDIT.name, elementId, this.submitWorkflowForm);
    }

    saveEditor() {
      const data = EditorsHelper.formToObject(this.formName);

      data.date = this.getStartStopDate(data);

      if (data.date == null) {
        data.date = new Date();
        data.date.setHours(0);
        data.date.setMinutes(0);
        data.date.setSeconds(0);
        data.date.setMilliseconds(0);
      }

      var match;

      var startTime = this.$find('#startTime').val();
      if (match = this.timePattern.exec(startTime)) {
        data.hour = match[1];
        data.minute = match[2];
      } else {
        data.hour = 0;
        data.minute = 0;
      }

      data.scheduleReminder = data.sendReminder;

      var remindTime = this.$find('#remindTime').val();
      if (match = this.timePattern.exec(remindTime)) {
        data.remindHour = match[1];
        data.remindMinute = match[2];
      } else {
        data.remindHour = 0;
        data.remindMinute = 0;
      }

      if (this.$find('[name="remindSpecificDate"]').val() === 'true') {
        data.remindDate = this.$find('#remindDate').datepicker("getDate");
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

      if (data.dateFieldOperator != this.config.equelsOperatorCode) {
        data.dateFieldOperator = this.config.equelsOperatorCode;
      }

      if (data.userType == 2) {
        data.remindAdminId = 0;
        data.recipients = $.trim(data.recipients)
          .toLowerCase()
          .split(/[,;\s\n\r]+/)
          .filter(function (address) {
            return !!address;
          })
          .join(', ');
      } else {
        data.recipients = '';
      }
      delete data.userType;

      data.rules = this.rulesTable.collect();
      return data;
    }

    validateEditorInternal(showErrors) {
      const self = this;
      let valid = true;

      const type = this.isStartEditor ?
        this.$find('select[name="startType"]').val() :
        this.$find('select[name="endType"]').val();

      if (this.isStartEditor && type === Def.constants.startTypeEvent) {
        validateStartEvent();
        validateStartDate();
      }

      if ((this.isStartEditor && type === Def.constants.startTypeDate)
        || (!this.isStartEditor && type === Def.constants.endTypeDate)) {
        validateStartDate();
      }

      if (valid && this.$find('#sendReminder').is(':checked')) {
        if (this.$find('[name="userType"]').val() == '2') {
          validateReminderRecipients();
        }
      }

      if (valid) {
        this.form.cleanFieldFeedback();
      }
      return valid;

      function validateStartEvent() {
        var event = self.$find('select[name="event"]').val();
        switch (event) {
          case Def.constants.startEventDate:
            var executionDate = self.$find('#executionDate').datepicker("getDate");
            if (executionDate) {
              var startTime = self.$find('#startTime').val();
              if (!self.timePattern.test(startTime)) {
                valid = false;
              }
            } else {
              valid = false;
            }
            var dateFieldValue = self.$find('#dateFieldValue');
            if (!$.trim(dateFieldValue.val())) {
              valid = false;
              if (showErrors) {
                Messages.warn('error.workflow.startDateOmitted');
              }
            }
            const $dateProfileField = self.$find('#dateProfileField');
            if (!$.trim($dateProfileField.val())) {
              valid = false;
              if (showErrors) {
                self.form.showFieldError('dateProfileField', self.requiredMsg)
              }
            }
            break;

          case Def.constants.startEventReaction:
            var reaction = self.$find('select[name="reaction"]').val();
            switch (reaction) {
              case Def.constants.reactionClickedLink:
                const linkId = self.$find('select[name="linkId"]').val();
                if (!linkId || parseInt(linkId, 10) <= 0) {
                  valid = false;
                  if (showErrors) {
                    Messages.warn('error.workflow.noLinkSelected');
                  }
                }
              // Fall-through
              case Def.constants.reactionOpened:
              case Def.constants.reactionClicked:
                const mailingId = self.$find('select[name="mailingId"]').val();
                if (parseInt(mailingId, 10) <= 0) {
                  valid = false;
                  if (showErrors) {
                    Messages.warn('error.workflow.noMailing');
                  }
                }
                break;
              case Def.constants.reactionChangeOfProfile:
                if (typeof (self.config.isBigData) != 'undefined' && self.config.isBigData == true) {
                  var profileField = self.$find('select[name="profileField"]').val();
                  if (!profileField) {
                    valid = false;
                  }
                }
                const $startProfileField = self.$find('#startProfileField');
                if (!$.trim($startProfileField.val())) {
                  valid = false;
                  if (showErrors) {
                    self.form.showFieldError('profileField', self.requiredMsg)
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
        var startDate = self.$find('#startDate').datepicker("getDate");
        if (startDate) {
          var startTime = self.$find('#startTime').val();
          if (!self.timePattern.test(startTime)) {
            valid = false;
          }
        } else {
          valid = false;
        }
      }

      function validateReminderRecipients() {
        var emails = $.trim(self.$find('textarea[name="recipients"]').val())
          .split(/[,;\s\n\r]+/)
          .filter(function (address) {
            return !!address
          });

        if (!emails.length) {
          if (showErrors) {
            valid = false;
            Messages.warn('error.workflow.emptyRecipientList');
          }
        }
      }
    }

    isValid() {
      return this.validateEditorInternal(true) && super.isValid();
    }

    isSetFilledAllowed() {
      return this.validateEditorInternal(false);
    }

    updateEditorType(isStartEditor, showStartEventOption) {
      const $type = $('#startStopType');

      $type.empty();

      if (isStartEditor) {
        $type.append(Template.text('start-types', {showStartEventTab: showStartEventOption}));
        $('#eventReaction').html(t('workflow.start.reaction_based'));
        $('#eventDate').html(t('workflow.start.date_based'));
      } else {
        $type.append(Template.text('stop-types'));
        if (this.checkPresentNodeOfType([Def.NODE_TYPE_ACTION_BASED_MAILING, Def.NODE_TYPE_DATE_BASED_MAILING])) {
          $('#endTypeAutomaticLabel').html(t('workflow.stop.open_end'));
        } else {
          $('#endTypeAutomaticLabel').html(t('workflow.stop.automatic_end'));
        }
      }
    }
    
    onStartTypeChanged() {
      const $startIconTime = this.$find('#startIconTime');
      const value = this.isStartEditor ?
        this.$find('select[name="startType"]').val() :
        this.$find('select[name="endType"]').val();
      if ((this.isStartEditor && value == Def.constants.startTypeDate)
        || (!this.isStartEditor && value == Def.constants.endTypeDate)) {
        this.toggleFormat();
        this.hide('#startEventPanel');
        this.show('#startDatePanel');
        this.show('#startIconTime');
        this.$find('#startDatePanel .date-time-container').append($startIconTime);
        this.show('#startRemindAdmin');
      } else if (this.isStartEditor && value == Def.constants.startTypeEvent) {
        this.hide('#startDatePanel');
        this.$find('#executionDatePanel .date-time-container').append($startIconTime);
        this.show('#startEventPanel');
        this.show('#startIconTime');
        this.show('#startRemindAdmin');
        this.onStartEventChanged();
      } else if ((this.isStartEditor && value == Def.constants.startTypeOpen)
        || (!this.isStartEditor && value == Def.constants.endTypeAutomatic)) {
        this.hide('#startDatePanel');
        this.$find('#executionDatePanel .date-time-container').append($startIconTime);
        this.hide('#startEventPanel');
        this.hide('#startIconTime');
        this.hide('#startRemindAdmin');
      }
    }

    onStartEventChanged() {
      var event = $('#startEvent').val();

      if (event == Def.constants.startEventReaction) {
        this.toggleFormat();
        this.hide('#dateStartPanel');
        this.show('#reactionStartPanel');
        this.onExecutionChanged();
        this.onProfileFieldChanged();
      } else if (event == Def.constants.startEventDate) {
        this.toggleFormat();
        this.hide('#reactionStartPanel');
        this.show('#dateStartPanel');
        this.hide('#executionDateLabel');
        this.show('#firstExecutionDateLabel');
        $('#startIconDateFormat').insertAfter('#startIconDateFieldOperator');
        this.show('#startIconDateFormat');
      }
    }

    toggleFormat() {
      this.$find('input#startTime').inputmask(this.isDateBasedStart ? 'h:00' : 'h:s');
    }
    
    get isDateBasedStart() {
      return this.$find('#start-type').val() === Def.constants.startTypeEvent
        && this.$find('select#startEvent').val() === Def.constants.startEventDate;
    }

    onRuleModeChanged() {
      this.toggle('#profileFieldRules', $('select[name="useRules"]').val() !== 'false')
    }

    onProfileFieldChanged() {
      this.updateDateFormatVisibility();
      this.rulesTable?.onProfileFieldChanged();
    }

    updateDateFormatVisibility() {
      const profileField = this.$find('select[name="profileField"]').val();
      const fieldType = this.config.profileFields[profileField];
      if (fieldType && fieldType.toLowerCase() == Def.FIELD_TYPE_DATE) {
        $('#startIconDateFormat').insertAfter('#reactionProfileField');
        this.show('#startIconDateFormat');
      } else {
        this.hide('#startIconDateFormat');
      }
    }
    
    onExecutionChanged() {
      const value = this.$find('[name="executeOnce"]').val();
      this.toggle('#executionDateLabel', value !== 'false');
      this.toggle('#firstExecutionDateLabel', value === 'false');
    }

    onReactionChanged() {
      const selectedReaction = this.$find('#startReaction').val();
      if (selectedReaction == Def.constants.reactionOpened
        || selectedReaction == Def.constants.reactionNotOpened
        || selectedReaction == Def.constants.reactionClicked
        || selectedReaction == Def.constants.reactionNotClicked
        || selectedReaction == Def.constants.reactionBought
        || selectedReaction == Def.constants.reactionNotBought
        || selectedReaction == Def.constants.reactionClickedLink) {
        this.hide('#reactionStartProfile');
        this.show('#reactionStartMailing');
      } else if (selectedReaction == Def.constants.reactionChangeOfProfile) {
        this.hide('#reactionStartMailing');
        this.show('#reactionStartProfile');
      } else {
        this.hide('#reactionStartMailing');
        this.hide('#reactionStartProfile');
      }
      this.toggle('#reactionStartMailingLink', selectedReaction === Def.constants.reactionClickedLink);
    }

    onReminderChanged() {
      if (this.$find('input[name="sendReminder"]').is(':checked')) {
        this.$find('#reminderComment').val(this.generateReminderComment());
        this.show('#reminderDetails');
      } else {
        this.hide('#reminderDetails');
      }
      return false;
    }

    onScheduleReminderDateChanged() {
      this.toggle('#dateTimePicker', this.$find('select[name="remindSpecificDate"]').val() === 'true')
      return false;
    }

    onMailingSelectChange(value, selectedValue) {
      const $linkSelect = this.$find('select[name="linkId"]');
      const mailingId = parseInt(value, 10);

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

      this.#updateMailingEditBtn(mailingId);
    }

    updateVisibility() {
      this.onStartTypeChanged();
      this.onStartEventChanged();
      this.onReactionChanged();
      this.onRuleModeChanged();
      this.onExecutionChanged();
      this.onReminderChanged();
      this.onScheduleReminderDateChanged();
    }

    formatTime(hours, minutes) {
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
    }

    checkPresentNodeOfType(types) {
      return EditorsHelper.getNodesByTypes(types).length > 0;
    }
  }

  AGN.Lib.WM.StartStopNodeEditor = StartStopNodeEditor;
})();
