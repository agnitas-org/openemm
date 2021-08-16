(function() {
    var Def = AGN.Lib.WM.Definitions,
        Node = AGN.Lib.WM.Node;

    var SELECTOR = '.js-draggable-button';

    function hasCollision(occupiedAreas, ui) {
        var newNodeBox = Node.getCollisionBox(ui.helper);

        return occupiedAreas.some(function(box) {
            return Node.detectBoxCollision(newNodeBox, box);
        });
    }

    function DraggableButtons(options) {
        this.enabled = !!options.enabled;
        this.$buttons = $(SELECTOR);

        var self = this;

        this.$buttons.each(function() {
            var $button = $(this);
            var type = $button.data('type');
            var occupiedAreas = [];

            $button.draggable({
                revert: true,
                revertDuration: 0,
                zIndex: Def.Z_INDEX_DRAGGABLE_BUTTON,
                disabled: !self.enabled,
                cursorAt: function() {
                    var rect = this.helper[0].getBoundingClientRect();
                    return [rect.width / 2 - 1, rect.height / 2];
                },
                appendTo: '#canvas',
                helper: function() {
                    return Node.createDraggable$(type);
                },
                start: function() {
                    occupiedAreas = options.getOccupiesAreas.call(self);
                    options.onStart.call(self);
                },
                stop: function() {
                    options.onStop.call(self);
                },
                drag: function(event, ui) {
                    var scale = options.getZoom.call(self);

                    ui.position.left = Math.round(ui.position.left / scale / Def.CANVAS_GRID_SIZE) * Def.CANVAS_GRID_SIZE;
                    ui.position.top = Math.round(ui.position.top / scale / Def.CANVAS_GRID_SIZE) * Def.CANVAS_GRID_SIZE;

                    ui.helper.toggleClass('js-collision-detected', hasCollision(occupiedAreas, ui, scale));
                }
            }).on('dblclick', function() {
                if (self.enabled) {
                    options.onDrop.call(self, type);
                }
            });
        });

        $('#viewPort').droppable({
            accept: SELECTOR,
            tolerance: 'fit',
            drop: function(event, ui) {
                if (ui.helper.hasClass('js-collision-detected')) {
                    return;
                }

                var scale = options.getZoom.call(self);
                var containerOffset = $('#canvas').offset();
                var x = Math.round((ui.offset.left - containerOffset.left) / scale);
                var y = Math.round((ui.offset.top - containerOffset.top) / scale);

                options.onDrop.call(self, ui.helper.data('type'), {
                    x: Math.round(x / Def.CANVAS_GRID_SIZE),
                    y: Math.round(y / Def.CANVAS_GRID_SIZE)
                });
            }
        });
    }

    DraggableButtons.prototype.setEnabled = function(isEnabled) {
        this.enabled = !!isEnabled;
        this.$buttons.draggable('option', 'disabled', !isEnabled);
    };

    AGN.Lib.WM.DraggableButtons = DraggableButtons;
})();
