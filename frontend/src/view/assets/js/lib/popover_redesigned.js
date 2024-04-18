(function ($) {

  const Popover = {};

  const popovers = [];

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

  Popover.getOrCreate = function ($el, options) {
    const defaultOpts = {
      trigger: 'hover',
      container: 'body',
      animation: false,
      content: $el.text(),
    }
    return Popover.get($el) || Popover.new($el, $.extend(defaultOpts, options));
  }
  
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

    popovers.push(popover);
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

    const $needle = $e.closest('div.popover');
    if ($needle.length) {
      return bootstrap.Popover.getInstance($needle);
    }
  };

  Popover.remove = function ($e) {
    if ($e.length !== 1) {
      return false;
    }

    const popoverIntance = bootstrap.Popover.getOrCreateInstance($e);
    removePopover(popoverIntance);
    return true;
  };

  Popover.validate = function () {
    const allPopovers = Array.from(popovers);
    _.each(allPopovers, function (popover) {
      if (popover._element && !$.contains(document.body, popover._element)) {
        removePopover(popover);
      }
    });
  };

  function removePopover(popoverIntance) {
    const popoverIndex = popovers.indexOf(popoverIntance);
    if (popoverIndex !== -1) {
      popovers.splice(popoverIndex, 1);
    }

    popoverIntance.dispose();
  }

  Popover.hide = function($el) {
    Popover.get($el)?.hide();
  }
  
  AGN.Lib.Popover = Popover;
})(jQuery);
