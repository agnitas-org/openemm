/*doc
---
title: Tile Toggles
name: tilesjs
category: Javascripts - Tile Toggles
---

Tiles can be opened/closed  by the `data-toggle-tile="jquerySelector"` directive. The directive should be attached to an anchor and will toggle the corresponding tile content.

The tiles save their state into localstorage under the jquerySelector provided to the directive. Thus if the same selector is used under different views those tiles share the same state.

The default state can be controlled by adding/removing the class `hidden` to the tile content.

For showing the direction you can use an icon in the anchor: `<i class="tile-toggle icon icon-caret-up"` or `<i class="tile-toggle icon icon-caret-down"`. The icon will be automatically adapted to the current state, however for the default state it's relevant to use the correct icon.

```htmlexample
<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tileclosed">
            <i class="tile-toggle icon icon-caret-down"></i>
            Tile Closed by Default
        </a>
    </div>

    <div class="tile-content tile-content-forms hidden" id="tileclosed">
      <p>Tile Content</p>
    </div>
</div>

<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tileopen">
            <i class="tile-toggle icon icon-caret-up"></i>
            Tile Open by Default
        </a>
    </div>

    <div class="tile-content tile-content-forms" id="tileopen">
      <p>Tile Content</p>
    </div>
</div>
```
*/

/*doc
---
title: Tiles & tabs
name: tilesjs-01-tabs
parent: tilesjs
---

Clicking on a tab of a closed tile will automatically open the tile

```htmlexample
<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tiletab">
            <i class="tile-toggle icon icon-caret-down"></i>
            Tile
        </a>
        <ul class="tile-header-nav">
            <li>
                <a href="#" data-toggle-tab="#tiletab1-id">Tab 1</a>
            </li>
            <li>
                <a href="#" data-toggle-tab="#tiletab2-id">Tab 2</a>
            </li>
        </ul>
    </div>

    <div class="tile-content tile-content-forms hidden" id="tiletab">
      <div id="tiletab1-id">
        <p>Tab 1 Content</p>
      </div>

      <div id="tiletab2-id">
        <p>Tab 2 Content</p>
      </div>
    </div>
</div>
```
*/

(function(){

  const Storage = AGN.Lib.Storage;

  function turnIcon($tile, hidden) {
    const $icon = $tile.find('.tile-header .tile-title .icon');
    $icon.toggleClass('icon-caret-down', hidden);
    $icon.toggleClass('icon-caret-up', !hidden);
  }

  function getCollapsedClass($tile) {
    return `collapsed${$tile.data('toggle-tile') === 'mobile' ? '-mobile' : ''}`;
  }

  function toggleTile($tile, hidden, updateStorage) {
    $tile.toggleClass(getCollapsedClass($tile), hidden);
    turnIcon($tile, hidden);

    if (updateStorage && $tile.attr('id')) {
      Storage.set(`toggle_tile${$tile.attr('id')}`, {hidden});
    }
    if (!hidden) {
      AGN.Lib.CoreInitializer.run('load', $tile.find('.tile-body')); // Load lazy data if any
    }
  }

  function initDefaultState($tile) {
    const defaultState = $tile.data('toggle-tile-default-state');
    if (!defaultState) {
      return;
    }
    toggleTile($tile, defaultState === 'open', true);
  }

  const init = function($tile) {
    const target = $tile.attr('id');
    const conf = target ? Storage.get('toggle_tile' + target) : undefined;

    if (!conf) {
      initDefaultState($tile);
      return;
    }
    toggleTile($tile, conf.hidden, true);
  }

  const toggle = function($tile) {
    toggleTile($tile, !$tile.hasClass(getCollapsedClass($tile)), true);
  }

  const trigger = function($needle) {
    return $needle.closest('.tile').find('[data-toggle-tile]');
  }

  const show = function($tile) {
    toggleTile($tile, false, true);
  }

  const hide = function($tile) {
    toggleTile($tile, true, true);
  }

  AGN.Lib.Tile = {
    toggle: toggle,
    init: init,
    show: show,
    hide: hide,
    trigger: trigger
  }
})();
