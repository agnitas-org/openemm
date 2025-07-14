/*doc
---
title: Date & Time
name: datepicker
category: Components - Date & Time
---

#####Datepicker

An input can be decorated with a datepicker using the `.date-picker-container` input wrapper and `.js-datepicker` class.
Options for the datepicker can be passed via the `[data-datepicker-options]` attribute.

data-datepicker-options   |Description                                |
--------------------------|-------------------------------------------|
`minDate: '02-04-2024'`   |The minimum selectable date. 0 - no minimum|
`maxDate: '02-04-2024'`   |The maximum selectable date. 0 - no maximum|
`dateFormat: 'dd-mm-yyyy'`|Date format pattern                        |
`yearRange: '2000:2014'`  |Year from 2000 inclusive to 2014 inclusive |

See more options at <a href="https://api.jqueryui.com/datepicker/" class="text-color-info" target="_blank"><i>Jquery Datepicker Widget API</i></a>

```htmlexample
<div>
    <label class="form-label" for="date">Date</label>
    <div class="date-picker-container">
        <input type="text" name="date" id="date" value="01.12.2024"
               class="form-control js-datepicker" placeholder="DD.MM.YYYY"
               data-datepicker-options="dateFormat: 'dd-mm-yyyy', minDate: '02-04-2024', maxDate: '25-04-2024'">
    </div>
</div>
```

#####Datepicker Range

If you have a couple of date pickers that represent a date range you usually need to make sure that user cannot break the rule `begin < end`.
To apply that restriction just put both date pickers to some container (e.g. `<div>`) and add `data-date-range` attribute to container element.
The first `.js-datepicker` in hierarchy will be treated as `begin` and the second — as `end`.

```htmlexample
<div data-date-range>
    <label for="date-from" class="form-label">Date</label>

    <div class="date-picker-container mb-1">
        <input id="date-from" type="text" name="fromDate" placeholder="From" class="form-control js-datepicker">
    </div>
    <div class="date-picker-container">
        <input id="date-to" type="text" name="toDate" placeholder="To" class="form-control js-datepicker">
    </div>
</div>
```

#####Timepicker

An input can be decorated with an input-mask for time by using the `.time-picker-container` wrapper and `.js-timepicker` class.
Options for the input-mask can be passed via the `[data-timepicker-options]` attribute. This is mainly used to define the mask:

data-timepicker-options|Description                                                |
-----------------------|-----------------------------------------------------------|
`mask: 'h:halfs'`      |Default, full control over hours, with 30 minute increments|
`mask: 'h:s'`          |full control                                               |
`mask: 'h:00'`         |full control over hours, minutes stay at 00                |
`mask: 'h:quarts'`     |full control over hours, with 15 minute increments         |

A documentation of the underlying jquery plugin can be found under <a href="https://github.com/RobinHerbots/jquery.inputmask" target="_blank"><i>Jquery Inputmask Doc</i></a>

<strong>CAVEAT: When passing a value to the input make sure it is zero-padded (08:45 instead of 8:45)</strong>

```htmlexample
<div class="d-flex gap-3">
  <div>
      <label class="form-label" for="time">Time (Default)</label>
      <div class="time-picker-container">
          <input type="text" value="08:30" class="form-control js-timepicker" />
      </div>
  </div>
  <div>
      <label class="form-label" for="time">Time h:s</label>
      <div class="time-picker-container">
          <input type="text" value="08:22" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'" />
      </div>
  </div>
  <div>
      <label class="form-label" for="time">Time h:00</label>
      <div class="time-picker-container">
          <input type="text" value="08:00" class="form-control js-timepicker" data-timepicker-options="mask: 'h:00'" />
      </div>
  </div>
  <div>
      <label class="form-label" for="time">Time h:quarts</label>
      <div class="time-picker-container">
          <input type="text" value="08:15" class="form-control js-timepicker" data-timepicker-options="mask: 'h:quarts'" />
      </div>
  </div>
</div>
```

#####Combined date and time

Use `.date-time-container` wrapper in order to create combined date & time input. 

```htmlexample
<div class="date-time-container">
    <div class="date-picker-container">
        <input type="text" name="date-and-time-day" id="date-and-time-date" class="form-control js-datepicker"/>
    </div>
    <div class="time-picker-container">
        <input type="text" id="date-and-time-time" name="date-and-time-time" class="form-control js-timepicker"/>
    </div>
</div>
```
*/

