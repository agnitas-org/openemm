(function () {
  AGN.Lib.Validator.new('export/form', {
    valid: function ($e, options) {
      return !this.errors($e, options).length;
    },
    errors: function ($e, options) {
      const errors = [];
      validateColumnMappings(errors);
      return errors;
    }
  });

  function validateColumnMappings(errors) {
    const allCustomColumnNames = [];
    _.each($('#custom-column-mappings tbody').find('tr'), function (row) {
      const $row = $(row);
      const $columnNameField = $row.find('[data-name="dbColumn"]');
      const name = getColumnName($columnNameField);
      checkColumnNameValid($columnNameField, errors);
      checkColumnNameUnique(name, allCustomColumnNames, errors, $columnNameField);
      allCustomColumnNames.push(name.toLowerCase());
    });
  }

  function checkColumnNameValid($columnNameField, errors) {
    const name = getColumnName($columnNameField);
    const isLastRow = $columnNameField.is(':last-child');

    if ((!isLastRow && name.length < 3) || (isLastRow && name && name.length < 3)) {
      errors.push({field: $columnNameField, msg: t('export.columnMapping.error.nameToShort')});
      return;
    }
    if ((!isLastRow && !/^[a-zA-Z0-9_]{3,}$/.test(name)) || (isLastRow && name && !/^[a-zA-Z0-9_]{3,}$/.test(name))) {
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
    return $.map($('select[name="userColumns"] option'), function(option) {
      return option.value.toLowerCase();
    });
  }
})();
