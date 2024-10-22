(() => {

  const EditableView = AGN.Lib.EditableView;
  const Action = AGN.Lib.Action;

  Action.new({click: '[data-edit-view]'}, function () {
    const $editableView = $(`[data-editable-view="${(this.el.data('edit-view'))}"]`);
    const editableView = EditableView.get($editableView);

    if (editableView.isInEditMode()) {
      editableView.applyChanges();
    } else {
      editableView.enableEditMode();
    }
  });

  Action.new({click: '[data-edit-tile-visibility]'}, function () {
    EditableView.get(this.el).toggleState(this.el);
  });

})();
