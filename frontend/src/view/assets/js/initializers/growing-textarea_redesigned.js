AGN.Lib.CoreInitializer.new('growing-textarea', function($scope = $(document)) {
  $scope.all('textarea.form-control:not([class*="js-editor"])').each(function() {
    const $textArea = $(this);

    if (!$textArea.closest('.text-area-grow-wrapper').exists()) {
      $textArea.wrap('<div class="text-area-grow-wrapper"></div>');
    }

    $textArea.trigger('input');
  });
});
