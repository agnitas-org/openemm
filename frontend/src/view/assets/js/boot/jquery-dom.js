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

  // May not work with inline elements
  $.expr[':'].truncated = function(el) {
    return el.scrollWidth - el.clientWidth > 1 || el.scrollHeight - el.clientHeight > 2;
  }

  // An alternative to :truncated that may be more accurate but requires more calculations
  $.expr[':'].truncatedAlt = function(el) {
    const temp = el.cloneNode(true);

    temp.style.width = "auto";
    temp.style.height = "auto";
    temp.style.position = "fixed";
    temp.style.overflow = "visible";
    temp.style.whiteSpace = "nowrap";
    temp.style.visibility = "hidden";

    el.parentElement.appendChild(temp);

    try {
      const fullWidth = temp.getBoundingClientRect().width;
      const displayWidth = el.getBoundingClientRect().width;

      return fullWidth > displayWidth;
    } finally {
      temp.remove();
    }
  }
})(jQuery);
