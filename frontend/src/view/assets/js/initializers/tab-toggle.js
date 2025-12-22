AGN.Lib.CoreInitializer.new('tab-toggle', function ($scope = $(document)) {
  _.each($scope.find('[data-toggle-tab]'), tab => AGN.Lib.Tab.init($(tab)))
});