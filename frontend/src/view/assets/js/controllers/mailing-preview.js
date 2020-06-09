AGN.Lib.Controller.new('mailing-preview', function() {
  this.addDomInitializer('mailing-preview', function($frame) {
    restoreFields();
    var form = AGN.Lib.Form.get($('#container-preview'));
    if (form.getValue('reloadPreview') == 'false') {
      form.setValue('reloadPreview', true);
      form.submit().done(function(){
          $('[data-stored-field]').on('change', function() {
            var $field = $(this);
            AGN.Lib.Storage.saveChosenFields($field);
          });
      });
    }
  });

  this.addAction({'click' : 'refresh-preview'}, function() {
    updatePreview();
  });

  this.addAction({'change': 'change-header-data'}, function() {
    updatePreview();
  });

  function updatePreview() {
    var form = AGN.Lib.Form.get($('#preview'));
    form.setValue('reloadPreview', false);
    form.setResourceSelectorOnce('#preview');
    form.submit();
  }

  function restoreFields() {
    var needReload = false;

    //restore all fields without emails
    $("[data-stored-field]").each(function() {
      var $e = $(this),
        name = $e.prop('name');

      if (name != 'previewCustomerATID' && name != 'previewCustomerEmail') {
        if (AGN.Lib.Storage.restoreChosenFields($e)) {
          needReload = true;
        }
      }
    });

    //Reload if ATID chosen and ATID email changed or if Email chosen and text email changed
    // also restore emails fields
    if (needReload) {
      AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerATID']"));
      AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerEmail']"));
    } else {
      if ($("#preview_customer_Email").prop("checked") == true) {
        if (AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerEmail']"))) {
          needReload = true;
        }
        AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerATID']"));
      } else if ($("#preview_customer_ATID").prop("checked") == true) {
        if (AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerATID']"))) {
          needReload = true;
        }
        AGN.Lib.Storage.restoreChosenFields($("[name='previewCustomerEmail']"));
      }
    }

    return needReload;
  }

});
