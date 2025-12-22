AGN.Lib.CoreInitializer.new('table-column-manager', function($scope = $(document)) {
  const TableColumnManager = AGN.Lib.TableColumnManager;

  $scope.all(TableColumnManager.SELECTOR).each(function () {
    new TableColumnManager($(this));
  });
});
