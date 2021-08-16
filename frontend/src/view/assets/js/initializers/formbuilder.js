AGN.Lib.DomInitializer.new('formbuilder', function($scope) {
    var Tooltip = AGN.Lib.Tooltip;

    if (!$scope) {
        $scope = $(document);
    }

    var config = this.config;

    _.each($scope.find('.js-form-builder'), function(textArea) {
        AGN.Lib.FormBuilder.get(textArea, config).promise().then(function () {
            var $textArea = $(textArea);
            $textArea.find('.form-actions button').each(function() {
                var $e = $(this);
                Tooltip.create($e, {title: $e.text()});
            });
        });
    });
});
