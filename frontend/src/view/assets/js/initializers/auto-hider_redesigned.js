/*doc
---
title: Show|Hide by Checkbox|Select
name: checkbox-select-hider
category: Components - Show|Hide by Checkbox|Select
---
*/

/*doc
---
title: Show|Hide by Checkbox
name: checkbox-hider
parent: checkbox-select-hider
---

By setting `[data-show-by-checkbox]` the object will be hidden if <a href="components_-_switches_&_checkboxes.html#checkbox"><i>checkbox</i></a> is not checked.

By setting `[data-hide-by-checkbox]` the object will be hidden if <a href="components_-_switches_&_checkboxes.html#checkbox"><i>checkbox</i></a> is checked.

Checkbox JQuery selector should be set in `[data-show|hide-by-checkbox]`.

<small class="text-secondary">
    Please note. The values of elements that are hidden with this method are <b>still sent</b> in the form on submit.
    To disable inputs on hidden state see <a href="javascripts_-_fields.html#fields-02-toggle-vis"><i>Toggle Visibility Field</i></a>
</small>

```htmlexample
<div class="form-column">
    <div class="form-check form-switch">
        <input class="form-check-input" id="control-switch" type="checkbox" role="switch">
        <label class="form-label form-check-label" for="switch-example">Checkbox or switch to control other elements</label>
    </div>
    <div data-show-by-checkbox="#control-switch">
        <label for="lastname" class="form-label">I'm shown when the switch is on</label>
        <input type="text" class="form-control" name="lastname" id="lastname"/>
    </div>
    <div class="notification-simple notification-simple--info" data-hide-by-checkbox="#control-switch">
        I'm hidden when the switch is on
    </div>
</div>
```
*/

/*doc
---
title: Show|Hide by Select
name: select-hider
parent: checkbox-select-hider
---

Use `[data-show-by-select]` or `[data-hide-by-select]` to show or hide element by selected options in <a href="components_-_selects.html"><i>select</i></a>.
These options can be set using `[data-show-by-select-values]` or `[data-show-by-select-values]` respectively
specifying csv option values.

<small class="text-secondary">
    Please note. The values of elements that are hidden with this method are <b>still sent</b> in the form on submit.
    To disable inputs on hidden state see <a href="javascripts_-_fields.html#fields-02-toggle-vis"><i>Toggle Visibility Field</i></a>
</small>

```htmlexample
<div class="form-column">
    <div>
        <label for="control-select" class="form-label">Change option</label>
        <select id="control-select" class="form-control js-select">
            <option value="other">Show nothing</option>
            <option value="input">Show input field</option>
            <option value="info">Show info message</option>
        </select>
    </div>
    <div data-show-by-select="#control-select" data-show-by-select-values="input">
        <label for="lastname" class="form-label">I'm shown when 'input' option is selected</label>
        <input type="text" class="form-control" name="lastname" id="lastname"/>
    </div>
    <div class="notification-simple notification-simple--info" data-show-by-select="#control-select" data-show-by-select-values="info">
        I'm shown when 'info' option is selected
    </div>
    <div class="btn btn-info" data-show-by-select="#control-select" data-show-by-select-values="input,info">
        I'm shown when either 'info' or 'input' option is selected
    </div>
</div>
```

If you need to display some option even in case if nothing if selected in the dropdown, please use `[data-show-if-no-selection]` attribute.

```htmlexample
<div class="form-column">
    <div>
          <label for="control-select2" class="form-label">Change option</label>
          <select id="control-select2" class="form-control js-select" multiple>
              <option value="input">Show input field</option>
              <option value="info">Show info message</option>
           </select>
    </div>
    <div data-show-by-select="#control-select2" data-show-by-select-values="input" data-show-if-no-selection>
        <label for="lastname" class="form-label">I'm shown when 'input' option is selected or nothing selected</label>
        <input type="text" class="form-control" name="lastname" id="lastname"/>
    </div>
    <div class="notification-simple notification-simple--info" data-show-by-select="#control-select2" data-show-by-select-values="info" data-show-if-no-selection>
        I'm shown when 'info' option is selected or nothing selected
    </div>
    <div class="notification-simple notification-simple--success" data-show-by-select="#control-select2" data-show-if-no-selection>
        I'm shown when nothing selected
    </div>
</div>
```
*/

;(() => {

  AGN.Lib.CoreInitializer.new('checkbox-hider', function($scope = $(document)) {
    const Storage = AGN.Lib.Storage;

    _.each($scope.find('[data-show-by-checkbox]'), el => {
      const $el = $(el);
      const $checkbox = $($el.data('show-by-checkbox'));
      if ($checkbox.is('[data-stored-field]')) {
        Storage.restoreChosenFields($checkbox);
      }
      updateVisibleByCheckbox($el, $checkbox, true);
      addCheckboxListener($el, $checkbox, true);
    });

    _.each($scope.find('[data-hide-by-checkbox]'), el => {
      const $el = $(el);
      const $checkbox = $($el.data('hide-by-checkbox'));
      updateVisibleByCheckbox($el, $checkbox, false);
      addCheckboxListener($el, $checkbox, false);
    });

    _.each($scope.find('[data-show-by-select]'), el => {
      const $el = $(el);
      const $select = $($el.data('show-by-select'));
      const { 'showBySelectValues': showForData = '' } = $el.data();

      if ($select.exists() && (showForData !== '' || $el.is('[data-show-if-no-selection]'))) {
        const options = showForData.toString().split(",").map(option => option.trim());

        updateVisibleBySelect($el, $select, options, true);
        addSelectListener($el, $select, options, true);
      }
    });

    _.each($scope.find('[data-hide-by-select]'), el => {
      const $el = $(el);
      const $select = $($el.data('hide-by-select'));
      const { 'hideBySelectValues': hideForData = '' } = $el.data();

      if ($select.exists() && hideForData !== '') {
        const options = hideForData.toString().split(",").map(option => option.trim());

        updateVisibleBySelect($el, $select, options, false);
        addSelectListener($el, $select, options, false);
      }
    });
  });

  function addCheckboxListener($el, $checkbox, showIfChecked){
    $checkbox.change(() => {
      updateVisibleByCheckbox($el, $checkbox, showIfChecked);
      if ($checkbox.is('[data-stored-field]')) {
        AGN.Lib.Storage.saveChosenFields($checkbox);
      }
    });
  }

  function updateVisibleByCheckbox($el, $checkbox, showIfChecked) {
    const hide= showIfChecked ? !$checkbox.is(":checked") : $checkbox.is(":checked");
    $el.toggleClass("hidden", hide);
    $el.trigger(hide ? "tile:hide" : "tile:show");
    AGN.Lib.CoreInitializer.run("truncated-text-popover", $el);
  }

  function addSelectListener($el, $select, options, showIfChecked){
    $select.change(function() {
      updateVisibleBySelect($el, $select, options, showIfChecked);
    });
  }

  function updateVisibleBySelect($el, $select, controlOptions, showIfChecked) {
    const selectVal = $select.val();
    const valueArr = Array.isArray(selectVal) ? selectVal : [selectVal];
    const selected = valueArr.some(val => controlOptions.includes(val));

    if (showIfChecked) {
      $el.toggleClass("hidden", !(selected || ($el.is('[data-show-if-no-selection]') && valueArr.length === 0)));
    } else {
      $el.toggleClass("hidden", selected);
    }

    $el.closest('select').trigger('change.select2');
    AGN.Lib.CoreInitializer.run("truncated-text-popover", $el);
  }
})();
