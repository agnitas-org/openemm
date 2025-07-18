(() => {

  const $window = $(window);
  const DISPLAY_TYPE = {DESKTOP: 1, MOBILE: 2};
  let viewportChanged;
  let currentDisplayType;

  const updateViewport = function() {
    if (viewportChanged) {
      clearTimeout(viewportChanged);
    }

    viewportChanged = setTimeout(function() {
      AGN.Lib.CoreInitializer.run(['scrollable', 'truncated-text-popover', 'table-cols-resizer', 'table']);

      _.each(AGN.Lib.Editor.all(), editor => editor.resize());

      $window.trigger('viewportChanged');
    }, 500);

    if (isDisplayTypeChanged()) {
      $window.trigger('displayTypeChanged', [currentDisplayType === DISPLAY_TYPE.MOBILE]);
    }
  };

  function isDisplayTypeChanged() {
    const displayTypeBefore = currentDisplayType;
    currentDisplayType = AGN.Lib.Helpers.isMobileView() ? DISPLAY_TYPE.MOBILE : DISPLAY_TYPE.DESKTOP;

    return displayTypeBefore !== currentDisplayType;
  }

  $window.on('resize', updateViewport);
  $window.on('agn:resize', updateViewport);

})();
