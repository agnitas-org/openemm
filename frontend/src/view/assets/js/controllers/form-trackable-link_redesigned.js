AGN.Lib.Controller.new('form-trackable-links', function () {
  this.addAction({submission: 'save-all'}, function () {
    const form = AGN.Lib.Form.get($('#userFormTrackableLinksForm'));
    setExtensionsToForm(form, "commonExtensions", $('#links-common-extensions'));
    form.submit();
  });

  this.addAction({submission: 'save-individual'}, function () {
    const form = AGN.Lib.Form.get($('#userFormTrackableLinkForm'));
    setExtensionsToForm(form, "extensions", $('#link-extensions'));
    form.submit();
  });

  function setExtensionsToForm(form, fieldName, $extensions) {
    const extensions = $extensions.data('table').collect();
    _.each(extensions, function (extension, index) {
      form.setValueOnce(fieldName + '[' + index + '].name', extension.name);
      form.setValueOnce(fieldName + '[' + index + '].value', extension.value);
    })
  }
});
