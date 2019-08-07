;(function(){
  /*
   * we use a minimal scrollXMarginOffset due to rounding errors in IE9
   */

  AGN.Initializers.Scrollable = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('.js-scrollable'), function(el) {
      var $el = $(el);

      $el.perfectScrollbar('destroy')
      $el.perfectScrollbar({wheelSpeed: 5, scrollXMarginOffset: 1})

    });

  }

})();
