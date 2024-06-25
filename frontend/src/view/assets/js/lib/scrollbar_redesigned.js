(function(){
  class Scrollbar {

    static DATA_KEY = 'agn:perfect-scrollbar';

    constructor($el) {
      this.$el = $el;
      this.scrollbar = new PerfectScrollbar($el[0], {wheelSpeed: 2, wheelPropagation: false, minScrollbarLength: 30});
      $el.data(Scrollbar.DATA_KEY, this);
    }

    destroy() {
      this.scrollbar.destroy();
      this.$el.data(Scrollbar.DATA_KEY, null);
    }

    update() {
      this.scrollbar.update();
    }

    static get($el) {
      return $el.data(Scrollbar.DATA_KEY) || $el.closest('.js-scrollable').data(Scrollbar.DATA_KEY);
    }
  }

  AGN.Lib.Scrollbar = Scrollbar;
})();
