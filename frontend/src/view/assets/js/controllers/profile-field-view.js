AGN.Lib.Controller.new('profile-field-view', function() {
  const Form = AGN.Lib.Form;

  this.addDomInitializer('profile-field-view', function($elem) {
    var data = $elem.json();

    $.i18n.load(data.translations);
  });

  this.addAction({submission: 'submit-profile-field-save'}, function () {
    const $form = this.el;
    const $stateIcons = $("span[class*='icon-state-']");

    $form.find($stateIcons).each(function() {
      const $e = $(this);
      $e.show();
      $e.closest('.form-group').addClass('has-feedback');
    });

    Form.get($form).submit();
  });
});
