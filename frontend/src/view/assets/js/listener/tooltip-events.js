;(function(){
  var Tooltip = AGN.Lib.Tooltip;
  $(window).on("load", function() {
    var body = $('body');
    if (AGN.isIE) {
      body.on('DOMNodeInserted', '[data-tooltip], [data-tooltip-help]', function (event) {
        var $e = $(event.target);
        Tooltip.create($e, Tooltip.options($e));
      });
    } else {
      body.on('DOMNodeInserted', function(event) {
        $(event.target).all('[data-tooltip], [data-tooltip-help]').each(function() {
          var $e = $(this);
          Tooltip.create($e, Tooltip.options($e));
        });
      });
    }
  });
})();