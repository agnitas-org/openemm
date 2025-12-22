(() => {
  const Storage = AGN.Lib.Storage;
  const COLLAPSED_CLASS = 'tile--collapsed';

  class Tile {
    constructor($el) {
      this.$el = $el.closest('.tile');
      this.$toggle = this.$el.all('[data-toggle-tile]');
      this.toggleCollapse(this.initiallyHidden);
      this.$el.data('tile', this);
    }

    get initiallyHidden() {
      const id = this.$el.attr('id');
      const conf = id ? Storage.get('toggle_tile#' + id) : undefined;
      return conf ? conf.hidden : this.$toggle.data('toggle-tile') === false;
    }

    get collapsed() {
      return !this.$el.hasClass(COLLAPSED_CLASS);
    }

    toggleCollapse(hidden = this.collapsed) {
      this.$el.toggleClass(COLLAPSED_CLASS, hidden);
      this.#rotateIcon(hidden);

      if (this.$el.attr('id')) {
        Storage.set(`toggle_tile#${this.$el.attr('id')}`, {hidden});
      }
      if (!hidden) {
        AGN.Lib.CoreInitializer.run('load', this.$el.find('.tile-body')); // Load lazy data if any
        AGN.Lib.Scrollbar.get(this.$el)?.update();
      }
    }

    #rotateIcon(hidden) {
      this.$el.find('> .tile-header .tile-title i.icon')
        .toggleClass('icon-caret-down', hidden)
        .toggleClass('icon-caret-up', !hidden);
    }

    static get($el) {
      const tile = $el.closest('.tile').data('tile');
      return !tile ? new Tile($el) : tile;
    }
  }

  AGN.Lib.Tile = Tile;
})();
