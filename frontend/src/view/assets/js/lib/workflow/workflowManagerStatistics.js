(function() {
    var WorkflowManagerStatistics = function(campaignManager) {

        var statisticsVisible = false;

        this.showStatistics = function(workflowId) {
            jQuery.ajax({
                type: "POST",
                url: AGN.url("/workflow/loadStatistics.action"),
                data: {
                    workflowId: workflowId
                },
                success: function(data) {
                    for(var nodeId in data) {
                        campaignManager.getCMNodes().getNodes()[nodeId].statisticsList = data[nodeId];
                    }
                    campaignManager.relayout();
                    window.status = 'wmLoadFinished';
                }
            });
        };

        this.hideStatistics = function() {
            var nodes = campaignManager.getCMNodes().getNodes();
            for(var i in nodes) {
                var node = nodes[i];
                if (node.statisticsList != undefined) {
                    delete node.statisticsList;
                }
            }
            campaignManager.relayout();
        };

        this.toggleStatistics = function(workflowId) {
            if (this.statisticsVisible) {
                this.hideStatistics();
            } else {
                this.showStatistics(workflowId);
            }
            this.statisticsVisible = !this.statisticsVisible;
        };

    };

    AGN.Lib.WM.WorkflowManagerStatistics = WorkflowManagerStatistics;
})();
