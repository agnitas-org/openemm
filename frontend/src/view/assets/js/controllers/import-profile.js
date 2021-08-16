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

  this.addDomInitializer('allMailinglists-checkbox', function() {
    setSeparateMailinglistChecboxes($('#allMalinglistsCheckbox').is(':checked'));
  });

  //#all-mailinglists-wrapper is visible just for ${allowedModesForAllMailinglists} import modes
  //if #allMalinglistsCheckbox checkbox is activated and is not visible just deactivate it
  this.addAction({change: 'mode-select-change'}, function() {
    var visible = $('#all-mailinglists-wrapper').is(':visible');
    if (!visible && $('#allMalinglistsCheckbox').is(':checked')) {
      $('#allMalinglistsCheckbox').click();
    }
  });

  this.addAction({change: 'allMailinglists-checkbox'}, function() {
    setSeparateMailinglistChecboxes($(this.el).is(':checked'));
  });

  var setSeparateMailinglistChecboxes = function (disabled) {
    $('#mailinglists [type="checkbox"]').prop('disabled', disabled);
  };
});
