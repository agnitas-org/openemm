(function() {
  var Action = AGN.Lib.Action;

  Action.new({'click': '[data-popup]'}, function() {
    var url = this.el.data('popup'),
        loc = window.location.href,
        baseurl = loc.substring(0, loc.lastIndexOf('/'));

    if (!url || url.indexOf('://') <= 0) {
        url = baseurl + '/' + url;
    }
    window.open(url,'help','width=800,height=600,left=0,top=0,scrollbars=yes');
  });

  $(document).on('click', 'a[data-modal]', function(e){
    e.preventDefault();
  });

  $(document).on('hidden.bs.modal', '.modal',function() {
    var $modal = $(this);

    $modal.trigger("modal:close");

    setTimeout(function() {
      $modal.remove();
    }, 100);
  })

})();
