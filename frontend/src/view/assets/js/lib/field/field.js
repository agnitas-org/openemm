/*doc
---
title: Fields
name: fields
category: Javascripts - Fields
---

Fields are small extensions of functionality for a form. They can be added via the `data-field="field-name"` directive.

```htmlexample
<form>
  <div data-field="toggle-vis">

  </div>

</form>
```
*/

(() => {

  class Field {
    constructor($field) {
      this.el = $field;
      this.options = _.extend({}, AGN.Lib.Helpers.objFromString($field.data('field-options')));
    }

    valid() {
      return true;
    }

    errors() {
      return [];
    }

    onSubmit() {
      // overridden in inherited classes
    }

    isInitializedFor($field) {
      return this.el.is($field);
    }

    static create($field) {
      const instance = $field.data('agn:field');
      if (instance) {
        return instance; // already initialized
      }
      const fieldType = $field.data('field');

      const type = AGN.Opt.Fields[fieldType] || this;
      const field = new type($field);
      $field.data('agn:field', field);
      return field;
    }
  }

  AGN.Lib.Field = Field;
})();
