AGN.Lib.Controller.new('mailing-settings-base-view', function () {

  const Messaging = AGN.Lib.Messaging;
  const Messages = AGN.Lib.Messages;
  const Form = AGN.Lib.Form;
  const EMAIL_CHANGED_MSG_KEYS = {
    '11': 'mailing.default.sender_and_reply_emails_changed',
    '10': 'mailing.default.sender_email_changed',
    '01': 'mailing.default.reply_email_changed'
  };
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

    const mailinglistData = mailinglists.find(data => data.id == mailinglistId);

    if (mailinglistData) {
      const $senderEmail = $('#emailSenderMail');
      const $replyEmail = $('#emailReplyEmail');

      if (!$senderEmail.exists() && !$replyEmail.exists()) {
        return;
      }

      const changeSenderEmail = mailinglistData.senderEmail && $senderEmail.val() !== mailinglistData.senderEmail;
      const changeReplyEmail = mailinglistData.replyEmail && $replyEmail.val() !== mailinglistData.replyEmail;

      if (changeSenderEmail) {
        $senderEmail.val(mailinglistData.senderEmail).trigger('change');
      }
      if (changeReplyEmail) {
        $replyEmail.val(mailinglistData.replyEmail);
      }

      const msgKey = _.result(EMAIL_CHANGED_MSG_KEYS, `${+!!changeSenderEmail}${+!!changeReplyEmail}`);
      if (msgKey) {
        Messages.info(msgKey);
      }
    }
  });

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
