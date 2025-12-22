AGN.Lib.Validator.new('upload-file/form', {
  valid: function (uploadForm, options, isFieldsValid) {
    let isSuccessfully = true;
    const $form = uploadForm.get$();

    if ($form.data('upload-selection').length < 1) {
      AGN.Lib.Messages.alert('error.upload.file');
      isSuccessfully = false;
    }

    return isFieldsValid && isSuccessfully && !this.errors(uploadForm, options).length;
  },

  errors: function (uploadForm, options) {
    const NAME_TOO_SHORT_MSG_KEY = 'error.workflow.shortName';
    const REQUIRED_MSG_KEY = 'fields.required.errors.missing';

    var validationRules = [{
      field: $('#name'),
      minLength: {val: 3, msgKey: NAME_TOO_SHORT_MSG_KEY},
      maxLength: 99,
    }, {
      field: $('#firstName'),
      minLength: {val: 3, msgKey: NAME_TOO_SHORT_MSG_KEY},
      maxLength: 99
    }, {
      field: $('#phone'),
      minLength: {val: 1, msgKey: REQUIRED_MSG_KEY},
      maxLength: 99,
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
      if (!value || value.length < 1) {
        errors.push({
          field: field,
          msg: t(REQUIRED_MSG_KEY)
        });
        return;
      }

      /* checking min length */
      if (rule.minLength && value.length < rule.minLength.val) {
        errors.push({field: field, msg: t(rule.minLength.msgKey)});
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
