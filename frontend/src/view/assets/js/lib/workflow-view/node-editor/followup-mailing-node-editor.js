(function () {
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Messages = AGN.Lib.Messages;
  const Def = AGN.Lib.WM.Definitions;
  
  class FollowupMailingNodeEditor extends AGN.Lib.WM.NodeEditor {
    
    constructor(baseMailingEditorBase, followupMailingEditorBase, disableFollowup) {
      super();
      this.safeToSave = false;
      this.disableFollowup = disableFollowup;
      this.baseMailingEditorBase = baseMailingEditorBase;
      this.followupMailingEditorBase = followupMailingEditorBase;
    }
    
    get formName() {
      return 'followupMailingForm';
    }
    
    get title() {
      return t('workflow.mailing.followup');
    }

    fillEditor(node) {
      this.followupMailingEditorBase.fillEditorBase(node);
      this.baseMailingEditorBase.fillEditorBase(node);

      this.$form.submit(false);
      this.$form.get(0).reset();

      EditorsHelper.fillFormFromObject(this.formName, node.getData(), '');

      if (this.disableFollowup) {
        Messages.warn('error.workflow.followupPermission');
        this.baseMailingEditorBase.disableInputs();
      }
    }

    createNewMailing() {
      this.followupMailingEditorBase.createNewMailing();
    }

    editMailing() {
      this.followupMailingEditorBase.editMailing(Def.constants.forwards.MAILING_EDIT.name);
    }

    copyMailing() {
      this.followupMailingEditorBase.copyMailing(Def.constants.forwards.MAILING_COPY.name);
    }

    saveEditor() {
      this.setNodeFields();
      return EditorsHelper.formToObject(this.formName);
    }

    saveWithCheckStatus() {
      var isNotSent = this.followupMailingEditorBase.showSecurityQuestion();
      if (isNotSent) {
        EditorsHelper.saveCurrentEditorWithUndo(this.followupMailingEditorBase);
      }
    }

    save() {
      if (this.disableFollowup) {
        return;
      }
      var baseMailingSelector = $(this.baseMailingEditorBase.formNameJId + ' ' + this.baseMailingEditorBase.selectNameJId);
      var followupMailingSelector = $(this.followupMailingEditorBase.formNameJId + ' ' + this.followupMailingEditorBase.selectNameJId);
      if (baseMailingSelector.val() > 0 && followupMailingSelector.val() > 0) {
        this.saveWithCheckStatus();
      } else {
        Messages.warn('error.workflow.noMailing');
      }
    }

    setNodeFields() {
      this.baseMailingEditorBase.node.setFilled(parseInt(this.baseMailingEditorBase.mailingId, 10) > 0 && parseInt(this.followupMailingEditorBase.mailingId, 10) > 0);
    }
  }

  AGN.Lib.WM.FollowupMailingNodeEditor = FollowupMailingNodeEditor;
})();
