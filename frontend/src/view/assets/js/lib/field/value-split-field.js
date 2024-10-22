/*doc
---
title: Value split Field
name: fields-06-value-split
parent: fields
---

Helps to separate data into several properties when submitting a form if they were entered inside one field.

The names of the fields for which the value should be split must be specified, separated by commas, inside the `data-field-split` attribute.
The splitter rule is specified in the `data-field-split-rule` attribute.

```htmlexample
<form>
  <div class="time-picker-container" data-field="split">
    <input type="text" id="sendTime" name="sendTime" class="form-control js-timepicker" value="10:30"
           data-field-split="sendHour, sendMinute" data-field-split-rule=":" data-timepicker-options="mask: 'h:00'" />
  </div>
</form>
```
*/
(() => {

  class ValueSplitField extends AGN.Lib.Field {
    constructor($field) {
      super($field);

      this.$source = this.el.find('[data-field-split]');
      this.targets = this.$source.data('field-split').split(/,\s?/);
      this.rule = new RegExp(this.$source.data('field-split-rule'));
    }

    valid() {
      const form = AGN.Lib.Form.get(this.el);
      const values = this.$source.val().split(this.rule);

      _.each(this.targets, (target, index) => {
        form.setValueOnce(target, values[index]);
      })

      return true;
    }
  }

  AGN.Lib.ValueSplitField = ValueSplitField;
  AGN.Opt.Fields['split'] = ValueSplitField;
})();
