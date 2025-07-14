(() => {

  class NewsTile extends AGN.Lib.Dashboard.DraggableTile {
    static ID = 'news';

    constructor(controller) {
      super(controller);
      this.dashboardNews = new AGN.Lib.Dashboard.News();
    }

    afterTileAdded() {
      super.afterTileAdded();
      this.dashboardNews.showNews(this.$el.find('> .tile-body'));
      this.dashboardNews.$sidebarNewsBtn.hide();
    }

    remove() {
      super.remove();
      this.dashboardNews.$sidebarNewsBtn.show(this.shown);
    }
  }

  AGN.Lib.Dashboard.NewsTile = NewsTile;
})();
