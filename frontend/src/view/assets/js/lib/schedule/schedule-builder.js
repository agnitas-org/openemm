(() => {

  AGN.Lib.Template.register('schedule-interval-type', `
   <div>
      <label for="interval-type" class="form-label">
        {{- t('schedule.interval') }}
      </label>
      <select id="interval-type" class="form-control" data-change-interval-type>
          {{ Object.keys(intervals).forEach(intervalType => { }}
            <option value="{{- intervalType }}" {{ intervalType === type ? print('selected') : print() }}>
              {{= intervals[intervalType].label }}
            </option>
          {{ }) }}
      </select>
    </div>
  `);

  const INTERVALS = {
    WEEKLY: {
      label: t('schedule.interval_type.weekly'),
      class: AGN.Lib.Schedule.ScheduleWeeklyInterval
    },
    BI_WEEKLY: {
      label: t('schedule.interval_type.bi_weekly'),
      class: AGN.Lib.Schedule.ScheduleBiWeeklyInterval
    },
    MONTHLY: {
      label: t('schedule.interval_type.monthly'),
      class: AGN.Lib.Schedule.ScheduleMontlyInterval
    },
    WEEKDAYS: {
      label: t('schedule.interval_type.weekdays'),
      class: AGN.Lib.Schedule.ScheduleWeekdaysInterval
    }
  }

  class ScheduleBuilder {

    static DATA_ATTR_NAME = 'agn:scheduleBuilder';

    constructor($container) {
      this.config = $container.find('script[data-config]').json();
      this.settings = JSON.parse(this.config.settings);
      this.settingsCache = new Map();

      this.#renderControls($container);
      $container.data(ScheduleBuilder.DATA_ATTR_NAME, this);
    }

    static get($needle) {
      return $needle.data(ScheduleBuilder.DATA_ATTR_NAME);
    }

    validate() {
      return this.interval.validate();
    }

    toJson() {
      return JSON.stringify({
        type: this.getCurrentIntervalType(),
        ... this.interval.getSettings()
      });
    }

    addAction(events, handler) {
      AGN.Lib.Action.new(events, handler, this.$container);
    }

    #renderControls($container) {
      this.$container = $('<div class="vstack gap-3">');
      this.#renderIntervalTypeControl();
      this.$dynamicContainer = $('<div>');

      $container.append(this.$container.append(this.$dynamicContainer));
      AGN.Lib.CoreInitializer.run('select', this.$container);

      this.changeIntervalType(
        INTERVALS[this.settings?.type]?.class ?? INTERVALS.WEEKLY.class,
        _.omit(this.settings, ['type'])
      );

      this.addAction({change: '[data-change-interval-type]'}, this.#handleChangedInterval.bind(this));
    }

    #renderIntervalTypeControl() {
      this.$container.append(AGN.Lib.Template.text('schedule-interval-type', {
        intervals: INTERVALS,
        type: this.settings?.type
      }));
    }

    #handleChangedInterval($el) {
      this.settingsCache.set(this.interval.constructor.name, this.interval.getSettings());

      const SettingsClass = INTERVALS[$el.val()].class;
      this.changeIntervalType(SettingsClass, this.settingsCache.get(SettingsClass.name) ?? {});
    }

    changeIntervalType(SettingsClass, settings) {
      this.interval = new SettingsClass(this.$dynamicContainer, settings);
      this.interval.drawSettings();
    }

    getCurrentIntervalType() {
      return this.$container.find('[data-change-interval-type]').val();
    }

  }

  AGN.Lib.Schedule.Builder = ScheduleBuilder;

})();