(function(){

  var Table = AGN.Lib.Table,
      WebStorage = AGN.Lib.WebStorage;

  $(document).on('click', '.js-data-table-first-page', function(e) {
    Table.get($(this)).api.paginationGoToFirstPage();
  });

  $(document).on('click', '.js-data-table-prev-page', function(e) {
    Table.get($(this)).api.paginationGoToPreviousPage();
  });

  $(document).on('click', '.js-data-table-page', function(e) {
    Table.get($(this)).api.paginationGoToPage($(this).data('page'));
  });

  $(document).on('click', '.js-data-table-next-page', function(e) {
    Table.get($(this)).api.paginationGoToNextPage();
  });

  $(document).on('click', '.js-data-table-last-page', function(e) {
    Table.get($(this)).api.paginationGoToLastPage();
  });

  $(document).on('click', '.js-data-table-paginate', function(e) {
    var $e = $(this);
    var pageSize = $e.data('page-size');
    var api = Table.get($e).api;

    api.paginationSetPageSize(pageSize);

    // Close drop down menu as user clicked on button within.
    $e.closest('.dropdown-menu').trigger('click');

    var bundle = $e.data('web-storage');
    if (bundle) {
      WebStorage.extend(bundle, {"paginationPageSize": parseInt(pageSize)});
    }
  });

  $(window).on('viewportChanged', function(e) {
    $(document).all('.js-data-table-body').each(function() {
      AGN.Lib.Table.get($(this)).redraw();
    })
  });

  $(document).on('click', '.js-data-table-bulk-delete', function(e) {
    var $e = $(this);
    bulkDelete($e, false);
  });

  $(document).on('click', '.js-data-table-bulk-delete-struts', function(e) {
    var $e = $(this);
    bulkDelete($e, true);
  });

  function bulkDelete($e, struts) {
    var field = $e.data('bulk-field') || 'id';
    var requestField = $e.data('bulk-request-field') || (struts ? 'bulkID' : 'bulkIds');

    var api = Table.get($e).api;
    var rows = api.getSelectedRows();
    var ids = rows.map(function(row) { return row[field]; });

    var data = {};
    if (struts) {
      data[requestField] = {};
      for (var index in ids) {
        var id = ids[index];
        data[requestField][id] = 'on';
      }
    } else {
      data[requestField] = ids;
    }

    requestBulkDelete(ids, $e.data('bulk-url'), data).done(function() {
      api.updateRowData({remove: rows});
    });
  }

  function requestBulkDelete(ids, url, data) {
    var deferred = $.Deferred();

    if (ids && ids.length) {
      var jqxhr = $.ajax(url, {
        method: 'POST',
        traditional: false,
        data: data
      }).fail(function(){
        deferred.reject();
      });

      AGN.Lib.Confirm.request(jqxhr)
        .then(deferred.resolve, deferred.reject)
    } else {
      deferred.reject();
      AGN.Lib.Messages(t("Error"), t("messages.error.nothing_selected"), "alert");
    }

    return deferred.promise();
  }
})();
