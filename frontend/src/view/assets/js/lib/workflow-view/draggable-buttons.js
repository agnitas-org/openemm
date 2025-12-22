(() => {
    const Def = AGN.Lib.WM.Definitions;
    const Node = AGN.Lib.WM.Node;

    const SELECTOR = '.js-draggable-button';

    function DraggableButtons(options) {
        this.enabled = !!options.enabled;
        this.$buttons = $(SELECTOR);

        const self = this;

        this.$buttons.each(function() {
            const $button = $(this);

            $button.draggable({
                revert: true,
                revertDuration: 0,
                zIndex: Def.Z_INDEX_DRAGGABLE_BUTTON,
                disabled: !self.enabled,
                cursorAt: function() {
                    const rect = this.helper[0].getBoundingClientRect();
                    return [rect.width / 2 - 1, rect.height / 2];
                },
                appendTo: '#canvas',
                helper: function() {
                    return Node.createDraggable$($button.data('type'));
                },
                start: function() {
                    options.onStart.call(self, $button.data('type'));
                },
                stop: function() {
                    options.onStop.call(self);
                },
                drag: function(event, ui) {
                    const scale = options.getZoom.call(self);

                    ui.position.left = Math.round(ui.position.left / scale / Def.CANVAS_GRID_SIZE) * Def.CANVAS_GRID_SIZE;
                    ui.position.top = Math.round(ui.position.top / scale / Def.CANVAS_GRID_SIZE) * Def.CANVAS_GRID_SIZE;

                    options.onDrag.call(self, ui.helper);
                }
            }).on('dblclick', function() {
                if (self.enabled) {
                    options.onDrop.call(self, null, $button.data('type'));
                }
            });
        });

        $('#viewPort').droppable({
            accept: SELECTOR,
            tolerance: 'fit',
            drop: function(event, ui) {
                const scale = options.getZoom.call(self);
                const containerOffset = $('#canvas').offset();
                const x = Math.round((ui.offset.left - containerOffset.left) / scale);
                const y = Math.round((ui.offset.top - containerOffset.top) / scale);

                options.onDrop.call(self, ui.helper, ui.helper.data('type'), {
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

    DraggableButtons.prototype.changeType = function (from, to) {
        this.$buttons.filter(function () {
            return $(this).data('type') === from;
        }).data('type', to);
    }

    AGN.Lib.WM.DraggableButtons = DraggableButtons;
})();
