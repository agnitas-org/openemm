
AGN.Lib.Controller.new('wysiwyg-image-browser', function () {

  let config;

  const funcNum = getUrlParam('CKEditorFuncNum');
  const mailingId = getUrlParam('mailingID');

  function getUrlParam(paramName) {
    const reParam = new RegExp('(?:[\?&]|&amp;)' + paramName + '=([^&]+)', 'i');
    const match = window.location.search.match(reParam);
    return (match && match.length > 1) ? match[1] : '';
  }

  this.addDomInitializer("wysiwyg-image-browser", function () {
    config = this.config;
    updateImg();
  });

  this.addAction({change: 'update-image'}, function () {
    updateImg();
  });

  function updateImg() {
    const imageNameValue = getOtherImgSelectVal();
    const $imgPreview = $('#image-preview');
    const $noImgMessage = $('#no-image-message');

    const imageExists = !!(imageNameValue && imageNameValue.length);

    $noImgMessage.toggleClass('hidden', imageExists);
    $imgPreview.parent().toggleClass('hidden', !imageExists);

    if (imageExists) {
      $imgPreview.attr('src', normalizeName(imageNameValue))
    }
  }

  this.addAction({click: 'submit-image'}, function () {
    const activeImageTab = $('.navbar li .active').data('image-tab-name');
    if (activeImageTab === 'mediapool') {
      applyMediapoolImage();
    } else {
      applyOtherImage();
    }
  });

  this.addAction({click: 'close-window'}, function () {
    window.close();
  });

  function applyOtherImage() {
    const correctLink = normalizeName(getOtherImgSelectVal());
    submitLink(correctLink);
  }

  function applyMediapoolImage() {
    const $pickedImage = $('tr.picked');
    if ($pickedImage.exists()) {
      const imgSrc = $pickedImage.find('img').attr('src');
      submitLink(imgSrc);
    } else {
      AGN.Lib.Messages.alert('messages.error.nothing_selected');
    }
  }
  
  function submitLink(link) {
    window.opener.CKEDITOR.tools.callFunction(funcNum, link);
    window.close();
  }

  function normalizeName(fname) {
    if (fname.substr(0, 4).toLowerCase() !== 'http') {
      return `${config.rdirDomain}/image?ci=${config.companyId}&mi=${mailingId}&name=${encodeURIComponent(fname)}`;
    }
    return encodeURI(fname)
  }

  function getOtherImgSelectVal() {
    return $('#image-dropdown').val();
  }
});
