AGN.Lib.Controller.new('user-overview', function () {

  let isRestfulPage;

  this.addDomInitializer("user-overview", function () {
    isRestfulPage = this.config.isRestfulPage;
  });

  this.addAction({'table-column-manager:add': 'update-columns'}, function () {
    const form = AGN.Lib.Form.get(this.el);
    form.setValueOnce('additionalColumns', this.data.columns);
    form.setValueOnce('inEditColumnsMode', true);
    form.submit();
  });

  this.addAction({'table-column-manager:apply': 'update-columns'}, function () {
    const additionalColumns = this.data.columns;

    $.ajax({
      type: 'POST',
      url: AGN.url(`/${isRestfulPage ? 'restfulUser' : 'admin'}/list/setAdditionalColumns.action`),
      traditional: true,
      data: { additionalColumns }
    }).done(resp => {
      if (resp.success) {
        AGN.Lib.WebStorage.extend('admin-overview', {'fields': additionalColumns});
      }
      AGN.Lib.JsonMessages(resp.popups);
    });
  });
});
