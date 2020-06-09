(function(){

  var truncate = AGN.Lib.Truncate.run;
  var breadcrumbs = AGN.Lib.Breadcrumbs.truncate;

  AGN.Lib.CoreInitializer.new('truncate', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    setTimeout(function() {

      _.each($scope.find('.l-header'), function(trunc) {
        var $trunc = $(trunc);
        truncate($trunc, '.header-nav');
        breadcrumbs($trunc.find('.header-nav'));
      });

    }, 50);
  });

})();
