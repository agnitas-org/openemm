(function ($) {

  const Popover = {};

  const placementFunction = function (balloon, element) {
    const $balloon = $(balloon);
    const $e = $(element);

    const windowTop = $(window).scrollTop();
    const windowHeight = $(window).outerHeight();
    const height = $e.outerHeight();
    const top = $e.offset().top - windowTop;
    const bottom = top + height;

    // Check if we have enough space at top to place an entire balloon
    if ($balloon.outerHeight() <= top) {
      return 'top';
    }

    // Otherwise use a placement that provides more space
    return (windowHeight - bottom > top) ? 'bottom' : 'top';
  };

  Popover.new = function ($e, options) {
    if ($e.length !== 1) {
      return false;
    }

    const popover = new bootstrap.Popover($e[0], $.extend({
      container: 'body',
      placement: placementFunction
    }, options));

    if (options && options.enableAgnRunAll) {
      $e.on('shown.bs.popover', function () {
        const $tip = $(popover.tip);

        AGN.Lib.Controller.init($tip);
        AGN.runAll($tip);
      });
    }

    return popover;
  };

  Popover.get = function ($e) {
    if ($e.length !== 1) {
      return null;
    }

    const popover = bootstrap.Popover.getInstance($e);
    if (popover) {
      return popover;
    }

    return bootstrap.Popover.getInstance($e.closest('div.popover'));
  };

  Popover.remove = function ($e) {
    if ($e.length !== 1) {
      return false;
    }

    $e.popover('dispose');
    return true;
  };

  Popover.validate = function () {
    $('.popover').each(function () {
      const popover = bootstrap.Popover.getInstance($(this));
      if (popover && popover.$element) {
        if (!$.contains(document.body, popover.$element[0])) {
          popover.destroy();
        }
      }
    });
  };

  AGN.Lib.Popover = Popover;
})(jQuery);
