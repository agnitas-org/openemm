(function(){

  var ResourceForm,
      Form = AGN.Lib.Form,
      Page = AGN.Lib.Page;

  // inherit from Form
  ResourceForm = function($form) {
    Form.apply(this, [$form]);
    this.resourceSelector = $form.data('resource-selector');
  };
  ResourceForm.prototype = Object.create(Form.prototype);

  ResourceForm.prototype.getResourceSelector = function() {
    if (this.resourceSelectorNextRequest) {
      var value = this.resourceSelectorNextRequest;
      this.resourceSelectorNextRequest = null;
      return value;
    }
    return this.resourceSelector;
  };

  ResourceForm.prototype.setResourceSelectorOnce = function(resourceSelector) {
    this.resourceSelectorNextRequest = resourceSelector;
  };

  ResourceForm.prototype.updateHtml = function(resp) {
    var selector = this.getResourceSelector();
    if (selector) {
      var $resp = $(resp);

      var $target = $(selector);
      var $source = $resp.all(selector);

      if ($target.length == 1 && $source.length == 1) {
        var isInitRequired = false;

        if ($target[0] == this.$form[0] || $.contains($target[0], this.$form[0])) {
          isInitRequired = true;
        }

        $target.html($source.html());

        this.handleMessages($resp);
        if (isInitRequired) {
          this.initFields();
          this.initValidator();
        }

        AGN.Lib.Controller.init($target);
        AGN.runAll($target);
      } else {
        Page.render(resp);
      }
    } else {
      Page.render(resp);
    }
  };

  AGN.Lib.ResourceForm = ResourceForm;
  AGN.Opt.Forms['resource'] = ResourceForm;

})();
