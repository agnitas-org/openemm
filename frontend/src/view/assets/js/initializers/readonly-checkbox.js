/**
 * The HTML specification does not support the `readonly` attribute for checkboxes.
 * If a checkbox is marked `disabled`, its value is excluded from form submissions.
 * This initializer simulates true `readonly` behavior. It uses the Event Capture
 * phase to intercept and kill interaction events (click, space key) before they
 * reach the checkbox. This prevents the user from toggling the state, while
 * ensuring the checkbox's value is still serialized and sent to the server.
 */
AGN.Lib.CoreInitializer.new('readonly-checkbox', function($scope = $(document)) {
  const container = $scope.get(0) || document;

  const trapReadonlyInteractions = function(e) {
    const $checkbox = $(e.target).is('input[type="checkbox"]')
      ? $(e.target)
      : $(e.target).closest('.form-check').find('input[type="checkbox"]').first();

    if (!($checkbox[0]?.hasAttribute('readonly'))) {
      return;
    }

    if (e.type === 'click' || e.code === 'Space' || e.key === ' ') {
      e.preventDefault();
      e.stopPropagation();
      return false;
    }
  };
  // Bind to the CAPTURE phase (the 'true' argument at the end)
  // We use vanilla JS here because jQuery's .on() defaults to the bubbling phase.
  container.addEventListener('click', trapReadonlyInteractions, true);
  container.addEventListener('keydown', trapReadonlyInteractions, true);
});
