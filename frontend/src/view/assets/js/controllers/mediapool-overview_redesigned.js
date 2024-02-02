AGN.Lib.Controller.new('mediapool-overview', function () {

  const Modal = AGN.Lib.Modal;
  const Template = AGN.Lib.Template;
  const Form = AGN.Lib.Form;
  let fileExtensions = {};
  const duplicatedElementsData = {};

  this.addDomInitializer('mediapool-overview', function () {
    fileExtensions = this.config.fileExtensions;
  });

  this.addAction({'upload:add': 'mediapool-upload'}, function () {
    const file = this.data.file;
    let $modal = $('#mediapool-upload-modal');
    if (!$modal.exists()) {
      $modal = Modal.createFromTemplate({}, 'mediapool-upload-modal-template');
      $modal.on('modal:close', clearDropzone);
    }

    const $container = $modal.find('#mediapool-uploads-container');

    nameDuplicationCheckRequest(file.name, resp => {
      const extension = getFileExtension(file);

      const $item = Template.dom('mediapool-upload-item', {
        index: this.data.index,
        itemId: resp.data ? resp.data.id : 0,
        fileName: file.name,
        previewUrl: detectPreviewUrl(file),
        typeCode: detectFileTypeCode(extension),
        isArchive: extension === 'zip'
      });

      $container.append($item);
      Form.get($modal).initFields();
      AGN.runAll($item);
    }, true);
  });

  function clearDropzone() {
    $('#upload-files').val('');
    $('#manual-upload-file').trigger('upload:reset');
  }

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

  function detectFileTypeCode(extension) {
    if (extension) {
      for (const key in fileExtensions) {
        if (fileExtensions[key].includes(extension)) {
          return key;
        }
      }
    }

    return null;
  }

  this.addAction({click: 'upload-files'}, function () {
    const form = Form.get(this.el);
    const files = Array.from($('#upload-files')[0].files);

    let valid = form.validate();
    for (let i = 0; i < files.length; i++) {
      const $overwriteSlider = $(`#item-overwrite-${i}`);
      const fileName = $(`#item-file-name-${i}`).val();
      const overwrite = $overwriteSlider.is(':checked');

      nameDuplicationCheckRequest(fileName, resp => {
        if (resp.data && !overwrite) {
          valid = false;
          $overwriteSlider.data('item-id', resp.data.id);
          $(`[name='uploads["${i}"].id']`).val(resp.data.id);
          toggleDuplicationRelatedBlocksVisibility(i, false);

          AGN.Lib.Messages(t('defaults.warning'), t('fields.mediapool.warnings.overwrite_inactive', fileName), 'warning');
        } else if (!overwrite) {
          toggleDuplicationRelatedBlocksVisibility(i, true);
        }
      });
    }

    if (valid) {
      for (let i = 0; i < files.length; i++) {
        form.setValue(`uploads[${i}].file`, files[i]);
      }
      form.submit();
      this.el.parents('.modal').hide(); // don't destroy just hide, because it's form used by the progress bar
    }
  });

  function nameDuplicationCheckRequest(filename, success, async = false) {
    function commonSuccess(resp) {
      if (resp.data) {
        duplicatedElementsData[resp.data.id] = resp.data;
      }

      success(resp);
    }

    $.ajax(AGN.url('/mediapool/findElementWithName.action'), {
      data: filename,
      contentType: 'application/json',
      async: async,
      type: 'POST',
      success: commonSuccess
    });
  }

  function toggleDuplicationRelatedBlocksVisibility(index, hide) {
    $(`#duplication-error-block-${index}`).toggleClass('hidden', hide);
    $(`#overwrite-block-${index}`).toggleClass('hidden', hide);
  }

  this.addAction({change: 'overwrite-data'}, function () {
    const $el = $(this.el);
    const itemId = $el.data('item-id');
    const index = $el.data('index');

    let newTitle = '';
    let newCategory = '';
    if ($el.is(':checked') && duplicatedElementsData[itemId]) {
      const item = duplicatedElementsData[itemId];
      newTitle = item.title;
      newCategory = String(item.category.categoryId);
    }

    $(`#item-title-${index}`).val(newTitle);
    $(`#item-category-${index}`).val(newCategory).trigger('change');
  });

});
