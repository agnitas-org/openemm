(function(){

  var Tile = AGN.Lib.Tile;

  AGN.Initializers.TileToggle = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-toggle-tile]'), function(tile) {
      Tile.init($(tile));
    })

  }

})();
