(() => {

  const Template = AGN.Lib.Template;
  const Helpers = AGN.Lib.Helpers;

  const Tooltip = {};

  Tooltip.get = function ($e) {
    const tooltip = bootstrap.Tooltip.getInstance($e);
    if (tooltip) {
      return tooltip;
    }
    return null;
  }

  const createTemplate = function (tooltipStyle, arrowStyle, innerStyle) {
    return Template.text('tooltip-template', {
      tooltipStyle: tooltipStyle,
      arrowStyle: arrowStyle,
      innerStyle: innerStyle
    });
  };

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
    tooltip?.dispose();
  };

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

})();
