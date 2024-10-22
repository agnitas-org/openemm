(function() {
    var Def = AGN.Lib.WM.Definitions,
        Node = AGN.Lib.WM.Node;

    var Snippets = {};

    Snippets.loadSample = function(type, gridEnabled, callback) {
        if ([
            Def.NODE_TYPE_SC_ABTEST, Def.NODE_TYPE_SC_DOI, Def.NODE_TYPE_ANNIVERSARY_MAIL,
            Def.NODE_TYPE_SC_BIRTHDAY, Def.NODE_TYPE_BIRTHDAY_WITH_COUPON,
            Def.NODE_TYPE_WELCOME_TRACK, Def.NODE_TYPE_WELCOME_TRACK_WITH_INCENTIVE,
            Def.NODE_TYPE_SHOPPING_CART_ABANDONERS_SMALL,
            Def.NODE_TYPE_SHOPPING_CART_ABANDONERS_LARGE
        ].includes(type)) {
            $.ajax({
                url: AGN.url('/workflow/getSampleWorkflowContent.action'),
                async: false,
                data: {
                    type: type,
                    gridEnabled: gridEnabled
                },
                success: asDeserializationCallback(callback)
            });
        } else {
            console.error('Unknown sample type: ' + type);
        }
    };

    Snippets.loadAutoOptSample = function (mailingsCount, gridEnabled, callback) {
        $
            .get(AGN.url('/workflow/autoOptWorkflowSample.action'), {mailingsCount: mailingsCount, gridEnabled: gridEnabled})
            .done(asDeserializationCallback(callback));
    };
    
    Snippets.loadOwnWorkflow = function(workflowId, copyContent, callback) {
        $.ajax({
            url: AGN.url('/workflow/getWorkflowContent.action'),
            async: false,
            data: {
                workflowId: workflowId,
                isWithContent: copyContent
            },
            success: asDeserializationCallback(callback)
        });
    };

    Snippets.removeStartStop = function(nodes, connections, removeStart, removeStop) {
        if (removeStart || removeStop) {
            var removedNodes = [];

            var newNodes = nodes.filter(function(node) {
                var type = node.getType();

                if (removeStart && type === Def.NODE_TYPE_START || removeStop && type === Def.NODE_TYPE_STOP) {
                    removedNodes.push(node);
                    return false;
                } else {
                    return true;
                }
            });

            if (newNodes.length < nodes.length) {
                Array.prototype.splice.apply(nodes, [0, nodes.length].concat(newNodes));

                // Also make sure to delete abandoned connections from/to deleted icons (if any).
                var newConnections = connections.filter(function(connection) {
                    return !(removedNodes.includes(connection.source) || removedNodes.includes(connection.target));
                });

                if (newConnections.length < connections.length) {
                    Array.prototype.splice.apply(connections, [0, connections.length].concat(newConnections));
                }
            }
        }
    };

    Snippets.adjustPositions = function(nodes, snippetStartX, snippetStartY, useStartPositionNode) {
        var startingX = 0, startingY = 0;

        if (useStartPositionNode) {
            var node = getStartPositionNode(nodes);

            startingX = node.getX();
            startingY = node.getY();
        } else {
            var minX = 0, maxX = 0, minY = 0, maxY = 0;
            var first = true;

            nodes.forEach(function(node) {
                if (first) {
                    minX = maxX = node.getX();
                    minY = maxY = node.getY();
                    first = false;
                } else {
                    minX = Math.min(minX, node.getX());
                    maxX = Math.max(maxX, node.getX());
                    minY = Math.min(minY, node.getY());
                    maxY = Math.max(maxY, node.getY());
                }
            });

            startingX = Math.round((minX + maxX) / 2);
            startingY = Math.round((minY + maxY) / 2);
        }

        nodes.forEach(function(node) {
            node.setCoordinates(
                snippetStartX + node.getX() - startingX,
                snippetStartY + node.getY() - startingY
            );
        });
    };

    function asDeserializationCallback(callback) {
        return function(icons) {
            var nodes = icons.map(Node.deserialize);
            var connections = Node.deserializeConnections(icons, Node.toMap(nodes));

            callback.call(null, nodes, connections);
        };
    }

    // Pick a node to be used as a starting position for expanding the whole snippet.
    function getStartPositionNode(nodes) {
        // First try to find node(s) of start type.
        var candidates = nodes.filter(function(node) {
            return Def.NODE_TYPE_START == node.getType();
        });

        // If nothing found then take all the nodes.
        if (candidates.length == 0) {
            candidates = nodes;
        }

        // Now pick a single one which position is closer to the left top corner.
        if (candidates.length > 1) {
            return candidates.reduce(function(node1, node2) {
                if (node1.getX() == node2.getX()) {
                    return node1.getY() < node2.getY() ? node1 : node2;
                } else {
                    return node1.getX() < node2.getX() ? node1 : node2;
                }
            });
        } else {
            return candidates[0];
        }
    }

    AGN.Lib.WM.Snippets = Snippets;
})();
