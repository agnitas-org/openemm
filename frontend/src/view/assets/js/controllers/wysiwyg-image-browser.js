
AGN.Lib.Controller.new('wysiwyg-image-browser', function() {

    var config = this.config;

    var funcNum = getUrlParam('CKEditorFuncNum');
    var mailingId = getUrlParam('mailingID');

    updateImg();

    this.addAction({click: 'submit-image'}, function() {
        var activeImageTab = $('li[image-tab-name].active').attr('image-tab-name');
        if(activeImageTab === 'mediapool') {
            submit_mediapool_images_tab();
        } else {
            submit_other_images_tab();
        }
    });

    this.addAction({change: 'update-image'}, function() {
        updateImg();
    });

    this.addAction({click: 'close-window'}, function () {
        window.close();
    });

    function updateImg() {
        var imageNameValue = getOtherImgSelectVal();
        var $imgPreview = $('#other-images-tab .image-preview');
        var $noImgMessage = $('#other-images-tab .no_image_message');
        if (!imageNameValue || !imageNameValue.length) {
            $noImgMessage.show();
            $imgPreview.hide();
        } else {
            $imgPreview.attr('src', normalizeName(imageNameValue))
            $imgPreview.show();
            $noImgMessage.hide();
        }
        return 1;
    }

    function getUrlParam(paramName) {
        var reParam = new RegExp('(?:[\?&]|&amp;)' + paramName + '=([^&]+)', 'i');
        var match = window.location.search.match(reParam);
        return (match && match.length > 1) ? match[1] : '';
    }

    function submit_other_images_tab() {
        var correctLink = normalizeName(getOtherImgSelectVal());
        window.opener.CKEDITOR.tools.callFunction(funcNum, correctLink);
        window.close();
    }

    function submit_mediapool_images_tab() {
        var selectedImgContainer = document.querySelector('[data-action="mpSelectImage"].active');
        if(selectedImgContainer) {
            var imgSrc = selectedImgContainer.querySelector('img').getAttribute('src');
            window.opener.CKEDITOR.tools.callFunction(funcNum, imgSrc);
            window.close();
        }
    }

    function normalizeName(fname) {
        if (fname.substr(0, 4).toLowerCase() !== 'http') {
            fname = config.rdirDomain + '/image?ci=' + config.companyId + '&mi=' + mailingId + '&name=' + fname;
        }
        return fname;
    }

    function getOtherImgSelectVal() {
        return $('#other-images-tab [data-action="update-image"]').val();
    }

});
