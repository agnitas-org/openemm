(function() {
  var Storage = AGN.Lib.Storage;
  var WebStorage = {};

  WebStorage.get = function(k) {
    var key = 'web-storage#' + $.trim(k);

    try {
      return Storage.get(key);
    } catch (e) {
      console.error(e);
    }

    return null;
  };

  WebStorage.set = function(k, v) {
    Storage.set('web-storage#' + $.trim(k), v);
  };

  WebStorage.extend = function(k, v) {
    var value = WebStorage.get(k);

    if (value != null) {
      value = $.extend(value, v);
    } else {
      value = v;
    }

    WebStorage.set(k, value);

    return value;
  };

  AGN.Lib.WebStorage = WebStorage;
})();
