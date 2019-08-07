AGN.Lib.Controller.new('profile-field-view', function() {
  var isHistorizationFeatureEnabled = false;
  var isHistorizationEnabled = false;
  var dependentWorkflowName = null;

  this.addDomInitializer('profile-field-view', function($elem) {
    var data = $elem.json();

    isHistorizationFeatureEnabled = data.isHistorizationFeatureEnabled;
    isHistorizationEnabled = data.isHistorizationEnabled;
    dependentWorkflowName = data.dependentWorkflowName;

    $.i18n.load(data.translations);
  });

  this.addAction({change: 'toggleHistorization'}, function() {
    if (isHistorizationFeatureEnabled && isHistorizationEnabled && dependentWorkflowName) {
      if (!this.el.is(':checked')) {
        AGN.Lib.Messages(t('defaults.warning'), t('warning.profilefield.inuse', dependentWorkflowName), 'warning');
      }
    }
  });
});
