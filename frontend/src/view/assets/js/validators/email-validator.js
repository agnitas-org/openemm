AGN.Lib.Validator.new('email-validator', {
  valid: function ($e, options) {
    return !this.errors($e, options).length;
  },
  errors: function ($e, options) {
    const emails = $e.is('select[multiple]')
      ? $e.val()
      : $e.val().split(',');

    if (!emails.length && options.required) {
      return [{field: $e, msg: t('fields.required.errors.missing')}];
    }

    if (AGN.Lib.Helpers.isValidEmails(emails)) {
      return [];
    }

    return [{field: $e, msg: t('defaults.invalidEmail')}];
  }
});
