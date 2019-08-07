AGN.Lib.DomInitializer.new('web-storage-persist', function() {
  if (this.config) {
    $.each(this.config, function(k, v) {
      var key = 'web-storage#' + $.trim(k);
      var value = null;

      try {
        value = AGN.Lib.Storage.get(key);
      } catch (e) {
        console.error(e);
      }

      if (value != null) {
        AGN.Lib.Storage.set(key, $.extend(value, v));
      } else {
        AGN.Lib.Storage.set(key, v);
      }
    });
  }
});
