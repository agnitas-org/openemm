AGN.Lib.Validator.new('required-id', {
  valid: function($e, options) {
    return !this.errors($e, options).length;
  },

  errors: function($e, options) {
    if ($e.data('select2') && !$e.parent().find('.select2-container').is(":visible")) {
      return [];
    }
    var content = $e.val();
    var value = parseInt(content);
    var errors = [];

    if (isNaN(value) || value.toString() !== content.toString().trim() || value <= 0 && options.positive !== false) {
      errors.push({field: $e, msg: t('fields.required.errors.missing')});
    }

    return errors;
  }
});
