(() => {

  const Action = AGN.Lib.Action;
  const TableColumnManager = AGN.Lib.TableColumnManager;

  Action.new({click: '[data-manage-table-columns]'}, function () {
    const manager = TableColumnManager.get(this.el);

    if (manager.isInEditMode()) {
      manager.applyChanges();
    } else {
      manager.toEditMode();
    }
  });

  Action.new({click: '[data-remove-table-column]'}, function () {
    TableColumnManager.get(this.el).removeColumn(this.el.closest('th'));
  });

  $(window).on("displayTypeChanged", (e, isMobileView) => {
    if (isMobileView) {
      TableColumnManager.get($('[data-manage-table-columns]'))?.discardChanges();
    }
  });
})();
