AGN.Lib.Controller.new('recipient-import-view', function () {

  this.addAction({click: 'delete-file'}, function () {
    $.post(AGN.url('/recipient/import/file/delete.action')).done(() => {
      this.el.parent().remove();
      $('#import-file').prop('disabled', false).parent().removeClass('hidden');
    });
  });

  this.addDomInitializer('recipient-import-view', function () {
    const attachmentCsvFileID = this.config.attachmentCsvFileID;

    if (attachmentCsvFileID > 0) {
      $('#useCsvUpload').prop('checked', true).trigger('change');
    }
  });

});
