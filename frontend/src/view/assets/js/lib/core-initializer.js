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
