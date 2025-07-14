AGN.Lib.Controller.new('mailing-stat', function() {

  let conf;

  this.addDomInitializer("mailing-stat", function() {
    conf = this.config;
  });

  this.addAction({change: 'change-mailing'}, $el => {
    const form = AGN.Lib.Form.get($el);
    const mailingId = parseInt($el.val());
    form.setActionOnce(AGN.url(mailingId
      ? `/statistics/mailing/${mailingId}/view.action?statWorkflowId=${conf.statWorkflowId}`
      : `/workflow/${conf.statWorkflowId}/statistic.action`))
    form.submit();
  });
});
