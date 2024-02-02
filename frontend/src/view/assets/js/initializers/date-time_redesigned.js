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

  AGN.Lib.CoreInitializer.new('pickadate', function ($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('.js-datepicker'), function (input) {
      const $input = $(input);
      $input.removeClass('hasDatepicker')

      let options = {
        showWeek: true,
        changeMonth: true,
        changeYear: true,
        selectOtherMonths: true,
        dateFormat: adoptDateFormat(window.adminDateFormat),
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
          instance.dpDiv.removeClass('hidden');
          $input.parent().addClass('is-active');
        },
        onClose: function () {
          $input.parent().removeClass('is-active');
        }
      };

      options = _.merge({}, options, Helpers.objFromString($input.data('datepicker-options')));
      options.initialMinDate = options.minDate;
      options.initialMaxDate = options.maxDate;

      $input.datepicker(options);
      $input.datepicker('widget').addClass('hidden');

      if (options.maxFor) {
        $input.on('change', function () {
          const maximum = $input.datepicker('getDate') || options.minDate;
          if (maximum) {
            $(options.maxFor).datepicker('option', 'maxDate', maximum);
          }
        })
      }

      if (options.minFor) {
        $input.on('change', function () {
          const minimum = $input.datepicker('getDate') || options.maxDate;
          if (minimum) {
            $(options.minFor).datepicker('option', 'minDate', minimum);
          }
        })
      }
    });

    function adoptDateFormat(dateFormat) {
      return dateFormat.replace('M', 'm')
        .replace('D', 'd')
        .replace('YYYY', 'yyyy')
        .replace('yyyy', 'yy');
    }

    function addControlButtons($input) {
      const $buttonsContainer = $('.ui-datepicker-buttonpane');

      $buttonsContainer.append(`<button id="clear-datepicker-btn" class="btn btn-outline-primary">${t('defaults.clear')}</button>`);
      $buttonsContainer.find('#clear-datepicker-btn').on('click', () => handleClear($input));

      // if original 'Today' button not exists, then it means that current date can't be selected by restrictions of minDate/maxDate.
      if ($buttonsContainer.find('.ui-datepicker-current').exists()) {
        $buttonsContainer.append(`<button id="today-datepicker-btn" class="btn btn-outline-primary">${t('defaults.today')}</button>`);
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
      const minFor = $input.datepicker('option', 'minFor');
      if (minFor) {
        $(minFor).datepicker('option', 'minDate', $(minFor).datepicker('option', 'initialMinDate'));
      }

      const maxFor = $input.datepicker('option', 'maxFor');
      if (maxFor) {
        $(maxFor).datepicker('option', 'maxDate', $(maxFor).datepicker('option', 'initialMaxDate'));
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
