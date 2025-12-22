AGN.Lib.CoreInitializer.new('ui-theme', function () {
  const theme = $('body').data('bs-theme');

  // prevent data-bs-theme from appearing in wysiwyg content and preview iframes
  $('iframe:not(.cke_wysiwyg_frame):not(.jodit-wysiwyg_iframe):not(.default-iframe)').each(function () {
    const $iframe = $(this);
    $iframe.on('load.iframe', function () {
      $iframe.contents().find('body').attr('data-bs-theme', theme);
    });
  });
});