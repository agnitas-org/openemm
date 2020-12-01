AGN.Lib.Controller.new('mailing-preview', function() {
  var Form = AGN.Lib.Form,
      Storage = AGN.Lib.Storage;

  var needReload = false;

  this.addDomInitializer('mailing-preview', function($frame) {
    restoreFields();
    var form = Form.get($('#container-preview'));
    if (form.getValue('previewForm.reload') == 'false') {
      form.setValue('previewForm.reload', true);

      form.submit().done(function() {
        $('[data-stored-field]').on('change', function() {
          var $field = $(this);
          Storage.saveChosenFields($field);
          //necessary to prevent endless reloading after change fields with data-action='change-stored-header-data'
          needReload = needReload || isTriggeredField($field.prop('name'));
        });

        form.initFields();
        form.initValidator();
      });
    }
  });

  this.addAction({'click' : 'refresh-preview'}, function() {
    needReload = false;
    updatePreview();
  });

  this.addAction({'change': 'change-stored-header-data'}, function() {
    if (needReload) {
      needReload = false;
      updatePreview();
    }
  });

  this.addAction({'change': 'change-header-data'}, function() {
    updatePreview();
  });

  this.addAction({'click': 'toggle-tab-recipientMode'}, function() {
    changePreviewMode(1);
  });

  this.addAction({'click': 'toggle-tab-targetGroupMode'}, function(){
    changePreviewMode(2);
  });

  function changePreviewMode(value) {
    var $field = $('[name="previewForm.modeTypeId"]');
    if ($field.exists()) {
      $field.val(value);
      $field.trigger('change');
    }
  }

  function updatePreview(formCallback) {
    var form = Form.get($('#preview'));
    if (formCallback && typeof(formCallback) == 'function') {
      formCallback(form);
    }
    form.setValue('previewForm.reload', false);
    form.setResourceSelectorOnce('#preview');
    form.submit();
  }

  function restoreFields() {
    var needReload = false;

    $("[data-stored-field]").each(function() {
      var $e = $(this),
          name = $e.prop('name');

      if (!isTriggeredField(name)) {
        if (Storage.restoreChosenFields($e)) {
          needReload = true;
        }
      }
    });

    Storage.restoreChosenFields($("[name='previewForm.modeTypeId']"));
    Storage.restoreChosenFields($("[name='previewForm.customerATID']"));
    Storage.restoreChosenFields($("[name='previewForm.customerEmail']"));
    Storage.restoreChosenFields($("[name='previewForm.targetGroupId']"));
  }

  function isTriggeredField(fieldName) {
    if (!fieldName) {
      return false;
    }

    return fieldName == 'previewForm.customerATID' ||
      fieldName == 'previewForm.customerEmail' ||
      fieldName == 'previewForm.modeTypeId' ||
      fieldName == 'previewForm.targetGroupId';
  }
});
