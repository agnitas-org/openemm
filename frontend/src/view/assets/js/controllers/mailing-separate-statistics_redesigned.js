AGN.Lib.Controller.new('mailing-separate-statistics', function () {

  this.addAction({'table-column-manager:add': 'update-columns'}, function () {
    const form = AGN.Lib.Form.get(this.el);
    form.setValueOnce('additionalFields', this.data.columns);
    form.setValueOnce('inEditColumnsMode', true);
    form.submit();
  });

  this.addAction({'table-column-manager:apply': 'update-columns'}, function () {
    const selectedFields = this.data.columns;

    $.ajax({
      type: 'POST',
      url: AGN.url('/statistics/mailing/setSelectedFields.action'),
      traditional: true,
      data: {selectedFields}
    }).done(resp => {
      if (resp.success) {
        AGN.Lib.WebStorage.extend('mailing-separate-stats-overview', {'fields': selectedFields});
      }
      AGN.Lib.JsonMessages(resp.popups);
    });
  });

});
