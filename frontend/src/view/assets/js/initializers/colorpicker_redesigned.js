AGN.Lib.CoreInitializer.new('colorpicker', function ($scope = $(document)) {
  const Colorpicker = AGN.Lib.Colorpicker;

  $scope.find('.js-colorpicker').each(function () {
    const $el = $(this);

    if (!Colorpicker.get($el)) {
      new Colorpicker($el);
    }
  });

});
