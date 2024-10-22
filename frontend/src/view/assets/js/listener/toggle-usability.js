;(() => {
    $(document).on('change', '[data-toggle-usability]', function() {
    var $this    = $(this),
        targets  = $this.data('toggle-usability'),
        $targets = $(document).find(targets);

    _.each($targets, function(target) {
      var $target = $(target);

      if ($target.prop('disabled')) {
        $target.prop('disabled', false);
      } else {
        $target.prop('disabled', true);
      }
    })

  })
})();
