(function() {
    var Def = AGN.Lib.WM.Definitions,
      Minimap = AGN.Lib.WM.Minimap;

    var Canvas = function($canvas) {
        var self = this;

        this.$canvas = $canvas;
        this.mouseWheelZoomEnabled = true;
        this.panningEnabled = false;
        this.zoomEventHandler = null;
        this.ignoreNextZoomEvent = false;
        this.minimap = new Minimap(this, {
            containment: {
                mode: 'panner-box', // mode 'none', 'minimap-box' OR 'panner-box'
                padding: 5
            },
            onPannerDrag: function(dX, dY) {
                if (dX || dY) {
                    var position = self.getPosition();
                    self.moveTo(position.x + dX, position.y + dY);
                }
            }
        });

        this.panzoom = panzoom($canvas[0], {
            minZoom: Def.MIN_ZOOM,
            maxZoom: Def.MAX_ZOOM,
            initialZoom: Def.DEFAULT_ZOOM,
            disableKeyboardInteraction: true,
            smoothScroll: false,

            // Disable zoom change on double click.
            onDoubleClick: function() {
                return false;
            },

            beforeWheel: function() {
                return !self.mouseWheelZoomEnabled;
            },

            beforeMouseDown: function() {
                return !self.panningEnabled;
            }
        });

        this.panzoom.on('zoom', function() {
            if (self.zoomEventHandler) {
                if (self.ignoreNextZoomEvent) {
                    self.ignoreNextZoomEvent = false;
                } else {
                    self.zoomEventHandler.call(null, self.getZoom());
                    self.minimap.onPanZoom();
                }
            }
        });

        this.panzoom.on('pan', function() {
            self.minimap.onPanZoom();
        });
    };

    Canvas.prototype.get$ = function() {
        return this.$canvas;
    };

    Canvas.prototype.setMinimapEnabled = function(isEnabled) {
        this.minimap.setEnabled(isEnabled);
    };

    Canvas.prototype.updateMinimap = function() {
        this.minimap.updateMinimap();
    };

    Canvas.prototype.setPanningEnabled = function(isEnabled) {
        this.panningEnabled = !!isEnabled;
    };

    Canvas.prototype.setMouseWheelZoomEnabled = function(isEnabled) {
        this.mouseWheelZoomEnabled = isEnabled;
    };

    Canvas.prototype.getZoom = function() {
        var transform = this.panzoom.getTransform();
        return transform.scale;
    };

    Canvas.prototype.getPosition = function() {
        var transform = this.panzoom.getTransform();
        return {
            x: transform.x,
            y: transform.y
        };
    };

    Canvas.prototype.getVisibleArea = function() {
        var $viewport = this.$canvas.parent();
        var viewportWidth = $viewport.width();
        var viewportHeight = $viewport.height();
        var transform = this.panzoom.getTransform();

        return {
            minX: -transform.x / transform.scale,
            minY: -transform.y / transform.scale,
            maxX: (viewportWidth - transform.x) / transform.scale,
            maxY: (viewportHeight - transform.y) / transform.scale
        };
    };

    Canvas.prototype.moveTo = function(x, y) {
        this.panzoom.moveTo(x, y);
    };

    Canvas.prototype.setZoom = function(scale) {
        var transform = this.panzoom.getTransform();
        this.ignoreNextZoomEvent = true;
        this.panzoom.zoomAbs(transform.x, transform.y, scale);
        this.minimap.onPanZoom();
    };

    Canvas.prototype.setOnZoom = function(handler) {
        this.zoomEventHandler = handler;
    };

    AGN.Lib.WM.Canvas = Canvas;
})();
