AGN.Lib.Controller.new('mailing-params', function() {
  let $form = $('#mailingSettingsForm');
  let initParams;
  let $mailingParamsTable = $('#mailingParamsTable');

  this.addDomInitializer('mailing-params', function() {
    initParams = this.config.params;
    $form = $('#mailingSettingsForm');
    $form.dirty('setAsClean');
    $form.dirty('refreshEvents');
    $mailingParamsTable.on('input-table:row-removed', checkParamsDirtyState);
  });

  function checkParamsDirtyState() {
    if (isDirtyOnlyMailingParamsFields() && !isParamsChanged()) {
      $form.dirty('setAsClean');
    }
  }

  function isParamsChanged() {
    const cleanInitParams = initParams.map(function(param) {
      const cleanParam = _.omit(param, 'mailingID', 'mailingInfoID', 'changeDate');
      Object.keys(cleanParam).forEach(function(key) {
          if(cleanParam[key] === null) {
            cleanParam[key] = '';
          }
      })
      return cleanParam;
    });
    
    return JSON.stringify(cleanInitParams) !== JSON.stringify($mailingParamsTable.data('table').collect());
  }

  function isDirtyOnlyMailingParamsFields() {
    return $form.dirty('showDirtyFields')
      .filter((_, e) => !($(e).closest('#mailingParamsTable').length))
      .length === 0;
  }
});
