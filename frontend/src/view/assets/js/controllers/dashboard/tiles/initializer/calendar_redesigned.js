(() => {
  const DateFormat = AGN.Lib.DateFormat;
  const Template = AGN.Lib.Template;
  const CalendarBase = AGN.Lib.Dashboard.CalendarBase;

  const monthMailingsUrl = AGN.url('/calendar/mailingsLight.action');
  const mailingsPopoverInfoUrl = AGN.url("/calendar/mailingsPopoverInfo.action");

  class Calendar extends CalendarBase {
    constructor($el, conf) {
      super($el, conf);
      this.$el = $el;
      this.firstDayOfWeek = conf.firstDayOfWeek;
      this.statisticsViewAllowed = conf.statisticsViewAllowed;
      this.#init();
    }

    get $month() { // override of the base method, keep it
      return $('#month_list');
    }

    get $year() { // override of the  base method, keep it
      return $('#month_list_year');
    }

    #init() {
      const now = new Date();
      this.#displayWeekDays();
      this.showMonth(now.getMonth(), now.getFullYear());
      this.$el.on('change', '#month_list, #month_list_year', this.switchMonth.bind(this));
    }

    #displayWeekDays() {
      AGN.Lib.DateFormat.getLocalizedShortWeekdays(window.adminLocale, this.firstDayOfWeek).forEach(weekDay => {
        $('#calendar-header').append(`<td><div class="calendar-cell">${weekDay}</div></td>`);
      });
    }

    addWeekNumber(date, trId) { // override of the base method, keep it
      const weekNumber = this.getWeek(date);
      $('#calendar-container').append(`<tr id="${trId}"><td><div class="calendar-cell">${weekNumber}</div></td></tr>`);
    }

    addDay(date, alienMonth, trId) { // override of the base method, keep it
      const today = !alienMonth && this.isToday(date);
      const dateStr = this.dateToServiceFormat(new Date(date.getFullYear(), date.getMonth(), date.getDate()));
      const newId = 'day-' + dateStr;
      $('#' + trId).append(`<td id="${newId}"></td>`);
      $('#' + newId).append(`<div class="calendar-cell calendar-day ${today ? 'calendar-day--today' : ''} ${alienMonth ? 'calendar-day--alien' : ''}" id="label-${newId}" data-date="${dateStr}">${date.getDate().toString().padStart(2, '0')}</div>`);
    }

    fillCalendar(startDate, endDate) { // override of the base method, keep it
      $
        .get(monthMailingsUrl, {start: startDate, end: endDate})
        .done(data => this.assignMailingsToCalendarDays(data));
    }

    removeCurrentCalendar() {
      $('#calendar-container tr:not(:first)').remove();
    }

    assignMailingsToCalendarDays(mailings) {
      mailings.forEach(mailing => mailing.link = this.getMailingLink(mailing));
      const mailingsByDays = _.groupBy(mailings, 'sendDate');

      this.makeDaysSelectable(mailingsByDays);
      this.makeCurrentDayActive(mailingsByDays);
    }

    makeCurrentDayActive(mailingsBySendDate) {
      const currentDateStr = this.dateToServiceFormat(new Date());
      $('#day-' + currentDateStr).click(() => this.changeSelectedDay(currentDateStr, mailingsBySendDate[currentDateStr]));
      this.changeSelectedDay(currentDateStr, mailingsBySendDate[currentDateStr]);
    }

    makeDaysSelectable(mailingsByDays) {
      _.forEach($('.calendar-day'), calendarDay => {
        const $calendarDay = $(calendarDay);
        const dateStr = $calendarDay.data("date");
        const dayMailings = mailingsByDays[dateStr];
        if (!_.isEmpty(dayMailings)) {
          $('#label-day-' + dateStr).addClass('calendar-day--selectable');
        }
        $calendarDay.click(() => this.changeSelectedDay(dateStr, _.sortBy(dayMailings, 'sendTime')))
      });
    }

    changeSelectedDay(dateStr, dayMailings) {
      $('#calendar-container').find('.calendar-day--selected').removeClass('calendar-day--selected');
      $('#label-day-' + dateStr).addClass('calendar-day--selected');
      const dayDate = DateFormat.parseFormat(dateStr, "dd-MM-yy");
      const dayOfWeek = dayDate.toLocaleString(window.adminLocale, {weekday: 'long'})
      dateStr = AGN.Lib.DateFormat.format(dayDate, window.adminDateFormat);
      $('#calendar-tile-day-mailings').html(Template.dom("dashboard-schedule-day", {dayOfWeek, dateStr, dayMailings}));
      this.createPopovers(dayMailings);
    }

    createPopovers(dayMailings) {
      if (!dayMailings?.length) {
        return
      }
      const mailingIds = dayMailings.map(mailing => mailing.mailingId).join(',');
      $
        .get(mailingsPopoverInfoUrl, { mailingIds })
        .done(mailings => mailings
          .map(mailing => ({ ...mailing, $el: $(`.schedule__day-mailing[data-mailing-id="${mailing.mailingId}"]`)}))
          .forEach(mailing => CalendarBase.createMailingPopover(mailing)));
    }

    switchMonth() {
      const month = parseInt($('#month_list option:selected').val());
      const year = parseInt($('#month_list_year option:selected').val());
      
      this.removeCurrentCalendar();
      this.showMonth(month, year);
      if (this.lastYearSelected()) {
        this.addYearOption();
      }
    }
  }

  AGN.Lib.Dashboard.Calendar = Calendar;
})();
