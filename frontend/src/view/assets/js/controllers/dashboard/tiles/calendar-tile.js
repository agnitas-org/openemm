class CalendarTile extends DraggableTile {

  constructor(controller) {
    super(controller);
    this.type = DraggableTile.def.TILE.TYPE.WIDE;
    this.variants = [{type: DraggableTile.def.TILE.TYPE.WIDE}, {type: DraggableTile.def.TILE.TYPE.TALL}]
    this._controller.addDomInitializer('dashboard-calendar', this.#domInitializer);
  }
  
  #domInitializer() {
    const CalendarService = AGN.Lib.CalendarService;
    const Action = AGN.Lib.Action;
  
    CalendarService.setUp(this.config);
    
    AGN.Lib.DateFormat.getLocalizedShortWeekdays(navigator.language, this.config.firstDayOfWeek).forEach(weekDay => {
      $('#calendar-header').append(`<td><div class="calendar-cell">${weekDay}</div></td>`);
    });
    CalendarService.generateMonthCalendar(CalendarService.getCurMonth(), CalendarService.getCurYear());
    
    Action.new({'change': '#month_list, #month_list_year'}, function() {
      AGN.Lib.CalendarService.showDate();
    });
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
