AGN.Lib.Controller.new('manage-tables-viewfield', function() {
  var previousValueNullableField = "true";

  this.addAction({change: 'typeFieldChangeAction'}, function() {
    var $nullableEl = $('#nullable');
    if (this.el.val() === 'DATE' || this.el.val() === 'DATETIME') {
      previousValueNullableField = $nullableEl.val();
      $nullableEl.val('true');
    } else {
      $nullableEl.val(previousValueNullableField);
    }
  });
});