AGN.Lib.Controller.new('template-styles', function() {
  function updatePreview() {
    var $templateForm = $('form#gridTemplateForm');
    var $previewForm = $('form#layoutPreviewForm');

    var $sources = $templateForm.find('input[name^="styles."]');

    $.each($sources, function(index, source) {
      var $source = $(source);
      var name = $source.attr('name');
      var $destination = $previewForm.find('input[name="' + name + '"]');

      $destination.val($source.val());
    });

    $previewForm.submit();
  }

  this.addAction({
    submission: 'updatePreview'
  }, function() {
    updatePreview();
  });

  this.addAction({
    click: 'updateLayoutPreview'
  }, function() {
    updatePreview();
  });

  this.addAction({
    click: 'saveLayoutStyles'
  }, function() {
    $('form#gridTemplateForm').submit();
  });
});
