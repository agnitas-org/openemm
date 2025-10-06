(() => {

  class Help {
    constructor($element) {
      this.$el = $element;
      this.config = null;

      const url = `help_${window.helpLanguage || 'en'}/${$element.data('help')}`;

      $.get(AGN.url(url)).done(xml => {
        this.config = $(xml);
        this.popover();
        this.show();
      });
    }

    show() {
      this.$el.popover('show');
    }

    getConfig(key) {
      const val = this.config.find(key).text().replace("<![CDATA[", "").replace("]]>", "");
      if (!this.$el.is('[data-help-options]')) {
        return val;
      }
      return _.template(val)(AGN.Lib.Helpers.objFromString(this.$el.data('help-options')));
    }

    popover() {
      if (!this.config) {
        return;
      }

      this.$el.popover('dispose');

      const popover = AGN.Lib.Popover.create(this.$el, {
        title: this.getConfig('title'),
        content: this.getConfig('content'),
        html: true,
        trigger: 'manual',
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

      const onFocusLosing = () => {
        timeout = setTimeout(() => popover.hide(), 100);
      };

      const onFocusObtaining = () => clearTimeout(timeout);

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
      const instance = $element.data('agn:help');
      if (instance) {
        instance.show();
      } else {
        $element.data('agn:help', new Help($element));
      }
    }
  }

  AGN.Lib.Help = Help;

})();
