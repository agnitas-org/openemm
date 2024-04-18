class DraggableTile {

  static get def() {
    return AGN.Lib.Dashboard.Definitions;
  }

  static get #utils() {
    return AGN.Lib.Dashboard.GridUtils;
  }

  constructor(controller) {
    if (new.target === DraggableTile) {
      throw new TypeError("Cannot construct Abstract Tile instances directly!");
    }
    this.type = DraggableTile.def.TILE.TYPE.REGULAR;
    this.variants = [{type: DraggableTile.def.TILE.TYPE.REGULAR}];
    this._controller = controller;
  }

  displayOnScreen($place) {
    if ($place.is('.draggable-tile')) {
      $place.replaceWith(this.$el);
    } else {
      DraggableTile.def.TILES_CONTAINER.append(this.$el);
    }
    this._$el.data("tile", this);
    DraggableTile.#utils.updateTilesPositions();
    this.#initDragAndDrop();
    AGN.runAll(this.$el);
    return this;
  }

  replaceWith(tile) {
    tile.displayOnScreen(this.$el);
  }

  remove() {
    this.$el.remove();
  }

  #initDragAndDrop() {
    DraggableTile.#utils.setDraggable(this);
    DraggableTile.#utils.setDroppable(this);
  }

  switchDroppable(on) {
    this.$el.droppable('option', 'disabled', !on);
  }

  swap(tileToSwapWith) {
    const tmp = $('<span>').hide();
    this.$el.before(tmp);
    tileToSwapWith.$el.before(this.$el);
    tmp.replaceWith(tileToSwapWith.$el);
  }

  get id() {
    return 'unknown';
  }

  get name() {
    return t(`dashboard.tile.${this.id}`);
  }

  thumbnail() {
    return AGN.url(`/assets/core/images/dashboard/tile/${this.id}.svg`);
  }

  allowed() {
    return AGN.Lib.Template.exists(this.#getTemplateName());
  }

  get $el() {
    if (!this._$el) {
      this._$el = AGN.Lib.Template.dom(this.#getTemplateName(), {
        tileName: this.name,
        tileType: this.type,
        overlay: this.getOverlay()
      });
    }
    return this._$el;
  }
  
  set $el($el) {
    this._$el = $el;
  }

  get position() {
    if (!this._position) {
      return {rows: [], cols: []};
    }
    return this._position;
  }

  set position(position) {
    this._position = position;
  }

  get beginCol() {
    return this.position.cols[0];
  }

  get beginRow() {
    return this.position.rows[0];
  }

  get endCol() {
    return this.position.cols[1];
  }

  get endRow() {
    return this.position.rows[1];
  }

  get isEmpty() {
    return this.id === DraggableTile.def.TILE.ID.EMPTY;
  }

  get isRegular() {
    return this.type === DraggableTile.def.TILE.TYPE.REGULAR;
  }

  get isTall() {
    return this.type === DraggableTile.def.TILE.TYPE.TALL;
  }

  get isWide() {
    return this.type === DraggableTile.def.TILE.TYPE.WIDE;
  }

  #getTemplateName() {
    return `dashboard-tile-${this.id}`;
  }

  getOverlay() {
    return AGN.Lib.Template.text('dashboard-tile-overlay');
  }
}
