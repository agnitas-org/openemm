(function () {
  const Def = AGN.Lib.WM.Definitions;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const NodeTitleHelper = AGN.Lib.WM.NodeTitleHelper;

  class ArchiveNodeEditor extends AGN.Lib.WM.NodeEditor {
    constructor() {
      super();
      this.safeToSave = true;
    }

    get formName() {
      return 'archiveNodeForm';
    }
    
    get title() {
      return t('workflow.mailing.archive');
    }
    
    get archiveSelectSelector() {
      return `#${this.formName} #settings_general_campaign`;
    }
    
    get archiveSelect() {
      return AGN.Lib.Select.get(this.$archiveSelect);
    }
    
    get $archiveSelect() {
      return $(this.archiveSelectSelector);
    }

    createNewArchive() {
      AGN.Lib.Confirm.request($.get(
        AGN.url(Def.constants.forwards.ARCHIVE_CREATE.url),
        {workflowId: Def.workflowId}
      )).done(({ archiveId, archiveName }) => {
        this.#addArchiveOption(archiveId, archiveName);
        this.archiveSelect.selectOption(archiveId);
        EditorsHelper.saveCurrentEditorWithUndo();
        AGN.Lib.Messages.defaultSaved();
      });
    }
    
    #addArchiveOption(id, name) {
      this.archiveSelect.addOption(id, name);
      NodeTitleHelper.addArchiveName(id, name);
    }

    fillEditor(node) {
      const data = node.getData();

      this.$form.submit(false);
      this.$form.get(0).reset();

      EditorsHelper.fillFormFromObject(this.formName, data, '');
      this.isValid();
    }

    saveEditor() {
      return EditorsHelper.formToObject(this.formName);
    }
  }

  AGN.Lib.WM.ArchiveNodeEditor = ArchiveNodeEditor;
})();
