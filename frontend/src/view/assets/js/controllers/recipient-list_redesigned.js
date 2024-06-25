AGN.Lib.Controller.new('recipient-list', function () {

  const Action = AGN.Lib.Action,
    Messages = AGN.Lib.Messages,
    Form = AGN.Lib.Form,
    Modal = AGN.Lib.Modal,
    Storage = AGN.Lib.Storage;

  const NO_MAILINGLIST_VALUE = '-1';
  const QUERY_BUILDER_SELECTOR = '#targetgroup-querybuilder';
  const QUERY_BUILDER_FILTER_FIELDS = ['firstname', 'lastname', 'email', 'gender'];

  let $filterQueryBuilderBlock;
  let $queryBuilder;

  let initialRules = {};
  let searchFieldsUpdatingRequired = false;
  let maxSelectedColumns;

  Action.new({'qb:invalidrules': QUERY_BUILDER_SELECTOR}, function () {
    Messages.alert('querybuilder.errors.invalid_definition');
  });

  this.addDomInitializer('recipient-list', function () {
    $filterQueryBuilderBlock = $('#filter-query-builder-block');
    $queryBuilder = $(QUERY_BUILDER_SELECTOR);

    maxSelectedColumns = this.config.maxSelectedColumns;
    initialRules = $.extend({}, JSON.parse(this.config.initialRules));
    Storage.restoreChosenFields($getAdvancedFilterToggle());

    // waits till query builder will be initialized
    window.setTimeout(() => {
      adjustAdvancedFilterBtnText();
      adjustUIAccordingToFilterSearchState();
      updateMailinglistDependentFieldsStates();
    }, 0);

    $queryBuilder.on('change', () => {
      $getTargetGroupSaveBtn().toggleClass('hidden', !containsNotEmptyRule());
    });

    $queryBuilder.on('afterClear.queryBuilder', () => {
      $getTargetGroupSaveBtn().toggleClass('hidden', true);
    });

    $queryBuilder.on('afterReset.queryBuilder afterDeleteGroup.queryBuilder afterDeleteRule.queryBuilder afterSetRules.queryBuilder afterUpdateRuleFilter.queryBuilder', () => {
      $getTargetGroupSaveBtn().toggleClass('hidden', !containsNotEmptyRule());
    });
  });

  function $getTargetGroupSaveBtn() {
    return $('#target-group-save-button');
  }

  this.addAction({change: 'toggle-filter'}, function () {
    adjustUIAccordingToFilterSearchState();
    Storage.saveChosenFields(this.el);
  });

  this.addAction({change: 'change-table-columns'}, function () {
    const selectedColumns = this.el.val();
    if (selectedColumns.length > maxSelectedColumns) {
      Messages.warn('recipient.maxColumnsSelected');
      selectedColumns.pop();
      this.el.val(selectedColumns).trigger('change');
    }
  });

  function adjustUIAccordingToFilterSearchState() {
    const isAdvancedSearchEnabled = isAdvancedSearch();
    $('#basic-filters-block').toggleClass('hidden', isAdvancedSearchEnabled);
    $('#advanced-filter-btn').toggleClass('hidden', !isAdvancedSearchEnabled);
  }

  function isAdvancedSearch() {
    return $getAdvancedFilterToggle().is(':checked');
  }

  function $getAdvancedFilterToggle() {
    return $('#use-advanced-filter');
  }

  this.addAction({click: 'advanced-filter'}, function () {
    const $modal = Modal.fromTemplate('recipient-advanced-filter-modal', {showTargetSaveBtn: containsNotEmptyRule()});
    $modal.on('modal:close', () => $queryBuilder.appendTo($filterQueryBuilderBlock));

    $queryBuilder.appendTo($modal.find('.modal-body'));
  });

  this.addAction({click: 'create-new-target'}, function () {
    getQbApi()?.clearErrors();

    if (validateQbRules()) {
      AGN.Lib.Confirm.createFromTemplate({rules: $('#queryBuilderRules').val()}, 'new-targetgroup-modal')
        .done(resp => getForm().updateHtml(resp));
    }
  });

  this.addAction({click: 'set-advanced-filter'}, function () {
    if (validateQbRules(true)) {
      Modal.getInstance(this.el).hide();
      performSearch();
    }
  });

  function validateQbRules(skipEmpty = false) {
    const validationEvent = $.Event('qb:validation');
    $queryBuilder.trigger(validationEvent, {ignore_qb_validation: false, skip_empty: skipEmpty});

    return !validationEvent.isDefaultPrevented();
  }

  function adjustAdvancedFilterBtnText() {
    const $btnTextLabel = $('#advanced-filter-btn').find('span');
    const activeTextSuffix = ` (${t('defaults.active')})`;

    $btnTextLabel.text($btnTextLabel.text().replace(activeTextSuffix, ''));
    if (hasQueryBuilderRules()) {
      $btnTextLabel.text($btnTextLabel.text() + activeTextSuffix);
    }
  }

  function hasQueryBuilderRules() {
    const avancedFilterRules = $queryBuilder.find('#queryBuilderRules').val();
    const rules = JSON.parse(avancedFilterRules).rules;
    return Array.isArray(rules) && rules.length > 0;
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
    performSearch();
  });

  function performSearch() {
    if (!isAdvancedSearch()) {
      updateQbRulesBasedOnBasicFields();
    }
    submitForm({ignore_qb_validation: false});
  }

  function submitForm(options) {
    const form = getForm();
    form.submit('', _.merge({ignore_qb_validation: true, skip_empty: true}, form.validatorOptions, options));
  }

  function getForm() {
    return Form.get($('#listForm'));
  }

  function updateQbRulesBasedOnBasicFields() {
    const rules = QUERY_BUILDER_FILTER_FIELDS.map(field => {
      const fieldType = getGenericFieldType(field);
      const operator = fieldType === 'string' ? 'contains' : 'equal';

      let fieldValue = $getFilterField(field).val().trim();
      if (fieldType === 'number' && fieldValue !== '') {
        fieldValue = Number(fieldValue);
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

      fieldNames.forEach(function (field) {
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
