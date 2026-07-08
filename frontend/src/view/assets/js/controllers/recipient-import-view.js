AGN.Lib.Controller.new('recipient-import-view', function () {

  const Select = AGN.Lib.Select;
  const Helpers = AGN.Lib.Helpers;

  this.addAction({click: 'delete-file'}, function () {
    $.post(AGN.url('/recipient/import/file/delete.action')).done(() => {
      this.el.parent().remove();
      $('#import-file').prop('disabled', false).parent().removeClass('hidden');
    });
  });

  this.addDomInitializer('recipient-import-view', function () {
    disableNotMatchedUploadOptions();
    $('#import-profile').on('change', disableNotMatchedUploadOptions);
    if (this.config.uploadId > 0) {
      $('#use-upload').prop('checked', true).trigger('change');
    }
  });

  function disableNotMatchedUploadOptions() {
    const $profileOption = Select.get($('#import-profile')).$selectedOption;
    const profileType = ($profileOption.data('file-type') || '').trim().toLowerCase();
    const allowedExtensions = Helpers.IMPORT_TYPE_MAP[profileType] || [profileType];

    Select.get($('#import-uploads')).disableOptionsByPredicate((option) => {
      const filename = $(option).text().trim();
      const fileExtension = Helpers.getFileExtension(filename);
      return !allowedExtensions.includes(fileExtension);
    }, true);
  }
});
