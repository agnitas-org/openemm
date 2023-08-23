AGN.Lib.Controller.new('importprofile-fields', function () {
  var config;
  var $columnMappingTable;
  var form;
  var textDefValInputTemplate;
  var dateDefValInputTemplate;
  var DONT_IMPORT_COL_OPTION = 'do-not-import-column';
  var usedColNames = [];
  var usedNewMappingColName;

  this.addDomInitializer('importprofile-fields', function () {
    config = this.config;
    textDefValInputTemplate = AGN.Lib.Template.prepare('text-def-val-input');
    dateDefValInputTemplate = AGN.Lib.Template.prepare('date-def-val-input');
    $columnMappingTable = $('#columnMappings tbody');
    appendRowToMappingTable();
    removeUsedOptionsFromColNameSelects();
    form = AGN.Lib.Form.get($('#importProfileColumnsForm'));
    form.initFields();
    usedColNames = getUsedColNames();
  });

  this.addAction({change: 'changeNewColName'}, function () {
    if (usedNewMappingColName && usedNewMappingColName !== DONT_IMPORT_COL_OPTION) {
      addOldOptionToColNameSelects(usedNewMappingColName);
    }
    var colName = this.el.find(':selected').val();
    usedNewMappingColName = colName;
    removeNewOptionFromColNameSelects(colName);
    updateNewMappingDefValInput(colName);
  });

  this.addAction({change: 'changeExistColName'}, function () {
    var oldColNameOption = getOldColNameOption();
    if (oldColNameOption) {
      addOldOptionToColNameSelects(oldColNameOption);
      removeNewOptionFromColNameSelects(this.el.val());
      usedColNames = getUsedColNames();
    }
  });

  this.addAction({click: 'deleteMapping'}, function () {
    var $form = $('#importProfileColumnsForm');
    var colIndex = this.el.data('col_index');
    $form.append('<input type="hidden" name="removeMapping_' + colIndex + '" value="' + colIndex + '" /> ');
    $form.submit();
  });

  function submitForm() {
    if (form.valid({})) {
      form.cleanErrors();
      if ($('[data-action=changeDateInput]').is(":checked")) {
        var daysCount = parseInt($('#daysCount').val() || 0);
        var additionalDays = daysCount === 0 ? '' : (daysCount > 0 ? '+' + daysCount : daysCount);
        form.setValue('valueType', 'function');
        $('[name=newColumnMapping\\.defaultValue]').val('CURRENT_TIMESTAMP' + additionalDays);
      } else {
        form.setValue('valueType', 'value');
      }
      form.submit();
    } else {
      form.handleErrors();
    }
  }

  this.addAction({click: 'save'}, function () {
    form.setValue('action', config.ACTION_SAVE);
    submitForm();
  });
  
  this.addAction({click: 'saveAndStart'}, function () {
    form.setValue('action', config.ACTION_SAVE_AND_START);
    submitForm();
  });
  
  this.addAction({click: 'addMapping'}, function () {
    $('#importProfileColumnsForm').append('<input type="hidden" name="add" value="add" />');
    submitForm();
  });
  
  this.addAction({change: 'changeDateInput'}, function () {
    $('#dateInput').toggle(!this.el.is(':checked'));
    $("#daysInput").toggle(this.el.is(':checked'));
    handleIntegerInput($('#daysCount'));
  });

  function handleIntegerInput($input) {
    $input.on('change.offset keyup.offset', function (e) {
      var value = $input.val();

      if (value) {
        var offset = parseInt(value);
        if (!isNaN(offset)) {
          var newValue = (offset < 0 ? '' : '+') + offset;
          if (newValue != value) {
            $input.val(newValue);
          }
        } else {
          $input.val(value.replace(/[^\d+-]/g, ""));
        }
      } else if (e.type === 'change') {
        $input.val('+0');
      }
    });
  }

  function removeUsedOptionsFromColNameSelects() {
    _.each($('#columnMappings tbody').find('[name^=dbColumn_]'), function (colNameSelect) {
      removeNewOptionFromColNameSelects($(colNameSelect).val());
    });
  }

  function removeNewOptionFromColNameSelects(option) {
    _.each($('#columnMappings tbody').find('[name^=dbColumn_]'), function (colNameSelect) {
      removeOptionFromColNameSelect($(colNameSelect), option);
    });
    removeOptionFromColNameSelect($('[name=newColumnMapping\\.databaseColumn]'), option);
  }

  function addOldOptionToColNameSelects(option) {
    _.each($('#columnMappings tbody').find('[name^=dbColumn_]'), function (colNameSelect) {
      addOptionToColNameSelect($(colNameSelect), option);
    });
    addOptionToColNameSelect($('[name=newColumnMapping\\.databaseColumn]'), option);
  }

  function removeOptionFromColNameSelect($select, option) {
    if ($select.val() !== option && option !== DONT_IMPORT_COL_OPTION) {
      $select.find('[value="' + option + '"]').remove();
    }
  }

  function addOptionToColNameSelect($select, option) {
    if ($select.val() !== option) {
      $select.append($('<option>', {
        value: option,
        text: option
      }));
    }
  }

  function performElemPostDraw($inputCell, type) {
    if (type === 'date') {
      AGN.runAll($inputCell);
    } else if (type === 'datetime') {
      form.initFields();
    }
  }

  function getColumnDefValInputType(colName) {
    var type = config.columns[colName].dataType.toLowerCase();
    switch (type) {
      case 'date':
      case 'datetime':
        return type;
      default:
        return 'text';
    }
  }

  function updateNewMappingDefValInput(colName) {
    var type = getColumnDefValInputType(colName);
    var $currentDefValInput = $('[name=newColumnMapping\\.defaultValue]');
    var val = $currentDefValInput.val();
    var $inputCell = $currentDefValInput.closest("td");
    if ($inputCell.children().first().data('type') !== type) {
      $inputCell.empty();
      $inputCell.append(getDefValInputByType(type, val))
      performElemPostDraw($inputCell, type);
    }
  }

  function getDefValInputByType(type, val) {
    switch (type) {
      case 'date':
      case 'datetime':
        return dateDefValInputTemplate({withTime: type === 'datetime', val: val});
      default:
        return textDefValInputTemplate;
    }
  }

  function appendRowToMappingTable() {
    var newMappingRowTemplate = AGN.Lib.Template.prepare('new-mapping-row');
    $columnMappingTable.append(newMappingRowTemplate());
    AGN.Lib.CoreInitializer.run("select", $('[name=newColumnMapping\\.databaseColumn]'));
    AGN.Lib.CoreInitializer.run("select", $('[name=valueType]'));
  }

  function getUsedColNames() {
    var colNames = [];
    _.each($('#columnMappings tbody').find('[name^=dbColumn_]'), function (colNameSelect) {
      colNames.push($(colNameSelect).val());
    });
    return colNames;
  }

  function getOldColNameOption() {
    var currentUsedColNames = getUsedColNames();
    return usedColNames.filter(function (colName) {
      return currentUsedColNames.indexOf(colName) < 0;
    })[0];
  }
});
