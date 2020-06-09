;(function(){
  var Helpers = AGN.Lib.Helpers;

  AGN.Lib.CoreInitializer.new('display-dimensions', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-display-dimensions]'), function(el) {
      var $el     = $(el),
          scope   = Helpers.objFromString($el.data('display-dimensions'))['scope'],
          $row    = $el.parents(scope),
          $target = $row.find('[data-dimensions]');

      $el.ensureLoad(function() {
        var width   = el.naturalWidth,
            height  = el.naturalHeight;

        $target.html(width + ' x ' + height + ' px');
      })

    });
  });

})();
