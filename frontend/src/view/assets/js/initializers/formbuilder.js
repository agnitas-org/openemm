AGN.Lib.DomInitializer.new('formbuilder', function($scope) {
    var Tooltip = AGN.Lib.Tooltip;

    if (!$scope) {
        $scope = $(document);
    }

    var config = this.config;
    var $commonConfig = $('script#' + CSS.escape('config:formbuilderCommon') + '[type="application/json"]');
    if($commonConfig.length === 1) {
        config = $.extend($commonConfig.json(), config);
    }

    _.each($scope.find('.js-form-builder'), function(textArea) {
        AGN.Lib.FormBuilder.FormBuilder.get(textArea, config).promise().then(function () {
            var $textArea = $(textArea);
            $textArea.find('.form-actions button').each(function() {
                var $e = $(this);
                Tooltip.create($e, {title: $e.text()});
            });
        });
    });

    _.each($scope.find('.modal-js-form-builder'), function (textArea) {
        var targetAttr = $(textArea).attr('data-target-formbuilder');
        if(!targetAttr) {
            return;
        }
        var $targetFB = $(targetAttr);
        if(!$targetFB) {
            return;
        }
        var targetFBObj = AGN.Lib.FormBuilder.FormBuilder.get($targetFB);
        AGN.Lib.FormBuilder.FormBuilder.get(textArea, config).setJson(targetFBObj.getJson());
    });
});
