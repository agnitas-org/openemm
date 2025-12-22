AGN.Lib.CoreInitializer.new('relative-href', function($scope = $(document)) {
  $scope.find('a[data-relative]').each(function() {
    const $a = $(this);
    $a.attr('href', AGN.url($a.attr('href')));
    $a.removeAttr('data-relative');
  });
});
