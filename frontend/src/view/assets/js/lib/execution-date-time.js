(function() {
    function TimeSchedule() {};

    TimeSchedule.defaults = function() {
      return [{
        active: false,
        time: ''
      }];
    };

    TimeSchedule.create = function(scheduleElement, extended) {
      scheduleElement = $.extend(TimeSchedule.defaults()[0], scheduleElement);
      if (extended) {
        return AGN.Lib.Template.dom('schedule-extended-time-wrapper', {scheduleElement: scheduleElement});
      } else {
        return AGN.Lib.Template.dom('schedule-time-wrapper', {scheduleElement: scheduleElement});
      }
    };

    TimeSchedule.get = function($needle) {
      return $needle.closest('.schedule-time-wrapper');
    };

    function DayRow($row) {
      var self = this;
      if (!$row.exists()) {
        $row = DayRow.create({});
      }

      self.$rowWrapper = $row;
    }

    DayRow.prototype.toJson = function() {
      var self = this;

      var weekDay = self.$rowWrapper.find('select[name=dayOfTheWeek]').val();

      var scheduledTime = _.map(self.$rowWrapper.find('.schedule-time-wrapper'), function(scheduleTime) {
        var $timeCheckbox = $(scheduleTime).find('.time-checkbox');
        var active = !$timeCheckbox.is(':disabled') && $timeCheckbox.is(':checked'),
          time = $(scheduleTime).find('.time-input').val();

        return {
          active: active,
          time: time
        }
      });

      var $hourCheckbox = self.$rowWrapper.find('.hour-checkbox');
      var scheduledInterval = {
        active: $hourCheckbox.is(':checked') && !$hourCheckbox.is(':disabled'),
        interval: self.$rowWrapper.find('select.hour-select').val()
      };

       return $.extend(DayRow.defaults(), {
        weekDay: weekDay,
        scheduledTime: scheduledTime,
        scheduledInterval: scheduledInterval
      });
    };

    DayRow.prototype.addSchedule = function(scheduleElement) {
        var self = this,
          wrapper = self.$rowWrapper;

        var $timeWrapper = TimeSchedule.create(scheduleElement, true);

        wrapper.find('.schedule-settings-wrapper').append($timeWrapper);

        AGN.runAll($timeWrapper);
        wrapper.trigger('row:add-schedule');
    };

    DayRow.prototype.removeSchedule = function($needle) {
        TimeSchedule.get($needle).remove();
        this.$rowWrapper.trigger('row:remove-schedule');
    };

    DayRow.prototype.hourCheckboxToggle = function(el) {
        var toDisable = el.attr('disables'),
            checked = el.is(':checked');

        this.$rowWrapper.find(toDisable).prop('disabled', checked);
    };

    DayRow.prototype.timeCheckboxToggle = function(el) {
        var toDisable = el.attr('disables'),
            checked = el.is(':checked'),
            $timeCheckboxes = this.$rowWrapper.find('.time-checkbox'),
            $hourCheckbox = this.$rowWrapper.find('.hour-checkbox'),
            hasChecked;

        for (var i = 0; i < $timeCheckboxes.length; i++) {
            var plainCheckbox = $timeCheckboxes[i];
            if (el[0] !== plainCheckbox && $(plainCheckbox).is(':checked')) {
                hasChecked = true;
                break;
            }
        }
        if ((!$hourCheckbox.is(':disabled') || !hasChecked)) {
            this.$rowWrapper.find(toDisable).prop('disabled', checked);
        }
    };

    DayRow.prototype.getAllScheduleTimes = function() {
      var rowJson = this.toJson();
      var times = [];

      rowJson.scheduledTime.forEach(function (time) {
        if (time.active) {
            times.push(parseInt(time.time.replace(':', '')));
        }
      });

      return times;
    };

    DayRow.prototype.isValidTimeIntervals = function() {
      var validator = new ScheduleValidation();
      var times = this.getAllScheduleTimes();

      return validator.valid(times);
    };

    DayRow.prototype.showTimeScheduleErrors = function(errorMsg) {
      var timeWrappers = this.$rowWrapper.find('.schedule-time-wrapper');
      var $errorBlock = $('<div class="form-control-feedback-message js-form-error-msg">' + errorMsg + '</div>');

      _.each(timeWrappers, function(time) {
        var $time = $(time);
        if (!$time.find('.js-form-error-msg').exists()) {
          $time.find('.time-input').after($errorBlock.clone());
          $time.addClass('has-alert has-feedback js-form-error');
        }
      });
    };

    DayRow.prototype.hideTimeScheduleErrors = function() {
      var timeWrapper = this.$rowWrapper.find('.schedule-time-wrapper');

      timeWrapper.find('.js-form-error-msg').remove();
      timeWrapper.removeClass("has-alert has-feedback js-form-error");
    };

    DayRow.defaults = function () {
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
    };

    DayRow.get = function($needle) {
        var $row;
        if ($needle.hasClass('tr.l-time-schedule-row')) {
            $row = $needle;
        } else {
            $row = $needle.closest('tr.l-time-schedule-row');
        }
        return new DayRow($row);
    };

    DayRow.create = function(entry) {
       entry = $.extend(DayRow.defaults(), entry);
       var $row = AGN.Lib.Template.dom('schedule-day-row', {entry: entry});
       var dayRow = new DayRow($row);


       $row.on('row:add-schedule', function() {
            var timeLines = $row.find('.schedule-time-wrapper');
            var multipleLines = timeLines.length > 1;

            $row.find('[data-action="remove-schedule"]').closest('.input-group-addon')
              .toggleClass('hidden', !multipleLines);
            $row.find('.interval-reminder').toggleClass('hidden', !multipleLines);

            var $hourCheckbox = $row.find('.hour-checkbox');
            dayRow.hourCheckboxToggle($hourCheckbox);

            var $timeCheckbox = $row.find('.time-checkbox');
            dayRow.timeCheckboxToggle($timeCheckbox);
       });
       $row.on('row:remove-schedule', function() {
            var timeLines = $row.find('.schedule-time-wrapper');
            var multipleLines = timeLines.length > 1;

            $row.find('[data-action="remove-schedule"]').closest('.input-group-addon')
              .toggleClass('hidden', !multipleLines);
            $row.find('.interval-reminder').toggleClass('hidden', !multipleLines);

            if (timeLines.length === 0) {
                dayRow.addSchedule();
            }
        });

       return $row;
    };

    DayRow.deserialize = function(data) {
        return DayRow.create(data);
    };

    function PeriodRow($row) {
      var self = this;
      if (!$row.exists()) {
        $row = PeriodRow.create({});
      }

      self.$rowWrapper = $row;
    }

    PeriodRow.prototype.toJson = function() {
      var self = this;

      var type = self.$rowWrapper.find('input[name=type]:checked').val();
      var weekDay = undefined;
      var monthDay = undefined;
      if (type == 'TYPE_MONTHLY') {
        monthDay = self.$rowWrapper.find('select[name=dayOfTheMonth]').val();
      } else {
        weekDay = self.$rowWrapper.find('select[name=dayOfTheWeek]').val();
      }

      var scheduledTime = _.map(self.$rowWrapper.find('.schedule-time-wrapper'), function(scheduleTime) {
        var $timeCheckbox = $(scheduleTime).find('.time-checkbox');

        var active = true;
        if ($timeCheckbox.exists()) {
          active = !$timeCheckbox.is(':disabled') && $timeCheckbox.is(':checked');
        }
        var time = $(scheduleTime).find('.time-input').val();

        return {
          active: active,
          time: time
        }
      });

       return $.extend(PeriodRow.defaults(), {
         type: type,
         weekDay: weekDay,
         monthDay: monthDay,
         scheduledTime: scheduledTime
      });
    };

    PeriodRow.prototype.addSchedule = function(scheduleElement) {
        var self = this,
          wrapper = self.$rowWrapper;

        var $timeWrapper = TimeSchedule.create(scheduleElement);

        wrapper.find('.schedule-settings-wrapper').append($timeWrapper);

        AGN.runAll($timeWrapper);
        wrapper.trigger('row:add-schedule');
    };

    PeriodRow.prototype.removeSchedule = function($needle) {
        TimeSchedule.get($needle).remove();
        this.$rowWrapper.trigger('row:remove-schedule');
    };

    PeriodRow.defaults = function () {
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
    };

    PeriodRow.deserialize = function(object, type) {
      var data = $.extend(PeriodRow.defaults(), object);
      data.type = convertTypeToInt(object.type);
      data.monthDay = convertMonthToInt(object.monthDay);
      return PeriodRow.create(data);
    };

    function convertTypeToInt(typeEnumName) {
      switch (typeEnumName) {
        case 'TYPE_MONTHLY':
          return 2;
        case  'TYPE_WEEKLY':
        default:
          return 1;
      }
    }
    function convertMonthToInt(monthEnumName) {
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

    PeriodRow.get = function($needle) {
        var $row;
        if ($needle.hasClass('tr.l-time-schedule-row')) {
            $row = $needle;
        } else {
            $row = $needle.closest('tr.l-time-schedule-row');
        }
        return new PeriodRow($row);
    };

    PeriodRow.create = function(entry) {
      entry = $.extend(PeriodRow.defaults(), entry);
      var $row = AGN.Lib.Template.dom('schedule-period-row', {entry: entry});
      var dayRow = new PeriodRow($row);


     $row.on('row:add-schedule', function() {
          //show delete buttons if more than one schedule lines
          var timeLines = $row.find('.schedule-time-wrapper');
          if (timeLines.length > 1) {
              $row.find('[data-action="remove-schedule"]').closest('.input-group-addon').removeClass('hidden');
          }
     });
     $row.on('row:remove-schedule', function() {
          //hide delete button if less than 2 schedule lines
          var timeLines = $row.find('.schedule-time-wrapper');
          if (timeLines.length < 2) {
              $row.find('[data-action="remove-schedule"]').closest('.input-group-addon').addClass('hidden');
          }

          if (timeLines.length === 0) {
              dayRow.addSchedule();
          }
      });

     return $row;
    };

    function ScheduleTimeTable($needle) {
        var self = this;
        self.$container = $needle.find('table tbody');

        self.$container.on('scheduler:add-day', function() {
            //show delete buttons if more than one day lines
            var dayLines = self.$container.find('.l-time-schedule-row');
            if (dayLines.length > 1) {
                self.$container.find('[data-action="remove-day"]').removeClass('hidden');
            }
        });

        self.$container.on('scheduler:remove-day', function() {
            //hide delete button if less than 2 day lines
            var dayLines = self.$container.find('.l-time-schedule-row');
            if (dayLines.length <= 1) {
                dayLines.find('[data-action="remove-day"]').addClass('hidden');
            }
        });
    }

    ScheduleTimeTable.prototype.clean = function() {
        this.$container.find('tr.l-time-schedule-row').remove();
    };


    ScheduleTimeTable.prototype.deleteRow = function(row) {
        this.deleteRowByNeedle(row.$rowWrapper);
    };

    ScheduleTimeTable.prototype.deleteRowByNeedle = function($row) {
        this.$container.find($row).remove();
        this.$container.trigger('scheduler:remove-day');
    };

    ScheduleTimeTable.prototype.addRow = function($row) {
        if ($row) {
          this.$container.append($row);

          AGN.runAll($row);
          this.$container.trigger('scheduler:add-day', $row);
          $row.trigger('row:add-schedule', $row);
        }
    };

    ScheduleTimeTable.prototype.addEmptyDayRow = function() {
      this.addRow(DayRow.create());
    };

    ScheduleTimeTable.prototype.addEmptyPeriodRow = function() {
      this.addRow(PeriodRow.create());
    };

    ScheduleTimeTable.prototype.getAllDayRows = function() {
      var daysElements = this.$container.find('tr.l-time-schedule-row');
      return _.map(daysElements, function(elem) {
        return DayRow.get($(elem));
      });
    };

    ScheduleTimeTable.prototype.getAllPeriodRows = function() {
      var daysElements = this.$container.find('tr.l-time-schedule-row');
      return _.map(daysElements, function(elem) {
        return PeriodRow.get($(elem));
      });
    };

    ScheduleTimeTable.prototype.getSubmissionJson = function(type) {
      var scheduleJson = {};
      if (type == 'day') {
        scheduleJson = _.map(this.getAllDayRows(), function(row) {
          return row.toJson();
        });
      } else if (type == 'period') {
        scheduleJson = _.map(this.getAllPeriodRows(), function(row) {
          return row.toJson();
        });
      }

      return JSON.stringify(scheduleJson);
    };

    function ScheduleValidation(minutes) {
      var min = 100; // 100 is interval of one hour

      if (minutes) {
        min = Number(minutes);
      }

      this.intervalPad = min;
    };

    ScheduleValidation.prototype.valid = function (times) {
      var interval = this.intervalPad;
      return times.every(ScheduleValidation.hasCorrectInterval, interval);
    };

    ScheduleValidation.hasCorrectInterval = function (time, index, times) {
      var interval = this;
      var isCorrect = true;
      times.splice(index, 1);
      times.forEach(function (element) {
          if (Math.abs(element - time) < interval) {
              isCorrect = false;
          }
      });
      return isCorrect;
    };

    AGN.Lib.ScheduleTimeTable = ScheduleTimeTable;
      AGN.Lib.ScheduleTimeTable.DayRow = DayRow,
      AGN.Lib.ScheduleTimeTable.PeriodRow = PeriodRow;
})();