(() => {

  AGN.Lib.CoreInitializer.new('table-row-actions', function($scope = $(document)) {
    $scope.find('.js-table tr').each(function() {
      const $row = $(this);
      initializeRowClicks($row);
      initializeRowPopovers($row);
    });
  });

  function initializeRowClicks($row) {
    const $link = $row.find('[data-view-row]');
    if (!$link.exists()) {
      return;
    }

    $row.data('link', $link.attr('href'));

    if ($link.data('view-row') === 'page') { // detail view content is opened with reload
      $row.wrap(`<a class="table-row-wrapper" href="${$link.attr('href')}"></a>`);
    } else if ($link.data('modal') || $link.data('action')) { // content is loaded as a modal from the mustache template
      passDataAttrsFromLinkToRow($link, $row);
    } // content is loaded as a modal from the server. default
    $link.remove();
  }

  function passDataAttrsFromLinkToRow($link, $row) {
    const DATA_PREFIX = 'data-';

    $.each($link[0].attributes, (index, attr) => {
      if (attr.name.startsWith(DATA_PREFIX) && attr.name !== 'data-view-row') {
        const attrName = attr.name.substring(DATA_PREFIX.length);
        $row.attr(`data-${attrName}`, attr.value);
      }
    });
  }

  function initializeRowPopovers($row) {
    const $popoverContent = $row.find('.js-row-popover');
    if (!$popoverContent.exists()) {
      return;
    }

    const templateId = _.uniqueId('agn-table-row-popover-');
    AGN.Lib.Template.register(templateId, $popoverContent.html());

    $row.attr('data-popover', '');
    $row.attr('data-popover-options', JSON.stringify({
      html: true,
      delay: {show: 300, hide: 100},
      templateName: templateId
    }));
  }

})();
