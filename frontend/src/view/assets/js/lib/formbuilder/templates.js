(function () {

  AGN.Lib.FormBuilder = AGN.Lib.FormBuilder || {};

  var TOOLTIPS_TEXT = {
    agnCI: t('userform.formBuilder.tooltips.agnCI'),
    agnUID: t('userform.formBuilder.tooltips.agnUID')
  }

  var DEFAULT_EMM_HIDDEN_FIELDS = ['agnCI', 'agnUID', 'agnMAILINGLIST', 'agnSUBSCRIBE'];

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
        $elements = $formField.find('.form-elements'),
        isInit = !oldEmmField,
        isSelectedNewEmmField = newEmmField !== oldEmmField;

    if(isInit) {
      $formField.data('currentEmmField', newEmmField);
    } else if (isSelectedNewEmmField ) { // selected another field
      $formField.data('currentEmmField', newEmmField);
      changeFieldsBySelectedEmmField($elements, newEmmField);
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
      var tooltipValue = TOOLTIPS_TEXT[fieldName];
      if (!tooltipValue) {
          $tooltip.hide();
          return;
      }
      $tooltip.attr('tooltip', tooltipValue);
      $tooltip.show();
  }

  function changeFieldsBySelectedEmmField($formElements, newEmmField) {
    var $valueIpt = $formElements.find('input[name="value"]'),
      $nameIpt = $formElements.find('input[name="name"]');

    var fieldSelected = newEmmField !== 'none';
    var value = fieldSelected ? '$!customerData.' + newEmmField : '',
      name = fieldSelected ? newEmmField : '';

    $valueIpt.val(value);
    $nameIpt.val(name);
    if (fieldSelected) {
      $nameIpt.prop('disabled', true);
    } else {
      $nameIpt.prop('disabled', false);
    }
  }

})();