(function () {
  const Def = AGN.Lib.WM.Definitions;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Select = AGN.Lib.Select;
  const Messages = AGN.Lib.Messages;

  class RecipientNodeEditor extends AGN.Lib.WM.NodeEditor {
    constructor(submitWorkflowForm) {
      super();
      this.submitWorkflowForm = submitWorkflowForm;
      this.targetSelector = '#recipientTargetSelect';
      this.altgSelector = '#recipientAltgSelect';
      this.safeToSave = true;
    }

    get formName() {
      return 'recipientForm';
    }
    
    get $targetSelect() {
      return $(this.targetSelector);
    }
    
    get targetSelect() {
      return Select.get(this.$targetSelect);
    }
    
    get altgSelect() {
      return new Select($(this.altgSelector));
    }

    createNewTarget() {
      EditorsHelper.processForward(Def.constants.forwards.TARGET_GROUP_CREATE.name, this.targetSelector, this.submitWorkflowForm);
    }

    editTarget(targetId) {
      $('#forwardTargetItemId').val(targetId);
      EditorsHelper.processForward(Def.constants.forwards.TARGET_GROUP_EDIT.name, this.targetSelector, this.submitWorkflowForm);
    }

    get title() {
      return t('workflow.recipient');
    }

    fillEditor(node) {
      var data = node.getData();
      if (!Def.constants.isAltgExtended) {
        if (Def.constants.accessLimitTargetId > 0) {
          data.targets = _.union([Def.constants.accessLimitTargetId], data.targets);
          data.targetsOption = 'ALL_TARGETS_REQUIRED';
        }
      }

      this.$form.submit(false);
      this.$form.get(0).reset();
      EditorsHelper.fillFormFromObject(this.formName, data, '');

      this.targetSelect.selectValue(data.targets);
      if (Def.constants.isAltgExtended) {
        this.altgSelect.selectValue(data.altgs);
      }
      this.isValid();
    }

    saveEditor() {
      var data = EditorsHelper.formToObject(this.formName);
      data.targets = this.targetSelect.getSelectedValue();
      if (Def.constants.isAltgExtended) {
        data.altgs = this.altgSelect.getSelectedValue();
      }
      return data;
    }
  }
  
  AGN.Lib.WM.RecipientNodeEditor = RecipientNodeEditor;
})();
