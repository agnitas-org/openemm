AGN.Lib.Controller.new('rules-add', function() {
  // keep a reference to the controller instance
  var self = this,
      ruleService = AGN.Lib.RuleService;

  this.addInitializer('readConfigRulesAdd', function($scope) {
    var config = $('[data-initializer="rules-config"]').data('config');
    self.config = AGN.Lib.Helpers.objFromString(config);
    self.config = ruleService.configure(self.config);

    $('select[data-action="updateMailingLinkIdNew"]').each(function() {
      var $select = $(this);
      ruleService.updateMailingLinksCache(self.config, $select.data('ruleid'), $select.val());
    });
  });

  this.addInitializer('initStateRulesAdd', function($scope) {
    ruleService.changedColumn(null, self.config);
  });

  this.addAction({
    'change': 'columnAndTypeNew, primaryOperatorNew'
  }, function() {
    var ruleId = this.el.data("ruleid");

    ruleService.changedColumn(ruleId, self.config);
  });

  this.addAction({
    'change': 'resetMailingUrlsNew'
  }, function() {
    var ruleId = this.el.data("ruleid");
    ruleService.resetMailingUrls(self.config, ruleId);
  });

  this.addAction({
    'change': 'updateMailingLinkIdNew'
  }, function() {
    var ruleId = this.el.data('ruleid');
    ruleService.updateMailingLinksCache(self.config, ruleId, this.el.val());
  });
});
