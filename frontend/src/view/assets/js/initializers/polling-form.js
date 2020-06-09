AGN.Lib.CoreInitializer.new('polling-form', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  $scope.find('[data-form="polling"]').each(function() {
    AGN.Lib.Form.get($(this)).submit();
  });
});
