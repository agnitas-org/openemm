(function(){

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
    });

    this.gridOptions = _.merge(this.#defaultGridOptions(columns), options || {});
    this.#setTableHeight();
    this.grid = new agGrid.Grid(this.$table.get(0), this.gridOptions);
    this.api = this.gridOptions.api;
    this.columnApi = this.gridOptions.columnApi;
    this.api.setRowData(this.gridOptions.singleNameTable ? data.map(name => ({name})) : data);
    this.redraw();
  };

  #defaultGridOptions(columns) {
    const self = this;
    return {
      autoSizePadding: 4,
      columnDefs: columns,
      accentedSort: true,
      enableColResize: true,
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

      onCellMouseOver: function(cell) {
        if (cell.colDef.textInPopoverIfTruncated === true) {
          var target = cell.event.target,
            isTruncated = target.clientWidth < target.scrollWidth;
          if (isTruncated) {
            Table.#getPopoverForCell(cell).show();
          }
        }
      },

      onCellMouseOut: function(cell) {
        if (cell.colDef.textInPopoverIfTruncated === true) {
          const popover = AGN.Lib.Popover.get($(cell.event.target));
          if (popover) {
            popover.hide();
          }
        }
      },
      suppressRowClickSelection: true,
      headerHeight: 40 - 1, // - bottom border
      suppressPaginationPanel: true,
      onPaginationChanged: function() {
        self.renderPagination();
      },
      defaultColDef: {
        filter: true,
        sortable: true,
        headerComponentParams: {
          template:`
          <div class="ag-cell-label-container" role="presentation">
            <span ref="eMenu" class="ag-header-icon ag-header-cell-menu-button"></span>
            <div ref="eLabel" class="ag-header-cell-label" role="presentation">
              <span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon" ></span>
              <span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon" ></span>
              <span ref="eSortNone" class="ag-header-icon ag-sort-none-icon" ></span>
              <span ref="eText" class="ag-header-cell-text" role="columnheader"></span>
              <span ref="eFilter" class="ag-header-icon ag-filter-icon"></span>
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
          width: 83,
          filter: 'agNumberColumnFilter',
          comparator: AGN.Lib.TableNumberComparator
        },
        dateColumn: {
          filter: AGN.Lib.TableDateFilter
        },
        setColumn: {
          filter: AGN.Lib.TableSetFilter
        },
        bulkSelectColumn: {
          headerName: '',
          editable: false,
          checkboxSelection: true,
          headerCheckboxSelection: true,
          headerComponent: AGN.Opt.TableHeaderComponents['NoLabelHeader'],
          suppressResize: true,
          suppressMenu: true,
          suppressSorting: true,
          cellAction: 'select',
          suppressSizeToFit: true,
          cellStyle: {
            textAlign: 'center'
          }
        },
        deleteColumn: {
          headerName: '',
          editable: false,
          suppressResize: true,
          suppressMenu: true,
          suppressSorting: true,
          cellAction: null,
          cellRenderer: AGN.Opt.TableCellRenderers['DeleteCellRenderer'],
          suppressSizeToFit: true,
          'button-tooltip': t('defaults.delete')
        },
        textCaseInsensitiveColumn: {
          comparator: AGN.Lib.TableCaseInsensitiveComparator
        }
      },
      icons: {
        menu: '<i class="icon icon-filter"/>',
        filter: '<i class="icon icon-filter"/>',
        columns: '<i class="icon icon-handshake-o"/>',
        sortAscending: '<i class="icon icon-sort-down"/>',
        sortDescending: '<i class="icon icon-sort-up"/>',
        checkboxChecked: '<i class="icon icon-check-square-o"/>',
        checkboxUnchecked: '<i class="icon icon-square-o"/>',
        checkboxIndeterminate: '<i class="icon icon-square-o"/>',
        checkboxCheckedReadOnly: '<i class="icon icon-check-square-o"/>',
        checkboxUncheckedReadOnly: '<i class="icon icon-square-o"/>',
        checkboxIndeterminateReadOnly: '<i class="icon icon-square-o"/>',
        sortUnSort: '<i class="icon icon-sort"/>'
      },
      localeText: window.I18n.tables
    }
  }

  get theme() {
    return 'bootstrap';
  }

  // max-height
  get #template() { // 40px - table controls
    return `<div class="ag-theme-${this.theme}" style="width: 100%; height: calc(100% - 40px)"></div>
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
  };

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

  static #getPopoverForCell(cell) {
    const $cellTarget = $(cell.event.target);
    const popover = AGN.Lib.Popover.get($cellTarget);
    if (!popover) {
      return AGN.Lib.Popover.new($cellTarget, {
        trigger: 'manual',
        container: 'body',
        content: cell.value
      });
    }
    return popover;
  }
}

AGN.Lib.Table = Table;
})();
