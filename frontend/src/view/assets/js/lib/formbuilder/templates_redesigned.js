(function () {

  AGN.Lib.FormBuilder = AGN.Lib.FormBuilder || {};

  var TOOLTIPS_TEXT = {
    agnCTOKEN: t('userform.formBuilder.tooltips.agnCTOKEN'),
    agnUID: t('userform.formBuilder.tooltips.agnUID')
  }

  const MIN_SELECT_OPTIONS = 2;

  const EMM_FIELDS_DEF_OPTIONS = {
    "gender": [
      {
        "selected": true,
        "label": "Gender Male",
        "value": 0
      },
      {
        "selected": false,
        "label": "Gender Female",
        "value": 1
      }
    ]
  }

  var DEFAULT_EMM_HIDDEN_FIELDS = ['agnCTOKEN', 'agnUID', 'agnMAILINGLIST', 'agnSUBSCRIBE'];

  AGN.Lib.FormBuilder.Templates = {
    default: function () {
      return {
        onRender: defaultOnRender
      };
    },
    hidden: function () {
      return {
        onRender: hiddenOnRender
      };
    },
    dateForRender: function (fieldData) {
      var self = this;
      var classStyle = (this.config.className || '') + ' js-datepicker';
      var config = $.extend({}, this.config, {
        'data-datepicker-options': "format: 'yyyy/mm/dd', formatSubmit: 'yyyy/mm/dd'",
        className: classStyle
      });
      return {
        field: self.markup('input', null, config),
        onRender: function (e) {
          var $formGroup = $(e.target);
          $formGroup.append('<input type="hidden" name="' + fieldData.name + '_format" value="' + 'yyyy/MM/dd' + '">');
        }
      };
    }
  };

  function defaultOnRender(e) {
    var $formField = $(e.target).parents('.form-field'),
        newEmmField = this.config.emmField,
        oldEmmField = $formField.data('currentEmmField'),
        isInit = !oldEmmField,
        isSelectedNewEmmField = newEmmField !== oldEmmField;

    if(isInit) {
      $formField.data('currentEmmField', newEmmField);
    } else if (isSelectedNewEmmField) { // selected another field
      $formField.data('currentEmmField', newEmmField);
      changeFieldsBySelectedEmmField($formField, this.config, newEmmField, oldEmmField);
    }
  }

  function hiddenOnRender(e) {
    var name = this.config.id.replace('-preview', '');
    var $eventTarget = $(e.target);
    var $elements = $eventTarget.parents('.form-field').find('.form-elements');
    var $emmFieldWrap = $elements.find('.emmField-wrap');
    if (DEFAULT_EMM_HIDDEN_FIELDS.includes(name)) {
      $emmFieldWrap.hide();
    } else {
      $emmFieldWrap.show();
    }

    if (!DEFAULT_EMM_HIDDEN_FIELDS.includes(name)) {
      defaultOnRender.apply(this, arguments);
    }

    var $tooltipEl = $eventTarget.parents('.form-field').find('.tooltip-element');
    displayTooltipIfExists($tooltipEl, name)
  }

  function displayTooltipIfExists($tooltip, fieldName) {
      const tooltipValue = TOOLTIPS_TEXT[fieldName];
      if (!tooltipValue) {
          $tooltip.hide();
          return;
      }
      $tooltip.removeAttr('tooltip').show();
      AGN.Lib.Tooltip.createTip($tooltip, tooltipValue)
  }

  function changeFieldsBySelectedEmmField($formField, fieldConfig, newEmmField, oldEmmField) {
    const $formElements = $formField.find('.form-elements'),
        $valueIpt = $formElements.find('input[name="value"]'),
        $nameIpt = $formElements.find('input[name="name"]'),
        fieldSelected = newEmmField !== 'none';

    var value = fieldSelected ? '$!customerData.' + newEmmField : '',
      name = fieldSelected ? newEmmField : '';

    $valueIpt.val(value);
    $nameIpt.val(name);

    if (fieldSelected) {
      $nameIpt.prop('disabled', true);
    } else {
      $nameIpt.prop('disabled', false);
    }

    if (fieldConfig.type === 'select') {
      updateSelectFieldOptions(newEmmField, oldEmmField, $formField, fieldConfig);
    }
  }

  function updateSelectFieldOptions(newEmmField, oldEmmField, $formField, fieldConfig) {
    const options = getOptionsByEmmField(newEmmField);

    if (options.length >= MIN_SELECT_OPTIONS) {
      const configOptions = {
        "values": options,
        "name": newEmmField
      };

      replaceField($formField, fieldConfig, configOptions);
    } else if (getOptionsByEmmField(oldEmmField).length > 0) {
        const configOptions = {
          "values": [], // let to FormBuilder set default options
          "name": newEmmField
        };

        replaceField($formField, fieldConfig, configOptions);
    }
  }

  function replaceField($formField, currentFieldConfig, configOptions) {
    const $builderContainer = $formField.parents('.js-form-builder');
    const formBuilder = $builderContainer.data('formBuilder')
    formBuilder.actions.removeField($formField.prop('id'));

    invokeAfterEventsFinishing(function () {
      const newConfig = _.mergeWith(currentFieldConfig, configOptions, function (a, b) {
           return _.isArray(b) ? b : undefined; // if 2nd value is an array, return it. If not - let merge handle it
      });

      formBuilder.actions.addField(newConfig, findFieldIndex($formField, $builderContainer));
    });

    invokeAfterEventsFinishing(function () {
      formBuilder.actions.toggleFieldEdit(formBuilder.actions.getCurrentFieldId());
    });
  }

  function findFieldIndex($formField, $builderContainer) {
    var fieldIndex = 0;

    _.each($builderContainer.find('.form-field'), function (el, index) {
      if ($(el).prop('id') === $formField.prop('id')) {
        fieldIndex = index;
      }
    });

    return fieldIndex;
  }

  function invokeAfterEventsFinishing(callback) {
    window.setTimeout(function () {
      callback();
    }, 0);
  }

  function getOptionsByEmmField(fieldName) {
    const options = EMM_FIELDS_DEF_OPTIONS[fieldName];
    return options ? options : [];
  }

})();