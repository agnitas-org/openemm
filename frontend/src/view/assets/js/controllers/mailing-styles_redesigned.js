AGN.Lib.Controller.new("mailing-styles", function () {

  const Form = AGN.Lib.Form;

  this.addAction({click: 'update-preview', submission: 'updatePreview'}, function () {
    const form = Form.get($('#layoutPreviewForm'));

    Form.get(this.el).get$().find('input[name^="styles["]').each(function () {
      const $el = $(this);
      form.setValue($el.attr('name'), $el.val());
    });

    form.submit();
  });

});
