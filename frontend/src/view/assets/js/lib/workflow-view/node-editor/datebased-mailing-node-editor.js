(function () {

  class DateBasedMailingNodeEditor extends AGN.Lib.WM.MailingNodeEditor {
    constructor(mailingEditorBase) {
      super(mailingEditorBase);
      this.safeToSave = false;
    }

    get formName() {
      return 'datebasedMailingForm';
    }

    get title() {
      return t('workflow.mailing.date_based');
    }

    get $editor() {
      return $('#datebased_mailing-editor');
    }
  }

  AGN.Lib.WM.DateBasedMailingNodeEditor = DateBasedMailingNodeEditor;
})();
