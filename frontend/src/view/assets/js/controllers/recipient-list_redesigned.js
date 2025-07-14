AGN.Lib.Controller.new('recipient-list', function () {

  const Action = AGN.Lib.Action,
    Messages = AGN.Lib.Messages,
    Form = AGN.Lib.Form,
    Storage = AGN.Lib.Storage;

  const NO_MAILINGLIST_VALUE = '-1';
  const QUERY_BUILDER_SELECTOR = '#targetgroup-querybuilder';
  const QUERY_BUILDER_FILTER_FIELDS = ['firstname', 'lastname', 'email', 'gender'];

  let $queryBuilder;

  let initialRules = {};
  let searchFieldsUpdatingRequired = false;

  Action.new({'qb:invalidrules': QUERY_BUILDER_SELECTOR}, function () {
    Messages.alert('querybuilder.errors.invalid_definition');
  });

  this.addDomInitializer('recipient-list', function () {
    $queryBuilder = $(QUERY_BUILDER_SELECTOR);

    initialRules = $.extend({}, JSON.parse(this.config.initialRules));
    if (!this.config.forceShowAdvancedSearchTab) {
      Storage.restoreChosenFields($getAdvancedFilterToggle());
    }

    adjustUIAccordingToFilterSearchState();
    updateMailinglistDependentFieldsStates();

    $queryBuilder.on('afterClear.queryBuilder', () => updateTargetSaveBtnVisibility(true));

    $queryBuilder.on(
      'change afterReset.queryBuilder afterDeleteGroup.queryBuilder afterDeleteRule.queryBuilder afterSetRules.queryBuilder afterUpdateRuleFilter.queryBuilder',
      () => updateTargetSaveBtnVisibility()
    );
  });

  this.addAction({change: 'toggle-filter'}, function () {
    adjustUIAccordingToFilterSearchState();
    Storage.saveChosenFields(this.el);
  });

  this.addAction({'table-column-manager:apply': 'save-selected-columns'}, function () {
    const selectedFields = this.data.columns;

    $.ajax({
      type: 'POST',
      url: AGN.url('/recipient/setSelectedFields.action'),
      traditional: true,
      data: {selectedFields}
    }).done(resp => {
      if (resp.success) {
        AGN.Lib.WebStorage.extend('recipient-overview', {'fields': selectedFields});
      }
      AGN.Lib.JsonMessages(resp.popups);
    });
  });

  function adjustUIAccordingToFilterSearchState() {
    const isAdvancedSearchEnabled = isAdvancedSearch();
    $('#basic-filters-block').toggleClass('hidden', isAdvancedSearchEnabled);
    $('#filter-query-builder-block').toggleClass('hidden', !isAdvancedSearchEnabled);
    updateTargetSaveBtnVisibility();
  }

  function updateTargetSaveBtnVisibility(forceHide = false) {
    $('#target-group-save-button').toggleClass('hidden', forceHide || !isAdvancedSearch() || !containsNotEmptyRule());
  }

  function isAdvancedSearch() {
    return $getAdvancedFilterToggle().is(':checked');
  }

  function $getAdvancedFilterToggle() {
    return $('#use-advanced-filter');
  }

  this.addAction({click: 'create-new-target'}, function () {
    getQbApi()?.clearErrors();

    if (validateQbRules()) {
      AGN.Lib.Confirm.from('new-targetgroup-modal', {rules: $('#queryBuilderRules').val()})
        .done(resp => getForm().updateHtml(resp));
    }
  });

  function validateQbRules(skipEmpty = false) {
    const validationEvent = $.Event('qb:validation');
    $queryBuilder.trigger(validationEvent, {ignore_qb_validation: false, skip_empty: skipEmpty});

    return !validationEvent.isDefaultPrevented();
  }

  this.addAction({change: 'change-mailinglist-id'}, updateMailinglistDependentFieldsStates);

  function updateMailinglistDependentFieldsStates() {
    const mailinglistId = $getFilterField('mailinglist').val();

    const disable = mailinglistId === NO_MAILINGLIST_VALUE;
    $getFilterField('type').prop('disabled', disable);
    $getFilterField('status').prop('disabled', disable);
  }

  //necessary to support keydown event submit
  this.addAction({enterdown: 'search-recipient'}, function () {
    searchFieldsUpdatingRequired = true;
  });
  this.addAction({validation: 'search-recipient'}, function () {
    if (searchFieldsUpdatingRequired) {
      const validationOptions = this.data;
      this.data = _.merge({}, validationOptions, {ignore_qb_validation: false, skip_empty: false});
    } else if (this.data && this.data.ignore_qb_validation) {
      resetNotAppliedRules();
    }
  });

  function resetNotAppliedRules() {
    const api = getQbApi();
    if (api) {
      api.clearErrors();
      api.setRules(initialRules);
      updateBasicFieldsBasedOnQbRules();
    }
  }

  this.addAction({submission: 'search-recipient'}, function () {
    if (searchFieldsUpdatingRequired) {
      updateQbRulesBasedOnBasicFields();
      submitForm({ignore_qb_validation: false});
    }
  });

  this.addAction({click: 'search'}, function () {
    if (!isAdvancedSearch()) {
      updateQbRulesBasedOnBasicFields();
    }
    submitForm({ignore_qb_validation: false});
  });

  function submitForm(options) {
    const form = getForm();
    form.setActionOnce(AGN.url('/recipient/search.action'));
    form.submit('', _.merge({ignore_qb_validation: true, skip_empty: true}, form.validatorOptions, options));
  }

  function getForm() {
    return Form.get($('#recipients-overview'));
  }

  function updateQbRulesBasedOnBasicFields() {
    const rules = QUERY_BUILDER_FILTER_FIELDS.map(field => {
      const fieldType = getGenericFieldType(field);
      const operator = fieldType === 'string' ? 'like' : 'equal';

      let fieldValue = $getFilterField(field).val().trim();
      if (fieldType === 'number' && fieldValue !== '') {
        fieldValue = Number(fieldValue);
      } else {
        fieldValue = fieldValue ? `%${fieldValue}%` : '';
      }

      return {id: field, value: fieldValue, operator: operator};
    });

    return addOrReplaceRules(rules, true);
  }

  function getGenericFieldType(field) {
    const type = getQbApi().getFilterById(field).type;
    return $.fn.queryBuilder.constructor.types[type];
  }

  function addOrReplaceRules(rules, recursively) {
    const api = getQbApi();

    if (!api) {
      return false;
    }

    return rules.reduce((accumulator, rule) => {
      const qbRules = api.getRules({allow_invalid: true, skip_empty: true});

      if (!rule.id) {
        return accumulator;
      }

      if (typeof rule.value === 'number' || rule.value) {
        return api.setOrReplaceRule(qbRules, rule, recursively) === true || accumulator;
      }

      return api.deleteRuleByField(null, rule.id, recursively) === true || accumulator;
    }, false);
  }

  function updateBasicFieldsBasedOnQbRules() {
    const valueMap = getQbFieldsValues();
    _.each(QUERY_BUILDER_FILTER_FIELDS, field => $getFilterField(field).val(valueMap[field]));
  }

  function getQbFieldsValues(fieldNames = QUERY_BUILDER_FILTER_FIELDS) {
    const api = getQbApi();
    const result = {};

    if (api) {
      const qbRules = api.getRules({allow_invalid: true, skip_empty: true});

      fieldNames.forEach(field=> {
        const rule = api.findRuleByField(qbRules, field, true);
        result[field] = rule?.value || '';
      });
    }
    return result;
  }

  function containsNotEmptyRule() {
    const api = getQbApi();
    return api ? api.containsNotEmptyRule() : false;
  }

  function $getFilterField(field) {
    return $(`#filter-${field}`);
  }

  function getQbApi() {
    return $queryBuilder.prop('queryBuilder');
  }
});
