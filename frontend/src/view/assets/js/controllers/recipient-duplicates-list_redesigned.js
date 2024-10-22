AGN.Lib.Controller.new('recipient-duplicates-list', function () {

  this.addAction({'table-column-manager:apply': 'save-selected-columns'}, function () {
    const selectedFields = this.data.columns;

    $.ajax({
      type: 'POST',
      url: AGN.url('/recipient/duplicate/setSelectedFields.action'),
      traditional: true,
      data: {selectedFields}
    }).done(resp => {
      if (resp.success) {
        AGN.Lib.WebStorage.extend('recipient-duplicate-overview', {'fields': selectedFields});
      }
      AGN.Lib.JsonMessages(resp.popups);
    });
  });

});
