(function(){

  var Messages = function (messageHead, messageContent, type, onClose, newestOnTop) {
    let $message,
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

    timeout = options['timeout'][type || 'info'] || 0;
    
    // check if message is already displayed by comparing content and head
    if (findMessagesWithSameContent(messageHead, messageContent).length) {
      return;
    }

    $message = toastr[type](messageHead, messageContent, {timeOut: timeout, extendedTimeOut: timeout, onCloseClick: onClose, newestOnTop: newestOnTop});

    AGN.Lib.Controller.init($message);
    AGN.runAll($message);
  };

  var findMessagesWithSameContent = function (title, message) {
    return $('.popup-tab:not(.arrow)').filter((i, tab) => {
      const options = $(tab).data('options');
      return title === options.title && message === options.message;
    });
  }
  
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
        $.each(messages.fields, function(name, errors) {
          if (errors) {
            displayMessages(t("defaults.error"), errors, 'alert');
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

  Messages.warn = function(msgCode, ...args) {
    return Messages(t('defaults.warning'), t(msgCode, ...args), 'warning');
  };

  Messages.alert = function(msgCode, ...args) {
    return Messages(t('Error'), t(msgCode, ...args), 'alert');
  };

  AGN.Lib.Messages = Messages;
  AGN.Lib.JsonMessages = JsonMessages;
  AGN.Lib.RenderMessages = RenderMessages;
})();
