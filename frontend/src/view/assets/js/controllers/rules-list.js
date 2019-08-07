AGN.Lib.Controller.new('rules-list', function() {
  // keep a reference to the controller instance
  var self = this,
      ruleService = AGN.Lib.RuleService;

  this.addInitializer('readConfigRulesList', function() {
    var config = $('[data-initializer="rules-config"]').data('config');
    self.config = AGN.Lib.Helpers.objFromString(config);
    self.config = ruleService.configure(self.config);

    $('select[data-action="updateMailingLinkId"]').each(function() {
      var $select = $(this);
      ruleService.updateMailingLinksCache(self.config, $select.data('ruleid'), $select.val());
    });

    $('select[data-selected-column]').each(function() {
      var $select = $(this);
      $select.on('select2-opening', function() {
        if ($select.data('_loaded')) {
          return;
        }

        var $options = self.config.allColumns.clone();
        $options.filter('option[value="' + $select.data('selected-column') + '"]').attr('selected', '');
        $select.html($options);
        $select.data('_loaded', true);
      });
    });
  });

  this.addAction({
    'change': 'columnAndType, primaryOperator'
  }, function() {
    var ruleId = this.el.data("ruleid");

    ruleService.changedColumn(ruleId, self.config);
  });

  this.addAction({
    'change': 'resetMailingUrls'
  }, function() {
    var ruleId = this.el.data("ruleid");
    ruleService.resetMailingUrls(self.config, ruleId);
  });

  this.addAction({
    'change': 'updateMailingLinkId'
  }, function() {
    var ruleId = this.el.data('ruleid');
    ruleService.updateMailingLinksCache(self.config, ruleId, this.el.val());
  });
});
