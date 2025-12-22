AGN.Lib.CoreInitializer.new('input-table', function($scope = $(document)) {
  _.each($scope.find('[data-input-table]'), el => new AGN.Lib.InputTable($(el)));
});
