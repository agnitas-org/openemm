AGN.Lib.Controller.new('company-view', function () {

  this.addDomInitializer('company-view', function () {
    updatePasswordPolicyRule();
  });

  this.addAction({click: 'create-postal-field'}, function () {
    const url = this.el.attr('href');
    const form = AGN.Lib.Form.get(this.el);

    form.jqxhr().done(() => AGN.Lib.Page.reload(url));
  });

  this.addAction({change: 'update-policy'}, function () {
    updatePasswordPolicyRule();
  });

  function updatePasswordPolicyRule() {
    const policy = $('#passwordPolicy').val();
    $('#password').attr('data-rule', policy);
  }

  this.addAction({change: 'change-password-expire'}, function () {
    this.el.find('[data-default-password-expire-option="false"]').remove();
  });

});
