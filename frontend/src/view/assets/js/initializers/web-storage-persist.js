AGN.Lib.DomInitializer.new('web-storage-persist', function() {
  if (this.config) {
    $.each(this.config, (k, v) => {
      AGN.Lib.WebStorage.extend(k, v);
    });
  }
});
