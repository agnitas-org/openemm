/*doc
---
title: Double Select Field
name: fields-01-double-select
parent: fields
---

A `double-select` Field can be used if the options of one select are dependent on the value of another select.

The parent select should be designated with the css class `js-double-select-trigger`, while the child/dependent select should have the class `js-double-select-target`.

For reading the values and titles for the options from a hash in AGN.Opt just set `data-provider="opt"` and `data-provider-src="KeyInAgnOpt"` on the element which has the `data-field="double-select"` directive.

```htmlexample
<form>

  <script type="text/javascript">
  AGN.Opt.DoubleSelectExample = {
      // if parent select value == parentSelectValue1
      "parentSelectValue1": {
          // show one option:
          "childSelectValue1": "Child Select Option 1"
      },
      // if parent select value == parentSelectValue2
      "parentSelectValue2": {
          // show the following 2 options:
          "childSelectValue1": "Child Select Option 1",
          "childSelectValue2": "Child Select Option 2"
      },
      // if parent select value == parentSelectValue3
      "parentSelectValue3": {
          // do not show any options
      }
  }
  </script>

  <div class="d-flex flex-column gap-2" data-field="double-select" data-provider="opt" data-provider-src="DoubleSelectExample">
    <div>
      <label class="form-label">Parent Control</label>
      <select class="form-control js-select js-double-select-trigger">
        <option value="parentSelectValue1">Parent Select Option 1</option>
        <option value="parentSelectValue2">Parent Select Option 2</option>
        <option value="parentSelectValue3">Parent Select Option 3</option>
      </select>
    </div>

    <div>
      <label class="form-label">Child/Dependent Control</label>
      <select class="form-control js-select js-double-select-target">
      </select>
    </div>
  </div>

</form>

```
*/

(() => {
  class DoubleSelectField extends AGN.Lib.Field {
    constructor($field) {
      super($field);

      this.$source = this.el.find('select.js-double-select-trigger');
      this.$target = this.el.find('select.js-double-select-target');

      this.$source.off('change');
      this.$source.on('change', () => {
        this.$target.val([])
        this.update();
      });

      AGN.Lib.DataProvider.get($field).done(options => {
        this.options = options;
        this.update();
      });
    }

    update() {
      const valueSource = this.$source.find('option:selected').val();
      const valueTarget = this.$target.find('option:selected').val();

      this.$target.html(AGN.Lib.Template.text(
        'double-select-options',
        {opts: this.options[valueSource], valueSelected: valueTarget}
      ));
      AGN.Lib.CoreInitializer.run('select', this.$target);
    }
  }

  AGN.Lib.DoubleSelectField = DoubleSelectField;
  AGN.Opt.Fields['double-select'] = DoubleSelectField;
})();
