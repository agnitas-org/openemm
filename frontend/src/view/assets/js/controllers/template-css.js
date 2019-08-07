AGN.Lib.Controller.new('template-css', function() {
  this.addAction({
    click: 'updatePreview'
  }, function() {
    var $previewForm = $('#layoutPreviewForm');

    $previewForm.find('input[name="css"]')
      .val($('#cssContents').val());
    $previewForm.submit();
  });

  this.addAction({
    click: 'saveLayoutCss'
  }, function() {
    $('#gridTemplateForm').submit();
  });
});
