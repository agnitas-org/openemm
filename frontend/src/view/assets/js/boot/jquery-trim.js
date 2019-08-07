(function($) {

  // possible fix for EMMGUI-431
  if (String.prototype.trim) {
    $.trim = function( text ) {
      return (text == null) ? "" : (text + "").trim();
    }
  }

})(jQuery);
