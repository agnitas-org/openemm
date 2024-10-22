(function(){

  /* Static Forms
     submit data without
     using a jqxhr request
  */

  var StaticForm,
      Form = AGN.Lib.Form;

  var CSRF = AGN.Lib.CSRF;

  // inherit from Form
  StaticForm = function($form) {
    Form.apply(this, [$form]);
  };
  StaticForm.prototype = Object.create(Form.prototype);

  StaticForm.prototype.jqxhr = function() {
    return this.form.submit();
  };

  StaticForm.prototype.submit = function() {
    var self = this,
        fieldsMissing = _.merge({}, this._data, this._dataNextRequest);

    if ( !this.valid() ) {
      this.handleErrors();
      return false;
    }

    this.$form.trigger('form:submit');

    if (CSRF.isProtectionEnabled()) {
      self.$form.find('[name="' + CSRF.getParameterName() + '"]').val(CSRF.readActualToken());
    }

    _.each(fieldsMissing, function(value, field) {
      var $input = $('<input type="hidden" name="' + field + '"/>');
      $input.val(value);
      $input.appendTo(self.$form);
    });

    this._dataNextRequest = {};
    this.$form.submit();
    return $.Deferred().promise();
  };

  AGN.Lib.StaticForm = StaticForm;
  AGN.Opt.Forms['static'] = StaticForm;
})();
