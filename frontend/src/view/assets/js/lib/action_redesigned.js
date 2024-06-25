(function(){

  class Action {
    constructor(events, action, $scope) {
      const self = this;

      action = _.bind(action, this);

      if ($scope && $scope.exists()) {
        $scope = $scope.first();
      } else {
        $scope = $(document);
      }

      _.each(events, (selector, trigger) => {
        $scope.on(trigger, selector, function(e, data) {
          self.event = e;
          self.trigger = trigger;
          self.el = $(this);
          self.data = data;
          action();

          const $target = $(self.event.target);
          if ($target.is("a") || $target.parent("a").length === 1) {
            self.event.preventDefault();
          }
        })
      });
    }

    static new(events, action, $scope) {
      new Action(events, action, $scope);
    }

    static translate(events) {
      const cevents = {};

      _.each(events, function(triggers, e) {
        triggers = triggers.split(/,\s?/);
        triggers = _.map(triggers, t => `[data-action="${t}"]`)
          .join(', ');

        cevents[e] = triggers;
      });

      return cevents;
    }
  }

  AGN.Lib.Action = Action;

})();
