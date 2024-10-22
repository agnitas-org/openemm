(function () {

  let config;

  AGN.Lib.Validator.new('import-profile-mappings/form', {
    valid: function ($e, options) {
      return !this.errors($e, options).length;
    },
    errors: function ($e, options) {
      const errors = [];
      validateDefaultValues(errors, options.$mappingsBlock);
      return errors;
    }
  });

  function isValidDataLength(columnInfo, value) {
    return value.length <= getMaxColumnLength(columnInfo);
  }

  function getMaxColumnLength(columnInfo) {
    const dataType = columnInfo.simpleDataType.toLowerCase();
    if (dataType === 'float' || dataType === 'numeric') {
      return columnInfo.numericPrecision + columnInfo.numericScale;
    }

    return columnInfo.characterLength;
  }

  function getDefValFieldValue($defValField) {
    let value = $defValField.val();
    if (value[0] == "'" && value[value.length - 1] == "'") {
      value = value.substring(1, value.length - 1);
    }
    return value;
  }

  function validateDefValField($defValField, errors) {
    const value = getDefValFieldValue($defValField);
    if (value && getColNameByDefValField($defValField) !== config.doNotImportValue) {
      const columnInfo = getValFieldColumnInfo($defValField);
      const dataType = columnInfo.simpleDataType.toLowerCase();

      if (columnInfo.columnName === 'email' && !AGN.Lib.Helpers.isValidEmail(value)) {
        errors.push({field: $defValField, msg: t('import.columnMapping.error.invalidEmail')});
      } else if (dataType === 'date' || dataType === 'datetime') {
        if (isDbDateFunction($defValField.val().trim())) {
          return;
        }
        validateDateOrDateTimeValue(dataType, value, errors, $defValField);
      } else if (!isValidValueForDataType(dataType, value)) {
        errors.push({field: $defValField, msg: t('import.columnMapping.error.type', dataType)});
      } else if (!isValidDataLength(columnInfo, value)) {
        errors.push({field: $defValField, msg: t('import.columnMapping.error.length', getMaxColumnLength(columnInfo))});
      }
    }
  }

  function validateDateOrDateTimeValue(dataType, value, errors, $defValField) {
    if (!value || !isValidValueForDataType(dataType, value)) {
      errors.push({
        field: $defValField, msg: t('import.columnMapping.error.dateFormat', dataType === 'datetime'
          ? window.adminDateFormat + ' hh:mm'
          : window.adminDateFormat)
      });
    }
  }

  function validateDefaultValues(errors, $mappingsBlock) {
    _.each($mappingsBlock.find('[data-mapping-defaultValue]'), defValueField => {
      const $defValueField = $(defValueField);
      const $row = $defValueField.closest('[data-mapping-row]');
      if (!$row.is('[data-mapping-new]') || (getColNameByDefValField($defValueField) !== config.doNotImportValue && !isCurrentTimeChecked())) {
        validateDefValField($defValueField, errors);
      }
    });
  }

  function getValFieldColumnInfo($defValField) {
    return config.columns[getColNameByDefValField($defValField)];
  }

  function getColNameByDefValField($defValField) {
    return $defValField.closest('[data-mapping-row]').find('[data-mapping-db-column]').first().val();
  }

  function isValidValueForDataType(dataType, value) {
    switch (dataType) {
      case 'numeric':
      case 'float':
      case 'double':
        return isNumeric(value);
      case 'integer':
        return isInteger(value);
      case 'date':
        return isValidDate(value);
      case 'datetime':
        return isValidDateTime(value);
      default:
        return true;
    }
  }

  function isDbDateFunction(val) {
    return /^(now|now\(\)|sysdate|sysdate\(\)|current_timestamp|current_timestamp\(\)|today)\s*([+\-]\s*\d+\s*)?$/mi.test(val);
  }

  function isValidDate(value) {
    return moment(value, window.adminDateFormat.toUpperCase(), true).isValid();
  }

  function isValidDateTime(value) {
    const dateAndTime = value.split(' ');
    return isValidDate(dateAndTime[0]) && /^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/.test(dateAndTime[1]);
  }

  function isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }

  function isInteger(value) {
    return /^-?\d+$/.test(value);
  }

  function isCurrentTimeChecked() {
    const currentTimeCheckbox = $('[data-action=set-today-date]');
    return currentTimeCheckbox.exists() && currentTimeCheckbox.is(":checked");
  }

  AGN.Lib.DomInitializer.new('import-profile-mappings-validator', function () {
    config = this.config;
  });
})();
