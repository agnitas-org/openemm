AGN.Lib.DomInitializer.new('autosubmit', function($e) {
  setTimeout(() => AGN.Lib.Form.get($e).submit(), 100);
});
