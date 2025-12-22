AGN.Lib.Controller.new('mailing-recipients', function () {

  const Form = AGN.Lib.Form;

  this.addAction({'table-column-manager:add': 'update-columns'}, function () {
    const form = Form.get(this.el);
    form.setValueOnce('selectedFields', this.data.columns);
    form.setValueOnce('inEditColumnsMode', true);
    form.submit();
  });

  this.addAction({'table-column-manager:apply': 'update-columns'}, function () {
    const selectedFields = this.data.columns;

    $.ajax({
      type: 'POST',
      url: AGN.url('/mailing/recipients/setSelectedFields.action'),
      traditional: true,
      data: {selectedFields}
    }).done(resp => {
      if (resp.success) {
        AGN.Lib.WebStorage.extend('mailing-recipient-overview', {'fields': selectedFields});
      }
      AGN.Lib.JsonMessages(resp.popups);
    });
  });

});
