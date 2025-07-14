AGN.Lib.Controller.new('import-modal', function() {
  this.addAction({submission: 'import-for-workflow'}, function () {
    const form = AGN.Lib.Form.get(this.el);
    if (!form.validate()) {
      return;
    }

    form.jqxhr().done(resp => {
      if (resp.success) {
        AGN.Lib.Messaging.send('workflow:mailingCreated', resp.data);
        AGN.Lib.Modal.getInstance(this.el).hide();
      } else {
        AGN.Lib.JsonMessages(resp.popups, true);
      }
    });
  });
});
