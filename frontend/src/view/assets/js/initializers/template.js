AGN.Initializers.Template = function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  _.each($scope.find('script[type="text/x-mustache-template"]'), function(template) {
    var $template = $(template),
      id        = $template.attr('id');

    AGN.Opt.Templates[id] = $template.html();
  });
}
