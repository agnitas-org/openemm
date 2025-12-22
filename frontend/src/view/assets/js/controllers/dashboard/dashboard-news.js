(() => {
  const Template = AGN.Lib.Template;
  const Modal = AGN.Lib.Modal;
  let NEWS_CACHE = [];

  class News {
    constructor() {
      this.$sidebarNewsBtn
        .off('click')
        .on('click', () => this.showNewsModal());
    }

    get $sidebarNewsBtn() {
      return $('#sidebar-news-btn');
    }

    get $sidebarNewsCounter() {
      return this.$sidebarNewsBtn.find('span:nth-child(2)');
    }

    get unreadNewsCount() {
      return parseInt(this.$sidebarNewsCounter?.text());
    }

    showNewsModal() {
      const $modal = Modal.fromTemplate('news-modal-template');
      this.showNews($modal.find('.modal-body'));
      if (this.unreadNewsCount < 1) {
        return;
      }
      this.$sidebarNewsCounter.hide();
      $.post(AGN.url('/administration/popupnews/reset-news-unread-count.action'));
    }

    showNews($el) {
      this.fetchNews().then(news =>
        news.forEach(newsItem => $el.append(Template.dom('news-item-template', newsItem))));
    }

    async fetchNews() {
      return NEWS_CACHE.length
        ? NEWS_CACHE
        : (NEWS_CACHE = await $.get(AGN.url('/administration/popupnews/news.action'))) || [];
    }
  }

  AGN.Lib.Dashboard.News = News;
})();
