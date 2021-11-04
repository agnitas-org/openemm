(function($) {
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

    var popover = $e.data('bs.popover');

    if (options && options.enableAgnRunAll) {
      $e.on('shown.bs.popover', function() {
        var $tip = popover.tip();

        AGN.Lib.Controller.init($tip);
        AGN.runAll($tip);
      });
    }

    return popover;
  };

  Popover.get = function($e) {
    if ($e.length !== 1) {
      return null;
    }

    var popover = $e.data('bs.popover');
    if (popover) {
      return popover;
    }

    return $e.closest('div.popover').data('bs.popover');
  };

  Popover.remove = function($e) {
    if ($e.length !== 1) {
      return false;
    }

    $e.popover('destroy');

    return true;
  };

  Popover.validate = function() {
    $('.popover').each(function () {
      var popover = $(this).data('bs.popover');
      if (popover && popover.$element) {
        if (!$.contains(document.body, popover.$element[0])) {
          popover.destroy();
        }
      }
    });
  };

  AGN.Lib.Popover = Popover;
})(jQuery);
