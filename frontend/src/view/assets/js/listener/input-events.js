(() => {

  const INPUT_SELECTOR = 'input[type="text"]';

  $(document).on('input change', INPUT_SELECTOR, function() {
    updatePopover($(this));
  });

  $(window).on('viewportChanged', function() {
    $(INPUT_SELECTOR).each(function () {
      updatePopover($(this));
    });
  });

  function updatePopover($input) {
    const Popover = AGN.Lib.Popover;
    const popover = Popover.get($input);

    if (popover?.inputTruncatedTextPopover) {
      popover.setContent({'.popover-body': $input.val()});
      if ($input.is(':truncated')) {
        popover.enable();

        if (!Popover.isShown(popover) && $input.is(':hover')) {
          popover.show();
        }
      } else {
        popover.disable();
      }
    }
  }
})();