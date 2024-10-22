(() => {
  const Def =  AGN.Lib.Dashboard.Def;
  const TileSize = Def.TileSize;

  class DraggableTile {

    constructor(controller) {
      if (new.target === DraggableTile) {
        throw new TypeError("Cannot construct Abstract Tile instances directly!");
      }
      this._controller = controller;
      this.variants = [TileSize.REGULAR];
      this.size = this.variants[0];
    }

    displayIn($place) {
      if ($place?.is('.draggable-tile')) {
        $place.replaceWith(this.$el);
      } else {
        $place.append(this.$el);
      }
      this.afterTileAdded();
    }

    afterTileAdded() {
      this._$el.data('tile', this);
      this.$el.trigger('agn:grid-modified', [this]);
      AGN.runAll(this.$el);
    }

    insertAfter(tile) {
      tile.$el.after(this.$el);
      this.afterTileAdded()
    }

    insertBefore(tile) {
      tile.$el.before(this.$el);
      this.afterTileAdded();
    }

    remove() {
      const $container = this.$container;
      this.$el.remove();
      $container.trigger('agn:grid-modified');
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

    get $container() {
      return this.$el.parent();
    }

    get id() {
      return this.constructor.ID;
    }

    get name() {
      return t(`dashboard.tile.${this.id}`);
    }

    thumbnail() {
      return AGN.url(`/assets/core/images/dashboard/dashboard-tile-sprite.svg#${this.id}`);
    }

    allowed() {
      return AGN.Lib.Template.exists(this.templateName);
    }

    get $el() {
      if (!this._$el) {
        this._$el = AGN.Lib.Template.dom(this.templateName, this.templateOptions);
      }
      return this._$el;
    }

    get templateOptions() {
      return {
        tileName: this.name,
        tileSize: this.size.name,
        overlay: this.getOverlay()
      };
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
      return this.id === AGN.Lib.Dashboard.EmptyTile.ID;
    }

    get isRegular() {
      return this.size === TileSize.REGULAR;
    }

    get isTall() {
      return this.size === TileSize.TALL;
    }

    get isXl() {
      return this.size === TileSize.X_LARGE;
    }

    get isWide() {
      return this.size === TileSize.WIDE;
    }

    get isXWide() {
      return this.size === TileSize.X_WIDE;
    }

    get templateName() {
      return `dashboard-tile-${this.id}`;
    }

    toggleHighlight(isHovered) {
      this.$el.toggleClass('draggable-accept--hovered', isHovered);
    }

    getOverlay() {
      return AGN.Lib.Template.text('dashboard-tile-overlay');
    }

    static get($tile) {
      return $tile.data('tile');
    }
  }

  AGN.Lib.Dashboard.DraggableTile = DraggableTile;
})();
