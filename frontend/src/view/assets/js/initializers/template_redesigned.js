AGN.Lib.CoreInitializer.new('template', function($scope = $(document)) {
  $scope.find('script[type="text/x-mustache-template"]').each(function() {
    const $template = $(this);
    const id = $template.attr('id');

    if (id) {
      AGN.Lib.Template.register(id, $template.html());
    }
  });
});
