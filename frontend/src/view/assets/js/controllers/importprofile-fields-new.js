AGN.Lib.Controller.new('import-profile-fields-new', function () {
    const DONT_IMPORT_COL_OPTION = 'do-not-import-column';

    const Template = AGN.Lib.Template;
    const Form = AGN.Lib.Form;
    const CoreInitializer = AGN.Lib.CoreInitializer;

    var config;
    var $columnMappingsTable;
    var form;
    var columnMappingRowTemplate;
    var usedDbColumns = [];
    var usedNewMappingColName;
    var $uploadContainer;
    var $contentContainer;

    this.addDomInitializer('import-profile-fields', function () {
        config = this.config;
        $uploadContainer = $('#upload-file-container');
        $contentContainer = $('#content-container');

        renderMappings(config.columnMappings);

        form = Form.get(this.el);
        form.initFields();
    });

    function renderMappings(mappings) {
        $columnMappingsTable = $('#columnMappings tbody');
        $columnMappingsTable.html('');
        columnMappingRowTemplate = Template.prepare('column-mapping-table-row');

        for (var colIndex = 0; colIndex < mappings.length; colIndex++) {
            const mapping = mappings[colIndex];
            appendRowToColumnMappingTable(colIndex, mapping.id, mapping.fileColumn, mapping.databaseColumn, mapping.mandatory, mapping.encrypted, mapping.defaultValue);
        }

        renderNewMappingRow();

        removeUsedOptionsFromDbColumnsSelects();
        usedDbColumns = findUsedDbColumns();
    }

    function appendRowToColumnMappingTable(index, id, fileColumn, databaseColumn, mandatory, encrypted, defaultValue) {
        const $rowTemplate = columnMappingRowTemplate({
            index: index,
            id: id,
            fileColumn: fileColumn,
            databaseColumn: databaseColumn,
            mandatory: mandatory,
            encrypted: encrypted,
            defaultValue: defaultValue
        });

        $columnMappingsTable.append($rowTemplate);

        const $dbColumn = $('[data-mapping-row="' + index + '"]').find('[data-mapping-db-column]');
        CoreInitializer.run("select", $dbColumn);
    }

    this.addAction({click: 'upload'}, function () {
        const form = Form.get(this.el);
        const file = $('#uploadFile').prop('files')[0];

        $.ajax(config.urls.UPLOAD, {
            type: 'POST',
            dataType: 'json',
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            data: form.data()
        }).done(function (resp) {
            if (resp.success === true) {
                renderMappings(getMappingsForRender(resp.data));

                const $uploadedBlock = Template.dom(
                    'uploaded-file-selected',
                    {fileName: file.name}
                );

                $uploadContainer.hide();
                $uploadedBlock.insertAfter($uploadContainer);

                $contentContainer.removeClass('hidden');
            } else {
                AGN.Lib.JsonMessages(resp.popups);
            }
        })
    });

    function getMappingsForRender(uploadedMappings) {
        const tableMappings = collectMappingsDataFromTable(false);
        const nonDataFileColumns = tableMappings.filter(function (mapping) {
            return !mapping.fileColumn;
        });
        const fileColumnMappings = [];

        for (var i = 0; i < uploadedMappings.length; i++) {
            const newMapping = uploadedMappings[i];
            const existingMappingIndex = tableMappings.findIndex(function (el) {
                return el.fileColumn === newMapping.fileColumn;
            });

            if (existingMappingIndex === -1) {
                fileColumnMappings.push(newMapping);
            } else {
                fileColumnMappings.push(tableMappings[existingMappingIndex]);
            }
        }

        return [].concat(fileColumnMappings, nonDataFileColumns);
    }

    this.addAction({click: 'delete-file'}, function () {
        const jqxhr = $.post(AGN.url('/recipient/import/file/delete.action'));
        jqxhr.done(function () {
            $('#uploaded-file-container').remove();
            $('#uploadFile').val('');
            $uploadContainer.show();
        });
    });

    this.addAction({submission: 'save-mappings'}, function () {
        const form = Form.get(this.el);

        _.each(collectMappingsDataFromTable(true), function (mapping, index) {
            form.setValueOnce('columnsMappings[' + index + '].id', mapping.id);
            form.setValueOnce('columnsMappings[' + index + '].fileColumn', mapping.fileColumn);
            form.setValueOnce('columnsMappings[' + index + '].databaseColumn', mapping.databaseColumn);
            form.setValueOnce('columnsMappings[' + index + '].mandatory', mapping.mandatory);
            form.setValueOnce('columnsMappings[' + index + '].encrypted', mapping.encrypted);
            form.setValueOnce('columnsMappings[' + index + '].defaultValue', mapping.defaultValue);
        })

        form.submit();
    });

    function collectMappingsDataFromTable(includingNewMapping) {
        if (!$columnMappingsTable || !$columnMappingsTable.length) {
            return [];
        }

        const mappings = _.map($columnMappingsTable.find('[data-mapping-row]'), function (row) {
            const $row = $(row);
            return {
                id: getMappingValueInRowByCol($row, 'id'),
                fileColumn: getMappingValueInRowByCol($row, 'fileColumn'),
                databaseColumn: getMappingValueInRowByCol($row, 'db-column'),
                mandatory: getMappingValueInRowByCol($row, 'mandatory'),
                encrypted: getMappingValueInRowByCol($row, 'encrypted'),
                defaultValue: getMappingValueInRowByCol($row, 'defaultValue')
            };
        });

        if (includingNewMapping) {
            const newMapping = findNewMapping();

            if (newMapping.databaseColumn && newMapping.databaseColumn !== DONT_IMPORT_COL_OPTION) {
                mappings.push(newMapping);
            }
        }

        return mappings;
    }

    function getMappingValueInRowByCol($row, col) {
        const $input = $row.find('[data-mapping-' + col + ']');

        if ($input.is(':checkbox')) {
            return $input.is(":checked");
        }

        return $input.val();
    }

    this.addAction({change: 'change-new-col-name'}, function () {
        if (usedNewMappingColName && usedNewMappingColName !== DONT_IMPORT_COL_OPTION) {
            addOptionToDbColumnsSelects(usedNewMappingColName);
        }
        const colName = this.el.find(':selected').val();
        usedNewMappingColName = colName;
        removeNewOptionFromDbColumnsSelects(colName);
        updateNewMappingDefValInput(colName);
    });

    this.addAction({change: 'change-exist-col-name'}, function () {
        const previousSelectedColumn = findPreviousSelectedDbColumn();
        if (previousSelectedColumn) {
            addOptionToDbColumnsSelects(previousSelectedColumn);
            removeNewOptionFromDbColumnsSelects(this.el.val());
            usedDbColumns = findUsedDbColumns();
        }
    });

    this.addAction({click: 'delete-mapping'}, function () {
        deleteMapping(this.el);
    });

    this.addAction({click: 'bulk-delete'}, function () {
        _.each($('.column-checkbox:checkbox:checked'), function (el) {
            deleteMapping($(el));
        })
    });

    function deleteMapping($el) {
        const $mappingRow = $el.closest('tr');

        const newFreeOption = getMappingValueInRowByCol($mappingRow, 'db-column');
        $mappingRow.remove();

        if (newFreeOption && newFreeOption !== DONT_IMPORT_COL_OPTION) {
            addOptionToDbColumnsSelects(newFreeOption);
        }
    }

    this.addAction({click: 'add-mapping'}, function () {
        if (!form.valid({})) {
            form.handleErrors();
            return;
        }

        form.cleanErrors();

        const newMapping = findNewMapping();

        if (isDuplicatedMapping(newMapping)) {
            AGN.Lib.Messages(t("defaults.error"), t("export.columnMapping.error.duplicate"), 'alert');
            return;
        }

        this.el.closest('tr').remove();
        const colIndex = findLastMappingIndex() + 1;

        appendRowToColumnMappingTable(
            colIndex,
            newMapping.id,
            newMapping.fileColumn,
            newMapping.databaseColumn,
            newMapping.mandatory,
            newMapping.encrypted,
            newMapping.defaultValue
        );

        renderNewMappingRow();
        removeUsedOptionsFromDbColumnsSelects();
    });

    function isDuplicatedMapping(mappingToCheck) {
        if (mappingToCheck.databaseColumn === DONT_IMPORT_COL_OPTION) {
            return false;
        }

        const existingMappings = collectMappingsDataFromTable(false);
        return existingMappings.some(function (mapping) {
            return mapping.fileColumn === mappingToCheck.databaseColumn || mapping.databaseColumn === mappingToCheck.databaseColumn;
        });
    }

    function findNewMapping() {
        const $encryptedInput = $('#encrypted-new');
        const isEncrypted = $encryptedInput ? $encryptedInput.is(':checked') : false;

        return {
            id: 0,
            fileColumn: '',
            databaseColumn: $('#database-column-new').val(),
            mandatory: $('#mandatory-new').is(':checked'),
            encrypted: isEncrypted,
            defaultValue: prepareDefaultValueForNewMapping()
        };
    }

    function prepareDefaultValueForNewMapping() {
        if ($('[data-action=change-date-input]').is(":checked")) {
            const daysCount = parseInt($('#daysCount').val() || 0);
            const additionalDays = daysCount === 0 ? '' : (daysCount > 0 ? '+' + daysCount : daysCount);
            return 'CURRENT_TIMESTAMP' + additionalDays;
        }

        return $('[name=default-value-new]').val();
    }

    function findLastMappingIndex() {
        return $('[data-mapping-row]').last().data('mapping-row');
    }

    this.addAction({change: 'change-date-input'}, function () {
        $('#dateInput').toggle(!this.el.is(':checked'));
        $("#daysInput").toggle(this.el.is(':checked'));
        handleIntegerInput($('#daysCount'));
    });

    function handleIntegerInput($input) {
        $input.on('change.offset keyup.offset', function (e) {
            const value = $input.val();

            if (value) {
                const offset = parseInt(value);
                if (!isNaN(offset)) {
                    const newValue = (offset < 0 ? '' : '+') + offset;
                    if (newValue != value) {
                        $input.val(newValue);
                    }
                } else {
                    $input.val(value.replace(/[^\d+-]/g, ""));
                }
            } else if (e.type === 'change') {
                $input.val('+0');
            }
        });
    }

    function removeUsedOptionsFromDbColumnsSelects() {
        _.each($columnMappingsTable.find('[data-mapping-db-column]'), function (colNameSelect) {
            removeNewOptionFromDbColumnsSelects($(colNameSelect).val());
        });
    }

    function removeNewOptionFromDbColumnsSelects(option) {
        _.each($columnMappingsTable.find('[data-mapping-db-column]'), function (colNameSelect) {
            removeOptionFromDbColumnSelect($(colNameSelect), option);
        });
        removeOptionFromDbColumnSelect($('#database-column-new'), option);
    }

    function addOptionToDbColumnsSelects(option) {
        _.each($columnMappingsTable.find('[data-mapping-db-column]'), function (select) {
            addNewSelectOption($(select), option);
        });
        addNewSelectOption($('#database-column-new'), option);
    }

    function removeOptionFromDbColumnSelect($select, option) {
        if ($select.val() !== option && option !== DONT_IMPORT_COL_OPTION) {
            $select.find('[value="' + option + '"]').remove();
        }
    }

    function addNewSelectOption($select, option) {
        if ($select.val() !== option) {
            $select.append($('<option>', {
                value: option,
                text: option
            }));
        }
    }

    function performElemPostDraw($inputCell, type) {
        if (type === 'date') {
            AGN.runAll($inputCell);
        } else if (type === 'datetime') {
            form.initFields();
        }
    }

    function getColumnDefValInputType(colName) {
        const type = config.columns[colName].dataType.toLowerCase();
        switch (type) {
            case 'date':
            case 'datetime':
                return type;
            default:
                return 'text';
        }
    }

    function updateNewMappingDefValInput(colName) {
        const type = getColumnDefValInputType(colName);
        const $currentDefValInput = $('[name=default-value-new]');
        const val = $currentDefValInput.val();
        const $inputCell = $currentDefValInput.closest("td");
        if ($inputCell.children().first().data('type') !== type) {
            $inputCell.empty();
            $inputCell.append(getDefValInputByType(type, val))
            performElemPostDraw($inputCell, type);
        }
    }

    function getDefValInputByType(type, val) {
        switch (type) {
            case 'date':
            case 'datetime':
                return Template.text('date-def-val-input', {withTime: type === 'datetime', val: val});
            default:
                return Template.text('text-def-val-input');
        }
    }

    function renderNewMappingRow() {
        $columnMappingsTable.append(Template.text('new-mapping-row'));
        CoreInitializer.run("select", $('#database-column-new'));
    }

    function findPreviousSelectedDbColumn() {
        const currentUsedDbColumns = findUsedDbColumns();
        return usedDbColumns.filter(function (colName) {
            return currentUsedDbColumns.indexOf(colName) < 0;
        })[0];
    }

    function findUsedDbColumns() {
        const colNames = [];
        _.each($columnMappingsTable.find('[data-mapping-db-column]'), function (colNameSelect) {
            colNames.push($(colNameSelect).val());
        });
        return colNames;
    }
});
