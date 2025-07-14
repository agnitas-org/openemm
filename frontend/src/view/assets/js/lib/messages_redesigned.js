/*doc
---
title: Messages
name: js_messages
category: Javascripts - Messages
---

There are several utility classes for displaying popups. All of them are useful for different cases.

*/

/*doc
---
title: AGN.Lib.Messages
name: js_messages_01_default
parent: js_messages
---

This class is useful when you need to display a popup of a certain type with certain content from code.

```htmlexample
<div class="d-flex flex-column gap-1">
  <button type="button" class="btn btn-danger" data-show-popup="alert" data-popup-header="Alert">Alert popup</button>
  <button type="button" class="btn btn-warning" data-show-popup="warning" data-popup-header="Warning">Warning popup</button>
  <button type="button" class="btn btn-success" data-show-popup="success" data-popup-header="Success">Success popup</button>
  <button type="button" class="btn btn-primary" data-show-popup="info" data-popup-header="Info">Info popup</button>
</div>

<script type="text/javascript">
  AGN.Lib.Action.new({click: '[data-show-popup]'}, function() {
    AGN.Lib.Messages(this.el.data('popup-header'), this.el.text(), this.el.data('show-popup'));
  });
</script>
```
 */

/*doc
---
title: AGN.Lib.RenderMessages
name: js_messages_02_render
parent: js_messages
---

This class is useful when you have messages inside the DOM element and want to display them.
Typically, it used if server sends part of html, and it contains messages that should be rendered.

```htmlexample
  <script type="text/javascript">
    AGN.Lib.Action.new({click: '#saveBtn'}, function() {
      $.get('/someUrl')
        .done(resp => AGN.Lib.RenderMessages($(resp));
    });
  </script>
```
*/

/*doc
---
title: AGN.Lib.JsonMessages
name: js_messages_03_json
parent: js_messages
---

This class is useful when server sends messages as JSON.
The structure of the JSON object you can find in example below.

```htmlexample
  <button id="saveBtn2" class="btn btn-primary">Click me</button>

  <script type="text/javascript">
    AGN.Lib.Action.new({click: '#saveBtn2'}, function() {
      const jsonResponse = {
        data: 'someData',
        popups: {
          success: ['Success1', 'Success2'],
          warning: ['Warning1', 'Warning2'],
          alert:   ['Alert1', 'Alert2', 'Alert3'],
          info:    ['Info1']
        }
      };

      AGN.Lib.JsonMessages(jsonResponse.popups);
    });
  </script>
```
*/

(() => {

  const Messages = function (messageHead, messageContent, type, onClose, newestOnTop) {
    const options = {
        timeout: {
          'success': 3000,
          'warning': 15000,
          'alert': 60000
        }
      };

    if (!type) {
      type = 'info';
    }

    const timeout = options['timeout'][type || 'info'] || 0;
    
    // check if message is already displayed by comparing content and head
    if (findMessagesWithSameContent(messageHead, messageContent).length) {
      return;
    }

    const $message = toastr[type](messageHead, messageContent, {timeOut: timeout, extendedTimeOut: timeout, onCloseClick: onClose, newestOnTop: newestOnTop});

    AGN.Lib.Controller.init($message);
    AGN.runAll($message);
  };

  const findMessagesWithSameContent = function (title, message) {
    return $('.popup-tab:not(.arrow)').filter((i, tab) => {
      const options = $(tab).data('options');
      return title === options.title && message === options.message;
    });
  }
  
  const displayMessages = function(title, messages, type) {
    messages.forEach(text => Messages(title, text, type));
  };

  const JsonMessages = function (messages, removeDisplayedMessages = false) {
    if (removeDisplayedMessages) {
      _.each($('.popup'), message => $(message).remove());
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

      messages.fields?.forEach(fieldData => {
        AGN.Lib.Form.showFieldError(fieldData.name, fieldData.message);
      });
    }
  };

  const RenderMessages = function ($resp) {
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

  Messages.success = function(msgCode, ...args) {
    Messages.successText(t(msgCode, ...args));
  };

  Messages.successText = function(text) {
    Messages(t('defaults.success'), text, 'success');
  };

  Messages.alert = function(msgCode, ...args) {
    Messages.alertText(t(msgCode, ...args));
  };

  Messages.alertText = function (text) {
    Messages(t('defaults.error'), text, 'alert');
  }

  Messages.warn = function(msgCode, ...args) {
    Messages.warnText(t(msgCode, ...args));
  };

  Messages.warnText = function (text) {
    Messages(t('defaults.warning'), text, 'warning');
  }

  Messages.info = function(msgCode, ...args) {
    Messages.infoText(t(msgCode, ...args));
  };

  Messages.infoText = function (text) {
    Messages(t('defaults.info'), text, 'info');
  }

  Messages.defaultSaved = function() {
    Messages.success('defaults.saved');
  };

  Messages.defaultError = function() {
    Messages.alert('defaults.error');
  };

  AGN.Lib.Messages = Messages;
  AGN.Lib.JsonMessages = JsonMessages;
  AGN.Lib.RenderMessages = RenderMessages;
})();
