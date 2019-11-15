AGN.Lib.Validator.new('length', {
  valid: function($e, options) {
    return !this.errors($e, options).length;
  },

  errors: function($e, options) {
    var content = $e.val();
    var errors = [];

    if (content) {
      if (options.min >= 0 && content.length < options.min) {
        errors.push({field: $e, msg: t('fields.errors.string_exceed_min_length', options.min)});
      } else if (options.max >= 0 && content.length > options.max) {
        errors.push({field: $e, msg: t('fields.errors.string_exceed_max_length', options.max)});
      }
    } else if (options.min > 0 || options.required === true) {
      errors.push({field: $e, msg: t('fields.required.errors.missing')});
    }

    return errors;
  }
});
