/*doc
---
title: Required Field
name: fields-03-required
parent: fields
---

The required field helps with preventing a form from submit, when its field value is blank.

```htmlexample
<form class="d-flex flex-column gap-2">
  <div>
    <label class="form-label">A required field</label>
    <input type="text" class="form-control" data-field="required" />
  </div>

  <a href="#" class="btn btn-primary" data-form-submit="">Submit</a>
</form>
```
*/
(() => {
  class RequiredField extends AGN.Lib.Field {

    valid() {
      if (this.options?.ignoreHidden && !this.el.is(':visible')) {
        return true;
      }
      if (this.el.exists()) {
        return $.trim(this.el.val());
      }
      return true;
    }

    errors() {
      const errors = [];

      if (this.valid()) return errors;

      errors.push({
        field: this.el,
        msg: t('fields.required.errors.missing')
      });

      return errors;
    }
  }

  AGN.Lib.RequiredField = RequiredField;
  AGN.Opt.Fields['required'] = RequiredField;
})();
