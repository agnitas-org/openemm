(() => {

  const Table = AGN.Lib.Table;
  const WebStorage = AGN.Lib.WebStorage;
  const Confirm = AGN.Lib.Confirm;

  const RESTORE_ROW_SELECTOR = '[data-restore-row]';
  const BULK_RESTORE_ROWS_SELECTOR = '[data-bulk-action="restore"]';

  $(document).on('click', '[data-js-table-page]', function () {
    const $el = $(this);
    const page = $el.data('js-table-page');

    const api = Table.get($el).api;

    if (page === 'first') {
      api.paginationGoToFirstPage();
    } else if (page === 'prev') {
      api.paginationGoToPreviousPage();
    } else if (page === 'next') {
      api.paginationGoToNextPage();
    } else if (page === 'last') {
      api.paginationGoToLastPage();
    } else {
      api.paginationGoToPage(page);
    }

    api.deselectAll();
  });

  $(document).on('change', "[data-js-table] [data-number-of-rows]", function () {
    const $e = $(this);
    const pageSize = $e.val();

    Table.get($e).api.setGridOption('paginationPageSize', pageSize);

    const bundle = $e.closest('.table-wrapper').data('web-storage');
    if (bundle) {
      WebStorage.extend(bundle, {"paginationPageSize": parseInt(pageSize)});
    }
  });

  $(window).on('viewportChanged', function () {
    $(document).all('.table-wrapper').each(function () {
      AGN.Lib.Table.get($(this))?.redraw();
    })
  });

  $(document).on('click', '.js-data-table-delete', function (e) {
    const $el = $(this);
    const table = Table.get($el);

    $.get($el.attr('href')).done(resp => {
      if ($(resp).all('.modal').exists()) {
        Confirm.create(resp).done(positiveResp => {
          const row = table?.findRowByElement($el);
          if (row) {
            row.data.deleted = true;
            row.data.active = 'false';
            table.api?.applyTransaction(isRestoreModeAvailable() ? {update: [row.data]} : {remove: [row.data]});
          }
          if (typeof positiveResp === 'object') {
            AGN.Lib.JsonMessages(positiveResp.popups, true);
          } else {
            AGN.Lib.RenderMessages($(positiveResp));
          }
        });
      } else {
        AGN.Lib.Page.render(resp);
      }
    });

    e.preventDefault();
  });

  $(document).on('click', '.js-data-table-bulk-delete', e => deleteRows($(e.currentTarget)));
  $(document).on('click', `${BULK_RESTORE_ROWS_SELECTOR}, ${RESTORE_ROW_SELECTOR}`, e => restoreRows($(e.currentTarget)));

  function deleteRows($el) {
    hideRowsAfterAction($el, false);
  }

  function restoreRows($el) {
    hideRowsAfterAction($el, true);
  }

  function hideRowsAfterAction($el, restore) {
    const table = Table.get($el);
    const tableApi = table.api;
    const rows = $el.is(RESTORE_ROW_SELECTOR) ? [table?.findRowByElement($el)?.data] : tableApi.getSelectedRows();
    const ids = rows.map(row => row.id);

    requestAction(ids, $el.data('bulk-url'), restore ? 'POST' : $el.data('method'))
      .done(() => removeRows(tableApi, rows, restore));
  }

  function removeRows(tableApi, rows, restore) {
    rows.forEach(row => {
      row.active = 'false';
      row.deleted = !restore;
    });
    tableApi.applyTransaction(isRestoreModeAvailable() ? {update: rows} : {remove: rows});
  }

  function isRestoreModeAvailable() {
    return $(BULK_RESTORE_ROWS_SELECTOR).exists();
  }

  function requestAction(bulkIds, url, method = 'GET') {
    const deferred = $.Deferred();

    if (bulkIds && bulkIds.length) {
      const jqxhr = $.ajax(url, {
        method,
        traditional: true,
        data: {bulkIds}
      }).fail(() => deferred.reject());

      Confirm.request(jqxhr)
        .then(deferred.resolve, deferred.reject)
    } else {
      deferred.reject();
      AGN.Lib.Messages.alert('messages.error.nothing_selected');
    }

    return deferred.promise();
  }

})();
