AGN.Lib.Controller.new("mailing-styles", function() {
  function updatePreview() {
    var $mailingForm = $('form#mailingGridForm');
    var $previewForm = $('form#mailingPreviewForm');

    var $sources = $mailingForm.find('input[name^="styles."]');

    $.each($sources, function(index, source) {
      var $source = $(source);
      var name = 'styles[' + $source.attr('name').split('.')[1] + ']';
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
    click: 'updateMailingPreview'
  }, function() {
    updatePreview();
  });

  this.addAction({
    click: 'saveMailingStyles'
  }, function() {
    $('form#mailingGridForm').submit();
  });
});
