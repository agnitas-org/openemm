AGN.Lib.Controller.new('allowed-profile-field-values', function() {
  var controller = this;

  this.addInitializer('allowed-values', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    var $controller = $scope.find('[data-controller="allowed-profile-field-values"]');
    if ($controller.length == 0) {
      return;
    }

    controller.container = $controller.find('table .js-allowed-value').first().parent();
  });

  this.addAction({click: 'addNewValue'}, function() {
    var $row = controller.getRow(this.el);
    var value = controller.getField($row).val();

    $row.remove();
    controller.appendRow(false, value);
    controller.appendRow(true);
  });

  this.addAction({click: 'deleteValue'}, function() {
    controller.getRow(this.el).remove();
  });

  this.appendRow = function(isLastRow, value) {
    var $row = $(_.template(AGN.Opt.Templates['allowed-value-new-row'], {isLastRow: isLastRow}));
    if (value) {
      this.getField($row).val(value);
    }
    this.container.append($row);
  };

  this.getRow = function($e) {
    return $e.closest('tr.js-allowed-value');
  };

  this.getField = function($row) {
    return $row.find('input[name="allowedValues"]');
  };
});
