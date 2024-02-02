(() => {

    AGN.Lib.Action.new({'change': '[data-preview-table]'}, function() {
        AGN.Lib.PreviewTable.toggle(this.el);
    });

})();
