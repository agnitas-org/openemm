AGN.Lib.Validator.new('mailing-priorities/form', {
  valid: function() {
    if ($('#priorityCount').val() > 0) {
      return true;
    }

    AGN.Lib.Messages.alert('error.mailing.invalidPriorityCount');

    return false;
  },

  errors: function() {
    return [];
  }
});
