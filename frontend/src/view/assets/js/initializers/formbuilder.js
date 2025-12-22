AGN.Lib.DomInitializer.new('formbuilder', function ($scope = $(document)) {

  let config = this.config;
  const $commonConfig = $(`script#${CSS.escape('config:formbuilderCommon')}[type="application/json"]`);
  if ($commonConfig.length === 1) {
    config = $.extend($commonConfig.json(), config);
  }

  _.each($scope.find('.js-form-builder'), textArea => {
    AGN.Lib.FormBuilder.FormBuilder.get(textArea, config).promise().then(function () {
      $(textArea).find('.form-actions button').each(function () {
        const $e = $(this);
        AGN.Lib.Tooltip.create($e, {title: $e.text()});
      });
    });
  });

  document.styleSheets[0].insertRule(`:root { --warn-dialog-title: "${t('defaults.warning')}"; }`, 0);
});
