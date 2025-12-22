AGN.Lib.Controller.new('edit-field-content', function () {

  this.addAction({click: 'calculateRecipients'}, function () {
    const $input = $("#recipients-count");
    const form = AGN.Lib.Form.get(this.el);

    $.ajax({
      url: AGN.url('/recipient/calculate.action'),
      data: {
        "targetId": form.getValue("targetId"),
        "mailinglistId": form.getValue("mailinglistId")
      }
    }).always(data => {
      $input.val(data.count || 0);
    }).fail(() => $input.val('?'))
  });

});
