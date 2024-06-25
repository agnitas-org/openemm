AGN.Lib.CoreInitializer.new('truncated-text-popover', ['table'],  function ($scope = $(document)) {
  const HOVER_DELAY = 400; // ms
  const Popover = AGN.Lib.Popover;
  const Helpers = AGN.Lib.Helpers
  
  document.fonts.ready.then(() => {
    $scope
      .find('.js-truncated-text-popover, td')
      .each((i, el) => showPopoverIfTruncated(el))
  });

  function showPopoverIfTruncated(el) {
    const $el = $(el);
    if (!containsTruncatedElement(el)) {
      Popover.remove($el)
      return;
    }
    const $cell = $el.closest('td');
    const tableMode = $cell.length > 0;
    const $hoverEl = tableMode ? $cell : $el;

    Popover.getOrCreate($hoverEl, {
      delay: { 'show': HOVER_DELAY, 'hide': 50 },
      content: $(Helpers.retrieveTextElement($hoverEl[0])).text()
    });
  }

  function containsTruncatedElement(el) {
    return $(el).is(':truncated')
      || ($(el).is('td') && Helpers.containsTruncatedElements(el)); // table mode
  }
});
