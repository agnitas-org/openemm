(function(){

    var WorkflowStartCellRenderer = function () {};

    // gets called once before the renderer is used
    WorkflowStartCellRenderer.prototype.init = function(params) {
        this.eGui = document.createElement('div');
        var innerHtml = "";

        if (params.value.startTypeId === 1) {
            innerHtml = " <span class=\"badge badge-campaigntype-actionbased\">\n" +
                "              <i class=\"icon icon-gear\"></i>\n" +
                "              <strong>" + t('workflow.start.action_based') + "</strong>\n" +
                "         </span>";
        } else if(params.value.startTypeId === 2) {
            innerHtml = "<span class=\"badge badge-campaigntype-datebased\">\n" +
                "              <i class=\"icon icon-calendar-o\"></i>\n" +
                "              <strong>" + t('workflow.start.date_based') + "</strong>\n" +
                "        </span>";
        } else {
            innerHtml = moment(params.value.dateLong).format(params.adminDateTimeFormat);
        }

        this.eGui.innerHTML = innerHtml;
    };

    // gets called once when grid ready to insert the element
    WorkflowStartCellRenderer.prototype.getGui = function() {
        return this.eGui;
    };

    // gets called whenever the user gets the cell to refresh
    WorkflowStartCellRenderer.prototype.refresh = function(params) {
        return false;
    };

    // gets called when the cell is removed from the grid
    WorkflowStartCellRenderer.prototype.destroy = function() {
        // do cleanup, remove event listener from button
    };

    AGN.Opt.TableCellRenderers['WorkflowStartCellRenderer'] = WorkflowStartCellRenderer;

})();
