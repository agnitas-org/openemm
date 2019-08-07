(function() {

  var Tooltip = {},
      Template = AGN.Lib.Template,
      Helpers = AGN.Lib.Helpers;

  var createTitle = (function() {
    var templateWithTitle = Template.prepare('tooltip-message-with-title');
    var templateJustContent = Template.prepare('tooltip-message-just-content');

    return function(title, text) {
      if (title) {
        return templateWithTitle({
          title: title,
          content: text
        });
      }

      return templateJustContent({
        content: text
      });
    };
  })();

  var createTemplate = (function() {
    var make = Template.prepare('tooltip-template');

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

  function getTooltipHelpOptions(title, text, style) {
    return {
      title: createTitle(title, text),
      container: 'body',
      html: true,
      placement: 'auto top',
      template: createTemplate(style, 'helper-popup-arrow', 'helper-popup')
    };
  }

  Tooltip.options = function($e) {
    var options = {};

    if ($e.is('[data-tooltip]')) {
      var title = $e.data('tooltip');

      var source = $e.data('tooltip-src');
      if (source) {
        title = Template.text(source, {element: $e});
      }

      options = getTooltipOptions(
        title,
        $e.data('tooltip-style')
      );
    } else if ($e.is('[data-tooltip-help]')) {
      options = getTooltipHelpOptions(
        $e.data('tooltip-help'),
        $e.data('tooltip-help-text'),
        $e.data('tooltip-style')
      );
    }

    return $.extend(
      options,
      Helpers.objFromString($e.data('tooltip-options'))
    );
  };

  Tooltip.create = function($e, options) {
    Tooltip.remove($e);

    $e.bsTooltip(options);
    $e.on({
      'dragstart.tooltip': function() {
        $e.bsTooltip('hide');
        $e.bsTooltip('disable');
      },
      'dragstop.tooltip': function() {
        $e.bsTooltip('enable');
      }
    });
  };

  Tooltip.remove = function($e) {
    $e.bsTooltip('hide');
    $e.bsTooltip('destroy');
    $e.off('dragstart.tooltip dragstop.tooltip');
  };

  Tooltip.setEnabled = function($e, isEnabled) {
    if (isEnabled || isEnabled === undefined) {
      $e.bsTooltip('enable');
    } else {
      $e.bsTooltip('hide');
      $e.bsTooltip('disable');
    }
  };

  Tooltip.createTip = function($e, text, style) {
    Tooltip.create($e, getTooltipOptions(text, style));
  };

  Tooltip.createHelpTip = function($e, title, text, style) {
    Tooltip.create($e, getTooltipHelpOptions(title, text, style));
  };

  AGN.Lib.Tooltip = Tooltip;

})();
