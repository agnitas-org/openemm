;(() => {

  // fixes select not opening on label clicks
  $(document).on('click', 'label', function (e) {
    if ($(e.target).is('[data-help]')) {
      return;
    }

    const $el = $(this);

    const $target = $(`#${$el.attr('for')}`);
    const selectApi = $target.data('select2');

    if ($target.length === 1 && selectApi) {
      setTimeout(() => {
        if (!selectApi.isOpen()) {
          selectApi.open();
        }
      }, 0);
    }
  });

})();
