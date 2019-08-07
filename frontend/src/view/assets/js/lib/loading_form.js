(function(){

  var LoadingForm,
      Form = AGN.Lib.Form;

  // inherit from Form
  LoadingForm = function($form) {
    Form.apply(this, [$form]);

    AGN.Lib.Loader.show();
    this.submit();
  };

  LoadingForm.prototype = Object.create(Form.prototype);
  LoadingForm.prototype.constructor = LoadingForm;

  LoadingForm.prototype.jqxhr = function() {
    var self = this,
        deferred = $.Deferred();

    this.$form.trigger('form:submit');

    window.setTimeout(function() {
      var jqxhr = $.ajax({
        url: self.url,
        method: self.method,
        data: self.params(),
        loader: self.loader()
      });

      jqxhr.done(function(resp) {
        deferred.resolve.apply(this, arguments);
      });

    }, parseInt(this.$form.data('polling-interval')));

    return deferred.promise();
  };

  LoadingForm.prototype.submit = function() {
    var jqxhr,
        self = this;

    jqxhr = this.jqxhr();
    jqxhr.done(function(resp) {
      self.updateHtml(resp);
    });

    return jqxhr;
  };

  LoadingForm.prototype.updateHtml = function(resp) {
    var $newForm,
        $resp = $(resp);

    $newForm = $resp.filter('[data-form="loading"]').
                  add($resp.find('[data-form="loading"]'));

    if ($newForm.length == 1) {
      this.$form.html($newForm.html());
      AGN.runAll(this.$form);

      this.submit();
    } else {
      AGN.Lib.Loader.hide();
      AGN.Lib.Page.render(resp);
    }
  };

  AGN.Lib.LoadingForm = LoadingForm;
  AGN.Opt.Forms['loading'] = LoadingForm;
})();
