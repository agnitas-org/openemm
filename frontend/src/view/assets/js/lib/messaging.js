/*doc
---
title: Messaging
name: js-messaging
category: Javascripts - Messaging
---

`AGN.Lib.Messaging` provides a micro messaging framework.
*/

/*doc
---
title: Subscribing
name: js-messaging-01
parent: js-messaging
---

Use `AGN.Lib.Messaging.subscribe(topic, callback)` to add a new callback as a listener for a given topic:

```js_example
AGN.Lib.Messaging.subscribe('abc:xyz', function() {
  console.log('ABC:XYZ');
});
```

You can add multiple callbacks to the same topic. An order of their execution (on message receive) corresponds to an order
of subscription calls:

```js_example
AGN.Lib.Messaging.subscribe('abc:xyz', function() {
  console.log('#1');
});

AGN.Lib.Messaging.subscribe('abc:xyz', function() {
  console.log('#2');
});
```
*/

/*doc
---
title: Unsubscribing
name: js-messaging-02
parent: js-messaging
---

Use `AGN.Lib.Messaging.unsubscribe(topic, callback)` to unsubscribe a particular callback from a given topic:

```js_example
function callback1() {
  console.log('Fire!');
}

// After this call an attempt to send a message will invoke a callback1
AGN.Lib.Messaging.subscribe('abc:xyz', callback1);

// After this call a callback1 is not subscribed to a given topic anymore
AGN.Lib.Messaging.unsubscribe('abc:xyz', callback1);
```

You can also unsubscribe all the callbacks of a given topic:

```js_example
AGN.Lib.Messaging.subscribe('abc:xyz', function() {
  console.log('#1');
});

AGN.Lib.Messaging.subscribe('abc:xyz', function() {
  console.log('#2');
});

// After this call no callback is listening a given topic.
AGN.Lib.Messaging.unsubscribe('abc:xyz');
```

Another way to unsubscribe a callback is to return explicit `false` value from that callback so it will not be invoked anymore:

```js_example
AGN.Lib.Messaging.subscribe('abc:xyz', function(options) {
  // Do some job.

  if (options && options.unsubscribe === true) {
    // Unsubscribe.
    return false;
  }
});
```
*/

/*doc
---
title: Sending messages
name: js-messaging-03
parent: js-messaging
---

To send a messages to all subscribed listeners use `AGN.Lib.Messaging.send(topic, [... args])`:

```js_example
AGN.Lib.Messaging.send('abc:xyz');
```

In addition you can pass some extra arguments that will be applied to callbacks invocation:

```js_example
AGN.Lib.Messaging.subscribe('abc:xyz', function(a, b, c) {
  console.log(a, b, c);
});

AGN.Lib.Messaging.send('abc:xyz', 111, 222, 333);
```
*/

(function() {
  var map = {};

  function send(topic) {
    var k = 'key#' + topic;
    var callbacks = map[k];
    var args = Array.prototype.slice.call(arguments, 1);

    if (callbacks) {
      // Unsubscribe all callbacks that returned false.
      map[k] = callbacks.filter(function(callback) {
        return callback.apply(this, args) !== false;
      });
    }
  }

  function subscribe(topic, callback) {
    var k = 'key#' + topic;

    if ($.isFunction(callback)) {
      if (!map[k]) {
        map[k] = [];
      }

      map[k].push(callback);
    } else {
      console.error('Callback must be a function');
    }
  }

  function unsubscribe(topic, /* optional */ callback) {
    if (arguments.length) {
      var k = 'key#' + topic;

      if (arguments.length > 1) {
        map[k] = map[k].filter(function(c) {
          return c !== callback;
        });
      } else {
        map[k] = [];
      }
    }
  }

  AGN.Lib.Messaging = {
    send: send,
    subscribe: subscribe,
    unsubscribe: unsubscribe
  };
})();
