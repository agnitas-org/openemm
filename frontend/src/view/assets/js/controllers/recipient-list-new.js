AGN.Lib.Controller.new('recipient-list-new', function () {

  var Action = AGN.Lib.Action,
    Modal = AGN.Lib.Modal,
    Form = AGN.Lib.Form,
    Messages = AGN.Lib.Messages,
    JsonMessages = AGN.Lib.JsonMessages,
    Storage = AGN.Lib.Storage;

  Action.new({'qb:invalidrules': '#targetgroup-querybuilder'}, function() {
      Messages(t('defaults.error'), t('querybuilder.errors.invalid_definition'), 'alert');
  });

  function checkAndRedirect(link, recipientId, checkLimitAccessUrl, viewUrl) {
    checkLimitAccessUrl = checkLimitAccessUrl.replace('{RECIPIENT_ID}', recipientId);
    $.ajax({
        url: checkLimitAccessUrl,
        type: 'POST'
    }).done(function(response) {
      if (response && response.hasOwnProperty('accessAllowed')) {
        var accessible = response.accessAllowed;

        if (accessible) {
          JsonMessages({}, true);
          window.location.href = viewUrl.replace('{RECIPIENT_ID}', recipientId);
        } else {
          Messages(t('Error'), t('error.recipient.restricted'), 'alert');
        }
      } else {
        console.error('Could not check access for recipient with id: ' + recipientId);
      }
    }).fail(function () {
        console.error('Could not check access for recipient with id: ' + recipientId);
    });
  }

  this.addDomInitializer('recipient-list-new', function ($scope) {
    var config = this.config;
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

    $scope.on('click', '.js-table .table-link a', function(e) {
      e.preventDefault();
      e.preventViewLoad = true;
      var link = $(this);
      var recipientId = link.closest('tr').find('[data-recipient-id]').data('recipient-id');
      checkAndRedirect(link, recipientId, config.CHECK_LIMITACCESS_URL, config.VIEW_URL);
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

  this.addAction({validation: 'search-recipient'}, function() {
    if (this.data && this.data.ignore_qb_validation) {
      resetNotAppliedRules();
    }
  });

  function resetNotAppliedRules() {
    var api = getQbApi();
    api.clearErrors();
    api.setRules(initialRules);
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

  this.addAction({click: 'refresh-basic-search'}, function () {
    updateQbRulesBasedOnBasicFields();
    submitForm($(this.el), {ignore_qb_validation: false});
  });

  this.addAction({click: 'refresh-advanced-search'}, function () {
    updateBasicFieldsBasedOnQbRules();
    submitForm($(this.el), {ignore_qb_validation: false});
  });

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
    var qbRules = api.getRules({allow_invalid: true, skip_empty: true});

    return rules.reduce(function (accumulator, rule) {
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
