AGN.Lib.Controller.new('profile-field-view', function() {

  let $fixedValuesContainer;

  this.addDomInitializer('profile-field-view', function() {
    $fixedValuesContainer = $('#fixed-values-container');

    const fixedValues = this.config.fixedValues ?? [];
    drawFixedValues(fixedValues);
  });

  function drawFixedValues(fixedValues) {
    _.each(fixedValues, val=> appendRow(false, val));
    appendRow(true);
  }

  function appendRow(isLastRow, value) {
    var $row = AGN.Lib.Template.dom('fixed-value-row', {isLastRow: isLastRow});
    if (value) {
      $row.find('input').val(value);
    }

    $fixedValuesContainer.append($row);
  }

  this.addAction({click: 'delete-fixed-value'}, function() {
    this.el.closest('[data-fixed-value-row]').remove();
  });

  this.addAction({click: 'add-fixed-value'}, function() {
    const $row = this.el.closest('[data-fixed-value-row]');
    const fixedValue = $row.find('input').val();

    $row.remove();
    appendRow(false, fixedValue);
    appendRow(true);
  });
});
