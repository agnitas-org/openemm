AGN.Lib.DomInitializer.new('recipient-report-initializer', function($scope) {
  if (!$('body').hasClass('dark-theme')) {
    return;
  }
  const $iframe = $scope.find('iframe');
  $iframe.on('load.iframe', function () {
    changeColorsToDarkTheme($iframe);
  });

  function changeColorsToDarkTheme($iframe) {
    const $body = $iframe.contents().find('body');
    const $center = $body.find('center');
    if ($center.length) {
      $body.css('background-color', 'var(--main-bg-color)');
      $center.css('background-color', 'var(--main-bg-color)');
    } else {
      $iframe.contents().find('body').css('color', '#fff');
    }
    $iframe.height($iframe.contents().height());
  }
});
