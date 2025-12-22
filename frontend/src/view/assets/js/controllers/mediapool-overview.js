AGN.Lib.Controller.new('mediapool-overview', function () {

  this.addDomInitializer('mediapool-edit', function () {
    $('[id$="-editor-modal"]').on('modal:close', e => {
      if (!$(e.target).data('closedOnUnchangedSave')) {
        AGN.Lib.Messages.warn('defaults.changesNotSaved');
      }
      $(e.target).removeData('closedOnUnchangedSave');
    });
  });

  this.addAction({click: 'change-category-bulk'}, function () {
    const bulkIds = [...$('input[name="bulkIds"]:checked')].map(checkbox => checkbox.value);
    if (!bulkIds?.length) {
      AGN.Lib.Messages.alert('messages.error.nothing_selected');
      return;
    }
    AGN.Lib.Modal.fromTemplate('category-select-modal', {bulkIds});
  });

  this.addAction({'upload:add': 'replace-file'}, function () {
    const reader = new FileReader();
    reader.readAsDataURL(this.data.file);
    reader.onload = function (e) {
      $('#mediapool-img-preview').attr('src', e.target.result);
    };
  });

  this.addAction({click: 'edit-webp'}, function () {
    const opts = this.el.data('modal-set');

    $.ajax({
      url: opts.link,
      method: 'GET',
      xhrFields: {responseType: 'arraybuffer'},
      success: buffer => {
        if (isAnimatedWebp(new Uint8Array(buffer))) {
          AGN.Lib.Modal.fromTemplate('file-editor-modal', opts);
        } else {
          AGN.Lib.Modal.fromTemplate('image-editor-modal', opts);
        }
      },
      error: () => AGN.Lib.Modal.fromTemplate('file-editor-modal', opts)
    });
  });

  function isAnimatedWebp(bytes) {
    // Check the RIFF header (first 12 bytes)
    const isWebP =
      bytes[0] === 0x52 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[3] === 0x46 && // "RIFF"
      bytes[8] === 0x57 && bytes[9] === 0x45 && bytes[10] === 0x42 && bytes[11] === 0x50; // "WEBP"

    if (!isWebP) {
      return false;
    }

    // Check for the "ANIM" chunk in the RIFF structure
    const animIndex = bytes.findIndex((_, i) =>
      bytes[i] === 0x41 && bytes[i + 1] === 0x4E && bytes[i + 2] === 0x49 && bytes[i + 3] === 0x4D // "ANIM"
    );

    return animIndex > -1;
  }
});
