(() => {
  const FULL_SCREEN_CLASS = 'tile--full-screen';

  // on mobile whole tile header except actions clickable
  // on desktop only tile title clickable
  function isPossibleToToggleCollapse($tile, isCurrentScreenMobile, e) {
    return !isMobileOnlyToggle($tile, isCurrentScreenMobile)
      && !$tile.hasClass(FULL_SCREEN_CLASS)
      && ((isCurrentScreenMobile && !isHeaderControls(e.target)) || isHeaderTitle(e.target));
  }

  $(document).on('click', '[data-toggle-tile] > .tile-header', function(e) {
    const $tile = $(this).closest('.tile');
    const isCurrentScreenMobile = AGN.Lib.Helpers.isMobileView();

    if (isPossibleToToggleCollapse($tile, isCurrentScreenMobile, e)) {
      AGN.Lib.Tile.get($tile).toggleCollapse();
    }
  });

  function isHeaderTitle(el) {
    return $(el).closest('.tile-header .tile-title').length;
  }

  function isHeaderControls(el) {
    return $(el).closest('.tile-header .tile-controls').length;
  }

  function isMobileOnlyToggle($tile, isCurrentScreenMobile) {
    return !isCurrentScreenMobile && $tile.find(`
    .tile-title > i.icon-caret-up.mobile-visible,
    .tile-title > i.icon-caret-down.mobile-visible`).exists();
  }

  $(document).on('click', '.tile [data-full-screen]', function(e) {
    const $btn = $(this);
    const $tile = $btn.closest('.tile');
    $tile.toggleClass(FULL_SCREEN_CLASS);
    _.each(AGN.Lib.Editor.all($tile), editor => editor.resize());

    const isFullScreen =  $tile.hasClass(FULL_SCREEN_CLASS);
    $btn.find('i')
      .toggleClass('icon-compress-arrows-alt', isFullScreen)
      .toggleClass('icon-expand-arrows-alt', !isFullScreen);
    e.preventDefault();
  });
})();
