AGN.Lib.Controller.new('manage-table-import', function() {
    var NEW_COLUMN_FORMAT = '#new-default-column-format';
    var NEW_COLUMN_VALUE = '#new-default-column-value';
    var NEW_COLUMN_ENCRYPTED = '#new-default-column-encrypted';

    var MAPPING_ROW = '.manage-tables-mapping-row';

    var Template = AGN.Lib.Template;
    var config = null;

    this.addDomInitializer('manage-table-import', function() {
        config = this.config;
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
