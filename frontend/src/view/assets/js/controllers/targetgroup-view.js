AGN.Lib.Controller.new('target-group-view', function ($scope) {

  var Action = AGN.Lib.Action,
    Form = AGN.Lib.Form;

  Action.new({'qb:invalidrules': '#targetgroup-querybuilder'}, function() {
      AGN.Lib.Messages(t('defaults.error'), t('querybuilder.errors.general'), 'alert');
  });

  this.addDomInitializer('target-group-view', function () {
    var $form = Form.getWrapper($(this.el));

    $form.on('click', '[data-toggle-tab]', function (e) {
      //skip empty rule to proper validation while toggling between tabs
      var form = AGN.Lib.Form.get($form);
      form.validatorOptions = $.extend(form.validatorOptions, {skip_empty: true});
      if (!form.valid()) {
        e.preventDefault();
        return false;
      }
    });
  });

  this.addAction({click: 'switch-tab-viewEQL'}, function() {
      var element = this.el,
        form = AGN.Lib.Form.get($(element));

      form.setValueOnce('method', 'viewEQL');
      form.submit();
    });

  this.addAction({click: 'switch-tab-viewQB'}, function() {
    var element = this.el,
      form = AGN.Lib.Form.get($(element));

    form.setValueOnce('method', 'viewQB');
    form.submit();
  });

  this.addAction({click: 'save-wizard-target'}, function() {
    var element = this.el,
        form = AGN.Lib.Form.get($(element));

    if (!containsNotEmptyRule()) {
      AGN.Lib.Messages(t('defaults.error'), t('querybuilder.errors.no_rule'), 'alert');
      return false;
    }

    var isValid = form.validate();
    if (isValid) {
      form.submit();
    }
  })

  function getQbApi() {
    return $('#targetgroup-querybuilder').prop('queryBuilder');
  }

  function containsNotEmptyRule() {
    return getQbApi().containsNotEmptyRule();
  }
})