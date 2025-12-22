(() => {

  $(document).on('agn:rail-mousedown', function(e, el) {
    $(el).find('iframe').addClass('pe-none');
  });

  $(document).on('agn:rail-mouseup', function(e, el) {
    $(el).find('iframe').removeClass('pe-none');
  });

  $(window).on("load", function () {
    new MutationObserver(() => AGN.Lib.Scrollbar.updateAll())
      .observe(document.body, {childList: true, subtree: true});
  });

})();
