AGN.Lib.Controller.new('profile-field-view', function() {
  const Form = AGN.Lib.Form;
  var config;

  this.addDomInitializer('profile-field-view', function() {
    config = this.config;
  });

  this.addAction({submission: 'submit-profile-field-save'}, function () {
    const $form = this.el;
    const $stateIcons = $("span[class*='icon-state-']");

    $form.find($stateIcons).each(function() {
      const $e = $(this);
      $e.show();
      $e.closest('.form-group').addClass('has-feedback');
    });

    const form = Form.get($form);
    if (config.targetUrl) {
      form.setValueOnce('targetUrl', config.targetUrl);
    }

    form.submit();
  });
});
