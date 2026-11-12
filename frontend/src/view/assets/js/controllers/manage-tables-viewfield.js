class RefTableFieldController extends AGN.Lib.Controller {
  constructor() {
    super();
    this.previousValueNullableField = "true";

    this.addAction({change: 'typeFieldChangeAction'}, () => this.changeType());
    this.addAction({click: 'save'}, () => this.save());
  }

  get $defaultValue() {
    return $('[name="defaultValue"]');
  }

  get defaultValue() {
    return this.$defaultValue.val();
  }

  get $type() {
    return $('#type');
  }

  get $now() {
    return $('#now-checkbox');
  }

  get isNow() {
    return this.$now.is(':checked');
  }

  get $nullable() {
    return $('#nullable');
  }

  get isRequired() {
    return !this.$nullable.is(':checked');
  }

  get type() {
    return this.$type.val();
  }

  get isDateTypeSelected() {
    return this.type === 'DATE';
  }

  get isDateTimeTypeSelected() {
    return this.type === 'DATETIME';
  }

  get isDateOrDatetimeTypeSelected() {
    return this.isDateTypeSelected || this.isDateTimeTypeSelected;
  }

  changeType() {
    if (this.isDateOrDatetimeTypeSelected) {
      this.previousValueNullableField = this.$nullable.val();
      this.$nullable.val('true');
    } else {
      this.$nullable.val(this.previousValueNullableField);
    }
  }

  getDefaultValueFromDates() {
    if (this.isNow) {
      return 'NOW';
    }
    if (this.isDateTypeSelected) {
      return $('#default-date input').val();
    }
    if (this.isDateTimeTypeSelected) {
      return AGN.Lib.Field.get($('#default-datetime')).strValue;
    }
    return '';
  }

  save() {
    if (this.isDateOrDatetimeTypeSelected) {
      this.$defaultValue.val(this.getDefaultValueFromDates());
    }
    const form = AGN.Lib.Form.get(this.$type);
    if (this.isRequired && !this.defaultValue) {
      form.showFieldError('defaultValue', t('fields.required.errors.missing'));
      return;
    }
    form.submit();
  }
}

AGN.Lib.Controller.new('manage-tables-viewfield', RefTableFieldController);
