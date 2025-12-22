(() => {

  const Helpers = AGN.Lib.Helpers;
  const Popover = AGN.Lib.Popover;
  const CoreInitializer = AGN.Lib.CoreInitializer;

  class Table {

    static DATA_KEY = 'agn:js-table';

    constructor($el, columns, data, options) {
      this.$el = $el;
      this.$el.append(this.#template);
      this.$el.data(Table.DATA_KEY, this);

      columns.forEach(column => {
        if (column.cellRenderer) {
          column.cellRenderer = AGN.Opt.TableCellRenderers[column.cellRenderer];
        }

        if (column.filter) {
          column.filter = AGN.Opt.Table['filters'][column.filter];
        }

        if (column.comparator) {
          column.comparator = AGN.Opt.Table['comparators'][column.comparator];
        }

        if (column.hasOwnProperty('suppressSizeToFit') && !data.length) {
          column.suppressSizeToFit = false;
        }
      });

      if (columns.some(col => col.type === 'colorBadge')) {
        const colors = Helpers.getStatColorsNames();
        _.each(data, (row, i) => row.color = colors[i % colors.length]);
      }

      columns[columns.length - 1].resizable = false;
      this.gridOptions = _.merge(this.#defaultGridOptions(columns), options || {});

      const actionsColumn = this.gridOptions.columnDefs.find(c => c.type === "tableActionsColumn");
      if (!actionsColumn || actionsColumn.hide) {
        this.gridOptions.selectionColumnDef.hide = true;
      }

      this.#setTableHeight();
      this.api = agGrid.createGrid(this.$table.get(0), this.gridOptions);
      this.#setAvailableActionsData(data);
      this.api.setGridOption('rowData', this.gridOptions.singleNameTable ? data.map(name => ({name})) : data)
      this.redraw();
      this.#bindFilters();
      this.popoverTimer = undefined;
      window.setTimeout(() => CoreInitializer.run('scrollable', $el), 100)
    };

    #defaultGridOptions(columns) {
      return {
        autoSizePadding: 4,
        columnDefs: columns,
        accentedSort: true,
        colResizeDefault: 'shift',
        scrollbarWidth: 0,
        pagination: true,
        showRecordsCount: true,
        paginationPageSize: 100,
        rowSelection: {
          mode: 'multiRow',
          selectAll: 'currentPage'
        },
        theme: 'legacy',
        loading: false,
        overlayNoRowsTemplate: `
          <div class="w-100 align-self-start">
           ${AGN.Lib.Template.text('notification-info', {message: t('defaults.table.empty')})}
          </div>
        `,
        onCellClicked: cell => {
          if (cell.colDef.colId === 'ag-Grid-SelectionColumn') {
            cell.node.setSelected(!cell.node.isSelected());
          }
        },
        onColumnResized: event => {
          if (event.finished) {
            AGN.Lib.CoreInitializer.run('truncated-text-popover', this.$el);
          }
        },
        onCellMouseOver: _.throttle(params => this.showCellPopoverIfTruncated(params), 50),
        onCellMouseOut: _.throttle(params => this.hideCellPopover(params), 50),
        onSelectionChanged: () => this.updateBulkActions(),
        onRowDataUpdated: () => {
          this.api.forEachNode(node => this.#setAvailableActionsData([node.data]));
          this.updateBulkActions();
          this.#updateOverlay();
        },
        headerHeight: 33,
        suppressPaginationPanel: true,
        onPaginationChanged: () => {
          if (this.api) {
            this.#renderPagination();
            this.$el.css('--ag-rows-count', this.#getDisplayedRowsCount());
          }
        },
        defaultColDef: {
          filter: true,
          sortable: true,
          resizable: true,
          autoHeight: true,
          wrapText: true,
          unSortIcon: true,
          suppressMovable: true,
          sortingOrder: ['asc', 'desc'],
          suppressHeaderFilterButton: true,
          suppressHeaderMenuButton: true,
          suppressHeaderContextMenu: true,
          headerComponentParams: {
            template: `
              <div class="ag-cell-label-container" role="presentation">
                <div data-ref="eLabel" class="ag-header-cell-label" role="presentation">
                    <span data-ref="eSortOrder" class="ag-header-icon ag-header-label-icon ag-sort-order" aria-hidden="true"></span>
                    <span data-ref="eSortAsc" class="ag-header-icon ag-header-label-icon ag-sort-ascending-icon" aria-hidden="true"></span>
                    <span data-ref="eSortDesc" class="ag-header-icon ag-header-label-icon ag-sort-descending-icon" aria-hidden="true"></span>
                    <span data-ref="eSortNone" class="ag-header-icon ag-header-label-icon ag-sort-none-icon" aria-hidden="true"></span>
                    <span data-ref="eText" class="ag-header-cell-text"></span>
                </div>
              </div>
            `
          },
          filterParams: {
            clearButton: true
          }
        },
        selectionColumnDef: {
          editable: false,
          resizable: false,
          sortable: false,
          suppressSizeToFit: true,
          cellStyle: {
            textAlign: 'center'
          }
        },
        columnTypes: {
          numberColumn: {
            cellDataType: "text", // 'text' - to enable 'contains' search while filter applied
            comparator: AGN.Lib.TableNumberComparator
          },
          numberRangeColumn: {
            filter: 'agNumberColumnFilter',
            filterParams: {
              inRangeInclusive: true
            }
          },
          dateColumn: {
            filter: DateRangeFilter,
            cellRenderer: Table.dateCellRenderer,
          },
          dateTimeColumn: {
            filter: DateRangeFilter,
            cellRenderer: Table.dateTimeCellRenderer,
          },
          setColumn: {
            filter: SetFilter,
          },
          tableActionsColumn: {
            headerName: '',
            editable: false,
            resizable: false,
            sortable: false,
            filter: SwitchFilter,
            cellRenderer: AGN.Opt.TableCellRenderers['TableActionsCellRenderer'],
            suppressSizeToFit: true
          },
          textCaseInsensitiveColumn: {
            editable: false,
            comparator: AGN.Lib.TableCaseInsensitiveComparator,
            filter: WildcardFilter
          },
          select: {
            comparator: AGN.Lib.TableCaseInsensitiveComparator
          },
          colorBadge: {cellRenderer: Table.colorBadgeCellRenderer}
        },
        icons: {
          menu: '<i class="icon icon-filter"/>',
          filter: '<i class="icon icon-filter"/>',
          columns: '<i class="icon icon-handshake-o"/>',
          sortAscending: '<i class="icon icon-sort-down"/>',
          sortDescending: '<i class="icon icon-sort-up"/>',
          checkboxChecked: '<i class="icon icon-check-square"/>',
          checkboxUnchecked: '<i class="icon icon-square"/>',
          checkboxIndeterminate: '<i class="icon icon-square-o"/>',
          checkboxCheckedReadOnly: '<i class="icon icon-check-square-o"/>',
          checkboxUncheckedReadOnly: '<i class="icon icon-square-o"/>',
          checkboxIndeterminateReadOnly: '<i class="icon icon-square-o"/>',
          sortUnSort: '<i class="icon icon-sort"/>'
        },
        localeText: window.I18n.tables,
        onFilterChanged: () => {
          this.#updateOverlay();
          this.api.forEachNode(node => this.#setAvailableActionsData([node.data]));
          this.api.deselectAll();
          this.updateBulkActions();
        },
        isRestoreMode: () => !!this.isRestoreMode,
        getRowClass: () => this.isRestoreMode ? 'ag-row-no-hover' : ''
      }
    }

    hideCellPopover(params) {
      clearTimeout(this.popoverTimer);
      const $cell = this.#getClosestCell(params.event.target);
      Popover.get($cell)?.hide()
    }

    showCellPopoverIfTruncated(params) {
      this.popoverTimer = setTimeout(() => {
        const $cell = this.#getClosestCell(params.event.target);
        if (this.#isTruncatedCell($cell[0])) {
          const content = $(Helpers.retrieveTextElement($cell[0])).text();
          Popover.getOrCreate($cell, {trigger: 'manual', content}).show();
        }
      }, 400);
    }

    #isTruncatedCell(cellEl) {
      return cellEl && ($(cellEl).is(':truncated') || Helpers.containsTruncatedElements(cellEl))
    }

    #updateOverlay() {
      const rowCount = this.api.getDisplayedRowCount();
      if (rowCount === 0) {
        this.api.showNoRowsOverlay();
      } else {
        this.api.hideOverlay();
      }
    }

    #getClosestCell(el) {
      const $el = $(el);
      return $el.is('.ag-cell') ? $el : $el.closest('.ag-cell');
    }

    static dateCellRenderer(params, withTime = false) {
      const format = withTime ? window.adminDateTimeFormat : window.adminDateFormat;
      const wrapper = Table.createCellWrapper(params);

      const contentDiv = document.createElement("div");
      contentDiv.classList.add("text-truncate-table");
      wrapper.appendChild(contentDiv);

      const date = params?.value?.date || params.value;
      contentDiv.innerHTML = date ? moment(date).format(format.replaceAll('d', 'D').replaceAll('y', 'Y')) : '';
      return wrapper;
    }

    static dateTimeCellRenderer(params) {
      return Table.dateCellRenderer(params, true);
    }

    static colorBadgeCellRenderer(params) {
      const wrapper = Table.createCellWrapper(params);

      const $cell = $('<div>')
        .addClass("text-truncate-table")
        .html(`
        <div class="d-flex gap-2">
            <span class="square-badge badge--stat-${params.data.color}"></span>
            <span class="text-truncate-table">${params.value}</span>
        </div>`);
      return $(wrapper).append($cell)[0];
    }

    static createCellWrapper(params) {
      const api = params?.api;
      return AGN.Lib.TableCellWrapper(
        api?.getGridOption('viewLinkTemplate'),
        api?.getGridOption('viewInModal'),
        api?.getGridOption('isRestoreMode')(),
        params?.data
      );
    }

    get theme() {
      return 'bootstrap';
    }

    // max-height
    get #template() {
      return `<div class="ag-theme-${this.theme}" style="width: 100%; height: 100%"></div>`
    }

    get $table() {
      if (!this._$table) {
        this._$table = this.$el.find(`.ag-theme-${this.theme}`);
      }
      return this._$table;
    }

    // max-height cant be used when grid option domLayout='autoHeight' applied
    // https://www.ag-grid.com/javascript-data-grid/grid-size/#grid-auto-height
    #setTableHeight() {
      if (!this.gridOptions.showRecordsCount && !this.gridOptions.pagination) {
        this.$table.height(this.$el.parent().height());
      }
    }

    #renderPagination() {
      const currentPage = this.api.paginationGetCurrentPage() + 1;
      const totalPages = this.api.paginationGetTotalPages();

      const paginationData = {
        pagination: this.gridOptions.pagination,
        showRecordsCount: this.gridOptions.showRecordsCount,
        currentPage: currentPage,
        totalPages: totalPages,
        pageSize: this.api.paginationGetPageSize(),
        pageSelects: this.#generatePageNumbers(currentPage, totalPages)
      }

      this.#updateDisplayedEntriesCount();

      const $tableFooter = AGN.Lib.Template.dom('table-footer', paginationData);
      const $existingFooter = this.$el.find('.table-wrapper__footer');

      if ($existingFooter.exists()) {
        $existingFooter.replaceWith($tableFooter);
      } else {
        this.$el.append($tableFooter);
      }
      CoreInitializer.run('select', $tableFooter);
      this.api.redrawRows();
    };

    #generatePageNumbers(currentPage, totalPages) {
      const maxPagesCount = parseInt(this.$el.css('--table-max-pages-count'));

      let startPage, endPage;

      if (totalPages <= maxPagesCount) {
        startPage = 1;
        endPage = totalPages;
      } else {
        if (currentPage <= Math.ceil(maxPagesCount / 2)) {
          startPage = 1;
          endPage = maxPagesCount;
        } else if (currentPage + Math.floor(maxPagesCount / 2) > totalPages) {
          startPage = totalPages - maxPagesCount + 1;
          endPage = totalPages;
        } else {
          startPage = currentPage - Math.floor(maxPagesCount / 2);
          endPage = currentPage + Math.floor(maxPagesCount / 2) - 1;
        }
      }

      const pages = [];
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }

      return pages;
    }

    #updateDisplayedEntriesCount() {
      const api = this.api;

      const totalRowsCount = this.#getTotalRowsCount();
      const filteredRowsCount = this.gridOptions.pagination ? api.paginationGetRowCount() : api.getDisplayedRowCount();

      const $entriesCount = this.$el.find('.table-wrapper__entries-label');
      const $entriesCountText = $entriesCount.children().first();

      if (totalRowsCount > filteredRowsCount) {
        $entriesCountText.text(`${filteredRowsCount} / ${totalRowsCount}`);
      } else {
        $entriesCountText.text(filteredRowsCount);
      }

      $entriesCount.removeClass('hidden');
    }

    #getTotalRowsCount() {
      let totalRowsCount = 0;

      this.api.forEachNode(node => {
        totalRowsCount++;
      });

      return totalRowsCount;
    }

    #getDisplayedRowsCount() {
      if (!this.gridOptions.pagination) {
        return this.api.getDisplayedRowCount();
      }

      const total = this.api.paginationGetRowCount();
      const page = this.api.paginationGetCurrentPage();
      const size = this.api.paginationGetPageSize();

      return Math.min(total - page * size, size);
    }

    redraw() {
      const columnsToAutoResize = this.api.getAllDisplayedColumns()
        .filter(column => column.getColDef().suppressSizeToFit === true)
        .map(column => column.getColId());

      if (columnsToAutoResize.length) {
        this.api.autoSizeColumns(columnsToAutoResize);
      }

      this.api.sizeColumnsToFit();
      AGN.Lib.CoreInitializer.run('truncated-text-popover', this.$el);
      this.$el.trigger('table:redraw');
    };

    #bindFilters() {
      $('#filter-tile').on('enterdown', e => {
        e.preventDefault();
        this.applyFilter.bind(this)();
      });
      $('#apply-filter').on('click', this.applyFilter.bind(this));
      $('#reset-filter').on('apply-filter', this.applyFilter.bind(this));
    }

    applyFilter() {
      const columns = _.groupBy(this.api.getColumns(), column => column.getColDef().type);
      const numberFilterModel = this.#getFilterModelForTextCols(columns['numberColumn']);
      const numberRangeFilterModel = this.#getFilterModelForNumberRangeCols(columns['numberRangeColumn']);
      const selectFilterModel = this.#getFilterModelForSelectCols(columns['select']);
      const emptyFilterModel = this.#getEmptyFilterModelForCols(_.union(columns['dateColumn'], columns['dateTimeColumn'], columns['customColumn'], columns['setColumn'], columns['tableActionsColumn'], columns['textCaseInsensitiveColumn']));
      const otherFilterModel = this.#getFilterModelForTextCols(columns['undefined']);
      this.api.setFilterModel({
        ...numberRangeFilterModel,
        ...numberFilterModel,
        ...selectFilterModel,
        ...otherFilterModel,
        ...emptyFilterModel,
      });
      this.isRestoreMode = $('#deleted-filter').prop('checked');
    }

    #getFilterModelForNumberRangeCols(numberRangeColumns = []) {
      return numberRangeColumns.reduce((acc, column) => {
        const colId = column.getColId();
        const min = $(`#${colId}-min-filter`).val() || Number.MIN_SAFE_INTEGER;
        const max = $(`#${colId}-max-filter`).val() || Number.MAX_SAFE_INTEGER;

        acc[colId] = {
          filterType: 'number',
          type: 'inRange',
          filter: min,
          filterTo: max,
        };
        return acc;
      }, {});
    }

    #getFilterModelForTextCols(textColumns = []) {
      return textColumns.reduce((acc, column) => {
        const colId = column.getColId();
        const text = $(`#${colId}-filter`.replaceAll('.', '\\.')).val()?.toLowerCase();
        acc[colId] = {type: 'contains', filter: text};
        return acc;
      }, {});
    }

    #getFilterModelForSelectCols(selectColumns = []) {
      return selectColumns.reduce((acc, column) => {
        const colId = column.getColId();
        const text = $(`#${colId}-filter`).val()?.toLowerCase();
        acc[colId] = {type: 'equals', filter: text};
        return acc;
      }, {});
    }

    #getEmptyFilterModelForCols(columns = []) {
      return columns.reduce((acc, column) => {
        acc[column.getColId()] = {};
        return acc;
      }, {});
    }

    findRowByElement($el) {
      return this.api.getRowNode($el.closest('.ag-row').attr('row-id'));
    }

    updateBulkActions() {
      const selectedRows = this.api.getSelectedRows();
      const actions = selectedRows.flatMap(row => row.rowActions);
      this.$el.trigger('table:updateBulkActions', [selectedRows.length, actions]);
    }

    #setAvailableActionsData(rows) {
      const buttonsCfg = this.api.getColumnDefs().find(def => def.type === 'tableActionsColumn')?.buttons;
      if (!buttonsCfg) {
        return;
      }
      rows.forEach(row => row.rowActions = buttonsCfg
        .filter(cfg => this.allowedToShowRowAction(cfg, row))
        .map(cfg => cfg.name));
    }

    allowedToShowRowAction(cfg, row) {
      if (this.isRestoreMode) {
        return cfg.name === 'restore';
      }
      if (cfg.hide === true) {
        return false;
      }
      const actionCondition = AGN.Opt.TableActionsConditions[cfg.name];
      return !actionCondition || actionCondition(row);
    }

    static get($needle) {
      return Table.getWrapper($needle).data(this.DATA_KEY);
    };

    static getWrapper($needle) {
      return $needle.closest('.table-wrapper');
    };
  }

  AGN.Lib.Table = Table;

})();
