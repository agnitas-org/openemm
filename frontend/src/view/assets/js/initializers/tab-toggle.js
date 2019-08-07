(function(){

  var Tab = AGN.Lib.Tab;

  AGN.Initializers.TabToggle = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-toggle-tab]'), function(tab) {
      Tab.init($(tab));
    })

  }

})();
