(function () {

  class ActionBasedMailingNodeEditor extends AGN.Lib.WM.MailingNodeEditor {
    constructor(mailingEditorBase) {
      super(mailingEditorBase);
      this.safeToSave = false;
    }

    get formName() {
      return 'actionbasedMailingForm';
    }

    get title() {
      return t('workflow.mailing.action_based');
    }
    
    get $editor() {
      return $('#actionbased_mailing-editor');
    }
  }

  AGN.Lib.WM.ActionBasedMailingNodeEditor = ActionBasedMailingNodeEditor;
})();
