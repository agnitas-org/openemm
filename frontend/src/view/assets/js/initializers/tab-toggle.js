(function(){

  var Tab = AGN.Lib.Tab;

  AGN.Lib.CoreInitializer.new('tab-toggle', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-toggle-tab]'), function(tab) {
      Tab.init($(tab));
    })
  });

})();
