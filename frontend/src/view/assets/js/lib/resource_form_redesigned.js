(function(){
  const Form = AGN.Lib.Form;
  const Page = AGN.Lib.Page;

  class ResourceForm extends Form {
    constructor($form) {
      super($form);
      this.resourceSelector = $form.data('resource-selector');
    }

    getResourceSelector() {
      if (this.resourceSelectorNextRequest) {
        var value = this.resourceSelectorNextRequest;
        this.resourceSelectorNextRequest = null;
        return value;
      }
      return this.resourceSelector;
    }

    setResourceSelectorOnce(resourceSelector) {
      this.resourceSelectorNextRequest = resourceSelector;
    }

    updateHtml(resp) {
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
          this.handleMessages(resp, true);
        }
      } else {
        Page.render(resp);
        this.handleMessages(resp, true);
      }
    }
  }

  AGN.Lib.ResourceForm = ResourceForm;
  AGN.Opt.Forms['resource'] = ResourceForm;
})();