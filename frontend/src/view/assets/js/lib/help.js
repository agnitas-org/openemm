(function(){

  var Help;
  var helpPopoverTemplate = '<div class="popover" role="tooltip">' +
      '<div class="arrow"></div>' +
      '<h3 class="popover-title"></h3>' +
      '<div class="popover-content popover-scrollable"></div>' +
      '</div>';

  Help = function($element) {
    var url = AGN.url($element.data('help')),
        self = this,
        jqhxr;

    this.el = $element;

    jqhxr = $.get(url);
    jqhxr.done(function(xml){
      self.config  = $(xml);

      self.popoverInteractive();
      self.show();
    });
  };

  Help.show = function($element) {
    var helpObj;

    helpObj = $element.data('_help');

    if (helpObj) {
      helpObj.show();
      return
    }

    helpObj = new Help($element);
    $element.data('_help', helpObj);
  };

  Help.prototype.show = function() {
    this.el.popover('show');
  };

  Help.prototype.getConfig = function(key) {

    var val = this.config.find(key).text();

    return val.
            replace("<![CDATA[", "").
            replace("]]>", "");
  };

  Help.prototype.popover = function() {
    if (!this.config) {
      return;
    }

    this.el.popover('destroy');

    AGN.Lib.Popover.new(this.el, {
      template: helpPopoverTemplate,
      title:   this.getConfig('title'),
      content: this.getConfig('content'),
      html: true,
      trigger: this.el.data("trigger") || "focus"
    });
  };

  Help.prototype.popoverInteractive = function() {
    if (!this.config) {
      return;
    }

    this.el.popover('destroy');

    var popover = AGN.Lib.Popover.new(this.el, {
      template: helpPopoverTemplate,
      title: this.getConfig("title"),
      content: this.getConfig("content"),
      html: true,
      trigger: 'manual'
    });

    var elem = this.el;
    var tip = popover.tip();

    // Made a tip (balloon) focusable
    tip.attr('tabindex', 0);
    // Disable an outline when a tip is focused
    tip.css('outline', 'none');

    // A popup help balloon should stay opened when either a help button or a help balloon has a focus
    var timeout = null;

    var onFocusLosing = function() {
      timeout = setTimeout(function() {
        popover.hide();
      }, 100);
    };

    var onFocusObtaining = function() {
      clearTimeout(timeout);
    };

    tip.focusout(onFocusLosing);
    elem.focusout(onFocusLosing);
    tip.focusin(onFocusObtaining);
    elem.focusin(onFocusObtaining);

    elem.on('shown.bs.popover', function() {
      var $content = tip.children('.popover-content');

      var minWidthOld = $content.css('min-width');
      var oldWidth = $content.outerWidth();

      $content.css('min-width', '100%');
      $content.css('min-width', $content.outerWidth());

      if (oldWidth > $content.outerWidth()) {
        $content.css('min-width', minWidthOld || '0px');
      }
    });
  };

  AGN.Lib.Help = Help;

})();
