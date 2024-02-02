;(function() {
  $(document).on('click', '.js-open-datepicker', function() {
    const $datePickerInput = $(this).parent().find('.js-datepicker');

    if (!$datePickerInput.datepicker('widget').is(":visible")) {
      $datePickerInput.datepicker('show');
    }
  });
})();
