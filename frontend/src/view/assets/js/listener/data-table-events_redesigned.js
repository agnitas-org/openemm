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

  $(document).on('change', ".js-data-table [name='numberOfRows']", function(e) {
    var $e = $(this);
    var pageSize = $e.val();
    var api = Table.get($e).api;

    api.paginationSetPageSize(pageSize);

    var bundle = $e.closest('.js-data-table-body').data('web-storage');
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

  function bulkDelete($e) {
    const field = $e.data('bulk-field') || 'id';
    const requestField = $e.data('bulk-request-field') || 'bulkIds';

    const api = Table.get($e).api;
    const rows = api.getSelectedRows();
    const ids = rows.map(row => row[field]);

    const data = {};
    data[requestField] = ids;

    requestBulkDelete(ids, $e.data('bulk-url'), data)
      .done(() => api.applyTransaction({remove: rows}));
  }

  function requestBulkDelete(ids, url, data) {
    const deferred = $.Deferred();

    if (ids && ids.length) {
      const jqxhr = $.ajax(url, {
        method: 'GET',
        traditional: true,
        data: data
      }).fail(() =>{
        deferred.reject();
      });

      AGN.Lib.Confirm.request(jqxhr)
        .then(deferred.resolve, deferred.reject)
    } else {
      deferred.reject();
      AGN.Lib.Messages.alert('messages.error.nothing_selected');
    }

    return deferred.promise();
  }
})();
