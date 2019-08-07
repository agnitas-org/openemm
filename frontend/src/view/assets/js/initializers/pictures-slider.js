;(function(){

  /**
   * Wrapper for pictures slider
   *
   * for more information please see the docs at
   * https://github.com/kenwheeler/slick/
   **/

  var Helpers = AGN.Lib.Helpers;

  AGN.Initializers.PicturesSlider = function($scope) {
      if (!$scope) {
          $scope = $(document);
      }

      _.each($scope.find('.js-pictures-slider'), function (el) {
          var $el = $(el);

          var updateDisabledStyle = function(slick, arrow) {
              _.each($scope.find(slick.getOption(arrow)), function (el) {
                  var $el = $(el);
                  if ($el.hasClass('slick-disabled')) {
                      $el.addClass('disabled');
                  } else {
                      $el.removeClass('disabled');
                  }
              })
          };

          $el.on('init', function(event, slick) {
              updateDisabledStyle(slick, 'prevArrow');
              updateDisabledStyle(slick, 'nextArrow');
          });

          $el.on('afterChange', function(event, slick, currentSlide) {
              updateDisabledStyle(slick,'prevArrow');
              updateDisabledStyle(slick, 'nextArrow');
          });

          var options = _.merge(
              {},
              Helpers.objFromString($el.data('pictures-slider-options'))
          );
          $el.slick(options);
      });
  }
})();
