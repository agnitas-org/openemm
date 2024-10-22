AGN.Lib.Controller.new('recipient-import-view', function () {

    this.addAction({click: 'delete-file'}, function () {
        const jqxhr = $.post(AGN.url('/recipient/import/file/delete.action'));
        jqxhr.done(function () {
            $('#uploaded-file-container').remove();
            $('#uploadFile').removeClass('hidden').prop('disabled', false);
        });
    });

    this.addDomInitializer('recipient-import-view', function () {
        const attachmentCsvFileID = this.config.attachmentCsvFileID;

        if (attachmentCsvFileID > 0) {
            const $csvUploadToggle = $('#useCsvUpload');
            $csvUploadToggle.prop('checked', true);
            $csvUploadToggle.trigger('change');
        }
    });

});
