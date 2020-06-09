AGN.Lib.Controller.new('mailing-followup-options', function() {
    var $container;
    var $mailingSelect = '';
    var additionalOptions = [];
    var advertisingUrl = '';

    this.addDomInitializer("mailing-followup-options", function() {
        var data = this.config;

        $container = $(data.followUpContainer);
        $mailingSelect = $(data.mailingIdContainer);
        additionalOptions = data.additionalOptions;
        advertisingUrl = data.advertisingUrl;

        changeFollowUpOptionsSet();

        $mailingSelect.on('change', function() {
            changeFollowUpOptionsSet();
        });
    });

    getAdditionalAdvertisingOptions = function() {
        return AGN.Lib.Template.text("followupAdvertisingOptions", {items: additionalOptions} );
    };

    changeFollowUpOptionsSet = function() {
        var mailingId = AGN.Lib.Select.get($mailingSelect).getSelectedValue();

        jQuery.ajax({
            action: "POST",
            url: advertisingUrl,
            data: {
                mailingId: mailingId
            },
            success: function (data) {
                $container.children('.advertisingOption').remove();
                if (data.isAdvertisingContentType) {
                    $container.append(getAdditionalAdvertisingOptions());
                }
            }
        });
    };
});
