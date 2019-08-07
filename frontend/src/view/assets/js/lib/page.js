(function() {
  var Confirm = AGN.Lib.Confirm,
      Page  = {};

  function renderMessages($resp) {
    $resp.all('script[data-message][type="text/html"]')
      .appendTo($(document.body));

    $resp.all('script[data-message][type="text/javascript"]').each(function() {
      try {
        eval($(this).html());
      } catch (exception) {
        console.debug(exception);
      }
    });
  }

  function getValidMethod(method) {
    if (method) {
      method = method.toUpperCase();

      switch (method) {
        case 'CONNECT':
        case 'DELETE':
        case 'GET':
        case 'HEAD':
        case 'OPTIONS':
        case 'POST':
        case 'PUT':
          return method;
      }
    }

    return 'GET';
  }

  Page.render = function(resp, address) {
    if (/<\s*\/\s*body\s*>/i.test(resp)) {
      var parser = new DOMParser();
      var doc = parser.parseFromString(resp, 'text/html');

      if (address !== false && doc.head) {
        var uri = $(doc.head).data('origin-uri');
        if (uri) {
          address = uri;
        }
      }

      if (address) {
        window.history.pushState({}, '', address);
      }

      var posY = $(document).scrollTop();
      setTimeout(function () {
        AGN.Lib.Controller.init();
        AGN.runAll();
        $(document).scrollTop(posY);
      }, 100);

      document.body = doc.body;
      document.title = doc.title;

      var $body = $(document.body);

      $body.find('script').each(function(index, e) {
        var $e = $(e);

        if ($e.attr('src')) {
          var $script = $('<script></script>');

          $.each(e.attributes, function() {
            if (this.specified) {
              $script.attr(this.name, this.value);
            }
          });

          try {
            $script.appendTo($body);
          } catch (exception) {
            console.debug(exception);
          }
        } else if ($e.attr('type') == 'text/javascript') {
          try {
            eval.call(window, $e.html());
          } catch (exception) {
            console.debug(exception);
          }
        }
      });
    } else {
      var $resp = $(resp);
      var $e = $resp.filter('.modal');

      if ($e.exists()) {
        var deferred = $.Deferred();

        renderMessages($resp);
        $resp.appendTo($(document.body));

        Confirm.create($resp);
        deferred.resolve(Confirm.get($resp));

        return deferred.promise();
      } else {
        $e = $resp.all('script[data-load]');

        if ($e.exists()) {
          return Page.reload($e.data('load'), true)
            .done(function() {
              renderMessages($resp);
            });
        } else {
          renderMessages($resp);
        }
      }
    }
  };

  Page.reload = function(address, ajax, method) {
    if (ajax) {
      address = address || window.location.href;

      var deferred = $.Deferred();

      $.ajax(address, {
        method: getValidMethod(method)
      }).done(function(resp) {
        var promise = Page.render(resp, address);
        if (promise) {
          promise
            .done(deferred.resolve)
            .fail(deferred.reject);
        } else {
          deferred.resolve();
        }
      }).fail(function() {
        deferred.reject();
      });

      return deferred.promise();
    } else {
      if (address && address != window.location.href) {
        window.location.assign(address);
      } else {
        window.location.reload(true);
      }
      return null;
    }
  };

  AGN.Lib.Page = Page;
})();
