(function(){

  var Tile = AGN.Lib.Tile;

  AGN.Lib.CoreInitializer.new('tile-toggle', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-toggle-tile]'), function(trigger) {
      Tile.init($(trigger));
    })
  });

})();
