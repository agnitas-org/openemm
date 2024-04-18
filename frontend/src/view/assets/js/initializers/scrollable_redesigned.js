;(function(){

  const Scrollbar = AGN.Lib.Scrollbar;

  AGN.Lib.CoreInitializer.new('scrollable', function($scope = $(document)) {
    _.each($scope.find('.js-scrollable, .table-scrollable, .ag-body-viewport'), function(el) {
      const $el = $(el);
      const scrollbar = Scrollbar.get($el);

      if (AGN.Lib.Helpers.isMobileView()) {
        scrollbar?.destroy();
        return;
      }

      if (scrollbar) {
        scrollbar.update();
      } else {
        new Scrollbar($el);
      }
    });
  });

})();
