// todo remove when ux update will be tested and old redesigned version will be removed

AGN.Lib.CoreInitializer.new('switchable-input', function($scope = $(document)) {
  const STR = AGN.Lib.ToggleVisField.STR; // constants
  STR.DATA_SHOW_ON_SWITCH = 'data-show-on-switch';
  STR.DATA_HIDE_ON_SWITCH = 'data-hide-on-switch';
  STR.BODY_SELECTOR = '.switchable-input__body';

  class SwitchableInput {
    constructor($el) {
      const switchableInput = $el.data('switchableInput');
      if (switchableInput) {
        return switchableInput; // return the existing object
      }
      this.$el = $el;
      this.#init();
    }

    #init() {
      this.$el.attr('data-field', 'toggle-vis');
      this.$checkbox
        .attr(STR.DATA_FIELD_VIS, '')
        .attr(STR.DATA_FIELD_VIS_SHOW, this.#getShowOnSwitchElementSelector())
        .attr(STR.DATA_FIELD_VIS_HIDE, this.#getHideOnSwitchElementSelector())
      this.$switch.prepend(this.#defaultVisHiddenInput());
      this.$el.data('switchableInput', this);
    }

    #getShowOnSwitchElementSelector() {
      return this.isSwapMode ? `[${STR.DATA_SHOW_ON_SWITCH}]` : STR.BODY_SELECTOR;
    }

    #getHideOnSwitchElementSelector() {
      return this.isSwapMode ? `[${STR.DATA_HIDE_ON_SWITCH}]` : '';
    }

    #defaultVisHiddenInput() {
      return $('<div class="hidden"></div>')
        .attr(STR.DATA_FIELD_VIS_DEFAULT, '')
        .attr(STR.DATA_FIELD_VIS_HIDE, this.#getShowOnSwitchElementSelector())
        .attr(STR.DATA_FIELD_VIS_SHOW, this.#getHideOnSwitchElementSelector());
    }

    get $switch() {
      return this.$el.find('.switchable-input__switch');
    }

    get $checkbox() {
      return this.$switch.find('[role="switch"]');
    }

    get isSwapMode() {
      return this.$body.find(`[${STR.DATA_SHOW_ON_SWITCH}]`).length > 0
        && this.$body.find(`[${STR.DATA_HIDE_ON_SWITCH}]`).length > 0;
    }

    get $body() {
      return this.$el.find(STR.BODY_SELECTOR);
    }
  }

  _.each($scope.find('.switchable-input'), function(el) {
    new SwitchableInput($(el));
  });
});
