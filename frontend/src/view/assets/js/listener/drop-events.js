;(function(){

  $(document).on('dragster:enter', function(e) {
    var $target = $(e.target);
    if (!$target.is('[data-upload-dropzone]')) {
      $target.addClass('drag-over');
    }
  });

  $(document).on('dragster:leave', function(e) {
    var $target = $(e.target);
    if (!$target.is('[data-upload-dropzone]')) {
      $target.removeClass('drag-over');
    }
  });

})();
