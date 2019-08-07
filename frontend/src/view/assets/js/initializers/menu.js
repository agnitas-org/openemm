(function(){

  AGN.Initializers.Menu = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    AGN.Lib.Menu.init();
  }

})();
