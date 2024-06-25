AGN.Lib.Controller.new('manage-table-import', function() {
    const Form = AGN.Lib.Form;

    var NEW_COLUMN_FORMAT = '#new-default-column-format';
    var NEW_COLUMN_VALUE = '#new-default-column-value';
    var NEW_COLUMN_ENCRYPTED = '#new-default-column-encrypted';

    var MAPPING_ROW = '.manage-tables-mapping-row';

    var Template = AGN.Lib.Template;
    var config = null;

    var $columnMappingsTable;
    var columnMappingRowTemplate;

    this.addDomInitializer('manage-table-import', function() {
        config = this.config;
        renderMappings(config.mappings);
    });

    this.addAction({
        'click': 'add-default-column'
    }, function() {
        var currentCount = $(MAPPING_ROW).length;
        var formatInputValue = $(NEW_COLUMN_FORMAT).val(),
            defaultInputValue = $(NEW_COLUMN_VALUE).val(),
            encryptedInputChecked = !!$(NEW_COLUMN_ENCRYPTED).prop('checked'),
            valueType = $('#new-default-column-type').val();

        if("value" === valueType && !(defaultInputValue.startsWith("'") && defaultInputValue.endsWith("'"))) {
            defaultInputValue = "'" + defaultInputValue + "'";
        }


        var $rowTemplate = Template.dom('manage-tables-import-row', {
            index: currentCount,
            targetColumns: config.importColumns,
            format: formatInputValue,
            value: defaultInputValue,
            encrypted: encryptedInputChecked
        });
        $('#manage-tables-tbody').append($rowTemplate);

        $(NEW_COLUMN_FORMAT).val('');
        $(NEW_COLUMN_VALUE).val('');
        $(NEW_COLUMN_ENCRYPTED).prop('checked', false);
    });

    this.addAction({
        'click': 'delete-mapping-row'
    }, function () {
        var $el = this.el,
            $row = $el.parents(MAPPING_ROW),
            $tbody = $el.parents('tbody');
        $row.remove();

        recalculateMappingIndexes($tbody.find(MAPPING_ROW));
    });

    this.addAction({click: 'create-mapping'}, function() {
        const form = Form.get(this.el);

        $.ajax(config.urls.CREATE_MAPPINGS, {
            type: 'POST',
            dataType: 'json',
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            data: form.data()
        }).done(function (resp) {
            if (resp.success === true) {
                renderMappings(resp.data);
                AGN.Lib.JsonMessages(resp.popups);
            } else {
                AGN.Lib.JsonMessages(resp.popups);
            }
        })
    });

    this.addAction({click: 'import-data'}, function() {
        const url = this.el.data('url');
        const form = Form.get($('#form'));
        form.setActionOnce(url);

        submitForm(form);
    });
    function renderMappings(mappings) {
        $columnMappingsTable = $('#manage-tables-tbody');
        $columnMappingsTable.html('');
        columnMappingRowTemplate = Template.prepare('column-mapping-table-row');

        for (var colIndex = 0; colIndex < mappings.length; colIndex++) {
            const mapping = mappings[colIndex];
            appendRowToColumnMappingTable(colIndex, mapping.id, mapping.sourceColumn, mapping.targetColumn, mapping.mandatory, mapping.encrypted, mapping.format, mapping.defaultValue);
        }
    }

    function collectMappingsDataFromTable() {
        if (!$columnMappingsTable || !$columnMappingsTable.length) {
            return [];
        }

        return _.map($columnMappingsTable.find('[data-mapping-row]'), function (row) {
            const $row = $(row);
            return {
                sourceColumn: getMappingValueInRowByCol($row, 'fileColumn'),
                targetColumn: getMappingValueInRowByCol($row, 'db-column'),
                mandatory: getMappingValueInRowByCol($row, 'mandatory'),
                encrypted: getMappingValueInRowByCol($row, 'encrypted'),
                defaultValue: getMappingValueInRowByCol($row, 'defaultValue'),
                format: getMappingValueInRowByCol($row, 'format')
            };
        });
    }

    function getMappingValueInRowByCol($row, col) {
        const $input = $row.find('[data-mapping-' + col + ']');

        if ($input.is(':checkbox')) {
            return $input.is(":checked");
        }

        return $input.val();
    }

    function appendRowToColumnMappingTable(index, id, sourceColumn, targetColumn, mandatory, encrypted, format, defaultValue) {
        const $rowTemplate = columnMappingRowTemplate({
            index: index,
            id: id,
            fileColumn: sourceColumn,
            databaseColumn: targetColumn,
            mandatory: mandatory,
            encrypted: encrypted,
            format: format,
            defaultValue: defaultValue
        });

        $columnMappingsTable.append($rowTemplate);

        const $dbColumn = $('[data-mapping-row="' + index + '"]').find('[data-mapping-db-column]');
        AGN.Lib.CoreInitializer.run("select", $dbColumn);
    }

    this.addAction({submission: 'save'}, function () {
        const form = Form.get(this.el);
        submitForm(form);
    });

    function submitForm(form) {
        _.each(collectMappingsDataFromTable(), function (mapping, index) {
            form.setValueOnce('mappings[' + index + '].sourceColumn', mapping.sourceColumn);
            form.setValueOnce('mappings[' + index + '].targetColumn', mapping.targetColumn);
            form.setValueOnce('mappings[' + index + '].mandatory', mapping.mandatory);
            form.setValueOnce('mappings[' + index + '].encrypted', mapping.encrypted);
            form.setValueOnce('mappings[' + index + '].defaultValue', mapping.defaultValue);
            form.setValueOnce('mappings[' + index + '].format', mapping.format);
        })
        form.submit();
    }

    function recalculateMappingIndexes($rows) {
        var currentIndex = 0;

        $rows.each(function () {
            var $row = $(this);
            var $columns = $row.find('[name*="mappings["]'); // 'mappings*' and '_mappings*'
            $columns.each(function () {
                var $column = $(this);
                var attrValue = $column.attr('name');
                attrValue = attrValue.replace(/mappings\[\d+]/g, 'mappings[' + currentIndex + ']');
                $column.attr('name', attrValue);
            });
            currentIndex++;
        });
    }
});
