AGN.Lib.Validator.new('max-length-validator', {
  valid: function($e, options) {
    return !this.errors($e, options).length;
  },

  errors: function($e, options) {
    var content = $e.val();
    var errors = [];

    if ($e.hasClass('js-wysiwyg') && !content) {
      content = $('#cke_' + $e.attr("id")).find('iframe').contents().find('body').text();
    }

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
