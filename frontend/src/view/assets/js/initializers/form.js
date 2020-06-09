(function(){

  var Form = AGN.Lib.Form;

  AGN.Lib.CoreInitializer.new('form', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    $scope.find('form input[type=text], form input[type=password]').on("keyup keypress", function(e) {
      var code = e.keyCode || e.which;
      if (code === 13) {
        e.preventDefault();
        return false;
      }
    });

    var $formInFocus = $scope.all('form[data-form-focus]').first();
    if ($formInFocus.exists()) {
      var $e = $formInFocus.find('[name="' + $formInFocus.data('form-focus') + '"]');

      if ($e.exists()) {
        setTimeout(function() {
          if (!$e.is(':disabled') && !$e.is(':hidden')) {
            $e.trigger('focus');
          }
        }, 10);
      }
    }

    _.each($scope.find('form'), function(form) {
      Form.get($(form));
    })
  });

})();
