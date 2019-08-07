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

(function(){

  var Field,
      CustomField,
      DoubleSelectField,
      ToggleVisField,
      PasswordField,
      ValueSplitField,
      FilterField,
      DateFilterField,
      DateSplitField,
      RequiredField;

  Field = function($field) {
    this.el = $field;
  }

  Field.create = function($field) {
    var type,
        fieldType = $field.data('field');

    type = AGN.Opt.Fields[fieldType] || Field;
    return new type($field);
  }

  Field.prototype.valid = function() {
    return true;
  }

  Field.prototype.errors = function() {
    return [];
  }

  AGN.Lib.Field = Field;

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

  <div class="form-group" data-field="validator">
    <div class="col-sm-4">
      <label class="control-label">Your nickname (min 3, max 20 characters)</label>
    </div>
    <div class="col-sm-4">
      <input type="text" class="form-control" data-field-validator="foo-edit/content" data-validator-options="min: 3, max: 20"/>
    </div>
  </div>

  <div class="btn-group">
    <a href="#" class="btn btn-regular btn-primary" data-form-submit="">Submit</a>
  </div>

  <script type="text/javascript">
    AGN.Lib.Validator.new('foo-edit/content', {
      valid: function($e, options) {
        return !this.errors($e, options).length;
      },

      errors: function($e, options) {
        var content = $e.val();
        var errors = [];

        if (content.length < options.min) {
          errors.push({
            field: $e,
            msg: "Must be at least " + options.min + " character(s) long"
          });
        } else if (content.length > options.max) {
          errors.push({
            field: $e,
            msg: "Must be shorter that " + options.max + " character(s)"
          });
        }

        return errors;
      }
    });
 </script>

</form>
```
*/

  CustomField = function($field) {
    // inherit from Field
    Field.apply(this, [$field]);

    var $target = this.el.find('[data-field-validator]');

    this.$target = $target;
    this.validatorName = $target.data('field-validator');

    var options = $target.data('validator-options');
    this.options = options ? AGN.Lib.Helpers.objFromString(options) : null;
  };

  CustomField.prototype = Object.create(Field.prototype);
  CustomField.prototype.constructor = CustomField;

  CustomField.prototype.valid = function() {
    var validator = AGN.Lib.Validator.get(this.validatorName);
    if (validator) {
      return validator.valid(this.$target, this.options);
    } else {
      console.debug('Validator `' + this.validatorName + "` doesn't exist");
      return true;
    }
  };

  CustomField.prototype.errors = function() {
    var validator = AGN.Lib.Validator.get(this.validatorName);
    if (validator) {
      return validator.errors(this.$target, this.options);
    } else {
      console.debug('Validator `' + this.validatorName + "` doesn't exist");
      return [];
    }
  };

  AGN.Lib.CustomField = CustomField;
  AGN.Opt.Fields['validator'] = CustomField;

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


  <div data-field="double-select" data-provider="opt" data-provider-src="DoubleSelectExample">
    <div class="form-group">
      <div class="col-sm-8">
        <label>Parent Control</label>
      </div>
      <div class="col-sm-4">
        <select class="form-control js-double-select-trigger">
          <option value="parentSelectValue1">Parent Select Option 1</option>
          <option value="parentSelectValue2">Parent Select Option 2</option>
          <option value="parentSelectValue3">Parent Select Option 3</option>
        </select>
      </div>
    </div>

    <div class="form-group">
      <div class="col-sm-8">
        <label>Child/Dependent Control</label>
      </div>
      <div class="col-sm-4">
        <select class="form-control js-double-select-target">
        </select>
      </div>
    </div>
  </div>


</form>

