(() => {

    AGN.Lib.Action.new({'change': '[data-preview-table]'}, function() {
        AGN.Lib.PreviewTable.toggle(this.el);
        AGN.Lib.CoreInitializer.run(['table-row-actions', 'scrollable', 'truncated-text-popover'], this.el.closest('.tile'));
    });

})();
