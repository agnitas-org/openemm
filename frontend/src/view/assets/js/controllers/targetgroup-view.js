AGN.Lib.Controller.new('target-group-view', function ($scope) {

  var Action = AGN.Lib.Action,
    Form = AGN.Lib.Form;

  Action.new({'qb:invalidrules': '#targetgroup-querybuilder'}, function() {
      AGN.Lib.Messages(t('defaults.error'), t('querybuilder.errors.general'), 'alert');
  });

  this.addDomInitializer('target-group-view', function () {
    var $el = $(this.el);

    $('[data-toggle-tab]').on('click', function (e) {
      //skip empty rule to proper validation while toggling between tabs
      var isValid = Form.get($el).validate({skip_empty: true});
      if (!isValid) {
        e.preventDefault();
        return false;
      }
    });
  });

  this.addAction({click: 'switch-tab-viewEQL'}, function() {
      var element = this.el,
        form = AGN.Lib.Form.get($(element));

      form.setValueOnce('viewFormat', 'EQL');
      form.submit('', {skip_empty: true});
  });

  this.addAction({click: 'switch-tab-viewQB'}, function() {
    var element = this.el,
      form = AGN.Lib.Form.get($(element));

    form.setValueOnce('viewFormat', 'QUERY_BUILDER');
    form.submit('', {skip_empty: true});
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