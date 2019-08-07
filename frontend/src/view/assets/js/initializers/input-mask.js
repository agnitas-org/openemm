;(function(){

    var Helpers = AGN.Lib.Helpers;

    AGN.Initializers.InputMask = function ($scope) {
        if (!$scope) {
            $scope = $(document);
        }

        
        _.each($scope.find('.js-inputmask'), function(input) {
        	var $input = $(input);
        	var options;
        	var mask;

        	options = _.merge({}, Helpers.objFromString($input.data("inputmask-options")));
        	mask = options.mask;
        	delete options.mask;
        	     	
        	$input.inputmask(mask, options);
        });
        
    }

})();
