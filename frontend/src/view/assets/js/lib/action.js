(function(){

  var Action;

  Action = function(events, action, $scope) {
    var self = this;

    action = _.bind(action, this);

    if ($scope && $scope.exists()) {
      $scope = $scope.first();
    } else {
      $scope = $(document);
    }

    _.each(events, function(selector, trigger) {
      $scope.on(trigger, selector, function(e) {
        self.event = e;
        self.trigger = trigger;
        self.el = $(this);
        action();

        if ($(self.event.target).is("a")) {
          self.event.preventDefault();
        }
      })
    })
  };

  Action.new = function(events, action, $scope) {
    new Action(events, action, $scope);
  };

  Action.translate = function(events) {
    var cevents = {};

    _.each(events, function(triggers, e) {
      triggers = triggers.split(/,\s?/);
      triggers = _.map(triggers, function(t) {
        return '[data-action="' + t + '"]';
      }).join(', ');

      cevents[e] = triggers;
    });

    return cevents;
  };

  AGN.Lib.Action = Action;

})();
