// /*doc
// ---
// title: Tiles & tabs
// name: tilesjs-01-tabs
// parent: tilesjs
// ---
//
// Clicking on a tab of a closed tile will automatically open the tile
//
// ```htmlexample
// <div class="tile">
//     <div class="tile-header">
//         <a href="#" class="headline" data-toggle-tile="#tiletab">
//             <i class="tile-toggle icon icon-angle-down"></i>
//             Tile
//         </a>
//         <ul class="tile-header-nav">
//             <li>
//                 <a href="#" data-toggle-tab="#tiletab1-id">Tab 1</a>
//             </li>
//             <li>
//                 <a href="#" data-toggle-tab="#tiletab2-id">Tab 2</a>
//             </li>
//         </ul>
//     </div>
//
//     <div class="tile-content tile-content-forms hidden" id="tiletab">
//       <div id="tiletab1-id">
//         <p>Tab 1 Content</p>
//       </div>
//
//       <div id="tiletab2-id">
//         <p>Tab 2 Content</p>
//       </div>
//     </div>
// </div>
// ```
// */

(function(){

  const Storage = AGN.Lib.Storage;

  var init,
      toggle,
      trigger,
      show,
      hide;


  init = function($trigger) {
    const target = $trigger.data('toggle-tile'),
          conf = Storage.get('toggle_tile' + target);

      if (!conf) {
        const defaultState = $trigger.data('toggle-tile-default-state');

        if (typeof defaultState !== 'undefined') {
          if (defaultState === 'open') {
            showTile($trigger, false);
          } else {
            hideTile($trigger, false);
          }
        }

       return;
    }

    if (conf.hidden) {
      hide($trigger);
    } else {
      show($trigger);
    }
  }

  toggle = function($trigger) {
    const $target = $($trigger.data('toggle-tile'));

    if ($target.hasClass('hidden')) {
      show($trigger);
    } else {
      hide($trigger);
    }
  }

  show = function($trigger) {
    showTile($trigger, true);
  }

  function showTile($trigger, updateStorage) {
    const $icon = $trigger.find('.icon'),
        target = $trigger.data('toggle-tile'),
        $target = $(target);

    $target.removeClass('hidden');
    $icon.addClass('icon-angle-up');
    $icon.removeClass('icon-angle-down');

    if (updateStorage) {
      Storage.set('toggle_tile' + target, {hidden: false});
    }

    // Load lazy data if any
    AGN.Lib.CoreInitializer.run('load', $target);
  }

  trigger = function($needle) {
    return $needle.
              closest('.tile').
              find('[data-toggle-tile]');
  }

  hide = function($trigger) {
    hideTile($trigger, true);
  }

  function hideTile($trigger, updateStorage) {
    const $icon = $trigger.find('.icon'),
        target = $trigger.data('toggle-tile');

    $(target).addClass('hidden');
    $icon.addClass('icon-angle-down');
    $icon.removeClass('icon-angle-up');

    if (updateStorage) {
      Storage.set('toggle_tile' + target, {hidden: true})
    }
  }

  AGN.Lib.Tile = {
    toggle: toggle,
    init: init,
    show: show,
    hide: hide,
    trigger: trigger
  }

})();
