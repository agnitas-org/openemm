AGN.Lib.DomInitializer.new('recipient-report-initializer', function($scope) {
  if (!$('body').attr('bs-theme')) {
    return;
  }

  const $iframe = $scope.find('iframe');
  $iframe.on('load.iframe', function () {
    changeColorsToUiTheme($iframe);
  });

  function changeColorsToUiTheme($iframe) {
    const $body = $iframe.contents().find('body');
    const $center = $body.find('center');
    if ($center.length) {
      $body.css('background-color', 'var(--bs-body-bg)');
      $center.css('background-color', 'var(--bs-body-bg)');
    } else {
      $iframe.contents().find('body').css('color', '#fff');
    }
  }
});
