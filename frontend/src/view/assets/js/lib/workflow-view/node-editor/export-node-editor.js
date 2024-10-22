(function () {
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Def = AGN.Lib.WM.Definitions;

  class ExportNodeEditor extends AGN.Lib.WM.RecipientTransferEditor {
    constructor(conf, submitWorkflowForm) {
      super(conf);
      this.submitWorkflowForm = submitWorkflowForm;
    }

    get formName() {
      return 'exportForm';
    }
    
    get title() {
      return t('auto_export');
    }

    get $editor() {
      return $('#export-editor');
    }
    
    get missingPermissionMsgCode() { // used in abstract class
      return 'error.workflow.autoExportPermission';
    }

    get dependencyType() { // used in abstract class
      return Def.DEPENDENCY_TYPE_AUTO_EXPORT;
    }

    get inUseMsgCode() { // used in abstract class
      return 'error.workflow.autoExportInUse'
    }

    get notSelectedMsgCode() { // used in abstract class
      return 'error.workflow.noExport';
    }

    createNewAutoExport() {
      EditorsHelper.processForward(Def.constants.forwards.AUTO_EXPORT_CREATE.name, 'form[name="' + this.formName + '"] select[name=importexportId]', this.submitWorkflowForm);
    }

    editAutoExport() {
      $('#forwardTargetItemId').val(this.selectedId);
      EditorsHelper.processForward(Def.constants.forwards.AUTO_EXPORT_EDIT.name, 'form[name="' + this.formName + '"] select[name=importexportId]', this.submitWorkflowForm);
    }

    addLinks() {
      if (this.selectedId > 0) {
        $('#export-editor #editAutoExportLink').show();
        $('#export-editor #createAutoExportLink').hide();
      } else {
        $('#export-editor #createAutoExportLink').show();
        $('#export-editor #editAutoExportLink').hide();
      }
    }
  }

  AGN.Lib.WM.ExportNodeEditor = ExportNodeEditor;
})();
