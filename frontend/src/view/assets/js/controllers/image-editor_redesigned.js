AGN.Lib.Controller.new('img-editor', function() {
  const Form = AGN.Lib.Form;
  let cropX, cropY;
  let cropWidth = 0, cropHeight = 0;
  let originalImageSrc;
  let image;
  let canvas;
  let context;

  this.addDomInitializer('img-editor', function() {
    canvas = document.getElementById('editor-canvas');
    context = canvas.getContext('2d');
    originalImageSrc = $('#editor-img').attr('src');
    loadImage();

    // Should be disabled until some changes.
    getResultFile$().prop('disabled', true);
  });

  function loadImage() {
    image = new Image();
    image.src = $('#editor-img').attr('src');
    image.onload = function() {
      canvas.width = image.width;
      canvas.height = image.height;
      context.drawImage(image, 0, 0);
      createResizable(); // tool btn controls enabled/disabled state of the resizable
      updateSizeInputs();
      resetSizePercent();
      updateUIDimensions();
    };
  }

  function updateSizeInputs(event, ui) {
    const width = ui?.size?.width || canvas.width;
    const height = ui?.size?.height || canvas.height;

    getImgWidth$().val(Math.round(width));
    getImgHeight$().val(Math.round(height));
  }

  function resetSizePercent() {
    $('#img-percent').val(100);
  }

  this.addAction({click: 'crop'}, async function() {
    disableAllTools('crop');
    if (!cropEnabled()) {
      toggleCrop();
      return;
    }
    await saveCrop();
  });

  async function saveCrop() {
    if (cropWidth <= 1 || cropHeight <= 1) {
      return;
    }

    const editFunc = function(tmpCanvas, tmpContext) {
      tmpCanvas.width = cropWidth;
      tmpCanvas.height = cropHeight;
      tmpContext.drawImage(image, cropX, cropY, cropWidth, cropHeight, 0, 0, cropWidth, cropHeight);
    }
    await editImageWithFunction(editFunc);
    toggleCrop();
  }

  this.addAction({click: 'resize'}, async function() {
    await saveCrop();
    disableAllTools('resize');
    toggleResize();
  });

  async function saveResize(event, ui) {
    const editFunc = function(tmpCanvas, tmpContext) {
      tmpCanvas.width = ui.size.width;
      tmpCanvas.height = ui.size.height;
      tmpContext.drawImage(image, 0, 0, ui.size.width, ui.size.height);
    }
    await editImageWithFunction(editFunc);
  }

  async function editImageWithFunction(editFunc) {
    await reloadImage(); // take into account percentage change
    const tmpCanvas = document.createElement('canvas');
    const tmpContext = tmpCanvas.getContext('2d');

    editFunc(tmpCanvas, tmpContext);

    canvas.width = tmpCanvas.width;
    canvas.height = tmpCanvas.height;
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.drawImage(tmpCanvas, 0, 0);

    await reloadImage();
    resetSizePercent();
  }

  async function reloadImage(keepResultDisabledState) {
    await new Promise(resolve => {
      image.src = canvas.toDataURL();
      if (!keepResultDisabledState) {
        getResultFile$().prop('disabled', false);
      }
      image.onload = resolve;
    });
  }

  this.addAction({click: 'rotate-clockwise'}, rotateClockwise);

  async function rotateClockwise() {
    await rotate(false);
  }

  this.addAction({click: 'rotate-counterclockwise'}, rotateCounterclockwise);

  async function rotateCounterclockwise() {
    await rotate(true);
  }

  async function rotate(counterclockwise) {
    disableAllTools();

    const editFunc = function(tmpCanvas, tmpContext) {
      tmpCanvas.width = canvas.height;
      tmpCanvas.height = canvas.width;

      tmpContext.translate(tmpCanvas.width / 2, tmpCanvas.height / 2);
      tmpContext.rotate((counterclockwise ? -1 : 1) * Math.PI / 2);
      tmpContext.drawImage(image, -image.width / 2, -image.height / 2);
    }
    await editImageWithFunction(editFunc);
    updateUIDimensions();
  }

  this.addAction({click: 'flip-horizontally'}, flipHorizontally);

  async function flipHorizontally() {
    await flip(false);
  }

  this.addAction({click: 'flip-vertically'}, flipVertically);

  async function flipVertically() {
    await flip(true);
  }

  async function flip(vertically) {
    await reloadImage();
    disableAllTools();
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.save();
    if (vertically) {
      context.scale(1, -1);
      context.drawImage(image, 0, -canvas.height);
    } else {
      context.scale(-1, 1);
      context.drawImage(image, -canvas.width, 0);
    }
    context.restore();
    resetSizePercent();
    await reloadImage();
  }

  function disableAllTools(exclude) {
    if (exclude !== 'crop') {
      disableCrop();
    }
    if (exclude !== 'resize') {
      disableResize();
    }
  }

  this.addAction({input: 'change-size'}, () => changeSize());

  this.addAction({input: 'change-size-via-percentage'}, () => changeSize(true));

  function getImgHeight$() {
    return $('#img-height');
  }

  function getImgWidth$() {
    return $('#img-width');
  }

  // Change size tool function not updates image src because with next user input origin dimensions
  // should still be reachable (i.e. percent change 100% -> 50% -> back to 100%)
  // Image src updated with after other tool applied or form submit
  function changeSize(viaPercentage) {
    disableAllTools();
    let {newWidth, newHeight} = viaPercentage
      ? getNewDimensionsToChangeSizeViaPercentage()
      : getNewDimensionsToChangeSize();

    canvas.width = newWidth;
    canvas.height = newHeight;
    context.save();
    context.drawImage(image, 0, 0, newWidth, newHeight);
    context.restore();
    getImgHeight$().val(newHeight);
    getImgWidth$().val(newWidth);
    updateUIDimensions();
    getResultFile$().prop('disabled', false);
  }

  function getNewDimensionsToChangeSize() {
    const oldWidth = image.width;
    const oldHeight = image.height;
    let newWidth = getImgWidth$().val();
    let newHeight = getImgHeight$().val();

    if (isProportionsLocked()) {
      if ($("#img-width").is(":focus")) {
        const percentage = newWidth / oldWidth;
        newHeight = Math.round(percentage * oldHeight);
      } else if ($("#img-height").is(":focus")) {
        const percentage = newHeight / oldHeight;
        newWidth = Math.round(percentage * oldWidth);
      }
    }
    return {newWidth, newHeight};
  }

  function getNewDimensionsToChangeSizeViaPercentage() {
    const percent = $('#img-percent').val();
    const oldWidth = image.width;
    const oldHeight = image.height;
    const newWidth = Math.round(oldWidth * (percent / 100));
    const newHeight = Math.round(oldHeight * (percent / 100));
    return {newWidth, newHeight};
  }

  function toggleBtn(toolName, state) {
    $(`[data-action="${toolName}"]`).toggleClass('active', state);
  }

  function disableCrop() {
    toggleCrop(false);
  }

  function cropEnabled() {
    return !!getJcrop();
  }

  function getJcrop() {
    return $('#editor-canvas').data('Jcrop');
  }

  function toggleCrop(state) {
    if (state === undefined) {
      state = !cropEnabled();
    }
    if (!state) {
      disableJCrop();
    } else {
      enableCrop();
    }
    toggleBtn('crop', state);
  }

  function createResizable() {
    if (getCanvas$().resizable( "instance" )) {
      getCanvas$().resizable( "destroy" );
    }
    getCanvas$().resizable({
      handles: 'ne, se, sw, nw, w, n, e, s',
      stop: saveResize,
      resize: updateSizeInputs,
      disabled: true
    });
  }

  function disableResize() {
    toggleResize(false);
  }

  this.addAction({change: 'lock-proportions'}, async function() {
    let locked = isProportionsLocked();
    toggleBtn('lock-proportions', locked);
    getCanvas$().resizable('option', 'aspectRatio', locked)
  });

  function isProportionsLocked() {
    return $('[data-action="lock-proportions"]').prop('checked');
  }

  function toggleResize(state) {
    if (state === undefined) {
      state = resizeDisabled();
    }
    getCanvas$().resizable(state ? "enable" : "disable");
    toggleBtn('resize', state);
  }

  function getCanvas$() {
    return $("#editor-canvas");
  }

  function resizeDisabled() {
    const $canvas = getCanvas$();
    return $canvas.resizable("instance")
      && $canvas.resizable("option", "disabled");
  }

  function disableJCrop() {
    const JcropAPI = getJcrop();
    if (typeof JcropAPI != 'undefined') {
      JcropAPI.destroy();
      createResizable();
      updateUIDimensions(); // after ui changed dimensions need to be fixed
    }
    cropWidth = 0;
    cropHeight = 0;
  }

  function enableCrop() {
    getCanvas$().Jcrop({
      onChange: calculateCropData,
      onSelect: calculateCropData,
      setSelect: [0, 0, canvas.width, canvas.height]
    });
  }

  function calculateCropData(jCropData) {
    cropX = jCropData.x;
    cropY = jCropData.y;
    cropWidth = jCropData.w;
    cropHeight = jCropData.h;
  }

  function updateUIDimensions() {
    const $canvas = getCanvas$();
    const $wrapper = $('.ui-wrapper');
    $canvas.css('width', canvas.width);
    $canvas.css('height', canvas.height);
    $wrapper.css('width', canvas.width);
    $wrapper.css('height', canvas.height);
    updateSizeInputs();
  }

  function getResultFile$() {
    return $('[name="encodedFile"]');
  }

  this.addAction({'click': 'save'}, async function() {
    await saveCrop();
    await reloadImage(true);
    const $result = getResultFile$();
    let newSrc = canvas.toDataURL();

    // Cut off a meta data prefix.
    const separatorPosition = newSrc.indexOf(',');
    if (separatorPosition >= 0) {
      newSrc = newSrc.substring(separatorPosition + 1);
    }

    if ($result.val() != newSrc) {
      $result.val(newSrc);
    }
    Form.get(this.el).submit();
  });

  this.addAction({'upload:add': 'replace'}, function() {
    disableAllTools();
    const reader = new FileReader();
    reader.readAsDataURL(this.data.file);
    reader.onload = function (e) {
      $('#editor-img').attr('src', e.target.result);
      loadImage();
      getResultFile$().prop('disabled', false);
    };
  });

  this.addAction({'click': 'reset'}, function() {
    disableAllTools();
    $('#editor-img').attr('src', originalImageSrc);
    loadImage();
    getResultFile$().prop('disabled', true); // disable input since file not changed, so nothing to update
  });
});
