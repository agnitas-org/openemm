AGN.Lib.CoreInitializer.new('schedule-builder', function($scope = $(document)) {
  _.each($scope.find('[data-schedule-builder]'), el => new AGN.Lib.Schedule.Builder($(el)));
});
