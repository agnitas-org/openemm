(function() {

  var READ_ONLY_PARAMS = {
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

  AGN.Lib.DomInitializer.new('target-group-query-builder', function($needle) {
    var $form = AGN.Lib.Form.getWrapper($needle);
    var qbSelector = '#targetgroup-querybuilder';
    var queryBuilders = $(qbSelector);

    var mailTrackingAvailable = this.config.mailTrackingAvailable;
    var isTargetGroupLocked = this.config.isTargetGroupLocked;
    var helpLanguage = this.config.helpLanguage;

    var isIE10 = navigator.userAgent.match('MSIE 10.0;');

    var self = this;
    _.each(queryBuilders, function(el) {
      var $el = $(el);
      var operator_groups = buildOperatorGroups();

      var qbFilters = readFiltersFromJson(self.config.queryBuilderFilters, {mail_tracking_available: mailTrackingAvailable});
      var rules = readRulesFromJson(JSON.parse(self.config.queryBuilderRules), isTargetGroupLocked);

      $el.queryBuilder({
        filters: qbFilters,
        allow_empty: true,
        operators: operator_groups,
        helpLanguage: helpLanguage,
        readonly: isTargetGroupLocked,
        plugins: {
          'sortable': {
            inherit_no_drop: true,
            inherit_no_sortable: true,
            icon: 'icon icon-arrows-alt'
          }
        },
        rules: rules
      });

      if (!isIE10) {
        AGN.Lib.CoreInitializer.run('select', $el);
      }

      $el.on('afterSetRules.queryBuilder afterUpdateRuleFilter.queryBuilder afterCreateRuleFilters.queryBuilder afterAddRule.querybuilder', function(event) {
        if (!isIE10) {
          AGN.Lib.CoreInitializer.run('select', $(this));
        }
      });

      $el.on('link-values-set', function(e) {
        if (!isIE10) {
          AGN.Lib.CoreInitializer.run('select', $(e.target));
        }
      });

      $el.on('mailing-values-set values-set ', function(e) {
        if (!isIE10) {
          AGN.Lib.CoreInitializer.run('select', $(e.target));
        }
      });

      $el.on('change-date-format', function(e) {
        if (!isIE10) {
          AGN.Lib.CoreInitializer.run('select', $(e.target));
        }
      });

      // Prevent form submission on enter press when QB inputs are in focus.
      $el.on('enterdown', 'input', function(e) {
        return false;
      });

    });

    $form
      .off()
      .on('validation', function(e, options) {
      // ignore_qb_validation is used to prevent validate QB rules
      // this option prevent update query builder rules field
      options = $.extend({ignore_qb_validation: false, skip_empty: false}, options);
      _.each($(qbSelector), function(el) {
        var $el = $(el);
        var qb = $el.prop('queryBuilder');
        if (!validate(e, qb, options, $el)) {
          return false;
        }
      });
    });

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
      if (!isIE10 && $element.is('select')) {
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
        var ids = {};

        filtersJson = filtersJson.filter(function(e) {
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
      if(options) {
        options.flags = READ_ONLY_PARAMS;

        _.each(options.rules, function(rule){
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
      var getEmptyMailingSelect = function(rule) {
        var $select = $('<select class="form-control qb-input-element-select mailings js-select"></select>');
        $select.attr("id", rule.id);
        return $select;
      };

      var getEmptyAutoImportSelect = function(rule) {
        var $select = $('<select class="form-control qb-input-element-select auto-imports js-select"></select>');
        $select.attr("id", rule.id);
        return $select;
      };

      var mailingsValueGetter = function(rule) {
        return rule.$el.find('.mailings :selected').val();
      };

      var autoImportsValueGetter = function(rule) {
        return rule.$el.find('.auto-imports :selected').val();
      };

      var generateValueSetter = function(selector) {
        var savedValues = {}, savedRules = {}, defaultValue = -1;

        $(qbSelector).on('values-set', selector, function(event) {
          if ($.isEmptyObject(savedRules)) {
            setValue($(event.target), defaultValue);
          } else {
            var id = $(event.target).attr('id'),
              savedRule = savedRules[id],
              $inputs = savedRule ? savedRule.$el.find(selector) : $(event.target);
            setValue($inputs, savedValues[id] || defaultValue);
          }
        });

        return function(rule, value) {
          var $inputs = rule.$el.find(selector);
          savedValues[rule.id] = value;
          savedRules[rule.id] = rule;
          setValue($inputs, value);
        }
      };

      var mailingValueSetter = generateValueSetter('select.mailings');
      var autoImportValueSetter = generateValueSetter('select.auto-imports');

      var augmentations = {
        'finished auto-import': {
          type: 'string',
          input_event: 'values-setup-finished',
          input: getEmptyAutoImportSelect,
          values: function($ruleInput) {
            requestAutoImports($ruleInput).done(function() {
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
          values: function($ruleInput) {
            requestMailings($ruleInput).done(function() {
              $ruleInput.trigger('values-setup-finished');
            });
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
          values: function($ruleInput) {
            requestMailings($ruleInput).done(function() {
              $ruleInput.trigger('values-setup-finished');
            });
          },
          valueSetter: mailingValueSetter,
          valueGetter: mailingsValueGetter,
          canBeNegated: options.mail_tracking_available,
          skipOperator: true
        },
        'clicked in mailing': {
          type: 'string',
          input_event: 'values-setup-finished',
          valueSelector: function(ruleId, index) {
            var ids = ['mailingID', 'linkID'];
            return ruleId + '_' + ids[index];
          },
          input: function(rule) {
            return $('<div class="qb-input-element-select">' +
              '<select class="form-control js-select mailings" data-rule-id="' + rule.id + '" id="' + rule.filter.valueSelector(rule.id, 0) + '" ></select>' +
              '</div>' +
              '<div class="qb-input-label">' +
              '<div class="qb-input-inner">Links</div>' +
              '</div>' +
              '<div class="qb-input-element-select">' +
              '<select class="form-control js-select links" data-rule-id="' + rule.id + '" id="' + rule.filter.valueSelector(rule.id, 1) + '" ></select>' +
              '</div>');
          },
          values: function($ruleInput) {
            var $mailings = $ruleInput.find('.mailings');
            var mailings = AGN.Lib.Select.get($mailings);
            $mailings.change(function() {
              var mailingID = mailings.getSelectedValue();
              requestMailingLinks(mailingID, function(data) {
                var $links = $ruleInput.find('.links');
                var links = AGN.Lib.Select.get($links);
                var options = [{id: -1, text: t('querybuilder.common.anyLink')}];

                $.each(data, function(key, value) {
                  options.push({id: value.id, text: value.url});
                });

                links.setOptions(options);
                $links.trigger('link-values-set');
              });
            });
            requestMailings($mailings, function(data) {
              var options = [];
              data.forEach(function(element) {
                options.push({id: element.mailingID, text: element.shortname})
              });

              mailings.setOptions(options);
              $mailings.trigger('mailing-values-set');
            });
          },
          valueGetter: function(rule) {
            var mailingID = 0, linkID = 1,
              value = [];

            value[mailingID] = rule.$el.find('#' + rule.filter.valueSelector(rule.id, mailingID)).val();
            value[linkID] = rule.$el.find('#' + rule.filter.valueSelector(rule.id, linkID)).val();
            return value;
          },
          valueSetter: (function() {
            var mailingID = 0, linkID = 1,
              savedValues = {}, savedRules = {};
            var valueSetter = function(rule, value) {
              savedValues[rule.id] = value;
              savedRules[rule.id] = rule;
            };
            //Optimize
            $(qbSelector).on('mailing-values-set', function(event) {
              var $select = $(event.target),
                id = $select.data('rule-id'),
                savedRule = savedRules[id], $mailings;
              if (savedRule) {
                $mailings = savedRule.$el.find('.mailings');
                setValue($mailings, savedValues[id][mailingID]);
                $mailings.trigger('change');
              } else {
                setValue($select);
                $select.trigger('change');
              }
            });
            $(qbSelector).on('link-values-set', function(event) {
              var id = $(event.target).data('rule-id'),
                savedRule = savedRules[id], $links;
              if (savedRule) {
                $links = savedRule.$el.find('.links');
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
            callback: function(values) {
              return values.every(function(v) { return v; }) || ["string_empty"];
            }
          },
          canBeNegated: options.mail_tracking_available,
          skipOperator: true
        },
        'revenue by mailing': {
          type: 'string',
          input_event: 'values-setup-finished',
          input: getEmptyMailingSelect,
          values: function($ruleInput) {
            requestMailings($ruleInput).done(function() {
              $ruleInput.trigger('values-setup-finished');
            });
          },
          valueSetter: mailingValueSetter,
          valueGetter: mailingsValueGetter,
          canBeNegated: options.mail_tracking_available,
          skipOperator: true
        }
      };

      if(!options.mail_tracking_available) {
        delete augmentations['received mailing'];
      }

      result.forEach(function(element) {
        var properties = augmentations[element.id];
        if (properties) {
          $.extend(element, properties);
        }
      });
    }

    function requestMailings($ruleInput, successCallback) {
      var success = successCallback || function(data) {
        var rule = AGN.Lib.Select.get($ruleInput);
        var options = [];

        data.forEach(function(element) {
          options.push({id: element.mailingID, text: element.shortname})
        });

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
      var success = successCallback || function(data) {
        data.forEach(function(element) {
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
      data.forEach(function(element) {
        var option = $('<option>');
        option.text(element.shortname);
        option.val(element.mailingID);
        $ruleInput.append(option);
      });
    }

    function requestMailingLinks(mailingId, success) {
      $.ajax({
        url: AGN.url('/mailing/ajax/' + mailingId + '/links.action'),
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
          valueGetter: function(rule) {
            var values = [];
            rule.operator.valueSelectors.forEach(function(element) {
              values.push(rule.$el.find(element).val());
            });
            return values;
          },
          valueSetter: function(rule, value) {
            rule.operator.valueSelectors.forEach(function(element, index) {
              rule.$el.find(element).val(value[index]);
            });
          },
          input: function(rule) {
            var operators = this.operators.filter(function(operator) {
              return operator.type !== 'mod' && operator.apply_to.includes('number');
            });
            var operatorSelect = this.getRuleOperatorSelect(rule, operators);
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
          validate: function(values) {
            return values.every(function(v) { return v; }) || ["string_empty"];
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
