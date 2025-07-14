(function(){

  var Messages = function (messageHead, messageContent, type, onClose, newestOnTop) {
        var $message,
            timeout,
            options = {
                timeout: {
                    'success': 3000,
                    'warning': 3000
                }
            };

    if (!type) {
      type = 'info';
    }

    if (newestOnTop === null) {
        newestOnTop = true;
    }

    timeout = options['timeout'][type || 'info'] || 0;

    // check if message is already displayed by comparing content and head
    _.each($('.notification'), function(message) {
      var $message = $(message),
          displayedMessageHead,
          displayedMessageContent;

      displayedMessageHead = $message.data('message-head');
      displayedMessageContent = $message.data('message-content');

      if (messageHead === displayedMessageHead && messageContent === displayedMessageContent) {
        $message.remove();
      }
    });

    $message = toastr[type](messageHead, messageContent, {timeOut: timeout, extendedTimeOut: timeout, onCloseClick: onClose, newestOnTop: newestOnTop});
    $message.data('message-head', messageHead);
    $message.data('message-content', messageContent);

    AGN.Lib.Controller.init($message);
    AGN.runAll($message);
  };

  var displayMessages = function(title, messages, type) {
    messages.forEach(function (text) {
      Messages(title, text, type);
    });
  };

  var hideMessages = function() {
    _.each($('.notification'), function(message) {
        message.remove();
    });
  };

  var JsonMessages = function (messages, removeDisplayedMessages) {
    if (!!removeDisplayedMessages) {
      hideMessages();
    }

    if (messages) {
      if (messages.success) {
        displayMessages(t("defaults.success"), messages.success, 'success');
      }

      if (messages.warning) {
        displayMessages(t("defaults.warning"), messages.warning, 'warning');
      }

      if (messages.alert) {
        displayMessages(t("defaults.error"), messages.alert, 'alert');
      }

      if (messages.fields) {
        messages.fields.forEach(function (f) {
          const $forms = $('form').filter(function () {
            return !!$(this)[0][f.name]
          });

          if ($forms.exists()) {
            AGN.Lib.Form.get($($forms[0])).showFieldError(f.name, f.message);
          }
        });
      }
    }
  };

  var RenderMessages = function ($resp) {
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
  
  AGN.Lib.Messages = Messages;
  AGN.Lib.JsonMessages = JsonMessages;
  AGN.Lib.RenderMessages = RenderMessages;
})();
