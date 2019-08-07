AGN.Lib.DomInitializer.new('autosubmit', function($e) {
  setTimeout(function() {
    AGN.Lib.Form.get($e).submit();
  }, 100);
});
