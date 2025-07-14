(() => {
  const MONTH_FORMATTER = new Intl.DateTimeFormat(window.adminLocale, { month: "long" });
  const SHORT_MONTH_FORMATTER = new Intl.DateTimeFormat(window.adminLocale, { month: "short" });
  const MONTH_YEAR_FORMATTER = new Intl.DateTimeFormat(window.adminLocale, { month: "long", year: "numeric" });

  class DashboardCalendarPeriodPicker {

    constructor(calendar) {
      this.calendar = calendar;
      this.initActions();
    }

    jumpToDate(date) {
      this.#drawYears(date.getFullYear());
      this.#drawMonths();
      this.#drawWeeks(date.getFullYear());
      this.$findYear(date.getFullYear()).prop('checked', true);
      this.$findMonth(date.getMonth()).prop('checked', true);
      this.$findWeek(this.calendar.getWeek(date)).prop('checked', true);
      this.updateCalendarTitle();
      if (this.calendar.isWeekMode) {
        this.showWeeks();
      } else {
        this.showMonths();
      }
    }

    initActions() {
      this.$pickerTitleBtn.on('click', () => this.widerPeriod());
      this.$arrowUp.on('click', () => this.prev());
      this.$arrowDown.on('click', () => this.next());
      $('#period-picker').on('click', () => this.#updateArrowIcons());
    }

    widerPeriod() {
      if (this.$monthsBox.is(":visible")) {
        this.showYears();
      }
      if (this.$weeksBox.is(":visible")) {
        this.showMonths();
      }
    }

    selectYear() {
      this.$months.prop('checked', false);
      this.#drawWeeks(this.year);
      this.showMonths();
    }

    selectMonth() {
      if (this.calendar.isWeekMode) {
        this.showWeeks();
      } else {
        this.dropdown.hide();
        this.calendar.flip();
      }
    }

    selectWeek() {
      this.dropdown.hide();
      this.calendar.flip(moment().locale(window.adminLocale).isoWeekYear(this.year).isoWeek(this.week).startOf('isoWeek').toDate());
    }

    showYears() {
      this.$yearsBox.show();
      this.$monthsBox.hide();
      this.$weeksBox.hide();
      this.$pickerTitleBtn.text(`${this.$years.first().val()}-${this.$years.last().val()}`);
    }

    showMonths() {
      this.$yearsBox.hide();
      this.$monthsBox.show();
      this.$weeksBox.hide();
      this.$pickerTitleBtn.text(this.year);
    }

    showWeeks() {
      this.$yearsBox.hide();
      this.$monthsBox.hide();
      this.$weeksBox.show();
      this.$pickerTitleBtn.text(MONTH_FORMATTER.format(new Date(this.year, this.month)));

      this.$weeks // reset
        .map((i, week) => $(week).next('label'))
        .each((i, week) => $(week).hide().find('span:not(:first)').removeClass('alien'));

      this.$getMonthWeeks(this.year, this.month).forEach(week => {
        $(week).next('label').show();
        $(week).find('+ label span:not(:first)').each((i, day_el) => {
          $(day_el).toggleClass("alien", parseInt($(day_el).data('month')) !== this.month);
        });
      });
    }

    $getMonthWeeks(year, month) {
      const weeks = new Set();
      const daysInMonth = moment().year(year).month(month).daysInMonth();

      for (let day = 1; day <= daysInMonth; day++) {
        const week = this.calendar.getWeek(new Date(year, month, day));
        if (!(month === 11 && week === 1) && !(month === 0 && week > 50)) {
          weeks.add(week);
        }
      }
      return [...weeks].map(week => this.$findWeek(week));
    }

    updateCalendarTitle() {
      this.calendar.$el.find('.tile-title > span').text(this.calendar.isWeekMode
        ? this.#getWeekText(this.year, this.week)
        : MONTH_YEAR_FORMATTER.format(new Date(this.year, this.month)));
    }

    #yearHtml(year) {
      return this.#btnHtml('year', year, year);
    }

    #monthHtml(month) {
      return this.#btnHtml('month', month, SHORT_MONTH_FORMATTER.format(new Date(2025, month)));
    }

    #weekHtml(week, content) {
      return this.#btnHtml('week', week, content);
    }

    #btnHtml(name, value, content) {
      return `
        <input type="radio" class="btn-check" name="${name}" value="${value}" id="${name}-${value}" autocomplete="off">
        <label class="btn" for="${name}-${value}">${content}</label>`;
    }

    get dropdown() {
      return bootstrap.Dropdown.getInstance($('#period-picker .dropdown-toggle'));
    }

    get year() {
      return parseInt(this.$yearsBox.find('input:checked').val());
    }

    get month() {
      return parseInt(this.$monthsBox.find('input:checked').val());
    }

    get week() {
      return parseInt(this.$weeksBox.find('input:checked').val());
    }

    get $yearsBox() {
      return this.$tile.find('.period-picker__years');
    }

    get $monthsBox() {
      return this.$tile.find('.period-picker__months');
    }

    get $weeksBox() {
      return this.$tile.find('.period-picker__weeks');
    }

    get $years() {
      return this.$yearsBox.find('input');
    }

    get $months() {
      return this.$monthsBox.find('input');
    }

    get $weeks() {
      return this.$weeksBox.find('input');
    }

    get $tile() {
      return $('.tile--period-picker');
    }

    get $pickerTitleBtn() {
      return this.$tile.find('.period-picker__title-btn');
    }

    get $arrowUp() {
      return this.$tile.find('.icon-caret-up');
    }

    get $arrowDown() {
      return this.$tile.find('.icon-caret-down');
    }

    next() {
      this.#step(true);
    }

    prev() {
      this.#step(false);
    }

    #step(next) {
      if (this.$yearsBox.is(":visible")) {
        this.#step12Years(next);
      }
      if (this.$monthsBox.is(":visible")) {
        this.#stepYear(next);
      }
      if (this.$weeksBox.is(":visible")) {
        this.#stepMonth(next);
      }
    }

    #step12Years(next) {
      this.#drawYears(parseInt(this.$years.first().val()) + (next ? 12 : -12));
      this.$pickerTitleBtn.text(`${this.$years.first().val()}-${this.$years.last().val()}`);
      this.$years.prop('checked', false);
    }

    #stepYear(next) {
      const nextYear = this.year + (next ?  1 : -1);
      let $nextYear = this.$findYear(nextYear);
      if (!$nextYear.length) {
        this.#drawYears(next ? nextYear : this.year - 12);
        $nextYear = this.$findYear(nextYear);
      }
      $nextYear.prop('checked', true);
      this.selectYear();
    }

    #stepMonth(next) {
      this.$findMonth(this.month + (next ? 1 : -1)).prop('checked', true);
      this.selectMonth();
    }

    $findYear(year) {
      return this.$yearsBox.find(`[value="${year}"]`);
    }

    $findMonth(month) {
      return this.$monthsBox.find(`[value="${month}"]`);
    }

    $findWeek(week) {
      return this.$weeksBox.find(`[value="${week}"]`);
    }

    #getWeekText(year, weekNumber) {
      const startOfWeek = moment().locale(window.adminLocale).isoWeekYear(year).isoWeek(weekNumber).startOf('isoWeek');
      const endOfWeek = moment(startOfWeek).endOf('isoWeek');
      const options = { year: 'numeric', month: '2-digit', day: '2-digit' };
      const startFormatted = new Intl.DateTimeFormat(window.adminLocale, options).format(startOfWeek.toDate());
      const endFormatted = new Intl.DateTimeFormat(window.adminLocale, options).format(endOfWeek.toDate());
      const isoYear = startOfWeek.isoWeekYear();
      return `${startFormatted} - ${endFormatted} (${t('workflow.defaults.week')} ${weekNumber.toString().padStart(2, '0')}, ${isoYear})`;
    }

    #drawYears(startYear) {
      startYear = Math.max(Math.min(startYear, this.calendar.today.getFullYear()), this.calendar.config.minYear);
      this.$yearsBox.empty();
      for (let year = startYear; year < startYear + 12; year++) {
        this.$yearsBox.append(this.#yearHtml(year));
      }
      this.$years.on('click', () => this.selectYear());
    }

    #drawMonths() {
      this.$monthsBox.empty();
      Array.from({length: 12}, (j, month) => this.$monthsBox.append(this.#monthHtml(month)));
      this.$months.on('click', () => this.selectMonth());
    }

    #drawWeeks(year) {
      this.$weeksBox.empty();

      const firstWeekDay = new Date(year, 0, 1 - new Date(year, 0, 1).getDay() + this.calendar.firstDayOfWeek);
      if (this.calendar.getWeek(firstWeekDay) !== 1) {
        firstWeekDay.setTime(firstWeekDay.getTime() + 604800000);
      }
      do {
        let weekNumber = this.calendar.getWeek(firstWeekDay);
        const $weekRow = $(`<div value="${weekNumber}">`);
        $weekRow.append($("<span>").text(weekNumber));
        for (let i = 0; i < 7; i++) {
          $weekRow.append($(`<span data-month="${firstWeekDay.getMonth()}">${firstWeekDay.getDate()}</span>`));
          firstWeekDay.setDate(firstWeekDay.getDate() + 1);
        }
        this.$weeksBox.append(this.#weekHtml(weekNumber, $weekRow.html()));
      } while ((this.calendar.getWeek(firstWeekDay) !== 1) && (firstWeekDay.getFullYear() === year));

      this.$weeks.on('click', () => this.selectWeek());
    }

    #updateArrowIcons() {
      this.$arrowDown.toggle(this.#shownArrowDown());
      this.$arrowUp.toggle(this.#shownArrowUp());
    }

    #shownArrowDown() {
      if (this.$yearsBox.is(":visible")) {
        return parseInt(this.$years.last().val()) < this.calendar.today.getFullYear() + 11;
      }
      if (this.$monthsBox.is(":visible")) {
        return this.year < this.calendar.today.getFullYear() + 11;
      }
      if (this.$weeksBox.is(":visible")) {
        return this.month < 11;
      }
      return false;
    }

    #shownArrowUp() {
      if (this.$yearsBox.is(":visible")) {
        return parseInt(this.$years.first().val()) > this.calendar.config.minYear;
      }
      if (this.$monthsBox.is(":visible")) {
        return this.year > this.calendar.config.minYear;
      }
      if (this.$weeksBox.is(":visible")) {
        return this.month > 0;
      }
      return false;
    }
  }

  AGN.Lib.Dashboard.DashboardCalendarPeriodPicker = DashboardCalendarPeriodPicker;
})();
