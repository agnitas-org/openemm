(function () {

  AGN.Lib.Action.new({click: '[data-upload-reset]'}, function () {
    const $dropzone = $(this.el.data('linked-dropzone'));
    if ($dropzone.length) {
      $dropzone.trigger('upload:reset');
    }
  });

})();