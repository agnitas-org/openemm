(function($) {

  if ($.fn.all) {
    console.error('$.fn.all is already defined');
  } else {
    $.fn.extend({
      all: function(selectors) {
        return this.filter(selectors)
          .add(this.find(selectors));
      }
    });
  }

  if ($.fn.exists) {
    console.error('$.fn.exists is already defined');
  } else {
    $.fn.extend({
      exists: function() {
        return this.length > 0;
      }
    });
  }

  if ($.fn.json) {
    console.error('$.fn.json is already defined');
  } else {
    $.fn.extend({
      json: function() {
        return JSON.parse(this.html());
      }
    });
  }

})(jQuery);
