(function(){

  var Tile = AGN.Lib.Tile;

  $(document).on('click', '[data-toggle-tile]', function(e) {
    Tile.toggle($(this));
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
