AGN.Lib.DomInitializer.new('logon-complete', function($form) {
  var form = AGN.Lib.Form.get($form);
  var data = {};

  if (this.config) {
    $.each(this.config, function(index, name) {
      // We don't need all the JS code to fail in case a some entry is invalid.
      try {
        // Read requested bundles from local storage to send them to server.
        var bundle = AGN.Lib.Storage.get('web-storage#' + name);
        if (bundle != null) {
          data[name] = bundle;
        }
      } catch (e) {
        console.error(e);
      }
    });
  }

  // Send retrieved bundles to server as JSON.
  form.setValue('webStorageJson', JSON.stringify(data));
  form.submit();
});
