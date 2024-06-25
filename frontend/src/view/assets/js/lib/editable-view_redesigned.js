(function () {

  const STATE = {
    MAIN: 'main',
    VISIBLE: 'visible',
    HIDDEN: 'hidden'
  }

  class EditableTile {

    static DATA_KEY = 'agn:editable-tile';
    static SELECTOR = '[data-editable-tile]';

    constructor($el, state) {
      this.$el = $el;
      this.state = state;

      this.toggle();
      this.#setOverlay();
      this.$el.data(EditableTile.DATA_KEY, this);
    }

    static get($needle) {
      const $tile = $needle.closest(EditableTile.SELECTOR);
      return $tile.data(EditableTile.DATA_KEY);
    }

    toggleState() {
      if (this.state === STATE.VISIBLE) {
        this.state = STATE.HIDDEN;
      } else {
        this.state = STATE.VISIBLE;
      }

      this.#setOverlay();
    }

    toggle() {
      this.$el.toggleClass('tile--hidden', this.state === STATE.HIDDEN);
    }

    #setOverlay() {
      const $overlay = AGN.Lib.Template.dom('tile-overlay', {state: this.state});

      if (this.$overlay) {
        this.$overlay.replaceWith($overlay);
      } else {
        this.$el.append($overlay);
      }

      this.$overlay = $overlay;
    }
  }

  AGN.Lib.EditableTile = EditableTile;

  class EditableView {

    static DATA_KEY = 'agn:editable-view';
    static SELECTOR = '[data-editable-view]'

    constructor($el, name) {
      this.$el = $el;
      this.name = name;
      this.inEditMode = false;
      this.$btn = $(`[data-edit-view="${name}"]`);
      this.$btnSiblings = this.$btn.closest('li').siblings('li:visible');

      this.config = AGN.Lib.Storage.get(this.#getStorageKey()) || {};
      this.tiles = this.#initTiles();

      this.#controlContainersVisibility();

      this.$el.removeClass('hidden');
      this.$el.data(EditableView.DATA_KEY, this);

      this.#updateControlViewBtn();
    }

    static get($needle) {
      const $view = $needle.closest(EditableView.SELECTOR);
      return $view.data(EditableView.DATA_KEY);
    }

    enableEditMode() {
      this.inEditMode = true;
      this.$el.addClass('edit-mode');
      this.#controlContainersVisibility();
      this.#updateControlViewBtn();
      this.$btnSiblings.hide();
    }

    applyChanges() {
      this.inEditMode = false;
      this.$el.removeClass('edit-mode');
      this.$btnSiblings.show();

      const tileConfig = {};

      this.tiles.forEach(tile => {
        tile.toggle();

        if (tile.state !== STATE.MAIN) {
          tileConfig[tile.$el.attr('id')] = tile.state;
        }
      });

      this.#controlContainersVisibility();

      AGN.Lib.Storage.set(this.#getStorageKey(), tileConfig);
      AGN.Lib.Messages.success('editableView.saved');

      this.#updateControlViewBtn();
    }

    #getCountOfHiddenTiles() {
      return this.#getTilesByState(STATE.HIDDEN).length;
    }

    #updateControlViewBtn() {
      const $btnText = this.$btn.find('.text');

      if (this.isInEditMode()) {
        $btnText.text(t('editableView.save'));
      } else {
        $btnText.text(t('editableView.edit'));

        const hiddenTilesCount = this.#getCountOfHiddenTiles();
        if (hiddenTilesCount > 0) {
          $btnText.append(` (${hiddenTilesCount} <i class="icon icon-eye-slash"></i>)`);
        }
      }
    }

    toggleState($el) {
      const hiddenTiles = this.#getTilesByState(STATE.HIDDEN);
      const tile = EditableTile.get($el);

      if (hiddenTiles.length === this.tiles.length - 1 && tile.state === STATE.VISIBLE) {
        AGN.Lib.Messages.alert('editableView.tile.error.cantRemove');
      } else {
        tile.toggleState();
      }
    }

    #getTilesByState(state) {
      return this.tiles.filter(tile => tile.state === state);
    }

    isInEditMode() {
      return this.inEditMode;
    }

    #initTiles() {
      const self = this;
      const tilesStates = new Map();

      this.$el.find(EditableTile.SELECTOR).each(function () {
        const $tile = $(this);
        const state = self.getTileState($tile);
        tilesStates.set($tile, state);
      });

      const isAllTilesHidden = [...tilesStates.values()].every(state => state === STATE.HIDDEN);
      return Array.from(tilesStates, ([tile, state]) => new EditableTile(tile, isAllTilesHidden ? STATE.VISIBLE : state));
    }

    getTileState($tile) {
      return $tile.data('editable-tile') || this.config[$tile.attr('id')] || STATE.VISIBLE;
    }

    #controlContainersVisibility() {
      const $containers = this.tiles.flatMap(tile => {
        const $parents = tile.$el.parentsUntil(this.$el);
        return $parents.toArray().map(parent => $(parent));
      });

      $containers.forEach($c => $c.toggleClass('tile-container--hidden', this.#areAllTilesHiddenInContainer($c)));
    }

    #areAllTilesHiddenInContainer($container) {
      if (this.inEditMode) {
        return false;
      }

      return $container.find(EditableTile.SELECTOR)
        .toArray()
        .every(tile => EditableTile.get($(tile)).state === STATE.HIDDEN);
    }

    #getStorageKey() {
      return `editable-view#${this.name}`;
    }
    
    #contains($tile) {
      const editableTile = EditableView.get($tile);
      return editableTile && this.tiles.includes(editableTile);
    }

    add($tile) {
      if (this.#contains($tile)) {
        return;
      }
      const tile = new EditableTile($tile, this.getTileState($tile));
      this.tiles.push(tile);
    }
  }

  AGN.Lib.EditableView = EditableView;

})();
