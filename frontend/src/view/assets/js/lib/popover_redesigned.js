(($) => {

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

  const offsetFunction = (data, el) => {
    const elWidth = $(el).outerWidth();

    if (data.placement === 'bottom-end') {
      return [-elWidth, 0];
    }

    if (data.placement === 'bottom-start') {
      return [elWidth, 0];
    }

    return [0, 0];
  }

  class Popover {

    static instances = [];

    static getOrCreate($el, options) {
      return this.get($el) || this.create($el, options);
    }

    static create($el, options) {
      if ($el.length !== 1) {
        return false;
      }

      const defaultOpts = {
        trigger: 'hover',
        container: 'body',
        animation: false,
        content: $el.text(),
        placement: placementFunction,
        offset: offsetFunction
      }

      const popover = new bootstrap.Popover($el[0], $.extend(defaultOpts, options));
      this.instances.push(popover);

      return popover;
    }

    static get($e) {
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
    }

    static remove($e) {
      this.#removePopover(this.get($e));
      return true;
    }

    static hide($el) {
      this.get($el)?.hide();
    }

    static validate() {
      _.each(Array.from(this.instances), instance => {
        if (instance._element && !$.contains(document.body, instance._element)) {
          this.#removePopover(instance);
        }
      });
    }

    static #removePopover(instance) {
      const index = this.instances.indexOf(instance);
      if (index !== -1) {
        this.instances.splice(index, 1);
      }

      instance?.dispose();
    }
  }

  AGN.Lib.Popover = Popover;

})(jQuery);
