(function() {
  var Validator;

  Validator = function(handlers) {
    if (handlers.init) {
      handlers.init();
      delete handlers.init;
    }
    return $.extend(this, handlers);
  };

  Validator.exists = function(name) {
    return AGN.Opt.Validators.hasOwnProperty(name);
  };

  Validator.get = function(name) {
    return Validator.exists(name) ? AGN.Opt.Validators[name] : null;
  };

  Validator.new = function(name, handlers) {
    if (Validator.exists(name)) {
      console.debug('Validator with name `' + name + '` was overridden');
    }

    var validator = new Validator(handlers);
    AGN.Opt.Validators[name] = validator;
    return validator;
  };

  // Stub implementation
  Validator.prototype.valid = function() {
    return true;
  };

  // Stub implementation
  Validator.prototype.errors = function() {
    return [];
  };

  AGN.Lib.Validator = Validator;
})();
