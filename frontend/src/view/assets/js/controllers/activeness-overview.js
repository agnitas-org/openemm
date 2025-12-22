AGN.Lib.Controller.new('activeness-overview', function () {

  const Table = AGN.Lib.Table;

  let activationUrl;

  this.addDomInitializer("activeness-overview", function () {
    activationUrl = this.config.url;
  });

  this.addAction({click: 'deactivate-js'}, function () {
    changeActivenessJs([getItemId(this.el)], false, this.el)
  });

  this.addAction({click: 'activate-js'}, function () {
    changeActivenessJs([getItemId(this.el)], true, this.el)
  });

  this.addAction({click: 'bulk-deactivate-js'}, function () {
    changeActivenessJs(getBulkIds(true), false, this.el)
  });

  this.addAction({click: 'bulk-activate-js'}, function () {
    changeActivenessJs(getBulkIds(true), true, this.el)
  });

  function changeActivenessJs(ids, activate, $el) {
    if (!checkSelectedIds(ids)) {
      return;
    }

    toggleBtnUsability($el);

    $.ajax(activationUrl, {
      method: 'POST',
      traditional: true,
      data: {ids, activate, fromOverview: true}
    }).done(resp => {
      if (resp.success) {
        const table = Table.get($el);
        const rowsData = resp.data.map(item => {
          const foundFieldsToUpdate = typeof item === 'object';
          const id = foundFieldsToUpdate ? item.id : item;
          const rowData = findRowData(table, id);

          if (foundFieldsToUpdate) {
            return _.merge(rowData, item);
          }

          return _.merge(rowData, {active: String(activate)});
        });

        table.api.applyTransaction({update: rowsData});
      }

      AGN.Lib.JsonMessages(resp.popups);
    }).always(() => toggleBtnUsability($el));
  }

  function findRowData(table, id) {
    const rows = [];

    table.api.forEachNodeAfterFilterAndSort(node => {
      if (id === node.data.id) {
        rows.push(node.data);
      }
    });

    return rows[0];
  }

  this.addAction({click: 'activate'}, function () {
    changeSingleActiveState(this.el, true);
  });

  this.addAction({click: 'deactivate'}, function () {
    changeSingleActiveState(this.el, false);
  });

  this.addAction({click: 'bulk-activate'}, function () {
    changeActiveness(getBulkIds(), true);
  });

  this.addAction({click: 'bulk-deactivate'}, function () {
    changeActiveness(getBulkIds(), false);
  });

  function changeSingleActiveState($el, active = true) {
    changeActiveness([getItemId($el)], active);
  }

  function changeActiveness(ids, activate) {
    if (!checkSelectedIds(ids)) {
      return;
    }

    const scrollTop = $('.table-wrapper__body').scrollTop();

    $.ajax(activationUrl, {
      method: 'POST',
      traditional: true,
      data: {ids, activate, fromOverview: true}
    }).done(resp => {
      AGN.Lib.Form.get($('#table-tile')).updateHtml(resp);
      scrollTableTo(scrollTop);
    });
  }

  function checkSelectedIds(ids) {
    if (ids.length > 0) {
      return true;
    }

    AGN.Lib.Messages.alert('messages.error.nothing_selected');
    return false;
  }

  function getItemId($el) {
    if ($el.closest('table').exists()) {
      return $el.closest('tr').find('[data-bulk-checkbox]').val();
    }

    return $el.data('item-id');
  }

  function getBulkIds(agGridTable = false) {
    if (agGridTable) {
      return Table.get($('.table-wrapper')).api.getSelectedRows().map(row => row.id);
    }

    return $(`[data-bulk-checkbox]:checked`).map(function () {
      return $(this).val();
    }).get();
  }

  function toggleBtnUsability($btn) {
    if ($btn.is('a')) {
      $btn.toggleClass('disabled');
      $btn.toggleClass('pe-none');
    } else {
      $btn.prop('disabled', !$btn.prop('disabled'));
    }
  }

  function scrollTableTo(value) {
    $('.table-wrapper__body').scrollTop(value);
  }

});
