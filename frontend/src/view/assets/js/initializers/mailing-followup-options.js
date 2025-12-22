AGN.Lib.Controller.new('mailing-followup-options', function() {

  let $container;
  let $mailingSelect = '';
  let additionalOptions = [];

  this.addDomInitializer("mailing-followup-options", function() {
    $container = $("#followUpType");
    $mailingSelect = $("#lightWeightMailingList");
    additionalOptions = this.config.additionalOptions;

    changeFollowUpOptionsSet();

    $mailingSelect.on('change', function() {
      changeFollowUpOptionsSet();
    });
  });

  const changeFollowUpOptionsSet = function() {
    const mailingIdToCheck = AGN.Lib.Select.get($mailingSelect).getSelectedValue();

    $container.children('.advertisingOption').remove();
    if (mailingIdToCheck && parseInt(mailingIdToCheck) > 0) {
      $.get(AGN.url(`/mailing/ajax/${mailingIdToCheck}/isAdvertisingContentType.action`), resp => {
        if (resp?.success) {
          $container.append(AGN.Lib.Template.text(
            'followupAdvertisingOptions',
            { items: additionalOptions }
          ));
        }
      });
    }
  };
});
