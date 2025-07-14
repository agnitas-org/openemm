AGN.Lib.Controller.new('import-profile', function () {

  const Template = AGN.Lib.Template;
  const Modal = AGN.Lib.Modal;
  const Messages = AGN.Lib.Messages;
  const Form = AGN.Lib.Form;
  const Select = AGN.Lib.Select;
  const CoreInitializer = AGN.Lib.CoreInitializer;
  const DB_COLUMN_ATTR_NAME = 'agn:db-column';

  let isMailingListWasDisabled;

  // gender variables
  let $genderMappingsBlock;
  let availableGenderIntValues;

  // column mappings variables
  let DO_NOT_IMPORT;
  let columnMappings;
  let profileFieldsColumns;
  let columnMappingRowTemplate;
  let $columnMappingsTable;
  let isClientForceSendingActive;

  this.addDomInitializer('import-profile-view', function () {
    isMailingListWasDisabled = $getRecipientMailinglists().prop('disabled');
    availableGenderIntValues = this.config.availableGenderIntValues;
    isClientForceSendingActive = this.config.isClientForceSendingActive;

    initGenderMappingTable(sortGenderMappingsByValue(this.config.genderMappings));
    updateMailingListsSelectState();
  });

  this.addAction({click: 'save'}, function () {
    if (isTextGendersUnique(getGenderTextValues(getGenderTableLastRow()))) {
      const form = Form.get(this.el);
      setGenderMappingsToForm(form);
      setColumnsMappingsToForm(form);
      form.submit();
    }
  });

  this.addAction({change: 'change-new-recipients-action'}, function () {
    updateMailingListsSelectState();
  });

  this.addAction({change: 'allMailinglists-toggle'}, function () {
    updateMailingListsSelectState();
  });

  //all mailinglists slider parent is visible just for ${allowedModesForAllMailinglists} import modes
  //if all Malinglists slider is activated and is not visible just deactivate it
  this.addAction({change: 'change-mode'}, function () {
    const $allMalinglistsSlider = $('#allMalinglistsCheckbox');
    if (!$allMalinglistsSlider.exists()) {
      return;
    }

    if (!$allMalinglistsSlider.parent().is(':visible')) {
      $allMalinglistsSlider.prop('checked', false).trigger('change');
    }
  });

  function updateMailingListsSelectState() {
    const $newRecipientsAction = $('#import_actionnewrecipients');
    const $allMalinglistsSlider = $('#allMalinglistsCheckbox');

    const actionSelected = $newRecipientsAction.exists() && $newRecipientsAction.val() > 0;
    const isAllMailinglistsSelected = $allMalinglistsSlider.exists() && $allMalinglistsSlider.is(':checked');

    const isMailingListsSelectDisabled = (actionSelected && !isClientForceSendingActive) || isMailingListWasDisabled || isAllMailinglistsSelected;
    const mailinglists = Select.get($getRecipientMailinglists());
    mailinglists.toggleDisabled(isMailingListsSelectDisabled);
    if (isMailingListsSelectDisabled) {
      mailinglists.clear();
    }

    if ($allMalinglistsSlider.exists()) {
      const shouldDisableAllMailinglistSlider = (actionSelected && !isClientForceSendingActive) || isMailingListWasDisabled;

      $allMalinglistsSlider.prop('disabled', shouldDisableAllMailinglistSlider);
      $allMalinglistsSlider.parent().toggleClass('hidden', shouldDisableAllMailinglistSlider);
    }
  }

  function $getRecipientMailinglists() {
    return $('#recipient-mailinglists');
  }

  // -------- Genders mappings block --------

  function setGenderMappingsToForm(form) {
    _.each(collectGenderMappings(), (textValuesStr, intValue) => {
      form.setValue(`genderMapping[${intValue}]`, textValuesStr);
    });
  }

  function collectGenderMappings() {
    const mappings = {};
    _.each($genderMappingsBlock.find('[data-gender-settings-row]'), row => {
      const $row = $(row);
      const textValuesStr = getGenderTextValuesStr($row);
      const intValue = getGenderIntValue($row);

      if (textValuesStr.length) {
        mappings[intValue] = !mappings.hasOwnProperty(intValue)
          ? textValuesStr
          : mappings[intValue] + ', ' + textValuesStr;
      }
    })
    return mappings;
  }

  function getGenderIntValue($row) {
    const $el = $row.find('[data-gender-int-value]');
    return $el.is('select') ? $el.val() : $el.text()[0];
  }

  this.addAction({click: 'delete-gender-mapping'}, function () {
    const $row = this.el.closest('[data-gender-settings-row]');

    if ($row.is(":first-child")) {
      const $nextRow = $row.next('[data-gender-settings-row]');
      $nextRow.find('.form-label').removeClass('hidden');
    }

    $row.remove();
  });

  this.addAction({click: 'add-gender-mapping', enterdown: 'gender-enterdown'}, function () {
    this.event.preventDefault();
    if (isLastRowCanBeAddedToGenderTable()) {
      replaceNewGenderMappingButtonWithDeleteButton();
      appendRowToGenderMappingTable('', '');
      getGenderTableLastRow().find('[data-gender-text-value]').focus();
    }
  });

  function isLastRowCanBeAddedToGenderTable() {
    const textValues = getGenderTextValues(getGenderTableLastRow());
    if (textValues.size < 1) {
      Messages.alert('import.gender.error.empty');
      return false;
    }
    return isTextGendersUnique(textValues);
  }

  function getGenderTableLastRow() {
    return $genderMappingsBlock.find('[data-gender-settings-row]:last-child');
  }

  function isTextGendersUnique(genders) {
    const rows = $genderMappingsBlock.find('[data-gender-settings-row]').toArray();
    for (let i = 0; i < rows.length - 1; i++) {
      if (_.intersection(Array.from(genders), Array.from(getGenderTextValues($(rows[i])))).length) {
        Messages.alert('import.gender.error.duplicate');
        return false;
      }
    }
    return true;
  }

  function getGenderTextValues($row) {
    const textValuesStr = getGenderTextValuesStr($row);
    const result = textValuesStr.length ? textValuesStr.split(',') : [];
    return new Set(_.without(_.map(result, _.trim), ''));
  }

  function getGenderTextValuesStr($row) {
    const $el = $row.find('[data-gender-text-value]');
    if ($el.is('input')) {
      return $el.val().trim();
    }

    return $el.text().trim();
  }

  function replaceNewGenderMappingButtonWithDeleteButton() {
    const newBtn = $genderMappingsBlock.find('[data-action="add-gender-mapping"]');
    newBtn.after(`
        <a href='#' class='btn btn-danger btn-icon' data-action='delete-gender-mapping'>
            <i class='icon icon-trash-alt'></i>
        </a>
    `);
    newBtn.remove();
  }

  const sortGenderMappingsByValue = (mappings) => {
    if (!mappings) {
      mappings = '';
    }

    const result = Object.keys(mappings).map(key => [key, mappings[key]]);
    result.sort((a, b) => a[1] - b[1]);

    return result;
  };

  function initGenderMappingTable(mappings) {
    $genderMappingsBlock = $('#gender-mappings-block');
    _.each(mappings, mapping => {
      appendRowToGenderMappingTable(mapping[0], mapping[1]);
    });
    appendRowToGenderMappingTable('', '');
  }

  function appendRowToGenderMappingTable(textValues, intValue) {
    const mappingsCount = $genderMappingsBlock.find('[data-gender-settings-row]').length;

    const genders = _.map(availableGenderIntValues,
      (value) => ({value, selected: intValue == value, title: t(`import.gender.short.${value}`)}));

    $genderMappingsBlock.append(Template.text('gender-settings-table-row', {intValue, textValues, mappingsCount, genders}));
    CoreInitializer.run("select", $genderMappingsBlock.find('[data-gender-int-value]:last-child'));
  }

  // ----------------------------------------

  // -------- Columns mappings block --------

  let columnMappingsConfig;

  function setColumnsMappingsToForm(form) {
    if (isAutoMappingsEnabled()) {
      return;
    }

    _.each(collectColumnMappingsDataFromTable(true), (mapping, index) => {
      form.setValueOnce('columnsMappings[' + index + '].id', mapping.id);
      form.setValueOnce('columnsMappings[' + index + '].fileColumn', mapping.fileColumn);
      form.setValueOnce('columnsMappings[' + index + '].databaseColumn', mapping.databaseColumn);
      form.setValueOnce('columnsMappings[' + index + '].mandatory', mapping.mandatory);
      form.setValueOnce('columnsMappings[' + index + '].encrypted', mapping.encrypted);
      form.setValueOnce('columnsMappings[' + index + '].defaultValue', mapping.defaultValue);
    });
  }

  this.addAction({change: 'change-auto-mapping'}, function () {
    updateColumnMappingsControlsState();
  });

  this.addDomInitializer('import-profile-mappings', function () {
    columnMappingsConfig = this.config;
    if (columnMappingsConfig) {
      columnMappings = columnMappingsConfig.columnMappings;
      DO_NOT_IMPORT = columnMappingsConfig.doNotImportValue;
      profileFieldsColumns = columnMappingsConfig.columns;

      $columnMappingsTable = $('#column-mappings-block');
      renderColumnMappings(columnMappings);
      updateColumnMappingsControlsState();
    }
  });

  function updateColumnMappingsControlsState() {
    const $manageFieldsTile = $('#manage-fields-tile');
    const autoMappingsEnabled = isAutoMappingsEnabled();

    $manageFieldsTile.find('.tile-controls').toggleClass('hidden', autoMappingsEnabled);
    $('[data-mapping-db-column]').prop('disabled', autoMappingsEnabled);
    $('[data-mappings-actions]').toggleClass('hidden', autoMappingsEnabled);
    $('#upload-mappings-btn').prop('disabled', autoMappingsEnabled);
    $('#uploadFile').prop('disabled', autoMappingsEnabled);
  }

  function isAutoMappingsEnabled() {
    return $('#import_automapping').is(":checked");
  }

  this.addAction({click: 'upload-column-mappings'}, function () {
    const form = Form.get(this.el);

    $.ajax(AGN.url('/import-profile/mappings/read.action'), {
      type: 'POST',
      dataType: 'json',
      enctype: 'multipart/form-data',
      processData: false,
      contentType: false,
      data: form.data()
    }).done(resp => {
      if (resp.success === true) {
        renderColumnMappings(getColumnMappingsForRender(resp.data));
      } else {
        AGN.Lib.JsonMessages(resp.popups);
      }
    })
  });

  function getColumnMappingsForRender(uploadedMappings) {
    const tableMappings = collectColumnMappingsDataFromTable();
    const nonDataFileColumns = tableMappings.filter(mapping => !mapping.fileColumn);

    const fileColumnMappings = uploadedMappings.map(newMapping => {
      const existingMapping = tableMappings.find(el => el.fileColumn === newMapping.fileColumn);
      return existingMapping !== undefined ? existingMapping : newMapping;
    });

    return [].concat(fileColumnMappings, nonDataFileColumns);
  }

  this.addAction({click: 'bulk-mappings-delete'}, function () {
    _.each($('[data-mapping-checkbox]:checked'), el => deleteColumnMapping($(el)));
    updateBulkActionsBlock();
  });

  this.addAction({change: 'change-database-column'}, function () {
    const previousSelectedColumn = this.el.data(DB_COLUMN_ATTR_NAME);
    const selectedColumn = this.el.val();

    addDbColumnOptionToSelects(previousSelectedColumn);
    removeDbColumnFromSelects(selectedColumn);

    this.el.data(DB_COLUMN_ATTR_NAME, selectedColumn);

    if (this.el.closest('[data-mapping-row]').is('[data-mapping-new]')) {
      updateNewColumnMappingDefValInput(selectedColumn);
    }
  });

  this.addAction({change: 'set-today-date'}, function () {
    $('#dateInput').toggleClass('hidden', this.el.is(':checked'));
    $("#daysInput").toggleClass('hidden', !this.el.is(':checked'));

    const $daysCount = $('#daysCount');
    $daysCount.on('change.offset keyup.offset', function (e) {
      const value = $daysCount.val();

      if (value) {
        const offset = parseInt(value);
        if (!isNaN(offset)) {
          const newValue = (offset < 0 ? '' : '+') + offset;
          if (newValue != value) {
            $daysCount.val(newValue);
          }
        } else {
          $daysCount.val(value.replace(/[^\d+-]/g, ""));
        }
      } else if (e.type === 'change') {
        $daysCount.val('+0');
      }
    });
  });

  function updateNewColumnMappingDefValInput(dbColumn) {
    if (!$columnMappingsTable.parents('table').exists()) {
      return;
    }

    const type = getColumnDefValInputType(dbColumn);

    const $currentDefValInput = $columnMappingsTable.find('[data-mapping-defaultValue]').last();
    const val = $currentDefValInput.val();
    const $inputCell = $currentDefValInput.closest('td');

    if ($inputCell.children().first().data('type') !== type) {
      $inputCell.empty();
      $inputCell.append(getDefValInputByType(type, val))
      performElemPostDraw($inputCell, type);
    }
  }

  function performElemPostDraw($inputCell, type) {
    if (type === 'date') {
      AGN.runAll($inputCell);
    } else if (type === 'datetime') {
      _.each($inputCell.find('[data-field]'), function (field) {
        AGN.Lib.Field.create($(field));
      });
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

  function getColumnDefValInputType(dbColumn) {
    const type = profileFieldsColumns[dbColumn].simpleDataType.toLowerCase();
    switch (type) {
      case 'date':
      case 'datetime':
        return type;
      default:
        return 'text';
    }
  }

  this.addAction({click: 'show-column-mappings'}, function () {
    const mappings = collectColumnMappingsDataFromTable();

    const $modal = Modal.fromTemplate('column-mappings-modal');
    $modal.on('modal:close', () => {
      $columnMappingsTable = $('#column-mappings-block');
      columnMappingRowTemplate = Template.prepare('column-mapping-row');
    });

    $columnMappingsTable = $('#extended-column-mappings-block');
    renderColumnMappings(mappings, true);
    CoreInitializer.run('table-cols-resizer', $columnMappingsTable.closest('table'));
  });

  this.addAction({click: 'save-extended-mappings'}, function () {
    if (!validateColumnMappings(this.el)) {
      return;
    }

    const mappings = collectColumnMappingsDataFromTable(true);
    $columnMappingsTable = $('#column-mappings-block');
    renderColumnMappings(mappings);

    Modal.getInstance(this.el).hide();
  });

  this.addAction({click: 'add-column-mapping'}, function () {
    if (this.el.closest('table').exists() && !validateColumnMappings(this.el)) {
      return;
    }

    const $row = this.el.closest('[data-mapping-row]');
    const newMapping = getNewColumnMappingData($row);

    if (isDuplicatedColumnMapping(newMapping)) {
      Messages.alert(t("export.columnMapping.error.duplicate"));
      return;
    }

    $row.remove();

    newMapping.index = findLastColumnMappingIndex() + 1;
    appendRowToColumnMappingTable(newMapping);
    renderNewColumnMappingRow();
    removeUsedDbColumnsFromSelects();
  });

  function validateColumnMappings($el) {
    const form = Form.get($el);
    form.validatorOptions = {$mappingsBlock: $columnMappingsTable};
    if (!form.valid()) {
      form.handleErrors();
      return false;
    }

    form.cleanFieldFeedback();
    return true;
  }

  function isDuplicatedColumnMapping(mappingToCheck) {
    if (mappingToCheck.databaseColumn === DO_NOT_IMPORT) {
      return false;
    }

    const existingMappings = collectColumnMappingsDataFromTable();
    return existingMappings.some(mapping => {
      return mapping.fileColumn === mappingToCheck.databaseColumn || mapping.databaseColumn === mappingToCheck.databaseColumn;
    });
  }

  function collectColumnMappingsDataFromTable(includingNewMapping = false) {
    if (!$columnMappingsTable || !$columnMappingsTable.exists()) {
      return [];
    }

    const mappings = _.map($columnMappingsTable.find('[data-mapping-row]'), function (row) {
      const $row = $(row);
      return {
        id: getColumnMappingValueInRowByCol($row, 'id'),
        fileColumn: getColumnMappingValueInRowByCol($row, 'fileColumn'),
        databaseColumn: getColumnMappingValueInRowByCol($row, 'db-column'),
        mandatory: getColumnMappingValueInRowByCol($row, 'mandatory'),
        encrypted: getColumnMappingValueInRowByCol($row, 'encrypted'),
        defaultValue: getColumnMappingValueInRowByCol($row, 'defaultValue')
      };
    });

    const lastNewMapping = mappings[mappings.length - 1];

    if (!includingNewMapping || DO_NOT_IMPORT === lastNewMapping.databaseColumn) {
      mappings.pop();
    }

    return mappings;
  }

  function getNewColumnMappingData($row) {
    const $mandatoryInput = $row.find('[data-mapping-mandatory]');
    const $encryptedInput = $row.find('[data-mapping-encrypted]');

    return {
      id: 0,
      fileColumn: '',
      databaseColumn: $row.find('[data-mapping-db-column]').val(),
      mandatory: $mandatoryInput.exists() ? $mandatoryInput.is(':checked') : false,
      encrypted: $encryptedInput.exists() ? $encryptedInput.is(':checked') : false,
      defaultValue: prepareDefaultValueForNewColumnMapping($row)
    };
  }

  function prepareDefaultValueForNewColumnMapping($row) {
    if ($row.find('[data-action=set-today-date]').is(":checked")) {
      const daysCount = parseInt($('#daysCount').val() || 0);
      const additionalDays = daysCount === 0
        ? ''
        : (daysCount > 0 ? `+${daysCount}` : daysCount);

      return `CURRENT_TIMESTAMP${additionalDays}`;
    }

    return $row.find('[data-mapping-defaultValue]').val();
  }

  this.addAction({click: 'delete-column-mapping'}, function () {
    deleteColumnMapping(this.el);
    updateBulkActionsBlock();
  });

  function updateBulkActionsBlock() {
    $('.table-wrapper').trigger('table:updateBulkActions');
  }

  function deleteColumnMapping($el) {
    const $mappingRow = $el.closest('[data-mapping-row]');
    const dbColumn = getColumnMappingValueInRowByCol($mappingRow, 'db-column');

    if ($mappingRow.is(":first-child")) {
      const $nextRow = $mappingRow.next('[data-mapping-row]');
      $nextRow.find('.form-label').removeClass('hidden');
    }

    $mappingRow.remove();
    addDbColumnOptionToSelects(dbColumn);
  }

  function addDbColumnOptionToSelects(option) {
    if (option && option !== DO_NOT_IMPORT) {
      _.each($columnMappingsTable.find('[data-mapping-db-column]'), select => {
        addNewSelectOption($(select), option);
      });
    }
  }

  function addNewSelectOption($select, option) {
    if ($select.val() !== option) {
      Select.get($select).addOption(option);
    }
  }

  function getColumnMappingValueInRowByCol($row, col) {
    const $input = $row.find(`[data-mapping-${col}]`);

    if ($input.is(':checkbox')) {
      return $input.is(":checked");
    }

    return $input.val();
  }

  function renderColumnMappings(mappings, extended = false) {
    if (!$columnMappingsTable.exists()) {
      return;
    }

    $columnMappingsTable.empty();
    columnMappingRowTemplate = Template.prepare(extended ? 'column-mapping-row-extended' : 'column-mapping-row');

    for (let colIndex = 0; colIndex < mappings.length; colIndex++) {
      const mapping = mappings[colIndex];
      mapping.index = colIndex;
      appendRowToColumnMappingTable(mapping);
    }

    renderNewColumnMappingRow();
    removeUsedDbColumnsFromSelects();

    $columnMappingsTable.find('[data-mapping-db-column]').each(function () {
      const $this = $(this);
      $this.data(DB_COLUMN_ATTR_NAME, $this.val())
    });
  }

  function renderNewColumnMappingRow() {
    appendRowToColumnMappingTable({
      index: -1,
      id: 0,
      fileColumn: '',
      databaseColumn: '',
      mandatory: false,
      encrypted: false,
      defaultValue: ''
    });
  }

  function removeUsedDbColumnsFromSelects() {
    $columnMappingsTable.find('[data-mapping-db-column]').each(function () {
      removeDbColumnFromSelects($(this).val());
    });
  }

  function removeDbColumnFromSelects(option) {
    $columnMappingsTable.find('[data-mapping-db-column]').each(function () {
      const $el = $(this);

      if ($el.val() !== option && option !== DO_NOT_IMPORT) {
        $el.find(`[value="${option}"]`).remove();
      }
    });
  }

  function findLastColumnMappingIndex() {
    return $('[data-mapping-row]').last().data('mapping-row');
  }

  function appendRowToColumnMappingTable(mapping) {
    mapping.isFirstMapping = $columnMappingsTable.find('[data-mapping-row]').length == 0;
    const $rowTemplate = $(columnMappingRowTemplate(mapping));
    $columnMappingsTable.append($rowTemplate);

    AGN.runAll($rowTemplate);
  }

  // ----------------------------------------
});
