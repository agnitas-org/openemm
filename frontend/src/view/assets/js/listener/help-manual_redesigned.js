(() => {

  $(document).on('click', '[data-manual]', function(e) {
    const $el = $(this);
    AGN.Lib.Helpers.openHelpModal('manual', {key: $el.data('manual')})
  });

})();
