(function() {

  var Node = AGN.Lib.WM.Node,
    Def = AGN.Lib.WM.Definitions;

  /**
   * Minimap Canvas element contains node prototypes scaled to fit into minimap box
   *
   * @param minimap
   * @param selector
   * @param options
   * @constructor
   */
  var MinimapCanvas = function(minimap, selector, options) {
    this.minimap = minimap;
    this.$element = minimap.$container.find(selector);
    this.scale = 1;
  };

  MinimapCanvas.prototype.setScale = function(scale) {
    this.scale = scale;
  };

  MinimapCanvas.prototype.getScale = function() {
    return this.scale;
  };

  MinimapCanvas.prototype.getPosition = function() {
    var position = this.$element.position();
    return {
      x: position.left,
      y: position.top
    }
  };

  MinimapCanvas.prototype.update = function () {
    this.reset();
    var pureBox = this.minimap.getPureIconsBox();
    var padding = Def.MINIMAP_PADDING * 2;

    var width = pureBox.maxX - pureBox.minX + padding;
    var height = pureBox.maxY - pureBox.minY + padding;

    var xRatio = (this.minimap.getWidth() - padding) / width;
    var yRatio = (this.minimap.getHeight() - padding) / height;

    this.setScale(Math.min(xRatio, yRatio));

    var left = (-pureBox.minX) * this.scale + Def.MINIMAP_PADDING;
    var top = (-pureBox.minY) * this.scale + Def.MINIMAP_PADDING;

    this.$element.css({
      'transform': 'scale(' + this.scale + ')',
      'width': width,
      'height': height,
      'left': left,
      'top': top
    });

    this.renderIcons();
  };

  MinimapCanvas.prototype.renderIcons = function() {
    var self = this;

    this.minimap.getPureIconsPosition().forEach(function(position) {
      var $node = $('<div class="miniview-element"></div>').css({
        width: Def.NODE_SIZE,
        height: Def.NODE_SIZE,
        left: position.x,
        top: position.y,
      });

      self.$element.append($node);
    });
  };

  MinimapCanvas.prototype.reset = function() {
    this.scale = 1;
    this.$element.css({
      'transform': 'scale(1)',
      'width': 0,
      'height': 0,
      'left': 0,
      'top': 0
    });
    this.$element.html('');
  };

  /**
   * Minimap Panner is an element that highlights the visible canvas area
   *
   * It is resized according to main canvas zoom
   *
   * @param minimap
   * @param selector
   * @param options
   * @constructor
   */
  var MinimapPanner = function (minimap, selector, options) {
    this.minimap = minimap;
    this.$element = minimap.get$().find(selector);
    this.onDrag = _.isFunction(options.onPannerDrag) ? options.onPannerDrag : _.noop;

    this.containment = $.extend({mode: '', padding: 0}, options.containment);
    this.containmentBox = null;

    var self = this;

    var prevX = 0;
    var prevY = 0;

    this.$element.draggable({
      start: function(event, ui) {
        prevX = ui.position.left;
        prevY = ui.position.top;

        if (!self.containmentBox) {
            self.containmentBox = self.getContainmentBox(self.containment);
        }
      },
      drag: function (event, ui) {
        var scale = self.getScale();

        if (self.containmentBox) {
          //hold back
          var left = ui.position.left;
          var top = ui.position.top;

          if (left < self.containmentBox.minX) {
            ui.position.left = self.containmentBox.minX;
          } else if (left > self.containmentBox.maxX) {
            ui.position.left = self.containmentBox.maxX;
          }

          if (top < self.containmentBox.minY) {
            ui.position.top = self.containmentBox.minY;
          } else if (top > self.containmentBox.maxY) {
            ui.position.top = self.containmentBox.maxY;
          }
        }

        var dX = (prevX - ui.position.left) / scale;
        var dY = (prevY - ui.position.top) / scale;
        prevX = ui.position.left;
        prevY = ui.position.top;

        self.onDrag.call(self, dX, dY);
      }
    });
  };

  MinimapPanner.prototype.getContainmentBox = function(options) {
    if (!options.mode || options.mode == 'none') {
      return undefined;
    }

    var $minimap = this.minimap.get$();

    var scaledWidth = this.getScaledWidth();
    var scaledHeight = this.getScaledHeight();

    if (options.mode == 'panner-box') {
      return {
        minX: -scaledWidth + options.padding,
        maxX: $minimap.width() - options.padding,
        minY: -scaledHeight + options.padding,
        maxY: $minimap.height() - options.padding
      };
    } else if (options.mode == 'minimap-box') {
      return {
        minX: options.padding,
        maxX: Math.max(0, $minimap.width() - scaledWidth) - options.padding,
        minY: options.padding,
        maxY: Math.max(0, $minimap.height() - scaledHeight) - options.padding
      }
    }

    return undefined;
  };

  MinimapPanner.prototype.getScale = function () {
    return this.minimap.getMiniCanvasScale() / this.minimap.getCanvasZoom();
  };

  MinimapPanner.prototype.getScaledWidth = function () {
    return this.getWidth() * this.getScale();
  };

  MinimapPanner.prototype.getScaledHeight = function () {
    return this.getHeight() * this.getScale();
  };

  MinimapPanner.prototype.getWidth = function() {
    return this.$element.width();
  };

  MinimapPanner.prototype.getHeight = function() {
    return this.$element.height();
  };

  MinimapPanner.prototype.update = function () {
    var viewPortScreen = this.minimap.getCanvasParent$();

    var originWidth = viewPortScreen.width();
    var originHeight = viewPortScreen.height();
    var width = originWidth + Def.MINIMAP_PADDING * 2;
    var height = originHeight + Def.MINIMAP_PADDING * 2;

    this.$element.css({
      width: width,
      height: height,
    });

    this.containmentBox = this.getContainmentBox(this.containment);
    this.updatePosition();
  };

  MinimapPanner.prototype.updatePosition = function() {
    var position = this.minimap.getCanvasPosition();
    var delta = this.minimap.getMiniCanvasPosition();
    var scale = this.getScale();

    var left = (-position.x * scale) + delta.x;
    var top = (-position.y * scale) + delta.y;

    this.$element.css({
      transform: 'scale(' + scale + ')',
      left: left,
      top: top
    });
  };

  var Minimap = function(canvas, options) {
    this.$container = $('#minimap');
    this.canvas = canvas;

    this.$collapseBtn = this.$container.find('.minimap-collapse');
    this.miniCanvas = new MinimapCanvas(this, '.minimap-canvas', options);
    this.miniPanner = new MinimapPanner(this, '.minimap-panner', options);

    this.enabled = false;

    var self = this;
    this.$container.on('click', '.minimap-panner, .minimap-collapse', function(e) {
      e.preventFocusOnArea = true;
    });

    this.$collapseBtn.on('click', function(e){
      self.collapseMinimap();
    });

    this.$container.on('click', function(e) {
      if (e.preventFocusOnArea) {
        return;
      }

      //detect minimap positions
      var rec = self.$container[0].getBoundingClientRect();
      var pX = e.clientX - rec.left;
      var pY = e.clientY - rec.top;

      self.focusOnClickedArea(pX, pY);
    });
  };

  Minimap.prototype.get$ = function() {
    return this.$container;
  };

  Minimap.prototype.getWidth = function() {
    return this.get$().width();
  };

  Minimap.prototype.getHeight = function() {
    return this.get$().height();
  };

  Minimap.prototype.moveCanvasTo = function(x, y) {
    this.canvas.moveTo(x, y);
  };

  Minimap.prototype.getCanvas$ = function() {
    return this.canvas.get$();
  };

  Minimap.prototype.getCanvasParent$ = function() {
    return this.canvas.get$().parent();
  };

  Minimap.prototype.focusOnClickedArea = function(pointerX, pointerY) {
    //calculate main area position according to minimap positions
    var miniCanvasPosition = this.getMiniCanvasPosition();
    var scale = this.getMiniPannerScale();
    // var scale = self.getMiniCanvasScale();
    var x = (pointerX - miniCanvasPosition.x) / scale;
    var y = (pointerY - miniCanvasPosition.y) / scale;

    //center the clicked position
    var miniPannerCenter = this.getMiniPannerCenter();
    var zoom = this.getCanvasZoom();
    var centeredX = (x - miniPannerCenter.x) * zoom;
    var centeredY = (y - miniPannerCenter.y) * zoom;

    this.moveCanvasTo(-centeredX, -centeredY);
  };

  Minimap.prototype.collapseMinimap = function() {
    this.$container.toggleClass('minimap-collapsed');
    this.updateMinimap();
  };

  Minimap.prototype.getIcons = function() {
    var icons = [];
    this.getCanvas$().children('.node').each(function(index, element) {
      icons.push($(element));
    });

    return icons;
  };

  Minimap.prototype.getPureIconsPosition = function() {
    var zoom = this.getCanvasZoom();
    return this.getIcons().map(function($element) {
      var position = Node.getPosition($element);

      return {
        x: position.x / zoom,
        y: position.y / zoom,
      };
    });
  };

  Minimap.prototype.getPureIconsBox = function() {
    var icons = this.getIcons();
    var zoom = this.getCanvasZoom();

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
  };

  Minimap.prototype.getMiniCanvasScale = function() {
    return this.miniCanvas.getScale();
  };

  Minimap.prototype.getMiniCanvasPosition = function() {
    return this.miniCanvas.getPosition();
  };

  Minimap.prototype.getMiniPannerScale = function() {
    return this.miniCanvas.getScale();
  };

  Minimap.prototype.getMiniPannerCenter = function() {
    return {
      x: this.miniPanner.getWidth() / 2,
      y: this.miniPanner.getHeight() / 2
    };
  };

  Minimap.prototype.getCanvasZoom = function() {
    return this.canvas.getZoom();
  };

  Minimap.prototype.getCanvasPosition = function() {
    return this.canvas.getPosition();
  };

  Minimap.prototype.updateMinimap = function() {
    if (this.enabled) {
      this.miniCanvas.update();
      this.miniPanner.update();
    }
  };

  Minimap.prototype.onPanZoom = function() {
    if (this.enabled) {
      this.miniPanner.updatePosition();
    }
  };

  Minimap.prototype.setEnabled = function(isEnabled) {
    this.enabled = isEnabled;
    if (isEnabled) {
      this.updateMinimap();
      this.get$().show();
    } else {
      this.get$().hide();
    }
  };

  AGN.Lib.WM.Minimap = Minimap;
})();
