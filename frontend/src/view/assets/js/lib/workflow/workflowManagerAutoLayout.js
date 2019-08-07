(function() {
  var AutoLayout = function AutoLayout(campaignManagerSettings) {

    // horizontal step of nodes placing (1.5 of node size)
    var nodeStepX = Math.round((campaignManagerSettings.nodeSize * 1.5) / campaignManagerSettings.gridSize);

    // vertical step of nodes placing (1.8 of node size)
    var nodeStepY = Math.round((campaignManagerSettings.nodeSize * 1.8) / campaignManagerSettings.gridSize);


    // Lays out the workflow. The algorithm is quite simple: it places nodes from left to right following the connections.
    // If the workflow is split - places the nodes of parallel branches one under another (centering by Y)
    // The nodes without connections are just placed at the end.
    this.layoutWorkflow = function(nodes, connections) {
      var processedNodesIds = [];
      var curX = 1;
      var minY = 0;

      // get starting nodes to start with
      var startingNodes = getStartingNodes(nodes, connections);
      if (startingNodes.length > 0) {
        for(var j = 0; j < startingNodes.length; j++) {
          var previousStepNodes = [];
          var currentStepNodes = [];
          currentStepNodes.push(startingNodes[j]);

          // repeat until the workflow ends (no further nodes)
          while (currentStepNodes.length > 0) {

            // the initial Y coordinate for current step is calculated according to number of
            // nodes in current step so that the nodes will be centered by Y coordinate
            var yStart = -Math.round(((currentStepNodes.length - 1) * nodeStepY) / 2) + 0;

            var newStepNodes = [];
            for(var i = 0; i < currentStepNodes.length; i++) {
              var curNode = currentStepNodes[i];
              if (processedNodesIds.indexOf(curNode.id) == -1) {

                // the x coordinate is increased for each step of nodes so that the workflow goes right
                curNode.x = curX;
                // nodes of one step should be placed in one row one under another
                curNode.y = yStart + i * nodeStepY;

                // calculate the minY for shifting to positive coordinates at the end
                if (curNode.y < minY) {
                  minY = curNode.y;
                }

                // get the nodes following the current node
                var nextNodes = getNextNodes(nodes, curNode, connections);
                newStepNodes = newStepNodes.concat(nextNodes);

                // remember that we already positioned this node
                processedNodesIds.push(curNode.id);
              }
            }
            previousStepNodes = currentStepNodes;
            currentStepNodes = [];

            // put the next step nodes to current step avoiding duplication
            jQuery.each(newStepNodes, function(i, el) {
              if (jQuery.inArray(el, currentStepNodes) === -1) currentStepNodes.push(el);
            });

            // if the number of nodes in adjacent steps differs by more than 1 - we need to add extra
            // X-space between steps to avoid the connections messing up with nodes
            var nodeNumberDiff = Math.abs(previousStepNodes.length - currentStepNodes.length);
            if (nodeNumberDiff > 1) {
              curX += nodeStepX + Math.round((campaignManagerSettings.nodeSize / campaignManagerSettings.gridSize) * 0.5 * nodeNumberDiff);
            }
            else {
              curX += nodeStepX;
            }

            // this code tries to place the mailing nodes going in parallel branches one under another
            if (containsMailing(previousStepNodes) && containsMailing(currentStepNodes)) {
              var mailingNodes = getMailingNodes(previousStepNodes);
              for(i = 0; i < mailingNodes.length; i++) {
                var nextByThisMailing = getNextNodes(nodes, mailingNodes[i], connections);
                currentStepNodes = removeAll(currentStepNodes, nextByThisMailing);
                currentStepNodes.push(mailingNodes[i]);
                processedNodesIds = removeAll(processedNodesIds, [mailingNodes[i].id]);
                // as we removed the mailing node from previous step - shift all nodes bellow 1 position up
                for(k = 0; k < previousStepNodes.length; k++) {
                  var nodeOfPrevStep = previousStepNodes[k];
                  if (nodeOfPrevStep.y > mailingNodes[i].y) {
                    nodeOfPrevStep.y -= nodeStepY;
                  }
                }
              }
            }
          }
        }
      }

      // layout nodes that don't have connection (just put them one after another at the end of workflow)
      for(i in nodes) {
        var node = nodes[i];
        if (processedNodesIds.indexOf(node.id) == -1) {
          node.y = 0;
          node.x = curX;
          curX += nodeStepX;
        }
        // bring nodes to positive y (starting from 1 y-position)
        node.y += -minY + 1;
      }

    };

    // removes sub-array in specified array
    var removeAll = function(inArray, subArray) {
      var newArray = [];
      jQuery.each(inArray, function(i, el) {
        if (jQuery.inArray(el, subArray) === -1) newArray.push(el);
      });
      return newArray;
    };

    // checks if set of nodes contains at least one mailing node
    var containsMailing = function(nodes) {
      for(var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (isMailingNode(node)) {
          return true;
        }
      }
      return false;
    };

    // checks if node is mailing
    var isMailingNode = function(node) {
      return (node.type == "mailing" || node.type == "actionbased_mailing" || node.type == "datebased_mailing"
        || node.type == "followup_mailing");
    };

    // gets all mailing nodes from given set of nodes
    var getMailingNodes = function(nodes) {
      var mailings = [];
      for(var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (isMailingNode(node)) {
          mailings.push(node);
        }
      }
      return mailings;
    };

    // gets nodes going after the current node (according to connections)
    var getNextNodes = function(nodes, node, connections) {
      var nextNodes = [];
      for(var i = 0; i < connections.length; i++) {
        var curConnection = connections[i];
        if (node.element.id == curConnection.source) {
          for(var j in nodes) {
            var testNode = nodes[j];
            if (testNode.element.id == curConnection.target) {
              nextNodes.push(testNode);
            }
          }
          if (node.type == "decision") {

            nextNodes.sort(function(firstNode, secondNode) {
              if (node.y >= firstNode.y && node.y >= secondNode.y) {
                var difference = firstNode.y - secondNode.y;
                return difference === 0 ? firstNode.x - secondNode.x : difference;
              }
              //one of node above and another under the decision node
              else {
                return firstNode.y - secondNode.y
              }
            });
          }
        }
      }
      return nextNodes;
    };

    // gets nodes with outgoing connections having no incoming connections
    var getStartingNodes = function(nodes, connections) {
      var startingNodes = [];
      for(var j in nodes) {
        var testNode = nodes[j];
        var hasIncoming = false;
        var hasOutgoing = false;
        for(var i = 0; i < connections.length; i++) {
          var curConnection = connections[i];
          if (testNode.element.id == curConnection.source) {
            hasOutgoing = true;
          }
          else if (testNode.element.id == curConnection.target) {
            hasIncoming = true;
          }
        }
        if (hasOutgoing && !hasIncoming) {
          startingNodes.push(testNode);
        }
      }
      return startingNodes;
    }
  };

  AGN.Lib.WM.AutoLayout = AutoLayout;
})();