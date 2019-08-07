AGN.Lib.Controller.new('mailinglist-list', function() {
  var config = null;

  function getTableApi() {
    var $table = $('.js-data-table-body');

    if ($table.exists()) {
      var table = $table.data('_table');
      if (table && table.api) {
        return table.api;
      }
    }

    return null;
  }

  function requestBulkDelete(ids) {
    var deferred = $.Deferred();

    if (ids && ids.length) {
      var jqxhr = $.ajax(config.urls.MAILINGLIST_BULK_DELETE, {
        method: 'POST',
        traditional: true,
        data: {
          bulkIds: ids
        }
      }).fail(function() {
        deferred.reject();
      });

      AGN.Lib.Confirm.request(jqxhr).then(deferred.resolve, deferred.reject);
    } else {
      deferred.reject();
      AGN.Lib.Messages(t("Error"), t("messages.error.nothing_selected"), "alert");
    }

    return deferred.promise();
  }

  this.addDomInitializer('mailinglist-list', function() {
    config = this.config;
  });

  this.addAction({
    'click': 'bulk-delete'
  }, function() {
    var api = getTableApi();
    if (api) {
      var rows = api.getSelectedRows();
      var ids = rows.map(function(row) { return row.id; });

      requestBulkDelete(ids).done(function() {
        api.updateRowData({remove: rows});
      });
    }
  });

});
