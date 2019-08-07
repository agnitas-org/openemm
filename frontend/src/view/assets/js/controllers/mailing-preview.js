AGN.Lib.Controller.new('mailing-preview', function() {
  var self = this;
  
  this.addInitializer('MailingPreview', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    restoreFields();

    $("[data-stored-field]").change(function () {
      AGN.Lib.Storage.saveChosenFields($(this));
    });
  });

  function restoreFields() {
    var needReload = false;
    
    //restore all fields without emails
    $("[data-stored-field]").each(function () {
      if ($(this).attr("name") !== "previewCustomerATID" && $(this).attr("name") !== "previewCustomerEmail"){
        if (AGN.Lib.Storage.restoreChosenFields($(this))){
          needReload = true;
        }
      }
    });
    
    //Reload if ATID chosen and ATID email changed or if Email chosen and text email changed
    // also restore emails fields
    if (needReload){
      AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerATID']"));
      AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerEmail']"));
    } else {
      if ($("#preview_customer_Email").prop("checked") == true){
        if (AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerEmail']"))){
          needReload = true;
        }
        AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerATID']"));
      } else if ($("#preview_customer_ATID").prop("checked") == true){
        if (AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerATID']"))){
          needReload = true;
        }
        AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerEmail']"));
      }
    }
    if (needReload){
      $("#preview").submit();
    }
  }

});
