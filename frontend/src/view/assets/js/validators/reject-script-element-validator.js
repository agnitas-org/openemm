AGN.Lib.Validator.new('reject-script-element', {
  valid: function($e, options) {
    return !this.errors($e, options).length;
  },

  errors: function ($e, options) {
    var errors = [];

    var doc = new DOMParser().parseFromString($e.val(), 'text/html');
    var scripts = doc.querySelectorAll('script');
    Array.from(scripts).some(function(script) {
      var attrs = Array.from(script.attributes);

      var type = (script.getAttribute('type') || '')
              .toLowerCase()
              .trim();

      if (type !== 'application/ld+json') {
        errors.push({
          field: $e,
          msg: t('fields.errors.illegal_script_type')
        });
        return true;
      }
      return false;
    });

    return errors;
  }
});