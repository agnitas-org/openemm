class CalendarTile extends DraggableTile {

  constructor(controller) {
    super(controller);
    this.type = DraggableTile.def.TILE.TYPE.WIDE;
    this.variants = [{type: DraggableTile.def.TILE.TYPE.WIDE}, {type: DraggableTile.def.TILE.TYPE.TALL}]
    this._controller.addDomInitializer('dashboard-calendar', this.#domInitializer);
  }
  
  #domInitializer() {
    new AGN.Lib.Calendar(this.el, this.config.firstDayOfWeek, this.config.statisticsViewAllowed);
  }

  get id() {
    return DraggableTile.def.TILE.ID.CALENDAR;
  }
  
  remove() {
    super.remove();
    this.$el = undefined;
  }

  thumbnail(type) {
    const suffix = type === DraggableTile.def.TILE.TYPE.TALL ? '-tall' : '';
    return AGN.url(`/assets/core/images/dashboard/tile/${this.id}${suffix}.svg`);
  }
}
