;(function(){

  AGN.Lib.CoreInitializer.new('table', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    $scope.all('.fit-content').each(function () {
      const $header = $(this);
      document.fonts.ready.then(() => {
        $header.width(calculateMaxHeaderWidth($header) + 1);
      });
    });

    $scope.all('.js-data-table').each(function() {
      const $el = $(this);
      const $body = $el.find('.js-data-table-body');
      const id = $el.data('table');

      if ($body.exists() && id) {
        let $config = $('script#' + CSS.escape(id)),
            config, options;

        if ($config.exists()) {
          config = $config.json();

          if ($body.data('web-storage')) {
            options = _.merge(AGN.Lib.WebStorage.get($body.data('web-storage')) || {}, config.options || {});
          } else {
            options = config.options || {};
          }

          new AGN.Lib.Table($body, config.columns, config.data, options).applyFilter();
        }
      }
    });

    $scope.all('.table-controls').each(function () {
      const $el = $(this);
      const $pagination = $el.find('.pagination');
      if ($pagination.exists()) {
        const pagesCount = $pagination.find("[data-page]").length;
        $el.addClass(`pages-${pagesCount}`)
      }
    });

    $scope.all('table th:visible').each((i, el)  => controlHeaderTooltips($(el)));

    $scope.all('.js-data-table-body').on('table:redraw', e => {
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

    $scope.all('.table-header-dropdown').each(function () {
      const $el = $(this);
      let $dropdownMenu;

      $el.on('show.bs.dropdown',function(){
        const $window = $(window);
        $dropdownMenu = $(this).find('.dropdown-menu');

        $('body').append($dropdownMenu.css({
          position:'fixed',
          left: $dropdownMenu.offset().left - $window.scrollLeft(),
          top: $dropdownMenu.offset().top - $window.scrollLeft()
        }).detach());
      });

      const returnToOriginalPlace = function () {
        $el.append($dropdownMenu.css({
          position:'absolute', left:false, top:false
        }).detach());
      }

      $el.on('hidden.bs.dropdown', returnToOriginalPlace);
      $el.find('.js-dropdown-close').on('click', returnToOriginalPlace);
    });

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
