(function () {
  const DateFormat = AGN.Lib.DateFormat;
  const Template = AGN.Lib.Template;
  
  const monthMailingsUrl = AGN.url('/calendar/mailings.action');
  const mailingsPopoverInfoUrl = AGN.url("/calendar/mailingsPopoverInfo.action");
  const mailingSettingsView = AGN.url('/mailing/:mailingId:/settings.action');
  const mailingStatView = AGN.url('/statistics/mailing/:mailingId:/view.action?init=true');

  class Calendar {
    constructor($el, firstDayOfWeek, statisticsViewAllowed) {
      this.$el = $el;
      this.firstDayOfWeek = firstDayOfWeek;
      this.statisticsViewAllowed = statisticsViewAllowed;
      this.#init();
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

    showMonth(month, year) {
      let date = new Date(year, month, 1);
      let monthDaysCount = this.daysInMonth(month + 1, year);

      let offset = date.getDay() - this.firstDayOfWeek;
      if (offset < 0) {
        offset = 7 + offset;
      }

      let weeksCount = Math.ceil((monthDaysCount + offset) / 7);
      let startDate = new Date(year, month, 1 - offset);
      let endDate = new Date(year, month, 1 + weeksCount * 7);

      date = new Date(startDate);
      for (let weekIndex = 0; weekIndex < weeksCount; weekIndex++) {
        const trId = 'row-' + (weekIndex + 1);
        const weekNumber = this.#getWeek(date);

        $('#calendar-container').append(`<tr id="${trId}"><td><div class="calendar-cell">${weekNumber}</div></td></tr>`);

        for (let index = 0; index < 7; index++) {
          const alienMonth = (date.getMonth() !== month);
          this.addDayToCalendar(date, trId, alienMonth);
          date.setDate(date.getDate() + 1);
        }
      }
      this.handleCalendarMailings(startDate, endDate);
      this.synchronizeMonthsList(month, year);
    }

    daysInMonth(month, year) {
      return new Date(year, month, 0).getDate();
    }

    removeCurrentCalendar() {
      $('#calendar-container tr:not(:first)').remove();
    }

    addDayToCalendar(date, trId, alienMonth) {
      const today = !alienMonth && this.isToday(date);
      const dateStr = this.dateToServiceFormat(new Date(date.getFullYear(), date.getMonth(), date.getDate()));
      const newId = 'day-' + dateStr;
      $('#' + trId).append(`<td id="${newId}"></td>`);
      $('#' + newId).append(`<div class="calendar-cell calendar-day ${today ? 'calendar-day--today' : ''} ${alienMonth ? 'calendar-day--alien' : ''}" id="label-${newId}" data-date="${dateStr}">${date.getDate().toString().padStart(2, '0')}</div>`);
    }

    isToday(date) {
      const curDate = new Date();
      curDate.setHours(0, 0, 0, 0);
      date.setHours(0, 0, 0, 0);
      return date.getTime() === curDate.getTime();
    }

    handleCalendarMailings(startDate, endDate) {
      $.get(monthMailingsUrl, {
        startDate: this.dateToServiceFormat(startDate),
        endDate: this.dateToServiceFormat(endDate)
      }).done(data => this.assignMailingsToCalendarDays(data));
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
        .done(mailings => mailings.forEach(mailing => this.createPopover(mailing)));
    }

    createPopover(mailing) {
      mailing.thumbnailUrl = AGN.url(this.getMailingThumbnailSrc(mailing.thumbnailComponentId, mailing.post));
      const content = Template.dom("calendar-mailing-popover-content", mailing);
      const $dayMailing = $(`.schedule__day-mailing[data-mailing-id="${mailing.mailingId}"]`);
      AGN.Lib.Popover.getOrCreate($dayMailing, {trigger: 'hover', html: true, content})
    }

    getMailingThumbnailSrc(previewComp, isPostType) {
      if (isPostType) {
        return "/assets/core/images/facelift/post_thumbnail.jpg";
      }
      if (previewComp) {
        return "/sc?compID=" + previewComp;
      }
      return "/assets/core/images/facelift/no_preview.svg";
    }

    getMailingLink(mailing) {
      return mailing.sent && mailing.workstatus !== 'mailing.status.test' && this.statisticsViewAllowed
        ? mailingStatView.replace(':mailingId:', mailing.mailingId)
        : mailingSettingsView.replace(':mailingId:', mailing.mailingId);
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

    addYearOption() {
      const $yearSelect = $("#month_list_year");
      const nextYear = parseInt($yearSelect.find("option:last").val(), 10) + 1;
      $yearSelect.append($("<option></option>").val(nextYear).text(nextYear));
    }

    lastYearSelected() {
      return $('#month_list_year option:selected').index() === $('#month_list_year option').length - 1;
    }

    synchronizeMonthsList(month, year) {
      let $monthListYear = $('#month_list_year');
      $monthListYear[0].selectedIndex = $('#month_list_year option[value = ' + year + ']')[0].index;
      let $monthList = $('#month_list');
      $monthList[0].selectedIndex = $('#month_list option[value = ' + month + ']')[0].index;
      AGN.runAll($monthListYear);
      AGN.runAll($monthList);
    }

    // helper methods
    pad(num, size) {
      let s = num + "";
      while (s.length < size) s = "0" + s;
      return s;
    }

    dateToServiceFormat(date) {
      let dd = this.pad(date.getDate(), 2);
      // Convert 0-based month number to 1-based
      let mm = this.pad((date.getMonth() + 1), 2);
      let yyyy = date.getFullYear();
      return dd + '-' + mm + '-' + yyyy;
    }

    /**
     * Function returns the number of week for current date
     */
    #getWeek(date) {
      // Create a copy of this date object
      var target = new Date(date.valueOf());
      var year = target.getFullYear();
      var month = target.getMonth();
      var day = target.getDate();
      // Make sure weeks won't be displayed wrong for US week setting
      if (this.firstDayOfWeek === 0) {
        day = day + 1;
      }
      month = month + 1;
      var a = Math.floor((14 - month) / 12);
      var y = year + 4800 - a;
      var m = month + 12 * a - 3;
      var J = day + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - Math.floor(y / 100) + Math.floor(y / 400) - 32045;
      var d4 = (((J + 31741 - (J % 7)) % 146097) % 36524) % 1461;
      var L = Math.floor(d4 / 1460);
      var d1 = ((d4 - L) % 365) + L;
      return Math.floor(d1 / 7) + 1;
    }
  }

  AGN.Lib.Calendar = Calendar;
})();
