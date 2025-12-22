AGN.Lib.CoreInitializer.new('number-input', ($scope = $(document)) => {

  $scope.on("keypress", 'input[type="number"]', function (evt) {
    if (evt.which !== 8 && evt.which !== 0 && (evt.which < 48 || evt.which > 57)) {
      evt.preventDefault();
    }
  });
});
