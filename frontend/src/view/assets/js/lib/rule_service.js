(function(){
  function TrackableLinks(url, cache) {
    this.src = url;
    this.promises = {};
    this.cache = $.extend({}, cache);
  }

  TrackableLinks.prototype.retrieve = function(mailingId) {
    var promises = this.promises;
    var cache = this.cache;

    var promise = promises[mailingId];
    if (promise && promise.state() == 'pending') {
      return promise;
    }

    var d = $.Deferred();
    promise = d.promise();

    if (cache[mailingId]) {
      d.resolve(cache[mailingId]);
    } else if (this.src) {
      var req = $.ajax(this.src, {
        type: 'POST',
        data: {
          mailingId: mailingId
        }
      });

      promises[mailingId] = promise;
      req.always(function () {
        delete promises[mailingId];
      });

      req.done(function (data) {
        if (data && data.mailingId == mailingId) {
          var links = data.links || [];
          cache[mailingId] = links;
          d.resolve(links, mailingId);
        } else {
          d.reject(mailingId);
        }
      });

      req.fail(function () {
        d.reject(mailingId);
      });
    } else {
      d.reject(mailingId);
    }

    return promise;
  };

  // Constants
  var COLUMN_TYPE_DATE = "DATE",
      COLUMN_TYPE_INTEGER = "INTEGER",
      COLUMN_TYPE_DOUBLE = "DOUBLE",
      COLUMN_TYPE_NUMBER = "NUMBER",
      COLUMN_TYPE_MAILING = "MAILING",
      COLUMN_TYPE_MAILING_LINKS = "MAILING_LINKS",
      COLUMN_TYPE_INTERVAL_MAILING = "INTERVAL_MAILING";

  function isNumericType(columnType) {
    switch (columnType) {
      case COLUMN_TYPE_INTEGER:
      case COLUMN_TYPE_DOUBLE:
      case COLUMN_TYPE_NUMBER:
        return true;

      default:
        return false;
    }
  }

  function getAvailablePrimaryOperators(config, columnType) {
    switch (columnType) {
      case COLUMN_TYPE_DATE:
        return config.dateOperators;

      case COLUMN_TYPE_MAILING:
      case COLUMN_TYPE_MAILING_LINKS:
        return config.mailingOperators;

      case COLUMN_TYPE_INTERVAL_MAILING:
        return config.intervalMailingOperators;

      default:
        if (isNumericType(columnType)) {
          return config.numericOperators;
        } else {
          return config.stringOperators;
        }
    }
  }

  // Public Functionality
  function changedColumn(idx, config) {
    var columnSel = getElemWithIndexedName("columnAndType", idx);

    if (columnSel) {
      var columnOpt = columnSel.options[columnSel.selectedIndex];

      var columnName = columnOpt.value.toUpperCase();
      var columnType = columnOpt.getAttribute('data-extra').toUpperCase();

      var primaryOp = getElemWithIndexedName("primaryOperator", idx);
      var primaryVal = getElemWithIndexedName("primaryValue", idx);

      if (primaryOp.value == config['OPERATOR_IS']) {
        switchToIsOperator(config, primaryVal, idx);
      } else if (primaryOp.value == config['OPERATOR_MOD'] && isNumericType(columnType)) {
        switchToModOperator(config, primaryVal, idx);
      } else if (columnType == COLUMN_TYPE_DATE) {
        switchToDateType(config, primaryVal, idx);
      } else if (columnType == COLUMN_TYPE_INTERVAL_MAILING) {
        switchToIntervalMailingType(config, primaryVal, idx);
      } else if (columnType == COLUMN_TYPE_MAILING) {
        switchToMailingType(config, primaryVal, idx);
      } else if (columnType == COLUMN_TYPE_MAILING_LINKS) {
        switchToMailingLinksType(config, primaryVal, idx);
      } else if (columnName == "MAILTYPE") {
        switchToMailType(config, primaryVal, idx);
      } else if (columnName == "GENDER") {
        switchToGender(config, primaryVal, idx);
      } else {
        switchToStringType(config, primaryVal, idx);
      }
      resetPrimaryOperators(config, primaryOp, columnType, idx);
    }
  }

  function resetPrimaryOperators(config, primaryOp, columnType, idx) {
    var op = primaryOp.value;

    var select = createSelectElement(getIndexedName("primaryOperator", idx), "form-control", "1", idx);
    var operators = getAvailablePrimaryOperators(config, columnType);

    $.each(extractValues(operators), function(index, operator) {
      appendOption(select, op, operator.id, operator.value);
    });

    replaceElement(primaryOp, select);
  }

  function resetMailingUrls(config, idx) {
    var primaryVal = getElemWithIndexedName("primaryValue", idx);
    var primaryValue = primaryVal.value;

    var newSelect = createSelectElement(getIndexedName("secondaryValue", idx), "form-control js-select js-option-popovers", "1", idx);
    newSelect.setAttribute('data-action', 'updateMailingLinkId' + (idx == null ? 'New' : ''));
    populateSelectWithLinks(config, primaryValue, newSelect, config.mailingLinksSelectionsCache[idx] && config.mailingLinksSelectionsCache[idx][primaryValue]);

    var $primaryVal = $(primaryVal);
    var existingSelect = $primaryVal.clone();
    existingSelect.val($primaryVal.val());
    existingSelect.css({ display: "block" });
    replaceElements([existingSelect, newSelect], $(primaryVal.parentNode));
  }

  function updateMailingLinksCache(config, idx, mailingLinkId) {
    var primaryVal = getElemWithIndexedName("primaryValue", idx);
    var mailingId = primaryVal.value;

    var cache = config.mailingLinksSelectionsCache[idx];
    if (!cache) {
      cache = {};
      config.mailingLinksSelectionsCache[idx] = cache;
    }

    cache[mailingId] = mailingLinkId;
  }

  function querySelectOptions(name) {
    return $('select[name="' + name + '"] option');
  }

  function mapSelectToObjects(name) {
    return mapSelectOptionsToObjects(querySelectOptions(name));
  }

  function mapSelectOptionsToObjects($options) {
    return $.map($options.toArray(), function(option) {
      var $option = $(option);
      return {
        id: $option.val(),
        value: $option.text()
      };
    });
  }

  function configure(options) {
    var mailingLinksCache = {};

    $('select[name="all_mailings_urls_config"] optgroup').each(function() {
      var $group = $(this);
      var $options = $group.find('option');

      mailingLinksCache[$group.attr('label')] = $options.map(function () {
        var $option = $(this);
        return {
          id: $option.val(),
          value: $option.text()
        };
      });
    });

    return $.extend(options, {
      mailingLinks: new TrackableLinks(options && options['URL_GET_MAILING_LINKS'], mailingLinksCache),
      mailingLinksSelectionsCache: {},
      allColumns: querySelectOptions('all_columns_config'),
      intervalMailings: mapSelectToObjects('interval_mailings_config'),
      modSecondaryOperators: mapSelectToObjects('mod_secondary_operators_config'),
      allMailings: mapSelectToObjects('all_mailings_config'),
      dateOperators: mapSelectToObjects('date_operators_config'),
      mailingOperators: mapSelectToObjects('mailing_operators_config'),
      intervalMailingOperators: mapSelectToObjects('interval_mailing_operators_config'),
      numericOperators: mapSelectToObjects('numeric_operators_config'),
      stringOperators: mapSelectToObjects('string_operators_config'),
      mailTypes: mapSelectToObjects('mail_types_config'),
      genders: mapSelectToObjects('genders_config'),
      dateFormats: mapSelectToObjects('date_formats_config')
    });
  }

  // Switch Functionality
  function switchToIsOperator(config, primaryValElement, idx) {
    var value = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var select = createSelectElement(getIndexedName("primaryValue", idx), "form-control", "1", null);
    appendOption(select, value, "null", "null");
    appendOption(select, value, "not null", "not null");
    replaceElements([select], $(primaryValElement.parentNode));
  }

  function switchToModOperator(config, primaryValElement, idx) {
    var modSecondaryOperators = config.modSecondaryOperators;

    var value = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var secondaryOp = getElemWithIndexedName("secondaryOperator", idx);
    var op = (secondaryOp == null) ? "" : secondaryOp.value;
    var secondaryVal = getElemWithIndexedName("secondaryValue", idx);
    var secondaryValue = (secondaryVal == null) ? "" : secondaryVal.value;

    var input = createInputElement(getIndexedName("primaryValue", idx), value, "form-control");
    var select = createSelectElement(getIndexedName("secondaryOperator", idx), "form-control", "1", null);

    $.each(extractValues(modSecondaryOperators), function(index, operator) {
      appendOption(select, op, operator.id, operator.value);
    });

    var input2 = createInputElement(getIndexedName("secondaryValue", idx), secondaryValue, "form-control");
    replaceElements([input, select, input2], $(primaryValElement.parentNode));
  }

  function switchToStringType(config, primaryValElement, idx) {
    var value = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var input = createInputElement(getIndexedName("primaryValue", idx), value, "form-control");
    replaceElements([input], $(primaryValElement.parentNode));
  }

  function switchToMailType(config, primaryValElement, idx) {
    var mailTypes = config.mailTypes;

    var value = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var select = createSelectElement(getIndexedName("primaryValue", idx), "form-control", "1", null);

    $.each(extractValues(mailTypes), function(index, mailType) {
      appendOption(select, value, mailType.id, mailType.value);
    });

    replaceElements([select], $(primaryValElement.parentNode));
  }

  function switchToGender(config, primaryValElement, idx) {
    var genders = config.genders;

    var value = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var select = createSelectElement(getIndexedName("primaryValue", idx), "form-control", "1", null);

    $.each(extractValues(genders), function(index, gender) {
      appendOption(select, value, gender.id, gender.value);
    });

    replaceElements([select], $(primaryValElement.parentNode));
  }

  function switchToDateType(config, primaryValElement, idx) {
    var dateFormats = config.dateFormats;

    var value = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var dateFormatEl = getElemWithIndexedName("dateFormat", idx);
    var dateFormat = (dateFormatEl == null) ? "" : dateFormatEl.value;

    var input = createInputElement(getIndexedName("primaryValue", idx), value, "form-control");

    var select = createSelectElement(getIndexedName("dateFormat", idx), "form-control", "1", null);

    $.each(extractValues(dateFormats), function(index, format) {
      appendOption(select, dateFormat, format.id, format.value);
    });

    replaceElements([input, select], $(primaryValElement.parentNode));
  }

  function switchToIntervalMailingType(config, primaryValElement, idx) {
    var intervalMailings = config.intervalMailings;

    var selectedValue = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var select = createSelectElement(getIndexedName("primaryValue", idx), "form-control js-select", "1", null);

    $.each(extractValues(intervalMailings), function(index, mailing) {
      appendOption(select, selectedValue, mailing.id, mailing.value);
    });

    replaceElements([select], $(primaryValElement.parentNode));
  }

  function switchToMailingType(config, primaryValElement, idx) {
    var allMailings = config.allMailings;

    var value = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var select = createSelectElement(getIndexedName("primaryValue", idx), "form-control js-select", "1", null);

    $.each(extractValues(allMailings), function(index, mailing) {
      appendOption(select, value, mailing.id, mailing.value);
    });

    replaceElements([select], $(primaryValElement.parentNode));
  }

  function switchToMailingLinksType(config, primaryValElement, idx) {
    var allMailings = config.allMailings;

    var value = (primaryValElement.value == null) ? "" : primaryValElement.value;
    var secondaryVal = getElemWithIndexedName("secondaryValue", idx);
    var secondaryValue = (secondaryVal == null) ? "" : secondaryVal.value;

    var select = createSelectElement(getIndexedName("primaryValue", idx), "form-control js-select", "1", idx);
    select.setAttribute("data-action", "resetMailingUrls" + (idx == null ? 'New' : ''));

    $.each(extractValues(allMailings), function(index, mailing) {
      appendOption(select, value, mailing.id, mailing.value);
    });

    value = select.value;

    var select2 = createSelectElement(getIndexedName("secondaryValue", idx), "form-control js-select js-option-popovers", "1", idx);
    select2.setAttribute('data-action', 'updateMailingLinkId' + (idx == null ? 'New' : ''));
    populateSelectWithLinks(config, value, select2, config.mailingLinksSelectionsCache[idx] && config.mailingLinksSelectionsCache[idx][value]);

    replaceElements([select, select2], $(primaryValElement.parentNode));
  }


  // Helper Functions
  function getElemWithIndexedName(name, idx) {
    return document.getElementsByName(getIndexedName(name, idx))[0];
  }

  function getIndexedName(name, idx) {
    return name + (idx != null ? "[" + idx + "]" : "New");
  }

  function populateSelectWithLinks(config, mailingId, select, selectedValue) {
    config.mailingLinks.retrieve(mailingId)
        .done(function(urls) {
          if (urls.length == 0) {
            return;
          }

          var selection = null;
          $.each(urls, function(index) {
            if (index == 0 || this.id == selectedValue) {
              selection = this.id;
            }
          });

          var $select = $(select);
          $.each(urls, function() {
            $select.append($('<option/>', {
              value: this.id,
              text: this.value,
              selected: selection == this.id
            }));
          });

          if ($select.data('select2')) {
            $select.select2('val', selection);
          }
        });
  }

  function createSelectElement(name, elClass, elSize, ruleid) {
    var select = document.createElement("select");
    select.setAttribute("name", name);
    select.setAttribute("class", elClass);
    select.setAttribute("size", elSize);
    select.setAttribute("data-action", name.replace(/\[\d+\]/, ''));
    if (ruleid != null) {
      select.setAttribute("data-ruleid", ruleid);
    }
    return select;
  }

  function replaceElement(oldOne, newOne) {
    var $newOne = $(newOne);

    $(oldOne).parent().html($newOne);
    if ($newOne.is('select')) {
      initSelect2($newOne);
    }
  }

  function replaceElements(newElements, $parentNode) {
    if (newElements.length > 0) {
      var numOfCellsInGrid = 12;
      var clsName = "col-sm-" + Math.round(numOfCellsInGrid / newElements.length);

      $parentNode = $parentNode.parent();
      $parentNode.empty();

      $.each(newElements, function() {
        var $element = $(this);

        var $divItemWrapper = $("<div>").addClass(clsName);
        $divItemWrapper.append($element);
        $parentNode.append($divItemWrapper);

        if ($element.is('select')) {
          initSelect2($element);
        }
      });
    }
  }

  function initSelect2($select) {
    AGN.Lib.CoreInitializer.run('select', $select.parent());
  }

  function appendOption(select, selectedValue, value, text) {
    var $option = $('<option/>', {
      value: value,
      text: text,
      selected: selectedValue.localeCompare(value) == 0
    });
    $option.appendTo($(select));
    return $option;
  }

  function createInputElement(name, value, elClass) {
    var input = document.createElement("input");
    input.setAttribute("type", "text");
    input.setAttribute("name", name);
    input.setAttribute("value", value);
    input.setAttribute("class", elClass);
    return input;
  }

  function extractValues(object) {
    var values = [];
    for (var k in object) {
      if (object.hasOwnProperty(k)) {
        values.push(object[k]);
      }
    }
    return values;
  }

  // Expose functionality
  AGN.Lib.RuleService = {
    changedColumn: changedColumn,
    resetPrimaryOperators: resetPrimaryOperators,
    resetMailingUrls: resetMailingUrls,
    updateMailingLinksCache: updateMailingLinksCache,
    switchTo: {
      operators: {
        mod: switchToModOperator,
        is: switchToIsOperator
      },
      types: {
        string: switchToStringType,
        mail: switchToMailType,
        gender: switchToGender,
        date: switchToDateType,
        mailing: switchToMailingType,
        mailingLinks: switchToMailingLinksType,
        intervalMailing: switchToIntervalMailingType
      }
    },
    configure: configure
  };

})();
