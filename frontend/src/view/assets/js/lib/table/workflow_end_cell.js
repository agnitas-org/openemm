// TODO: remove (if not necessary) after EMMGUI-714 will be tested and old design will be removed
(function(){

    var WorkflowEndCellRenderer = function () {};

    // gets called once before the renderer is used
    WorkflowEndCellRenderer.prototype.init = function(params) {
        this.eGui = AGN.Lib.TableCellWrapper(params.data.show);
        var innerHtml = "";

        if (params.value.endTypeId === 1) {
            innerHtml = t('workflow.stop.automatic_end');
        } else if(params.value.endTypeId === 2) {
            innerHtml = params.value.date;
        }

        this.eGui.innerHTML = innerHtml;
    };

    // gets called once when grid ready to insert the element
    WorkflowEndCellRenderer.prototype.getGui = function() {
        return this.eGui;
    };

    // gets called whenever the user gets the cell to refresh
    WorkflowEndCellRenderer.prototype.refresh = function(params) {
        return false;
    };

    // gets called when the cell is removed from the grid
    WorkflowEndCellRenderer.prototype.destroy = function() {
        // do cleanup, remove event listener from button
    };

    AGN.Opt.TableCellRenderers['WorkflowEndCellRenderer'] = WorkflowEndCellRenderer;

})();
