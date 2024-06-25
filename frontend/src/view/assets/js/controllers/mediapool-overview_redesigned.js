AGN.Lib.Controller.new('mediapool-overview', function () {

  this.addDomInitializer('mediapool-edit', function () {
    $('[id$="-editor-modal"]').on('modal:close', () => AGN.Lib.Messages.warn('defaults.changesNotSaved'));
  });

  this.addAction({click: 'change-category-bulk'}, function() {
    const bulkIds = [...$('input[name="bulkIds"]:checked')].map(checkbox => checkbox.value);
    if (!bulkIds?.length) {
      AGN.Lib.Messages.alert('messages.error.nothing_selected');
      return;
    }
    AGN.Lib.Modal.fromTemplate('category-select-modal', {bulkIds});
  });

});
