;(function(){

  $(document).on('click', '.dropdown-menu *', function(e) {
    $target = $(e.target);

    if ($target.hasClass("js-dropdown-open") ) {
      e.preventDropdownClose = true
    }
    if ($target.parent(".js-dropdown-open").length == 1 ) {
      e.preventDropdownClose = true
    }

    if ($target.is("button")) return;
    if ($target.is("a")) return;
    if ($target.parent("a").length == 1) return;
    if ($target.parent("button").length == 1 ) return;
    if ($target.hasClass("js-dropdown-close") ) return;
    if ($target.parent(".js-dropdown-close").length == 1 ) return;

    e.preventDropdownClose = true;
  });

})();
