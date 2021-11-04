AGN.Lib.Controller.new('exportwizard-view', function () {
  var adminHasOwnColumnPermission;
  var columnMappingRowTemplate;
  var bounceUserStatusCode;
  var profileFieldColumns;
  var $columnMappingTable;

  this.addDomInitializer('exportwizard-view', function () {
    adminHasOwnColumnPermission = this.config.adminHasOwnColumnPermission;
    if (adminHasOwnColumnPermission) {
      profileFieldColumns = this.config.profileFieldColumns;
      bounceUserStatusCode = this.config.bounceUserStatusCode;
      updateBouncesColumnOptionVisibility();
      initColumnMappingTable(getColumnMappingsSortedByName(this.config.columnMappings));
    }
  });

  this.addAction({click: 'save'}, function () {
    submitForm();
  });

  this.addAction({click: 'collect-data'}, function () {
    submitForm();
  });

  this.addAction({change: 'add-bounce-col-to-choose'}, function () {
    if (adminHasOwnColumnPermission) {
      updateBouncesColumnOptionVisibility();
    }
  });

  this.addAction({click: 'add-column-mapping', enterdown: 'mapping-enterdown'}, function () {
    this.event.preventDefault();
    if (lastRowCanBeAddedToTable()) {
      replaceNewButtonWithDeleteButton();
      appendRowToColumnMappingTable('', '');
      getMappingTableLastRow().find('[data-column-name]').focus();
    }
  });

  this.addAction({click: 'delete-column-mapping'}, function () {
    this.el.closest('tr').remove();
  });

  function initColumnMappingTable(mappings) {
    $columnMappingTable = $('#columnMappings tbody');
    columnMappingRowTemplate = AGN.Lib.Template.prepare('column-mapping-table-row');
    _.each(mappings, function (mapping) {
      appendRowToColumnMappingTable(mapping[0], mapping[1]);
    });
    appendRowToColumnMappingTable('', '');
  }

  function getColumnName($row) {
    return $row.find('[data-column-name]').val().trim();
  }

  function getColumnValue($row) {
    return $row.find('[data-column-value]').val().trim();
  }

  function appendRowToColumnMappingTable(name, value) {
    $columnMappingTable.append(columnMappingRowTemplate({name: name, value: value}));
  }

  function replaceNewButtonWithDeleteButton() {
    var btn = $columnMappingTable.find('[data-action="add-column-mapping"]')
    btn.after("<a href='#' class='btn btn-regular btn-alert' data-action='delete-column-mapping'>" +
      "<i class='icon icon-trash-o'></i></a>");
    btn.remove();
  }

  function lastRowCanBeAddedToTable() {
    var name = getColumnName(getMappingTableLastRow());
    return name && isColumnNameValid(name) && isColumnNameUnique(name);
  }

  function isColumnNameUnique(name) {
    var rows = $columnMappingTable.find('[data-column-mapping-row]').toArray();
    for (var i = 0; i < rows.length - 1; i++) {
      if (name == getColumnName($(rows[i]))) {
        AGN.Lib.Messages(t("defaults.error"), t("export.columnMapping.error.duplicate"), 'alert');
        return false;
      }
    }
    if (profileFieldColumns.map(function (profileField) {
      return profileField.column.toLowerCase();
    }).indexOf(name.toLowerCase()) >= 0) {
      AGN.Lib.Messages(t("defaults.error"), t("export.columnMapping.error.exist"), 'alert');
      return false;
    }
    return true;
  }

  function isColumnNameValid(name) {
    if (name.trim().length < 3) {
      AGN.Lib.Messages(t("defaults.error"), t("export.columnMapping.error.nameToShort"), 'alert');
      return false;
    }
    if (!/^[a-zA-Z0-9_]{3,}$/.test(name.trim())) {
      AGN.Lib.Messages(t("defaults.error"), t("export.columnMapping.error.invalidColName"), 'alert');
      return false;
    }
    return true;
  }

  function getMappingTableLastRow() {
    return $columnMappingTable.find('[data-column-mapping-row]:last-child');
  }

  function collectColumnMappings() {
    var mappings = [];
    _.each($columnMappingTable.find('[data-column-mapping-row]'), function (row) {
      var $row = $(row);
      var name = getColumnName($row);
      if (name) {
        mappings.push([name, getColumnValue($row)]);
      }
    })
    return mappings;
  }

  function setColumnMappingsToForm(form) {
    _.each(collectColumnMappings(), function (mapping) {
      form.setValue('customColumnMappings.' + mapping[0], mapping[1]);
    });
  }

  function getColumnMappingsSortedByName(mappings) {
    var result = [];
    Object.keys(mappings).forEach(function (key) {
      result.push([key, mappings[key]]);
    });
    result.sort(function (a, b) {
      if (a[0] < b[0]) {
        return -1;
      }
      if (a[0] > b[0]) {
        return 1;
      }
      return 0;
    });
    return result;
  }

  function updateBouncesColumnOptionVisibility() {
    var $bounceOption = $('#mailing-bounce-column-option');
    var selectedRecipientStatus = $('#recipient-export-recipientstatus').find(':selected').val();
    if (selectedRecipientStatus == bounceUserStatusCode) {
      $bounceOption.removeAttr("disabled");
    } else {
      var $columnsSelect = $('[name="columns"]');
      var options = $columnsSelect.val();
      var optionsWithoutBounceOption = options.filter(function (option) {
        return option != "MAILING_BOUNCE";
      });
      $columnsSelect.val(optionsWithoutBounceOption);
      $columnsSelect.trigger('change');
      $bounceOption.attr('disabled', 'disabled');
    }
  }

  function isLastColumnMappingValid() {
    var lastColName = getColumnName(getMappingTableLastRow());
    return lastColName == '' || isColumnNameValid(lastColName) && isColumnNameUnique(lastColName);
  }

  function isFormValid() {
    return isLastColumnMappingValid();
  }

  function submitForm() {
    if (isFormValid()) {
      var form = AGN.Lib.Form.get($('#exportWizardForm'));
      setColumnMappingsToForm(form);
      form.submit();
    }
  }
});
