(function($) {

  if ($.fn.style) {
    console.error('$.fn.style is already defined');
  } else {
    $.fn.extend({
      style: function(property, value, priority) {
        if (value === undefined) {
          return this.css(property);
        }

        if ($.isFunction(value)) {
          this.each(function() {
            if (this.style) {
              var newValue = value.call(this, this.style.getPropertyValue(property));
              this.style.setProperty(property, newValue, priority);
            }
          });
        } else {
          this.each(function() {
            if (this.style) {
              this.style.setProperty(property, value, priority);
            }
          });
        }
      }
    });
  }

})(jQuery);
