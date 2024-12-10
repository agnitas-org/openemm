AGN.Lib.CoreInitializer.new('editable-view', function ($scope = $(document)) {
  const EditableView = AGN.Lib.EditableView;
  const EditableTile = AGN.Lib.EditableTile;
  
  $scope.all(EditableView.SELECTOR).each(function () {
    const $el = $(this);
    new EditableView($el, $el.data('editable-view'));
  });

  if ($scope.is(EditableTile.SELECTOR)) {
    EditableView.get($scope)?.add($scope);
    $(window).trigger('agn:resize');
  }
});
