AGN.Lib.CoreInitializer.new('preview-table', function ($scope = $(document)) {
  $scope.find('[data-preview-table]').each(function () {
    AGN.Lib.PreviewTable.init($(this));
  });
});
