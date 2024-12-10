/*doc
---
title: Editable view
name: editable-view
category: Javascripts - Editable View
---

This directive is used to hide or show tiles in the view.

On the container (mostly `.tiles-container`) you need to add the `data-editable-view` attribute and specify the name of the view.

Tiles that are inside must have a unique ID, as well as a `data-editable-tile` attribute.
If the tile is the main one and should not be hidden, then the value `main` must be added inside the `data-editable-tile` attribute.

To control the editing mode, you need to create a button. For it you need to specify the `data-edit-view` attribute and specify the name of the view there.

```htmlexample
<button class="btn btn-primary w-100 mb-2" type="button" data-edit-view="styleguide">
    <i class="icon icon-edit"></i>
    <span class="text">Edit view</span>
</button>

<div class="tiles-container" style="height: 500px;" data-editable-view="styleguide">
    <div id="tile-1" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title">Tile1</h1>
        </div>
        <div class="tile-body">
            Tile content!
        </div>
    </div>

    <div class="tiles-block flex-column">
        <div id="tile-2" class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title">Tile2</h1>
            </div>
            <div class="tile-body">
                Tile content!
            </div>
        </div>

        <div id="tile-3" class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title">Tile3</h1>
            </div>
            <div class="tile-body">
                Tile content!
            </div>
        </div>
    </div>
</div>
```

*/

(() => {

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
      this.setOverlay();
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

      this.setOverlay();
    }

    toggle() {
      this.$el.toggleClass('tile--hidden', this.state === STATE.HIDDEN);
    }

    setOverlay() {
      const $overlay = AGN.Lib.Template.dom('tile-overlay', {state: this.state});

      if (this.$overlay && document.body.contains(this.$overlay[0])) {
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

      this.$el.addClass('is-initialized');
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
      const editableTile = EditableTile.get($tile);
      return editableTile && this.tiles.includes(editableTile);
    }

    add($tile) {
      if (this.#contains($tile)) {
        EditableTile.get($tile).setOverlay();
        return;
      }
      const tile = new EditableTile($tile, this.getTileState($tile));
      this.tiles.push(tile);
      this.#controlContainersVisibility();
      this.#updateControlViewBtn();
    }
  }

  AGN.Lib.EditableView = EditableView;

})();
