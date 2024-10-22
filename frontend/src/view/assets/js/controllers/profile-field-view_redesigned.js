AGN.Lib.Controller.new('profile-field-view', function() {

  let fieldType;
  let $fixedValuesContainer;

  this.addDomInitializer('profile-field-view', function() {
    $fixedValuesContainer = $('#fixed-values-container');
    fieldType = this.config.fieldType;
    drawFixedValues(this.config.fixedValues ?? []);
  });

  this.addAction({click: 'delete-fixed-value'}, function() {
    this.el.closest('[data-fixed-value-row]').remove();
  });

  this.addAction({click: 'add-fixed-value'}, function() {
    const $row = this.el.closest('[data-fixed-value-row]');
    const fixedValue = $row.find('[name="allowedValues"]').val();

    $row.remove();
    appendRow(false, fixedValue);
    appendRow(true);
  });

  this.addAction({change: 'change-field-type'}, function() {
    fieldType = this.el.val();
    const fixedValues = AGN.Lib.Form.get(this.el).getValues('allowedValues');
    drawFixedValues(fixedValues);
  });

  function drawFixedValues(fixedValues) {
    $fixedValuesContainer.empty();
    _.each(fixedValues, val=> {
      if (val.trim() !== '') {
        appendRow(false, val)
      }
    });
    appendRow(true);
  }

  function appendRow(isLastRow, value = '') {
    const $row = AGN.Lib.Template.dom('fixed-value-row', {isLastRow, value, fieldType});
    $fixedValuesContainer.append($row);

    AGN.Lib.Form.get($fixedValuesContainer).initFields();
    AGN.runAll($row);
  }

});
