AGN.Lib.Controller.new('custom-thumbnail-view', function() {

  this.addAction({
    change: 'use-custom-thumbnail'
  }, function() {
    var $file = $('#customThumbnailFile');
    // Remove name property to avoid input submitting
    $file.prop('name', this.el.is(':checked') ? 'customThumbnailFile' : '');
  });

  this.addAction({
    change: 'custom-thumbnail-image'
  }, function() {
    var $file = this.el;

    var files = $file.prop('files');
    if (files && files.length) {
      var file = files[0];
      if (file.type == 'image' || file.type.match('image/.*')) {
        var reader = new FileReader();
        reader.onload = function(e) {
          showCustomThumbnailPreview($file, file.name, e.target.result);
        };
        reader.readAsDataURL(file);
      } else {
        AGN.Lib.Helpers.clearFormField($file);
        showCustomThumbnailPreview($file, '', '');
      }
    } else {
      showCustomThumbnailPreview($file, '', '');
    }
  });

  function showCustomThumbnailPreview($file, name, content) {
    var targets;

    targets = $file.data('preview-name');
    if (targets) {
      $(targets).text(name);
    }

    targets = $file.data('preview-image');
    if (targets) {
      if (content) {
        $(targets).prop('src', content);
      } else {
        $(targets).each(function() {
          var $target = $(this);
          $target.prop('src', $target.data('no-preview-src') || '');
        });
      }
    }
  }

});
