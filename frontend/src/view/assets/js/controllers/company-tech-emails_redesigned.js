AGN.Lib.Controller.new('email-list-controller', function () {

  let targetFieldName = '';
  let separator = ' ';
  let emailsListManager;

  const dataList = () => {
    let result = [""];
    const stringValue = $(`input[name="${targetFieldName}"]`).val();
    if (stringValue) {
      result = stringValue.split(separator);
    }
    return result;
  };

  const saveDataToField = () => {
    const jsonData = emailsListManager.getJsonData();
    $(`input[name="${targetFieldName}"]`).val(jsonData.join(separator));
  };

  this.addDomInitializer('email-list-initializer', function () {
    const $scope = $(this.el);
    const disabled = this.config?.disabled;

    targetFieldName = $scope.data('target-field') || '';
    separator = $scope.data('data-emails-separator') || separator;
    emailsListManager = AGN.Lib.EmailsListManager.initialize($scope, dataList(), {disabled});
  });

  this.addAction({'click': 'delete-email'}, function () {
    emailsListManager.deleteRow($(this.el));
    saveDataToField();
  });

  this.addAction({'click': 'create-email'}, function () {
    emailsListManager.createRowAfter($(this.el));
    saveDataToField();
  });

  this.addAction({'change': 'change-trigger'}, function () {
    saveDataToField();
  });
});
