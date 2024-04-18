AGN.Lib.CoreInitializer.new('input-table', function($scope = $(document)) {
  const Templates = AGN.Opt.Templates;
  const addBtnTemplate = _.template(Templates['plus-btn']);
  const deleteBtnTemplate = _.template(Templates['trash-btn']);

  class InputTable {
    constructor($container) {
      const $config = $container.find('script[data-config]');
      this.config = $config.exists() ? $config.json() : {readonly: false, data: []};
      this.readonly = this.config.readonly;
      this.$container = $container;
      this.rowTemplateMustache = _.template(this.rowTemplate);
      this.renderTable(this.config.data);
      this.addActions();
      this.$container.data('table', this);
    };

    static get(container) {
      return $(container).data('table');
    }

    get rowTemplate() {
      if (this._rowTemplate?.length) {
        return this._rowTemplate;
      }
      this._rowTemplate = this.$container.find('[data-row-template]').html();
      return this._rowTemplate;
    }

    get $rowTemplate() {
      return $(this.rowTemplate);
    }

    renderTable(data) {
      this.$table = $('<table>').addClass('input-table').append($('<tbody>'));
      if (this.readonly) {
        this.$table.addClass('input-table--readonly');
      }
      this.$container.prepend(this.$table);
      _.each(data, (rowData) => this.addRow(rowData));
      if (!this.readonly) {
        this.addEmptyRow();
      }
    }

    addActions() {
      const self = this;
      this.addAction({click: '[data-add-row]'}, this.addEmptyRow.bind(this));
      this.addAction({enterdown: 'input'}, function() {
        this.event.preventDefault();
        self.getNextInputToFocus(this.el).focus();
      });

      this.addAction({click: '[data-delete-row]'}, function () {
        this.el.closest('tr').remove();
        self.$container.trigger('input-table:row-removed');
      });
    }

    addAction(events, handler) {
      AGN.Lib.Action.new(events, handler, this.$table);
    }

    collect(includeLastRow = true) {
      let rows = this.$table.find('tr').toArray();
      if (!includeLastRow) {
        rows = rows.slice(0, -1);
      }

      return rows
        .filter(row => !this.isEmptyRow$($(row)))
        .map(row => this.getRowData($(row)));
    };

    getRowData($row) {
      const dataObj = {}
      $row.find(':input[data-name]').each((i, input) => dataObj[$(input).data('name')] = $(input).val());
      return dataObj;
    }

    addRow(rowData) {
      if (!this.isUniqueRow(rowData)) {
        return;
      }
      const button = this.#generateRowBtn(rowData);
      const $row = $(this.rowTemplateMustache(rowData));
      if (!this.readonly) {
        $row.append($('<td>').html(button));
      }
      this.$table.append($row);
      AGN.runAll($row);
      AGN.Lib.Form.get(this.$table).initFields();
    }

    getNextInputToFocus($current) {
      const $currentRow = $current.closest('tr');
      if (this.isEmptyRow$($currentRow)) { // if row is empty focus on first input back
        return $currentRow.find(':input[type="text"]:first');
      }
      const $inputsInRow = $currentRow.find(':input[type="text"]');
      const currentIndex = $inputsInRow.index($current);
      let $nextInput = $inputsInRow.eq(currentIndex + 1);
      if ($nextInput.length) {
        return $nextInput;
      }
      $nextInput = $currentRow.next().find(':input[type="text"]:first');
      if ($nextInput.length) {
        return $nextInput;
      }
      this.addEmptyRow();
      return this.$table.find('tr:last input[type="text"]:first')
    }

    #generateRowBtn(data) {
      return this.#isEmptyRow(data) ? this.generateAddBtn() : this.generateDeleteBtn();
    }

    generateAddBtn() {
      return addBtnTemplate({attrs: 'data-add-row'});
    }

    generateDeleteBtn() {
      return deleteBtnTemplate({attrs: 'data-delete-row'});
    }

    #isEmptyRow(data) {
      return this.getRowEntries(data).every(([, val]) => this.#isBlank(val));
    }

    getNames() {
      return this.$rowTemplate.find('[data-name]').toArray().map(input => $(input).data('name'));
    }

    getRowEntries(data) {
      const names = this.getNames();
      return Object.entries(data)
        .filter(([key]) => names.includes(key));
    }

    isEmptyRow$($row) {
      return _.every($row.find(':input:not([type="hidden"])'), input => {
        const $input = $(input);
        return $input.prop('disabled') || this.#isBlank($input.val())
      });
    }

    #isBlank(value) {
      return value === null || value === undefined || String(value).trim() === '';
    }

    addEmptyRow() {
      const $lastRow = this.$table.find('tr:last');
      if (!$lastRow.length || !this.isEmptyRow$($lastRow)) {
        this.replaceNewButtonWithDeleteButton();
        this.addRow(this.getNames().reduce((obj, key) => {
          obj[key] = '';
          return obj;
        }, {}));
      }
      this.focusOnLastRow();
    }

    focusOnLastRow() {
      this.$table.find('tr:last input:first').focus();
    }

    isUniqueRow(data) {
      if (this.#isEmptyRow(data)) {
        return true;
      }
      const rowEntries = this.getRowEntries(data);
      return !this.$table.find('tr').toArray().some(row => {
        return rowEntries.every(([name, val]) => $(row).find(`[data-name="${name}"]`).val() === val);
      });
    }

    replaceNewButtonWithDeleteButton() {
      const newBtn = this.$table.find('[data-add-row]');
      newBtn.after(this.generateDeleteBtn());
      newBtn.remove();
    }

    clean() {
      this.$table.find('tr').remove();
      this.addEmptyRow();
    }
  }
  AGN.Lib.InputTable = InputTable;

  _.each($scope.find('[data-input-table]'), function(el) {
    new InputTable($(el));
  });
});
