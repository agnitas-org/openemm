AGN.Lib.Controller.new('target-group-view', function () {

  const Form = AGN.Lib.Form;

  let scheduleTimeTable;

  AGN.Lib.Action.new({'qb:invalidrules': '#targetgroup-querybuilder'}, function () {
    AGN.Lib.Messages.alert('querybuilder.errors.general');
  });

  this.addDomInitializer('target-group-view', function () {
    if (this.config.errorPositionDetails) {
      handleEqlErrorDetails(this.config.errorPositionDetails);
    }
  });

  this.addAction({change: 'toggle-editor-tab'}, function () {
    const $el = this.el;
    const isChecked = $el.is(':checked');
    $el.prop('checked', !isChecked); // change switch to previous state (it will be changed after form submit)

    const form = Form.get($el);
    if (!form.validate({skip_empty: true})) {
      return;
    }

    form.setValueOnce('viewFormat', isChecked ? 'EQL' : 'QUERY_BUILDER');
    form.submit('', {skip_empty: true});
  });

  this.addAction({submission: 'save-target'}, function () {
    Form.get(this.el)
      .submit(this.el.data('submit-type'))
      .done(resp => {
        if (typeof resp == 'object' && !resp.success) {
          handleEqlErrorDetails(resp.data);
          AGN.Lib.JsonMessages(resp.popups);
        }
      });
  });

  function handleEqlErrorDetails(details) {
    AGN.Lib.Editor.get($('#eql')).goToLine(details.line, details.column);
  }

  // Scheduler

  this.addDomInitializer('target-scheduler-init', function () {
    scheduleTimeTable?.clean();
    scheduleTimeTable = new ScheduleTimeTable($('#target-scheduler-block'));

    let period = [];
    if (this.config.intervalAsJson) {
      period = JSON.parse(this.config.intervalAsJson)
        .map(object => PeriodRow.deserialize(object));
    }

    if (period.length === 0) {
      scheduleTimeTable.addEmptyPeriodRow();
    } else {
      period.forEach(day => scheduleTimeTable.addRow(day));
    }

    Form.get(this.el).initFields();
  });

  this.addAction({click: 'activate-schedule'}, function () {
    const url = this.el.attr('href');
    const intervalAsJson = scheduleTimeTable.getSubmissionJson();

    $.post(url, {intervalAsJson}).done(resp => AGN.Lib.Page.render(resp));
  });

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
      new PeriodRow($row);

      $row.on('row:add-schedule', function () {
        //show delete buttons if more than one schedule lines
        const timeLines = $row.find('[data-schedule-time-wrapper]');
        if (timeLines.length > 1) {
          $row.find('[data-action="remove-schedule"]').closest('.input-group-addon').removeClass('hidden');
        }
      });

      return $row;
    }

    static deserialize(object) {
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
      let weekDay;
      let monthDay;

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

  }

  class ScheduleTimeTable {
    constructor($needle) {
      this.$container = $needle.find('table tbody');
    }

    clean() {
      this.$container.find('tr[data-schedule-row]').remove();
    }

    addRow($row) {
      if ($row) {
        this.$container.append($row);

        AGN.runAll($row);
        this.$container.trigger('scheduler:add-day', $row);
        $row.trigger('row:add-schedule', $row);
      }
    }

    addEmptyPeriodRow() {
      this.addRow(PeriodRow.create());
    }

    getAllPeriodRows() {
      const daysElements = this.$container.find('tr[data-schedule-row]');
      return _.map(daysElements, elem => PeriodRow.get($(elem)));
    }

    getSubmissionJson() {
      let scheduleJson = _.map(this.getAllPeriodRows(), row => row.toJson());
      return JSON.stringify(scheduleJson);
    }
  }

});
