AGN.Lib.CoreInitializer.new('truncated-text-popover', ['table'],  function ($scope = $(document)) {

  const Popover = AGN.Lib.Popover;
  const Helpers = AGN.Lib.Helpers
  
  document.fonts.ready.then(() => {
    $scope
      .find('.text-truncate, table:not(.input-table) td')
      .each((i, el) => showPopoverIfTruncated(el))
  });

  function showPopoverIfTruncated(el) {
    const $el = detectHoverEl$(el);
    const popover = Popover.get($el);

    if (containsTruncatedElement(el)) {
      if (!popover) {
        Popover.create($el, {
          delay: { 'show': 400, 'hide': 50 },
          content: $(Helpers.retrieveTextElement($el[0])).text()
        }).forTruncatedText = true;
      }
    } else if (popover?.forTruncatedText) { // if not an instance created by this initializer -> skip
      Popover.remove($el)
    }
  }

  function detectHoverEl$(el) {
    const $el = $(el);
    const $cell = $el.closest('td');

    return $cell.exists() ? $cell : $el;
  }

  function containsTruncatedElement(el) {
    let $el = $(el);
    if ($el.is('.text-truncate-alt')) {
      return $el.is(':truncatedAlt') // see jquery-dom.js
    }
    return $el.is(':truncated')
      || ($el.is('td') && Helpers.containsTruncatedElements(el)); // table mode
  }
});
