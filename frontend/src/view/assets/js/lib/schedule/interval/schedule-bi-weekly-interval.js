(() => {

  AGN.Lib.Template.register('bi-weekly-schedule-interval', `
    <div>
      <label class="form-label">{{- t('schedule.daysAndTime') }}</label>
      <div class="d-flex flex-column gap-1">
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

  class BiWeeklyInterval extends AGN.Lib.Schedule.ScheduleInterval {

    static defaults() {
      return {
        weekDay: '',
        time: ''
      }
    }

    getSettings() {
      const weekDay = this.$container.find('[data-schedule-day-of-week]').val();
      const time = this.$container.find('[data-schedule-time]').val();
      return {weekDay, time};
    }

    getSettingsHtml() {
      const options = $.extend(BiWeeklyInterval.defaults(), this.settings);
      options.weekDays = this.getPossibleWeekDays();

      return AGN.Lib.Template.text('bi-weekly-schedule-interval', options);
    }

  }

  AGN.Lib.Schedule.ScheduleBiWeeklyInterval = BiWeeklyInterval;

})();