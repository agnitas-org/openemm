(function(){

  var Load;

  /* Loads an url and replace the content of the element with
     the results

     data-load="url"
     data-load-target="jQuery Selector"
     data-load-interval="int"
  */
  Load = function($element) {
    var callback, self = this;

    self.el = $element;
    self.url = $element.data('load');
    self.interval = parseInt($element.data('load-interval'));
    self.target = $element.data('load-target');

    if (self.interval) {
      callback = function() {
        self.load()
      };
      self._interval = window.setInterval(callback, self.interval);
    }

    self.el.removeAttr('data-load');
  };

  Load.load = function($element, isAlwaysLoad) {
    var loadObj;

    loadObj = $element.data('_load');

    if (loadObj) {
      loadObj.load();
      return
    }

    if ($element.is(':hidden') && isAlwaysLoad !== true) {
      // Postpone loading of hidden elements (keep an attribute)
      return;
    }

    loadObj = new Load($element);
    loadObj.load();
    $element.data('_load', loadObj);
  };

  Load.prototype.stop = function() {
    window.clearInterval(this._interval)
  };

  Load.prototype.load = function() {
    var jqxhr,
        self = this;

    if (self.loading) {
      return;
    }

    self.loading = true;
    jqxhr = $.get(self.url);

    jqxhr.always(function() {
      if (self.interval) {
        self.loading = false;
      }
    });

    jqxhr.fail(function() {
      self.stop();
    });

    jqxhr.done(function(resp) {
      var $resp = $(resp),
          $target,
          $loadStop;

      $loadStop = $resp.all('[data-load-stop]');

      if ($loadStop.length != 0) {
        self.stop();
      }

      if (!self.target) {
        $target = resp;
      } else if (self.target == 'body') {
        $target = /<body[^>]*>((.|[\n\r])*)<\/body>/im.exec(resp)[1];
      } else {
        $target = $resp.all(self.target);
      }

      self.el.html($target);
      AGN.Lib.Controller.init(self.el);
      AGN.runAll(self.el);
    });
  };


  AGN.Lib.Load = Load;

})();
