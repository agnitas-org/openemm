;(function(){

  AGN.Lib.CoreInitializer.new('table', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    $scope.all('.js-data-table').each(function() {
      var $el = $(this);
      var $body = $el.find('.js-data-table-body');
      var id = $el.data('table');

      if ($body.exists() && id) {
        var $config = $('script#' + CSS.escape(id)),
            config, options;

        if ($config.exists()) {
          config = $config.json();

          if ($body.data('web-storage')) {
            options = _.merge(AGN.Lib.WebStorage.get($body.data('web-storage')) || {}, config.options || {});
          } else {
            options = config.options || {};
          }

          new AGN.Lib.Table($body, config.columns, config.data, options);
        }
      }
    });

    $scope.all('.table-controls').each(function () {
      const $el = $(this);
      const $pagination = $el.find('.pagination');
      if ($pagination.exists()) {
        const pagesCount = $pagination.find("[data-page]").length;
        $el.addClass(`pages-${pagesCount}`)
      }
    });
  });

})();
