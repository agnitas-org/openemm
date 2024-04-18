(function() {

  const Tooltip = {};
  const Template = AGN.Lib.Template;
  const Helpers = AGN.Lib.Helpers;
  const Popover = AGN.Lib.Popover;

  Tooltip.get = function ($e) {
    const tooltip = bootstrap.Tooltip.getInstance($e);
    if (tooltip) {
      return tooltip;
    }
    return null;
  }

  const createTemplate = (function() {
    const make = Template.prepare('tooltip-template');

    return function (tooltipStyle, arrowStyle, innerStyle) {
      return make({
        tooltipStyle: tooltipStyle,
        arrowStyle: arrowStyle,
        innerStyle: innerStyle
      });
    };
  })();

  function getTooltipOptions(title, style, trigger) {
    return {
      title,
      container: 'body',
      template: createTemplate(style),
      ...(trigger && { trigger }),
    };
  }

  Tooltip.options = function($e) {
    let options = {};

    if ($e.is('[data-tooltip]')) {
      let title = $e.data('tooltip');
      const source = $e.data('tooltip-src');
      
      if (source) {
        title = Template.text(source, {element: $e});
      }
      options = getTooltipOptions(title, $e.data('tooltip-style'));
    }

    return $.extend(
      options,
      Helpers.objFromString($e.data('tooltip-options'))
    );
  };

  Tooltip.create = function($e, options) {
    Tooltip.remove($e);

    const tooltip = new bootstrap.Tooltip($e, options);
    // $e.on({
    //   'dragstart.tooltip': function() {
    //     $e.bsTooltip('hide');
    //     $e.bsTooltip('disable');
    //   },
    //   'dragstop.tooltip': function() {
    //     $e.bsTooltip('enable');
    //   }
    // });
    $e.on('remove', function(){
      Tooltip.remove($e);
    })

    if (options.trigger !== 'manual') {
      // Create a new Intersection Observer
      const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            tooltip.hide();
          }
        });
      });
      observer.observe($e[0]);
    }
    return tooltip;
  };

  Tooltip.remove = function($e) {
    const tooltip = bootstrap.Tooltip.getInstance($e);
    if (tooltip) {
      tooltip.dispose();
    }
    // $e.off('dragstart.tooltip dragstop.tooltip');
  };

//   Tooltip.setEnabled = function($e, isEnabled) {
//     if (isEnabled || isEnabled === undefined) {
//       $e.bsTooltip('enable');
//     } else {
//       $e.bsTooltip('hide');
//       $e.bsTooltip('disable');
//     }
//   };

  Tooltip.setShown = function($e, isShown) {
    if (isShown || isShown === undefined) {
      $e.tooltip('show');
    } else {
      $e.tooltip('hide');
    }
  };

  Tooltip.createTip = function($e, text, style, trigger) {
    return Tooltip.create($e, getTooltipOptions(text, style, trigger));
  };

  AGN.Lib.Tooltip = Tooltip;
  
  class HelpTooltip {
    static defaultOptions = {
      trigger: 'hover',
      html: true,
      popperConfig: {
        placement: 'bottom-start'
      }
    }

    constructor($el) {
      this.$el = $el;
      this.elementData = this.$el.data('tooltip-help');
      this.$el.one('mouseenter', () => Popover.getOrCreate($el, this.getOptions($el)).show())
    }

    getOptions($el) {
      return $.extend(HelpTooltip.defaultOptions, this.getElementSpecificOptions($el));
    }

    #getXOffset() {
      const data = this.elementData;
      const elWidth = this.$el.outerWidth();
      return data.placement === 'bottom-end' ? -elWidth : elWidth;
    }

    getElementSpecificOptions() {
      const data = this.elementData;
      const offset = [this.#getXOffset(), 0];

      if (!data.content) {  // can be content text or object with popper.js properties
        return { "content": data, offset };
      }
      if (data.placement) {
        data.popperConfig = { placement: data.placement }
      }
      data.offset = offset;
      return data;
    }
  }
  AGN.Lib.HelpTooltip = HelpTooltip;
})();
