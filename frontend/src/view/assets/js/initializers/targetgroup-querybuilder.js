(function() {

  var READ_ONLY_PARAMS = {
    filter_readonly: true,
    condition_readonly: true,
    operator_readonly: true,
    value_readonly: true,
    no_delete: true,
    no_add_rule: true,
    no_add_group: true
  };
  var WORKFLOW_URLS;

  AGN.Initializers.TargetGroupQueryBuilder = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    var qbSelector = '#targetgroup-querybuilder';
    var queryBuilders = $scope.find(qbSelector);

    var config = {
      jSessionId: "",
      isTargetGroupLocked: false,
      WORKFLOW_URLS: {}
    };

    if (queryBuilders.exists()) {
      if ($scope.find("script#target-group-query-builder").exists()) {
        config = $scope.find("script#target-group-query-builder").json();
        WORKFLOW_URLS = config.WORKFLOW_URLS;
      }

    }

    var jSessionId = config.jSessionId;
    var isTargetGroupLocked = config.isTargetGroupLocked;

    var isIE10 = navigator.userAgent.match('MSIE 10.0;');

    _.each(queryBuilders, function(el) {
      var $el = $(el);
      var operator_groups = buildOperatorGroups();
      var qbFilters = readFiltersFromJson($el, "#queryBuilderFilters");
      $el.queryBuilder({
        filters: qbFilters,
        allow_empty: true,
        operators: operator_groups,
        plugins: {
          'sortable': {
            inherit_no_drop: true,
            inherit_no_sortable: true,
            icon: 'icon icon-arrows-alt'
          }
        },
        rules: readRulesFromJson($el, "#queryBuilderRules", isTargetGroupLocked)
      });

      if (!isIE10) {
        AGN.Initializers.Select($el);
      }

      // Register event handler callback to receive notifications on any change on groups that affects getRules()
      $el.on('afterMove.queryBuilder rulesChanged.queryBuilder', function(event) {
        setRulesAsJson(event.builder);
      });

      // Register event handler callback to receive notifications on any change on groups that affects getRules()
      $el.on('afterAddGroup.queryBuilder afterDeleteGroup.queryBuilder afterUpdateGroupCondition.queryBuilder', function(event) {
        setRulesAsJson(event.builder);
      });

      // Register event handler callback to receive notifications on any change on rules that affects getRules()
      $el.on('afterAddRule.queryBuilder afterDeleteRule.queryBuilder afterUpdateRuleValue.queryBuilder afterUpdateRuleOperator.queryBuilder afterUpdateRuleFilter.queryBuilder', function(event) {
        setRulesAsJson(event.builder);
      });

      $el.on('afterSetRules.queryBuilder afterUpdateRuleFilter.queryBuilder afterCreateRuleFilters.queryBuilder afterAddRule.querybuilder', function(event) {
        if (!isIE10) {
          AGN.Initializers.Select($(this));
        }
      });

      $el.on('link-values-set', function(e) {
        if (!isIE10) {
          AGN.Initializers.Select($(e.target));
        }
      });

      $el.on('mailing-values-set values-set ', function(e) {
        if (!isIE10) {
          AGN.Initializers.Select($(e.target));
        }
      });

      $el.on('change-date-format', function(e) {
        if (!isIE10) {
          AGN.Initializers.Select($(e.target));
        }
      });

      $el.on("save-target-group.queryBuilder", function(event){
        var self = this;
        var queryBuilder = self.queryBuilder;
          queryBuilder.clearInitialRuleSettings();
          queryBuilder.validate();
      });
    });

    function setRulesAsJson(builder) {
      var json = JSON.stringify(builder.getRules({allow_invalid: true}));
      _.each($scope.find("#queryBuilderRules"), function(el) {
        var $el = $(el);
        $el.val(json);
      });
    }

    function setValue($element, value) {
      if (!isIE10 && $element.is('select')) {
        AGN.Lib.Select.get($element).selectValueOrSelectFirst(value);
      } else {
        $element.val(value);
      }
    }

    function readFiltersFromJson($scope, name) {
      var result = readQBJSON($scope, name);

      if (result === null) {
        result = [{
          id: '?',
          label: '?',
          type: 'string'
        }];
      } else {
        var ids = {};

        result = result.filter(function(e) {
          if (ids[e.id]) {
            return false;
          }

          ids[e.id] = true;
          return true;
        });
      }

      augmentIndependentFields(result);

      return result;
    }

    function addReadOnlyFlags(options) {
      if(options) {
        options.flags = READ_ONLY_PARAMS;

        _.each(options.rules, function(rule){
          rule.flags = READ_ONLY_PARAMS;
        });
      }
      return options;
    }

    function augmentIndependentFields(result) {
      //An additional properties for independent fields

      var getEmptyMailingSelect = function(rule) {
        var $select = $('<select class="form-control qb-input-element-select mailings js-select">');
        $select.attr("id", rule.id);
        return $select;
      };

      var getEmptyAutoImportSelect = function(rule) {
        var $select = $('<select class="form-control qb-input-element-select auto-imports js-select">');
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
          canBeNegated: true,
          values: function($ruleInput) {
            requestMailings($ruleInput).done(function() {
              $ruleInput.trigger('values-setup-finished');
            });
          },
          valueSetter: mailingValueSetter,
          valueGetter: mailingsValueGetter,
          skipOperator: true
        },
        'clicked in mailing': {
          type: 'string',
          input_event: 'values-setup-finished',
          input: function(rule) {
            return $(
              '<div class="qb-input-element">' +
              '<select class="form-control js-select mailings" id="' + rule.id + '" />' +
              '</div>' +
              '<div class="qb-input-label">' +
              '<div class="qb-input-inner">Links</div>' +
              '</div>' +
              '<div class="qb-input-element">' +
              '<select class="form-control js-select links" id="' + rule.id + '" />' +
              '</div>'
            );
          },
          values: function($ruleInput) {
            var $mailingsSelect = $ruleInput.find('.mailings');
            $mailingsSelect.change(function(event) {
              var mailingID = $(event.target).find(':selected').val();
              requestMailingLinks(mailingID, function(data) {
                var $linksContainer = $ruleInput.find('.links'),
                  option = $('<option value="-1">' + t('querybuilder.common.anyLink') +
                    '</option>');
                $linksContainer.empty();
                $linksContainer.append(option);
                $.each(data, function(key, value) {
                  option = $('<option/>');
                  option.text(value.url);
                  option.val(value.id);
                  $linksContainer.append(option);
                });
                $linksContainer.trigger('link-values-set');
              });
            });
            requestMailings($mailingsSelect, function(data) {
              defaultMailingsSuccessCallback(data, $mailingsSelect);
              $mailingsSelect.trigger('mailing-values-set');
            });
          },
          valueGetter: function(rule) {
            var value = [];
            var mailingID = 0, linkID = 1;
            value[mailingID] = rule.$el.find('select.mailings').val();
            value[linkID] = rule.$el.find('select.links').val();
            return value;
          },
          valueSetter: (function() {
            var mailingID = 0, linkID = 1, qbSelector = '#tab-targetgroupQueryBuilderEditor',
              savedValues = {}, savedRules = {};
            var valueSetter = function(rule, value) {
              savedValues[rule.id] = value;
              savedRules[rule.id] = rule;
            };
            //Optimize
            $(qbSelector).on('mailing-values-set', function(event) {
              var $select = $(event.target),
                id = $select.attr('id'),
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
              var id = $(event.target).attr('id'),
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
          canBeNegated: true,
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
          canBeNegated: true,
          skipOperator: true
        }
      };

      result.forEach(function(element) {
        var properties = augmentations[element.id];
        if (properties) {
          $.extend(element, properties);
        }
      });
    }

    function requestMailings($ruleInput, successCallback) {
      var success = successCallback || function(data) {
        data.forEach(function(element) {
          var option = $('<option>');
          option.text(element.shortname);
          option.val(element.mailingID);
          $ruleInput.append(option);
        });
        $ruleInput.trigger('values-set');
      };

      return $.ajax({
        type: 'POST',
        url: WORKFLOW_URLS.getAllMailingSorted,
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
        url: 'autoimport.do;jsessionid=' + jSessionId,
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
        type: 'POST',
        url: WORKFLOW_URLS.getMailingLinks,
        data: {
          mailingId: mailingId
        },
        success: success
      });
    }

    function readRulesFromJson($scope, name, isTGLocked) {
      var result =  readQBJSON($scope, name, isTGLocked);
      if(isTGLocked == 'true') {
        result = addReadOnlyFlags(result);
      }
      return result;
    }

    function readQBJSON($scope, name) {
      var result = null;
      _.each($scope.find(name), function(el) {
        if (result === null) {
          var $el = $(el),
            source = $el.val();
          result = JSON.parse(source);
        }
      });
      return result;
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
  }
})();
