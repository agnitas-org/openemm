;(function() {

    // fixes select not opening on label clicks
    $(document).on('click', 'label', function(e) {
      var $this = $(this),
          $target,
          selectApi;

      $target = $('#' + $this.attr('for'));
      selectApi = $target.data('select2');

      if ($target.length == 1 && selectApi) {
        setTimeout(function() {
            if (!selectApi.opened()) {
                selectApi.open();
            }
        }, 0);
      }
    });

})();
