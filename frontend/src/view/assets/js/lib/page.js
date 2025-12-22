(() => {

  const Confirm = AGN.Lib.Confirm;

  class Page {

    static render(resp, address) {
      if (/<\s*\/\s*body\s*>/i.test(resp)) {
        Page.#renderBody(resp, address);
        return;
      }

      const $resp = $(resp);
      AGN.Lib.RenderMessages($resp);

      if ($resp.filter('.modal').exists()) {
        const deferred = $.Deferred();

        Confirm.create($resp);
        deferred.resolve(Confirm.get($resp));

        return deferred.promise();
      }
    }

    static #renderBody(resp, address) {
      const doc = new DOMParser().parseFromString(resp, 'text/html');

      if (address !== false && doc.head) {
        const uri = $(doc.head).data('origin-uri');
        if (uri) {
          address = uri;
        }
      }

      if (address) {
        window.history.pushState({}, '', address);
      }

      const posY = $(document).scrollTop();
      setTimeout(function () {
        AGN.Lib.Controller.init();
        AGN.runAll();
        $(document).scrollTop(posY);
      }, 100);

      document.body = doc.body;
      document.title = doc.title;

      const $body = $(document.body);

      $body.find('script').each(function(index, e) {
        const $e = $(e);

        if ($e.attr('src')) {
          const $script = $('<script></script>');

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
    }

    static reload(address, ajax, method) {
      if (ajax) {
        address = address || window.location.href;

        const deferred = $.Deferred();

        $.ajax(address, {
          method: Page.#getValidMethod(method)
        }).done(resp => {
          const promise = Page.render(resp, address);
          if (promise) {
            promise
              .done(deferred.resolve)
              .fail(deferred.reject);
          } else {
            deferred.resolve();
          }
        }).fail(() => deferred.reject());

        return deferred.promise();
      } else {
        if (address && address != window.location.href) {
          window.location.assign(address);
        } else {
          window.location.reload(true);
        }
        return null;
      }
    }

    static #getValidMethod(method) {
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

  }

  AGN.Lib.Page = Page;

})();
