/*doc
---
title: Tile Toggles
name: tilesjs
category: Javascripts - Tile Toggles
---

Tiles can be opened/closed  by the `data-toggle-tile="jquerySelector"` directive. The directive should be attached to an anchor and will toggle the corresponding tile content.

The tiles save their state into localstorage under the jquerySelector provided to the directive. Thus if the same selector is used under different views those tiles share the same state.

The default state can be controlled by adding/removing the class `hidden` to the tile content.

For showing the direction you can use an icon in the anchor: `<i class="tile-toggle icon icon-angle-up"` or `<i class="tile-toggle icon icon-angle-down"`. The icon will be automatically adapted to the current state, however for the default state it's relevant to use the correct icon.

```htmlexample
<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tileclosed">
            <i class="tile-toggle icon icon-angle-down"></i>
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
            <i class="tile-toggle icon icon-angle-up"></i>
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
            <i class="tile-toggle icon icon-angle-down"></i>
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

  var Storage = AGN.Lib.Storage;

  var init,
      toggle,
      trigger,
      show,
      hide;


  init = function($trigger) {
    var target = $trigger.data('toggle-tile'),
        conf = Storage.get('toggle_tile' + target);

    if (!conf) {
      return;
    }

    if (conf.hidden) {
      hide($trigger);
    } else {
      show($trigger);
    }

  }

  toggle = function($trigger) {
    var $target = $($trigger.data('toggle-tile'));

    if ($target.hasClass('hidden')) {
      show($trigger);
    } else {
      hide($trigger);
    }
  }

  show = function($trigger) {
    var $icon = $trigger.find('.icon'),
        target = $trigger.data('toggle-tile'),
        $target = $(target);

    $target.removeClass('hidden');
    $icon.addClass('icon-angle-up');
    $icon.removeClass('icon-angle-down');

    Storage.set('toggle_tile' + target, {hidden: false});

    // Load lazy data if any
    AGN.Initializers.Load($target);
  }

  trigger = function($needle) {
    return $needle.
              closest('.tile').
              find('[data-toggle-tile]');
  }

  hide = function($trigger) {
    var $icon = $trigger.find('.icon'),
        target = $trigger.data('toggle-tile');

    $(target).addClass('hidden');
    $icon.addClass('icon-angle-down');
    $icon.removeClass('icon-angle-up');

    Storage.set('toggle_tile' + target, {hidden: true})
  }

  AGN.Lib.Tile = {
    toggle: toggle,
    init: init,
    show: show,
    hide: hide,
    trigger: trigger
  }

})();
