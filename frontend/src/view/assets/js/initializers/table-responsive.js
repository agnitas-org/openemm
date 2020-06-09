AGN.Lib.CoreInitializer.new('table-responsive-wrap', function($scope) {
  if (!$scope) {
    $scope = $(document)
  }

  $scope.find('.table-wrapper:not(.table-overflow-visible) table').wrap('<div class="table-responsive" />');
  $scope.find('.table-wrapper.table-overflow-visible table').wrap('<div class="table-responsive" style="overflow: visible;" />');
});
