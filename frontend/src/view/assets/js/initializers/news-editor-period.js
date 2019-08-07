(function () {
    AGN.Lib.DomInitializer.new('news-editor-period', function ($elem) {
        initDatePeriodSelector($elem)
    });

    function initDatePeriodSelector($scope) {
        if (!$scope) {
            $scope = $(document);
        }

        var periodElements = [];
        _.each($scope.find('.js-datepicker'), function (input) {
            var $input = $(input);

            if ($input.hasClass("js-datepicker-period-start")) {
                if (!($input.attr("datepicker-period-id") in periodElements)) {
                    periodElements[$input.attr("datepicker-period-id")] = {startEl: null, endEl: null};
                }
                periodElements[$input.attr("datepicker-period-id")].startEl = $input;
            }
            if ($input.hasClass("js-datepicker-period-end")) {
                if (!($input.attr("datepicker-period-id") in periodElements)) {
                    periodElements[$input.attr("datepicker-period-id")] = {startEl: null, endEl: null};
                }
                periodElements[$input.attr("datepicker-period-id")].endEl = $input;
            }
        });

        for (var i = 0; i < periodElements.length; i++) {
            initPeriodDatePickers(periodElements[i].startEl.pickadate('picker'), periodElements[i].endEl.pickadate('picker'));
        }
    }

    function initPeriodDatePickers($datePickerStart, $datePickerEnd) {
        var pickerStartDay = $datePickerStart;
        var pickerEndDay = $datePickerEnd;

        if (pickerStartDay.get('value')) {
            pickerEndDay.set('min', pickerStartDay.get('select'));
        }
        if (pickerEndDay.get('value')) {
            pickerStartDay.set('max', pickerEndDay.get('select'));
        }

        pickerStartDay.on('set', function (event) {
            if (event.select) {
                pickerEndDay.set('min', pickerStartDay.get('select'));
            }
            else if ('clear' in event) {
                pickerEndDay.set('min', false);
            }
        });

        pickerEndDay.on('set', function (event) {
            if (event.select) {
                pickerStartDay.set('max', pickerEndDay.get('select'));
            }
            else if ('clear' in event) {
                pickerStartDay.set('max', false);
            }
        });
    }
})();