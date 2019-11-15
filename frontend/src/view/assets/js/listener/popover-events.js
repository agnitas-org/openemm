(function() {

  $(document).on('click', '[data-dismiss="popover"]', function() {
    var popover = AGN.Lib.Popover.get($(this));
    if (popover) {
      popover.hide();
    }
  });

})();
