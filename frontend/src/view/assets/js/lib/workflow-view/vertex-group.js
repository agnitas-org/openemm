(function() {
    var Vertex = AGN.Lib.WM.Vertex;

    // Represents a group of vertices (nodes) that all are connected to each other (directly or not).
    function VertexGroup(vertices) {
        this.vertices = vertices;

        // Calculate a bounding rectangle for the whole group.
        this.box = vertices.map(function(v) { return v.box; })
            .reduce(function(b1, b2) {
                return {
                    minX: Math.min(b1.minX, b2.minX),
                    minY: Math.min(b1.minY, b2.minY),
                    maxX: Math.max(b1.maxX, b2.maxX),
                    maxY: Math.max(b1.maxY, b2.maxY)
                };
            });
    }

    VertexGroup.detectGroups = function(vertices) {
        var groups = [];
        var visitedIds = {};

        // Detect all groups (including standalone nodes).
        vertices.forEach(function(vertex) {
            var group = VertexGroup.detectGroup(vertex, visitedIds);
            if (group) {
                groups.push(group);
            }
        });

        return groups.sort(VertexGroup.compareByPosition);
    };

    VertexGroup.detectGroup = function(vertex, visitedIds) {
        // Already processed (belongs to another group which is already detected).
        if (visitedIds[vertex.id]) {
            return null;
        }

        var group = [];
        var vertices = vertex.siblings();

        group.push(vertex);
        visitedIds[vertex.id] = true;

        while (vertices.length) {
            vertices = _.flatMap(vertices, function(v) {
                if (visitedIds[v.id]) {
                    return [];
                } else {
                    group.push(v);
                    visitedIds[v.id] = true;

                    return v.siblings();
                }
            });
        }

        return new VertexGroup(group);
    };

    VertexGroup.compareByPosition = function(g1, g2) {
        var d = (g1.box.minX + g1.box.maxX) - (g2.box.minX + g2.box.maxX);
        if (d == 0) {
            return (g1.box.minY + g1.box.maxY) - (g2.box.minY + g2.box.maxY);
        }
        return d;
    };

    VertexGroup.prototype.getNodes = function() {
        return this.vertices.map(function(vertex) {
            return vertex.getNode();
        });
    };

    VertexGroup.prototype.getStartingVertices = function() {
        // Normally there should be a single node which has no incoming connections but a user actually can compose any kind of
        // illegal schema (even though it's impossible to activate that campaign) so we have to handle these situations as well:
        // - there's a loop so every single node has incoming connections;
        // - there are multiple starting nodes;

        var startingVertices = this.vertices.filter(function(vertex) {
            return vertex.connections.incoming.length == 0;
        });

        if (startingVertices.length) {
            // If there are several starting icons then preserve their order defined by user.
            return startingVertices.sort(Vertex.compareByPosition);
        }

        // In case a group doesn't have such a starting node we'll pick up some node which looks better for that role. :)
        // The more outgoing connections (and the less incoming connections) it has the better it looks for a starting node.
        // If all the nodes have the same "connections rating" then we'll start at the one which user places closer to left top corner.

        var bestCandidate = this.vertices.reduce(function(v1, v2) {
            var rate1 = v1.connections.outgoing.length / v1.connections.incoming.length;
            var rate2 = v2.connections.outgoing.length / v2.connections.incoming.length;

            if (rate1 == rate2) {
                return Vertex.compareByPosition(v1, v2) >= 0 ? v1 : v2;
            }

            return rate1 > rate2 ? v1 : v2;
        });

        return [bestCandidate];
    };

    AGN.Lib.WM.VertexGroup = VertexGroup;
})();
