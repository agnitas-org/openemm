AGN.Lib.Controller.new('export-view', function () {
  var adminHasOwnColumnPermission;
  var columnMappingRowTemplate;
  var bounceUserStatusCode;
  var $columnMappingsTable;

  this.addDomInitializer('export-view', function () {
    adminHasOwnColumnPermission = this.config.adminHasOwnColumnPermission;
    bounceUserStatusCode = this.config.bounceUserStatusCode;
    updateBouncesColumnOptionVisibility();
    if (adminHasOwnColumnPermission) {
      initColumnMappingTable(getColumnMappingSortedByName(this.config.customColumns));
    }
  });

  this.addAction({click: 'save'}, function () {
    submitForm();
  });

  this.addAction({click: 'evaluate'}, function () {
    submitForm();
  });

  this.addAction({change: 'add-bounce-col-to-choose'}, updateBouncesColumnOptionVisibility);

  this.addAction({click: 'add-column-mapping', enterdown: 'mapping-enterdown'}, function () {
    var form = AGN.Lib.Form.get($('#exportForm'));
    var $lastRowNameInput = getMappingTableLastRow().find('[data-column-name]');
    this.event.preventDefault();
    
    if (form.valid({})) {
      if ($lastRowNameInput.val()) {
        form.cleanErrors();
        replaceNewButtonWithDeleteButton();
        appendRowToColumnMappingTable('', '');
        getMappingTableLastRow().find('[data-column-name]').focus();
      } else {
        $lastRowNameInput.focus();
      }
    } else {
      form.handleErrors();
    }
  });

  this.addAction({click: 'delete-column-mapping'}, function () {
    this.el.closest('tr').remove();
  });

  function initColumnMappingTable(mappings) {
    $columnMappingsTable = $('#columnMappings tbody');
    columnMappingRowTemplate = AGN.Lib.Template.prepare('column-mapping-table-row');
    _.each(mappings, function (mapping) {
      appendRowToColumnMappingTable(mapping.dbColumn, mapping.defaultValue);
    });
    appendRowToColumnMappingTable('', '');

    if (!AGN.Lib.Form.get($('#exportForm')).editable) {
      $columnMappingsTable.find(':input, button').prop('disabled', true);
      $columnMappingsTable.find('a').attr('disabled', 'disabled');
    }
  }

  function getColumnName($row) {
    return $row.find('[data-column-name]').val().trim();
  }

  function getColumnValue($row) {
    return $row.find('[data-column-value]').val().trim();
  }

  function appendRowToColumnMappingTable(name, value) {
    $columnMappingsTable.append(columnMappingRowTemplate({name: name, value: value}));
  }

  function replaceNewButtonWithDeleteButton() {
    var btn = $columnMappingsTable.find('[data-action="add-column-mapping"]')
    btn.after("<a href='#' class='btn btn-regular btn-alert' data-action='delete-column-mapping'>" +
      "<i class='icon icon-trash-o'></i></a>");
    btn.remove();
  }

  function getMappingTableLastRow() {
    return $columnMappingsTable.find('[data-column-mapping-row]:last-child');
  }

  function collectColumnMapping() {
    var mappings = [];
    _.each($columnMappingsTable.find('[data-column-mapping-row]'), function (row) {
      var $row = $(row);
      var name = getColumnName($row);
      if (name) {
        mappings.push([name, getColumnValue($row)]);
      }
    })
    return mappings;
  }

  function setColumnMappingToForm(form) {
    _.each(collectColumnMapping(), function (mapping, index) {
      form.setValue('customColumns[' + index + '].dbColumn', mapping[0]);
      form.setValue('customColumns[' + index + '].defaultValue', mapping[1]);
    });
  }

  function getColumnMappingSortedByName(mappings) {
    return mappings.sort(function (a, b) {
      if (a.dbColumn < b.dbColumn) {
        return -1;
      }
      if (a.dbColumn > b.dbColumn) {
        return 1;
      }
      return 0;
    });
  }

  function updateBouncesColumnOptionVisibility() {
    var $bounceOption = $('#mailing-bounce-column-option');
    var selectedRecipientStatus = $('#recipient-export-recipientstatus').find(':selected').val();
    if (selectedRecipientStatus == bounceUserStatusCode) {
      $bounceOption.removeAttr("disabled");
    } else {
      var $columnsSelect = $('[name="userColumns"]');
      var options = $columnsSelect.val();
      var optionsWithoutBounceOption = options.filter(function (option) {
        return option != "mailing_bounce";
      });
      $columnsSelect.val(optionsWithoutBounceOption);
      $columnsSelect.trigger('change');
      $bounceOption.attr('disabled', 'disabled');
    }
  }
  
  function submitForm() {
    var form = AGN.Lib.Form.get($('#exportForm'));
    if (form.valid({})) {
      setColumnMappingToForm(form);
      form.submit();
    } else {
      form.handleErrors();
    }
  }
});
