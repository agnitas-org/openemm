(() => {

  AGN.Lib.Template.register('schedule-time-item-add-btn', `
    <a href="#" class="btn btn-icon btn-primary" data-schedule-add-time-item data-tooltip="{{- t('defaults.add') }}">
      <i class="icon icon-plus"></i>
    </a>
  `);

  AGN.Lib.Template.register('schedule-time-item-remove-btn', `
    <a href="#" class="btn btn-icon btn-danger" data-schedule-remove-time-item data-tooltip="{{- t('defaults.clear') }}">
      <i class="icon icon-trash-alt"></i>
    </a>
  `);

  AGN.Lib.Template.register('schedule-time-item', `
    <div class="input-group" data-schedule-time-item>
        {{ const isExplicitTimeSelected = time.type === 'EXPLICIT'; }}
        <select class="form-control" data-schedule-time-interval>
            <option value="" {{ isExplicitTimeSelected ? print('selected') : print('') }}>
              {{- t('schedule.defineTime') }}
            </option>
            
            {{ intervals.forEach(interval => { }}
               <option value="{{- interval }}" {{ !isExplicitTimeSelected && time.value == interval ? print('selected') : print('') }}>
                {{- t('schedule.intervals.' + interval) }}
               </option> 
            {{ }); }}
        </select>

        <div class="input-icon-container flex-grow-1 {{ isExplicitTimeSelected ? print('') : print('hidden') }}">
          <input class="form-control js-timepicker" type="text" value="{{- isExplicitTimeSelected ? time.value : '' }}"
                 data-schedule-time data-timepicker-options="mask: 'h:s'"
                 placeholder="_" {{ isExplicitTimeSelected ? print('') : print('disabled') }}/>

          <i class="icon icon-clock" data-tooltip="{{- window.agnTimeZoneId }}"></i>
        </div>
    </div>
  `);

  AGN.Lib.Template.register('schedule-day-item', `
    <div class="row g-1 mb-1" data-schedule-day-item>
        <div class="col d-flex flex-column gap-1">
            <select class="form-control" size="1" data-schedule-day-of-week>
              {{ weekDays.forEach(weekDay => { }}
                  <option value="{{- weekDay }}" {{ weekDay == entry.weekDay ? print('selected') : print('') }}>
                    {{- t('schedule.weekdays.' + weekDay) }}
                  </option>
              {{ }); }}
            </select>

            <div class="d-flex flex-column gap-inherit" data-schedule-time-items>
              {{ _.each(entry.times, time => { }}
                  {{= AGN.Lib.Template.text('schedule-time-item', { time, intervals }) }}
              {{ }); }}
            </div>
        </div>
        <div class="col-auto">
          <a href="#" class="btn btn-icon btn-primary" data-add-day data-tooltip="{{- t('defaults.add') }}">
              <i class="icon icon-plus"></i>
          </a>
          <a href="#" class="btn btn-icon btn-danger hidden" data-remove-day data-tooltip="{{- t('defaults.clear') }}">
              <i class="icon icon-trash-alt"></i>
          </a>
        </div>
    </div>
  `);

  class DayItem {

    static DATA_ATTR_NAME = 'agn:scheduleBuilder:weeklyDayItem';
    static ALLOWED_INTERVALS = [1, 2, 3, 4, 6, 12];

    static defaults() {
      return {
        weekDay: '0',
        times: [
          {
            type: 'EXPLICIT',
            value: ''
          }
        ]
      };
    }

    static get($needle) {
      let $item;
      if ($needle.is('[data-schedule-day-item]')) {
        $item = $needle;
      } else {
        $item = $needle.closest('[data-schedule-day-item]');
      }

      return $item.data(DayItem.DATA_ATTR_NAME);
    }

    static create(settings, options = {}) {
      const $item = AGN.Lib.Template.dom('schedule-day-item', {
        entry: $.extend(DayItem.defaults(), options),
        intervals: DayItem.ALLOWED_INTERVALS,
        ... settings
      });

      new DayItem($item);
      return $item;
    }

    constructor($item, weekDays) {
      if (!$item.exists()) {
        $item = DayItem.create({ weekDays });
      }

      this.$el = $item;
      this.$el.data(DayItem.DATA_ATTR_NAME, this);
    }

    getSettings() {
      const weekDay = this.$el.find('[data-schedule-day-of-week]').val();

      const times = this.#getTimeItems$().map((i, item) => {
        const $item = $(item);
        const isIntervalSelection = this.#isTimeIntervalSelected($item);

        return {
          type: isIntervalSelection ? 'INTERVAL' : 'EXPLICIT',
          value: isIntervalSelection ? this.#getTimeInterval($item) : this.#getExplicitTime($item)
        }
      }).get();

      return {weekDay, times};
    }

    toggleTimePickerVisibility($needle) {
      const $timeItem = this.#getTimeItem$($needle);
      const isIntervalSelected = this.#isTimeIntervalSelected($timeItem);

      this.#getExplicitTime$($timeItem).prop('disabled', isIntervalSelected)
        .parent().toggleClass('hidden', isIntervalSelected);
    }

    toggleTimeItemsButtonsVisibility() {
      const $timeItems = this.#getTimeItems$();

      $timeItems.each((i, item) => {
        const $item = $(item);
        const $btnIcon = $item.find('.btn-icon');

        const $newBtn = AGN.Lib.Template.dom(
          $item.is(':last-child') ? 'schedule-time-item-add-btn' : 'schedule-time-item-remove-btn'
        );

        if ($btnIcon.exists()) {
          $btnIcon.replaceWith($newBtn);
        } else {
          $item.append($newBtn);
        }

        AGN.Lib.CoreInitializer.run('tooltip', $newBtn);
      })
    }

    addTimeItem(options = {}) {
      const $timeWrapper = AGN.Lib.Template.dom('schedule-time-item', {
        time: $.extend(DayItem.defaults().times[0], options),
        intervals: DayItem.ALLOWED_INTERVALS
      });

      this.$el.find('[data-schedule-time-items]').append($timeWrapper);
      AGN.runAll($timeWrapper);
      this.toggleTimeItemsButtonsVisibility();
    }

    removeTimeItem($needle) {
      this.#getTimeItem$($needle).remove();
      this.toggleTimeItemsButtonsVisibility();
    }

    getDayOfWeek() {
      return parseInt(this.$el.find('[data-schedule-day-of-week]').val());
    }

    disableDayOptions(values) {
      const $daySelect = this.$el.find('[data-schedule-day-of-week]');
      $daySelect.find('option').prop('disabled', false);
      AGN.Lib.Select.get($daySelect).disableOptions(values);
    }

    disableSelectedIntervalOptions() {
      const $timeItems = this.#getTimeItems$();
      const usedIntervals = $timeItems
        .filter((i, item) => this.#isTimeIntervalSelected($(item)))
        .map((i, item) => this.#getTimeInterval($(item)))
        .get();

      $timeItems.find('[data-schedule-time-interval]').each((i, select) => {
        const $select = $(select);
        $select.find('option').prop('disabled', false);

        const intervalsToDisable = usedIntervals.filter(interval => interval !== $select.val());
        AGN.Lib.Select.get($select).disableOptions(intervalsToDisable);
      });
    }

    #isTimeIntervalSelected($timeItem) {
      return !!this.#getTimeInterval($timeItem);
    }

    #getTimeInterval($timeItem) {
      return $timeItem.find('[data-schedule-time-interval]').val();
    }

    #getExplicitTime($timeItem) {
      return this.#getExplicitTime$($timeItem).val();
    }

    #getExplicitTime$($timeItem) {
      return $timeItem.find('[data-schedule-time]');
    }

    #getTimeItems$() {
      return this.$el.find('[data-schedule-time-item]');
    }

    #getTimeItem$($needle) {
      return $needle.closest('[data-schedule-time-item]');
    }

  }

  class WeeklyInterval extends AGN.Lib.Schedule.ScheduleInterval {

    getSettings() {
      const days = this.#hasDefaultSettings()
        ? []
        : this.#getAllDayItems().map(item => item.getSettings());

      return { days };
    }

    validate() {
      return this.#hasDefaultSettings() || super.validate();
    }

    drawSettings() {
      super.drawSettings();

      if (this.settings?.days?.length) {
        this.settings.days.map(options => DayItem.create({weekDays: this.getPossibleWeekDays()}, options))
          .forEach($dayItem => this.#addDayItem($dayItem));
      } else {
        this.#addEmptyDayItem();
      }

      this.#addActions();
    }

    getPossibleWeekDays() {
      return [0, ... super.getPossibleWeekDays()];
    }

    getSettingsHtml() {
      return `<label class="form-label">${t('schedule.daysAndTime')}</label>`;
    }

    #addActions() {
      AGN.Lib.Action.new({click: '[data-add-day]'}, () => {
        const dayItems = this.#getAllDayItems();
        const usedDays = dayItems.map(item => item.getDayOfWeek());
        const nextFreeDay = this.getPossibleWeekDays()
          .find(day => !usedDays.includes(day));

        this.#addDayItem(DayItem.create({weekDays: this.getPossibleWeekDays()}, {weekDay: nextFreeDay}));
      }, this.$container);

      AGN.Lib.Action.new({click: '[data-remove-day]'}, $el => {
        this.#deleteDayItem(DayItem.get($el));
      }, this.$container);

      AGN.Lib.Action.new({click: '[data-schedule-add-time-item]'}, $el => {
        const dayItem = DayItem.get($el);
        dayItem.addTimeItem();
        dayItem.disableSelectedIntervalOptions();
      }, this.$container);

      AGN.Lib.Action.new({click: '[data-schedule-remove-time-item]'}, $el => {
        const dayItem = DayItem.get($el);
        dayItem.removeTimeItem($el);
        dayItem.disableSelectedIntervalOptions();
      }, this.$container);

      AGN.Lib.Action.new({change: '[data-schedule-time-interval]'}, $el => {
        const dayItem = DayItem.get($el);
        dayItem.toggleTimePickerVisibility($el);
        dayItem.disableSelectedIntervalOptions();
      }, this.$container);

      AGN.Lib.Action.new({change: '[data-schedule-day-of-week]'}, () => {
        this.#updateDaysOptions();
      }, this.$container);

      AGN.Lib.Action.new({change: '[data-schedule-time-items]'}, () => this.validate(), this.$container);
    }

    #getAllDayItems() {
      return this.$container.find('[data-schedule-day-item]')
        .map((i, item) => DayItem.get($(item)))
        .get();
    }

    #deleteDayItem(dayItem) {
      this.$container.find(dayItem.$el).remove();
      this.#toggleDayButtonsVisibility();
      this.#updateDaysOptions();
    }

    #addEmptyDayItem() {
      this.#addDayItem(DayItem.create({weekDays: this.getPossibleWeekDays()}));
    }

    #addDayItem($dayItem) {
      this.$container.append($dayItem);

      AGN.runAll($dayItem);
      this.#toggleDayButtonsVisibility();

      const dayItem = DayItem.get($dayItem);
      dayItem.toggleTimeItemsButtonsVisibility();
      dayItem.disableSelectedIntervalOptions();

      this.#updateDaysOptions();
    }

    #updateDaysOptions() {
      const dayItems = this.#getAllDayItems();

      dayItems.forEach(item => {
        const optionsToDisable = dayItems
          .filter(dayItem => item !== dayItem)
          .map(dayItem => String(dayItem.getDayOfWeek()));

        item.disableDayOptions(optionsToDisable);
      });
    }

    #toggleDayButtonsVisibility() {
      const dayItems = this.#getAllDayItems();
      const dayItemsCount = dayItems.length;

      const $removeDayBtns = this.$container.find('[data-remove-day]').addClass('hidden');
      const $addDayBtns = this.$container.find('[data-add-day]').addClass('hidden');

      if (dayItemsCount > 1) {
        $removeDayBtns.slice(0, -1).removeClass('hidden');
      }

      if (dayItemsCount < this.getPossibleWeekDays().length) {
        $addDayBtns.last().removeClass('hidden');
      } else {
        $removeDayBtns.last().removeClass('hidden');
      }
    }

    #hasDefaultSettings() {
      const dayItems = this.#getAllDayItems();
      return dayItems.length === 1 && _.isEqual(dayItems[0].getSettings(), DayItem.defaults());
    }

  }

  AGN.Lib.Schedule.ScheduleWeeklyInterval = WeeklyInterval;

})();