;(function(){

  AGN.Initializers.PollingForm = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-form="polling"]'), function(el) {
      var $el = $(el);

      AGN.Lib.Form.get($el).submit();
    });

  }

})();
