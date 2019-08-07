AGN.Lib.Controller.new('import-profile', function () {
  this.addDomInitializer('new-recipients-action-select', function() {
    setMailingListSelectionEnabled(this.el.val() <= 0);
  });

  this.addAction({change: 'new-recipients-action-select'}, function() {
    setMailingListSelectionEnabled(this.el.val() <= 0);
  });

  var setMailingListSelectionEnabled = function(isEnabled) {
    $('#mailinglists').toggleClass('hidden', !isEnabled);
    $('#mailinglists-to-show').toggleClass('hidden', isEnabled);
  };
});
