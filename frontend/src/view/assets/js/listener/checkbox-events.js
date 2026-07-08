/**
 * The HTML specification does not support the `readonly` attribute for checkboxes.
 * If a checkbox is marked `disabled`, its value is excluded from form submissions.
 * This initializer simulates true `readonly` behavior. It uses the Event Capture
 * phase to intercept and kill interaction events (click, space key) before they
 * reach the checkbox. This prevents the user from toggling the state, while
 * ensuring the checkbox's value is still serialized and sent to the server.
 */
(() => {
  const trapReadonlyInteractions = function(e) {
    const $target = $(e.target);
    const $checkbox = $target.is('input[type="checkbox"]')
      ? $target
      : $target.closest('.form-check').find('input[type="checkbox"]').first();

    if (!($checkbox[0]?.hasAttribute('readonly'))) {
      return;
    }

    if (e.type === 'click' || e.code === 'Space' || e.key === ' ') {
      e.preventDefault();
      e.stopPropagation();
    }
  };
  document.addEventListener('click', trapReadonlyInteractions, { capture: true });
  document.addEventListener('keydown', trapReadonlyInteractions, { capture: true });
})();
