(function(){

  var View = AGN.Lib.View;

  AGN.Initializers.View = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    View.init();
  }

})();
