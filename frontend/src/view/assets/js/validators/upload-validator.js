AGN.Lib.Validator.new('upload-file/form', {
  valid: function (uploadForm, options) {
    var isSuccessfully = true;
    var $upload = $('[data-initializer="upload"]');

    if ($upload.exists() && $upload.data('upload-selection').length < 1) {
      AGN.Lib.Messages(t('defaults.error'), t('error.upload.file'), 'alert');
      isSuccessfully = false;
    }

    return isSuccessfully && !this.errors(uploadForm, options).length;
  },

  errors: function (uploadForm, options) {
    var validationRules = [{
      field: $('#name'),
      maxLength: 99
    }, {
      field: $('#firstName'),
      maxLength: 99
    }, {
      field: $('#phone'),
      maxLength: 99
    }, {
      field: $('#email'),
      maxLength: 99,
      regexpRules: [{
        regexp: /\S+@\S+\.\S{2,}/,
        message: t('error.upload.email')
      }]
    }];

    var errors = [];
    validationRules.forEach(function (rule) {
      var field = rule.field;
      var value = field.val();

      /* checking empty string */
      if (!value || value.length < 3) {
        errors.push({
          field: field,
          msg: t('fields.required.errors.missing')
        });
        return;
      }

      /* checking length */
      if (rule.maxLength && value.length > rule.maxLength) {
        errors.push({
          field: field,
          msg: t('fields.errors.contentLengthExceedsLimit', rule.maxLength)
        });
        return;
      }

      /* checking by regexps */
      if (rule.regexpRules && rule.regexpRules.length > 0) {
        for (var i = 0; i < rule.regexpRules.length; ++i) {
          var regexpRule = rule.regexpRules[i];
          if (!value.match(regexpRule.regexp)) {
            errors.push({
              field: field,
              msg: regexpRule.message
            });
            return;
          }
        }
      }
    });

    return errors;
  }
});
