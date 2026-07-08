AGN.Lib.Validator.new('max-length-validator', {
  valid: function($e, options) {
    return !this.errors($e, options).length;
  },

  errors: function($e, options) {
    const content = $e.val();
    const errors = [];

    if (content) {
      if (content.length > options.max) {
        errors.push({
          field: $e,
          msg: t('fields.errors.contentLengthExceedsLimit', options.max)
        });
      }
    } else if (options.required === true){
      errors.push({
        field: $e,
        msg: t('fields.required.errors.missing')
      });
    }
    return errors;
  }
});
