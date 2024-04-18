/*doc
---
title: Datepicker Directive
name: datepicker-directive
parent: directives
---

An input can be decorated with a datepicker using the `js-datepicker` class. Options for the datepicker can be passed via the `data-datepicker-options` attribute. For aligning the datepicker on the right side you can pass the `js-datepicker-right` class.

A documentation of the available options can be found under <a href="http://amsul.ca/pickadate.js/date/" target="_blank">PickADate Doc</a>

```htmlexample
<div class="form-group">
    <div class="col-sm-4">
        <label class="control-label">Date</label>
    </div>
    <div class="col-sm-8">
        <div class="input-group">
            <div class="input-group-controls">
                <input type="text" class="form-control datepicker-input js-datepicker" value="01.12.2015" />
            </div>
            <div class="input-group-btn">
                <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                    <i class="icon icon-calendar-o"></i>
                </button>
            </div>
        </div>
    </div>
</div>
```
*/

/*doc
---
title: Datepicker Range
name: datepicker-range
parent: directives
---

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
*/

/*doc
---
title: Timepicker Directive
name: timepicker-directive
parent: directives
---

An input can be decorated with an input-mask for time by using the `js-timepicker` class. Options for the input-mask can be passed via the `data-timepicker-options` attribute. This is mainly used to define the mask:

data-timepicker-options | Used for
----------------|-----------------
`mask: 'h:halfs'` | Default, full control over hours, with 30 minute increments
`mask: 'h:s'` | full control
`mask: 'h:00'` | full control over hours, minutes stay at 00
`mask: 'h:quarts'` | full control over hours, with 15 minute increments


A documentation of the underlying jquery plugin can be found under <a href="https://github.com/RobinHerbots/jquery.inputmask" target="_blank">Jquery Inputmask Doc</a>

<strong>CAVEAT: When passing a value to the input make sure it is zero-padded (08:45 instead of 8:45)</strong>

```htmlexample
<div class="form-group">
    <div class="col-sm-4">
        <label class="control-label">Time (Default)</label>
    </div>
    <div class="col-sm-8">
        <div class="input-group">
            <div class="input-group-controls">
                <input type="text" value="08:30" class="form-control js-timepicker" />
            </div>
            <div class="input-group-addon">
                <span class="addon">
                    <i class="icon icon-clock-o"></i>
                </span>
            </div>
        </div>
    </div>
</div>
<div class="form-group">
    <div class="col-sm-4">
        <label class="control-label">Time h:s</label>
    </div>
    <div class="col-sm-8">
        <div class="input-group">
            <div class="input-group-controls">
                <input type="text" value="08:22" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'" />
            </div>
            <div class="input-group-addon">
                <span class="addon">
                    <i class="icon icon-clock-o"></i>
                </span>
            </div>
        </div>
    </div>
</div>
<div class="form-group">
    <div class="col-sm-4">
        <label class="control-label">Time h:00</label>
    </div>
    <div class="col-sm-8">
        <div class="input-group">
            <div class="input-group-controls">
                <input type="text" value="08:00" class="form-control js-timepicker" data-timepicker-options="mask: 'h:00'" />
            </div>
            <div class="input-group-addon">
                <span class="addon">
                    <i class="icon icon-clock-o"></i>
                </span>
            </div>
        </div>
    </div>
</div>
<div class="form-group">
    <div class="col-sm-4">
        <label class="control-label">Time h:quarts</label>
    </div>
    <div class="col-sm-8">
        <div class="input-group">
            <div class="input-group-controls">
                <input type="text" value="08:15" class="form-control js-timepicker" data-timepicker-options="mask: 'h:quarts'" />
            </div>
            <div class="input-group-addon">
                <span class="addon">
                    <i class="icon icon-clock-o"></i>
                </span>
            </div>
        </div>
    </div>
</div>
```
*/

;(function () {

  const Helpers = AGN.Lib.Helpers;
  const DATA_ATTR_PREFIX = 'agn:datepicker-opt';

  AGN.Lib.CoreInitializer.new('pickadate', function ($scope = $(document)) {
    _.each($scope.find('.js-datepicker'), function (input) {
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
          const maximum = $input.datepicker('getDate') || options.minDate;
          $(options.maxFor).datepicker('option', 'maxDate', maximum);
        }
        $input.on('change', updateMaxFor);
        updateMaxFor();
      }

      if (options.minFor) {
        $input.data(`${DATA_ATTR_PREFIX}-minFor`, options.minFor);
        const updateMinFor = () => {
          const minimum = $input.datepicker('getDate') || options.maxDate;
          $(options.minFor).datepicker('option', 'minDate', minimum);
        }
        $input.on('change', updateMinFor);
        updateMinFor();
      }
    });

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

      $buttonsContainer.append(`
        <button id="clear-datepicker-btn" class="btn btn-inverse gap-1">
            <i class="icon icon-sync"></i>
            <span class="text">${t('defaults.clear')}</span>
        </button>
      `);
      $buttonsContainer.find('#clear-datepicker-btn').on('click', () => handleClear($input));

      // if original 'Today' button not exists, then it means that current date can't be selected by restrictions of minDate/maxDate.
      if ($buttonsContainer.find('.ui-datepicker-current').exists()) {
        $buttonsContainer.append(`
            <button id="today-datepicker-btn" class="btn btn-inverse gap-1">
                <i class="icon icon-calendar-alt"></i>
                <span class="text">${t('defaults.today')}</span>
            </button>
        `);
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
