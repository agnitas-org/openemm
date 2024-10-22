(() => {
  class Scrollbar {

    static DATA_KEY = 'agn:perfect-scrollbar';
    static MARKER_ATTR_NAME = 'agn-scrollbar'

    static INSTANCES = [];

    constructor($el, options = {}) {
      this.$el = $el;
      this.scrollbar = new PerfectScrollbar($el[0], _.extend(this.options, options));
      $el.attr(Scrollbar.MARKER_ATTR_NAME, '');
      $el.data(Scrollbar.DATA_KEY, this);
      Scrollbar.INSTANCES.push(this);
    }

    get options() {
      return {
        wheelSpeed: 1,
        wheelPropagation: false,
        minScrollbarLength: 30
      };
    }

    destroy() {
      this.scrollbar.destroy();
      this.$el.data(Scrollbar.DATA_KEY, null);
      this.$el.removeAttr(Scrollbar.MARKER_ATTR_NAME);
      delete Scrollbar.INSTANCES[this];
    }

    update() {
      this.scrollbar.update();
    }

    static get($el, searchAncestors = true) {
      const scrollbar = $el.data(Scrollbar.DATA_KEY);
      if (!searchAncestors) {
        return scrollbar;
      }

      return scrollbar || $el.closest(`[${Scrollbar.MARKER_ATTR_NAME}]`).data(Scrollbar.DATA_KEY);
    }

    static updateAll() {
      Scrollbar.INSTANCES.forEach(s => s.update());
    }
  }

  AGN.Lib.Scrollbar = Scrollbar;
})();
