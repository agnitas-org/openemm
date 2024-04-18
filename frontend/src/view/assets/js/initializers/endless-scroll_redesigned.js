AGN.Lib.CoreInitializer.new('endless-scroll', function ($scope) {

  const EndlessScroll = AGN.Lib.EndlessScroll;

  if (!$scope) {
    $scope = $(document);
  }

  $scope.find('.js-endless-scroll').each(function () {
    const $el = $(this);
    let options = {};

    if ( $el.is('[data-endless-scroll-opts]') ) {
      const elementOptions = AGN.Lib.Helpers.objFromString($el.data('endless-scroll-opts'));
      options = _.extend(options, elementOptions);
    }

    new EndlessScroll($el, $el.data('url'), options);
  });
});
