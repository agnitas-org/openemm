AGN.Lib.Controller.new('mailing-attachments', function() {
  var self = this,
      clearAttachment,
      updateFilename,
      updatePdfFilename;


  clearAttachment = function() {
    $('#newAttachmentName').val('');
    $('attachmentTargetID').prop("selectedIndex", 0);
  }

  updateFilename = function() {
    var filename = $("#newAttachment").val().match(/[^\\\/]+$/);

    $("#newAttachmentName").val(filename);
  }

  updatePdfFilename = function() {
    var filename = $("#attachmentPdfFileID").find(':selected').attr('title');

    $("#newAttachmentName").val(filename);
  }


  this.addAction({
    'change': 'use-pdf'
  }, function(){
    self.runInitializer('AttachmentUsePdf');
  });

  this.addAction({
    'change': 'update-filename'
  }, function(){
    updateFilename();
  })

  this.addAction({
    'change': 'update-pdf-filename'
  }, function(){
    updatePdfFilename();
    clearAttachment();
  });

  this.addInitializer('AttachmentUsePdf', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    var $trigger = $scope.find('[data-action="use-pdf"]');

    if ( $trigger.prop('checked') ) {
      $('#attachmentPdfFileID').prop('disabled', false);
      $('#newAttachment').prop('disabled', true);
      updatePdfFilename();
    } else {
      $('#attachmentPdfFileID').prop('disabled', true);
      $('#newAttachment').prop('disabled', false);
    }

    clearAttachment();

  });


});
