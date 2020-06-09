(function($) {
    var IconsSetLoader = function IconsSetLoader(campaignManager) {

        this.loadIconsSetAJAX = function(rootNode, useOriginal) {
            var sample = rootNode.type !== 'ownWorkflow';

            var expandData = {
                x: 0,
                y: 0,
                icons: []
            };

            var self = this;
            var params;
            var isSampleRequest;

            if (sample && !useOriginal) {
                isSampleRequest = true;
                params = {
                    type: rootNode.type
                };
            } else {
                isSampleRequest = false;
                params = {
                    workflowId: rootNode.data.ownWorkflowId,
                    isWithContent: rootNode.data.copyContent
                };
            }

            var campaignManagerNodes = campaignManager.getCMNodes();

            $.ajax({
                type: "POST",
                url: isSampleRequest ? AGN.url('/workflow/getSampleWorkflowContent.action') : AGN.url('/workflow/getWorkflowContent.action'),
                data: params,
                async: false
            }).done(function(data) {
                $.ajax({
                    type: 'POST',
                    url: AGN.url('/workflow/getAllMailingSorted.action'),
                    data: {
                        sortField: 'shortname',
                        sortDirection: 'asc'
                    },
                    async: false
                }).always(function(mailings) {
                    var mailingsMap = {};

                    mailings.forEach(function(mailing) {
                        mailingsMap[mailing.mailingID] = mailing.shortname;
                    });

                    campaignManager.extendAllUsedEntities({allMailings: mailingsMap});

                    expandData.icons = data;
                    rootNode.data.copyContent = true;

                    if (campaignManager.getCampaignManagerNodes().count() > 1) {
                        // if we already have some icons in WM - we need to remove start/end icons
                        // of inserted inner workflow (and connections connecting those icons)
                        self.removeStartAndEndIcons(expandData);
                    }

                    if (!useOriginal) {
                        // Generate new ids to make sure that uniqueness is not violated.
                        self.generateNewIDs(expandData.icons);
                    }
                    self.expand(rootNode, expandData);
                    campaignManagerNodes.openNodeIcon();
                });
            });
        };

        this.generateNewIDs = function(icons) {
            var idsMap = {};

            icons.forEach(function(icon) {
                idsMap[icon.id] = campaignManager.getCMNodes().generateId();
            });

            icons.forEach(function(icon) {
                icon.id = idsMap[icon.id];
                if (icon.connections) {
                    icon.connections = icon.connections.map(function(connection) {
                        return {targetIconId: idsMap[connection.targetIconId]};
                    });
                } else {
                    icon.connections = [];
                }
            });
        };

        this.removeStartAndEndIcons = function(expandData) {
            var icons = expandData.icons;
            var removedIds = {};

            icons.forEach(function(icon) {
                if (icon.type === 'start' || icon.type === 'stop') {
                    removedIds[icon.id] = true;
                }
            });

            icons = icons.filter(function(icon) {
                if (removedIds[icon.id]) {
                    // Exclude removed icons.
                    return false;
                } else {
                    if (icon.connections) {
                        icon.connections = icon.connections.filter(function(connection) {
                            // Exclude connections to removed icons.
                            return !removedIds[connection.targetIconId];
                        });
                    } else {
                        icon.connections = [];
                    }

                    // Keep this icon.
                    return true;
                }
            });

            var minX = 100000;
            var minY = 100000;

            // Find minX and minY to shift icons after deleting.
            icons.forEach(function(icon) {
                minX = Math.min(minX, icon.x);
                minY = Math.min(minY, icon.y);
            });

            // Shift icons.
            if (minX > 0 || minY > 0) {
                icons.forEach(function(icon) {
                    icon.x -= minX;
                    icon.y -= minY;
                });
            }

            expandData.icons = icons;
        };

        this.expand = function(rootNode, expandData) {
            var icons = expandData.icons;

            var previousState = campaignManager.getCurrentState();
            campaignManager.setCurrentState(campaignManager.STATE_EXPANDING_ICONS_SET);

            var prevNeedSaveSnapshot = campaignManager.needSaveSnapshot;
            campaignManager.needSaveSnapshot = false;

            // delete current workflow node
            expandData.x = rootNode.x;
            expandData.y = rootNode.y;

            campaignManager.deleteNode(rootNode.elementJQ.attr('id'), false, false);

            icons.forEach(function(icon) {
                icon.x += rootNode.x;
                icon.y += rootNode.y;
            });

            // as we expand the workflow node - we need to shift editor icons in order the workflow content to fit
            shiftNodes(expandData);

            // add all workflow nodes and connections to the editor
            campaignManager.restoreWorkflow(icons);

            campaignManager.callWorkflowManagerStateChangedCallback();
            campaignManager.needSaveSnapshot = prevNeedSaveSnapshot;

            icons.forEach(function(icon) {
                icon.x -= rootNode.x;
                icon.y -= rootNode.y;
            });

            campaignManager.setCurrentState(previousState);
        };

        var shiftNodes = function(expandData) {
            var maxXY = getMaxXY(expandData.icons);
            var campaignManagerNodes = campaignManager.getCMNodes();
            var nodes = campaignManagerNodes.getNodesFromRegion(
                expandData.x,
                expandData.y,
                expandData.x + maxXY.maxX,
                expandData.y + maxXY.maxY, 1);

            if (!$.isEmptyObject(nodes)) {
                for(var index in nodes) {
                    if (nodes.hasOwnProperty(index)) {
                        var node = nodes[index];

                        var shiftX = 0;
                        var shiftY = 0;

                        //shift node to top
                        if (node.y < expandData.y) {
                            var delta = expandData.y - node.y - 3;
                            shiftY = (Math.abs(delta) === 0 ? -1 : delta);
                        } else {
                            //shift node to right
                            shiftX = maxXY.maxX - expandData.x;
                            shiftY = maxXY.maxY - expandData.y;
                        }

                        node.x = node.x + shiftX;
                        node.y = node.y + shiftY;
                    }
                }
            }
        };

        var getMaxXY = function(icons) {
            var maxX = 0;
            var maxY = 0;

            icons.forEach(function(icon) {
                if (icon.x > maxX) {
                    maxX = icon.x;
                }
                if (icon.y > maxY) {
                    maxY = icon.y;
                }
            });

            return {maxX: maxX, maxY: maxY};
        };

    };

    AGN.Lib.WM.IconsSetLoader = IconsSetLoader;
})(jQuery);
