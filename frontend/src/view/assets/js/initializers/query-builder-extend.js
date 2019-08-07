(function () {
    AGN.Initializers.TargetGroupQueryBuilderExtend = function () {
      var MIN_EXPECTED_SIZE = 2;
      var Utils = {};

        var getHelpLanguage = function() {
            return $('#helpLanguage').val();
        };

        var dateFormatInput = function (availableFormats) {
            return "<select class='form-control' id='date-format'>" + generateDateFormatSelect(availableFormats) +
                "</select>";
        };

        function addOperator(value, dateValue) {
            if (Math.sign(value) === -1) {
                dateValue.push("sub");
            } else {
                dateValue.push("add");
            }
        }

        function updateValue(rule, value, operator) {
            if (operator && operator === 'sub') {
                value = "-" + Math.abs(value);
            }

            rule.$el.find('#date-filter').val(value);
        }

        $.fn.queryBuilder.extend({

            optionsByType: {
                'date': {
                    'availableFormats' : ['YYYYMMDD', 'DDMMYYYY', 'MMDD', 'DDMM', 'DD', 'MM', 'YYYY'],
                    'input': function (rule, options) {
                        var checkboxHideSelector = 'input[name=today]',
                            html =
                            "<div class='qb-input-label'>" +
                                "<label class='checkbox-inline'>" +
                                    "<input name='today' type='checkbox'>" +
                                    "<span class='text'>" +
                                        t('defaults.today') +
                                    "</span>" +
                                "</label>" +
                            "</div>" +
                            "<div class='qb-input-element date-filter'>" +
                                "<input class='form-control' type='number' id='date-filter'/>" +
                            "</div>" +
                            "<div class='qb-input-label'>" +
                                "<div class='qb-input-inner'>" +
                                    t('date.formats.label') +
                                "</div>" +
                            "</div>" +
                            "<div class='qb-input-element'>" + dateFormatInput(options.availableFormats)+
                            "</div>" +
                            "<div class='qb-input-label'>" +
                                "<div class='qb-input-inner'>" +
                                    "<button type='button' class='icon icon-help' data-help='help_" + getHelpLanguage() + "/targets/DateFormat.xml'></button>" +
                                "</div>"
                            "</div>",
                            $html = $(html);
                        //Cannot use default data attributes and AGN.runAll, since runAll is performed on document and this part is detached.
                        return $html;
                    },
                    valueSetter: function (rule, values) {
                        var valuesCopy = values.slice(0);
                        var todayCheckbox = rule.$el.find('input[name=today]');
                        var isToday = false, operator, dateFormat, value;

                        if (values.length > MIN_EXPECTED_SIZE) {
                            if (valuesCopy.shift() === 'today') {
                                isToday = true;
                            }
                            value = valuesCopy.shift() ;
                            operator = valuesCopy.shift() ;
                            dateFormat = valuesCopy.shift() ;
                        } else {
                            value = valuesCopy.shift();
                            if (value === 'today') {
                                isToday = true;
                                value = '';
                            }
                            dateFormat = valuesCopy.shift() ;
                        }
                        todayCheckbox.prop('checked', isToday);
                        updateValue(rule, value, operator);
                        rule.$el.find('#date-format').val(dateFormat);
                    },
                    valueGetter: function (rule) {
                        var dateValue = [],
                            value = rule.$el.find('#date-filter').val(),
                            dateFormat = rule.$el.find('#date-format').val(),
                            today = rule.$el.find('input[name=today]').is(':checked');

                        if (today) {
                            dateValue.push('today');
                            dateValue.push(value);
                            addOperator(value, dateValue);
                        } else {
                            dateValue.push(value);
                        }

                        dateValue.push(dateFormat);
                        return dateValue;
                    },
                    validate: function (value) {
                        var result = true, data = 0;
                        if (!value[data]) {
                            result = ['empty_date'];
                        }
                        return result;
                    }
                }
            },

            getRuleInput: function (rule, value_id) {

                //Unconventional variables to avoid unnecessary OOTB code changes.
                var QueryBuilder = $.fn.queryBuilder.constructor;
                var Utils = QueryBuilder.utils;

                var filter = rule.filter;
                var validation = rule.filter.validation || {};
                var name = rule.id + '_value_' + value_id;
                var c = filter.vertical ? ' class=block' : '';
                var h = '';

                if (typeof filter.input === 'function') {
                    h = filter.input.call(this, rule, name);
                    //GWUA-3476 Start. Allow custom input for operator level
                } else if (typeof  rule.operator.input === 'function') {
                    h = rule.operator.input.call(this, rule, name);
                    //Allow type based overrides
                } else if (this.optionsByType[rule.filter.type] && typeof this.optionsByType[rule.filter.type].input === 'function') {
                    h = this.optionsByType[rule.filter.type].input.call(this, rule, this.optionsByType[rule.filter.type], name);
                    //GWUA-3476 End
                } else {
                    switch (filter.input) {
                        case 'radio':
                        case 'checkbox':
                            Utils.iterateOptions(filter.values, function (key, val) {
                                h += '<label' + c + '><input type="' + filter.input + '" name="' + name + '" value="' + key + '"> ' + val + '</label> ';
                            });
                            break;

                        case 'select':
                            h = this.getRuleValueSelect(name, rule);
                            break;

                        case 'textarea':
                            h += '<textarea class="form-control" name="' + name + '"';
                            if (filter.size) h += ' cols="' + filter.size + '"';
                            if (filter.rows) h += ' rows="' + filter.rows + '"';
                            if (validation.min !== undefined) h += ' minlength="' + validation.min + '"';
                            if (validation.max !== undefined) h += ' maxlength="' + validation.max + '"';
                            if (filter.placeholder) h += ' placeholder="' + filter.placeholder + '"';
                            h += '></textarea>';
                            break;

                        case 'number':
                            h += '<input class="form-control" type="number" name="' + name + '"';
                            if (validation.step !== undefined) h += ' step="' + validation.step + '"';
                            if (validation.min !== undefined) h += ' min="' + validation.min + '"';
                            if (validation.max !== undefined) h += ' max="' + validation.max + '"';
                            if (filter.placeholder) h += ' placeholder="' + filter.placeholder + '"';
                            if (filter.size) h += ' size="' + filter.size + '"';
                            h += '>';
                            break;

                        default:
                            h += '<input class="form-control" type="text" name="' + name + '"';
                            if (filter.placeholder) h += ' placeholder="' + filter.placeholder + '"';
                            if (filter.type === 'string' && validation.min !== undefined) h += ' minlength="' + validation.min + '"';
                            if (filter.type === 'string' && validation.max !== undefined) h += ' maxlength="' + validation.max + '"';
                            if (filter.size) h += ' size="' + filter.size + '"';
                            h += '>';
                    }
                }

                return this.change('getRuleInput', h, rule, name);
            },

            getRuleValueSelect: function(name, rule) {
                var h = this.templates.ruleValueSelect({
                    builder: this,
                    name: name,
                    rule: rule,
                    icons: this.icons,
                    settings: this.settings,
                    translate: this.translate.bind(this)
                });

                return this.change('getRuleValueSelect', h, name, rule);
            },

            updateRuleOperator: function (rule, previousOperator) {
                var Selectors = $.fn.queryBuilder.constructor.selectors;

                var $valueContainer = rule.$el.find(Selectors.value_container);
                if (!rule.operator || rule.operator.nb_inputs === 0) {
                    $valueContainer.hide();

                    rule.__.value = undefined;
                }
                else {
                    $valueContainer.css('display', '');
                    //GWUA-3476 Force to redraw input since there are operator level input function
                    if ($valueContainer.is(':empty') || !previousOperator ||
                        typeof previousOperator.input === 'function' ||
                        typeof rule.operator.input === 'function'||
                        rule.operator.nb_inputs !== previousOperator.nb_inputs ||
                        rule.operator.optgroup !== previousOperator.optgroup
                    ) {
                        this.createRuleInput(rule);
                    }
                    //GWUA-3476 End
                }
                if (rule.operator) {
                    rule.$el.find(Selectors.rule_operator).val(rule.operator.type);

                    // refresh value if the format changed for this operator
                    rule.__.value = this.getRuleInputValue(rule);
                }
                this.trigger('afterUpdateRuleOperator', rule, previousOperator);
                this.trigger('rulesChanged');
            },

            createRuleOperators: function(rule) {
                var Selectors = $.fn.queryBuilder.constructor.selectors;
                var $operatorContainer = rule.$el.find(Selectors.operator_container).empty();

                if (!rule.filter) {
                    return;
                }
                var operators = this.getOperators(rule.filter);
                //GWUA-3476 Start allow filter without operators
                if (!rule.filter.skipOperator) {
                    var $operatorSelect = $(this.getRuleOperatorSelect(rule, operators));
                    $operatorContainer.html($operatorSelect);
                }
                //GWUA-3476 End
                // set the operator without triggering update event
                if (rule.filter.default_operator) {
                    rule.__.operator = this.getOperatorByType(rule.filter.default_operator);
                }
                else {
                    rule.__.operator = operators[0];
                }

                rule.$el.find(Selectors.rule_operator).val(rule.operator.type);

                this.trigger('afterCreateRuleOperators', rule, operators);
                this.applyRuleFlags(rule);
            },

            createRuleInput: function(rule) {
                var Selectors = $.fn.queryBuilder.constructor.selectors;
                var $valueContainer = rule.$el.find(Selectors.value_container).empty();

                rule.__.value = undefined;

                if (!rule.filter || !rule.operator || rule.operator.nb_inputs === 0) {
                    return;
                }

                var self = this;
                var $inputs = $();
                var filter = rule.filter;

                for (var i = 0; i < rule.operator.nb_inputs; i++) {
                    var $ruleInput = $(this.getRuleInput(rule, i));
                    if (filter.canBeNegated) {
                        var $negateInput = self.getNegateInput();
                        $valueContainer.prepend($negateInput);
                        $inputs = $inputs.add($negateInput);
                    }
                    if (i > 0) $valueContainer.append(this.settings.inputs_separator);
                    $valueContainer.append($ruleInput);
                    //GWUA-3476 Async values support
                    if (typeof filter.values === 'function') {
                        filter.values.call(this, $ruleInput);
                    }
                    //GWUA-3476
                    $inputs = $inputs.add($ruleInput);
                }

                // $valueContainer.show();
                $valueContainer.css('display', '');

                $inputs.on('change ' + (filter.input_event || ''), function() {
                   if (!rule._updating_input) {
                        rule._updating_value = true;
                        rule.negated = self.getNegateValue(rule);
                        rule.value = self.getRuleInputValue(rule);
                        rule._updating_value = false;
                    }
                });

                if (filter.plugin) {
                    $inputs[filter.plugin](filter.plugin_config || {});
                }

                this.trigger('afterCreateRuleInput', rule);

                if (filter.default_value !== undefined) {
                    rule.value = filter.default_value;
                } else {
                    rule._updating_value = true;
                    rule.negated = self.getNegateValue(rule);
                    rule.value = self.getRuleInputValue(rule);
                    rule._updating_value = false;
                }

                this.applyRuleFlags(rule);
            },

            setRuleInputValue: function (rule, value) {
                var Selectors = $.fn.queryBuilder.constructor.selectors;
                var Utils = $.fn.queryBuilder.constructor.utils;

                var filter = rule.filter;
                var operator = rule.operator;

                if (!filter || !operator) {
                    return;
                }

                rule._updating_input = true;

                if (filter.valueSetter) {
                    filter.valueSetter.call(this, rule, value);
                    //GWUA-3476 Allow operator level input
                } else if (typeof rule.operator.valueSetter === 'function') {
                    rule.operator.valueSetter.call(this, rule, value);
                } else if (this.optionsByType[rule.filter.type] && typeof this.optionsByType[rule.filter.type].valueSetter === 'function') {
                    this.optionsByType[rule.filter.type].valueSetter.call(this, rule, value);
                    //GWUA-3476
                } else {
                    var $value = rule.$el.find(Selectors.value_container);
                    if (operator.nb_inputs === 1) {
                        value = [value];
                    }

                    for (var i = 0; i < operator.nb_inputs; i++) {
                        var name = Utils.escapeElementId(rule.id + '_value_' + i);
                        switch (filter.input) {
                            case 'radio':
                                $value.find('[name=' + name + '][value="' + value[i] + '"]').prop('checked', true).trigger('change');
                                break;

                            case 'checkbox':
                                if (!$.isArray(value[i])) {
                                    value[i] = [value[i]];
                                }
                                value[i].forEach(function (value) {
                                    $value.find('[name=' + name + '][value="' + value + '"]').prop('checked', true).trigger('change');
                                });
                                break;

                            default:
                                if (operator.multiple && filter.value_separator && $.isArray(value[i])) {
                                    value[i] = value[i].join(filter.value_separator);
                                }
                                $value.find('[name=' + name + ']').val(value[i]).trigger('change');
                                break;
                        }
                    }
                }
                this.setNegated(rule);
                rule._updating_input = false;
            },

            getRuleInputValue: function(rule) {
                var Selectors = $.fn.queryBuilder.constructor.selectors;
                var Utils = $.fn.queryBuilder.constructor.utils;

                var filter = rule.filter;
                var operator = rule.operator;
                var value = [];
                if (filter.valueGetter) {
                    value = filter.valueGetter.call(this, rule);
                    //GWUA-3476 Allow operator level getter
                } else if (typeof rule.operator.valueGetter === 'function') {
                    value = rule.operator.valueGetter.call(this, rule);
                } else if (this.optionsByType[rule.filter.type] && typeof this.optionsByType[rule.filter.type].valueGetter === 'function') {
                    value = this.optionsByType[rule.filter.type].valueGetter.call(this, rule);
                    //GWUA-3476 End
                } else {
                    var $value = rule.$el.find(Selectors.value_container);
                    for (var i = 0; i < operator.nb_inputs; i++) {
                        var name = Utils.escapeElementId(rule.id + '_value_' + i);
                        var tmp;
                        switch (filter.input) {
                            case 'radio':
                                value.push($value.find('[name=' + name + ']:checked').val());
                                break;

                            case 'checkbox':
                                tmp = [];
                                $value.find('[name=' + name + ']:checked').each(function() {
                                    tmp.push($(this).val());
                                });
                                value.push(tmp);
                                break;

                            case 'select':
                                if (filter.multiple) {
                                    tmp = [];
                                    $value.find('[name=' + name + '] option:selected').each(function() {
                                        tmp.push($(this).val());
                                    });
                                    value.push(tmp);
                                }
                                else {
                                    value.push($value.find('[name=' + name + '] option:selected').val());
                                }
                                break;

                            default:
                                value.push($value.find('[name=' + name + ']').val());
                        }
                    }

                    value = value.map(function(val) {
                        if (operator.multiple && filter.value_separator && typeof val == 'string') {
                            val = val.split(filter.value_separator);
                        }

                        if ($.isArray(val)) {
                            return val.map(function(subval) {
                                return Utils.changeType(subval, filter.type);
                            });
                        }
                        else {
                            return Utils.changeType(val, filter.type);
                        }
                    });

                    if (operator.nb_inputs === 1) {
                        value = value[0];
                    }

                    // @deprecated
                    if (filter.valueParser) {
                        value = filter.valueParser.call(this, rule, value);
                    }
                }
                return this.change('getRuleValue', value, rule);
            },

            validateValue: function(rule, value) {
                var validation = rule.filter.validation || {};
                var result = true;

                if (validation.callback) {
                    result = validation.callback.call(this, value, rule);
                    //GWUA-3476 allow operator validation
                } else if (typeof rule.operator.validate === 'function') {
                    result = rule.operator.validate.call(this, value, rule);
                } else if (this.optionsByType[rule.filter.type] && typeof this.optionsByType[rule.filter.type].validate === 'function') {
                    result = this.optionsByType[rule.filter.type].validate.call(this, value, rule);
                    //GWUA-3476
                } else {
                    result = this._validateValue(rule, value);
                }
                return this.change('validateValue', result, value, rule);
            },

            getRules: function(options) {
                options = $.extend({
                    get_flags: false,
                    allow_invalid: false,
                    skip_empty: false
                }, options);

                var valid = this.validate(options);
                if (!valid && !options.allow_invalid) {
                    return null;
                }

                var self = this;

                var out = (function parse(group) {
                    var groupData = {
                        condition: group.condition,
                        rules: []
                    };

                    if (group.data) {
                        groupData.data = $.extendext(true, 'replace', {}, group.data);
                    }

                    if (options.get_flags) {
                        var flags = self.getGroupFlags(group.flags, options.get_flags === 'all');
                        if (!$.isEmptyObject(flags)) {
                            groupData.flags = flags;
                        }
                    }

                    group.each(function(rule) {
                        if (!rule.filter && options.skip_empty) {
                            return;
                        }

                        var value = null;

                        if (!rule.operator || rule.operator.nb_inputs !== 0) {
                            value = rule.value;
                        }

                        var ruleData = {
                            id: rule.filter ? rule.filter.id : null,
                            field: rule.filter ? rule.filter.field : null,
                            type: rule.filter ? rule.filter.type : null,
                            input: rule.filter ? rule.filter.input : null,
                            operator: rule.operator ? rule.operator.type : null,
                            value: value
                        };

                        if (rule.filter && rule.filter.data || rule.data) {
                            ruleData.data = $.extendext(true, 'replace', {}, rule.filter.data, rule.data);
                        }

                        if (rule.filter && rule.filter.canBeNegated) {
                            ruleData.negated = rule.negated;
                        }

                        if (options.get_flags) {
                            var flags = self.getRuleFlags(rule.flags, options.get_flags === 'all');
                            if (!$.isEmptyObject(flags)) {
                                ruleData.flags = flags;
                            }
                        }

                        groupData.rules.push(self.change('ruleToJson', ruleData, rule));

                    }, function(model) {
                        var data = parse(model);
                        if (data.rules.length !== 0 || !options.skip_empty) {
                            groupData.rules.push(data);
                        }
                    }, this);

                    return self.change('groupToJson', groupData, group);

                }(this.model.root));

                out.valid = valid;

                return this.change('getRules', out);
            },

            setRules: function(data, options) {
                options = $.extend({
                    allow_invalid: false
                }, options);

                if ($.isArray(data)) {
                    data = {
                        condition: this.settings.default_condition,
                        rules: data
                    };
                }

                if (!data || !data.rules || (data.rules.length === 0 && !this.settings.allow_empty)) {
                    Utils.error('RulesParse', 'Incorrect data object passed');
                }

                this.clear();
                this.setRoot(false, data.data, this.parseGroupFlags(data));
                this.applyGroupFlags(this.model.root);

                data = this.change('setRules', data, options);

                var self = this;

                (function add(data, group) {
                    if (group === null) {
                        return;
                    }

                    if (data.condition === undefined) {
                        data.condition = self.settings.default_condition;
                    }
                    else if (self.settings.conditions.indexOf(data.condition) == -1) {
                        Utils.error(!options.allow_invalid, 'UndefinedCondition', 'Invalid condition "{0}"', data.condition);
                        data.condition = self.settings.default_condition;
                    }

                    group.condition = data.condition;

                    data.rules.forEach(function(item) {
                        var model;
                        if (item.rules !== undefined) {
                            if (self.settings.allow_groups !== -1 && self.settings.allow_groups < group.level) {
                                Utils.error(!options.allow_invalid, 'RulesParse', 'No more than {0} groups are allowed', self.settings.allow_groups);
                                self.reset();
                            }
                            else {
                                model = self.addGroup(group, false, item.data, self.parseGroupFlags(item));
                                if (model === null) {
                                    return;
                                }

                                self.applyGroupFlags(model);

                                add(item, model);
                            }
                        }
                        else {
                            if (!item.empty) {
                                if (item.id === undefined) {
                                    Utils.error(!options.allow_invalid, 'RulesParse', 'Missing rule field id');
                                    item.empty = true;
                                }
                                if (item.operator === undefined) {
                                    item.operator = 'equal';
                                }
                            }

                            model = self.addRule(group, item.data, self.parseRuleFlags(item));
                            if (model === null) {
                                return;
                            }

                            if (item.id) {
                                if (!item.empty) {
                                    model.filter = self.getFilterById(item.id, !options.allow_invalid);
                                }
                                if (model.filter) {
                                    model.operator = self.getOperatorByType(item.operator, !options.allow_invalid);

                                    if (!model.operator) {
                                        model.operator = self.getOperators(model.filter)[0];
                                    }
                                }

                                model.negated = item.negated;

                                if (model.operator && model.operator.nb_inputs !== 0) {
                                    if (item.value !== undefined) {
                                        model.value = item.value;
                                    }
                                    else if (model.filter.default_value !== undefined) {
                                        model.value = model.filter.default_value;
                                    }
                                }
                            }

                            if (self.change('jsonToRule', model, item) != model) {
                                Utils.error('RulesParse', 'Plugin tried to change rule reference');
                            }
                        }
                    });
                    if (self.change('jsonToGroup', group, data) != group) {
                        Utils.error('RulesParse', 'Plugin tried to change group reference');
                    }
                }(data, this.model.root));
                this.validate();
                this.trigger('afterSetRules');
            },

            getNegateValue: function(rule) {
                var $negateInput = rule.$el.find('select.negate'), result;
                if ($negateInput.length !== 0) {
                    result = $negateInput.val() === 'true';
                }
                return result;
            },

            getNegateInput: function() {
                var negationSelect =
                    '<div class="qb-input-negate">' +
                        '<select class="form-control negate">' +
                            '<option value="false">' + t('defaults.yes') +
                            '</option>' +
                            '<option value="true">' + t('defaults.no') +
                            '</option>' +
                        '</select>' +
                    '</div>';
                return $(negationSelect);
            },

            setNegated: function(rule) {
                var negateSelect = rule.$el.find('select.negate');
                if (negateSelect.length !== 0) {
                    negateSelect.val('' + rule.negated);
                }
            },
            displayError: function(node) {
                if (this.settings.display_errors) {
                    if (node.error === null) {
                        node.$el.removeClass('has-error');
                    }
                    else {
                        // translate the text without modifying event array
                        var errorMessage = this.translate('errors', node.error[0]);
                        errorMessage = this.constructor.utils.fmt(errorMessage, node.error.slice(1));

                        errorMessage = this.change('displayError', errorMessage, node.error, node);

                        node.$el.addClass('has-error')
                            .find(this.constructor.selectors.error_container).eq(0)
                            .attr('data-tooltip', errorMessage);

                        AGN.Initializers.Tooltip(node.$el);
                    }
                }
            }
        })
    }

    function generateDateFormatSelect(availableFormats) {
        var resultMarkup = [];
        availableFormats.forEach(function(element) {
            var $option = $('<option />');
            $option.text(t('date.formats.' + element));
            $option.val(element);
            //Workaround to get outer HTML
            resultMarkup.push($('<div/>').append($option).html());
        });
        return resultMarkup.join('');
    }
})();