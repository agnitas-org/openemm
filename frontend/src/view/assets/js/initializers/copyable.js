AGN.Lib.CoreInitializer.new('copyable', function ($scope = $(document)) {

  $scope.all('.click-to-copy').each(function () {
    const $el = $(this);
    if ($el.data('copyable-initialized')) {
      return;
    }
    $el
      .attr('data-copyable', '')
      .attr('data-tooltip', t('defaults.clickToCopy'))
      .data('copyable-initialized', true)
      .on('click', function (e) {
        e.preventDefault();
      })
    AGN.Lib.CoreInitializer.run('tooltip', $el);
  });
});
