AGN.Lib.Controller.new('recipient-list-new', function () {

  var Action = AGN.Lib.Action,
    Modal = AGN.Lib.Modal,
    Form = AGN.Lib.Form,
    Messages = AGN.Lib.Messages,
    Storage = AGN.Lib.Storage;

  var initialRules;
  var searchFieldsUpdatingRequired = false;

  Action.new({'qb:invalidrules': '#targetgroup-querybuilder'}, function() {
      Messages(t('defaults.error'), t('querybuilder.errors.invalid_definition'), 'alert');
  });

  this.addDomInitializer('recipient-list-new', function ($scope) {
    var config = this.config;
    searchFieldsUpdatingRequired = false;
    initialRules = $.extend({}, JSON.parse(config.initialRules));

    $('#basicSearch').on('click', function () {
      $('input[name=advancedSearch]').val(false);
      resetNotAppliedRules();
    });

    $('#advancedSearch').on('click', function () {
      $('input[name=advancedSearch]').val(true);
    });

    var $queryBuilder = $('#targetgroup-querybuilder');
    $queryBuilder.on('change', function() {
      $('#target-group-save-button').toggleClass('hidden', !containsNotEmptyRule());
    });

    $queryBuilder.on('afterClear.queryBuilder', function() {
      $('#target-group-save-button').toggleClass('hidden', true);
    });

    $queryBuilder.on(
      'afterReset.queryBuilder ' +
      'afterDeleteGroup.queryBuilder ' +
      'afterDeleteRule.queryBuilder ' +
      'afterSetRules.queryBuilder ' +
      'afterUpdateRuleFilter.queryBuilder', function(e) {
      $('#target-group-save-button').toggleClass('hidden', !containsNotEmptyRule());
    });

    var basicSearchActive = $('#basicSearch').hasClass('tab active');
    var advancedSearchActive = $('#advancedSearch').hasClass('tab active');
    var duplicateAnalysisActive = $('#duplicateAnalysis').hasClass('tab active');
    if (advancedSearchActive) {
      $('input[name=advancedSearch]').val(true);
    }
    if (basicSearchActive) {
      $('input[name=advancedSearch]').val(false);
    }
    if (duplicateAnalysisActive) {
      var controls = $('#recipientForm').find('.table-controls .well');
      controls.first().html(controls.last().html())
    }
  });

  //necessary to support keydown event submit
  this.addAction({enterdown: 'search-recipient'}, function() {
    searchFieldsUpdatingRequired = true;
  });

  this.addAction({validation: 'search-recipient'}, function() {
    if (searchFieldsUpdatingRequired) {
      var validationOptions = this.data;
      this.data =  _.merge({}, validationOptions, {ignore_qb_validation: false, skip_empty: false});
    } else if (this.data && this.data.ignore_qb_validation) {
      resetNotAppliedRules();
    }
  });

  this.addAction({submission: 'search-recipient'}, function() {
    if (searchFieldsUpdatingRequired) {
      var isAdvancedSearch = $('input[name=advancedSearch]').val();
      if (isAdvancedSearch == true) {
        updateBasicFieldsBasedOnQbRules();
      } else {
        updateQbRulesBasedOnBasicFields();
      }
      submitForm($(this.el), {ignore_qb_validation: false});
    }
  });

  function resetNotAppliedRules() {
    var api = getQbApi();
    api.clearErrors();
    api.setRules(initialRules);
    updateBasicFieldsBasedOnQbRules();
  }

  this.addAction({click: 'target-group-save'}, function() {
    getQbApi().clearErrors();

    var form = Form.get($(this.el));
    var isValid = form.validate(_.merge({}, form.validatorOptions, {ignore_qb_validation: false, skip_empty: false}));

    if (isValid) {
      Modal.createFromTemplate({}, 'target-group-save');
    }
  });

  this.addAction({change: 'change-ml-field'}, function () {
    var value = $(this.el).select2('val');
    disabledMlDependentFields(value == '-1');

    //synchronize advanced search field value
    $('#search_mailinglist_advanced').select2('val', value);
  });

  this.addAction({change: 'change-ml-field-advanced'}, function () {
    var value = $(this.el).select2('val');
    disabledMlDependentFields(value == '-1');

    //synchronize basic search field value
    $('#search_mailinglist').select2('val', value);
  });

  function disabledMlDependentFields(disabled) {
    $('#search_recipient_type').prop('disabled', disabled);
    $('#search_recipient_state').prop('disabled', disabled);

    $('#search_recipient_type_advanced').prop('disabled', disabled);
    $('#search_recipient_state_advanced').prop('disabled', disabled);
  }

  this.addAction({change: 'change-target-group'}, function () {
    var value = $('#search_targetgroup').val();
    $('#search_targetgroup_advanced').select2('val', value);
  });

  this.addAction({change: 'change-recipient-type'}, function () {
    var value = $('#search_recipient_type').val();
    $('#search_recipient_type_advanced').select2('val', value);
  });

  this.addAction({change: 'change-user-status'}, function () {
    var value = $('#search_recipient_state').val();
    $('#search_recipient_state_advanced').select2('val', value);
  });

  this.addAction({change: 'change-target-group-advanced'}, function () {
    var value = $('#search_targetgroup_advanced').val();
    $('#search_targetgroup').select2('val', value);
  });

  this.addAction({change: 'change-recipient-type-advanced'}, function () {
    var value = $('#search_recipient_type_advanced').val();
    $('#search_recipient_type').select2('val', value);
  });

  this.addAction({change: 'change-user-status-advanced'}, function () {
    var value = $('#search_recipient_state_advanced').val();
    $('#search_recipient_state').select2('val', value);
  });

  this.addAction({click: 'choose-advanced-search'}, function () {
    if (updateQbRulesBasedOnBasicFields()) {
      submitForm($(this.el), {ignore_qb_validation: false});
    }
  });

  this.addAction({click: 'toggle-recipient-tab'}, function () {
    //imitate tab toggling for recipient tab overview
    //necessary to redirect to specific link
    var $el = $(this.el);
    var siblings = $('[data-action="toggle-recipient-tab"]');
    _.each(siblings, function(sibling){
      Storage.set('toggle_tab' + $(sibling).data('tab-id'), {hidden: true});
    });

    Storage.set('toggle_tab' + $el.data('tab-id'), {hidden: false});
    var url = $el.data('url');
    if (!!url) {
      window.location.href = url;
    }
  });

  this.addAction({click: 'reset-search'}, function () {
    /* cleaning basic search */
    $('#search_first_name').val('');
    $('#search_name').val('');
    $('#search_email').val('');
    $('#search_mailinglist').select2('val', 0);
    $('#search_targetgroup').select2('val', 0);
    $('#search_recipient_type').select2('val', '');
    $('#search_recipient_state').select2('val', 0);

    /* cleaning advanced search */
    $('#search_mailinglist_advanced').select2('val', 0);
    $('#search_targetgroup_advanced').select2('val', 0);
    $('#search_recipient_type_advanced').select2('val', '');
    $('#search_recipient_state_advanced').select2('val', 0);

    resetQueryBuilderRules();

    submitForm($(this.el), {ignore_qb_validation: false});
  });

  /**
   * @deprecated remove after GWUA-4769 test successfully
   */
  this.addAction({click: 'refresh-basic-search'}, function () {
    updateQbRulesBasedOnBasicFields();
    submitForm($(this.el), {ignore_qb_validation: false});
  });

  /**
   * @deprecated remove after GWUA-4769 test successfully
   */
  this.addAction({click: 'refresh-advanced-search'}, function () {
    updateBasicFieldsBasedOnQbRules();
    submitForm($(this.el), {ignore_qb_validation: false});
  });

  this.addAction({click: 'createNewTarget'}, function() {
    getQbApi().clearErrors();
    var form = Form.get($(this.el));
    var isValid = form.validate(_.merge({}, form.validatorOptions, {ignore_qb_validation: false, skip_empty: false}));

    if (isValid) {
      AGN.Lib.Confirm.createFromTemplate({rules: form.get$().find('[name="searchQueryBuilderRules"]').val()}, 'new-targetgroup-modal')
        .done(function(resp) {
          form.updateHtml(resp);
        });
    }
  });

  this.addAction({change: 'change-mailinglist-id'}, function() {
    var mlId = $(this.el).select2('val');
    disabledMlDependentFields(mlId == '-1');
  });

  this.addAction({click: 'refresh-basic-search-new'}, function () {
    updateQbRulesBasedOnBasicFields();
    disableIndependentFields(false);
    submitForm($(this.el), {ignore_qb_validation: false});
  });

  this.addAction({click: 'refresh-advanced-search-new'}, function() {
    updateBasicFieldsBasedOnQbRules();
    disableIndependentFields(true);
    submitForm($(this.el), {ignore_qb_validation: false});
  });

  function disableIndependentFields(isAdvanced) {
    var prefix = isAdvanced ? '' : '_advanced';
    var fieldsToDisable = ['#search_mailinglist', '#search_targetgroup', '#search_recipient_type', '#search_recipient_state'];
    fieldsToDisable.forEach(function(selector) {
      $(selector + prefix).prop('disabled', true);
    })
  }

  function updateQbRulesBasedOnBasicFields() {
    var fieldsMap = {
      'firstname': $('#search_first_name').val().trim(),
      'lastname': $('#search_name').val().trim(),
      'email': $('#search_email').val().trim()
    };

    var rules = Object.keys(fieldsMap).map(function(key) {
      return {id: key, value: fieldsMap[key], operator: 'like'};
    });

    return addOrReplaceRules(rules, true);
  }

  function updateBasicFieldsBasedOnQbRules() {
    var valueMap = getQbFieldsValue(['firstname', 'lastname', 'email']);
    $('#search_first_name').val(valueMap.firstname);
    $('#search_name').val(valueMap.lastname);
    $('#search_email').val(valueMap.email);
  }

  function submitForm($el, options) {
    var form = Form.get($el);
    form.submit('', _.merge({ignore_qb_validation: true, skip_empty: true}, form.validatorOptions, options));
  }

  function getQbApi() {
    return $('#targetgroup-querybuilder').prop('queryBuilder');
  }

  function containsNotEmptyRule() {
    return getQbApi().containsNotEmptyRule();
  }

  function resetQueryBuilderRules() {
    var api = getQbApi();
    api.clearErrors();
    return api.reset()
  }

  function getQbFieldsValue(fieldNames) {
    var api = getQbApi();
    var qbRules = api.getRules({allow_invalid: true, skip_empty: true});

    var result = {};
    fieldNames.forEach(function(field) {
      var rule = api.findRuleByField(qbRules, field, true);
      result[field] = rule ? rule.value : '';
    });
    return result;
  }

  function addOrReplaceRules(rules, recursively) {
    var api = getQbApi();

    return rules.reduce(function (accumulator, rule) {
      var qbRules = api.getRules({allow_invalid: true, skip_empty: true});
      if (rule.id) {
        if (rule.value) {
          return api.setOrReplaceRule(qbRules, rule, recursively) === true || accumulator;
        } else {
          return api.deleteRuleByField(null, rule.id, recursively) === true || accumulator;
        }
      } else {
        return accumulator;
      }
    }, false);
  }
});
