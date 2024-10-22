;(() => {
  $(document).on('click', '.date-picker-container', function() {
    const $input = $(this).find('.js-datepicker');

    if (!$input.prop('disabled') && !$input.datepicker('widget').is(":visible")) {
      $input.datepicker('show');
    }
  });
})();
