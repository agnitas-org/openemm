AGN.Lib.Controller.new('custom-thumbnail-view', function() {

  this.addAction({change: 'use-custom-thumbnail'}, function() {
    // Remove name property to avoid input submitting
    $('#customThumbnailFile').prop('name', this.el.is(':checked') ? 'customThumbnailFile' : '');
  });

  this.addAction({change: 'custom-thumbnail-image'}, function() {
    const $file = this.el;
    const files = $file.prop('files');

    if (files?.length) {
      const file = files[0];
      if (file.type == 'image' || file.type.match('image/.*')) {
        const reader = new FileReader();
        reader.onload = e => {
          showCustomThumbnailPreview($file, file.name, e.target.result);
        };
        reader.readAsDataURL(file);
      } else {
        AGN.Lib.Helpers.clearFormField($file);
        showCustomThumbnailPreview($file);
      }
    } else {
      showCustomThumbnailPreview($file);
    }
  });

  this.addAction({click: 'delete-custom-thumbnail-image'}, function() {
    const $file = this.el.parent().parent().find('input[type="file"]');

    if ($file.prop('files')?.length) {
      AGN.Lib.Helpers.clearFormField($file);
      showCustomThumbnailPreview($file);
    }
  });

  function showCustomThumbnailPreview($file, name = '', content = '') {
    let targets = $file.data('preview-name');
    if (targets) {
      $(targets).text(name);
    }

    targets = $file.data('preview-image');
    if (targets) {
      if (content) {
        $(targets).prop('src', content);
      } else {
        $(targets).each(function() {
          const $target = $(this);
          $target.prop('src', $target.data('no-preview-src') || '');
        });
      }
    }
  }

});
