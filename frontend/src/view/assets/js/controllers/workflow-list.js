AGN.Lib.Controller.newExtended('workflow-list', 'activeness-overview', function () {

  this.addAction({click: 'bulk-delete'}, function () {
    const api = AGN.Lib.Table.get(this.el).api;
    const rows = api.getSelectedRows();
    const ids = rows.map(row => row.id);

    if (!ids.length) {
      AGN.Lib.Messages.alert('messages.error.nothing_selected');
      return;
    }

    $.ajax(AGN.url('/workflow/bulkDelete.action'), {
      method: 'GET',
      traditional: true,
      data: {bulkIds: ids}
    })
      .done(confirmResp => {
        AGN.Lib.Confirm.create(confirmResp).done(resp => {
          if (ids.length > 1) {
            const removedIds = resp.data;
            const removedRows = rows.filter(row => removedIds.includes(row.id));

            api.applyTransaction({remove: removedRows});
            AGN.Lib.JsonMessages(resp.popups, true);
          } else {
            api.applyTransaction({remove: rows});
            AGN.Lib.RenderMessages($(resp));
          }
        });
      });
  });

});
