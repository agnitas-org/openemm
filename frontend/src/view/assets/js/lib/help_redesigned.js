(function () {

  class Help {
    constructor($element) {
      this.$el = $element;

      const url = AGN.url($element.data('help'));
      this.config = null;

      $.get(url).done(xml => {
        this.config = $(xml);
        this.popoverInteractive();
        this.show();
      });
    }

    show() {
      this.$el.popover('show');
    }

    getConfig(key) {
      const val = this.config.find(key).text();
      return val.replace("<![CDATA[", "").replace("]]>", "");
    }

    popover() {
      if (!this.config) {
        return;
      }

      this.$el.popover('dispose');

      AGN.Lib.Popover.new(this.$el, {
        title: this.getConfig('title'),
        content: this.getConfig('content'),
        html: true,
        trigger: this.$el.data("trigger") || "focus",
        offset: [20, 0],
        popperConfig: {
          placement: 'bottom-start'
        }
      });
    }

    popoverInteractive() {
      if (!this.config) {
        return;
      }

      this.$el.popover('dispose');

      const popover = AGN.Lib.Popover.new(this.$el, {
        title: this.getConfig('title'),
        content: this.getConfig('content'),
        html: true,
        trigger: 'manual',
        offset: [20, 0],
        popperConfig: {
          placement: 'bottom-start'
        }
      });

      const $tip = $(popover.tip);

      // Made a tip (balloon) focusable
      $tip.attr('tabindex', 0);
      // Disable an outline when a tip is focused
      $tip.css('outline', 'none');

      // A popup help balloon should stay opened when either a help button or a help balloon has a focus
      let timeout = null;

      const onFocusLosing = function () {
        timeout = setTimeout(function () {
          popover.hide();
        }, 100);
      };

      const onFocusObtaining = function () {
        clearTimeout(timeout);
      };

      $tip.focusout(onFocusLosing);
      this.$el.focusout(onFocusLosing);
      $tip.focusin(onFocusObtaining);
      this.$el.focusin(onFocusObtaining);

      this.$el.on('shown.bs.popover', function () {
        const $content = $tip.children('.popover-content');

        const minWidthOld = $content.css('min-width');
        const oldWidth = $content.outerWidth();

        $content.css('min-width', '100%');
        $content.css('min-width', $content.outerWidth());

        if (oldWidth > $content.outerWidth()) {
          $content.css('min-width', minWidthOld || '0px');
        }
      });
    }

    static show($element) {
      const helpObj = $element.data('_help');
      if (helpObj) {
        helpObj.show();
        return
      }

      $element.data('_help', new Help($element));
    }
  }

  AGN.Lib.Help = Help;

})();
