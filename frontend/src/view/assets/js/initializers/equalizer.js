;(function(){
	
  var Equalizer = AGN.Lib.Equalizer;

  AGN.Lib.CoreInitializer.new('equalizer', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    window.setTimeout(function() {
      $scope.find('[data-equalizer]').each(function() {
        Equalizer($(this));
      });
    }, 1);
  });

})();
