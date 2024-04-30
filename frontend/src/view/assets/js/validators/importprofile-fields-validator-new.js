AGN.Lib.CoreInitializer.new('import-profile-fields-validator', function () {
    const DATE_FORMAT = 'dd.mm.yyyy';
    const DONT_IMPORT_COL_OPTION = 'do-not-import-column';
    var config;

    AGN.Lib.Validator.new('import-profile-fields/form', {
        init: function () {
            const $config = $("#config\\:import-profile-fields-validator");
            config = $config.exists() ? $config.json() : {};
        },
        valid: function ($e, options) {
            return !this.errors($e, options).length;
        },
        errors: function ($e, options) {
            const errors = [];
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
        const value = getDefValFieldValue($defValField);
        if (value && getColNameByDefValField($defValField) !== DONT_IMPORT_COL_OPTION) {
            const columnInfo = getValFieldColumnInfo($defValField);
            const dataType = columnInfo.dataType.toLowerCase();

            if (columnInfo.column === 'email' && !AGN.Lib.Helpers.isValidEmail(value)) {
                errors.push({field: $defValField, msg: t('import.columnMapping.error.invalidEmail')});
            } else if (dataType === 'date' || dataType === 'datetime') {
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
            errors.push({
                field: $defValField, msg: t('import.columnMapping.error.dateFormat', dataType === 'datetime'
                    ? DATE_FORMAT + ' hh:mm'
                    : DATE_FORMAT)
            });
        }
    }

    function validateNewMappingDefVal(errors) {
        const $defValField = $('[name=default-value-new]');
        if (getColNameByDefValField($defValField) !== DONT_IMPORT_COL_OPTION && !isCurrentTimeChecked()) {
            validateDefValField($defValField, errors);
        }
    }

    function validateDefaultValues(errors) {
        _.each($('#columnMappings tbody').find('[data-mapping-defaultValue]'), function (defValueField) {
            validateDefValField($(defValueField), errors);
        });
        validateNewMappingDefVal(errors);
    }

    function getValFieldColumnInfo($defValField) {
        return config.columns[getColNameByDefValField($defValField)];
    }

    function getColNameByDefValField($defValField) {
        return $defValField.attr('name') === 'default-value-new'
            ? $('#database-column-new').val()
            : $defValField.closest('tr').find('[data-mapping-db-column]').first().val();
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
        const currentTimeCheckbox = $('[data-action=change-date-input]');
        return currentTimeCheckbox.length && currentTimeCheckbox.is(":checked");
    }
});
