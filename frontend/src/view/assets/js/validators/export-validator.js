(function () {
  AGN.Lib.Validator.new('export/form', {
    valid: function ($e, options) {
      return !this.errors($e, options).length;
    },
    errors: function ($e, options) {
      var errors = [];
      validateColumnMappings(errors);
      return errors;
    }
  });

  function validateColumnMappings(errors) {
    var allCustomColumnNames = [];
    _.each($('#columnMappings tbody').find('[data-column-mapping-row]'), function (row) {
      var $row = $(row);
      var $columnNameField = $row.find('[data-column-name]');
      var name = getColumnName($columnNameField);
      checkColumnNameValid($columnNameField, errors);
      checkColumnNameUnique(name, allCustomColumnNames, errors, $columnNameField);
      allCustomColumnNames.push(name.toLowerCase());
    });
  }

  function checkColumnNameValid($columnNameField, errors) {
    var name = getColumnName($columnNameField);
    if ((!$columnNameField.is(':last-child') && name.length < 3) 
      || ($columnNameField.is(':last-child') && name && name.length < 3)) {
      errors.push({field: $columnNameField, msg: t('export.columnMapping.error.nameToShort')});
      return;
    }
    if ((!$columnNameField.is(":last-child") && !/^[a-zA-Z0-9_]{3,}$/.test(name))
      || ($columnNameField.is(":last-child") && name && !/^[a-zA-Z0-9_]{3,}$/.test(name))) {
      errors.push({field: $columnNameField, msg: t('export.columnMapping.error.invalidColName')});
    }
  }

  function checkColumnNameUnique(name, allCustomColumnNames, errors, $columnNameField) {
    if (name && allCustomColumnNames.indexOf(name.toLowerCase()) >= 0) {
      errors.push({field: $columnNameField, msg: t('export.columnMapping.error.duplicate')});
    }
    if (getProfileFieldColumnNames().indexOf(name.toLowerCase()) >= 0) {
      errors.push({field: $columnNameField, msg: t('export.columnMapping.error.exist')});
    }
  }

  function getColumnName($row) {
    return $row.val().trim();
  }
  
  function getProfileFieldColumnNames() {
    return $.map($('select[name="columns"] option') ,function(option) {
      return option.value.toLowerCase();
    });
  }
})();
