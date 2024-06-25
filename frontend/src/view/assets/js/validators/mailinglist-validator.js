AGN.Lib.Validator.new('mailinglist-exist', {
  valid: function ($e, options) {
    return !this.errors($e, options).length;
  },
  errors: function ($e, options) {
    var content = $e.val();
    var errors = AGN.Lib.Validator.get('required-id').errors($e, options);

    if (errors && errors.length <= 0 && options && content && options.removedMailinglistId == content.trim()) {
      errors.push({field: $e, msg: t('fields.mailinglist.errors.removed')});
    }
    return errors;
  }
});
