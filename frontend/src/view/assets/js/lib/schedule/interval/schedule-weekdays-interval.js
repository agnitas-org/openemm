(() => {

  AGN.Lib.Template.register('weekdays-schedule-interval', `
    <div>
      <label class="form-label">{{- t('schedule.monthAndTime') }}</label>
      
      <div class="d-flex flex-column gap-1">
        <select class="form-control" data-schedule-day-of-month>
            {{ for (const _monthDay of _.range(1, 6)) { }}
                <option value="{{- _monthDay }}" {{ _monthDay == monthDay ? print('selected') : print('') }}>
                    {{- t('schedule.weekDayOrdinal.' + _monthDay) }}
                </option>
            {{ } }}
        </select>
        
        <select class="form-control" data-schedule-day-of-week>
           {{ weekDays.forEach(_weekDay => { }}
              <option value="{{- _weekDay }}" {{ _weekDay == weekDay ? print('selected') : print('') }}>
                  {{- t('schedule.weekdays.' + _weekDay) }}
              </option>
           {{ }); }}
        </select>
        
        <div class="input-icon-container">
            <input class="form-control js-timepicker" type="text" value="{{- time }}"
                data-schedule-time data-timepicker-options="mask: 'h:s'" placeholder="_" />
            <i class="icon icon-clock" data-tooltip="{{- window.agnTimeZoneId }}"></i>
        </div>
      </div>
    </div>
  `);

  class WeekdaysInterval extends AGN.Lib.Schedule.ScheduleInterval {

    static defaults() {
      return {
        weekDay: '',
        monthDay: '',
        time: ''
      }
    }

    getSettings() {
      const weekDay = this.$container.find('[data-schedule-day-of-week]').val();
      const monthDay = this.$container.find('[data-schedule-day-of-month]').val();
      const time = this.$container.find('[data-schedule-time]').val();

      return {weekDay, monthDay, time};
    }

    getSettingsHtml() {
      const options = $.extend(WeekdaysInterval.defaults(), this.settings);
      options.weekDays = this.getPossibleWeekDays();

      return AGN.Lib.Template.text('weekdays-schedule-interval', options);
    }

  }

  AGN.Lib.Schedule.ScheduleWeekdaysInterval = WeekdaysInterval;

})();