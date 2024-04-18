AGN.Lib.CoreInitializer.new('csrf', function($scope) {
    if (!$scope) {
        $scope = $(document);
    }

    $scope.find('meta[name="_csrf_header"]').each(function() {
        window.csrfHeaderName = $(this).attr('content');
        window.csrfCookieName = window.csrfHeaderName;
    });

    $scope.find('meta[name="_csrf_parameter"]').each(function() {
        window.csrfParameterName = $(this).attr('content');
    });

    $scope.find('div > input[type="hidden"][name="_csrf"]:only-child').parent('div').hide();
});
