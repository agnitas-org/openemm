AGN.Lib.Controller.new('email-list-controller', function() {

    var targetFieldName = '';
    var separator = ' ';
    var emailsListManager;

    var dataList = function() {
        var result = [""];
        var stringValue = $('input[name="' + targetFieldName + '"]').val();
        if(stringValue) {
            result = stringValue.split(separator);
        }
        return result;
    };

    var saveDataToField = function() {
        var jsonData = emailsListManager.getJsonData();
        $('input[name="' + targetFieldName + '"]').val(jsonData.join(separator));
    };

    this.addDomInitializer('email-list-initializer', function() {
        var scope = $(this.el);
        targetFieldName =  scope.data('target-field') || '';
        separator =  scope.data('data-emails-separator') || separator;

        emailsListManager = AGN.Lib.EmailsListManager.initialize(scope, dataList());
    });

    this.addAction({'click': 'delete-email'}, function(){
        emailsListManager.deleteRow($(this.el));
        saveDataToField();
    });

    this.addAction({'click': 'create-email'}, function() {
        emailsListManager.createRowAfter($(this.el));
        saveDataToField();
    });

    this.addAction({'change': 'change-trigger'}, function() {
        saveDataToField();
    });
});
