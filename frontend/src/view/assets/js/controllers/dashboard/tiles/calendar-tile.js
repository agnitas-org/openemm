(() => {
  const Def = AGN.Lib.Dashboard.Def;
  const TileSize = Def.TileSize;

  class CalendarTile extends AGN.Lib.Dashboard.DraggableTile {

    static ID = "calendar";

    constructor(controller) {
      super(controller);
      this._controller.addDomInitializer('dashboard-calendar-tile', this.#domInitializer);
      this.variants = [TileSize.WIDE, TileSize.TALL];
      this.size = this.variants[0];
    }

    #domInitializer() {
      if (this.config.isUxUpdateRollback) {
        new AGN.Lib.Dashboard.Calendar(this.el, this.config);
      } else {
        new AGN.Lib.Dashboard.CalendarLight(this.el, this.config);
      }
    }

    remove() {
      super.remove();
      this.$el = undefined;
    }

    thumbnail(size) {
      return super.thumbnail() + (size === TileSize.TALL ? '-tall' : '');
    }
  }

  AGN.Lib.Dashboard.CalendarTile = CalendarTile;
})();
