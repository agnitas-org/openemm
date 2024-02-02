(function() {

  const Tooltip = {};
  const Template = AGN.Lib.Template;
  const Helpers = AGN.Lib.Helpers;

//   const createTitle = (function() {
//     const templateWithTitle = Template.prepare('tooltip-message-with-title');
//     const templateJustContent = Template.prepare('tooltip-message-just-content');
//
//     return function(title, text) {
//       if (title) {
//         return templateWithTitle({
//           title: title,
//           content: text
//         });
//       }
//
//       return templateJustContent({
//         content: text
//       });
//     };
//   })();

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

  function getTooltipOptions(title, style) {
    return {
      title: title,
      container: 'body',
      template: createTemplate(style)
    };
  }

  // function getTooltipHelpOptions(title, text, style) {
  //   return {
  //     title: 'TEST',
  //     // title: createTitle(title, text),
  //     container: 'body',
  //     html: true,
  //     placement: 'auto top',
  //     template: createTemplate(style, 'helper-popup-arrow', 'helper-popup')
  //   };
  // }

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
    // else if ($e.is('[data-tooltip-help]')) {
    //   options = getTooltipHelpOptions(
    //     $e.data('tooltip-help'),
    //     $e.data('tooltip-help-text'),
    //     $e.data('tooltip-style')
    //   );
    // }

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
    
    // Create a new Intersection Observer
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          tooltip.hide();
        }
      });
    });
    observer.observe($e[0]);
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
//
//   Tooltip.setShown = function($e, isShown) {
//     if (isShown || isShown === undefined) {
//       $e.bsTooltip('show');
//     } else {
//       $e.bsTooltip('hide');
//     }
//   };
//
//   Tooltip.createTip = function($e, text, style) {
//     Tooltip.create($e, getTooltipOptions(text, style));
//   };
//
//   Tooltip.createHelpTip = function($e, title, text, style) {
//     Tooltip.create($e, getTooltipHelpOptions(title, text, style));
//   };

  AGN.Lib.Tooltip = Tooltip;

})();
