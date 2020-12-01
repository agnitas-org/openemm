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

        this.validateTemplateField(errors, $('#successTemplate'), 'SUCCESS');
        this.validateTemplateField(errors, $('#errorTemplate'), 'ERROR');

        return errors;
    },
    validateTemplateField: function(errors, field, type) {
        if (!this.validateDirectives(errors, field)){
            return false;
        }
        if (!this.validateHrefPattern(errors, field, type)) {
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