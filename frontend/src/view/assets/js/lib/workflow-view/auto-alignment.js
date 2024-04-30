(function() {
    var Def = AGN.Lib.WM.Definitions,
        Vertex = AGN.Lib.WM.Vertex,
        VertexGroup = AGN.Lib.WM.VertexGroup;

    function PreAlignedGroup(layers, height) {
        this.layers = layers;
        this.height = height;
    }

    PreAlignedGroup.fromVertexGroup = function(vertexGroup) {
        var layers = [];
        var visitedIds = {};
        var layer = Vertex.filterUnvisited(vertexGroup.getStartingVertices(), visitedIds);
        var height = 0;

        while (layer.length) {
            height = Math.max(height, layer.length);

            layers.push(layer);
            layer = _.flatMap(layer, function(vertex) {
                return Vertex.filterUnvisited(vertex.rightSiblingsOrdered(), visitedIds);
            });
        }

        return new PreAlignedGroup(layers, height);
    };

    PreAlignedGroup.prototype.align = function(startX, maxHeight) {
        var previousLayer = null;

        this.layers.forEach(function(layer) {
            // Align every layer with all the others (including layers of other groups).
            var startY = Math.round(Def.AUTO_ALIGN_STEP_Y * (maxHeight - layer.length) / 2);

            if (previousLayer) {
                // To prevent a mess add extra space if nodes number in adjacent layers changes by more than 1.
                var nodesNumberDelta = Math.abs(previousLayer.length - layer.length);

                if (nodesNumberDelta > 1) {
                    startX += Math.round(Def.AUTO_ALIGN_EXTRA_STEP_X * nodesNumberDelta);
                }
            }

            layer.forEach(function(vertex) {
                vertex.node.setCoordinates(startX, startY);

                startY += Def.AUTO_ALIGN_STEP_Y;
            });

            startX += Def.AUTO_ALIGN_STEP_X;
            previousLayer = layer;
        });

        return startX;
    };

    function align(vertices) {
        var preAlignedGroups = VertexGroup.detectGroups(vertices)
            .map(function(vertexGroup) {
                return PreAlignedGroup.fromVertexGroup(vertexGroup);
            });

        var maxHeight = 0;
        preAlignedGroups.forEach(function(group) {
            maxHeight = Math.max(maxHeight, group.height);
        });

        var startX = 1;
        preAlignedGroups.forEach(function(group) {
            startX = group.align(startX, maxHeight);
            startX += Def.AUTO_ALIGN_STEP_BETWEEN_GROUPS;
        });
    }

    AGN.Lib.WM.AutoAlignment = {
        align: align
    };
})();
