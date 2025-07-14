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

})();
