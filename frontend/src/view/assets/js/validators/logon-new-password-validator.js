// Used for password change and password reset forms.
AGN.Lib.Validator.new('logon-new-password/form', {
  valid: function() {
    return !this.errors().length;
  },

  errors: function() {
    var errors = [];

    var $input1 = $('#password');
    var $input2 = $('#password-repeat');

    var password = $input1.val();

    if (!$.trim(password)) {  // Check if blank.
      errors.push({
        field: $input1,
        msg: t('fields.required.errors.missing')
      });
    } else if (password != $input2.val()) {  // Check if passwords don't match.
      errors.push({
        field: $input2,
        msg: t('fields.password.errors.notMatching')
      });
    }

    return errors;
  }
});
