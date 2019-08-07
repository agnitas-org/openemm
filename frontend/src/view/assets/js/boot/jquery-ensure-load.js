(function($) {

  $.fn.extend({
    ensureLoad: function(handler) {
      return this.each(function() {
        if(this.complete) {
          handler.call(this);
        } else {
          $(this).load(handler);
        }
      });
    }
  });


})(jQuery);
