(() => {

  const Form = AGN.Lib.Form;

  AGN.Lib.CoreInitializer.new('form', ['select', 'switchable-input'], function($scope = $(document)) {
    $scope.find('form input[type=text], form input[type=password]').on("keyup keypress", function(e) {
      const code = e.keyCode || e.which;
      if (code === 13) {
        e.preventDefault();
        return false;
      }
    });

    const $formInFocus = $scope.all('form[data-form-focus]').first();
    if ($formInFocus.exists()) {
      setTimeout(() => {
        const $e = $formInFocus.find(`[name="${$formInFocus.data('form-focus')}"]`);
        if ($e.exists() && !$e.is(':disabled') && !$e.is(':hidden')) {
          $e.trigger('focus');
          Form.scrollToField($e);
        }
      }, 10);
    }

    _.each($scope.all('form'), form => Form.get($(form)));
  });
  
})();
