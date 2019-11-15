AGN.Lib.Controller.new('workflow-list', function() {
    var config = null;

    function getTableApi() {
        var $table = $('.js-data-table-body');

        if ($table.exists()) {
            var table = $table.data('_table');
            if (table && table.api) {
                return table.api;
            }
        }

        return null;
    }

    this.addDomInitializer('workflow-list', function() {
        config = this.config;
    });

    function requestBulk(ids, url) {
        var deferred = $.Deferred();

        if (ids && ids.length) {
            var jqxhr = $.ajax(url, {
                method: 'POST',
                traditional: true,
                data: {
                    bulkIds: ids
                }
            }).fail(function() {
                deferred.reject();
            });

            AGN.Lib.Confirm.request(jqxhr).then(deferred.resolve, deferred.reject);
        } else {
            deferred.reject();
            AGN.Lib.Messages(t("Error"), t("messages.error.nothing_selected"), "alert");
        }

        return deferred.promise();
    }

    this.addAction({
        'click': 'bulk-delete'
    }, function() {
        var api = getTableApi();
        if (api) {
            var rows = api.getSelectedRows();
            var ids = rows.map(function (row) {
                return row.id;
            });

            requestBulk(ids, config.urls.WORKFLOW_BULK_DELETE).done(function () {
                api.updateRowData({remove: rows});
            })
        }
    });

    this.addAction({
        'click': 'bulk-deactivate'
    }, function() {
        var api = getTableApi();
        if (api) {
            var rows = api.getSelectedRows();
            var rowNodes = api.getSelectedNodes();
            var ids = rows.map(function (row) {
                return row.id;
            });

            requestBulk(ids, config.urls.WORKFLOW_BULK_DEACTIVATE).done(function () {
                rowNodes.forEach(function (row) {
                    row.setDataValue('status', {name: "inactive", messageKey: "default.status.inActive"});
                });
                api.refreshClientSideRowModel('filter');
                api.deselectAll();
            })
        }
    });
});
