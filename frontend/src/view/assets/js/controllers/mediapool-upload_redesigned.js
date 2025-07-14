AGN.Lib.Controller.new('mediapool-upload', function () {

  const Template = AGN.Lib.Template;
  const Form = AGN.Lib.Form;

  const UPLOAD_DATA_ATTR_NAME = 'data-mediapool-upload';
  const UPLOAD_SELECTOR = `[${UPLOAD_DATA_ATTR_NAME}]`;

  const duplicatedElementsData = {};
  let fileExtensions = {};

  let $container;
  let $cancelBtn;
  let $dropzone;

  this.addDomInitializer('mediapool-upload', function () {
    fileExtensions = this.config.fileExtensions;
    $container = $('#components-uploads-container');
    $cancelBtn = $('#upload-reset-btn');
    $dropzone = $('#dropzone');

    $dropzone.on('upload:reset', function () {
      $container.empty();
      toggleUploadNotification(true);
      toggleCancelBtnVisibility(false);
    });
  });

  this.addAction({'upload:add': 'mediapool-upload'}, function () {
    const file = this.data.file;
    const index = this.data.index;

    toggleUploadNotification(false);
    toggleCancelBtnVisibility(true);

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
    let valid = form.validate();

    findUploads$().each(function () {
      const $el = $(this);
      const $overwriteSlider = $el.find('[data-action="overwrite-data"]');

      const fileName = $el.find('[name$=".fileName"]').val();
      const overwrite = $overwriteSlider.is(':checked');

      nameDuplicationCheckRequest(fileName, resp => {
        if (resp.data && !overwrite) {
          valid = false;
          $overwriteSlider.data('item-id', resp.data.id);
          $(`[name='uploads[${getUploadIndex($el)}].id']`).val(resp.data.id);
          toggleDuplicationRelatedBlocksVisibility($el, true);

          AGN.Lib.Messages.warn('fields.mediapool.warnings.overwrite_inactive', fileName);
        } else if (!overwrite) {
          toggleDuplicationRelatedBlocksVisibility($el, false);
        }
      });
    });

    if (valid) {
      form.submit();
    }
  });

  function updateToggleAllBtn() {
    AGN.Lib.CoreInitializer.run("slider", $('#upload-files-tile'));
  }

  function nameDuplicationCheckRequest(fileName, success, async = false) {
    const commonSuccess = resp => {
      if (resp.data) {
        duplicatedElementsData[resp.data.id] = resp.data;
      }

      success(resp);
      updateToggleAllBtn();
    }

    $.ajax(AGN.url('/mediapool/findElementWithName.action'), {
      data: {fileName},
      async: async,
      type: 'GET',
      success: commonSuccess
    });
  }

  this.addAction({change: 'overwrite-data'}, function () {
    const $el = $(this.el);
    const $tile = $el.closest('.tile');

    const itemId = $el.data('item-id');

    let newTitle = '';
    let newCategory = '';
    if ($el.is(':checked') && duplicatedElementsData[itemId]) {
      const item = duplicatedElementsData[itemId];
      newTitle = item.title;
      newCategory = String(item.category.categoryId);
    }

    $tile.find('[name$=".title"]').val(newTitle);
    $tile.find('[name$=".categoryId"]').val(newCategory).trigger('change');
  });

  this.addAction({change: 'change-mobile-base'}, function () {
    const $el = this.el;
    const config = AGN.Lib.Helpers.objFromString($el.data('config'));

    const selectedValue = AGN.Lib.Select.get($el).getSelectedValue();

    const isMobile = selectedValue && selectedValue.length > 0;
    const fileName = isMobile ? config.mobilePrefix + selectedValue : config.initialValue;

    const $fileName = $el.closest('.tile').find('[name$=".fileName"]');
    $fileName.val(fileName).prop('readonly', isMobile);
  });

  this.addAction({click: 'remove-upload'}, function () {
    const index = getUploadIndex(this.el);
    const form = Form.get(this.el);

    AGN.Lib.Upload.get($dropzone).removeSelection(index);
    this.el.closest('.tile').remove();

    updateIndexes(index);
    form.initFields();

    if (!findUploads$().exists()) {
      toggleUploadNotification(true);
      toggleCancelBtnVisibility(false);
    }
    updateToggleAllBtn();
  });

  function updateIndexes(fromIndex) {
    const $uploads = findUploads$().filter(function () {
      const index = getUploadIndex($(this));
      return parseInt(index) > parseInt(fromIndex);
    });

    $uploads.each(function () {
      const $el = $(this);
      const index = getUploadIndex($el) - 1;

      $el.find('[name*="uploads["]').each(function () {
        const $input = $(this);
        $input.attr('name', $input.attr('name').replace(/uploads\[\d+]/g, `uploads[${index}]`));
      });

      $el.attr(UPLOAD_DATA_ATTR_NAME, index);
    });
  }

  function findUploads$() {
    return $(UPLOAD_SELECTOR);
  }

  function getUploadIndex($el) {
    const $upload = $el.is(UPLOAD_SELECTOR) ? $el : $el.closest(UPLOAD_SELECTOR);
    return $upload.attr(UPLOAD_DATA_ATTR_NAME);
  }

  function toggleUploadNotification(show) {
    $container.prev().toggleClass('hidden', !show);
  }

  function toggleCancelBtnVisibility(show) {
    $cancelBtn.parent().toggleClass('hidden', !show);
  }

  function toggleDuplicationRelatedBlocksVisibility($scope, show) {
    $scope.find('[data-duplication-error-block]').toggleClass('hidden', !show);
    $scope.find('[data-overwrite-block]').toggleClass('hidden', !show);
  }
});
