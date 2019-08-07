(function () {
    AGN.Lib.Validator.new('value-range', {
        valid: function ($e, options) {
            return !this.errors($e, options).length;
        },
        errors: function ($e, options) {
            var value = parseInt($e.val()), errors = [];

            if (value < options.min) {
                errors.push({
                    field: $e,
                    msg: t('fields.errors.range')
                });
            } else if (value > options.max) {
                errors.push({
                    field: $e,
                    msg: t('fields.errors.range')
                });
            }
            return errors;
        }
    });
})();