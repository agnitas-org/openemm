AGN.Lib.CoreInitializer.new('display-dimensions', function ($scope = $(document)) {
  _.each($scope.find('[data-display-dimensions]'), el => {
    const $el = $(el);
    const scope = AGN.Lib.Helpers.objFromString($el.data('display-dimensions'))['scope'];
    const $target = $el.parents(scope).find('[data-dimensions]');

    $el.ensureLoad(function () {
      const width = el.naturalWidth;
      const height = el.naturalHeight;

      $target.html(`${width} x ${height} px`);
    })
  });
});