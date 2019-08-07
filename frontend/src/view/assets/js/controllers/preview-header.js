AGN.Lib.Controller.new('preview-header', function() {
    var self = this;

    this.addInitializer('set-preview-customer-options', function() {
        var previewCustomerATID = $('#useCustomerEmail').prop("checked");
        $('#recipientEmail').prop("disabled", previewCustomerATID);
        $('#buttonPreviewCustomerEmail').prop("disabled", previewCustomerATID);
        $('#recipientId').prop("disabled", !previewCustomerATID);
    });

    this.addAction({'change': 'change-preview-customer-options'}, function() {
        self.runInitializer('set-preview-customer-options');
    });

});