;(() => {

  const Helpers = AGN.Lib.Helpers;
  const Template = AGN.Lib.Template;
  const DATA_ATTR_PREFIX = 'agn:datepicker-opt';

  AGN.Lib.CoreInitializer.new('datepicker', function ($scope = $(document)) {
    _.each($scope.find('.js-datepicker'), input => {
      const $input = $(input);
      $input.removeClass('hasDatepicker');

      let options = {
        showWeek: true,
        changeMonth: true,
        changeYear: true,
        selectOtherMonths: true,
        dateFormat: window.adminDateFormat,
        showButtonPanel: true,
        showOtherMonths: true,
        firstDay: 1, // Monday
        weekHeader: t('calendar.common.weekNumber'),
        monthNamesShort: t('date.monthsFull'),
        dayNamesMin: t('date.weekdaysShort'),
        onUpdateDatepicker: function (instance) {
          addControlButtons($input);
          customizeSelects(instance.dpDiv, $input);
        },
        beforeShow: function (input, instance) {
          if ($input.attr('readonly')) {
            return false;
          }

          instance.dpDiv.removeClass('hidden');
          $input.parent().addClass('is-active');
        },
        onClose: function () {
          $input.parent().removeClass('is-active');
        }
      };

      options = _.merge({}, options, Helpers.objFromString($input.data('datepicker-options')));

      let maskDateFormat = options.dateFormat?.toLowerCase()?.replace(/d+/, "dd")?.replace(/m+/, "mm")?.replace(/y+/, "yyyy");
      $input.inputmask(maskDateFormat, { placeholder: "_", showMaskOnHover: false,  });

      options.dateFormat = adoptDateFormat(options.dateFormat);

      if (options.formatSubmit) {
        options.altFormat = adoptDateFormat(options.formatSubmit);
        options.altField = createAltFormat$($input, options.dateFormat, options.altFormat);
      }

      $input.data(`${DATA_ATTR_PREFIX}-initialMinDate`, options.minDate);
      $input.data(`${DATA_ATTR_PREFIX}-initialMaxDate`, options.maxDate);

      $input.datepicker(options);
      $input.datepicker('widget').addClass('hidden');

      const $rangeContainer = $input.closest('[data-date-range]');
      if ($rangeContainer.exists()) {
        const $datePickers = $rangeContainer.find('.js-datepicker');

        if ($input.is($datePickers.get(0))) {
          options.minFor = $($datePickers.get(1));
        } else {
          options.maxFor = $($datePickers.get(0));
        }
      }

      if (options.maxFor) {
        $input.data(`${DATA_ATTR_PREFIX}-maxFor`, options.maxFor);
        const updateMaxFor = () => {
          fixRestrictionsAfterManualTextInput(options, $input);

          const maxDate = $input.datepicker('getDate');
          if (maxDate) {
            options.maxFor.datepicker('option', 'maxDate', maxDate);
          } else {
            resetDependentDateLimits($input);
          }
        }
        $input.on('change', updateMaxFor);
        updateMaxFor();
      }

      if (options.minFor) {
        $input.data(`${DATA_ATTR_PREFIX}-minFor`, options.minFor);

        const updateMinFor = () => {
          fixRestrictionsAfterManualTextInput(options, $input);

          const minDate = $input.datepicker('getDate');
          if (minDate) {
            options.minFor.datepicker('option', 'minDate', minDate);
          } else {
            resetDependentDateLimits($input);
          }
        }
        $input.on('change', updateMinFor);
        window.setTimeout(updateMinFor, 100);
      }
      fixRestrictionsAfterManualTextInput(options, $input);
    });

    function fixRestrictionsAfterManualTextInput(options, $input) {
      if (options.minDate || options.maxDate) {
        $input.datepicker("setDate", $.datepicker.parseDate(options.dateFormat, $input.val()));
      }
    }
    
    function adoptDateFormat(dateFormat) {
      return dateFormat.replace(/M/g, 'm')
        .replace(/D/g, 'd')
        .replace('YYYY', 'yyyy')
        .replace('yyyy', 'yy');
    }

    function createAltFormat$($input, format, altFormat) {
      const dateInNewFormat = convertDateToAltFormat($input.val(), format, altFormat);
      const $formatInput = $(`<input type="hidden" name="${$input.attr('name')}" value="${dateInNewFormat}">`);
      $input.attr('name', '');
      $formatInput.insertAfter($input);

      return $formatInput;
    }

    function convertDateToAltFormat(dateStr, fromFormat, toFormat) {
      const date = $.datepicker.parseDate(fromFormat, dateStr);
      return $.datepicker.formatDate(toFormat, date);
    }

    function addControlButtons($input) {
      const $buttonsContainer = $('.ui-datepicker-buttonpane');

      $buttonsContainer.append(Template.text('datepicker-btn', {
        id: 'clear-datepicker-btn',
        iconClass: 'icon-undo-alt',
        text: t('defaults.reset')
      }));
      $buttonsContainer.find('#clear-datepicker-btn').on('click', () => handleClear($input));

      // if original 'Today' button not exists, then it means that current date can't be selected by restrictions of minDate/maxDate.
      if ($buttonsContainer.find('.ui-datepicker-current').exists()) {
        $buttonsContainer.append(Template.text('datepicker-btn', {
          id: 'today-datepicker-btn',
          iconClass: 'icon-calendar-alt',
          text: t('defaults.today')
        }));
        $buttonsContainer.find('#today-datepicker-btn').on('click', () => handleTodayButtonClick($input));
      }
    }

    function handleTodayButtonClick($input) {
      $input.datepicker("setDate", new Date());
      $input.datepicker("hide");
      $input.trigger('change');
      $input.blur(); // remove focus
    }

    function handleClear($input) {
      $input.val('');
      $input.datepicker("setDate", null);
      $input.datepicker("hide");
      $input.blur(); // remove focus

      resetDependentDateLimits($input);
    }

    function resetDependentDateLimits($input) {
      if ($input.data(`${DATA_ATTR_PREFIX}-minFor`)) {
        const $minFor = $($input.data(`${DATA_ATTR_PREFIX}-minFor`));
        $minFor.datepicker('option', 'minDate', $minFor.data(`${DATA_ATTR_PREFIX}-initialMinDate`));
      }

      if ($input.data(`${DATA_ATTR_PREFIX}-maxFor`)) {
        const $maxFor = $($input.data(`${DATA_ATTR_PREFIX}-maxFor`));
        $maxFor.datepicker('option', 'maxDate', $maxFor.data(`${DATA_ATTR_PREFIX}-initialMaxDate`));
      }
    }

    function customizeSelects($container, $input) {
      customizeSelect($input, $container.find('select.ui-datepicker-month'));
      customizeSelect($input, $container.find('select.ui-datepicker-year'));

      AGN.runAll($container);
    }

    function customizeSelect($input, $select) {
      $select.addClass('form-control has-arrows');
      $select.attr('data-select-options', "minimumResultsForSearch: -1, dropdownParent: '#ui-datepicker-div'");
    }

    _.each($scope.find('.js-timepicker'), function (input) {
      let $input = $(input),
        options,
        mask,
        baseOptions = {
          mask: "h:halfs",
          oncomplete: function () {
            $input.trigger('timepicker:complete')
          },
          onincomplete: function () {
            $input.trigger('timepicker:incomplete')
          }
        };

      options = _.merge({}, baseOptions, Helpers.objFromString($input.data('timepicker-options')));

      mask = options.mask;
      delete options.mask;

      $input.inputmask(mask, options);
      $input.attr('placeholder', $input.data('_inputmask').opts.placeholder)

    });
  });

})();
