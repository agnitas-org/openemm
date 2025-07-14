;(() => {

  AGN.Lib.CoreInitializer.new('table', ['js-table'], function ($scope = $(document)) {
    if (AGN.Lib.Storage.get('truncate-table-text') === false) {
      disableTableTruncation($scope);
    }

    updateColSpanForEmptyTableRow($scope);
  });

  function updateColSpanForEmptyTableRow($scope) {
    $scope.all('tr.empty').each(function () {
      const $el = $(this);
      const visibleHeadersCount = $el.closest('table').find('th:visible').length;
      $el.find('td').attr('colspan', visibleHeadersCount);
    });
  }

  function disableTableTruncation($scope) {
    $scope.all('[data-toggle-table-truncation]').each((i, el) => {
      $(el).closest('.table-wrapper').find('table, .ag-body').toggleClass('no-truncate');
    });
  }

})();
