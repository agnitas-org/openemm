AGN.Lib.Controller.new('mailing-followup-options', function() {
  var $container;
  var $mailingSelect = '';
  var additionalOptions = [];

  this.addDomInitializer("mailing-followup-options", function() {
    $container = $("#followUpType");
    $mailingSelect = $("#lightWeightMailingList");
    additionalOptions = this.config.additionalOptions;

    changeFollowUpOptionsSet();

    $mailingSelect.on('change', function() {
      changeFollowUpOptionsSet();
    });
  });

  var getAdditionalAdvertisingOptions = function() {
    return AGN.Lib.Template.text("followupAdvertisingOptions", {items: additionalOptions});
  };

  var changeFollowUpOptionsSet = function() {
    var mailingIdToCheck = AGN.Lib.Select.get($mailingSelect).getSelectedValue();

    $container.children('.advertisingOption').remove();
    if (mailingIdToCheck) {
      $.ajax({
        type: 'POST',
        url: AGN.url("/mailing/ajax/" + mailingIdToCheck + "/isAdvertisingContentType.action")
      }).done(function(resp) {
        if (resp && resp.success) {
          $container.append(getAdditionalAdvertisingOptions());
        }
      });
    }
  };
});
