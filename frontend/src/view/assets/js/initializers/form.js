(function(){

  var Form = AGN.Lib.Form;

  AGN.Lib.CoreInitializer.new('form', ['select'], function($scope) {
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
      setTimeout(function () {
        var $e = $formInFocus.find('[name="' + $formInFocus.data('form-focus') + '"]');
        if ($e.exists() && !$e.is(':disabled') && !$e.is(':hidden')) {
          focusToElement($e);
        }
      }, 10);
    }

    _.each($scope.find('form'), function(form) {
      Form.get($(form));
    })
  });
  
  function focusToElement($e) {
      var scrollPos;
      var topPadding = 25;
      var $form = Form.get($e).$form;
      var $view = $e.closest('[data-sizing=scroll], .modal').first();
      
      if (!$view.length) {
        $view = $(document);
        scrollPos = $e.offset().top - $form.offset().top - topPadding;
      } else {
        scrollPos = $e.offset().top - $view.offset().top + $view.scrollTop() - topPadding;
      }
      
      $e.trigger('focus');
      $view.scrollTop(scrollPos);
  }
})();
