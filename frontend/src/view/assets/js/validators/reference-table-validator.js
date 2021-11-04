(function () {
  AGN.Lib.Validator.new('reference-table/form', {

    valid: function ($e, options) {
      return !this.errors($e, options).length;
    },

    errors: function ($e, options) {
      var errors = [];
      var $name = $('[name="shortname"]');
      var $keyColumn = $('[name="keyColumn"]');
      
      if ($name.length) validateTextField($name, errors, 5, options.isVoucher ? 27 : 29, new RegExp('^[a-z]*$', 'i'),
          'referenceTables.table.error.name');
      if ($keyColumn.length) validateTextField($keyColumn, errors, 3, 30, /^(?![0-9_])[a-z0-9_]*$/,
          'referenceTables.table.error.keyColumn');
      return errors;
    }
  });

  function validateTextField($field, errors, minLength, maxLength, regex, errorMessage) {
    if (!validateFieldLength($field, errors, minLength, maxLength)) {
      return;
    }
    validateFieldPattern($field, errors, regex, errorMessage);
  }

  function validateFieldLength($field, errors, min, max) {
    var lengthErrors = AGN.Lib.Validator.get('length').errors($field, {min: min, max: max, required: true});
    if (lengthErrors.length) {
      lengthErrors.forEach(function (error) {
        errors.push(error)
      });
      return false;
    }
    return true;
  }

  function validateFieldPattern($field, errors, regex, errorMessage) {
    if (!$field.val().match(regex)) {
      errors.push({field: $field, msg: t(errorMessage)});
    }
  }
})();
