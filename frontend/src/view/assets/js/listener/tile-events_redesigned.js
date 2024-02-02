(function(){

  var Tile = AGN.Lib.Tile;

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
  $(document).on('click', '[data-toggle-tile] .tile-header', function(e) {
    const $tile = $(this).closest('.tile');
    const isCurrentScreenMobile = AGN.Lib.Helpers.isMobileView();

    if ($tile.data('toggle-tile') === 'mobile' && !isCurrentScreenMobile) {
      return;
    }
    if ((isCurrentScreenMobile && !isHeaderControls(e.target)) || isHeaderTitle(e.target)) {
      Tile.toggle($tile);
      return;
    }
    e.preventDefault();
  });

  $(document).on('click', '[data-toggle-tile-all]', function(e) {
    var action = $(this).data('toggle-tile-all');
    _.each($('[data-toggle-tile]'), function(tile) {
      var trigger = $(tile);
      if(action == 'expand') {
        Tile.show(trigger);
      } else {
        Tile.hide(trigger);
      }
      e.preventDefault();
    });
  });

})();
