(function(){

  var PollingForm,
      Page = AGN.Lib.Page,
      Form = AGN.Lib.Form;

  // inherit from Form
  PollingForm = function($form) {
    Form.apply(this, [$form]);
  };

  PollingForm.prototype = Object.create(Form.prototype);
  PollingForm.prototype.constructor = PollingForm;

  PollingForm.prototype.jqhxr = function() {
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
        var $resp = $(resp);
        var $newForm = $resp.all('[data-form="polling"]');

        if ($newForm.length == 1) {
          self.$form.data('polling-interval', $newForm.data('polling-interval'));
          self.jqhxr().done(function() {
            deferred.resolve.apply(this, arguments);
          })
        } else {
          deferred.resolve.apply(this, arguments);
        }
      })

    }, parseInt(this.$form.data('polling-interval')));

    return deferred.promise();
  };

  PollingForm.prototype.submit = function() {
    var jqxhr,
        self = this;

    AGN.Lib.Loader.show();
    jqxhr = this.jqxhr();

    jqxhr.done(function(resp) {
      AGN.Lib.Loader.hide();
      self.updateHtml(resp);
    });

    return jqxhr;
  };

  PollingForm.prototype.updateHtml = function(resp) {
    var $newForm,
        $resp = $(resp),
        renderMode = this.$form.data('render');

    if (renderMode == 'page') {
      Page.render(resp);
    } else {
      if (this.$form.attr('id')) {
        $newForm = $resp.all('#' + this.$form.attr('id'));
      } else if (this.$form.attr('name')) {
        $newForm = $resp.all('[name="' + this.$form.attr('name') + '"]');
      } else {
        $newForm = $resp.all('form');
      }

      setTimeout(function() {
        AGN.Lib.Controller.init();
        AGN.runAll();
      }, 100);

      this.handleMessages(resp);
      this.$form.replaceWith($newForm);
    }
  };

  AGN.Lib.PollingForm = PollingForm;
  AGN.Opt.Forms['polling'] = PollingForm;
})();
