AGN.Lib.Controller.new('mailing-followup-options', function() {

    var container = '';
    var mailingId = '';
    var additionalOptions = [];
    var advertisingUrl = '';

    this.addDomInitializer("mailing-followup-options", function() {
        var data = this.config;
        container = data.followUpContainer;
        mailingId = data.mailingIdContainer;
        additionalOptions = data.additionalOptions;
        advertisingUrl = data.advertisingUrl;

        changeFollowUpOptionsSet();

    });
    getAdditionalAdvertisingOptions = function() {
        return AGN.Lib.Template.text("followupAdvertisingOptions", {items: additionalOptions} );
    };

    changeFollowUpOptionsSet = function() {
        var mailingSelectedValue = AGN.Lib.Select.get($(mailingId)).getSelectedValue();

        jQuery.ajax({
            action: "POST",
            url: advertisingUrl,
            data: {
                mailingId: mailingSelectedValue
            },
            success: function (data) {
                $(jQuery.find(container +" .advertisingOption")).remove();
                if (data.isAdvertisingContentType) {
                    $(jQuery.find(container)).append(getAdditionalAdvertisingOptions());
                }
            }
        });
    };

    AGN.Lib.Action.new({'change' : mailingId}, function() {
        changeFollowUpOptionsSet()
    });
});