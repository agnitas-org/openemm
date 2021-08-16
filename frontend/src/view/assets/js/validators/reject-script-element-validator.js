AGN.Lib.Validator.new('reject-script-element', {
  valid: function($e, options) {
        return !this.errors($e, options).length;
  },
  errors: function ($e, options) {
      var errors = [];

      var scriptElement = new DOMParser()
        .parseFromString($e.val(), 'text/html')
        .querySelector('script');

      if (scriptElement) {
        errors.push({field: $e, msg: t('fields.errors.illegal_script_element')});

      }
      return errors;
  }
});