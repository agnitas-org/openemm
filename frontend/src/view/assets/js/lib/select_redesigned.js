(function(){

  const Select = function($select) {
    this.$select = $select;
    this.select  = $select[0];
  };

  Select.get = function($needle) {
    const $select = $needle;
    let selectObj = $select.data('_select');

    if (!selectObj) {
      selectObj = new Select($select);
      $select.data('_select', selectObj);
    }

    return selectObj;
  };

  // map of {value: option text}
  Select.prototype.map = function() {
    const self = this;

    return _.reduce(self.values(), function(obj, val) {
      obj[val] = self.$select.find("option[value='" + val +"']").html();
      return obj;
    }, {});
  };

  // array of values
  Select.prototype.values = function() {
    return _.map(this.$select.find("option"), el => $(el).val());
  };

  Select.prototype.hasOption = function (value) {
    return this.values().indexOf(value) !== -1;
  }

  Select.prototype.clear = function() {
    this.selectValue('');
  };

  Select.prototype.selectNext = function () {
    const values = this.values();
    const selectedValue = this.getSelectedValue();
    const nextIndex = values.indexOf(selectedValue) + 1;

    if (nextIndex < values.length) {
      this.selectValue(values[nextIndex]);
    }
  }

  Select.prototype.selectValue = function(val) {
    this.$select.val(val).trigger('change');

    // NOTE: own hack since select2 not updates dropdown selection properly after input change (v. 4.1.0).
    // issue: https://github.com/select2/select2/issues/6255
    if (this.$select.is('[multiple]')) {
      const adapter = this.$select.data('select2').dataAdapter;

      this.$select.find('option').each(function () {
        const $option = $(this);
        const optionData = adapter.item($option);

        if ($option.is(":selected")) {
          adapter.select(optionData);
        } else {
          adapter.unselect(optionData);
        }
      });
    }
  };

  Select.prototype.getFirstValue = function() {
    return this.$select.find("option:not(:disabled):first").val();
  };

  Select.prototype.selectFirstValue = function() {
    this.$select.val(this.getFirstValue()).trigger('change');
  };

  Select.prototype.selectValueOrSelectFirst = function(values) {
    const valuesAvailable = this.values();

    if (!_.isArray(values)) {
      values = [values];
    }

    const allValuesFound = _.every(values, (val) => {
      return valuesAvailable.indexOf(val + "") != -1
    });

    if (allValuesFound && values.length != 0) {
      this.selectValue(values);
    } else {
      this.selectFirstValue();
    }
  };

  Select.prototype.resetOptions = function() {
    this.$select.empty();
  };

  // format of data: [{id: value, text: "option text"} ...]
  Select.prototype.setOptions = function(data) {
    const options = _.reduce(data, (html, option) => {
      html += `<option value="${option.id}">${option.text}</option>`;
      return html;
    }, "");

    this.$select.html(options);
  };

  Select.prototype.addFormattedOption = function(option) {
    this.$select.append(option);
  };

  Select.prototype.getSelectedValue = function() {
    return this.$select.val() || '';
  };

  Select.prototype.addOption = function (value, text = value) {
    this.$select.append($('<option>', {
      value: value,
      text: text
    }));
  }

  Select.prototype.selectOptions = function (values) {
    values.forEach(value => this.selectOption(value));
  }
  
  Select.prototype.selectOption = function (value) {
    this.$findOption(value).prop('selected', true);
    this.$select.trigger('change');
  }

  Select.prototype.isOptionSelected = function (value) {
    return this.$findOption(value).prop('selected');
  }

  Select.prototype.$findOption = function (value) {
    return this.$select.find(`option[value="${value}"`);
  }

  Select.prototype.getSelectedText = function () {
    return this.$select.find("option:selected").text();
  }

  Select.prototype.disableOptions = function (values = [], forceSelected) {
    this.$select.find('option').prop('disabled', false);
    this.$select.find(`option${forceSelected ? '': ':not(:selected)'}`)
      .filter((i, option) => values.includes($(option).val()))
      .each((i, option) => $(option).prop('disabled', true));
    if (forceSelected) {
      this.selectFirstValue();
    }
    this.$select.trigger('change', [{programmatic: true}]);
  }

  Select.prototype.disableOption = function (value, forceSelected) {
    this.disableOptions([value], forceSelected);
  }

  Select.prototype.setReadonly = function (isReadonly) {
    if (isReadonly) {
      this.$select.attr("readonly", "readonly");
    } else {
      this.$select.removeAttr("readonly");
    }
  }

  AGN.Lib.Select = Select;
})();
