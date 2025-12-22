AGN.Lib.Action.new({change: '[data-toggle-usability]'}, $el => {
  _.each($($el.data('toggle-usability')), target => {
    const $target = $(target);
    $target.prop('disabled', !$target.prop('disabled'));
  });
});