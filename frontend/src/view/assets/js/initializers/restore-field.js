AGN.Lib.CoreInitializer.new('restore', function($scope = $(document)) {
  const Storage = AGN.Lib.Storage;

  _.each($scope.find('[data-restore-fields]'), el  => {
    Storage.restoreChosenFields($(el))
    addChangeListener($(el));
  });

  function addChangeListener($el) {
    return $el.on('change', function() {
      return Storage.saveChosenFields($(this));
    });
  }
});
