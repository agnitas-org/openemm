AGN.Lib.Controller.new('template-css', function() {

  const Form = AGN.Lib.Form;

  this.addAction({click: 'saveCss'}, function() {
    const form = Form.get($('#gridTemplateForm'));
    const ids = this.el.closest('.modal').find(`[name="templatesToUpdate"]`).val();

    const currentTemplateId = form.getValue("templateId");
    if (currentTemplateId) {
      ids.push(currentTemplateId);
    }

    form.setValueOnce("bulkIds", ids);
    form.submit();
  });
});
