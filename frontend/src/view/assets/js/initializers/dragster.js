AGN.Lib.CoreInitializer.new('dragster', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  $scope.find('.dropzone').each(function() {
    new Dragster(this);
  });
});
