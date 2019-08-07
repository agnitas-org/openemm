(function(){

  AGN.Initializers.Sync = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-sync]'), function(el) {
      var $this   = $(el),
          $source = $($this.data('sync')),
          editor  = $this.data('_editor');

      if (editor) {
        editor.val($source.val());
      } else {
        $this.val($source.val());
      }

    })
  }

})();
