(function() {
    var Node = AGN.Lib.WM.Node;

    // Just a wrapper for a node to hold some cached values.
    function Vertex(node) {
        this.id = node.getId();
        this.node = node;
        this.position = Node.getPosition(node.get$());
        this.box = Node.getBox(node.get$());
        this.connections = {incoming: [], outgoing: []};
    }

    Vertex.nodesToVertices = function(nodes) {
        return nodes.map(function(node) {
            return new Vertex(node);
        });
    };

    Vertex.verticesToMap = function(vertices) {
        var map = {};

        vertices.forEach(function(vertex) {
            map[vertex.id] = vertex;
        });

        return map;
    };

    Vertex.verticesFrom = function(nodes, connections) {
        var vertices = Vertex.nodesToVertices(nodes);
        var vertexMap = Vertex.verticesToMap(vertices);

        connections.forEach(function(connection) {
            var source, target;

            if ('sourceId' in connection) {
                source = vertexMap[connection.sourceId];
                target = vertexMap[connection.targetId];
            } else {
                source = vertexMap[connection.source.getId()];
                target = vertexMap[connection.target.getId()];
            }

            source.connections.outgoing.push(target);
            target.connections.incoming.push(source);
        });

        return vertices;
    };

    Vertex.compareByPosition = function(v1, v2) {
        var box1 = v1.box;
        var box2 = v2.box;

        if (box1.minX == box2.minX) {
            // Ascending order by Y coordinate.
            return box1.minY - box2.minY;
        }

        // Ascending order by X coordinate.
        return box1.minX - box2.minX;
    };

    Vertex.filterUnvisited = function(vertices, visitedIds) {
        return vertices.filter(function(vertex) {
            if (visitedIds[vertex.id]) {
                return false;
            } else {
                visitedIds[vertex.id] = true;
                return true;
            }
        });
    };

    Vertex.getDecisionBranches = function(sourceVertex) {
        var connections = sourceVertex.connections.outgoing.map(function(targetVertex) {
            return {
                source: sourceVertex.node,
                target: targetVertex.node,
                targetVertex: targetVertex
            };
        });

        var targets = Node.getDecisionBranches(connections);
        if (targets) {
            return {positive: targets.positive.targetVertex, negative: targets.negative.targetVertex};
        }

        return null;
    };

    Vertex.getIncomingChains = function(vertex) {
        var visitedIds = {};
        var chains = [];

        collectIncomingChains([], vertex, visitedIds, chains);

        return chains;
    };

    Vertex.getOutgoingChains = function(vertex) {
        var visitedIds = {};
        var chains = [];

        collectOutgoingChains([], vertex, visitedIds, chains);

        return chains;
    };

    Vertex.prototype.getNode = function() {
        return this.node;
    };

    Vertex.prototype.siblings = function() {
        return this.connections.incoming.concat(this.connections.outgoing);
    };

    Vertex.prototype.rightSiblingsOrdered = function() {
        var siblings = this.connections.outgoing;

        if (Node.isBranchingDecisionNode(this.node)) {
            var targets = Vertex.getDecisionBranches(this);
            if (targets) {
                return [targets.positive, targets.negative];
            }
        }

        // Order outgoing connections by Y coordinate.
        return siblings.sort(function(v1, v2) {
            return v1.node.getY() - v2.node.getY();
        });
    };

    function collectIncomingChains(chain, vertex, visitedIds, chains) {
        if (visitedIds[vertex.id]) {
            chains.push(chain);
        } else {
            chain.push(vertex.node);

            var connections = vertex.connections.incoming;
            if (connections.length) {
                visitedIds[vertex.id] = true;
                if (connections.length == 1) {
                    collectIncomingChains(chain, connections[0], visitedIds, chains);
                } else {
                    connections.forEach(function(connection) {
                        collectIncomingChains(chain.slice(), connection, visitedIds, chains);
                    });
                }
                visitedIds[vertex.id] = false;
            } else {
                chains.push(chain);
            }
        }
    }

    function collectOutgoingChains(chain, vertex, visitedIds, chains) {
        if (visitedIds[vertex.id]) {
            chains.push(chain);
        } else {
            chain.push(vertex.node);

            var connections = vertex.connections.outgoing;
            if (connections.length) {
                visitedIds[vertex.id] = true;
                if (connections.length == 1) {
                    collectOutgoingChains(chain, connections[0], visitedIds, chains);
                } else {
                    connections.forEach(function(connection) {
                        collectOutgoingChains(chain.slice(), connection, visitedIds, chains);
                    });
                }
                visitedIds[vertex.id] = false;
            } else {
                chains.push(chain);
            }
        }
    }

    AGN.Lib.WM.Vertex = Vertex;
})();
