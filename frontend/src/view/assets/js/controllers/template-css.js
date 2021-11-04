AGN.Lib.Controller.new('template-css', function() {
  this.addAction({
    click: 'updatePreview'
  }, function() {
    var $previewForm = $('#layoutPreviewForm');

    $previewForm.find('input[name="css"]')
      .val($('#cssContents').val());
    $previewForm.submit();
  });

  this.addAction({
    click: 'saveLayoutCss'
  }, function() {
    var formObj = AGN.Lib.Form.get($('#gridTemplateForm'));
    var mailingsIds = $('#save-css-approve-modal-form select[name="templatesToUpdate"]').val();

    var currentTemplateId = formObj.getValue("templateId");
    if(currentTemplateId) {
      mailingsIds.push(currentTemplateId);
    }

    formObj.setValueOnce("bulkIds", mailingsIds);
    formObj.submit();
  });
});
