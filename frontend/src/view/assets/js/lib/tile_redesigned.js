(() => {

  const Storage = AGN.Lib.Storage;

  function rotateIcon($tile, hidden) {
    const $icon = $tile.find('> .tile-header .tile-title .icon');
    $icon.toggleClass('icon-caret-down', hidden);
    $icon.toggleClass('icon-caret-up', !hidden);
  }

  function getCollapsedClass($tile) {
    return `collapsed${$tile.data('toggle-tile') === 'mobile' ? '-mobile' : ''}`;
  }

  function toggleTile($tile, hidden, updateStorage) {
    $tile.toggleClass(getCollapsedClass($tile), hidden);
    rotateIcon($tile, hidden);

    if (updateStorage && $tile.attr('id')) {
      Storage.set(`toggle_tile#${$tile.attr('id')}`, {hidden});
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
    toggleTile($tile, defaultState !== 'open', false);
  }

  const init = function($tile) {
    const target = $tile.attr('id');
    const conf = target ? Storage.get('toggle_tile#' + target) : undefined;

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
