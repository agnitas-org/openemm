AGN.Lib.Controller.new('iframe-progress', function () {

  this.addDomInitializer('iframe-progress', function () {
    const $previewProgress = $('#preview-progress');

    const loaderTimeout = setTimeout(() => {
      $previewProgress.show();
    }, 100); // prevent progress blink in case of fast iframe load

    $('.default-iframe').on('load', () => {
      clearTimeout(loaderTimeout)
      $previewProgress.hide();
    });
  });

});
