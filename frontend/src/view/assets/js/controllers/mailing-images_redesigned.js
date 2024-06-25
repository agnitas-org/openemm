AGN.Lib.Controller.new('mailing-images', function () {

  const Modal = AGN.Lib.Modal;
  const Template = AGN.Lib.Template;
  const Form = AGN.Lib.Form;

  this.addAction({'upload:add': 'image-upload'}, function () {
    const file = this.data.file;
    const index = this.data.index;

    let $modal = $('#components-upload-modal');
    if (!$modal.exists()) {
      $modal = Modal.fromTemplate('userform-images-upload-modal-template');
      $modal.on('modal:close', clearDropzone);
    }

    const $container = $modal.find('#components-uploads-container');

    const $item = Template.dom('mailing-upload-image', {
      index: index,
      fileName: file.name,
      previewUrl: detectPreviewUrl(file),
      isArchive: getFileExtension(file) === 'zip'
    });

    $container.append($item);
    AGN.runAll($item);
  });

  function getFileExtension(file) {
    if (file.name && file.name.includes('.')) {
      return file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase();
    }

    return '';
  }

  function detectPreviewUrl(file) {
    if (/^image/.test(file.type)) {
      return URL.createObjectURL(file);
    }

    return '';
  }

  function clearDropzone() {
    $('#upload-files').val('');
    $('#dropzone').trigger('upload:reset');
  }

  this.addAction({click: 'upload-images'}, function () {
    const form = Form.get(this.el);
    const $modal = Modal.getWrapper(this.el);

    form.$form.on('submitted', () => {
      if (document.contains($modal[0])) {
        $modal.css('opacity', 1);
      }
    });

    form.submit();
    $modal.css('opacity', 0); // don't destroy just hide, because it's form used by the progress bar
  });

});
