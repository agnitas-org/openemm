AGN.Lib.Controller.new('exportwizard-view', function () {
  var adminHasOwnColumnPermission;
  var columnMappingRowTemplate;
  var bounceUserStatusCode;
  var $columnMappingTable;

  this.addDomInitializer('exportwizard-view', function () {
    adminHasOwnColumnPermission = this.config.adminHasOwnColumnPermission;
    bounceUserStatusCode = this.config.bounceUserStatusCode;
    updateBouncesColumnOptionVisibility();
    if (adminHasOwnColumnPermission) {
      initColumnMappingTable(getColumnMappingsSortedByName(this.config.columnMappings));
    }
  });

  this.addAction({click: 'save'}, function () {
    submitForm();
  });

  this.addAction({click: 'collect-data'}, function () {
    submitForm();
  });

  this.addAction({change: 'add-bounce-col-to-choose'}, updateBouncesColumnOptionVisibility);

  this.addAction({click: 'add-column-mapping', enterdown: 'mapping-enterdown'}, function () {
    var form = AGN.Lib.Form.get($('#exportWizardForm'));
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
  
  function submitForm() {
    var form = AGN.Lib.Form.get($('#exportWizardForm'));
    if (form.valid({})) {
      setColumnMappingsToForm(form);
      form.submit();
    } else {
      form.handleErrors();
    }
  }
});
