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

  this.addAction({change: 'change-password-expire'}, function () {
    this.el.find('[data-default-password-expire-option="false"]').remove();
  });

});
