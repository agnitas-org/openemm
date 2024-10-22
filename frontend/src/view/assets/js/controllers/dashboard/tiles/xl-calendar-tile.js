(() => {
  const Storage = AGN.Lib.Storage;
  const TileSize = AGN.Lib.Dashboard.Def.TileSize;
  const Label = AGN.Lib.Dashboard.XlCalendarLabel;
  const XlCalendar = AGN.Lib.Dashboard.XlCalendar;

  class XlCalendarTile extends AGN.Lib.Dashboard.DraggableTile {

    static ID = 'xl-calendar';

    constructor(controller) {
      super(controller);
      const tile = this;
      this.variants = [TileSize.X_LARGE];
      this.size = this.variants[0];
      this.#addInitializers(tile);
    }

    #addInitializers(tile) {
      this._controller.addDomInitializer(`dashboard-xl-calendar`, function () {
        const calendar = tile.calendar = new XlCalendar(this.el, this.config);
        this.addAction({change: 'toggle-xl-calendar-comments'}, $el => calendar.toggleComments(!$el.prop('checked')));
        this.addAction({change: 'toggle-xl-calendar-mode'}, $el => calendar.toggleMode($el.prop('checked')));
        this.addAction({dblclick: 'show-xl-calendar-comment'}, $el => Label.get($el).showModal());
        this.addAction({click: 'create-xl-calendar-comment'}, $el => calendar.createComment($el.data('date')));
        this.addAction({click: 'load-more-day-mailings'}, $el => calendar.showMoreMailings($el));
        this.addAction({change: 'flip-xl-calendar'}, () => calendar.flip());
        this.addAction({change: 'xl-calendar-change-year'}, () => calendar.changeYear());
        this.addAction({click: 'xl-calendar-today'}, () => calendar.jumpToday());
      });

      this._controller.addDomInitializer('xl-calendar-comment-modal', function () {
        const calendar = tile.calendar;
        this.addAction({click: 'save-xl-calendar-comment'}, $el => calendar.saveComment($el.data('comment-id')));
        this.addAction({click: 'delete-xl-calendar-comment'}, $el => calendar.removeComment($el.data('comment-id')));
      });
    }

    get templateOptions() {
      return {
        ...super.templateOptions,
        isWeekMode: this.isWeekMode,
        isUnsentMailingsPlannedType: this.isUnsentMailingsPlannedType,
        isCommentsShown: this.isCommentsShown
      };
    }

    get isWeekMode() {
      return Storage.get(XlCalendar.MODE_STORAGE_KEY) === 'week';
    }

    get isUnsentMailingsPlannedType() {
      return Storage.get(XlCalendar.UNSENT_MAILINGS_STORAGE_KEY) === 'planned';
    }

    get isCommentsShown() {
      return Storage.get(XlCalendar.SHOW_COMMENTS_STORAGE_KEY) !== false;
    }

    get templateName() {
      return `dashboard-tile-${XlCalendarTile.ID}`;
    }

    remove() {
      super.remove();
      this.$el = undefined;
    }
  }

  AGN.Lib.Dashboard.XlCalendarTile = XlCalendarTile;
})();
