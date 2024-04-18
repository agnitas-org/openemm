(function(){

  AGN.Lib.CoreInitializer.new('tab-related', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-tab-related]'), function(el) {
      const $el = $(el);
      const $tab = $($el.data('tab-related'));

      $tab.on('tile:show', () => $el.removeClass('hidden'));
      $tab.on('tile:hide', () => $el.addClass('hidden'));
    })
  });

})();
