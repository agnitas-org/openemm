(() => {

  class Select {

    static DATA_KEY = 'agn:select';

    constructor($select) {
      this.$select = $select;
      this.select = $select[0];

      this.api = this.$select.data('select2');
    }

    static get($needle) {
      const $select = $needle;
      const selectObj = $select.data(Select.DATA_KEY) || new Select($select);
      $select.data(Select.DATA_KEY, selectObj);

      return selectObj;
    }

    $get() {
      return this.$select;
    }

    values() {
      return _.map(this.$select.find("option"), el => $(el).val());
    }

    hasOption(value) {
      return this.values().indexOf(value) !== -1;
    }

    clear() {
      this.selectValue('');
    }

    toggleDisabled(disabled) {
      this.$select.prop('disabled', disabled);
    }

    selectNext() {
      const values = this.values();
      const selectedValue = this.getSelectedValue();
      const nextIndex = values.indexOf(selectedValue) + 1;

      if (nextIndex < values.length) {
        this.selectValue(values[nextIndex]);
      }
    }

    selectValue(val) {
      this.$select.val(val).trigger('change.select2');

      // NOTE: own hack since select2 not updates dropdown selection properly after input change (v. 4.1.0).
      // issue: https://github.com/select2/select2/issues/6255
      if (this.isMultiple()) {
        AGN.Lib.CoreInitializer.run('select', this.$select);
      }
    }
    
    isMultiple() {
     return this.$select.is('[multiple]');
    }

    unselectValue(value) {
      const newValues = this.$select.val().filter(val => val !== value);
      this.selectValue(newValues)
    }

    getFirstValue() {
      return this.$select.find("option:not(:disabled):first").val();
    }

    selectFirstValue() {
      this.selectValue(this.getFirstValue());
    }

    selectValueOrSelectFirst(values) {
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
    }

    resetOptions() {
      this.$select.empty();
    }

    // format of data: [{id: value, text: "option text"} ...]
    setOptions(data) {
      const options = _.reduce(data, (html, option) => {
        html += `<option value="${option.id}">${option.text}</option>`;
        return html;
      }, "");

      this.$select.html(options);
    }

    addFormattedOption(option) {
      this.$select.append(option);
    }

    getSelectedValue() {
      return this.$select.val() || '';
    }

    addOption(value, text = value) {
      this.$select.append($('<option>', {
        value: value,
        text: text
      }));
    }

    selectOptions(values) {
      values.forEach(value => this.selectOption(value));
    }

    selectOption(value) {
      this.$findOption(value).prop('selected', true);
      this.$select.trigger('change.select2');
    }

    isOptionSelected(value) {
      return this.$findOption(value).prop('selected');
    }

    $findOption(value) {
      return this.$select.find(`option[value="${value}"`);
    }

    getSelectedText() {
      return this.$select.find("option:selected").text();
    }

    disableOptions(values = [], forceSelected) {
      const currentValue = this.$select.val(); // may be disabled too
      
      this.$select.find('option').prop('disabled', false);
      this.$select.find(`option${forceSelected ? '' : ':not(:selected)'}`)
        .filter((i, option) => values.includes($(option).val()))
        .each((i, option) => $(option).prop('disabled', true));
      
      if (forceSelected && !this.isMultiple() && values.includes(currentValue)) {
        this.selectFirstValue();
      }
      
      this.$select.trigger('change.select2');
    }

    disableOption(value, forceSelected) {
      this.disableOptions([value], forceSelected);
    }

    setReadonly(isReadonly) {
      if (isReadonly) {
        this.$select.attr("readonly", "readonly");
      } else {
        this.$select.removeAttr("readonly");
      }
    }
  }

  AGN.Lib.Select = Select;
})();
