AGN.Lib.Controller.new('emm-activeness', function () {

  let isRequestInProgress = false;
  let saveUrl;

  this.addDomInitializer('emm-activeness', function() {
    saveUrl = this.config.urls.SAVE;
  });

  this.addAction({
    change: 'toggle-active'
  }, function () {
    const $el = $(this.el);
    const table = AGN.Lib.Table.get($el);

    const itemId = $el.data('item-id');
    const isActive = $el.is(':checked');

    if (isRequestInProgress) {
      $el.prop('checked', !isActive);
      return;
    }

    isRequestInProgress = true;

    $.ajax(AGN.url(saveUrl), {
      method: 'POST',
      traditional: false,
      data: {'activeness': { [itemId]: isActive }}
    }).done(resp => {
      if (resp && resp.success === true) {
        applyState(table, itemId, isActive);
      }
      AGN.Lib.JsonMessages(resp.popups);
    }).fail(() => {
      applyState(table, itemId, !isActive);
      $el.prop('checked', !isActive);
      AGN.Lib.Messages.defaultError();
    }).always(() => isRequestInProgress = false);
  });

  function applyState(table, itemId, active) {
    const data = findRowData(table, itemId);
    data.activeStatus = active ? 'ACTIVE' : 'INACTIVE';

    table.api.applyTransaction({update: [data]});
  }

  function findRowData(table, itemId) {
    const rows = [];

    table.api.forEachNodeAfterFilterAndSort(function (rowNode, index) {
      const id = rowNode.data.id;
      if (id === itemId) {
        rows.push(rowNode.data);
      }
    });

    return rows[0];
  }
});
