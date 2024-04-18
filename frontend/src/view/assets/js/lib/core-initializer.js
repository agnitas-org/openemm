/*doc
---
title: Overview
name: js-initializers
category: Javascripts - Initializers
---

Initializers are simple JS functions that are automatically invoked to initialize the document (page) or its fragment.

Initialization is usually required when the document (page) is loaded but there are other cases when initialization happens:
you can load some fragments from server (e.g. by submitting a form) or render them from `mustache` templates. Either way you
need to attach some event handlers, populate UI controls with data and so on.
So once html code is rendered the initializers are triggered.

There are two types of initializers available for that purpose: `Core` initializers and `Dom` initializers.

The `AGN.runAll()` call invokes all `Core` initializers first, then it invokes `Dom` initializers.

*/

/*doc
---
title: Core Initializers
name: js-initializers-01
parent: js-initializers
---

The `Core` initializer is simply invoked when document or fragment is loaded. A typical use case is to scan the document/fragment
for certain elements, attach event handlers or call the 3rd-party initializers (like ACE Editor or Select2) for each one.

Here is a very basic example:

```js_example
AGN.Lib.CoreInitializer.new('alert-buttons', function() {
  $('[data-alert]').on('click', function() {
    alert($(this).data('alert'));
  });
});
```

```html_example
<button data-alert="Code red" class="btn btn-regular btn-primary">
  Code red
</button>

<button data-alert="Code blue" class="btn btn-regular btn-primary">
  Code blue
</button>

<button data-alert="Code green" class="btn btn-regular btn-primary">
  Code green
</button>
```

There's a `$scope` argument passed to a handler. It tells which part of a document has been refreshed and needs to be initialized.
If it's `undefined` then the whole document is a scope.

So the previous example should better look like this, so you don't re-initialize what's already been initialized:

```js
AGN.Lib.CoreInitializer.new('alert-buttons', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  $scope.all('[data-alert]').on('click', function() {
    alert($(this).data('alert'));
  });
});
```

Some initializers depend on each other. They could be defined in separate files but there's a way to make them execute in proper order.
All you need to do is to specify dependencies argument:

```js_example
var progress = [];

AGN.Lib.CoreInitializer.new('eating', ['cooking'], function() {
  progress.push('eating');

  $('#show-initialization-order-button').on('click', function() {
    alert(progress);
  });
});

AGN.Lib.CoreInitializer.new('cooking', ['shopping'], function() {
  progress.push('cooking');
});

AGN.Lib.CoreInitializer.new('shopping', function() {
  progress.push('shopping');
});
```

```html_example
<button id="show-initialization-order-button" class="btn btn-regular btn-primary">
  Tell me the initialization order
</button>
```

No matter where you define the initializers their execution order will be adjusted to fulfill dependencies.
Keep in mind that cyclic dependencies are not allowed.

*/

(function() {
  var map = {};

  function register(name) {
    var dependencies = null;
    var handler;

    if (arguments.length > 2) {
      dependencies = arguments[1];
      handler = arguments[2];
    } else {
      handler = arguments[1];
    }

    if (_.isFunction(handler)) {
      map['key#' + name] = {name: name, handler: handler, dependencies: dependencies};
    } else {
      console.error('Handler must be a function');
    }
  }

  function execute($scope, initializer, executed, pending) {
    var key = 'key#' + initializer.name;

    if (executed[key]) {
      return;
    }

    if (Array.isArray(initializer.dependencies) && initializer.dependencies.length) {
      pending.push(initializer.name);

      initializer.dependencies.forEach(function(dependencyName) {
        var dependency = map['key#' + dependencyName];

        if (dependency) {
          if (pending.includes(dependencyName)) {
            var description = pending.map(function(value) { return '`' + value + '`'; }).join(' => ');
            console.error('Cyclic initializer dependency detected (' + description + " => `" + dependencyName + '`)');
            return;
          }

          execute($scope, dependency, executed, pending);
        } else {
          console.error('Unknown initializer dependency: `' + name + "`");
        }
      });

      pending.pop();
    }

    try {
      initializer.handler.call(initializer.handler, $scope);
    } catch (e) {
      console.error(e);
    }

    executed[key] = true;
  }

  function autorun($scope) {
    var executed = {};
    var pending = [];

    $.each(map, function(k, initializer) {
      execute($scope, initializer, executed, pending);
    });
  }

  function run(name, $scope) {
    if (Array.isArray(name)) {
      name.forEach(function(nm) {
        run(nm, $scope);
      });
    } else {
      var initializer = map['key#' + name];

      if (initializer) {
        initializer.handler.call(initializer.handler, $scope);
        return true;
      } else {
        console.error('Unknown initializer: `' + name + "`");
        return false;
      }
    }
  }

  AGN.Lib.CoreInitializer = {
    autorun: autorun,
    run: run,
    new: register
  };
})();
