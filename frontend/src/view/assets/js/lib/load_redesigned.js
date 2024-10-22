(() => {

  class Load {

    static DATA_KEY = 'agn:load';

    constructor($element) {
      this.el = $element;
      this.url = $element.data('load');
      this.interval = parseInt($element.data('load-interval'));
      this.target = $element.data('load-target');

      if (this.interval) {
        this._interval = window.setInterval(() => this.load(), this.interval);
      }

      this.el.removeAttr('data-load');
      this.el.data(Load.DATA_KEY, this);
    }

    stop() {
      window.clearInterval(this._interval)
    }

    load() {
      if (this.loading) {
        return;
      }

      this.loading = true;

      $.get(this.url)
        .always(() => {
          if (this.interval) {
            this.loading = false;
          }
        })
        .done(resp => {
          const $resp = $(resp);
          if ($resp.all('[data-load-stop]').exists()) {
            this.stop();
          }

          let $target;
          if (!this.target) {
            $target = resp;
          } else if (this.target == 'body') {
            $target = /<body[^>]*>((.|[\n\r])*)<\/body>/im.exec(resp)[1];
          } else {
            $target = $resp.all(this.target);
          }

          if (this.el.is('[data-load-replace]')) {
            this.el.replaceWith($target);
            this.el = $target;
          } else {
            this.el.html($target);
          }

          AGN.Lib.Controller.init(this.el);
          AGN.runAll(this.el);
        })
        .fail(() => this.stop);
    }

    static load($element) {
      const instance = Load.get($element);
      if (instance) {
        instance.load();
        return;
      }

      if ($element.is(':hidden')) {
        // Postpone loading of hidden elements (keep an attribute)
        return;
      }

      new Load($element).load();
    }

    static get($el) {
      return $el.data(Load.DATA_KEY);
    }
  }


  AGN.Lib.Load = Load;

})();
