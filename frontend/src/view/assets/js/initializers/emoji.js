(function() {

  AGN.Initializers.Emoji = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    $scope.all('[data-emoji]').each(function() {
      $(this).emojioneArea({
        // Explicitly set to null to fix caret positioning in Firefox
        // when a textarea is empty.
        placeholder: null
      });
    });
  };

})();
