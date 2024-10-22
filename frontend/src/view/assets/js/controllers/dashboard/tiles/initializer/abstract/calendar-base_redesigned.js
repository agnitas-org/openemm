(() => {
  const Template = AGN.Lib.Template;

  const mailingSettingsView = AGN.url('/mailing/:mailingId:/settings.action');
  const mailingStatView = AGN.url('/statistics/mailing/:mailingId:/view.action?init=true');

  class CalendarBase {

    constructor($el, conf) {
      this.$el = $el;
      this.firstDayOfWeek = conf.firstDayOfWeek;
      this.statisticsViewAllowed = conf.statisticsViewAllowed;
    }

    get $year() {
      throw this.notImplementedError("$year()");
    }

    get $month() {
      throw this.notImplementedError("$month()");
    }

    get removeCurrentCalendar() {
      throw this.notImplementedError("removeCurrentCalendar()");
    }

    isToday(date) {
      return moment(date).isSame(moment(), 'day');
    }

    showMonth(month, year) {
      this.removeCurrentCalendar();
      const monthDaysCount = this.daysInMonth(month + 1, year);
      let date = new Date(year, month, 1);

      let offset = date.getDay() - this.firstDayOfWeek;
      if (offset < 0) {
        offset = 7 + offset;
      }

      const weeksCount = Math.ceil((monthDaysCount + offset) / 7);
      const startDate = new Date(year, month, 1 - offset);
      const endDate = new Date(year, month, 1 + weeksCount * 7);

      date = new Date(startDate);
      for (let weekIndex = 0; weekIndex < weeksCount; weekIndex++) {
        const trId = 'row-' + (weekIndex + 1);

        this.addWeekNumber(date, trId);

        for (let index = 0; index < 7; index++) {
          const alien = date.getMonth() !== month;
          this.addDay(date, alien, trId);
          date.setDate(date.getDate() + 1);
        }
      }
      const startDateStr = this.dateToServiceFormat(startDate);
      const endDateStr = this.dateToServiceFormat(endDate);
      this.fillCalendar(startDateStr, endDateStr);
      this.synchronizeMonthsList(month, year);
    }

    addWeekNumber() {
      throw this.notImplementedError("addWeekNumber()")
    }
    addDay() {
      throw this.notImplementedError("addDay()")
    }

    fillCalendar() {
      throw this.notImplementedError("fillCalendar()")
    }

    notImplementedError(funcName) {
      return new Error(`${funcName} must be implemented in extended class`);
    }

    getMailingLink(mailing) {
      return mailing.sent && mailing.workstatus !== 'mailing.status.test' && this.statisticsViewAllowed
        ? mailingStatView.replace(':mailingId:', mailing.mailingId)
        : mailingSettingsView.replace(':mailingId:', mailing.mailingId);
    }

    addYearOption() {
      const $yearSelect = $("#month_list_year");
      const nextYear = parseInt($yearSelect.find("option:last").val(), 10) + 1;
      $yearSelect.append($("<option></option>").val(nextYear).text(nextYear));
    }

    lastYearSelected() {
      return $('#month_list_year option:selected').index() === $('#month_list_year option').length - 1;
    }

    daysInMonth(month, year) {
      return new Date(year, month, 0).getDate();
    }

    // should not trigger 'change' event in order to not provoke date change endless loop
    synchronizeMonthsList(month, year) {
      this.$year[0].selectedIndex =  this.$year.find(`option[value=${year}]`)[0].index;
      this.$month[0].selectedIndex = this.$month.find(`option[value=${month}]`)[0].index;
      AGN.runAll(this.$year);
      AGN.runAll(this.$month);
    }

    static pad(num, size) {
      let s = num + "";
      while (s.length < size) s = "0" + s;
      return s;
    }

    dateToServiceFormat(date) {
      let dd = CalendarBase.pad(date.getDate(), 2);
      // Convert 0-based month number to 1-based
      let mm = CalendarBase.pad((date.getMonth() + 1), 2);
      let yyyy = date.getFullYear();
      return dd + '-' + mm + '-' + yyyy;
    }

    /**
     * Function returns the number of week for current date
     */
    getWeek(date) {
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

    static createMailingPopover(mailing) {
      mailing.thumbnailUrl = AGN.url(CalendarBase.getMailingThumbnailSrc(mailing.thumbnailComponentId, mailing.post));
      const content = Template.dom("calendar-mailing-popover-content", mailing);
      AGN.Lib.Popover.getOrCreate(mailing.$el, {trigger: 'hover', html: true, content})
    }

    static getMailingThumbnailSrc(previewComp, isPostType) {
      if (isPostType) {
        return "/assets/core/images/facelift/post_thumbnail.jpg";
      }
      if (previewComp) {
        return "/sc?compID=" + previewComp;
      }
      return "/assets/core/images/facelift/no_preview.svg";
    }
  }

  AGN.Lib.Dashboard.CalendarBase = CalendarBase;
})();
