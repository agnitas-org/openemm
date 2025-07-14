/*doc
---
title: Toggle Visibility Field
name: fields-02-toggle-vis
parent: fields
---

A `toggle-vis` Field can be used if elements should be displayed depending on the state of a checkbox, a radio field or a select.

The radio/select or checkbox elements which control the visibility should all have the `data-field-vis=""`. When checked/selected they will hide all elements which are set in data-field-vis-hide="jQuerySelector" and show all all elements which are set in data-field-vis-show="jQuerySelector"

Inputs in the targets will be disabled when hidden, to avoid sending them with the form.

For usage with a checkbox an element with the `data-field-vis-default=""` directive should be added to control the state if no checkbox is checked.

Caveat:
Only targets inside the container designated by the `toggle-vis` directive will be hidden/shown.
To change this and specify a specific container you need to set the `data-field-vis-scope` attribute and specify the container selector there.

```htmlexample
<form class="d-flex flex-column gap-2">

  <div class="d-flex flex-column gap-2" data-field="toggle-vis">
    <div>
      <label class="form-label">Control via Radiobuttons</label>
      <div class="switch">
        <input type="radio" id="radio-hide" name="combinator" value="1" checked data-field-vis="" data-field-vis-hide="#exampleRadioTarget" />
        <label for="radio-hide">HIDE</label>
        <input type="radio" id="radio-show" name="combinator" value="0" data-field-vis="" data-field-vis-show="#exampleRadioTarget" />
        <label for="radio-show">SHOW</label>
      </div>
    </div>

    <div id="exampleRadioTarget">
      <label class="form-label">Disabled when hidden</label>
      <input type="text" class="form-control">
    </div>
  </div>

  <div class="d-flex flex-column gap-2" data-field="toggle-vis">
    <div>
      <label class="form-label">Control via Select</label>
      <select name="hideTargetViaSelect" data-field-vis="" class="form-control js-select">
        <option value="1" data-field-vis-hide="#exampleSelectTarget" selected>
          Hide Target
        </option>
        <option value="2" data-field-vis-show="#exampleSelectTarget">
          Show Target
        </option>
      </select>
    </div>

    <div id="exampleSelectTarget">
      <label class="form-label">Disabled when hidden</label>
      <input type="text" class="form-control">
    </div>
  </div>

  <div class="d-flex flex-column gap-2" data-field="toggle-vis">
    <div>
      <label class="form-label">Control via Checkbox</label>

      <div class="form-check form-switch">
        <input class="form-check-input" id="switch-button-1" type="checkbox" role="switch" checked data-field-vis="" data-field-vis-show="#button1">
        <label class="form-label form-check-label" for="switch-button-1">Show Button 1</label>
      </div>

      <div class="form-check form-switch">
        <input class="form-check-input" id="switch-button-2" type="checkbox" role="switch" checked data-field-vis="" data-field-vis-show="#button2">
        <label class="form-label form-check-label" for="switch-button-2">Show Button 2</label>
      </div>

      <div class="hidden" data-field-vis-default="" data-field-vis-hide="#button1, #button2"></div>
    </div>

    <div class="d-flex gap-2">
      <a href="#" class="btn btn-primary" id="button1">Button 1</a>
      <a href="#" class="btn btn-primary" id="button2">Button 2</a>
    </div>
  </div>
</form>
```
*/

(() => {
  const FIELD_VIS = 'field-vis';
  const FIELD_VIS_SHOW = 'field-vis-show';
  const FIELD_VIS_HIDE = 'field-vis-hide';
  const DATA_FIELD_VIS = `data-${FIELD_VIS}`;
  const DATA_FIELD_VIS_SHOW = `data-${FIELD_VIS_SHOW}`;
  const DATA_FIELD_VIS_HIDE = `data-${FIELD_VIS_HIDE}`;
  const DATA_FIELD_VIS_DEFAULT = 'data-field-vis-default';

  class ToggleVisField extends AGN.Lib.Field {

    static STR = {
      DATA_FIELD_VIS,
      DATA_FIELD_VIS_SHOW,
      DATA_FIELD_VIS_HIDE,
      DATA_FIELD_VIS_DEFAULT
    };

    constructor($field) {
      super($field);

      this.$source = this.el.find(`[${DATA_FIELD_VIS}]`);
      this.$source.on('change change.select2', () => this.update());

      this.update();
    }

    update() {
      this.#updateDefaults();
      this.#updateSelects();
      this.#updateCheckboxes();
    }

    #updateSelects() {
      _.each(this.$source.filter('select'), el => {
        this.#toggleFields($(el).find(':selected'));
      })
    }

    #updateDefaults() {
      _.each(this.el.find(`[${DATA_FIELD_VIS_DEFAULT}]`), el => {
        this.#toggleFields($(el));
      })
    }

    #updateCheckboxes() {
      _.each(this.$source.filter(':checked'), el => {
        this.#toggleFields($(el));
      })
    }

    #toggleFields($el) {
      if ($el.is(':disabled') && !$el.is('[data-field-vis-nondisabled]')) {
        return;
      }

      const fieldsToHide = $el.data(FIELD_VIS_HIDE);
      const fieldsToShow = $el.data(FIELD_VIS_SHOW);

      let $scope = this.el;
      if (this.el.is('[data-field-vis-scope]')) {
        $scope = $(this.el.data('field-vis-scope'));
      }

      _.each($scope.find(fieldsToHide), el => {
        $el = $(el);
        $el.all(":input").each(function () {
          const $input = $(this);
          if (!$input.prop('disabled')) {
            $input.data('field-disabled', true);
            $input.prop("disabled", true);
          }
        });
        $el.hide();

        if ($el.data('select2')) {
          $el.next('.select2-container').hide();
          AGN.Lib.CoreInitializer.run('select', $el.parents('select'));
        }

        if ($el.is('option')) {
          $el.prop('disabled', true);
          AGN.Lib.CoreInitializer.run('select', $el.parents('select'));
        }
      });

      _.each($scope.find(fieldsToShow), el => {
        $el = $(el);
        $el.all(":input").each(function () {
          const $input = $(this);
          if ($input.data('field-disabled')) {
            $input.prop("disabled", false);
            $input.removeData('field-disabled');
          }
        });
        $el.show();

        if ($el.data('select2')) {
          $el.next('.select2-container').show();
          AGN.Lib.CoreInitializer.run('select', $el.parents('select'));
        }

        if ($el.is('option')) {
          $el.prop('disabled', false);
          AGN.Lib.CoreInitializer.run('select', $el.parents('select'));
        }
      });
    }
  }

  AGN.Lib.ToggleVisField = ToggleVisField;
  AGN.Opt.Fields['toggle-vis'] = ToggleVisField;
})();
