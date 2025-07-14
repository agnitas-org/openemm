/*doc
---
title: Custom Field (Custom Validation)
name: fields-00-custom
parent: fields
---

A `validator` Field enables custom field validation provided by `AGN.Lib.Validator`.

This field prevents form submission when an input is not valid.

```htmlexample
<form>
  <div class="d-flex flex-column gap-1" >
    <div data-field="validator">
      <label class="form-label">Your nickname (min 3, max 20 characters)</label>
      <input type="text" class="form-control" data-field-validator="foo-edit/content" data-validator-options="min: 3, max: 20"/>
    </div>

    <a href="#" class="btn btn-primary" data-form-submit="">Submit</a>
  </div>

  <script type="text/javascript">
    AGN.Lib.Validator.new('foo-edit/content', {
      valid: function($e, options) {
        return !this.errors($e, options).length;
      },

      errors: function($e, options) {
        const content = $e.val();
        const errors = [];

        if (content.length < options.min) {
          errors.push({
            field: $e,
            msg: `Must be at least ${options.min} character(s) long`
          });
        } else if (content.length > options.max) {
          errors.push({
            field: $e,
            msg: `Must be shorter that ${options.max} character(s)`
          });
        }

        return errors;
      }
    });
 </script>

</form>
```
*/

(() => {
  class CustomField extends AGN.Lib.Field {
    constructor($field) {
      super($field);

      this.$target = this.el.find('[data-field-validator]');
      this.validatorName = this.$target.data('field-validator');

      const options = this.$target.data('validator-options');
      this.options = options ? AGN.Lib.Helpers.objFromString(options) || {} : {};
    }

    valid() {
      return !this.errors().length;
    }

    errors() {
      if (!this.$target.exists() || this.$target.prop('disabled')) {
        return [];
      }

      return this.#getValidators()
        .map(v => v.errors(this.$target, this.options))
        .flat();
    }

    #getValidators() {
      const validators = [];

      this.validatorName.split(',').map(vName => vName.trim()).forEach(validatorName => {
        const validator = AGN.Lib.Validator.get(validatorName);

        if (validator) {
          validators.push(validator);
        } else {
          console.debug(`Validator \`${validatorName}\` doesn't exist`);
        }
      });

      return validators;
    }

    isInitializedFor($field) {
      return super.isInitializedFor($field) || this.$target.is($field);
    }
  }

  AGN.Lib.CustomField = CustomField;
  AGN.Opt.Fields['validator'] = CustomField;
})();
