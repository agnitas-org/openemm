(function(){

    var WorkflowReactionCellRenderer = function () {};

    // gets called once before the renderer is used
    WorkflowReactionCellRenderer.prototype.init = function(params) {
        this.eGui = document.createElement('div');

        if (params.value) {
            var innerHtml = "  <i class=\"" + params.value.iconClass + "></i>\n" +
                "              <strong>" + t('workflow.reaction' + params.value.name) + "</strong>";
            this.eGui.innerHTML = innerHtml;
        }
    };

    // gets called once when grid ready to insert the element
    WorkflowReactionCellRenderer.prototype.getGui = function() {
        return this.eGui;
    };

    // gets called whenever the user gets the cell to refresh
    WorkflowReactionCellRenderer.prototype.refresh = function(params) {
        return false;
    };

    // gets called when the cell is removed from the grid
    WorkflowReactionCellRenderer.prototype.destroy = function() {
        // do cleanup, remove event listener from button
    };

    AGN.Opt.TableCellRenderers['WorkflowReactionCellRenderer'] = WorkflowReactionCellRenderer;

})();
