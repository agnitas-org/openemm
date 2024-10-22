(function () {

  class SmsMailingNodeEditor extends AGN.Lib.WM.MailingNodeEditor {
    constructor(mailingEditorBase) {
      super(mailingEditorBase);
      this.safeToSave = false;
    }

    get formName() {
      return 'smsMailingForm';
    }
    
    get title() {
      return t('workflow.mailing.mediatype_sms');
    }
    
    get $editor() {
      return $('#mailing_mediatype_sms-editor');
    }
  }

  AGN.Lib.WM.SmsMailingNodeEditor = SmsMailingNodeEditor;
})();
