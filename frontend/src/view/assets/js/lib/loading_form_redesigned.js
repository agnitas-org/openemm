(function(){
  const Form = AGN.Lib.Form;

  class LoadingForm extends Form {
    constructor($form) {
      super($form);
      AGN.Lib.Loader.show();
      this.submit();
    }

    jqxhr() {
      const deferred = $.Deferred();

      this.$form.trigger('form:submit');

      window.setTimeout(() => {
        $.ajax({
          url: this.url,
          method: this.method,
          data: this.params(),
          loader: this.loader()
        }).done(function (resp) {
          deferred.resolve.apply(this, arguments);
        });
      }, parseInt(this.$form.data('polling-interval')));

      return deferred.promise();
    }

    submit() {
      const jqxhr = this.jqxhr();
      jqxhr.done(resp => this.updateHtml(resp));

      return jqxhr;
    }

    updateHtml(resp) {
      const $newForm = $(resp).all('[data-form="loading"]')

      if ($newForm.exists()) {
        this.$form.html($newForm.html());
        AGN.runAll(this.$form);

        this.submit();
      } else {
        AGN.Lib.Loader.hide();
        AGN.Lib.Modal.getInstance(this.$form)?.hide()
        AGN.Lib.Page.render(resp);
      }
    }
  }

  AGN.Lib.LoadingForm = LoadingForm;
  AGN.Opt.Forms['loading'] = LoadingForm;
})();
