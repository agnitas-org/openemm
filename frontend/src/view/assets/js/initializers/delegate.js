AGN.Lib.CoreInitializer.new('delegate', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  $scope.all('[data-delegate]').each(function() {
    var $source = $(this),
      delegates = $source.data('delegate'),
      events = $source.data('delegate-events') || 'click';

    $source.on(events, function(event) {
      var $delegates = $(delegates);
      if ($delegates.length > 0) {
        $delegates.trigger($.Event(event));
      }
    });
  });
});
