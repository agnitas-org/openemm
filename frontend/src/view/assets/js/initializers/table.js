;(function(){

  var Table = AGN.Lib.Table;

  AGN.Initializers.Table = function($scope) {
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

          new Table($body, config.columns, config.data, options);
        }
      }
    });

  }

})();
