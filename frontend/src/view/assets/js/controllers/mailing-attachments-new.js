AGN.Lib.Controller.new('mailing-upload-attachment-new', function() {

  this.addDomInitializer('mailing-upload-attachment', function() {
    resetFields();
  });

  this.addAction({
    'change': 'use-pdf'
  }, function(){
    updateFields($(this.elem).prop('checked'));
  });

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

  function resetFields() {
    $('#attachment').val('');
    $('#attachmentName').val('');
    $('#backgroundAttachment').val('');
    $('#targetId').select2('val', 0)
    AGN.Lib.Select.get($('#type')).selectFirstValue();
  }

  function updateFilename(useUploadActivated) {
    var attachmentName = '';
    if (useUploadActivated) {
      attachmentName = $('#pdfUploadId').select2('val');
    } else {
      var files = $('#attachment').prop('files');

      if (files && files.length) {
        attachmentName = files[0].name;
      }
    }

    $('#attachmentName').val(attachmentName);
  }

  function updateFields(useUploadActivated) {
    resetFields();
    updateFilename(useUploadActivated)
  }
});
