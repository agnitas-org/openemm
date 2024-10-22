(() => {

  function isHeaderTitle(el) {
    const titleSelector = '.tile-header .tile-title';
    return $(el).is(titleSelector) || $(el).closest(titleSelector).length;
  }

  function isHeaderControls(el) {
    const controlsSelector = '.tile-header .tile-controls';
    return $(el).is(controlsSelector) || $(el).closest(controlsSelector).length;
  }

  // on mobile whole tile header except actions clickable
  // on desktop only tile title clickable
  $(document).on('click', '[data-toggle-tile] > .tile-header', function(e) {
    const $tile = $(this).closest('.tile');
    const isCurrentScreenMobile = AGN.Lib.Helpers.isMobileView();

    if ($tile.data('toggle-tile') === 'mobile' && !isCurrentScreenMobile) {
      return;
    }

    if ((isCurrentScreenMobile && !isHeaderControls(e.target)) || isHeaderTitle(e.target)) {
      AGN.Lib.Tile.toggle($tile);
      AGN.Lib.Scrollbar.get($tile)?.update();
      return;
    }
    e.preventDefault();
  });

})();
