(function () {
  const Def = AGN.Lib.WM.Definitions;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;

  class MailingNodeEditor extends AGN.Lib.WM.NodeEditor {
    constructor(mailingEditorBase) {
      super();
      this.safeToSave = true;
      this.mailingEditorBase = mailingEditorBase;
    }

    get formName() {
      return 'mailingForm';
    }

    get title() {
      return t('workflow.defaults.mailing');
    }

    get $editor() {
      return $('#mailing-editor');
    }

    fillEditor(node) {
      this.$form.submit(false);
      this.$form.get(0).reset();

      EditorsHelper.fillFormFromObject(this.formName, node.getData(), '');
      this.mailingEditorBase.fillEditorBase(node);
    }

    createNewMailing() {
      this.mailingEditorBase.createNewMailing();
    }

    editMailing() {
      this.mailingEditorBase.editMailing(Def.constants.forwards.MAILING_EDIT.name);
    }

    saveEditor() {
      this.mailingEditorBase.setNodeFields();
      return EditorsHelper.formToObject(this.formName);
    }

    saveWithCheckStatus() {
      const isNotSent = this.mailingEditorBase.showSecurityQuestion();
      if (isNotSent) {
        EditorsHelper.saveCurrentEditorWithUndo(this.mailingEditorBase);
      }
    }

    copyMailing() {
      this.mailingEditorBase.copyMailing(Def.constants.forwards.MAILING_COPY.name);
    }

    save() {
      this.mailingEditorBase.validateEditor(() => this.saveWithCheckStatus());
    }
  }

  AGN.Lib.WM.MailingNodeEditor = MailingNodeEditor;
})();
