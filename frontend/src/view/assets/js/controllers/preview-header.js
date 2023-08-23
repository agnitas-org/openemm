AGN.Lib.Controller.new('preview-header-new', function() {
    var self = this;

    this.addInitializer('set-preview-customer-options', function() {
        var isUsedCustomerEmail = $('[name="useCustomerEmail"]:checked').val() == 'true';

        $('[name="customerEmail"]').prop('disabled', !isUsedCustomerEmail);
        $('#btnCustomEmailRefresh').prop('disabled', !isUsedCustomerEmail);

        $('select[name="customerID"]').prop('disabled', isUsedCustomerEmail);
    });

    this.addAction({'change': 'change-preview-customer-options'}, function() {
        self.runInitializer('set-preview-customer-options');
    });
});
