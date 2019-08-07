(function() {
  var Popover = {};

  var placementFunction = function(balloon, element) {
    var $balloon = $(balloon);
    var $e = $(element);

    var windowTop = $(window).scrollTop();
    var windowHeight = $(window).outerHeight();
    var height = $e.outerHeight();
    var top = $e.offset().top - windowTop;
    var bottom = top + height;

    // Check if we have enough space at top to place an entire balloon
    if ($balloon.outerHeight() <= top) {
      return 'top';
    }

    // Otherwise use a placement that provides more space
    return (windowHeight - bottom > top) ? 'bottom' : 'top';
  };

  Popover.new = function($e, options) {
    if ($e.length !== 1) {
      return false;
    }

    $e.popover($.extend({
      container: 'body',
      placement: placementFunction
    }, options));

    return $e.data('bs.popover');
  };

  Popover.get = function($e) {
    if ($e.length !== 1) {
      return null;
    }

    return $e.data('bs.popover');
  };

  Popover.remove = function($e) {
    if ($e.length !== 1) {
      return false;
    }

    $e.popover('destroy');

    return true;
  };

  Popover.validate = function() {
    $('.popover').each(function() {
      var popover = $(this).data('bs.popover');
      if (popover) {
        popover.validate();
      }
    });
  };

  AGN.Lib.Popover = Popover;
})();
