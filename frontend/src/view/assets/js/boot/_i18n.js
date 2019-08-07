(function(){

  var flatten = function(hash) {
    var result = {};

    _.each(hash, function(value, key) {
      if ( _.isPlainObject(value) ) {
        _.each(flatten(value), function(hValue, hKey) {
          result[key + "." + hKey ] = hValue;
        })
      } else {
        result[key] = value;
      }
    })
    return result;
  }


  $.i18n.load(flatten(window.I18n));
  window.t = function() {
    return $.i18n._.apply($.i18n, Array.prototype.slice.call(arguments));
  }
})();
