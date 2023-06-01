AGN.Lib.Controller.new('emc-template-preview-controller', function() {
  
  this.addDomInitializer('emc-template-preview-initializer', function() {
    if ($('[name="view-state"]').length) {
      modifyPreviewHeaderForBlockView();
    }
  });
  
  function modifyPreviewHeaderForBlockView() {
    $("#preview").find(".tile-header").removeAttr("data-sizing");
    $('#content_wrapper').removeAttr('data-sizing');
    $('#previewTabsMenu').replaceWith('<h2 class="headline">' + t('defaults.previewMode') + '</h2>');
  }
});
