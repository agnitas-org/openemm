(function () {
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Messages = AGN.Lib.Messages;
  const Def = AGN.Lib.WM.Definitions;

  class RecipientTransferEditor extends AGN.Lib.WM.NodeEditor  {
    constructor(conf) {
      super();
      this.conf = conf;
      this.safeToSave = true;
    }

    get formName() {
      throw new Error("formName() must be implemented")
    }

    get $idSelect() {
      return this.$find('select[name=importexportId]');
    }

    get selectedId() {
      return this.$idSelect.val();
    }
    
    get type() {
      throw new Error("type() must be implemented")
    }
    
    get $editor() {
      throw new Error("$editor() must be implemented");
    }
    
    fillEditor(node) {
      const data = node.getData();
      this.$form.submit(false);
      this.$form.get(0).reset();
      EditorsHelper.fillFormFromObject(this.formName, data, '');
      this.addLinks();

      if (this.conf.isDisabled === 'true') {
        Messages.warn(this.missingPermissionMsgCode);
      }
      this.isValid();
    }
    
    get missingPermissionMsgCode() {
      throw new Error("missingPermissionMsgCode() must be implemented");
    }
    
    get dependencyType() {
      throw new Error("dependencyType() must be implemented");
    }

    get inUseMsgCode() {
      throw new Error("inUseMsgCode() must be implemented");
    }

    get notSelectedMsgCode() {
      throw new Error("notSelectedMsgCode() must be implemented");
    }

    isValidDependency() {
      let valid = false;
      $.ajax({
        url: AGN.url('/workflow/validateDependency.action'),
        async: false,
        data: {
          workflowId: Def.workflowId || 0,
          type: this.dependencyType,
          entityId: this.selectedId
        }
      }).done(data => {
        if (data.valid === true) {
          valid = true;
        } else {
          Messages.warn(this.inUseMsgCode);
        }
      }).fail(() => {
        Messages.warn('Error');
      });
      return valid;
    }
    
    isValid() {
      return super.isValid() && this.isValidDependency();
    }
    
    save() {
      if (this.conf.isDisabled === 'true') {
        return;
      }
      super.save();
    }

    onChange() {
      this.addLinks();
    }

    addLinks() {
      throw new Error("addLinks() must be implemented");
    }

    saveEditor() {
      return EditorsHelper.formToObject(this.formName);
    }

    isSetFilledAllowed() {
      const $option = this.$idSelect.find(':selected');
      return !(this.selectedId == 0 || $option.data('is-available') != true);
    }
  }

  AGN.Lib.WM.RecipientTransferEditor = RecipientTransferEditor;
})();
