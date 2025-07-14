AGN.Lib.CoreInitializer.new('input-popover', ['popover'], ($scope = $(document)) => {
  $scope.all('input[type="text"]').each(function () {
    const $el = $(this);
    let popover = AGN.Lib.Popover.get($el);

    if (!popover) {
      popover = AGN.Lib.Popover.create($el, {content: () => $el.val()});
      popover.inputTruncatedTextPopover = true;

      if (!$el.is(':truncated')) {
        popover.disable();
      }
    }
  });
});
