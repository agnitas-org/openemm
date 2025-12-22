(() => {
  const Template = AGN.Lib.Template;
  const Storage = AGN.Lib.Storage;
  const Label = AGN.Lib.Dashboard.DashboardCalendarLabel;
  const CommentLabel = AGN.Lib.Dashboard.DashboardCalendarCommentLabel;
  const MailingLabel = AGN.Lib.Dashboard.DashboardCalendarMailingLabel;
  const AutoOptLabel = AGN.Lib.Dashboard.DashboardCalendarAutoOptLabel;
  const PushLabel = AGN.Lib.Dashboard.DashboardCalendarPushLabel;
  const CalendarBase = AGN.Lib.Dashboard.CalendarBase;
  const Def = AGN.Lib.Dashboard.Def;
  const WEEK_MS = 604800000;

  class DashboardCalendar extends CalendarBase {

    static DAY_CLASS = 'dashboard-calendar-day';
    static MODE_STORAGE_KEY = 'dashboard.calendar.mode';
    static SCROLL_STORAGE_KEY = 'dashboard.calendar.scroll';

    constructor($el, config) {
      super($el, config);

      this.today = new Date();
      this.dayMailingsLimit = config.showALlMailingsPerDay ? -1 : 6; // 6 = 5 allowed by default +1 to detect if more mailings are present
      this.commentsManager = new AGN.Lib.Dashboard.DashboardCalendarCommentsManager(this);
      this.periodPicker = new AGN.Lib.Dashboard.DashboardCalendarPeriodPicker(this);
      this.#initToggles();
      this.flip(this.today);
      this.$grid.css('background-color', 'var(--xl-calendar-cell-border-color)'); // table grid borders delayed in order to prevent displaying of the blank background during initialization
    }

    get daySelector() {
      return `.${DashboardCalendar.DAY_CLASS}`;
    }

    get $days() {
      return $(this.daySelector);
    }

    get $grid() {
      return $('#dashboard-calendar-grid');
    }

    get isScrollShown() {
      return Storage.get(DashboardCalendar.SCROLL_STORAGE_KEY);
    }

    set isScrollShown(show) {
      return Storage.set(DashboardCalendar.SCROLL_STORAGE_KEY, show);
    }

    get isWeekMode() {
      return Storage.get(DashboardCalendar.MODE_STORAGE_KEY) === 'week';
    }

    set mode(mode) {
      return Storage.set(DashboardCalendar.MODE_STORAGE_KEY, mode);
    }

    get selectedDate() {
      const date = new Date(this.periodPicker.year, this.periodPicker.month);

      if (date.getMonth() === this.today.getMonth() && date.getFullYear() === this.today.getFullYear()) {
        return this.today;
      }
      return date;
    }

    get $unsentList() {
      return $('#dashboard-calendar-unsent-tile').find('.tile-body');
    }

    get labels() {
      return this.$el
        .find(Label.SELECTOR)
        .map((i, el) => Label.get($(el)))
        .toArray();
    }

    #initToggles() {
      $('[data-action="switch-mode"]').prop('checked', this.isWeekMode).parent().show();
      $('#toggle-calendar-scroll').prop('checked', this.isScrollShown);
    }

    $day(date) {
      return this.$grid.find(`${this.daySelector}[data-date="${date}"]`)
    }

    removeCurrentCalendar() {
      this.$grid.empty();
    }

    showWeek(day, month, year) {
      this.removeCurrentCalendar();
      const date = new Date(year, month, day);
      date.setDate(day - date.getDay() - 1 + this.firstDayOfWeek);

      const startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
      startDate.setDate(startDate.getDate() + 1);
      if (month === 0 && day === 1 && this.getWeek(startDate) !== 1) {
        startDate.setTime(startDate.getTime() + WEEK_MS);
        date.setTime(date.getTime() + WEEK_MS);
      }
      for (let i = 0; i < 7; i++) {
        date.setDate(date.getDate() + 1);
        this.addDay(date, false);
      }
      this.fillCalendar(this.dateToServiceFormat(startDate), this.dateToServiceFormat(date));
    }

    addWeekNumber(date) { // override
      // nothing to do
    }

    addDay(date, alien) {
      const dateStr = this.dateToServiceFormat(new Date(date.getFullYear(), date.getMonth(), date.getDate()));
      const $cell = Template.dom('dashboard-calendar-day', { day: date.getDate(), date: dateStr})
      $cell.find('.dashboard-calendar-day')
        .toggleClass(`${DashboardCalendar.DAY_CLASS}--today`, this.isToday(date))
        .toggleClass(`${DashboardCalendar.DAY_CLASS}--alien`, alien);

      this.$grid.append($cell);

      if (!this.isWeekMode && $cell.is(':nth-child(7n+1)')) {
        $cell.append(`<div class="dashboard-calendar__week-number">${this.getWeek(date)}</div>`);
      }
    }

    fillCalendar(start, end) {
      this.#loadData(start, end);
      this.toggleScroll(this.isScrollShown);
      this.$days.droppable({accept: Label.SELECTOR});
    }

    #loadData(start, end) {
      const dayMailingsLimit = this.dayMailingsLimit;
      const loadComments = this.commentsManager.isCommentsShown;
      $
        .get(AGN.url('/calendar/labels.action'), { start, end, dayMailingsLimit, loadComments  })
        .done(data => {
          _.each(data.optimizations, autoOpt => AutoOptLabel.getOrCreate(autoOpt));
          _.each(data.pushes, push => PushLabel.getOrCreate(push));
          _.each(data.mailings, mailing => this.addMailingLabel(mailing, dayMailingsLimit));
          _.each(data.comments, comment => CommentLabel.getOrCreate(comment));
          this.commentsLoaded = loadComments;
          AGN.Lib.CoreInitializer.run('truncated-text-popover', this.$grid);
        });
    }

    flip(date = this.selectedDate) {
      if (this.isWeekMode) {
        this.showWeek(date.getDate(), date.getMonth(), date.getFullYear());
      } else {
        this.showMonth(date.getMonth(), date.getFullYear());
      }
      this.periodPicker.jumpToDate(date);
    }

    toggleMode(isWeekMode) {
      this.mode = isWeekMode ? 'week' : 'month';
      this.flip();
    }

    synchronizeMonthsList(month, year) {
      // nothing to do;
    }

    jumpToToday() {
      this.flip(this.today);
    }

    dropLabel(event, ui) {
      const $label = ui.draggable;
      Label.get($label).drop($(this));
    }

    initUnsentList(mailings, planned) {
      if (!planned) {
        this.$unsentList.droppable({accept: '[id^="dashboard-calendar-mailing-"]'});
      }
      mailings
        .map(mailing => this.asUnsentMailingData(mailing, planned))
        .forEach(mailing => new MailingLabel(mailing));
    }

    asUnsentMailingData(mailing, planned) {
      return {
        ...mailing,
        link: AGN.url(`/mailing/${mailing.id}/settings.action`),
        mediatype: mailing.mediatype?.toLowerCase(),
        labelId: `dashboard-calendar-unsent-${mailing.id}`,
        inUnsentList: true,
        planned,
        sent: false,
        sendTime: false,
      };
    }

    showMoreMailings($btn) {
      const date = $btn.closest(this.daySelector).data('date');
      this.getMailings(date, date, -1);
      $btn.remove();
    }

    getMailings(start, end, dayMailingsLimit) {
      $
        .get(AGN.url('/calendar/mailings.action'), {start, end, dayMailingsLimit})
        .done(mailings => _.each(mailings, mailing => this.addMailingLabel(mailing, dayMailingsLimit)));
    }

    $dayBody($day) {
      return $day.find(`${this.daySelector}__body`);
    }

    addMailingLabel(mailing, limit) {
      const mailingSendDate = moment(mailing.sendDate).tz(window.agnTimeZoneId);
      mailing.sendDate = mailingSendDate.format(Def.DATE_FORMAT);
      mailing.sendTime = mailingSendDate.format(Def.TIME_FORMAT);
      const $day = this.$day(mailing.sendDate);
      if (limit > 0 && $day.find('[id^="dashboard-calendar-mailing-"]').length >= limit - 1) {
        if (!$day.find('[data-action="show-more-mailings"]').exists()) {
          this.$dayBody($day).append(Template.text('dashboard-calendar-show-more-btn'));
        }
        return;
      }
      mailing.link = this.getMailingLink(mailing);
      MailingLabel.getOrCreate(mailing);
    }

    toggleScroll(show) {
      this.$days.map((i, day) => this.$dayBody($(day))).each((i, body) => {
        $(body).toggleClass('js-scrollable', !show)
        if (show) {
          AGN.Lib.Scrollbar.get($(body), false)?.destroy();
        }
      });

      if (!show) {
        AGN.Lib.Scrollbar.get(this.$grid, false)?.destroy();
      }
      AGN.Lib.CoreInitializer.run(['scrollable', 'tooltip'], $('#dashboard-calendar-table'));
      this.isScrollShown = show;
    }

    search(str) {
      this.labels.forEach(label => label.toggle(label.text?.toLowerCase().includes(str)));
    }

    getLabelsByType($day, type) {
      return $day
        .find(Label.SELECTOR).map(label => Label.get($(label)))
        .filter(label => label.type === type);
    }

    getLastLabelOfType($day, type) {
      return this.getLabelsByType($day, type).slice(-1)[0];
    }
  }

  AGN.Lib.Dashboard.DashboardCalendar = DashboardCalendar;
})();
