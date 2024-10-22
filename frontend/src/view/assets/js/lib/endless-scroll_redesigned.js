(() => {

  class EndlessScroll {

    static DATA_ATTR_NAME = 'agn:endless-scroll';

    constructor($el, url, options = {}) {
      this.$el = $el;
      this.url = url;
      this.options = options;
      this.currentPage = 0;
      this.allPagesLoaded = false;
      this.$loader = options.loader ? $(options.loader) : null;

      this.$content = $el.find('[data-endless-scroll-content]');
      if (!this.$content.exists()) {
        this.$content = $el.find('> div:first');
      }

      this.$content.wrap('<div class="endless_scroll_inner_wrap"></div>')

      $el.endlessScroll({
        fireOnce: true,
        fireDelay: 1000,
        loader: '',
        insertAfter: '',
        callback: () => this.loadContent()
      });

      this.loadContent();

      $el.data(EndlessScroll.DATA_ATTR_NAME, this);
    }

    static get($el) {
      return $el.data(EndlessScroll.DATA_ATTR_NAME);
    }

    load(url) {
      this.$content.empty();

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

        // for styleguide
        if (resp instanceof Array) {
          this.$content.append(JSON.stringify(resp));
        }

        this.$content.append($items);
        this.allPagesLoaded = $resp.is('[data-endless-scroll-stop]');

        if (displayLoader) {
          this.$loader.addClass('hidden');
        }

        if (this.options.onLoad) {
          this.options.onLoad($items);
        }
      });

      return jqhxr;
    }
  }

  AGN.Lib.EndlessScroll = EndlessScroll;

})();
