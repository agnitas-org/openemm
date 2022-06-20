AGN.Lib.Controller.new('image-editor', function() {
    var Confirm = AGN.Lib.Confirm,
      Template = AGN.Lib.Template;

    var x, y;
    var selectedWidth = 0, selectedHeight = 0;
    var isBlocked = false;
    var isEnableCrop = false;

    function calculateCropData(jCropData) {
        x = jCropData.x;
        y = jCropData.y;
        selectedWidth = jCropData.w;
        selectedHeight = jCropData.h;
    }

    this.addDomInitializer('img-editor-init', function() {
        isBlocked = false;
        isEnableCrop = false;

        // Should be disabled until some changes.
        $('#editor-result').prop('disabled', true);
    });

    this.addAction({
        click: 'editCategory'
    }, function() {
        Confirm.create(Template.text('modal-element-category-select', {categoryId: $('#category-id').val()}))
          .done(function(resp) {
              if (resp && resp.categoryId) {
                  $('#category-id').val(resp.categoryId);
              }
          });
    });

    this.addAction({
        click: 'cropImage'
    }, function() {
        getLastImageState();

        if (isEnableCrop) {
            destroyJcrop();
            updateOriginalImage();
            isEnableCrop = false;
        } else {
            $("#editor-canvas").Jcrop({
                onChange: calculateCropData,
                onSelect: calculateCropData,
                bgOpacity: 1
            });
            $('#editor-crop-btn').addClass('btn-primary');
            isEnableCrop = true;
        }
    });

    this.addAction({
        click: 'saveCrop'
    }, function() {
        getLastImageState();

        var canvas = document.getElementById('editor-canvas');
        var context = canvas.getContext('2d');

        var imageObj = new Image();
        imageObj.src = $('#editor-img').attr('src');

        if (selectedWidth > 1 && selectedHeight > 1) {
            imageObj.onload = function () {
                canvas.width = selectedWidth;
                canvas.height = selectedHeight;
                context.save();
                context.drawImage(imageObj, x, y, selectedWidth, selectedHeight, 0, 0, selectedWidth, selectedHeight);
                context.restore();

                var contentType = $('#editor-img').data('content-type');
                var newSrc = canvas.toDataURL(contentType);
                $('#editor-img').attr('src', newSrc);
                $('#editor-canvas').css('width', selectedWidth);
                $('#editor-canvas').css('height', selectedHeight);
                $('#l-editor-img-width').attr('value', selectedWidth);
                $('#l-editor-img-height').attr('value', selectedHeight);
                updateOriginalImage();
                destroyJcrop();
            };
        }
    });

    this.addAction({
        click: 'rotateImage'
    }, function() {
        destroyJcrop();
        getLastImageState();

        var canvas = document.getElementById('editor-canvas');
        var context = canvas.getContext('2d');

        var contentType = $('#editor-img').data('content-type');
        var newSrc = canvas.toDataURL(contentType);
        var imageObj = new Image();
        imageObj.src = newSrc;
        $('#editor-img').attr('src', newSrc);

        imageObj.onload = function() {
            var canvasWidth = canvas.width;
            var canvasHeight = canvas.height;

            canvas.width = canvasHeight;
            canvas.height = canvasWidth;

            canvasWidth = canvas.width;
            canvasHeight = canvas.height;

            context.save();
            context.translate(canvasWidth, canvasHeight/canvasWidth);
            context.rotate(Math.PI/2);
            context.drawImage(imageObj, 0, 0);
            context.restore();
            updateOriginalImage();
        };
    });

    this.addAction({
        input: 'changeSize'
    }, function() {
        destroyJcrop();
        var canvas = document.getElementById('editor-canvas');
        var context = canvas.getContext('2d');

        var imageObj = new Image();
        imageObj.src = $('#editor-img').attr('src');

        imageObj.onload = function() {
            var newImgWidth = $('#l-editor-img-width').val();
            var newImgHeight = $('#l-editor-img-height').val();

            var oldWidth = imageObj.width;
            var oldHeight = imageObj.height;

            if (isBlocked) {
                if ($("#l-editor-img-width").is(":focus")) {
                    var proportion = newImgWidth / oldWidth;
                    newImgHeight = Math.round(proportion * oldHeight);
                    $('#l-editor-img-height').attr('value', newImgHeight);
                } else if ($("#l-editor-img-height").is(":focus")) {
                    var proportion = newImgHeight / oldHeight;
                    newImgWidth = Math.round(proportion * oldWidth);
                    $('#l-editor-img-width').attr('value', newImgWidth);
                }
            }

            canvas.width = newImgWidth;
            canvas.height = newImgHeight;
            context.save();

            context.drawImage(imageObj, 0, 0, newImgWidth, newImgHeight);
            context.restore();
            updateOriginalImage();
        }
    });

    this.addAction({
        input: 'pcChangeSize'
    }, function() {
        destroyJcrop();
        var canvas = document.getElementById('editor-canvas');
        var context = canvas.getContext('2d');

        var imageObj = new Image();
        imageObj.src = $('#editor-img').attr('src');

        imageObj.onload = function() {
            var proportion = $('#l-editor-img-percent').val();
            var oldWidth = imageObj.width;
            var oldHeight = imageObj.height;

            var newImgWidth = Math.round(oldWidth * (proportion/100));
            var newImgHeight = Math.round(oldHeight * (proportion/100));
            $('#l-editor-img-height').attr('value', newImgHeight);
            $('#l-editor-img-width').attr('value', newImgWidth);

            canvas.width = newImgWidth;
            canvas.height = newImgHeight;
            context.save();

            context.drawImage(imageObj, 0, 0, newImgWidth, newImgHeight);
            context.restore();
            updateOriginalImage();
        }
    });

    this.addAction({
        click: 'blockSizes'
    }, function() {
        if (isBlocked) {
            isBlocked = false;
            $('#editor-lock-btn').removeClass('btn-primary');
        } else {
            isBlocked = true;
            $('#editor-lock-btn').addClass('btn-primary');
        }
    });

    function getLastImageState() {
        var canvas = document.getElementById('editor-canvas');
        var contentType = $('#editor-img').data('content-type');
        var newSrc = canvas.toDataURL(contentType);
        $('#editor-img').attr('src', newSrc);
    }

    function updateOriginalImage() {
        var canvas = document.getElementById('editor-canvas');
        var contentType = $('#editor-img').data('content-type');
        var newSrc = canvas.toDataURL(contentType);
        var $result = $('#editor-result');

        // Cut off a meta data prefix.
        var separatorPosition = newSrc.indexOf(',');
        if (separatorPosition >= 0) {
            newSrc = newSrc.substring(separatorPosition + 1);
        }

        if ($result.val() != newSrc) {
            $result.val(newSrc);
            $result.prop('disabled', false);
        }

        var newWidthOfEditor = canvas.width + $('#l-img-editor-tools').width() + 10;
        $('#l-img-editor').css('min-width', newWidthOfEditor);
    }

    function destroyJcrop() {
        var JcropAPI = $('#editor-canvas').data('Jcrop');
        if (typeof JcropAPI != 'undefined') {
            $('#editor-canvas').appendTo('#canvas-editor-area');
            $('#editor-canvas').removeAttr("style");
            JcropAPI.destroy();
        }
        $('#editor-crop-btn').removeClass('btn-primary');
        isEnableCrop = false;
        selectedWidth = 0;
        selectedHeight = 0;
    }
});
