(function(){

  var Tile = AGN.Lib.Tile,
      Tab  = AGN.Lib.Tab;

  $(document).on('click', '[data-toggle-tab]', function(e) {
    var $this = $(this),
        $tileTrigger = Tile.trigger($this),
        toggleMethod = $this.data('toggle-tab-method') || 'show';

    Tile.show($tileTrigger);
    Tab[toggleMethod]($this);

    e.preventDefault();
    return false;
  });

})();
