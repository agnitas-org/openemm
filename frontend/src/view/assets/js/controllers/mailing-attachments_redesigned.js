AGN.Lib.Controller.new('mailing-upload-attachment', function() {

  this.addAction({
    'change': 'change-attachment-file'
  }, function(){
    updateFilename(false);
  })

  this.addAction({
    'change': 'change-upload-file'
  }, function(){
    updateFilename(true);
  });

  this.addAction({change: 'use-pdf'}, function(){
    updateFilename(this.el.is(':checked'));
  });

  function updateFilename(useUploadActivated) {
    let attachmentName = '';
    if (useUploadActivated) {
      attachmentName = AGN.Lib.Select.get($('#pdfUploadId')).getSelectedText();
    } else {
      const files = $('#attachmentFile').prop('files');

      if (files && files.length) {
        attachmentName = files[0].name;
      }
    }

    $('#attachmentName').val(attachmentName);
  }
});
