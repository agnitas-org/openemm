(() => {

  /** Representation of <th> element that located inside the DOM */
  class TableColumn {

    static DATA_ATTR_NAME = 'agn:table-manager-col';

    constructor($th) {
      this.$th = $th;
      this.$td = this.#$findCorrespondingTd();
      this._name = $th.data('table-column');

      this.#wrapPlainTextInSpan();
      this.#addControls();

      this.$th.data(TableColumn.DATA_ATTR_NAME, this);
    }

    static get($el) {
      return $el.data(TableColumn.DATA_ATTR_NAME);
    }

    get name() {
      return this._name;
    }

    get permanent() {
      return !this.name;
    }

    get text() {
      return this.$th.children().first().text().trim();
    }

    toggleMode() {
      this.$controls.toggleClass('hidden');
      this.$controls.prev().toggleClass('hidden');
    }

    toggle(hide = false) {
      this.$th.toggleClass('hidden', hide);
      this.$td.toggleClass('hidden', hide);
    }

    isHidden() {
      return this.$th.hasClass('hidden');
    }

    #addControls() {
      const $controls = AGN.Lib.Template.dom('deletable-table-column',
        {text: this.text, permanent: this.permanent});

      this.$th.append($controls);
      this.$controls = $controls;
    }

    #$findCorrespondingTd() {
      const index = this.$th.index() + 1;
      return this.$th.closest('table').find(`td:nth-child(${index})`);
    }

    #wrapPlainTextInSpan() {
      if (this.$th.contents().length === 1) {
        $(this.$th.contents()[0]).wrap('<span></span>');
      }
    }
  }

  class TableColumnManager {

    static DATA_ATTR_NAME = 'agn:table-column-manager';
    static SELECTOR = '[data-table-column-manager]'
    static MAX_SELECTED_COLUMNS = 8;

    constructor($el) {
      this.$el = $el;

      const $config = $el.find('[data-table-column-manager-config]');
      this.config = $config.exists() ? $config.json() : {};
      this.$btn = $el.find('[data-manage-table-columns]');
      this.$columnPickerCell = $el.find('th.columns-picker');
      this._editModeEnabled = false;

      this.columns = $el.find('[data-table-column]').map(function () {
        return new TableColumn($(this));
      }).get();

      this.#addColumnPicker();
      this.#addUiHandlers();

      if (this.config.editMode) {
        this.toEditMode();
        this.$columnPicker.find('> .icon-btn').trigger('click');
      }

      this.#hideExcessColumns();

      $el.data(TableColumnManager.DATA_ATTR_NAME, this);
    }

    static get($needle) {
      return $needle.closest(TableColumnManager.SELECTOR)
        .data(TableColumnManager.DATA_ATTR_NAME);
    }

    toEditMode() {
      this.#toggleMode();
      this._columnsBeforeEdit = this.#findVisibleColumns();
    }

    applyChanges() {
      this.#toggleMode();
      this.$el.trigger($.Event('table-column-manager:apply'), {columns: this.#collectSelectedColumns()});
    }

    discardChanges() {
      if (!this.isInEditMode()) {
        return;
      }

      this.columns.forEach(c => c.toggle(!this._columnsBeforeEdit.includes(c)));
      this.#toggleMode();
    }

    removeColumn($th) {
      const column = TableColumn.get($th);
      this.#removeColumn(column);
      AGN.Lib.CoreInitializer.run(['table-cols-resizer', 'table'], this.$el);
    }

    isInEditMode() {
      return this._editModeEnabled;
    }

    #hideExcessColumns() {
      const visibleColumns = this.#findVisibleColumns();

      if (visibleColumns.length <= TableColumnManager.MAX_SELECTED_COLUMNS) {
        return;
      }

      const excessColumnsCount = visibleColumns.length - TableColumnManager.MAX_SELECTED_COLUMNS;
      const selectedColumns = this.#collectSelectedColumns();

      for (let i = 0; i < excessColumnsCount; i++) {
        const tableColumn = this.#findColumnByName(selectedColumns[i])
        this.#removeColumn(tableColumn);
      }
    }

    #removeColumn(tableColumn) {
      AGN.Lib.Select.get(this.$columnSelect).unselectValue(tableColumn.name);
      tableColumn.toggle(true);
    }

    #collectSelectableColumns() {
      return this.columns.filter(col => !col.permanent);
    }

    #collectSelectedColumns() {
      return this.#collectSelectableColumns().filter(col => !col.isHidden())
        .map(col => col.name);
    }

    #toggleMode() {
      this.columns.forEach(col => col.toggleMode());
      this._editModeEnabled = !this._editModeEnabled;
      this.$el.toggleClass('is-edit-mode');
      this.$columnPicker.toggleClass('hidden');
      this.#updateToggleModeBtn();
      AGN.Lib.CoreInitializer.run(['table-cols-resizer', 'table'], this.$el);
    }

    #addColumnPicker() {
      let columnsData = this.config.columns;
      if (!columnsData) {
        columnsData = this.#collectSelectableColumns().map(col => {
          return {text: col.text, name: col.name, selected: !col.isHidden()};
        });
      }

      const $picker = AGN.Lib.Template.dom('table-column-picker', {columns: columnsData});

      this.$columnPickerCell.append($picker);

      this.$columnPicker = $picker;
      this.$columnSelect = $picker.find('select');

      AGN.runAll($picker);
    }

    #addUiHandlers() {
      let $dropdownMenu;

      this.$columnPicker.on('show.bs.dropdown', function () {
        $dropdownMenu = $(this).find('.dropdown-menu');
        $('body').append($dropdownMenu.detach());
      });

      this.$columnPicker.on('hidden.bs.dropdown', () => {
        this.$columnPicker.append($dropdownMenu.detach());
      });

      this.$columnSelect.on('select2:selecting', e => {
        if (!this.#isPossibleToAddColumn()) {
          e.preventDefault();
        }
      });

      this.$columnSelect.on('select2:select', e => {
        const columnName = e.params.data.id;
        const tableColumn = this.#findColumnByName(columnName);

        if (tableColumn) {
          tableColumn.toggle();
          AGN.Lib.CoreInitializer.run(['table-cols-resizer', 'table'], this.$el);
        } else {
          const columns = this.#collectSelectedColumns().concat(columnName);
          this.$el.trigger($.Event('table-column-manager:add'), {columns});

          $dropdownMenu.remove();
        }
      });
    }

    #isPossibleToAddColumn() {
      if (this.#findVisibleColumns().length === TableColumnManager.MAX_SELECTED_COLUMNS) {
        AGN.Lib.Messages.warn('defaults.table.maxColumnsSelected');
        return false;
      }

      return true;
    }

    #findVisibleColumns() {
      return this.columns.filter(col => !col.isHidden());
    }

    #findColumnByName(name) {
      return this.columns.find(col => col.name === name);
    }

    #updateToggleModeBtn() {
      const $btnText = this.$btn.find('span');

      if (this.isInEditMode()) {
        $btnText.text(t('defaults.table.saveColumns'));
      } else {
        $btnText.text(t('defaults.table.editColumns'));
      }
    }
  }

  AGN.Lib.TableColumnManager = TableColumnManager;

})();