```
*/

  DoubleSelectField = function($field) {
    // inherit from Field
    Field.apply(this, [$field]);

    var self = this;

    this.$source = this.el.find('.js-double-select-trigger');
    this.$target = this.el.find('.js-double-select-target');

    this.$source.on('change', function() {
      self.$target.val([])
      self.update();
    });

    AGN.Lib.DataProvider.get($field).done(function(options) {
      self.options = options;
      self.update();
    });
  }

  // inherit from Field
  DoubleSelectField.prototype = Object.create(Field.prototype);
  DoubleSelectField.prototype.constructor = DoubleSelectField;

  DoubleSelectField.prototype.update = function() {
    var valueSource = this.$source.find('option:selected').val(),
        valueTarget = this.$target.find('option:selected').val(),
        template = '{{ _.forEach(opts, function(name, value) { }}<option value="{{- value }}" {{- ( value == valueSelected ) ? "selected" : "" }}>{{- name }}</option>{{ }); }}';

    template = _.template(template, { opts: this.options[valueSource], valueSelected: valueTarget });
    this.$target.html(template);
    AGN.runAll(this.el);
  }

  AGN.Lib.DoubleSelectField = DoubleSelectField;
  AGN.Opt.Fields['double-select'] = DoubleSelectField;

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

```htmlexample
<form>

  <div data-field="toggle-vis">
    <div class="form-group">
      <div class="col-sm-8">
        <label>Control via Radiobuttons</label>
      </div>
      <div class="col-sm-4">
        <label class="radio">
          <input type="radio" name="hideTargetViaRadio" value="1" checked data-field-vis="" data-field-vis-hide="#exampleRadioTarget">
          Hide Element
        </label>
        <label class="radio">
          <input type="radio" name="hideTargetViaRadio" value="0" data-field-vis="" data-field-vis-show="#exampleRadioTarget">
          Show Element
        </label>
      </div>
    </div>

    <div class="form-group" id="exampleRadioTarget">
      <div class="col-sm-8">
        <label>Disabled when hidden</label>
      </div>
      <div class="col-sm-4">
        <input type="text" class="form-control">
      </div>
    </div>
  </div>

  <div data-field="toggle-vis">
    <div class="form-group">
      <div class="col-sm-8">
        <label>Control via Select</label>
      </div>
      <div class="col-sm-4">
        <select name="hideTargetViaSelect" data-field-vis="" class="form-control">
          <option value="1" data-field-vis-hide="#exampleSelectTarget" selected >
            Hide Target
          </option>
          <option value="2" data-field-vis-show="#exampleSelectTarget">
            Show Target
          </option>
        </select>
      </div>
    </div>

    <div class="form-group" id="exampleSelectTarget">
      <div class="col-sm-8">
        <label>Disabled when hidden</label>
      </div>
      <div class="col-sm-4">
        <input type="text" class="form-control">
      </div>
    </div>
  </div>

  <div data-field="toggle-vis">
    <div class="form-group">
      <div class="col-sm-8">
        <label>Control via Checkbox</label>
      </div>
      <div class="col-sm-4">
        <label class="checkbox">
          <input type="checkbox" checked data-field-vis="" data-field-vis-show="#button1">
          Show Button 1
        </label>
        <label class="checkbox">
          <input type="checkbox" checked data-field-vis="" data-field-vis-show="#button2">
          Show Button 2
        </label>

        <div class="hidden" data-field-vis-default="" data-field-vis-hide="#button1, #button2"></div>
      </div>
    </div>

    <div class="btn-group">
      <a href="#" class="btn btn-regular btn-primary" id="button1">Button 1</a>
      <a href="#" class="btn btn-regular btn-primary" id="button2">Button 2</a>
    </div>
  </div>
</form>

