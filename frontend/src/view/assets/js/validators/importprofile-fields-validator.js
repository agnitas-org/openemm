AGN.Lib.CoreInitializer.new('importprofile-fields-validator', function () {
  var config;
  var DATE_FORMAT = 'dd.mm.yyyy';
  
  AGN.Lib.Validator.new('importprofile-fields/form', {
    init: function () {
      var $config = $("#config\\:importprofile-fields-validator");
      config = $config.exists() ? $config.json() : {};
    },
    valid: function ($e, options) {
      return !this.errors($e, options).length;
    },
    errors: function ($e, options) {
      var errors = [];
      if ($('#columnMappings').length) {
        validateDefaultValues(errors);
      }
      return errors;
    }
  });

  function isValidDataLength(columnInfo, value) {
    return value.length <= columnInfo.maxDataSize;
  }

  function getDefValFieldValue($defValField) {
    var value = $defValField.val();
    if (value[0] == "'" && value[value.length - 1] == "'") {
      value = value.substring(1, value.length - 1);
    }
    return value;
  }

  function validateDefValField($defValField, errors) {
    var value = getDefValFieldValue($defValField);
    if (value && getColNameByDefValField($defValField) !== 'do-not-import-column') {
      var columnInfo = getValFieldColumnInfo($defValField);
      var dataType = columnInfo.dataType.toLowerCase();
      if (dataType === 'date' || dataType === 'datetime') {
        if (isDbDateFunction($defValField.val().trim())) {
          return;
        }
        validateDateOrDateTimeValue(dataType, value, errors, $defValField);
      } else if (!isValidValueForDataType(dataType, value)) {
        errors.push({field: $defValField, msg: t('import.columnMapping.error.type', dataType)});
      } else if (!isValidDataLength(columnInfo, value)) {
        errors.push({field: $defValField, msg: t('import.columnMapping.error.length', columnInfo.maxDataSize)});
      }
    }
  }

  function validateDateOrDateTimeValue(dataType, value, errors, $defValField) {
    if (!value || !isValidValueForDataType(dataType, value)) {
      errors.push({field: $defValField, msg: t('import.columnMapping.error.dateFormat', dataType === 'datetime'
        ? DATE_FORMAT + ' hh:mm'
        : DATE_FORMAT)});
    }
  }

  function validateNewMappingDefVal(errors) {
    var $defValField = $('[name=newColumnMapping\\.defaultValue]');
    if (getColNameByDefValField($defValField) !== 'do-not-import-column' && !isCurrentTimeChecked()) {
      validateDefValField($defValField, errors);
    }
  }

  function validateDefaultValues(errors) {
    _.each($('#columnMappings tbody').find('[name^=default_value_]'), function (defValueField) {
      validateDefValField($(defValueField), errors);
    });
    validateNewMappingDefVal(errors);
  }

  function getValFieldColumnInfo($defValField) {
    return config.columns[getColNameByDefValField($defValField)];
  }
  
  function getColNameByDefValField($defValField) {
    return $defValField.attr('name') === 'newColumnMapping.defaultValue'
      ? $('[name=newColumnMapping\\.databaseColumn]').val()
      : $defValField.closest('tr').find('[name^=dbColumn_]').first().val();
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
    return moment(value, DATE_FORMAT.toUpperCase(), true).isValid();
  }
  
  function isValidDateTime(value) {
    var dateAndTime = value.split(' ');
    return isValidDate(dateAndTime[0]) &&  /^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/.test(dateAndTime[1]);
  }
  
  function isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }
  
  function isInteger(value) {
      return /^-?\d+$/.test(value);
  }
  
  function isCurrentTimeChecked() {
    var currentTimeCheckbox = $('[data-action=changeDateInput]');
    return currentTimeCheckbox.length && currentTimeCheckbox.is(":checked");
  }
});
