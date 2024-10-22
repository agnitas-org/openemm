AGN.Lib.Controller.new('export-view', function () {
  let bounceUserStatusCode;
  let customMappingTable;
  let form;

  this.addDomInitializer('export-view', function () {
    bounceUserStatusCode = this.config.bounceUserStatusCode;
    updateBouncesColumnOptionVisibility();
    form = AGN.Lib.Form.get($('#exportForm'));
    customMappingTable = new CustomMappingTable($('#custom-column-mappings'));
    control24ColInfoDisplaying();
  });

  function control24ColInfoDisplaying() {
    const selectedColumns = $('#user-columns').val();
    const shown = selectedColumns.includes('lastopen_date') || selectedColumns.includes('lastclick_date');
    $('#24h-col-info').toggle(shown);
  }
  
  this.addAction({click: 'save'}, submitForm);
  this.addAction({click: 'evaluate'}, submitForm);
  this.addAction({change: 'add-bounce-col-to-choose'}, updateBouncesColumnOptionVisibility);
  this.addAction({change: 'user-columns-update'}, control24ColInfoDisplaying);

  function updateBouncesColumnOptionVisibility() {
    const $bounceOption = $('#mailing-bounce-column-option');
    const selectedRecipientStatus = $('#recipient-export-recipientstatus').find(':selected').val();
    if (selectedRecipientStatus == bounceUserStatusCode) {
      $bounceOption.removeAttr("disabled");
    } else {
      const columnsSelect = AGN.Lib.Select.get($('#user-columns'));
      columnsSelect.unselectValue("mailing_bounce")
      $bounceOption.attr('disabled', 'disabled');
    }
  }

  function setColumnMappingToForm(form) {
    _.each(customMappingTable.collect(), function (mapping, index) {
      form.setValue('customColumns[' + index + '].dbColumn', mapping.dbColumn);
      form.setValue('customColumns[' + index + '].defaultValue', mapping.defaultValue);
    });
  }
  
  function submitForm() {
    if (form.valid({})) {
      setColumnMappingToForm(form);
      form.submit();
    } else {
      form.handleErrors();
    }
  }
  
  class CustomMappingTable extends AGN.Lib.InputTable {
    addEmptyRow() {
      if (form.valid({})) {
        form.cleanFieldFeedback();
        super.addEmptyRow();
      } else {
        form.handleErrors();
      }
    }
  }
});
