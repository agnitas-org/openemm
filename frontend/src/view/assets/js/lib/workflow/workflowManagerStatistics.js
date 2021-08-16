(function() {
    var WorkflowManagerStatistics = function(campaignManager) {

        this.statisticsVisible = false;

        this.showStatistics = function(workflowId) {
            $.post(AGN.url("/workflow/loadStatistics.action"), {workflowId: workflowId})
              .done(function(data) {
                for(var nodeId in data) {
                    campaignManager.getCMNodes().getNodes()[nodeId].statisticsList = data[nodeId];
                }
                campaignManager.relayout();
                window.status = 'wmLoadFinished';
              }).fail(function() {
                  AGN.Lib.Messages(t("Error"), t("defaults.error"), "alert");
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
