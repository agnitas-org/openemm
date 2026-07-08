(() => {

  const SELECTOR = '#main-loader';

  let hiding,
    requestCount = 0,
    prevent = false;

  class Loader {

    static show() {
      window.clearTimeout(hiding);

      if (!prevent) {
        requestCount += 1;
        $(SELECTOR).removeClass('hidden');
      } else {
        prevent = false;
      }
    }

    static hide() {
      requestCount -= 1;

      if (requestCount <= 0) {
        hiding = window.setTimeout(() => $(SELECTOR).addClass('hidden'), 5);
        requestCount = 0;
      }
    }

    static prevent() {
      prevent = true;
    }
  }

  AGN.Lib.Loader = Loader;

  class ProcessingLoader {
    constructor($el, formSelector) {
      if ($el.data('emm-loader')) {
        return $el.data('emm-loader');
      }
      this.$el = $el;
      this.$form = $(formSelector);
      this.$form.on("form:abort", () => this.$loader.hide());
      this.$loader = AGN.Lib.Template.dom('processing-loader', { targetForm: formSelector });
      this.$el.data('emm-loader', this);
    }

    show() {
      prevent = true;
      this.$el.hide().before(this.$loader.show());
    }

    hide() {
      this.$loader.hide();
      this.$el.show();
      prevent = false;
    }
  }

  AGN.Lib.ProcessingLoader = ProcessingLoader;
})();
