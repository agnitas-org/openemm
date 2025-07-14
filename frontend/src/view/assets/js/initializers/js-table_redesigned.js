AGN.Lib.CoreInitializer.new('js-table', function ($scope = $(document)) {
  $scope.all('[data-js-table]').each(function () {
    const $el = $(this);
    const $tableWrapper = $el.all('.table-wrapper');
    const id = $el.data('js-table');

    if (!$tableWrapper.exists() || !id) {
      return;
    }

    const $config = $(`script#${CSS.escape(id)}`);
    if (!$config.exists()) {
      console.warn(`Config for JS table (${id}) not found!`);
      return;
    }

    const config = $config.json();

    let options;
    if ($tableWrapper.data('web-storage')) {
      options = _.merge(AGN.Lib.WebStorage.get(
        $tableWrapper.data('web-storage')) || {},
        config.options || {}
      );
    } else {
      options = config.options || {};
    }

    new AGN.Lib.Table($tableWrapper, config.columns, config.data, options).applyFilter();
  });
});