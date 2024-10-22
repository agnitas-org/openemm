;(() => {

  AGN.Lib.CoreInitializer.new('table', function($scope = $(document)) {

    $scope.all('.fit-content').each(function () {
      const $header = $(this);
      document.fonts.ready.then(() => {
        $header.width(calculateMaxHeaderWidth($header) + 1);
      });
    });

    $scope.all('[data-js-table]').each(function() {
      const $el = $(this);
      const $tableWrapper = $el.all('.table-wrapper');
      const id = $el.data('js-table');

      if ($tableWrapper.exists() && id) {
        const $config = $(`script#${CSS.escape(id)}`);

        if ($config.exists()) {
          const config = $config.json();

          let options;
          if ($tableWrapper.data('web-storage')) {
            options = _.merge(AGN.Lib.WebStorage.get($tableWrapper.data('web-storage')) || {}, config.options || {});
          } else {
            options = config.options || {};
          }

          new AGN.Lib.Table($tableWrapper, config.columns, config.data, options).applyFilter();
        }
      }
    });

    $scope.all('.table-wrapper__footer').each(function () {
      const $el = $(this);
      const $pagination = $el.find('.pagination');
      if ($pagination.exists()) {
        const pagesCount = $pagination.find("[data-page]").length;
        $el.addClass(`table-wrapper__footer--pages-${pagesCount}`)
      }
    });

    $scope.all('table th:visible').each((i, el)  => controlHeaderTooltips($(el)));

    if (AGN.Lib.Storage.get('truncate-table-text') === false) {
      $scope.all('[data-toggle-table-truncation]').each((i, el) => {
        $(el).closest('.table-wrapper').find('.table, .ag-body').toggleClass('no-truncate');
      });
    }

    $scope.all('.table-wrapper').on('table:redraw', e => {
      const $header = $(e.currentTarget).find('.ag-header-cell');
      controlHeaderTooltips($header);
    });

    function controlHeaderTooltips($header) {
      $header.off("mouseenter mouseleave");
      $header.hover(showTooltipIfTruncated, hideTooltip);
    }

    function showTooltipIfTruncated(e) {
      const $header = $(e.currentTarget);
      const $text = $header.is('.ag-header-cell') ? $header.find('.ag-header-cell-text') : $header;

      if ($text.length && $text[0].clientWidth < $text[0].scrollWidth) {
        const tooltip = getTooltipForHeader($header);
        tooltip.show();
      }
    }

    function hideTooltip(e) {
      AGN.Lib.Tooltip.get($(e.currentTarget))?.hide();
    }

    function getTooltipForHeader($header) {
      const tooltip = AGN.Lib.Tooltip.get($header);
      if (tooltip) {
        return tooltip;
      }
      return AGN.Lib.Tooltip.createTip($header, $header.text(), '', 'manual');
    }

    updateColSpanForEmptyTableRow($scope);
    $(window).on("displayTypeChanged", () => updateColSpanForEmptyTableRow($scope));
  });

  function updateColSpanForEmptyTableRow($scope) {
    $scope.all('tr.empty').each(function () {
      const $el = $(this);
      const visibleHeadersCount = $el.closest('table').find('th:visible').length;
      $el.find('td').attr('colspan', visibleHeadersCount);
    });
  }

  function calculateMaxHeaderWidth($header) {
    const inlineWrapper = '<span class="table-cell-inline-wrapper" style="display: inline-flex">';

    $header.contents().wrapAll(inlineWrapper);
    let maxWidth = $header.children()?.first()?.width() || 0;
    $header.find('.table-cell-inline-wrapper').contents().unwrap();

    $(`td:nth-child(${$header.index() + 1})`).each(function () {
      const $cell = $(this);
      if ($cell.contents().length > 0) {
        $cell.contents().wrapAll(inlineWrapper);
        maxWidth = Math.max(maxWidth, $cell.find('span').first().width());
        $cell.find('.table-cell-inline-wrapper').contents().unwrap();
      }
    });

    return maxWidth;
  }

})();
