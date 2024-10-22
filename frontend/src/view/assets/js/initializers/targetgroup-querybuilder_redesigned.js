(() => {

  const READ_ONLY_PARAMS = {
    header_readonly: true,
    filter_readonly: true,
    condition_readonly: true,
    operator_readonly: true,
    value_readonly: true,
    no_delete: true,
    no_add_rule: true,
    no_sortable: true,
    no_add_group: true
  };

  AGN.Lib.DomInitializer.new('target-group-query-builder', function ($needle) {
    const config = this.config;
    const $form = AGN.Lib.Form.getWrapper($needle);
    const qbSelector = '#targetgroup-querybuilder';
    const queryBuilders = $(qbSelector);

    const mailTrackingAvailable = config.mailTrackingAvailable;
    const isTargetGroupLocked = config.isTargetGroupLocked;
    const helpLanguage = config.helpLanguage;

    _.each(queryBuilders, el => {
      const $el = $(el);
      const operator_groups = buildOperatorGroups();

      const qbFilters = readFiltersFromJson(config.queryBuilderFilters, {mail_tracking_available: mailTrackingAvailable});
      const rules = readRulesFromJson(JSON.parse(config.queryBuilderRules), isTargetGroupLocked);

      $el.queryBuilder({
        filters: qbFilters,
        allow_empty: true,
        operators: operator_groups,
        helpLanguage: helpLanguage,
        readonly: isTargetGroupLocked,
        plugins: {
          'sortable': {
            inherit_no_drop: true,
            inherit_no_sortable: true
          }
        },
        rules: rules
      });

      AGN.Lib.CoreInitializer.run('select', $el);

      $el.on('afterSetRules.queryBuilder afterUpdateRuleFilter.queryBuilder afterCreateRuleFilters.queryBuilder afterAddRule.querybuilder', function (event) {
        AGN.Lib.CoreInitializer.run('select', $(this));
      });

      $el.on('link-values-set', function (e) {
        AGN.Lib.CoreInitializer.run('select', $(e.target));
      });

      $el.on('mailing-values-set values-set ', function (e) {
        AGN.Lib.CoreInitializer.run('select', $(e.target));
      });

      $el.on('change-date-format', function (e) {
        AGN.Lib.CoreInitializer.run('select', $(e.target));
      });

      // Prevent form submission on enter press when QB inputs are in focus.
      $el.on('enterdown', 'input', function (e) {
        return false;
      });

    });

    const validationCallback = function (e, options) {
      // ignore_qb_validation is used to prevent validate QB rules
      // this option prevent update query builder rules field
      options = $.extend({ignore_qb_validation: false, skip_empty: false}, options);
      _.each($(qbSelector), function (el) {
        const $el = $(el);
        const qb = $el.prop('queryBuilder');
        if (!validate(e, qb, options, $el)) {
          return false;
        }
      });
    }

    queryBuilders.on('qb:validation', validationCallback);
    $form.off().on('validation', validationCallback);

    function validate(e, qb, options, $el) {
      qb.clearInitialRuleSettings();

      if (options.ignore_qb_validation) {
        //don't update query builder rule
        return true;
      } else if (qb.validate({skip_filter_validation: false, skip_empty: options.skip_empty})) {
        $el.find('#queryBuilderRules').val(getRulesAsJson(qb, options.skip_empty));
        return true;
      } else {
        $el.trigger('qb:invalidrules');
        e.preventDefault();
        return false;
      }
    }

    function getRulesAsJson(qb, skipEmpty) {
      return JSON.stringify(qb.getRules({allow_invalid: true, skip_empty: skipEmpty}));
    }

    function setValue($element, value) {
      if ($element.is('select')) {
        AGN.Lib.Select.get($element).selectValueOrSelectFirst(value);
      } else {
        $element.val(value);
      }
    }

    function readFiltersFromJson(filtersJson, options) {
      if (filtersJson === null) {
        filtersJson = [{
          id: '?',
          label: '?',
          type: 'string'
        }];
      } else {
        const ids = {};

        filtersJson = filtersJson.filter(function (e) {
          if (ids[e.id]) {
            return false;
          }

          ids[e.id] = true;
          return true;
        });
      }

      augmentIndependentFields(filtersJson, options);

      return $.extend([], filtersJson);
    }

    function addReadOnlyFlags(options) {
      if (options) {
        options.flags = READ_ONLY_PARAMS;

        _.each(options.rules, function (rule) {
          rule.flags = READ_ONLY_PARAMS;
          if (rule.hasOwnProperty('condition')) {
            addReadOnlyFlags(rule)
          }
        });
      }
      return options;
    }

    function augmentIndependentFields(result, options) {
      //An additional properties for independent fields

      options = $.extend({mail_tracking_available: false}, options);
      const getEmptyMailingSelect = function (rule) {
        return $(`
          <div class="qb-select-container">
            <select id="${rule.id}" class="form-control mailings js-select" data-select-options="dropdownAutoWidth: true"></select>
          </div>
        `);
      };

      const getEmptyAutoImportSelect = function (rule) {
        const $select = $('<select class="form-control auto-imports js-select"></select>');
        $select.attr("id", rule.id);
        return $select;
      };

      const mailingsValueGetter = function (rule) {
        return rule.$el.find('.mailings :selected').val();
      };

      const autoImportsValueGetter = function (rule) {
        return rule.$el.find('.auto-imports :selected').val();
      };

      const generateValueSetter = function (selector) {
        const savedValues = {}, savedRules = {}, defaultValue = -1;

        $(qbSelector).on('values-set', selector, function (event) {
          if ($.isEmptyObject(savedRules)) {
            setValue($(event.target), defaultValue);
          } else {
            const id = $(event.target).attr('id'),
              savedRule = savedRules[id],
              $inputs = savedRule ? savedRule.$el.find(selector) : $(event.target);
            setValue($inputs, savedValues[id] || defaultValue);
          }
        });

        return function (rule, value) {
          const $inputs = rule.$el.find(selector);
          savedValues[rule.id] = value;
          savedRules[rule.id] = rule;
          setValue($inputs, value);
        }
      };

      const mailingValueSetter = generateValueSetter('select.mailings');
      const autoImportValueSetter = generateValueSetter('select.auto-imports');

      const augmentations = {
        'finished auto-import': {
          type: 'string',
          input_event: 'values-setup-finished',
          input: getEmptyAutoImportSelect,
          values: function ($ruleInput) {
            requestAutoImports($ruleInput).done(function () {
              $ruleInput.trigger('values-setup-finished');
            });
          },
          valueSetter: autoImportValueSetter,
          valueGetter: autoImportsValueGetter,
          canBeNegated: true,
          skipOperator: true
        },
        'received mailing': {
          type: 'string',
          input_event: 'values-setup-finished',
          input: getEmptyMailingSelect,
          values: function ($ruleInput) {
            const $mailings = $ruleInput.find('select.mailings');
            requestMailings($mailings).done(() => $mailings.trigger('values-setup-finished'));
          },
          valueSetter: mailingValueSetter,
          valueGetter: mailingsValueGetter,
          canBeNegated: true,
          skipOperator: true
        },
        'opened mailing': {
          type: 'string',
          input_event: 'values-setup-finished',
          input: getEmptyMailingSelect,
          values: function ($ruleInput) {
            const $mailings = $ruleInput.find('select.mailings');
            requestMailings($mailings).done(() => $mailings.trigger('values-setup-finished'));
          },
          valueSetter: mailingValueSetter,
          valueGetter: mailingsValueGetter,
          canBeNegated: options.mail_tracking_available,
          skipOperator: true
        },
        'clicked in mailing': {
          type: 'string',
          input_event: 'values-setup-finished',
          valueSelector: function (ruleId, index) {
            const ids = ['mailingID', 'linkID'];
            return ruleId + '_' + ids[index];
          },
          input: function (rule) {
            return $(`
             <div class="qb-select-container">
               <select id="${rule.filter.valueSelector(rule.id, 0)}" class="form-control js-select mailings" data-rule-id="${rule.id}" data-select-options="dropdownAutoWidth: true"></select>
             </div>
             <label class="form-label d-flex align-items-center">Links</label>
             <div class="qb-select-container">
               <select id="${rule.filter.valueSelector(rule.id, 1)}" class="form-control js-select links" data-rule-id="${rule.id}" data-select-options="dropdownAutoWidth: true"></select>
             </div>
            `);
          },
          values: function ($ruleInput) {
            const $mailings = $ruleInput.find('select.mailings');
            const mailings = AGN.Lib.Select.get($mailings);
            $mailings.change(function () {
              const mailingID = mailings.getSelectedValue();
              requestMailingLinks(mailingID, function (data) {
                const $links = $ruleInput.find('select.links');
                const links = AGN.Lib.Select.get($links);
                const options = [{id: -1, text: t('querybuilder.common.anyLink')}];

                $.each(data, function (key, value) {
                  options.push({id: value.id, text: value.url});
                });

                links.setOptions(options);
                $links.trigger('link-values-set');
              });
            });
            requestMailings($mailings, function (data) {
              const options = [];
              data.forEach(function (element) {
                options.push({id: element.mailingID, text: element.shortname})
              });

              mailings.setOptions(options);
              $mailings.trigger('mailing-values-set');
            });
          },
          valueGetter: function (rule) {
            const mailingID = 0, linkID = 1,
              value = [];

            value[mailingID] = rule.$el.find('#' + rule.filter.valueSelector(rule.id, mailingID)).val();
            value[linkID] = rule.$el.find('#' + rule.filter.valueSelector(rule.id, linkID)).val();
            return value;
          },
          valueSetter: (function () {
            const mailingID = 0, linkID = 1,
              savedValues = {}, savedRules = {};
            const valueSetter = function (rule, value) {
              savedValues[rule.id] = value;
              savedRules[rule.id] = rule;
            };
            //Optimize
            $(qbSelector).on('mailing-values-set', function (event) {
              let $select = $(event.target),
                id = $select.data('rule-id'),
                savedRule = savedRules[id], $mailings;
              if (savedRule) {
                $mailings = savedRule.$el.find('select.mailings');
                setValue($mailings, savedValues[id][mailingID]);
                $mailings.trigger('change');
              } else {
                setValue($select);
                $select.trigger('change');
              }
            });
            $(qbSelector).on('link-values-set', function (event) {
              let id = $(event.target).data('rule-id'),
                savedRule = savedRules[id], $links;
              if (savedRule) {
                $links = savedRule.$el.find('select.links');
                setValue($links, savedValues[id][linkID] || -1);
              } else {
                $links = $(event.target);
                setValue($links);
              }
              $links.trigger('values-setup-finished');
            });
            return valueSetter;
          })(),
          validation: {
            callback: function (values) {
              return values.every(function (v) {
                return v;
              }) || ["string_empty"];
            }
          },
          canBeNegated: options.mail_tracking_available,
          skipOperator: true
        },
        'revenue by mailing': {
          type: 'string',
          input_event: 'values-setup-finished',
          input: getEmptyMailingSelect,
          values: function ($ruleInput) {
            const $mailings = $ruleInput.find('select.mailings');
            requestMailings($mailings).done(() => $mailings.trigger('values-setup-finished'));
          },
          valueSetter: mailingValueSetter,
          valueGetter: mailingsValueGetter,
          canBeNegated: options.mail_tracking_available,
          skipOperator: true
        }
      };

      if (!options.mail_tracking_available) {
        delete augmentations['received mailing'];
      }

      result.forEach(function (element) {
        const properties = augmentations[element.id];
        if (properties) {
          $.extend(element, properties);
        }
      });
    }

    function requestMailings($ruleInput, successCallback) {
      const success = successCallback || function (data) {
        const rule = AGN.Lib.Select.get($ruleInput);
        const options = [];

        data.forEach((element) => options.push({id: element.mailingID, text: element.shortname}));

        rule.setOptions(options);
        $ruleInput.trigger('values-set');
      };

      return $.ajax({
        type: 'GET',
        url: AGN.url('/workflow/getAllMailingSorted.action'),
        data: {
          sortField: 'date',
          sortDirection: 'desc'
        },
        success: success
      });
    }

    function requestAutoImports($ruleInput, successCallback) {
      const success = successCallback || function (data) {
        data.forEach(function (element) {
          $ruleInput.append($('<option>', {
            text: element.shortname,
            value: element.autoImportId
          }));
        });
        $ruleInput.trigger('values-set');
      };

      return $.ajax({
        type: 'POST',
        url: AGN.url('/autoimport.do'),
        data: {
          method: 'listJson'
        },
        success: success
      });
    }

    function defaultMailingsSuccessCallback(data, $ruleInput) {
      data.forEach(function (element) {
        const option = $('<option>');
        option.text(element.shortname);
        option.val(element.mailingID);
        $ruleInput.append(option);
      });
    }

    function requestMailingLinks(mailingId, success) {
      $.ajax({
        url: AGN.url(`/mailing/ajax/${mailingId}/links.action`),
        success: success
      });
    }

    function readRulesFromJson(rulesJson, isTGLocked) {
      if (isTGLocked) {
        rulesJson = addReadOnlyFlags(rulesJson);
      }
      return $.extend({}, rulesJson);
    }

    function buildOperatorGroups() {
      return [
        {
          type: "equal",
          apply_to: ["string", "number", "datetime"]
        },
        {
          type: "not_equal",
          apply_to: ["string", "number", "datetime"]
        },
        {
          type: "less",
          apply_to: ["string", "number"]
        },
        {
          type: "less_or_equal",
          apply_to: ["string", "number"]
        },
        {
          type: "greater",
          apply_to: ["string", "number"]
        },
        {
          type: "greater_or_equal",
          apply_to: ["string", "number"]
        },
        {
          type: "like",
          nb_inputs: 1,
          apply_to: ["string"]
        },
        {
          type: "not_like",
          nb_inputs: 1,
          apply_to: ["string"]
        },
        {
          type: "contains",
          nb_inputs: 1,
          apply_to: ["string"]
        },
        {
          type: "not_contains",
          nb_inputs: 1,
          apply_to: ["string"]
        },
        {
          type: "begins_with",
          nb_inputs: 1,
          apply_to: ["string"]
        },
        {
          type: "not_begins_with",
          nb_inputs: 1,
          apply_to: ["string"]
        },
        {
          type: 'mod',
          nb_inputs: 1,
          apply_to: ['number'],
          valueSelectors: ['#first', '#operator select', '#second'],
          valueGetter: function (rule) {
            const values = [];
            rule.operator.valueSelectors.forEach(function (element) {
              values.push(rule.$el.find(element).val());
            });
            return values;
          },
          valueSetter: function (rule, value) {
            rule.operator.valueSelectors.forEach(function (element, index) {
              rule.$el.find(element).val(value[index]);
            });
          },
          input: function (rule) {
            const operators = this.operators.filter(function (operator) {
              return operator.type !== 'mod' && operator.apply_to.includes('number');
            });
            const operatorSelect = this.getRuleOperatorSelect(rule, operators);
            return "" +
              "<div class='qb-input-element'>" +
              "<input class='form-control' id='first' type='number'/>" +
              "</div>" +
              "<div class='qb-input-element' id='operator'>" +
              operatorSelect +
              "</div>" +
              "<div class='qb-input-element'>" +
              "<input class='form-control' id='second' type='number'/>" +
              "</div>";
          },
          validate: function (values) {
            return values.every(function (v) {
              return v;
            }) || ["string_empty"];
          }
        },
        {
          type: "is_empty",
          nb_inputs: 0,
          apply_to: ["string", "number", "datetime"]
        },
        {
          type: "is_not_empty",
          nb_inputs: 0,
          apply_to: ["string", "number", "datetime"]
        },
        {
          type: "before",
          nb_inputs: 1,
          apply_to: ["datetime"]
        },
        {
          type: "after",
          nb_inputs: 1,
          apply_to: ["datetime"]
        }
      ];
    }
  });

})();
