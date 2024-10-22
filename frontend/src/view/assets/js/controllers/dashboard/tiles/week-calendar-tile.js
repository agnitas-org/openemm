(() => {
  const TileSize = AGN.Lib.Dashboard.Def.TileSize;

  class WeekCalendarTile extends AGN.Lib.Dashboard.XlCalendarTile {

    static ID = 'week-calendar';

    constructor(controller) {
      super(controller);
      this.variants = [TileSize.X_WIDE];
      this.size = this.variants[0];
    }

    get isWeekMode() {
      return true;
    }
  }

  AGN.Lib.Dashboard.WeekCalendarTile = WeekCalendarTile;
})();
