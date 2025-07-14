/*doc
---
title: Switchable tile
name: tiles_17_switchable
parent: tiles
---

This feature is basically a syntactic sugar for <a href="javascripts_-_fields.html#fields-02-toggle-vis"><i>`[data-field="toggle-vis"]`</i></a> to make it easier to use.<br/>
Add `.tile--switchable` to the `.tile` element if you need to show or hide tile body by (un-) checking a checkbox<br/>
<small class="text-muted">
    Hidden inputs will be disabled, to avoid sending them with the form.
</small>

```htmlexample
<form>
  <div class="tile tile--switchable">
      <div class="tile-header">
          <div class="form-check form-switch">
              <input class="form-check-input" type="checkbox" role="switch">
          </div>
          <span class="text-truncate">Click the slider to toggle tile body</span>
      </div>
      <div class="tile-body">
          I am hidden when header switch is off
      </div>
  </div>
</form>
```

If you need to switch between two versions of UI instead of just toggling a visibility of a single element,
you can add `data-show-on-switch` and `data-hide-on-switch` attributes to elements under `.tile-body`.

The element with `data-show-on-switch` will be shown if the switch (checkbox) is on;<br/>
The element with `data-hide-on-switch` will be shown if the switch (checkbox) is off;

```htmlexample
<form>
  <div class="tile tile--switchable">
      <div class="tile-header">
          <div class="form-check form-switch">
              <input class="form-check-input" type="checkbox" role="switch">
          </div>
          <span class="text-truncate">Click the slider to toggle tile body</span>
      </div>
      <div class="tile-body">
        <div data-hide-on-switch>
          <input type="text" class="form-control" id="email" placeholder="E-mail"/>
        </div>
        <div class="hstack gap-3" data-show-on-switch>
          <input type="text" class="form-control" placeholder="Username"/>
          <input type="text" class="form-control mt-1" placeholder="Password"/>
        </div>
      </div>
  </div>
</form>
```
*/

AGN.Lib.CoreInitializer.new('switchable-tile', function($scope = $(document)) {
  const STR = AGN.Lib.ToggleVisField.STR; // constants
  STR.DATA_SHOW_ON_SWITCH = 'data-show-on-switch';
  STR.DATA_HIDE_ON_SWITCH = 'data-hide-on-switch';

  class SwitchableTile {
    constructor($el) {
      const switchableTile = $el.data('switchableTile');
      if (switchableTile) {
        return switchableTile; // return the existing object
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
      this.$el.data('switchableTile', this);
    }

    #getShowOnSwitchElementSelector() {
      return this.isSwapMode ? `[${STR.DATA_SHOW_ON_SWITCH}]` : '.tile-body';
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
      return this.$el.find('.tile-header .form-switch:first-child').first();
    }

    get $checkbox() {
      return this.$switch.find('[role="switch"]');
    }

    get isSwapMode() {
      return this.$body.find(`[${STR.DATA_SHOW_ON_SWITCH}]`).length > 0
        && this.$body.find(`[${STR.DATA_HIDE_ON_SWITCH}]`).length > 0;
    }

    get $body() {
      return this.$el.find('.tile-body');
    }
  }

  _.each($scope.find('.tile--switchable'), el => new SwitchableTile($(el)));
});
