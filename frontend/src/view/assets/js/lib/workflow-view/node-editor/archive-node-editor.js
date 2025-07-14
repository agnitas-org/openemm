(() => {

  const Def = AGN.Lib.WM.Definitions;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const NodeTitleHelper = AGN.Lib.WM.NodeTitleHelper;

  class ArchiveNodeEditor extends AGN.Lib.WM.NodeEditor {
    constructor(submitWorkflowForm) {
      super();
      this.safeToSave = true;
      this._submitWorkflowForm = submitWorkflowForm;
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
      const forwardParams = this.#getCreateArchiveForwardParams();
      AGN.Lib.Confirm.request($.get(
        AGN.url(Def.constants.forwards.ARCHIVE_CREATE.url),
        {workflowId: Def.workflowId, workflowForwardParams: forwardParams}
      )).done(({ archiveId, archiveName }) => {
        this.#addArchiveOption(archiveId, archiveName);
        this.archiveSelect.selectOption(archiveId);
        EditorsHelper.saveCurrentEditorWithUndo();

        if (Def.workflowId === 0) {
          _.defer(() => {
            this._submitWorkflowForm(false, {forwardParams: forwardParams, forwardTargetItemId: archiveId});
          });
        } else {
          AGN.Lib.Messages.defaultSaved();
        }
      });
    }

    #getCreateArchiveForwardParams() {
      const params = {
        nodeId: EditorsHelper.curEditingNode.getId()
      }

      return Object.entries(params)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join(';')
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
