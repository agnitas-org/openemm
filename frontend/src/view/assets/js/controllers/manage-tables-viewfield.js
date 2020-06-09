AGN.Lib.Controller.new('manage-tables-viewfield', function() {
  var previousValueNullableField = "true";

  this.addAction({change: 'typeFieldChangeAction'}, function() {
    var $nullableEl = $('#nullable');
    var $fieldNullableDivEl = $('#fieldNullableDiv');
    if(this.el.val() === 'DATE'){
      previousValueNullableField = $nullableEl.val();
      $nullableEl.val('true');
      $fieldNullableDivEl.hide();
    } else {
      $nullableEl.val(previousValueNullableField);
      $fieldNullableDivEl.show();
    }
  });
});