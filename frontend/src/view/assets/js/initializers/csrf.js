AGN.Lib.CoreInitializer.new('csrf', function ($scope = $(document)) {
  const CSRF = AGN.Lib.CSRF;

  $scope.find('meta[name="_csrf_header"]').each(function () {
    const headerName = $(this).attr('content');
    CSRF.setHeaderName(headerName);
    CSRF.setCookieName(headerName);
  });

  $scope.find('meta[name="_csrf_parameter"]').each(function () {
    CSRF.setParamName($(this).attr('content'));
  });

  if (AGN.Lib.CSRF.isProtectionEnabled()) {
    $scope.find(`div > input[type="hidden"][name="${CSRF.getParameterName()}"]:only-child`).parent('div').hide();
  }

});