```
*/


  ToggleVisField = function($field) {
    // inherit from Field
    Field.apply(this, [$field]);

    var self = this;

    this.$source = this.el.find('[data-field-vis]');

    this.$source.on('change', function() {
      self.update();
    });

    self.update();
  }

  // inherit from Field
  ToggleVisField.prototype = Object.create(Field.prototype);
  ToggleVisField.prototype.constructor = ToggleVisField;

  ToggleVisField.prototype._updateSelects = function() {
    var self = this;

    _.each(this.$source.filter('select'), function(el) {
      self._toggleFields($(el).find(':selected'));
    })
  }

  ToggleVisField.prototype._updateDefaults = function() {
    var self = this;

    _.each(this.el.find('[data-field-vis-default]'), function(el) {
      self._toggleFields($(el));
    })
  }

  ToggleVisField.prototype._updateCheckboxes = function() {
    var self = this;

    _.each(this.$source.filter(':checked'), function(el) {
      self._toggleFields($(el));
    })
  }

  ToggleVisField.prototype._toggleFields = function($el) {
    var fieldsToHide,
        fieldsToShow;

    if ($el.is(':disabled')) {
      return;
    }

    fieldsToHide = $el.data('field-vis-hide');
    fieldsToShow = $el.data('field-vis-show');

    _.each(this.el.find(fieldsToHide), function(el) {
      $el = $(el);
      $el.all(":input").each(function() {
        var $input = $(this);
        if (!$input.prop('disabled')) {
          $input.data('field-disabled', true);
          $input.prop("disabled", true);
        }
      });
      $el.hide();

      if ($el.data('select2')) {
        $el.select2("container").hide();
        $el.select2("enable",false)
      }

      if ($el.is('option')) {
        $el.prop('disabled', true);
        AGN.Initializers.Select($el.parents('select'))
      }
    });

    _.each(this.el.find(fieldsToShow), function(el) {
      $el = $(el);
      $el.all(":input").each(function() {
        var $input = $(this);
        if ($input.data('field-disabled')) {
          $input.prop("disabled", false);
          $input.removeData('field-disabled');
        }
      });
      $el.show();

      if ($el.data('select2')) {
        $el.select2("container").show();
        $el.select2("enable",true)
      }

      if ($el.is('option')) {
        $el.prop('disabled', false);
        AGN.Initializers.Select($el.parents('select'))
      }
    });
  }

  ToggleVisField.prototype.update = function() {
    this._updateDefaults();
    this._updateSelects();
    this._updateCheckboxes();
  }


  AGN.Lib.ToggleVisField = ToggleVisField;
  AGN.Opt.Fields['toggle-vis'] = ToggleVisField;

/*doc
---
title: Required Field
name: fields-03-required
parent: fields
---

The required field helps with preventing a form from submit, when its field value is blank.

```htmlexample
<form>

  <div class="form-group" data-field="required">
    <div class="col-sm-4">
      <label class="control-label">A required field</label>
    </div>
    <div class="col-sm-4">
      <input type="text" class="form-control" data-field-required="" />
    </div>
  </div>

  <div class="btn-group">
    <a href="#" class="btn btn-regular btn-primary" data-form-submit="">Submit</a>
  </div>
