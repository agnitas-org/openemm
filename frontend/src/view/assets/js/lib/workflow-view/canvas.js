(function() {
  const Def = AGN.Lib.WM.Definitions;
  const Node = AGN.Lib.WM.Node;
  const Minimap = AGN.Lib.WM.Minimap;
  
  class Canvas {
    constructor($canvas) {
      const self = this;
      this.$canvas = $canvas;
      this.mouseWheelZoomEnabled = false;
      this.panningEnabled = false;
      this.zoomEventHandler = null;
      this.moveEventHandler = null;
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
        onDoubleClick: () => false,
        beforeWheel: () => !self.mouseWheelZoomEnabled,
        beforeMouseDown: e => e.button !== Def.MOUSE_BUTTON_MIDDLE,
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

        if (self.moveEventHandler) {
          self.moveEventHandler.call(null, self.getPosition());
        }
      });
    }

    get$() {
      return this.$canvas;
    }

    setMinimapEnabled(isEnabled) {
      this.minimap.setEnabled(isEnabled);
    }

    updateMinimap() {
      this.minimap.updateMinimap();
    }

    setPanningEnabled(isEnabled) {
      this.panningEnabled = !!isEnabled;
    }

    setMouseWheelZoomEnabled(isEnabled) {
      this.mouseWheelZoomEnabled = isEnabled;
    }

    getZoom() {
      var transform = this.panzoom.getTransform();
      return transform.scale;
    }

    getPosition() {
      var transform = this.panzoom.getTransform();
      return {
        x: transform.x,
        y: transform.y
      };
    }

    getVisibleArea() {
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
    }

    moveTo(x, y) {
      this.panzoom.moveTo(x, y);
    }

    setZoom(scale) {
      var transform = this.panzoom.getTransform();
      this.ignoreNextZoomEvent = true;
      this.panzoom.zoomAbs(transform.x, transform.y, scale);
      this.minimap.onPanZoom();
    }

    setOnZoom(handler) {
      this.zoomEventHandler = handler;
    }

    setOnMove(handler) {
      this.moveEventHandler = handler;
    }

    getCanvasParent$() {
      return this.get$().parent();
    }

    getIcons() {
      var icons = [];
      this.get$().children('.node').each(function(index, element) {
        icons.push($(element));
      });

      return icons;
    }

    getPureIconsBox() {
      var icons = this.getIcons();
      var zoom = this.getZoom();

      if (icons.length) {
        var box = icons.map(function($node) {
          return Node.getBox($node);
        }).reduce(function(result, current, index) {
          return {
            minX: Math.min(result.minX, current.minX),
            minY: Math.min(result.minY, current.minY),
            maxX: Math.max(result.maxX, current.maxX),
            maxY: Math.max(result.maxY, current.maxY)
          };
        });

        return {
          minX: box.minX / zoom - Def.CANVAS_GRID_SIZE,
          minY: box.minY / zoom - Def.CANVAS_GRID_SIZE,
          maxX: box.maxX / zoom + Def.CANVAS_GRID_SIZE,
          maxY: box.maxY / zoom + Def.CANVAS_GRID_SIZE
        };
      } else {
        var $viewport = this.getCanvasParent$();

        return {
          minX: -Def.CANVAS_GRID_SIZE,
          minY: -Def.CANVAS_GRID_SIZE,
          maxX: $viewport.width() / zoom + Def.CANVAS_GRID_SIZE,
          maxY: $viewport.height() / zoom + Def.CANVAS_GRID_SIZE
        };
      }
    }

    fitPdfPage(size, orientation) {
      var box = this.getPureIconsBox();

      var width = box.maxX - box.minX + Def.NODE_MIN_MARGIN;
      var height = box.maxY - box.minY + Def.NODE_MIN_MARGIN;

      if (!Def.PAGE_DIMENSIONS[size]) {
        //wrong size parameter, leave everything as it is
        return;
      }

      var pageDimension = Def.PAGE_DIMENSIONS[size];
      var isLandscape = orientation == 'landscape';
      var sceneWidth = isLandscape ? pageDimension.height : pageDimension.width;
      var sceneHeight = isLandscape ? pageDimension.width : pageDimension.height

      var transformX = 0;
      var transformY = 0;
      var scale = 1.0;
      if (width > sceneWidth || height > sceneHeight) {
        if (width / height < sceneWidth / sceneHeight) {
          scale = sceneHeight / height;
        } else {
          scale = sceneWidth / width;
        }
      }

      if (box.minX < 0) {
        transformX = -box.minX * scale;
      }
      if (box.minY < 0) {

        transformY = -box.minY * scale;
      }

      var transformCss = 'matrix(' + scale + ', 0, 0, ' + scale + ', ' + transformX + ', ' + transformY + ')';
      var transformOrigin = 'left top 0';

      this.getCanvasParent$().css({
        'transform': transformCss,
        '-webkit-transform': transformCss,
        'transform-origin': transformOrigin,
        '-webkit-transform-origin': transformOrigin,
        'backface-visibility': 'hidden',
        '-webkit-backface-visibility': 'hidden',
        'overflow': 'visible'
      })
    }
  }

  AGN.Lib.WM.Canvas = Canvas;
})();
