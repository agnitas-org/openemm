(function () {
    AGN.Lib.Validator.new('number', {
        valid: function ($e, options) {
            return !this.errors($e, options).length;
        },
        errors: function ($e, options) {
            var errors = [];

            var exactValue = $e.val();
            if (exactValue) {
                var value = options.float === true ? parseFloat(exactValue) : parseInt(exactValue);

                if (isNaN(value)) {
                    errors.push({field: $e, msg: t('fields.errors.number_nan')});
                } else {
                    if (value < options.min) {
                        errors.push({field: $e, msg: t('fields.errors.number_exceed_min', options.min - 1)});
                    } else if (value > options.max) {
                        errors.push({field: $e, msg: t('fields.errors.number_exceed_max', options.max)});
                    } else if (options.strict === true) {
                        if ($.trim(exactValue) !== value.toString()) {
                            if (options.float === true) {
                                // TODO: provide more sophisticated strict validation for decimal numbers.
                                errors.push({field: $e, msg: t('fields.errors.number_not_double')});
                            } else {
                                errors.push({field: $e, msg: t('fields.errors.number_not_integer')});
                            }
                        }
                    }
                }
            } else if (options.required === true) {
                errors.push({field: $e, msg: t('fields.required.errors.missing')});
            }

            return errors;
        }
    });
})();
