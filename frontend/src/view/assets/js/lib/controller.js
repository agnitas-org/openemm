(() => {

  const Controller = function(func) {
    func = _.bind(func, this);
    func(this);
  };

  Controller.prototype.addAction = function(events, action) {
    AGN.Lib.Action.new(AGN.Lib.Action.translate(events), action);
  };

  Controller.prototype.addInitializer = function(name, initializer) {
    AGN.Lib.CoreInitializer.new(name, initializer);
  };

  Controller.prototype.addDomInitializer = function(name, initializer) {
    AGN.Lib.DomInitializer.new(name, initializer);
  };

  Controller.prototype.runInitializer = function(name) {
    AGN.Lib.CoreInitializer.run(name);
  };

  Controller.new = function(name, func) {
    AGN.Opt.Controllers[name] = func;
  };

  Controller.newExtended = function (name, baseName, func) {
    Controller.new(name, instance => {
      const baseController = AGN.Opt.Controllers[baseName];
      if (baseController) {
        new Controller(baseController);
      }

      _.bind(func, instance)(instance);
    })
  }

  Controller.init = function($scope = $(document)) {
    $scope.all('[data-controller]').each(function() {
      const name = $(this).data('controller');
      const init = AGN.Opt.Controllers[name];

      if (init) {
        new AGN.Lib.Controller(init);

        // slated for removal
        AGN.Opt.Controllers[name] = undefined;
      }
    });
  };

  AGN.Lib.Controller = Controller;

})();
