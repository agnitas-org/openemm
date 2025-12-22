AGN.Lib.CoreInitializer.new('input-mask', function($scope = $(document)) {
  _.each($scope.find('.js-inputmask'), input => {
    const $input = $(input);

    const options = _.merge({}, AGN.Lib.Helpers.objFromString($input.data("inputmask-options")));
    const mask = options.mask;
    delete options.mask;

    $input.inputmask(mask, options);
  });
});