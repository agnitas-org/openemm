(function() {

  const Node = AGN.Lib.WM.Node;
  const Def = AGN.Lib.WM.Definitions;

  /**
   * Minimap Canvas element contains node prototypes scaled to fit into minimap box
   *
   * @param minimap
   * @param selector
   * @param options
   * @constructor
   */
  class MinimapCanvas {
    constructor(minimap, selector) {
      this.minimap = minimap;
      this.$element = minimap.$container.find(selector);
      this.scale = 1;
    }

    setScale(scale) {
      this.scale = scale;
    }
  
    getScale() {
      return this.scale;
    }
  
    getPosition() {
      var position = this.$element.position();
      return {
        x: position.left,
        y: position.top
      }
    }
  
    update () {
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
    }
  
    renderIcons() {
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
    }
  
    reset() {
      this.scale = 1;
      this.$element.css({
        'transform': 'scale(1)',
        'width': 0,
        'height': 0,
        'left': 0,
        'top': 0
      });
      this.$element.html('');
    }
  }
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
  class MinimapPanner {
    constructor(minimap, selector, options) {
      this.minimap = minimap;
      this.$element = minimap.get$().find(selector);
      this.onDrag = _.isFunction(options.onPannerDrag) ? options.onPannerDrag : _.noop;

      this.containment = $.extend({mode: '', padding: 0}, options.containment);
      this.containmentBox = null;

      var self = this;

      var prevX = 0;
      var prevY = 0;

      this.$element.draggable({
        start: function (event, ui) {
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
    }

    getContainmentBox(options) {
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
        }
      } else if (options.mode == 'minimap-box') {
        return {
          minX: options.padding,
          maxX: Math.max(0, $minimap.width() - scaledWidth) - options.padding,
          minY: options.padding,
          maxY: Math.max(0, $minimap.height() - scaledHeight) - options.padding
        }
      }

      return undefined;
    }

    getScale() {
      return this.minimap.getMiniCanvasScale() / this.minimap.getCanvasZoom();
    }

    getScaledWidth() {
      return this.getWidth() * this.getScale();
    }

    getScaledHeight() {
      return this.getHeight() * this.getScale();
    }

    getWidth() {
      return this.$element.width();
    }

    getHeight() {
      return this.$element.height();
    }

    update() {
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
    }

    updatePosition() {
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
    }
  }

  class Minimap {
    constructor(canvas, options) {
      this.$container = $('#minimap');
      this.canvas = canvas;
    
      this.$collapseBtn = this.$container.find('.minimap-collapse');
      this.miniCanvas = new MinimapCanvas(this, '.minimap-canvas', options);
      this.miniPanner = new MinimapPanner(this, '.minimap-panner', options);
    
      this.enabled = false;
    
      // const self = this;
      this.$container.on('click', '.minimap-panner, .minimap-collapse', function(e) {
        e.preventFocusOnArea = true;
      });
    
      // this.$collapseBtn.on('click', function(e){
      //   self.collapseMinimap();
      // });
      //
      this.$container.on('click', e => this.moveToClickPos(e));
      // this.$container.find('.minimap-arrow').on('click', function (e) {
      //   const direction = $(e.target).data('arrow');
      //   var miniCanvasPosition = self.getMiniCanvasPosition();
      //   var scale = self.getMiniPannerScale();
      //   var x = (miniCanvasPosition.x) / scale;
      //   var y = (miniCanvasPosition.y) / scale;
      //   const newPos = self.#getStepPos(x, y, direction);
      //   self.moveCanvasTo(newPos.x, newPos.y);
      // });
    }
    
    // #getStepPos(x, y, direction) {
    //   const step = 10;
    //   switch (direction) {
    //     case 'up':
    //       return [x, y - step];
    //     case 'right':
    //       return [x + step, y];
    //     case 'down':
    //       return [x, y + step];
    //     case 'left':
    //       return [x - step, y];
    //   }
    // }

    moveToClickPos(e) {
      if (e.preventFocusOnArea) {
        return;
      }
      //detect minimap positions
      const rec = this.$container[0].getBoundingClientRect();
      const pX = e.clientX - rec.left;
      const pY = e.clientY - rec.top;
      this.focusOnClickedArea(pX, pY);
    }

    get$() {
      return this.$container;
    }
  
    getWidth() {
      return this.get$().width();
    }
  
    getHeight() {
      return this.get$().height();
    }
  
    moveCanvasTo(x, y) {
      this.canvas.moveTo(x, y);
    }
  
    getCanvas$() {
      return this.canvas.get$();
    }
  
    getCanvasParent$() {
      return this.canvas.get$().parent();
    }
  
    focusOnClickedArea(pointerX, pointerY) {
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
    }
  
    collapseMinimap() {
      this.$container.toggleClass('minimap-collapsed');
      this.updateMinimap();
    }
  
    getIcons() {
      return this.canvas.getIcons();
    }
  
    getPureIconsPosition() {
      var zoom = this.getCanvasZoom();
      return this.getIcons().map(function($element) {
        var position = Node.getPosition($element);
  
        return {
          x: position.x / zoom,
          y: position.y / zoom,
        };
      });
    }
  
    getPureIconsBox() {
      return this.canvas.getPureIconsBox();
    }
  
    getMiniCanvasScale() {
      return this.miniCanvas.getScale();
    }
  
    getMiniCanvasPosition() {
      return this.miniCanvas.getPosition();
    }
  
    getMiniPannerScale() {
      return this.miniCanvas.getScale();
    }
  
    getMiniPannerCenter() {
      return {
        x: this.miniPanner.getWidth() / 2,
        y: this.miniPanner.getHeight() / 2
      };
    }
  
    getCanvasZoom() {
      return this.canvas.getZoom();
    }
  
    getCanvasPosition() {
      return this.canvas.getPosition();
    }
  
    updateMinimap() {
      if (this.enabled) {
        this.miniCanvas.update();
        this.miniPanner.update();
      }
    }
  
    onPanZoom() {
      if (this.enabled) {
        this.miniPanner.updatePosition();
      }
    }
  
    setEnabled(isEnabled) {
      this.enabled = isEnabled;
      if (isEnabled) {
        this.updateMinimap();
        this.get$().show();
      } else {
        this.get$().hide();
      }
    }
  }
  AGN.Lib.WM.Minimap = Minimap;
})();