AGN.Lib.Controller.new('mailing-settings-base-view', function () {

  const Messaging = AGN.Lib.Messaging;
  const Messages = AGN.Lib.Messages;
  const Form = AGN.Lib.Form;
  let selectedRemovedMailinglistId;
  let mailinglists = [];

  Messaging.subscribe('mailing-settings:updateMailinglistForFollowUp', () => {
    setMailingListSelectByFollowUpMailing();
  });

  this.addDomInitializer('mailing-settings-base-view', function () {
    selectedRemovedMailinglistId = this.config.selectedRemovedMailinglistId;
    mailinglists = this.config.mailinglists;

    if (selectedRemovedMailinglistId) {
      showRemovedMailinglistError(this.el);
    }

    updateGenerateButton();

    if (this.config.isFollowUpMailing) {
      setMailingListSelectByFollowUpMailing();
    }
  });

  function showRemovedMailinglistError($el) {
    Form.get($el).showFieldError(
      'mailinglistId',
      t('fields.mailinglist.errors.removed'),
      true
    );
  }

  this.addAction({change: 'save-mailing-list-id'}, function () {
    const mailinglistId = this.el.select2("val");

    Messaging.send('mailing-settings:mailinglistChanged', mailinglistId);

    if (selectedRemovedMailinglistId) {
      AGN.Lib.Select.get(this.el).$findOption(selectedRemovedMailinglistId).remove();
      Form.get($(this.el)).cleanFieldError('mailinglistId');
      selectedRemovedMailinglistId = null;
    }
    updateSenderAndReplyToFields(mailinglistId);
  });

  function updateSenderAndReplyToFields(mailinglistId) {
    const mailinglist = mailinglists.find(data => data.id === +mailinglistId);

    if (!mailinglist) {
      return;
    }

    const changedLabels = [
      {value: mailinglist.senderEmail, $field: $('#emailSenderMail')},
      {value: mailinglist.replyEmail, $field: $('#emailReplyEmail')},
      {value: mailinglist.senderName, $field: $('#emailSenderName')},
      {value: mailinglist.replyToName, $field: $('#emailReplyName')}
    ]
      .filter(({value, $field}) => value && $field.val() !== value)
      .map(({value, $field}) => {
        $field.val(value).trigger('change');
        return $(`label[for="${$field.attr('id')}"]`).text().trim().replaceAll(/\s*\*/g, '');
      });

    if (changedLabels.length) {
      Messages.info('mailing.default.sender_and_reply_emails_changed');
    }
  }

  this.addAction({'change input': 'change-mailing-base-info'}, function () {
    updateGenerateButton();
  });

  function updateGenerateButton() {
    const $generateBtn = $("#generateBtn");
    if (!$generateBtn.exists()) {
      return;
    }

    const isFormValid = Form.get($generateBtn).valid();

    $generateBtn.toggleClass('btn-grey-out', !isFormValid);
    AGN.Lib.Tooltip.toggleState($generateBtn, !isFormValid);
  }

  this.addAction({change: 'set-parent-mail-mailinglist'}, function () {
    setMailingListSelectByFollowUpMailing();
  });

  function setMailingListSelectByFollowUpMailing() {
    const parentMailingId = $('#lightWeightMailingList').val();
    const selectedParentMailingMailingListId = $(`#parentmailing-${parentMailingId}-mailinglist`).attr('data-mailing-list-id');

    const mailingListSelect = AGN.Lib.Select.get($('#settingsGeneralMailingList'));

    mailingListSelect.selectOption(selectedParentMailingMailingListId);
    mailingListSelect.setReadonly(true);
  }

  this.addAction({click: 'generate-mailing-for-workflow'}, function () {
    const form = Form.get(this.el);
    if (!form.validate()) {
      return;
    }

    form.jqxhr().done(resp => {
      if (resp.success) {
        Messaging.send('workflow:mailingCreated', resp.data);
        AGN.Lib.Modal.getInstance(this.el).hide();
      } else {
        AGN.Lib.JsonMessages(resp.popups, true);
      }
    });
  });

});
