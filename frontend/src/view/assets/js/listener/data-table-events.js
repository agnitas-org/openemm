(function(){

  var Table = AGN.Lib.Table,
      WebStorage = AGN.Lib.WebStorage;

  $(document).on('click', '.js-data-table-first-page', function(e) {
    Table.get($(this)).api.paginationGoToFirstPage();
  });

  $(document).on('click', '.js-data-table-prev-page', function(e) {
    Table.get($(this)).api.paginationGoToPreviousPage();
  });

  $(document).on('click', '.js-data-table-page', function(e) {
    Table.get($(this)).api.paginationGoToPage($(this).data('page'));
  });

  $(document).on('click', '.js-data-table-next-page', function(e) {
    Table.get($(this)).api.paginationGoToNextPage();
  });

  $(document).on('click', '.js-data-table-last-page', function(e) {
    Table.get($(this)).api.paginationGoToLastPage();
  });

  $(document).on('click', '.js-data-table-paginate', function(e) {
    var $e = $(this);
    var pageSize = $e.data('page-size');
    var api = Table.get($e).api;

    api.paginationSetPageSize(pageSize);

    // Close drop down menu as user clicked on button within.
    $e.closest('.dropdown-menu').trigger('click');

    var bundle = $e.data('web-storage');
    if (bundle) {
      WebStorage.extend(bundle, {"paginationPageSize": parseInt(pageSize)});
    }
  });

  $(window).on('viewportChanged', function(e) {
    $(document).all('.js-data-table-body').each(function() {
      AGN.Lib.Table.get($(this)).redraw();
    })
  })

})();
