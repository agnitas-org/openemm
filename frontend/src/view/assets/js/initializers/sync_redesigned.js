AGN.Lib.CoreInitializer.new('sync', ['ace'], function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  _.each($scope.find('[data-sync]'), function(el) {
    var $this   = $(el),
        $source = $($this.data('sync')),
        editor  = $this.data('_editor');

    if (editor) {
      editor.val($source.val());
    } else if ($this?.prop('type')?.trim() === 'checkbox') {
      $this.prop('checked', $source.val() === 'true');
    } else {
      $this.val($source.val());
    }
  })
});
