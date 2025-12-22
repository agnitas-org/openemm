(function(){
  const Form = AGN.Lib.Form;

  /** Static Forms submits data without using a jqxhr request */
  class StaticForm extends Form {
    constructor($form) {
      super($form);
    }

    jqxhr() {
      return this.form.submit();
    }

    submit() {
      if (!this.valid()) {
        this.handleErrors();
        return false;
      }

      const fieldsMissing = _.merge({}, this._data, this._dataNextRequest);
      this.$form.trigger('form:submit');

      AGN.Lib.CSRF.updateTokenInDOM(this.$form);

      _.each(fieldsMissing, (value, field) => {
        const $input = $(`<input type="hidden" name="${field}"/>`);
        $input.val(value);
        $input.appendTo(this.$form);
      });

      this._dataNextRequest = {};
      this.$form.submit();
      return $.Deferred().promise();
    }
  }

  AGN.Lib.StaticForm = StaticForm;
  AGN.Opt.Forms['static'] = StaticForm;
})();
