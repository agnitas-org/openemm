AGN.Lib.CoreInitializer.new('slider', function($scope = $(document)) {

  _.each($scope.find('[data-toggle-checkboxes]'), function(el) {
    const $toggleAll = $(el);
    controlToggleAllCheckbox($toggleAll);
    getCheckboxesControlledBy($toggleAll).click(() => controlToggleAllCheckbox($toggleAll));
    $toggleAll.on('update-toggle-all', () => controlToggleAllCheckbox($toggleAll));
    addClickListener($toggleAll);
  });

  function addClickListener($toggleAll) {
    $toggleAll.on('click', function() {
      const $elem = $(this);
      _.each($elem.closest('.tile').find('.tile-body [type=checkbox]:enabled:visible'),
        checkbox => $(checkbox).prop('checked', $elem.is(":checked")));
    });
  }

  function getCheckboxesControlledBy($toggleAll) {
    return $toggleAll.closest('.tile').find('.tile-body').find('.form-check-input:visible');
  }

  function controlToggleAllCheckbox($toggleAll) {
    const $checkboxes = getCheckboxesControlledBy($toggleAll);
    $toggleAll.prop("checked", isAllEnabledCheckboxesChecked($checkboxes));
    $toggleAll.prop("disabled", isAllCheckboxesDisabled($checkboxes));
  }

  function isAllEnabledCheckboxesChecked(checkboxes) {
    return _.every(checkboxes.filter(':not(:disabled)'), checkbox => checkbox.checked);
  }

  function isAllCheckboxesDisabled(checkboxes) {
    return checkboxes.filter(':not(:disabled)').length === 0;
  }
});
