(function(){

  var Controller;

  Controller = function(func) {
    func = _.bind(func, this);
    func();
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

  Controller.init = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    $scope.all('[data-controller]').each(function() {
      var $controller = $(this),
          init = AGN.Opt.Controllers[$controller.data('controller')];

      if (init) {
        new AGN.Lib.Controller(init);

        // slated for removal
        AGN.Opt.Controllers[$controller.data('controller')] = undefined;
      }
    });
  };

  AGN.Lib.Controller = Controller;

})();
