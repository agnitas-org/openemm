AGN.Lib.Validator.new('userform-edit/form', {
    valid: function ($e, options) {
        return !this.errors($e, options).length;
    },
    errors: function ($e, options) {
        var errors = [];
        var formName = $('[name="formName"]');
        if (!formName.val().match(/^[a-zA-Z0-9-_]+$/gi)) {
            errors.push({field: formName, msg: t('userform.error.invalid_name')});
        }

        this.validateUrlField(errors, 'success');
        this.validateUrlField(errors, 'error');
        this.validateTemplateField(errors, $('#successTemplate'), 'SUCCESS');
        this.validateTemplateField(errors, $('#errorTemplate'), 'ERROR');

        return errors;
    },

    validateUrlField(errors, type) {
        const $url = $(`#${type}URL`);
        if ($(`[name="${type}Settings.useUrl"]`).prop('checked') && !AGN.Lib.Helpers.isUrl($url.val())) {
            errors.push({field: $url, msg: t(`userform.error.invalid_${type}Url`)});
        }
    },
  
    validateTemplateField: function(errors, field, typeUpperCase) {
      const type = typeUpperCase.toLowerCase();
      if (!$(`[name="${type}Settings.useUrl"]`).prop('checked') && !field.val().trim()) {
          errors.push({field: $(`#${type}-form-editor .tile-body`), msg: t(`userform.error.invalid_${type}Html`)});
        }
        if (!this.validateDirectives(errors, field)){
            return false;
        }
        if (!this.validateHrefPattern(errors, field, typeUpperCase)) {
            return false;
        }
    },

    validateDirectives: function(errors, field) {
        var res = field.val().match(/#(?:include|parse)/gi);
        if (res) {
            errors.push({field: field, msg: t('userform.error.illegal_directive', res.join(', '))});
            return false;
        }
        return true;
    },
    validateHrefPattern: function(errors, field, type) {
        var value = field.val();
        var res = value.search(/\shref\s*=\s*(["'])\s/gi);
        if (res != -1) {
            var lineNumber = value.slice(0, res).match(/\r?\n/g).length + 1;
            errors.push({field: field, msg: t('userform.error.invalid_link', type, lineNumber)});
            return false;
        }
        return true;
    }
});