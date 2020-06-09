;(function(){

  /**
   * Wrapper for bootstrap-slider
   *
   * for possible data-slider-options please see the docs at
   * https://github.com/seiyria/bootstrap-slider
   **/

  var Helpers = AGN.Lib.Helpers;

  AGN.Lib.CoreInitializer.new('slider', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('.js-slider'), function(el) {
      var $el = $(el),
          baseOptions,
          options;

      baseOptions = {
        handle: 'round',
        min: 0,
        max: 101
      };

      options = _.merge({},
        baseOptions,
        Helpers.objFromString($el.data('slider-options'))
      );

      $el.slider(options);
    });

  });

})();
