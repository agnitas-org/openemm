AGN.Lib.Validator.new('mailing-priorities/form', {
  valid: function() {
    if ($('#priorityCount').val() > 0) {
      return true;
    }

    AGN.Lib.Messages(t('defaults.error'), t('error.mailing.invalidPriorityCount'), 'alert');

    return false;
  },

  errors: function() {
    return [];
  }
});
