;(() => {

  class ResizableColumn {

    static MIN_WIDTH = 40;

    constructor(th) {
      this.el = th;
      this.$el = $(this.el);

      this.$siblings = this.$el.siblings().filter(function () {
        return $(this).is(':visible');
      });

      this.$el.addClass('resizable');
      this._separatorWidth = parseInt(this.$el.css('--table-separator-width'));
    }

    get separatorWidth() {
      return this._separatorWidth;
    }

    startResize(eventX, tableWidth) {
      this.$nextSibling = this.$el.nextAll(':visible')
        .filter((i, sibling) => !$(sibling).css('--table-not-resizable-cell'))
        .first();
      this.$prevSibling = this.$el.prevAll(':visible')
        .filter((i, sibling) => !$(sibling).css('--table-not-resizable-cell'))
        .first();

      this.siblingsWidths = this.#getSiblingsWidthsMap();

      const startWidth = this.el.offsetWidth;

      this._resizeHandler = event => {
        const newWidth = Math.max(ResizableColumn.MIN_WIDTH, startWidth + (event.clientX - eventX));

        if (newWidth < startWidth) {
          this.#decreaseWidth(startWidth, newWidth);
        } else {
          this.#increaseWidth(startWidth, newWidth, tableWidth);
        }
      };

      document.addEventListener("mousemove", this._resizeHandler);
    }

    stopResize() {
      document.removeEventListener("mousemove", this._resizeHandler);
    }

    destroy() {
      this.el.style.width = '';
      this.$el.removeClass('resizable');
    }

    #decreaseWidth(startWidth, newWidth) {
      const $sibling = this.$nextSibling.exists()
        ? this.$nextSibling
        : this.$prevSibling;

      $sibling.get(0).style.width = `${this.#getStartSiblingWidth($sibling) + (startWidth - newWidth)}px`;
      this.el.style.width = `${newWidth}px`;
    }

    #increaseWidth(startWidth, newWidth, tableWidth) {
      if (this.$nextSibling.exists()) {
        let newSiblingWidth = this.#getStartSiblingWidth(this.$nextSibling) - (newWidth - startWidth);
        if (newSiblingWidth < ResizableColumn.MIN_WIDTH) {
          newWidth -= (ResizableColumn.MIN_WIDTH - newSiblingWidth);
          newSiblingWidth = ResizableColumn.MIN_WIDTH;
        }

        this.$nextSibling.get(0).style.width = `${newSiblingWidth}px`;
        this.el.style.width = `${newWidth}px`;

        return;
      }

      if (this.#getTotalSiblingsWidth() + newWidth <= tableWidth) {
        this.el.style.width = `${newWidth}px`;
      }
    }

    #getSiblingsWidthsMap() {
      const siblingsWidths = {};
      this.$siblings.each((i, sibling) => {
        siblingsWidths[$(sibling).index()] = sibling.clientWidth;
      });

      return siblingsWidths;
    }

    #getStartSiblingWidth($sibling) {
      return this.siblingsWidths[$sibling.index()];
    }

    #getTotalSiblingsWidth() {
      return Object.values(this.siblingsWidths)
        .reduce((acc, val) => acc + val, 0);
    }
  }

  class TableColumnsResizer {

    static DATA_KEY = 'agn:table-cols-resizer';

    constructor($table) {
      this.table = $table.get(0);
      this.$table = $table;
      this.$cols = $table.find('th:visible');
      this.tableId = $table.attr('id');

      this.$fitContentCols = this.$cols.filter((i, col) => $(col).hasClass('fit-content'));
      this.$nonResizableCols = this.$cols.filter((i, col) => $(col).css('--table-not-resizable-cell') === 'true');
      this.$resizableCols = this.#detectResizableColumns();

      $table.data(TableColumnsResizer.DATA_KEY, this);
    }

    resize() {
      this._resizableColumns = [];

      document.fonts.ready.then(() => {
        this.$resizableCols.each((i, col) => this.#initResizableCol(col));

        this.#moveColWidthsToStyles(this.$fitContentCols);
        this.#moveColWidthsToStyles(this.$nonResizableCols);

        this.$table.addClass('table-layout-fixed');
        this.#restoreWidths();

        AGN.Lib.CoreInitializer.run('truncated-text-popover', this.$table.parent());
      });
    }

    #detectResizableColumns() {
      let $resizableCols = this.$cols.not(this.$nonResizableCols).not(this.$cols.last());

      const $lastResizable = $resizableCols.last();
      const $nextCol = $lastResizable.next();

      if ($nextCol.exists() && $nextCol.is(this.$nonResizableCols)) {
        $resizableCols = $resizableCols.not($lastResizable);
      }

      return $resizableCols;
    }

    #initResizableCol(col) {
      const resizableCol = new ResizableColumn(col);
      this._resizableColumns.push(resizableCol);

      $(col).on('mousedown', event => {
        if (event.offsetX >= col.offsetWidth - resizableCol.separatorWidth) {
          this.#moveResizableColsWidthsToStyles();
          resizableCol.startResize(event.clientX, this.table.clientWidth);
          this._mouseupHandler = () => this.#stopResizing(resizableCol);
          document.addEventListener("mouseup", this._mouseupHandler);
        }
      });
    }

    #moveResizableColsWidthsToStyles() {
      if (!this._widthsWasSetToStyles) {
        this.#moveColWidthsToStyles(this.$resizableCols);
        this._widthsWasSetToStyles = true;
      }
    }

    #moveColWidthsToStyles($cols) {
      $cols.each((i, col) => {
        col.style.width = getComputedStyle(col).width;
      });
    }

    static get($table) {
      return $table.data(this.DATA_KEY);
    }

    destroy() {
      this._resizableColumns.forEach(c => c.destroy());
      this.$resizableCols.off('mousedown');
      this.$nonResizableCols.each((i, col) => col.style.width = '')
      this.$table.removeClass('table-layout-fixed');
      this.$table.data(TableColumnsResizer.DATA_KEY, null);
    }

    #stopResizing(resizableColumn) {
      resizableColumn.stopResize();
      document.removeEventListener("mouseup", this._mouseupHandler);

      this.#storeWidths();

      AGN.Lib.CoreInitializer.run('truncated-text-popover', this.$table.parent());
    }

    #storeWidths() {
      if (!this.tableId) {
        return;
      }

      AGN.Lib.Storage.set(this.#getStorageKey(), {
        width: this.table.clientWidth,
        colsCount: this.$cols.length,
        widths: this.#getWidthsMap()
      });
    }

    #restoreWidths() {
      if (!this.tableId) {
        return;
      }

      const settings = AGN.Lib.Storage.get(this.#getStorageKey());
      if (!settings || this.table.clientWidth !== settings.width || this.$cols.length !== settings.colsCount) {
        return;
      }

      if (!_.isEmpty(_.xor(Object.keys(settings.widths), this.#getResizableColsNames()))) {
        return;
      }

      this.$cols.each(function () {
        const size = settings.widths[$(this).text()];

        if (size) {
          this.style.width = `${size}px`;
        }
      });
    }

    #getStorageKey() {
      return `table-#${this.tableId}-widths`;
    }

    #getWidthsMap() {
      const widthsMap = {};
      this.$resizableCols.each(function () {
        widthsMap[$(this).text()] = this.clientWidth;
      });

      return widthsMap;
    }

    #getResizableColsNames() {
      return this.$resizableCols.map((i, col) => $(col).text()).get();
    }
  }

  AGN.Lib.CoreInitializer.new('table-cols-resizer', function ($scope = $(document)) {
    $scope.all('.table--borderless').each(function () {
      const $table = $(this);

      TableColumnsResizer.get($table)?.destroy();
      if (!$table.hasClass('table--preview') && !AGN.Lib.Helpers.isMobileView()) {
        new TableColumnsResizer($table).resize();
      }
    });
  });

})();
