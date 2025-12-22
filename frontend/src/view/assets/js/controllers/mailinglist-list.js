AGN.Lib.Controller.new('mailinglist-list', function () {

  this.addAction({click: 'bulk-delete'}, function () {
    const api = getTableApi(this.el);
    if (!api) {
      return;
    }

    const rows = api.getSelectedRows();
    const ids = rows.map(row => row.id);

    requestBulk(ids, AGN.url('/mailinglist/confirmBulkDelete.action'))
      .done(resp => {
        if (typeof resp == 'object') {
          const removedIds = resp.data;
          const removedRows = rows.filter(row => removedIds.includes(row.id));

          api.applyTransaction({remove: removedRows});
          AGN.Lib.JsonMessages(resp.popups, true);
        } else {
          AGN.Lib.RenderMessages($(resp));
        }
      });
  });

  function requestBulk(ids, url) {
    const deferred = $.Deferred();

    if (ids && ids.length) {
      const jqxhr = $.ajax(url, {
        method: 'GET',
        traditional: true,
        data: {
          bulkIds: ids
        }
      }).done(resp => {
        if (resp) {
          AGN.Lib.RenderMessages($(resp));
        }
      }).fail(() => deferred.reject());

      AGN.Lib.Confirm.request(jqxhr).then(deferred.resolve, deferred.reject);
    } else {
      deferred.reject();
      AGN.Lib.Messages.alert('messages.error.nothing_selected');
    }

    return deferred.promise();
  }

  function getTableApi($el) {
    return AGN.Lib.Table.get($el)?.api;
  }

});
