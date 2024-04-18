(function () {

  class EndlessScroll {

    static DATA_ATTR_NAME = 'agn:endless-scroll';

    constructor($el, url, options = {}) {
      this.$el = $el;
      this.url = url;
      this.options = options;
      this.currentPage = 0;
      this.allPagesLoaded = false;
      this.$loader = options.loader ? $(options.loader) : null;

      $el.endlessScroll({
        fireOnce: true,
        fireDelay: 1000,
        loader: '',
        insertAfter: '.js-endless-scroll .js-endless-scroll-content',
        callback: () => this.loadContent()
      });

      this.loadContent();

      $el.data(EndlessScroll.DATA_ATTR_NAME, this);
    }

    static get($el) {
      return $el.data(EndlessScroll.DATA_ATTR_NAME);
    }

    load(url) {
      this.$el.find('.js-endless-scroll-content').empty();

      if (url) {
        this.url = url;
      }

      this.currentPage = 0;
      this.allPagesLoaded = false;
      this.loadContent();
    }

    loadContent() {
      if (this.allPagesLoaded) {
        return;
      }

      const displayLoader = this.$loader?.exists() && this.$el.is(':visible');

      if (displayLoader) {
        AGN.Lib.Loader.prevent();
        this.$loader.removeClass('hidden');
      }

      const jqhxr = $.get(this.url, {page: this.currentPage + 1});

      jqhxr.done(resp => {
        this.currentPage += 1;
        const $resp = $(resp);
        const $items = $(resp).children();

        this.$el.find('.js-endless-scroll-content').append($items);
        this.allPagesLoaded = $resp.is('[data-endless-scroll-stop]');
        AGN.Lib.Scrollbar.get(this.$el)?.update();

        if (displayLoader) {
          this.$loader.addClass('hidden');
        }

        if (this.options.onLoad) {
          this.options.onLoad($items);
        }
      });

      return jqhxr;
    }
  };

  AGN.Lib.EndlessScroll = EndlessScroll;

})();