</form>
```
*/

  RequiredField = function($field) {
    // inherit from Field
    Field.apply(this, [$field]);

    this.$target = this.el.find('[data-field-required]');
  };

  // inherit from Field
  RequiredField.prototype = Object.create(Field.prototype);
  RequiredField.prototype.constructor = RequiredField;

  RequiredField.prototype.valid = function() {
    if (this.$target.exists()) {
      return $.trim(this.$target.val());
    } else {
      return true;
    }
  };

  RequiredField.prototype.errors = function() {
    var errors = [];

    if ( this.valid() ) return errors;

    errors.push({
      field: this.$target,
      msg: t('fields.required.errors.missing')
    });

    return errors;
  };

  AGN.Lib.RequiredField = RequiredField;
  AGN.Opt.Fields['required'] = RequiredField;


  PasswordField = function($field) {
    // inherit from Field
    Field.apply(this, [$field]);

    var self = this;

    this.$strength = this.el.find('.js-password-strength');
    this.$strengthIndicator = this.el.find('.js-password-strength-indicator');
    this.$match = this.el.find('.js-password-match');
    this.$matchIndicator = this.el.find('.js-password-match-indicator');

    this.$strength.on('keyup', function() {
      self.updateStrength();
      self.updateMatch();
    });

    this.$match.on('keyup', function() {
      self.updateMatch();
    })
  }

  // inherit from Field
  PasswordField.prototype = Object.create(Field.prototype);
  PasswordField.prototype.constructor = PasswordField;

  PasswordField.prototype.match = function() {
    return this.$strength.val() == this.$match.val();
  }

  PasswordField.prototype.safe = function() {
    var self = this,
        pass = this.$strength.val(),
        checks = ["[a-z]", "[A-Z]", "[0-9]", "[^a-zA-Z0-9]" ];


    if ( pass.length == 0 ) return true;
    if ( pass.length < 8 ) return false;

    return _.all(checks, function(check) {
      return self.passesCheck(pass, check);
    });

  }

  PasswordField.prototype.passesCheck = function(pass, check) {
    if (pass.match(check)) {
      return true;
    }

    return false;
  }

  PasswordField.prototype.valid = function() {
    if ( this.$match.length == 0 ) return true;

    return this.match() && this.safe();
  }

  PasswordField.prototype.errors = function() {
    var errors = [];

    if ( this.valid() ) return errors;

    if (!this.match()) {
      errors.push({
        field: this.$match,
        msg: t('fields.password.errors.notMatching')
      });
    }

    if (!this.safe()) {
      errors.push({
        field: this.$strength,
        msg:  t('fields.password.errors.unsafe')
      });
    }

    return errors;
  }

  PasswordField.prototype.resetStrength = function() {
    var form = AGN.Lib.Form.get(this.el);

    form.cleanErrors(this.$strengthIndicator.parents('.form-group'));

    this.$strengthIndicator.addClass('hidden');
    this.$strengthIndicator.removeClass('state-alert');
    this.$strengthIndicator.removeClass('state-success');
    this.$strengthIndicator.html('');
  }

  PasswordField.prototype.resetMatch = function() {
    var form = AGN.Lib.Form.get(this.el);

    form.cleanErrors(this.$matchIndicator.parents('.form-group'));

    this.$matchIndicator.addClass('hidden');
    this.$matchIndicator.removeClass('state-success');
    this.$matchIndicator.removeClass('state-alert');
    this.$matchIndicator.html('');

  }

  PasswordField.prototype.updateStrength = function() {
    this.resetStrength();

    if (this.$strength.val().length == 0) {
      return;
    }

    this.$strengthIndicator.removeClass('hidden');

    if (this.safe()) {
      this.$strengthIndicator.addClass('state-success');
      this.$strengthIndicator.html(t('fields.password.successHtml', t('fields.password.safe')))
    } else {
      this.$strengthIndicator.addClass('state-alert');
      this.$strengthIndicator.html(t('fields.password.unsuccessHtml', t('fields.password.unsafe')))

    }
  }

  PasswordField.prototype.updateMatch = function() {
    this.resetMatch();

    if (this.$match.val().length == 0) {
      return;
    }

    this.$matchIndicator.removeClass('hidden');

    if (this.match()) {
      this.$matchIndicator.addClass('state-success');
      this.$matchIndicator.html(t('fields.password.successHtml', t('fields.password.matches')))
    } else {
      this.$matchIndicator.addClass('state-alert');
      this.$matchIndicator.html(t('fields.password.unsuccessHtml', t('fields.password.matchesNot')))
    }
  }

  AGN.Lib.PasswordField = PasswordField;
  AGN.Opt.Fields['password'] = PasswordField;

  ValueSplitField = function($field) {
    // inherit from Field
    Field.apply(this, [$field]);

    this.$source = this.el.find('[data-field-split]');
    this.targets = this.$source.data('field-split').split(/,\s?/);
    this.rule    = new RegExp(this.$source.data('field-split-rule'));
  }

  // inherit from Field
  ValueSplitField.prototype = Object.create(Field.prototype);
  ValueSplitField.prototype.constructor = ValueSplitField;

  ValueSplitField.prototype.valid = function() {
    var values,
        form = AGN.Lib.Form.get(this.el);

    values = this.$source.val().split(this.rule);

    _.each(this.targets, function(target, index) {
      form.setValueOnce(target, values[index]);
    })

    return true;
  }

  AGN.Lib.ValueSplitField = ValueSplitField;
  AGN.Opt.Fields['split'] = ValueSplitField;

  FilterField = function($field) {
    var self = this;

    Field.apply(this, [$field]);

    this.$filters = this.el.find('[data-field-filter]');
    this.$filters.on('change', function() {
      self.update()
    });

    self.position();
    self.update();
  };

  // inherit from Field
  FilterField.prototype = Object.create(Field.prototype);
  FilterField.prototype.constructor = FilterField;

  FilterField.prototype.position = function() {
    var $target;

    $target = $(this.el.data('filter-target'));

    // move to target;
    this.el.detach();
    this.el.appendTo($target);

    // refresh table rendering ;
    $target.parents('table').hide();
    $target.parents('table').show();
  };

  FilterField.prototype.isEnabled = function() {
    return this.$filters.get()
      .some(function(e) {
        var $e = $(e);

        if ($e.is('input')) {
          var type = $e.attr('type');
          if (type == 'text' || type == 'number' || type == 'password') {
            return !!$e.val();
          }
        }

        return !!$e.serialize();
      });
  };

  FilterField.prototype.isDisabled = function() {
    return !this.isEnabled();
  };

  FilterField.prototype.update = function() {
    if ( this.isEnabled() ) {
      this.el.addClass('filter-active');
    } else {
      this.el.removeClass('filter-active')
    }
  };

  AGN.Lib.FilterField      = FilterField;
  AGN.Opt.Fields['filter'] = FilterField;

  DateFilterField = function($field) {
    var self = this;

    Field.apply(this, [$field]);

    this.$filterMin = this.el.find('[data-filter-date-min]');
    this.$filterMax = this.el.find('[data-filter-date-max]');
    this.$filters = this.el.find('[data-filter-date-min],[data-filter-date-max]');

    this.$filterMin.on('change', function() {
      var apiMax = self.$filterMax.data('pickadate');

      apiMax.set('min', self.$filterMin.val())
    })

    this.$filterMax.on('change', function() {
      var apiMin = self.$filterMin.data('pickadate');

      apiMin.set('max', self.$filterMax.val())
    })

    this.$filters.on('change', function() {
      self.update()
    });

    self.position();
    self.update();
  }

  // inherit from FilterField
  DateFilterField.prototype = Object.create(FilterField.prototype);
  DateFilterField.prototype.constructor = DateFilterField;

  DateFilterField.prototype.isEnabled = function() {
    return _.any(this.$filters, function(filter) {
      return $(filter).val() != '';
    });
  }


  AGN.Lib.DateFilterField       = DateFilterField;
  AGN.Opt.Fields['date-filter'] = DateFilterField;

  DateSplitField = function($field) {
    var self = this;

    Field.apply(this, [$field]);

    this.$source = this.el.find('[data-field-date-split]');
    this.$source.on('change', function() {
      self.update();
    });
  };

  DateSplitField.prototype = Object.create(Field.prototype);
  DateSplitField.prototype.constructor = DateSplitField;

  DateSplitField.prototype.update = function() {
    var date = null;

    var timestamp = this.$source.val();
    if (timestamp) {
      date = new Date(parseInt(timestamp));
    }

    var $targets = this.el.find('[data-date-split-target]');
    $targets.each(function() {
      var $target = $(this);
      var param = $target.attr('data-date-split-target');
      if (date && param) {
        switch (param.toUpperCase()) {
          case 'MILLISECONDS':
            $target.val(date.getMilliseconds());
            break;
          case 'SECONDS':
            $target.val(date.getSeconds());
            break;
          case 'MINUTES':
            $target.val(date.getMinutes());
            break;
          case 'HOURS':
            $target.val(date.getHours());
            break;
          case 'DAY':
            $target.val(date.getDate());
            break;
          case 'MONTH':
            $target.val(date.getMonth() + 1);
            break;
          case 'YEAR':
            $target.val(date.getFullYear());
            break;
          default:
            $target.val(date.toJSON());
        }
      } else {
        $target.val('');
      }
    });
  };

  AGN.Lib.DateSplitField       = DateSplitField;
  AGN.Opt.Fields['date-split'] = DateSplitField;

})();
