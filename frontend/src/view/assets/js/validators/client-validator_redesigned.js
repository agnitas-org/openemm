(function () {
  AGN.Lib.Validator.new('client/form', {
    valid: function ($e, options, isFieldsValid) {
      return isFieldsValid && !this.errors($e, options).length;
    },
    errors: ($e, options) => {
      const errors = [];

      $('#technical-contact-block input').each(function () {
        const $input = $(this);
        const val = $input.val();

        if (!$.trim(val)) {
          errors.push({field: $input, msg: t('fields.required.errors.missing')})
        } else if (!AGN.Lib.Helpers.isValidEmail(val)) {
          errors.push({field: $input, msg: t('import.columnMapping.error.invalidEmail')});
        }
      });
      return errors;
    }
  });

})();
