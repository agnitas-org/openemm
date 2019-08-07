(function() {
  var CampaignManagerContextMenu = {

    nodeSelector: "",

    init: function(params) {
      this.nodeSelector = params.nodeSelector;

      var campaignManager = params.campaignManager;
      var campaignManagerSelection = params.campaignManagerSelection;

      var instance = this;

      // context menu for icon nodes
      jQuery.contextMenu({
        selector: instance.nodeSelector,
        build: function($trigger, e) {
          // if the current right-clicked element is not
          // selected - clear the selection and select that element
          var clickedId = e.currentTarget.id;
          if (!campaignManagerSelection.isSelected(clickedId)) {
            campaignManagerSelection.clear();
            campaignManagerSelection.select(jQuery("#" + clickedId));
          }

          var menuItems = {};

          // if we have at least two elements selected - show Connect menu item
          if (campaignManagerSelection.selected.length > 1) {
            menuItems['connect'] = {
              name: t('workflow.connect'),
              icon: "connect",
              callback: function(key, options) {
                if (campaignManagerSelection.selected.length == 2) {
                  campaignManager.connectIntermediateNodes(campaignManagerSelection.selected);
                } else {
                  var prev = null;
                  jQuery.each(campaignManagerSelection.selected, function(index, current) {
                    if (prev != null) {
                      campaignManager.connectNodes(prev, current, true);
                    }
                    prev = current;
                  });
                }
              }
            };

            var connections = campaignManager.getConnectionsFromSelected(campaignManagerSelection.selected);
            if (connections.length > 0) {
              menuItems['disconnect'] = {
                name: t('workflow.disconnect'),
                icon: "disconnect",
                callback: function(key, options) {
                  campaignManager.deleteConnections(connections);
                }
              };
            }
          }

          // if we have only one element selected - show edit menu item
          if (campaignManagerSelection.selected.length == 1) {
            var node = campaignManager.getNodesFromSelected(campaignManagerSelection.selected)[0];
            menuItems['edit'] = {
              name: t('workflow.defaults.edit'),
              icon: "edit",
              disabled: (node.data.editable === false),
              callback: function(key, options) {
                campaignManager.editNode(undefined, true);
              }
            };
          }

          // delete menu item is shown in any case
          menuItems['delete'] = {
            name: t('workflow.defaults.delete'),
            icon: "delete",
            callback: function(key, options) {
              campaignManager.deleteSelectedNode();
            }
          };

          // add comment
          menuItems['iconComment'] = {
            name: t('workflow.defaults.comment'),
            icon: "comment",
            callback: function(key, options) {
              campaignManager.getCommentControls().showCommentEditDialog();
            }
          };

          return {
            callback: function(key, options) {
            },
            items: menuItems
          }
        }
      });

      // context menu for connectors
      jQuery.contextMenu({
        selector: "._jsPlumb_connector",
        build: function($trigger, e) {
          var connectionMenuItems = {};

          if (campaignManagerSelection.selected.length > 1) {
            var connections = campaignManager.getConnectionsFromSelected(campaignManagerSelection.selected);
            if (connections.length > 0) {
              connectionMenuItems['disconnect'] = {
                name: t('workflow.disconnect'),
                icon: "disconnect",
                callback: function(key, options) {
                  campaignManager.deleteConnections(connections);
                }
              };
            }
          }

          connectionMenuItems['delete'] = {
            name: t('workflow.defaults.delete'),
            icon: "delete",
            callback: function(key, options) {
              campaignManager.deleteConnectionByHtmlElement(options.$trigger[0]);
            }
          };

          return {
            callback: function(key, options) {
            },
            items: connectionMenuItems
          };
        }
      });
    }
  };

  AGN.Lib.WM.CampaignManagerContextMenu = CampaignManagerContextMenu;
})();