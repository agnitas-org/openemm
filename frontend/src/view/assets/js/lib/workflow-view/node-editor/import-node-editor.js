(function () {
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Def = AGN.Lib.WM.Definitions;

  class ImportNodeEditor extends AGN.Lib.WM.RecipientTransferEditor  {
    constructor(conf, submitWorkflowForm) {
      super(conf);
      this.submitWorkflowForm = submitWorkflowForm;
    }

    get formName() {
      return 'importForm';
    }
    
    get title() {
      return t('auto_import');
    }

    get $editor() {
      return $('#import-editor');
    }

    get missingPermissionMsgCode() { // used in abstract class
      return 'error.workflow.autoImportPermission';
    }

    get dependencyType() { // used in abstract class
      return Def.DEPENDENCY_TYPE_AUTO_IMPORT;
    }
    
    get inUseMsgCode() { // used in abstract class
      return 'error.workflow.autoImportInUse'
    }
    
    get notSelectedMsgCode() { // used in abstract class
      return 'error.workflow.noImport';
    }

    createNewAutoImport() {
      EditorsHelper.processForward(Def.constants.forwards.AUTO_IMPORT_CREATE.name, '#importexportId', this.submitWorkflowForm);
    }

    editAutoImport() {
      $('#forwardTargetItemId').val(this.selectedId);
      EditorsHelper.processForward(Def.constants.forwards.AUTO_IMPORT_EDIT.name, 'form[name="' + this.formName + '"] select[name=importexportId]', this.submitWorkflowForm);
    }

    addLinks() {
      if (this.selectedId > 0) {
        $('#import-editor #editAutoImportLink').show();
        $('#import-editor #createAutoImportLink').hide();
      } else {
        $('#import-editor #createAutoImportLink').show();
        $('#import-editor #editAutoImportLink').hide();
      }
    }
  }

  AGN.Lib.WM.ImportNodeEditor = ImportNodeEditor;
})();
