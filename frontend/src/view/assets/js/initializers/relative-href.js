AGN.Lib.CoreInitializer.new('relative-href', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  $scope.find('a[data-relative]').each(function() {
    var $a = $(this);
    $a.attr('href', AGN.url($a.attr('href')));
    $a.removeAttr('data-relative');
  });
});
