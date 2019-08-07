(function(){

  var DataProvider = {};

  DataProvider.get = function($element) {
    var type = $element.data('provider'),
        src  = $element.data('provider-src'),
        provider;

    switch(type) {
      case 'opt':
        provider = DataProvider.opt(src);
        break;
      case 'remote':
        provider = DataProvider.remote(src);
        break;
      default:
        break;
    }

    return provider;
  }

  DataProvider.opt = function(src) {
    var deferred = $.Deferred();

    if (AGN.Opt[src]) {
      setTimeout(function() {
        deferred.resolve(AGN.Opt[src]);
      }, 100);
    } else {
      setTimeout(function() {
        deferred.reject();
      }, 100);
    }

    return deferred;
  }

  DataProvider.remote = function(src) {
    return $.get(src);
  }


  AGN.Lib.DataProvider = DataProvider;

})();
