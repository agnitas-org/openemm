AGN.Lib.Controller.new('company-view', function() {

  this.addAction({
    click: 'create-postal-field'
  }, function() {
    const url = this.el.attr('href');
    const form = AGN.Lib.Form.get(this.el);

    form.jqxhr().done(function(resp) {
      AGN.Lib.Page.reload(url);
    });
  });

});
