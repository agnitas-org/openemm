(function(){

    var WorkflowStatusCellRenderer = function () {};

    // gets called once before the renderer is used
    WorkflowStatusCellRenderer.prototype.init = function(params) {
        this.eGui = AGN.Lib.TableCellWrapper(params.data.show);

        if (params.value) {

            var statusName = params.value.name;
            this.eGui.innerHTML = "<span class=\"status-badge campaign.status." + statusName + "\"></span> " + t('workflow.status.' + statusName);
        }
    };

    // gets called once when grid ready to insert the element
    WorkflowStatusCellRenderer.prototype.getGui = function() {
        return this.eGui;
    };

    // gets called whenever the user gets the cell to refresh
    WorkflowStatusCellRenderer.prototype.refresh = function(params) {
        return false;
    };

    // gets called when the cell is removed from the grid
    WorkflowStatusCellRenderer.prototype.destroy = function() {
        // do cleanup, remove event listener from button
    };

    AGN.Opt.TableCellRenderers['WorkflowStatusCellRenderer'] = WorkflowStatusCellRenderer;

})();
