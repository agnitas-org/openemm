AGN.Lib.Controller.new('mediapool-upload', function () {

  const Template = AGN.Lib.Template;
  const Form = AGN.Lib.Form;
  let fileExtensions = {};
  const duplicatedElementsData = {};
  let $container;
  let $cancelBtn;

  this.addDomInitializer('mediapool-upload', function () {
    fileExtensions = this.config.fileExtensions;
    $container = $('#components-uploads-container');
    $cancelBtn = $('#upload-reset-btn');

    $('#dropzone').on('upload:reset', function () {
      $container.empty();
      $container.prev().removeClass('hidden');
      $cancelBtn.parent().addClass('hidden');
    });
  });

  this.addAction({'upload:add': 'mediapool-upload'}, function () {
    const file = this.data.file;
    const index = this.data.index;
    $container.prev().addClass('hidden');
    $cancelBtn.parent().removeClass('hidden');

    nameDuplicationCheckRequest(file.name, resp => {
      const extension = getFileExtension(file);

      const $item = Template.dom('mediapool-upload-item', {
        index: index,
        itemId: resp.data ? resp.data.id : 0,
        fileName: file.name,
        previewUrl: detectPreviewUrl(file),
        typeCode: detectFileTypeCode(extension),
        isArchive: extension === 'zip'
      });

      $container.append($item);
      Form.get($container).initFields();
      AGN.runAll($item);
    }, true);
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
    const files = form._dataNextRequest;

    let valid = form.validate();

    let i = 0;
    for (key in files) {
      const $overwriteSlider = $(`#item-overwrite-${i}`);
      const fileName = $(`#item-file-name-${i}`).val();
      const overwrite = $overwriteSlider.is(':checked');

      nameDuplicationCheckRequest(fileName, resp => {
        if (resp.data && !overwrite) {
          valid = false;
          $overwriteSlider.data('item-id', resp.data.id);
          $(`[name='uploads["${i}"].id']`).val(resp.data.id);
          toggleDuplicationRelatedBlocksVisibility(i, false);

          AGN.Lib.Messages.warn('fields.mediapool.warnings.overwrite_inactive', fileName);
        } else if (!overwrite) {
          toggleDuplicationRelatedBlocksVisibility(i, true);
        }
      });

      i++;
    }

    if (valid) {
      for (let i = 0; i < files.length; i++) {
        form.setValue(`uploads[${i}].file`, files[i]);
      }
      form.submit();
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

  this.addAction({change: 'change-mobile-base'}, function() {
    const $el = this.el;
    const config = AGN.Lib.Helpers.objFromString($el.data('config'));

    const selectedValue = AGN.Lib.Select.get($el).getSelectedValue();

    const isMobile = selectedValue && selectedValue.length > 0;
    const fileName = isMobile ? config.mobilePrefix + selectedValue : config.initialValue;

    const $fileName = $(`#item-file-name-${config.index}`);
    $fileName.val(fileName).prop('readonly', isMobile);
  });
});
