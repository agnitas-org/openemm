(function () {
  const Def = AGN.Lib.WM.Definitions;

  class GridBackground {
    constructor(canvas) {
      this.canvas = canvas;
      this.$gridBackground = $('#grid-background');
      this.scale = 1;
      this.#updateCellSize();
      this.canvas.setOnMove(() => this.move());
    }

    get shown() {
      return !this.$gridBackground.hasClass('hidden');
    }

    setZoom(scale) {
      this.scale = scale;
      this.move();
      this.#updateCellSize();
    }

    toggle() {
      this.$gridBackground.toggleClass('hidden');
    }

    move() {
      const {x, y} = this.canvas.getPosition();
      const deltaX = this.#calculateOffsetDelta(x);
      const deltaY = this.#calculateOffsetDelta(y);

      this.$gridBackground.css({
        'left': '-' + deltaX + 'px',
        'top': '-' + deltaY + 'px',
        'width': 'calc(100% + ' + deltaX + 'px)',
        'height': 'calc(100% + ' + deltaY + 'px)'
      });
    }

    #updateCellSize() {
      const cellSize = Def.NODE_SIZE * this.scale;
      this.$gridBackground.css('background-size', cellSize + 'px ' + cellSize + 'px');
    }

    #calculateOffsetDelta(positionValue) {
      if (positionValue <= 0) {
        return positionValue * -1;
      }

      const cellSize = Def.NODE_SIZE * this.scale;
      const partCellSize = (positionValue / cellSize) % 1;

      return cellSize - (cellSize * partCellSize);
    }
  }

  AGN.Lib.WM.GridBackground = GridBackground;
})();
