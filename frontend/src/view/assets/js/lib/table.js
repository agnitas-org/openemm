(function(){

  var Table,
      template;

  template =
  '<div class="pagination-top"></div>' +
  '<div class="ag-theme-bootstrap" style="height: calc(100% - 106px); width: 100%"></div>' +
  '<div class="pagination-bottom"></div>';

  Table = function($el, columns, data, options) {
    var self = this;

    this.$el = $el;
    this.$el.html(template);
    this.$el.data('_table', self);
    this.$paginationTop = this.$el.find('.pagination-top');
    this.$paginationBottom = this.$el.find('.pagination-bottom');

    _.each(columns, function(column) {
      if (column.cellRenderer) {
        column.cellRenderer = AGN.Opt.TableCellRenderers[column.cellRenderer];
      }
    });

    this.gridOptions = _.merge({
      columnDefs: columns,
      enableSorting: true,
      enableFilter: true,
      enableColResize: true,
      pagination: true,
      paginationPageSize: 100,
      rowSelection: 'multiple',
      onCellClicked: function(cell) {
        if (cell.colDef.cellAction == 'select') {
          cell.node.setSelected(!cell.node.isSelected());
          return;
        }

        if (cell.colDef.cellAction == 'goTo') {
          if (cell.data.show) {
            window.location.href = cell.data.show;
          }
          return;
        }
      },
      suppressRowClickSelection: true,
      headerHeight: 41,
      rowHeight: 34,
      suppressPaginationPanel: true,
      onPaginationChanged: function() {
        self.renderPagination();
      },
      defaultColDef: {
        headerComponentParams: {
          template:
          '<div class="ag-cell-label-container" role="presentation">' +
          '  <span ref="eMenu" class="ag-header-icon ag-header-cell-menu-button"></span>' +
          '  <div ref="eLabel" class="ag-header-cell-label" role="presentation">' +
          '    <span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon" ></span>' +
          '    <span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon" ></span>' +
          '    <span ref="eText" class="ag-header-cell-text" role="columnheader"></span>' +
          '    <span ref="eFilter" class="ag-header-icon ag-filter-icon"></span>' +
          '    <span ref="eSortOrder" class="ag-header-icon ag-sort-order" ></span>' +
          '    <span ref="eSortNone" class="ag-header-icon ag-sort-none-icon" ></span>' +
          '  </div>' +
          '</div>'
        },
        filterParams: {
          clearButton: true,
        }
      },
      columnTypes: {
        numberColumn: {
          width: 83,
          filter: 'agNumberColumnFilter',
          comparator: AGN.Lib.TableNumberComparator,
        },
        dateColumn: {
          filter: AGN.Lib.TableDateFilter,
        },
        setColumn: {
          filter: AGN.Lib.TableSetFilter
        }
      },
      icons: {
        menu: '<i class="icon icon-filter"/>',
        filter: '<i class="icon icon-filter"/>',
        columns: '<i class="icon icon-handshake-o"/>',
        sortAscending: '<i class="icon icon-sort-asc"/>',
        sortDescending: '<i class="icon icon-sort-desc"/>',
        checkboxChecked: '<i class="icon icon-check-square-o"/>',
        checkboxUnchecked: '<i class="icon icon-square-o"/>',
        checkboxIndeterminate: '<i class="icon icon-square-o"/>',
        checkboxCheckedReadOnly: '<i class="icon icon-check-square-o"/>',
        checkboxUncheckedReadOnly: '<i class="icon icon-square-o"/>',
        checkboxIndeterminateReadOnly: '<i class="icon icon-square-o"/>'
      },
      localeText: window.I18n.tables
    }, options || {});


    this.grid = new agGrid.Grid(this.$el.find('.ag-theme-bootstrap').get(0), this.gridOptions);
    this.api = this.gridOptions.api;
    this.api.setRowData(data);
    this.api.sizeColumnsToFit();
  };

  Table.prototype.renderPagination = function() {
    var api = this.api,
        currentPage,
        totalPages,
        pageSelects,
        paginationData,
        paginationTopTemplate = AGN.Lib.Template.prepare('table-controls-top');
        paginationBottomTemplate = AGN.Lib.Template.prepare('table-controls-bottom');

    if (!api) { return }

    currentPage = api.paginationGetCurrentPage() + 1;
    totalPages  = api.paginationGetTotalPages();
    pageSelects = [currentPage];

    _.times(5, function(i) {
      if (currentPage - (i + 1) > 0) {
        pageSelects.unshift(currentPage - (i + 1))
      }

      if (currentPage + (i + 1) <= totalPages) {
        pageSelects.push(currentPage + (i + 1))
      }
    });

    paginationData = {
      pagination: this.gridOptions.pagination,
      currentPage: currentPage,
      totalPages: totalPages,
      itemStart: (api.paginationGetPageSize() * (currentPage - 1)) + 1,
      itemEnd: currentPage == totalPages ? api.paginationGetRowCount() : api.paginationGetPageSize() * currentPage,
      itemTotal: api.paginationGetRowCount(),
      pageSelects: pageSelects
    };

    this.$paginationTop.html(paginationTopTemplate(paginationData));
    this.$paginationBottom.html(paginationBottomTemplate(paginationData));
    this.api.redrawRows();
  };

  Table.prototype.redraw = function() {
    this.api.sizeColumnsToFit();
  };

  Table.get = function($needle) {
    var $table,
        tableObj;

    $table = Table.getWrapper($needle);
    tableObj = $table.data('_table');

    return tableObj;
  };

  Table.getWrapper = function($needle) {
    var $table;

    $table = $($needle.data('table-body'));

    if ( $table.length == 0 ) {
      $table = $needle.closest('.js-data-table-body');
    }

    return $table;
  };


  AGN.Lib.Table = Table;
})();
