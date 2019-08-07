;(function() {
    $(document).on('click', '.js-open-datepicker', function(e) {
      var $this   = $(this),
          $input  = $this.parents('.input-group').find('.js-datepicker'),
          $picker = $input.pickadate('picker');
      $picker.open();
    });

    $(document).on('click', '.js-datepicker', function(e) {
      var $input   = $(this),
          $picker = $input.pickadate('picker');
      $picker.open();
      $input.focus();
    });
})();
