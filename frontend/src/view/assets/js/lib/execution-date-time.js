(() => {

  // TODO: GWUA-6633: Remove redundant code when removing 'migration.import.interval' permission

  class TimeSchedule {

    static defaults() {
      return [{
        active: false,
        time: ''
      }];
    }

    static create(scheduleElement, extended) {
      scheduleElement = $.extend(TimeSchedule.defaults()[0], scheduleElement);
      return AGN.Lib.Template.dom(
        extended ? 'schedule-extended-time-wrapper' : 'schedule-time-wrapper',
        {scheduleElement}
      );
    }

    static get($needle) {
      return $needle.closest('[data-schedule-time-wrapper]');
    }

  }

  class DayRow {

    constructor($row) {
      if (!$row.exists()) {
        $row = DayRow.create({});
      }

      this.$rowWrapper = $row;
    }

    static defaults() {
      return {
        weekDay: '',
        scheduledInterval: {
          active: false,
          interval: ''
        },
        scheduledTime: [
          {
            active: false,
            time: ''
          }
        ]
      };
    }

    static get($needle) {
      let $row;
      if ($needle.is('tr[data-schedule-row]')) {
        $row = $needle;
      } else {
        $row = $needle.closest('tr[data-schedule-row]');
      }
      return new DayRow($row);
    }

    static create(entry) {
      entry = $.extend(DayRow.defaults(), entry);

      const $row = AGN.Lib.Template.dom('schedule-day-row', {entry});
      const dayRow = new DayRow($row);

      $row.on('row:add-schedule row:remove-schedule', () => {
        const rowsCount = $row.find('[data-schedule-time-wrapper]').length;

        const $removeScheduleBtns = $row.find('[data-action="remove-schedule"]');
        const $addScheduleBtns = $row.find('[data-action="add-schedule"]');

        $removeScheduleBtns.addClass('hidden');
        $addScheduleBtns.addClass('hidden');

        if (rowsCount > 1) {
          $removeScheduleBtns.slice(0, -1).removeClass('hidden');
        }

        $addScheduleBtns.last().removeClass('hidden');
      });

      $row.find('[data-schedule-interval]').on('change', function () {
        const isIntervalSelection = dayRow.isIntervalSelection();
        const $timeRows = $row.find('[data-schedule-time-wrapper]');

        $timeRows.find('.btn').toggleClass('disabled', isIntervalSelection);
        $timeRows.find(':input').prop('disabled', isIntervalSelection);

        if (!isIntervalSelection && !$timeRows.exists()) {
          dayRow.addSchedule();
        }
      });

      return $row;
    }

    static deserialize(data) {
      // if not presented at least one active entry -> skip
      if (!data.scheduledInterval.active && !data.scheduledTime.some(time => time.active)) {
        return null;
      }

      return DayRow.create(data);
    }

    toJson() {
      const weekDay = this.$rowWrapper.find('[data-schedule-dayOfTheWeek]').val();

      const isIntervalSelection = this.isIntervalSelection();

      const scheduledTime = _.map(this.$rowWrapper.find('[data-schedule-time-wrapper]'), scheduleTime => {
        const time = $(scheduleTime).find('[data-schedule-time]').val();
        return {
          active: !isIntervalSelection && !!time,
          time: time
        }
      });

      const scheduledInterval = {
        active: isIntervalSelection,
        interval: this.$rowWrapper.find('[data-schedule-interval]').val()
      };

      return $.extend(DayRow.defaults(), {
        weekDay: weekDay,
        scheduledTime: scheduledTime,
        scheduledInterval: scheduledInterval
      });
    }

    isIntervalSelection($row) {
      return this.$rowWrapper.find('[data-schedule-interval]').val() !== '0';
    }

    addSchedule(scheduleElement) {
      const $timeWrapper = TimeSchedule.create(scheduleElement, true);

      this.$rowWrapper.find('[data-schedule-time-container]').append($timeWrapper);
      AGN.runAll($timeWrapper);
      this.$rowWrapper.trigger('row:add-schedule');
    }

    removeSchedule($needle) {
      TimeSchedule.get($needle).remove();
      this.$rowWrapper.trigger('row:remove-schedule');
    }

    getAllScheduleTimes() {
      const times = [];

      this.toJson().scheduledTime.forEach(time => {
        if (time.active) {
          times.push(parseInt(time.time.replace(':', '')));
        }
      });

      return times;
    }

    isValidTimeIntervals() {
      const times = this.getAllScheduleTimes();
      return new ScheduleValidation().valid(times);
    }

    validateTimeIntervals() {
      if (this.isValidTimeIntervals()) {
        this.hideTimeScheduleErrors();
        return true;
      }

      this.showTimeScheduleErrors(t('error.delay.60'));
      return false;
    }

    showTimeScheduleErrors(errorMsg) {
      const timeWrappers = this.$rowWrapper.find('[data-schedule-time-wrapper]');
      const $errorBlock = $(`<div class="form-control-feedback-message">${errorMsg}</div>`);

      _.each(timeWrappers, time => {
        const $time = $(time);
        if (!$time.find('.form-control-feedback-message').exists()) {
          $time.find('[data-schedule-time]').parent().append($errorBlock.clone());
          $time.addClass('has-alert has-feedback');
        }
      });
    }

    hideTimeScheduleErrors() {
      const timeWrapper = this.$rowWrapper.find('[data-schedule-time-wrapper]');

      timeWrapper.find('.form-control-feedback-message').remove();
      timeWrapper.removeClass('has-alert has-feedback');
    }
  }

  class PeriodRow {

    constructor($row) {
      if (!$row.exists()) {
        $row = PeriodRow.create({});
      }

      this.$rowWrapper = $row;
    }

    static defaults() {
      return {
        type: '1',
        weekDay: '',
        monthDay: '',
        scheduledTime: [
          {
            active: false,
            time: ''
          }
        ]
      };
    }

    static get($needle) {
      let $row;
      if ($needle.is('tr[data-schedule-row]')) {
        $row = $needle;
      } else {
        $row = $needle.closest('tr[data-schedule-row]');
      }
      return new PeriodRow($row);
    }

    static create(entry) {
      entry = $.extend(PeriodRow.defaults(), entry);
      const $row = AGN.Lib.Template.dom('schedule-period-row', {entry: entry});
      const periodRow = new PeriodRow($row);

      $row.on('row:add-schedule', function () {
        //show delete buttons if more than one schedule lines
        const timeLines = $row.find('[data-schedule-time-wrapper]');
        if (timeLines.length > 1) {
          $row.find('[data-action="remove-schedule"]').closest('.input-group-addon').removeClass('hidden');
        }
      });
      $row.on('row:remove-schedule', function () {
        //hide delete button if less than 2 schedule lines
        const timeLines = $row.find('[data-schedule-time-wrapper]');
        if (timeLines.length < 2) {
          $row.find('[data-action="remove-schedule"]').closest('.input-group-addon').addClass('hidden');
        }

        if (timeLines.length === 0) {
          periodRow.addSchedule();
        }
      });

      return $row;
    }

    static deserialize(object, type) {
      const data = $.extend(PeriodRow.defaults(), object);
      data.type = PeriodRow._convertTypeToInt(object.type);
      data.monthDay = PeriodRow._convertMonthToInt(object.monthDay);
      return PeriodRow.create(data);
    }

    static _convertTypeToInt(typeEnumName) {
      switch (typeEnumName) {
        case 'TYPE_MONTHLY':
          return 2;
        case  'TYPE_WEEKLY':
        default:
          return 1;
      }
    }

    static _convertMonthToInt(monthEnumName) {
      switch (monthEnumName) {
        case 'TYPE_MONTHLY_FIRST':
          return 1;
        case  'TYPE_MONTHLY_15TH':
          return 15;
        case  'TYPE_MONTHLY_LAST':
          return 99;
        default:
          return 1;
      }
    }

    toJson() {
      const type = this.$rowWrapper.find('[data-schedule-type]').val();
      let weekDay = undefined;
      let monthDay = undefined;
      if (type == 'TYPE_MONTHLY') {
        monthDay = this.$rowWrapper.find('[data-schedule-day-of-month]').val();
      } else {
        weekDay = this.$rowWrapper.find('[data-schedule-day-of-week]').val();
      }

      const scheduledTime = _.map(this.$rowWrapper.find('[data-schedule-time-wrapper]'), scheduleTime => {
        const $timeCheckbox = $(scheduleTime).find('.time-checkbox');

        let active = true;
        if ($timeCheckbox.exists()) {
          active = !$timeCheckbox.is(':disabled') && $timeCheckbox.is(':checked');
        }

        return {
          active: active,
          time: $(scheduleTime).find('[data-schedule-time]').val()
        }
      });

      return $.extend(PeriodRow.defaults(), {
        type: type,
        weekDay: weekDay,
        monthDay: monthDay,
        scheduledTime: scheduledTime
      });
    }

    addSchedule(scheduleElement) {
      const $timeWrapper = TimeSchedule.create(scheduleElement);

      this.$rowWrapper.find('[data-schedule-time-container]').append($timeWrapper);
      AGN.runAll($timeWrapper);
      this.$rowWrapper.trigger('row:add-schedule');
    }

    removeSchedule($needle) {
      TimeSchedule.get($needle).remove();
      this.$rowWrapper.trigger('row:remove-schedule');
    }

  }

  class ScheduleTimeTable {
    constructor($needle) {
      this.$container = $needle.find('table tbody');

      this.$container.on('scheduler:add-day scheduler:remove-day', () => {
        const rowsCount = this.$container.find('[data-schedule-row]').length;

        const $removeDayBtns = this.$container.find('[data-action="remove-day"]');
        const $addDayBtns = this.$container.find('[data-action="add-day"]');

        $removeDayBtns.addClass('hidden');
        $addDayBtns.addClass('hidden');

        if (rowsCount > 1) {
          $removeDayBtns.slice(0, -1).removeClass('hidden');
        }

        $addDayBtns.last().removeClass('hidden');
      });
    }

    clean() {
      this.$container.find('tr[data-schedule-row]').remove();
    }

    deleteRow(row) {
      this.deleteRowByNeedle(row.$rowWrapper);
    }

    deleteRowByNeedle($row) {
      this.$container.find($row).remove();
      this.$container.trigger('scheduler:remove-day');
    }

    addRow($row) {
      if ($row) {
        this.$container.append($row);

        AGN.runAll($row);
        this.$container.trigger('scheduler:add-day', $row);
        $row.trigger('row:add-schedule', $row);
      }
    }

    addEmptyDayRow() {
      this.addRow(DayRow.create());
    }

    addEmptyPeriodRow() {
      this.addRow(PeriodRow.create());
    }

    getAllDayRows() {
      const daysElements = this.$container.find('tr[data-schedule-row]');
      return _.map(daysElements, elem => DayRow.get($(elem)));
    }

    getAllPeriodRows() {
      const daysElements = this.$container.find('tr[data-schedule-row]');
      return _.map(daysElements, elem => PeriodRow.get($(elem)));
    }

    getSubmissionJson(type) {
      let scheduleJson = {};
      if (type == 'day') {
        scheduleJson = _.map(this.getAllDayRows(), row => row.toJson());
      } else if (type == 'period') {
        scheduleJson = _.map(this.getAllPeriodRows(), row => row.toJson());
      }

      return JSON.stringify(scheduleJson);
    }
  }

  class ScheduleValidation {
    constructor(minutes) {
      let min = 100; // 100 is interval of one hour
      if (minutes) {
        min = Number(minutes);
      }

      this.intervalPad = min;
    }

    static hasCorrectInterval(time, index, times) {
      const interval = this;
      let isCorrect = true;
      times.splice(index, 1);
      times.forEach(element => {
        if (Math.abs(element - time) < interval) {
          isCorrect = false;
        }
      });
      return isCorrect;
    }

    valid(times) {
      const interval = this.intervalPad;
      return times.every(ScheduleValidation.hasCorrectInterval, interval);
    }
  }

  AGN.Lib.ScheduleTimeTable = ScheduleTimeTable;
  AGN.Lib.ScheduleTimeTable.DayRow = DayRow;
  AGN.Lib.ScheduleTimeTable.PeriodRow = PeriodRow;

})();