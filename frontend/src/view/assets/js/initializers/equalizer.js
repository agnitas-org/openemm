;(function(){
	
  var Equalizer = AGN.Lib.Equalizer;

  AGN.Initializers.Equalizer = function($scope) {

    window.setTimeout(function() {
      _.each($('[data-equalizer]'), function(el) {
        Equalizer($(el));
      });
    }, 1);
  }

})();
