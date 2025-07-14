/*doc
---
title: DateTime Field
name: fields-07-date-time
parent: fields
---

Creates 2 fields for selecting date and time. When submitting a form, it combines them into one property, the name of which must be specified in the `data-property` attribute.

Field options:

Option                     |Description                                                                      |
---------------------------|---------------------------------------------------------------------------------|
`value: '4/8/2024 18:55'`  |Value to be shown on initialization                                              |
`defaultSubmitTime:'12:34'`|Will set specified hours:minutes value on submit. Use `'now'` to set current time|

```htmlexample
<form class="d-flex flex-column gap-2">
  <div>
    <label class="form-label">Without options</label>
    <div data-field="datetime" data-property="sendDate" data-field-options="value: '4/8/2024 18:55'"></div>
  </div>

  <div>
    <label class="form-label">Without default value</label>
    <div data-field="datetime" data-property="sendDate1" data-field-options="value: '4/8/2024 18:55'"></div>
  </div>

  <div>
    <label class="form-label">With default value</label>
    <div data-field="datetime" data-property="sendDate2" data-field-options="value: '', defaultValue: '1/1/2024 14:00'"></div>
  </div>
</form>
```
*/

(() => {
  const TIME_FORMAT = 'HH:mm';

  class DateTimeField extends AGN.Lib.Field {
    constructor($field) {
      super($field);
      this._value = this.options.value || this.options.defaultValue || '';
      this._propertyName = this.el.data('property');

      this.#prepareDatetimeInput();
      this.el.on('change', () => this.setDateTimeFieldValue());
    }

    get dateValue() {
      return moment(this.strValue, `${window.adminDateFormat.toUpperCase()} ${TIME_FORMAT}`).toDate();
    }

    get strValue() {
      return this.$hiddenInput.val();
    }

    get $hiddenInput() {
      return this.el.find(`[name="${this._propertyName}"]`);
    }

    setDateTimeFieldValue() {
      this.$hiddenInput.val(this.#prepareDateTimeValue());
    }

    valid() {
      return this.errors().length === 0;
    }

    errors() {
      const $date = this.#getDate$();
      const $time = this.#getTime$();

      if (!$date.val() && $time.val()) {
        return [{
          field: $date,
          msg: t('fields.dateTime.errors.incompleteDate')
        }];
      }

      return [];
    }

    onSubmit() {
      this.setDateTimeFieldValue();
    }

    #prepareDateTimeValue() {
      let date = this.#getDate$().val();
      const time = this.#getTime$().val() || this.#getDefaultSubmitTime();

      if (!date) {
        return '';
      }
      return this.#formatDateTime(date, time.replace(/[_hm]/g, '0'));
    }

    #getDefaultSubmitTime() {
      let defaultSubmitTime = this.options.defaultSubmitTime;
      if (defaultSubmitTime !== '' && !defaultSubmitTime) {
        return '00:00';
      }
      if (defaultSubmitTime === 'now') {
        return moment().format(TIME_FORMAT);
      }
      return defaultSubmitTime;
    }

    #getDate$() {
      return this.el.find('.js-datepicker');
    }

    #getTime$() {
      return this.el.find('.js-timepicker');
    }

    #formatDateTime(date, time) {
      if (/^(\d{2}):(\d{2})$/.test(time)) {
        return `${date} ${time}`;
      }

      return date;
    }

    #prepareDatetimeInput() {
      const extraAttrs = this.el.data('field-extra-attributes');
      const extraAttrsMap = extraAttrs
        ? AGN.Lib.Helpers.objFromString(extraAttrs) || {}
        : {};

      const $input = $('<input>', {
        type: 'hidden',
        name: this._propertyName,
        value: this._value
      });

      $.each(extraAttrsMap, (key, value) => $input.attr(key, value));

      this.el.append(AGN.Lib.Template.text('datetime-picker', {
        date: this._value ? this._value.split(' ')[0] : '',
        time: this.#getTimeFromValue(),
        timeMask: this.options?.timeMask,
        extraAttrs: extraAttrs || ''
      }));

      this.el.find('.js-datepicker').data('datepicker-options', this.el.data('datepicker-options'));

      this.el.append($input);
      AGN.runAll(this.el);
    }

    #getTimeFromValue() {
      if (!this._value) {
        return '';
      }

      const parts = this._value.split(' ');
      return parts.length === 2 ? parts[1] : '';
    }
  }

  AGN.Lib.DateTimeField = DateTimeField;
  AGN.Opt.Fields['datetime'] = DateTimeField;
})();
