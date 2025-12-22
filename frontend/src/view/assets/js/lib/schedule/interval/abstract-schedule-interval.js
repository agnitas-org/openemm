(() => {

  class ScheduleInterval {

    constructor($container, settings) {
      if (new.target === AGN.Lib.Schedule.ScheduleInterval) {
        throw new TypeError(`${new.target.name} is an abstract class and cannot be instantiated directly`);
      }

      this.$container = $container;
      this.settings = settings ?? {};
    }

    drawSettings() {
      this.$container.off();
      this.$container.html(this.getSettingsHtml());
      AGN.runAll(this.$container);
    }

    getPossibleWeekDays() {
      return [2, 3, 4, 5, 6, 7, 1];
    }

    getSettingsHtml() {
      throw new Error(`${this.constructor.name}: getSettingsHtml() must be implemented`);
    }

    getSettings() {
      throw new Error(`${this.constructor.name}: getSettings() must be implemented`);
    }

    validate() {
      const $timeInputs = this.$container.find('[data-schedule-time]');
      let isValid = true;

      $timeInputs.each((i, input) => {
        const $input = $(input);
        AGN.Lib.Form.cleanFieldFeedback$($input);

        if (!$input.val() && $input.is(':visible')) {
          AGN.Lib.Form.showFieldError$($input, t('fields.required.errors.missing'));
          isValid = false;
        }
      });

      return isValid;
    }

  }

  AGN.Lib.Schedule.ScheduleInterval = ScheduleInterval;

})();