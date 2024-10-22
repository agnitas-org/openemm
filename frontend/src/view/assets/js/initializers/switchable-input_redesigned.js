/*doc
---
title: Switchable Input
name: fields-08-switchable-input
parent: fields
---

This feature is basically a syntactic sugar for `data-field="toggle-vis"` to make it easier to use.

If you need to show or hide certain piece of UI by (un-) checking a checkbox:<br/>
1. Add `switchable-input` CSS class to a container element (typically `<div>`);<br/>
2. Add `switchable-input__body` CSS class to a target element which you want to show/hide;<br/>
3. Add `role="switch"` attribute to an `<input type="checkbox">` element that should toggle target's visibility;<br/>
4. Wrap checkbox element with wrapper having `switchable-input__switch` CSS class.

Also make sure that the target (`switchable-input__body`) and the switch (`switchable-input__switch`) are inside the container.

```htmlexample
<form>
  <div class="switchable-input">
    <div class="switchable-input__header">
      <label class="switchable-input__label">Checkbox or toggle</label>
      <div class="switchable-input__switch">
          <input class="form-check-input" type="checkbox" role="switch">
      </div>
    </div>

    <div class="switchable-input__body">
      <label for="username" class="form-label">Some input field</label>
      <input type="text" class="form-control" name="username" id="username"/>
    </div>
  </div>
</form>
```

If you need to switch between two versions of UI instead of just toggling a visibility of a single element,
you can add `data-show-on-switch` and `data-hide-on-switch` attributes to elements under `switchable-input__body`.

The element with `data-show-on-switch` will be shown if the switch (checkbox) is on;

The element with `data-hide-on-switch` will be shown if the switch (checkbox) is off;

```htmlexample
<form>
  <div class="switchable-input">
    <div class="switchable-input__header">
      <label class="switchable-input__label">Swap</label>
      <div class="switchable-input__switch">
          <input class="form-check-input" type="checkbox" role="switch">
      </div>
    </div>

    <div class="switchable-input__body">
      <div data-hide-on-switch>
        <label for="email" class="form-label">E-mail</label>
        <input type="text" class="form-control" id="email" placeholder="E-mail"/>
      </div>

      <div data-show-on-switch>
        <label class="form-label">Credentials</label>
        <input type="text" class="form-control" placeholder="Username"/>
        <input type="text" class="form-control mt-1" placeholder="Password"/>
      </div>
    </div>
  </div>
</form>
```
*/

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
