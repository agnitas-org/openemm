(function() {
  var ChainProcessor = function(properties) {
    this.STATE_IDLE = 1;
    this.STATE_PROCESSING_CHAIN = 2;
    this.STATE_TIMER_WAITING = 3;

    var self = this;
    var campaignManager = properties.campaignManager;
    var editorsHelper = properties.editorsHelper;
    var nodeFactory = properties.nodeFactory;
    var state = this.STATE_IDLE;

    var setState = function(newState) {
      state = newState;
    };

    var getIconMap = function(icons) {
      var iconMap = {};  // iconId -> icon

      icons.forEach(function(icon) {
        iconMap[icon.id] = icon;
      });

      return iconMap;
    };

    var getForwardConnectionMap = function(icons) {
      var connectionMap = {};  // source -> {target...}

      icons.forEach(function(icon) {
        if (icon.connections) {
          icon.connections.forEach(function(connection) {
            var sourceId = icon.id;
            var targetId = connection.targetIconId;

            if (!connectionMap[sourceId]) {
              connectionMap[sourceId] = {};
            }

            connectionMap[sourceId][targetId] = true;
          });
        }
      });

      return connectionMap;
    };

    var getBackwardConnectionMap = function(icons) {
      var connectionMap = {};  // target -> {source...}

      icons.forEach(function(icon) {
        if (icon.connections) {
          icon.connections.forEach(function(connection) {
            var sourceId = icon.id;
            var targetId = connection.targetIconId;

            if (!connectionMap[targetId]) {
              connectionMap[targetId] = {};
            }

            connectionMap[targetId][sourceId] = true;
          });
        }
      });

      return connectionMap;
    };

    var calculateDerivedParameterValue = function(decisionIconId) {
      var chains = self.getIncomingChainsForIcon(decisionIconId);
      var visitedIds = {};
      var parameterIcons = [];

      for(var chainIndex = 0; chainIndex < chains.length; chainIndex++) {
        var chain = chains[chainIndex];

        if (chain[1] && chain[1].type == nodeFactory.NODE_TYPE_MAILING) {
          if (chain[2] && chain[2].type == nodeFactory.NODE_TYPE_PARAMETER) {
            var icon;

            if (chain[3] && chain[3].type == nodeFactory.NODE_TYPE_PARAMETER) {
              icon = chain[3];
            } else {
              icon = chain[2];
            }

            if (!visitedIds[icon.id]) {
              visitedIds[icon.id] = true;
              parameterIcons.push(icon);
            }
          }
        }
      }

      var sum = 0;

      if (parameterIcons.length > 0) {
        for(var i = 0; i < parameterIcons.length; i++) {
          var parameterIcon = parameterIcons[i];
          if (parameterIcon.filled) {
            sum += parseInt(parameterIcon.value, 10);
          } else {
            return null;
          }
        }

        return Math.max(0, 100 - sum);
      } else {
        return null;
      }
    };

    /**
     * Call this function if you want update final parameter value for auto-optimization chain.
     */
    this.updateParameterValueAfterDecision = function() {
      if (campaignManager.getCurrentState() == campaignManager.STATE_WAITING) {
        setState(self.STATE_PROCESSING_CHAIN);

        var PREFIX = campaignManager.getCMNodes().getNodeIdPrefix();

        var icons = campaignManager.getIconsForSubmission();
        var iconMap = getIconMap(icons);
        var connectionForwardMap = getForwardConnectionMap(icons);
        var connectionBackwardMap = getBackwardConnectionMap(icons);

        function getNextIcon(iconId, type, isForwardDirection) {
          var connectionMap = isForwardDirection ? connectionForwardMap : connectionBackwardMap;
          var nextIds = Object.keys(connectionMap[iconId] || {});
          var visitedIds = {};

          while (nextIds.length > 0) {
            var ids = nextIds;

            nextIds = [];

            for(var i = 0; i < ids.length; i++) {
              var id = ids[i];
              var icon = iconMap[id];

              if (icon.type === type) {
                return icon;
              }

              visitedIds[id] = true;

              Object.keys(connectionMap[id] || {}).forEach(function(nextId) {
                if (!visitedIds[nextId]) {
                  nextIds.push(nextId);
                }
              });
            }
          }

          return false;
        }

        icons.filter(function(icon) { return icon.type == nodeFactory.NODE_TYPE_DECISION; })
          .forEach(function(decision) {
            if (decision.decisionType == constants.decisionTypeAutoOptimization) {
              var parameterIcon = getNextIcon(decision.id, nodeFactory.NODE_TYPE_PARAMETER, true);
              if (parameterIcon && parameterIcon.editable === false) {
                var parameterNode = campaignManager.getCMNodes().getNodeById(PREFIX + parameterIcon.id);

                var value = calculateDerivedParameterValue(decision.id);

                if (value == null) {
                  parameterNode.data.value = 0;
                  parameterNode.filled = false;
                } else {
                  parameterNode.data.value = value;
                  parameterNode.filled = true;
                }

                campaignManager.updateNode(parameterNode);
              }
            }
          });

        setState(self.STATE_IDLE);
      } else if (state == self.STATE_IDLE) {
        setTimeout(self.updateParameterValueAfterDecision, 100);
        setState(self.STATE_TIMER_WAITING);
      }
    };

    this.getIncomingChainsForIcon = function(iconId) {
      var icons = campaignManager.getIconsForSubmission();
      var iconMap = getIconMap(icons);  // iconId -> icon
      var connectionBackMap = getBackwardConnectionMap(icons);  // target -> {source...}

      function collectIncomingChains(id, visitedIds) {
        var chains = [];
        var icon = iconMap[id];

        if (icon && !visitedIds[id]) {
          var connections = Object.keys(connectionBackMap[id] || {});

          if (connections.length > 0) {
            visitedIds[id] = true;
            connections.forEach(function(previousId) {
              collectIncomingChains(previousId, visitedIds).forEach(function(previousChain) {
                previousChain.unshift(icon);
                chains.push(previousChain);
              });
            });
            visitedIds[id] = false;
          } else {
            chains.push([icon]);
          }
        }

        return chains;
      }

      return collectIncomingChains(iconId || editorsHelper.curEditingNode.id, {});
    };

    this.updateRecipientNodesChains = function () {
      var icons = campaignManager.getIconsForSubmission();
      var nodes = Object.values(campaignManager.getCampaignManagerNodes().getNodes());
      var nodesMap = getIconMap(nodes);

      nodes.forEach(function (node) {
        node.isRecipientDependent = false;
      });

      var connectionForwardMap = getForwardConnectionMap(icons);
      Object.keys(connectionForwardMap).filter(function (key) {
        return nodesMap[key].type !== nodeFactory.NODE_TYPE_RECIPIENT;
      }).forEach(function (key) {
        delete connectionForwardMap[key];
      });

      for (var stub in connectionForwardMap) {
        for (var index in connectionForwardMap) {
          var subsidiaries = connectionForwardMap[index];
          if (!subsidiaries || !Object.keys(subsidiaries).length) {
            continue;
          }
          var node = nodesMap[index];
          var mailingListId = node.data.mailinglistId;
          for (var subsdr in subsidiaries) {
            nodesMap[subsdr].data.mailinglistId = mailingListId;
            nodesMap[subsdr].isRecipientDependent = true;
          }
        }
      }
    }
  };

  AGN.Lib.WM.ChainProcessor = ChainProcessor;

})();
