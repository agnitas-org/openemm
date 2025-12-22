AGN.Lib.CoreInitializer.new('tile-toggle', function ($scope = $(document)) {

  _.each($scope.find('[data-toggle-tile]'), trigger => new AGN.Lib.Tile($(trigger)))
});
