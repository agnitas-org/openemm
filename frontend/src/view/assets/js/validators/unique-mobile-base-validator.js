AGN.Lib.Validator.new('unique-mobile-base', {
  valid: function($e, options) {
    return !this.errors($e, options).length;
  },

  errors: function($e, options) {
    var value = AGN.Lib.Select.get($e).getSelectedValue();
    var id = $e.attr('id');
    var errors = [];

    if (!this.isUniqueMobileBase(value, id)) {
      errors.push({field: $e, msg: t('fields.mediapool.errors.mobile_base_duplicate')});
    }

    return errors;
  },

  isUniqueMobileBase: function(value, excludeId) {
    if (value == '') {
      return true;
    }
    var duplicates = $('[id^=image-mobile-base]:not(#' + excludeId + ')')
      .filter(function() {
        var $elem = $(this);
      return AGN.Lib.Select.get($elem).getSelectedValue() === value;
    });
    return duplicates.length === 0;
  }
});