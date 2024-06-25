/*doc
---
title: Ajax
name: ajax-directives
category: Javascripts - Ajax
---

The `data-ajax` attribute enables ajax mode for a link (an `a` element)
so `AGN.Lib.Page.reload` is going to be used to perform a request (see `href` attribute):

```htmlexample
<a href="index.html" class="btn btn-regular btn-primary" data-ajax="">Go to index.html</a>
```

A `.js-ajax` css class can be used instead:

```htmlexample
<a href="index.html" class="btn btn-regular btn-primary js-ajax">Go to index.html</a>
```

By default a `GET` HTTP method is used. But you can specify an HTTP method to use as a value of the `data-ajax` attribute:

```htmlexample
<a href="index.html" class="btn btn-regular btn-primary" data-ajax="POST">Go to index.html</a>
```
*/

(function(){

  var Loader = AGN.Lib.Loader,
      Template  = AGN.Lib.Template,
      Page   = AGN.Lib.Page,
      CSRF = AGN.Lib.CSRF;

  var AjaxLoader = {
    initialize: function(options) {
      if (options.loader === true || options.loader === undefined) {
        options.loader = {
          show: function() {
            Loader.show();
          },
          hide: function() {
            Loader.hide();
          }
        };
      }
    },

    show: function(options) {
      var loader = options.loader;

      if (loader && loader.show) {
        try {
          loader.show();
        } catch (e) {
          console.error(e);
        }
      }
    },

    hide: function(options) {
      var loader = options.loader;

      if (loader && loader.hide) {
        try {
          loader.hide();
        } catch (e) {
          console.error(e);
        }
      }
    }
  };

  $.ajaxPrefilter(function(options) {
    AjaxLoader.initialize(options);
  });

  $(document).ajaxSend(function(e, jqxhr, options) {
    AjaxLoader.show(options);
    CSRF.setTokenToReqHeader(jqxhr, options.type);
  });

  $(document).ajaxComplete(function(e, jqxhr, options) {
    AjaxLoader.hide(options);
    CSRF.updateTokenInDOM();
  });

  $(document).ajaxError(function(e, jqxhr, options) {
    AjaxLoader.hide(options);

    // EMMGUI-471
    // skip error when request is aborted
    if (jqxhr.readyState == 0 || jqxhr.status == 0) {
      return;
    }

    if (options.statusCode && jqxhr.status in options.statusCode) {
      // Skip error if there's an explicit handler declared
      return;
    }

    var errorMessage;

    if (jqxhr.status == 401) {
      var event = $.Event('ajax:unauthorized');

      // Chiefly for retaining unsaved changes.
      $(document).trigger(event);

      if (!event.isDefaultPrevented() && jqxhr.responseText) {
        // Preserve existing behavior unless different one required (defined via options.statusCode['401'])
        Page.render(jqxhr.responseText);
      }
    } else if(jqxhr.status == 403) {
      if (jqxhr.responseText) {
        const $resp = $(jqxhr.responseText);
        const $csrfErrorMessage = $resp.filter('#csrf-error-message');

        if ($csrfErrorMessage.exists()) {
          AGN.Lib.Modal.create(_.template($csrfErrorMessage.html())({}), {})
          return;
        }
      }
      AGN.Lib.Modal.fromTemplate('permission-denied', {
        title: t('messages.permission.denied.title'),
        text: t('messages.permission.denied.text'),
        btn: t('defaults.ok')
      });
    } else {
      let isDefault = true;

      if (jqxhr.responseText) {
        try {
          const $resp = $(jqxhr.responseText);
          const $scriptMessages = $resp.all('[data-message]');

          if ($scriptMessages.exists()) {
            Page.render(jqxhr.responseText);
            isDefault = false;
          }
        } catch (e) {
          console.debug(e);
        }
      }

      if (isDefault) {
        AGN.Lib.Modal.fromTemplate('error', {
          headline: t('messages.error.headline'),
          text: t('messages.error.text'),
          reload: t('messages.error.reload')
        });
      }
    }
  });

  $(document).on('click', 'a[data-ajax], a.js-ajax', function(e) {
    var $e = $(this);
    e.preventDefault();
    Page.reload($e.attr('href'), true, $e.data('ajax'));
  });

  $(document).on('click', '[data-prevent-load]', function() {
    Loader.prevent();
  });

  window.onbeforeunload = function() {
    Loader.show();
  };

})();
