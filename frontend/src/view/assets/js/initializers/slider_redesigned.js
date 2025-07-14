/*doc
---
title: Toggle All
name: toggle_all
parent: checkboxes
---

Use `[data-toggle-checkboxes]` inside <a href="components_-_tiles.html#tiles_03_header">`.tile-header`</a>
in order to control toggling of the all switches in <a href="components_-_tiles.html#tiles_07_body">`.tile-body`</a>.


When all internal switches are turned on, the "toggle all" switch automatically becomes checked.
If any internal switch is turned off, the "toggle all" switch automatically becomes unchecked.
Disabled switches are ignored.

```htmlexample
<div class="tile">
    <div class="tile-header">
        <div class="tile-title">Toggle all example</div>
        <div class="form-check form-check-reverse form-switch">
            <input class="form-check-input" type="checkbox" role="switch" data-toggle-checkboxes>
        </div>
    </div>
    <div class="tile-body form-column-1">
        <div class="form-check form-switch">
            <input id="toggle-all-1" type="checkbox" class="form-check-input" role="switch"/>
            <label class="form-label form-check-label" for="toggle-all-1">Default</label>
        </div>
        <div class="form-check form-switch">
            <input id="toggle-all-2" type="checkbox" class="form-check-input" role="switch" checked/>
            <label class="form-label form-check-label" for="toggle-all-2">Initially checked</label>
        </div>
        <div class="form-check form-switch">
            <input id="toggle-all-3" type="checkbox" class="form-check-input" role="switch" disabled/>
            <label class="form-label form-check-label" for="toggle-all-3">Disabled</label>
        </div>
        <div class="form-check form-switch">
            <input id="toggle-all-4" type="checkbox" class="form-check-input" role="switch" checked disabled/>
            <label class="form-label form-check-label" for="toggle-all-4">Disabled and checked</label>
        </div>
    </div>
</div>
```
*/

AGN.Lib.CoreInitializer.new('slider', function($scope = $(document)) {

  _.each($scope.find('[data-toggle-checkboxes]'), function(el) {
    const $toggleAll = $(el);
    controlToggleAllCheckbox($toggleAll);
    getCheckboxesControlledBy($toggleAll).click(() => controlToggleAllCheckbox($toggleAll));
    $toggleAll.on('update-toggle-all', () => controlToggleAllCheckbox($toggleAll));
    addClickListener($toggleAll);
    $toggleAll.closest('.form-switch').toggle(getCheckboxesControlledBy($toggleAll).length > 0);
  });

  function addClickListener($toggleAll) {
    $toggleAll.on('click', function() {
      const $elem = $(this);
      _.each($elem.closest('.tile').find('.tile-body [type=checkbox]:enabled:visible'),
        checkbox => $(checkbox).prop('checked', $elem.is(":checked")).trigger('change'));
    });
  }

  function getCheckboxesControlledBy($toggleAll) {
    return $toggleAll.closest('.tile').find('.tile-body').find('.form-check-input:visible');
  }

  function controlToggleAllCheckbox($toggleAll) {
    const $checkboxes = getCheckboxesControlledBy($toggleAll);
    $toggleAll.prop("checked", isAllEnabledCheckboxesChecked($checkboxes));
    $toggleAll.prop("disabled", isAllCheckboxesDisabled($checkboxes));
  }

  function isAllEnabledCheckboxesChecked(checkboxes) {
    return _.every(checkboxes.filter(':not(:disabled)'), checkbox => checkbox.checked);
  }

  function isAllCheckboxesDisabled(checkboxes) {
    return checkboxes.filter(':not(:disabled)').length === 0;
  }
});
