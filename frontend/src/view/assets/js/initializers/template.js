AGN.Lib.CoreInitializer.new('template', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  $scope.find('script[type="text/x-mustache-template"]').each(function() {
    var $template = $(this);
    var id = $template.attr('id');

    if (id) {
      AGN.Opt.Templates[id] = $template.html();
    }
  });
});
