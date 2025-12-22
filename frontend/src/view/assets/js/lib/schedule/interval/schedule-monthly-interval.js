(() => {

  AGN.Lib.Template.register('monthly-schedule-interval', `
    <div>
      <label for="interval-month" class="form-label">{{- t('schedule.monthAndTime') }}</label>
      <div class="d-flex flex-column gap-1">
          <select class="form-control" data-schedule-month>
            {{ for (const _month of _.range(1, 13)) { }}
                <option value="{{- _month }}" {{ _month == month ? print('selected') : print('') }}>
                    {{- t('schedule.everyXMonth.' + _month) }}
                </option>
            {{ } }}
          </select>
      
          <select class="form-control" data-schedule-day-of-month>
            <option value="1" {{ monthDay == 1 ? print('selected') : print('') }}>
                {{- t('schedule.firstDayOfMonth') }}
            </option>
            {{ for (const _monthDay of _.range(2, 32)) { }}
                <option value="{{- _monthDay }}" {{ _monthDay == monthDay ? print('selected') : print('') }}>
                  {{- _monthDay }}.
                </option>
            {{ } }}
            <option value="-1" {{ monthDay == -1 ? print('selected') : print('') }}>
              {{- t('schedule.lastDayOfMonth') }}
            </option>
          </select>
      
          <div class="input-icon-container">
             <input class="form-control js-timepicker" type="text" value="{{- time }}"
                 data-schedule-time data-timepicker-options="mask: 'h:s'" placeholder="_" />
             <i class="icon icon-clock" data-tooltip="{{- window.agnTimeZoneId }}"></i>
          </div>
      </div>
    </div>
  `);

  class MonthlyInterval extends AGN.Lib.Schedule.ScheduleInterval {

    static defaults() {
      return {
        month: '',
        monthDay: '',
        time: ''
      }
    }

    getSettings() {
      const month = this.$container.find('[data-schedule-month]').val();
      const monthDay = this.$container.find('[data-schedule-day-of-month]').val();
      const time = this.$container.find('[data-schedule-time]').val();

      return {month, monthDay, time};
    }

    getSettingsHtml() {
      const options = $.extend(MonthlyInterval.defaults(), this.settings);
      return AGN.Lib.Template.text('monthly-schedule-interval', options);
    }

  }

  AGN.Lib.Schedule.ScheduleMontlyInterval = MonthlyInterval;

})();