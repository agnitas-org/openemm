class PlanningTile extends DraggableTile {

  constructor(controller) {
    super(controller);
    const instance = this;

    this._controller.addDomInitializer('dashboard-planning', function () {
      $.i18n.load(this.config.translations);
      instance.#setupDatePickers();
      instance.#displayData(new Date(), new Date());
    });
    this.type = DraggableTile.def.TILE.TYPE.TALL;
    this.variants = [{type: DraggableTile.def.TILE.TYPE.TALL}];
  }

  get id() {
    return DraggableTile.def.TILE.ID.PLANNING;
  }

  #setupDatePickers() {
    const $startDate = this.$el.find('#schedule-start-date');
    const $endDate = this.$el.find('#schedule-end-date');

    this.#setupDatePicker($startDate, $startDate, $endDate);
    this.#setupDatePicker($endDate, $startDate, $endDate);
  }

  #setupDatePicker($datePicker, $startDate, $endDate) {
    const instance = this;

    $datePicker.on('change', function () {
      const startDate = $startDate.datepicker('getDate');
      const endDate = $endDate.datepicker('getDate');

      if (startDate && endDate) {
        instance.#displayData(startDate, endDate);
      }
    });

    $datePicker.datepicker('option', 'onChangeMonthYear', function (year, month, instance) {
      const $this = $(this);
      const [minYear, maxYear] = $this.datepicker('option', 'yearRange').split(':');

      if (parseInt(year) === parseInt(maxYear)) {
        const newYearRange = `${minYear}:${parseInt(maxYear) + 1}`;
        const $anotherDatepicker = $this.is($startDate) ? $endDate : $startDate;

        instance.settings.yearRange = newYearRange;
        $anotherDatepicker.datepicker('option', 'yearRange', newYearRange);
      }
    });
  }

  #displayData(startDate, endDate) {
    this.#fetchData(startDate, endDate, window.adminDateFormat).then(data => {
      let content = '';
      if (data.length) {
        content = `<div class="grid w-100" style="--bs-columns: 1">${this.#handleMailings(data)}</div>`;
      } else {
        content = AGN.Lib.Template.text("notification-info", {message: t('dashboard.schedule.noMailings')});
      }

      const $dataContainer = this.$el.find('#schedule-data-container');
      $dataContainer.html(content);
      AGN.runAll($dataContainer);
    });
  }

  #fetchData(startDate, endDate, dateFormat) {
    return new Promise((resolve, reject) => {
      $.get(AGN.url('/dashboard/scheduledMailings.action'), {
        startDate: AGN.Lib.DateFormat.format(startDate, dateFormat),
        endDate: AGN.Lib.DateFormat.format(endDate, dateFormat)
      })
        .done(data => resolve(data))
        .fail(error => reject(error));
    });
  }

  #handleMailings(data) {
    _.each(data, (item) => item.link = AGN.url(`/mailing/send/${item.id}/view.action`));
    const groupedData = _.groupBy(data, 'sendDate');

    const sortedData = _.sortBy(groupedData, (group) => group[0].maildropSendDate) // sort by send date
      .map((group) => _.sortBy(group, 'sendTime')); // sort by send time

    return sortedData.reduce((result, dayMailings) => {
      const date = new Date(dayMailings[0].maildropSendDate);
      const dayOfWeek = date.toLocaleString(window.adminLocale, {weekday: 'long'});
      const dateStr = dayMailings[0].sendDate;
      const stringItem = AGN.Lib.Template.text('dashboard-schedule-day', {dayOfWeek, dateStr, dayMailings});
      return result + stringItem;
    }, '');
  }
}
