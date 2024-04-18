(function(){
const Helpers = AGN.Lib.Helpers;
const Popover = AGN.Lib.Popover;

class Table {
  constructor($el, columns, data, options) {
    this.$el = $el;
    this.$el.html(this.#template);
    this.$tableControls = this.$el.find('.table-controls-wrapper');
    this.$el.data('_table', this);

    columns.forEach(function(column) {
      if (column.cellRenderer) {
        column.cellRenderer = AGN.Opt.TableCellRenderers[column.cellRenderer];
      }

      if (column.headerComponent) {
        column.headerComponent = AGN.Opt.TableHeaderComponents[column.headerComponent];
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
    columns[columns.length - 1].resizable = false;
    this.gridOptions = _.merge(this.#defaultGridOptions(columns), options || {});
    this.#setTableHeight();
    this.grid = new agGrid.Grid(this.$table.get(0), this.gridOptions);
    this.api = this.gridOptions.api;
    this.columnApi = this.gridOptions.columnApi;
    this.api.setRowData(this.gridOptions.singleNameTable ? data.map(name => ({name})) : data);
    this.redraw();
    this.#bindFilters();
    this.popoverTimer = undefined;
  };

  #defaultGridOptions(columns) {
    const self = this;
    return {
      autoSizePadding: 4,
      columnDefs: columns,
      accentedSort: true,
      colResizeDefault: 'shift',
      scrollbarWidth: 0,
      pagination: true,
      showRecordsCount: true,
      filtersDescription: {
        enabled: false,
        templateName: '',
        templateParams: {}
      },
      paginationPageSize: 100,
      rowSelection: 'multiple',
      unSortIcon: true,
      overlayNoRowsTemplate: `
        <div class="w-100 align-self-start">
         ${AGN.Lib.Template.text('notification-info', {message: t('defaults.table.empty')})}
        </div>
      `,
      onCellClicked: function(cell) {
        switch (cell.colDef.cellAction) {
          case 'select':
            cell.node.setSelected(!cell.node.isSelected());
            break;

          case 'goTo':
            const url = cell.data.show;
            if (url) {
              if (cell.event.ctrlKey) {
                window.open(url, "_blank");
              } else {
                window.location.href = url;
              }
            }
            break;
        }
      },
      onCellMouseOver: _.throttle(params => this.showCellPopoverIfTruncated(params), 50),
      onCellMouseOut: _.throttle(params => this.hideCellPopover(params), 50),
      suppressRowClickSelection: true,
      headerHeight: 30,
      suppressPaginationPanel: true,
      onPaginationChanged: function() {
        self.renderPagination();
      },
      defaultColDef: {
        filter: true,
        sortable: true,
        resizable: true,
        headerComponentParams: {
          template:`
          <div class="ag-cell-label-container" role="presentation">
            <!-- <span ref="eMenu" class="ag-header-icon ag-header-cell-menu-button"></span> filters are displaying in separate 'Filter' tile-->
            <div ref="eLabel" class="ag-header-cell-label" role="presentation">
              <span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon" ></span>
              <span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon" ></span>
              <span ref="eSortNone" class="ag-header-icon ag-sort-none-icon" ></span>
              <span ref="eText" class="ag-header-cell-text" role="columnheader"></span>
              <!-- <span ref="eFilter" class="ag-header-icon ag-filter-icon"></span> filters are displaying in separate 'Filter' tile-->
              <span ref="eSortOrder" class="ag-header-icon ag-sort-order" ></span>
            </div>
          </div>`
        },
        filterParams: {
          clearButton: true
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
        bulkSelectColumn: {
          headerName: '',
          editable: false,
          checkboxSelection: true,
          headerCheckboxSelection: true,
          headerComponent: AGN.Opt.TableHeaderComponents['NoLabelHeader'],
          resizable: false,
          suppressMenu: true,
          sortable: false,
          cellAction: 'select',
          suppressSizeToFit: true,
          cellStyle: {
            textAlign: 'center'
          }
        },
        deleteColumn: {
          headerName: '',
          editable: false,
          resizable: false,
          suppressMenu: true,
          sortable: false,
          cellAction: null,
          cellRenderer: AGN.Opt.TableCellRenderers['DeleteCellRenderer'],
          suppressSizeToFit: true,
          'button-tooltip': t('defaults.delete')
        },
        textCaseInsensitiveColumn: {
          comparator: AGN.Lib.TableCaseInsensitiveComparator
        },
        select: {
          comparator: AGN.Lib.TableCaseInsensitiveComparator
        }
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
      onFilterChanged: function() {
        const rowCount = self.api.getModel().rowsToDisplay.length;
        if (rowCount === 0) {
          self.api.showNoRowsOverlay();
        } else {
          self.api.hideOverlay();
        }
      },
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
        Popover.getOrCreate($cell, { trigger: 'manual', content }).show();
      }
    }, 400);
  }

  #isTruncatedCell(cellEl) {
    return cellEl && ($(cellEl).is(':truncated') || Helpers.containsTruncatedElements(cellEl))
  }

  #getClosestCell(el) {
    const $el = $(el);
    return $el.is('.ag-cell') ? $el : $el.closest('.ag-cell');
  }

  static dateCellRenderer(params, withTime = false) {
    const format = withTime ? window.adminDateTimeFormat : window.adminDateFormat;
    const wrapper = AGN.Lib.TableCellWrapper(params?.data?.show);
    const date = params?.value?.date || params.value;
    wrapper.innerHTML = date ? moment(date).format(format.replaceAll('d', 'D').replaceAll('y', 'Y')) : '';
    return wrapper;
  }

  static dateTimeCellRenderer(params) {
    return Table.dateCellRenderer(params, true);
  }

  get theme() {
    return 'bootstrap';
  }

  // max-height
  get #template() { // 30px - table controls
    return `<div class="ag-theme-${this.theme}" style="width: 100%; height: calc(100% - 30px)"></div>
            <div class="table-controls-wrapper"></div>`
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

  renderPagination() {
    const api = this.api;
    if (!api) {
      return
    }

    const currentPage = api.paginationGetCurrentPage() + 1;
    const totalPages = api.paginationGetTotalPages();
    const pageSelects = [currentPage];
    const paginationBottomTemplate = AGN.Lib.Template.prepare('table-controls');

    _.times(5, function(i) {
      if (currentPage - (i + 1) > 0) {
        pageSelects.unshift(currentPage - (i + 1))
      }

      if (currentPage + (i + 1) <= totalPages) {
        pageSelects.push(currentPage + (i + 1))
      }
    });

   const paginationData = {
      pagination: this.gridOptions.pagination,
      showRecordsCount: this.gridOptions.showRecordsCount,
      currentPage: currentPage,
      totalPages: totalPages,
      pageSize: api.paginationGetPageSize(),
      itemStart: (api.paginationGetPageSize() * (currentPage - 1)) + 1,
      itemEnd: currentPage == totalPages ? api.paginationGetRowCount() : api.paginationGetPageSize() * currentPage,
      itemTotal: this.gridOptions.pagination ? api.paginationGetRowCount() : api.getDisplayedRowCount(),
      pageSelects: pageSelects
    }

    const filtersDescription = this.gridOptions.filtersDescription;
    if (filtersDescription.enabled && filtersDescription.templateName) {
      this.$paginationTop.find('#filtersDescription').html(AGN.Lib.Template.text(filtersDescription.templateName), filtersDescription.templateParams);
    }
    this.$tableControls.html(paginationBottomTemplate(paginationData));
    AGN.Lib.CoreInitializer.run('select', this.$tableControls);
    this.api.redrawRows();
  };

  redraw() {
    const columnsToAutoResize = this.columnApi.getAllDisplayedColumns()
      .filter(function(column) {
        return column.getColDef().suppressSizeToFit === true;
      })
      .map(function(column) {
        return column.getColId();
      });

    if (columnsToAutoResize.length) {
      this.columnApi.autoSizeColumns(columnsToAutoResize);
    }

    this.api.sizeColumnsToFit();
    this.$el.trigger('table:redraw');
  };

  #bindFilters() {
    $('#filter-tile').on('enterdown', (e) => {
      e.preventDefault();
      this.applyFilter.bind(this)();
    });
    $('#apply-filter').on('click', this.applyFilter.bind(this));
    $('#reset-filter').on('apply-filter', this.applyFilter.bind(this));
  }

  applyFilter() {
    const columns = _.groupBy(this.columnApi.getAllDisplayedColumns(), column => column.getColDef().type);
    const numberFilterModel = this.#getFilterModelForTextCols(columns['numberColumn']);
    const numberRangeFilterModel = this.#getFilterModelForNumberRangeCols(columns['numberRangeColumn']);
    const textFilterModel = this.#getFilterModelForTextCols(columns['textCaseInsensitiveColumn']);
    const selectFilterModel = this.#getFilterModelForSelectCols(columns['select']);
    const emptyFilterModel = this.#getEmptyFilterModelForCols(_.union(columns['dateColumn'], columns['dateTimeColumn'], columns['customColumn'], columns['setColumn']));
    const otherFilterModel = this.#getFilterModelForTextCols(columns['undefined']);
    this.api.setFilterModel({
      ...numberRangeFilterModel,
      ...numberFilterModel,
      ...textFilterModel,
      ...selectFilterModel,
      ...otherFilterModel,
      ...emptyFilterModel,
    });
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

  static get($needle) {
    return Table.getWrapper($needle).data('_table');
  };

  static getWrapper($needle) {
    let $table = $($needle.data('table-body'));

    if ($table.length == 0) {
      $table = $needle.closest('.js-data-table-body');
    }

    return $table;
  };
}

AGN.Lib.Table = Table;
})();
