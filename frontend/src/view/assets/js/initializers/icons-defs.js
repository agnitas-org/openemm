AGN.Lib.CoreInitializer.new('icons-defs', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  _.each($scope.find('[data-icon]'), function(icon) {
    var $icon = $(icon);

    var url = AGN.url('assets/icons-defs.svg#' + $icon.data('icon'), true);

    $icon.html('<svg viewBox="0 0 45 45"><use xlink:href="' + url + '"/></svg>');
  });

  svg4everybody();
});
