AGN.Lib.Controller.new('auto-import-export', function () {
    var data,
        $entryRow,
        $entryHourInput,
        $removeButton = $(
            '<div class="input-group-addon">' +
            '<a href="#" class="btn btn-regular btn-alert" data-action="remove-schedule">' +
            '<i class="icon icon-trash-o"></i>' +
            '</a>' +
            '</div>'),
        $removeDayButton = $(
            '<a href="#" class="btn btn-regular" data-action="remove-day">' +
            '<i class="icon icon-trash-o"></i>' +
            '<span class="text">' +
            t('defaults.delete') +
            '</span>' +
            '</a>'),
        $errorInput = $('<div class="form-control-feedback-message js-form-error-msg">' + t('error.delay.60') + '</div>'),
        profileMailinglists = {},
        isAutoImportNew = false;

    var scheduleValid = new ScheduleValidation();
    var exportTypeHandler = new ExportTypeHandler();

    var extractValue = function () {
        var days = $('#tile-importexport-schedule-time').find('select[name=dayOfTheWeek]');
        var scheduleAsJson = [];
        days.each(function () {
            var scheduleEntry = {},
                $wrapperElement = $(this).parents('tr'),
                $hourCheckbox = $wrapperElement.find('.hour-checkbox');
            scheduleEntry.weekDay = $wrapperElement.find('select[name=dayOfTheWeek]').val();
            scheduleEntry.scheduledTime = [];
            $wrapperElement.find('.schedule-time-wrapper').each(function () {
                var $timeCheckbox = $(this).find('.time-checkbox');
                var timeInput = {
                    active: $timeCheckbox.is(':checked') && !$timeCheckbox.is(':disabled'),
                    time: $(this).find('.time-input').val()
                };
                scheduleEntry.scheduledTime.push(timeInput);
            });

            scheduleEntry.scheduledInterval = {
                active: $hourCheckbox.is(':checked') && !$hourCheckbox.is(':disabled'),
                interval: $wrapperElement.find('select.hour-select').val()
            };
            scheduleAsJson.push(scheduleEntry);
        });
        return scheduleAsJson;
    };

    var setValue = function (scheduledAsJson) {
        var $importTable = $('#tile-importexport-schedule-time'),
            $tableBody = $importTable.find('tbody'),
            $tableRow;
        scheduledAsJson.forEach(function (scheduleEntry, index) {
            if (index === 0) {
                $tableRow = $tableBody.find('tr');
                fillSettings($tableRow, scheduleEntry);
            } else {
                $tableRow = $entryRow.clone();
                fillSettings($tableRow, scheduleEntry);
                $tableBody.append($tableRow);
            }
            setDisabledElements($tableRow);
        });
        AGN.runAll($tableBody);
    };

    var saveToJson = function () {
        var value = extractValue();
        $('input[name=intervalAsJson]').val(JSON.stringify(value));
    };

    var fillSettings = function ($tableRow, entry) {
        var $scheduleSettingsWrapper = $tableRow.find('.schedule-settings-wrapper');
        $tableRow.find('select[name=dayOfTheWeek]').val(entry.weekDay);
        $tableRow.find('.hour-checkbox').prop('checked', entry.scheduledInterval.active);
        $tableRow.find('select.hour-select').val(entry.scheduledInterval.interval);
        entry.scheduledTime.forEach(function (scheduleElement, index) {
            if (index === 0) {
                var $hourInput = $scheduleSettingsWrapper.find('.schedule-time-wrapper');
                if (entry.scheduledTime.length > 1) {
                    $hourInput.find('.input-group').append($removeButton.clone());
                }
                fillScheduledTimeWrapper($hourInput, scheduleElement);
                $scheduleSettingsWrapper.append($hourInput);
            } else {
                var $newHourInput = createEmptyHourInput();
                fillScheduledTimeWrapper($newHourInput, scheduleElement);
                $scheduleSettingsWrapper.append($newHourInput);
            }
        });
    };

    var fillScheduledTimeWrapper = function ($element, scheduleElement) {
        $element.find('.time-checkbox').prop('checked', scheduleElement.active);
        $element.find('.time-input').val(scheduleElement.time);
    };

    var hourCheckboxToggle = function () {
        var $targetElement = this.el,
            toDisable = $targetElement.attr('disables'),
            checked = $targetElement.is(':checked'),
            $scheduleTimeWrapper = $targetElement.parents('.schedule-settings-wrapper');
        $scheduleTimeWrapper.find(toDisable).prop('disabled', checked);
    };

    var timeCheckboxToggle = function () {
        var $targetElement = this.el,
            toDisable = $targetElement.attr('disables'),
            checked = $targetElement.is(':checked'),
            $scheduleTimeWrapper = $targetElement.parents('.schedule-settings-wrapper'),
            $entryTimeCheckboxes = $targetElement.parents('tr').find('.time-checkbox'),
            $hourCheckbox = $scheduleTimeWrapper.find('.hour-checkbox'),
            hasChecked;
        for (var i = 0; i < $entryTimeCheckboxes.length; i++) {
            var plainCheckbox = $entryTimeCheckboxes[i],
                $checkbox = $(plainCheckbox);
            if ($targetElement[0] !== plainCheckbox && $checkbox.is(':checked')) {
                hasChecked = true;
                break;
            }
        }
        if ((!$hourCheckbox.is(':disabled') || !hasChecked) && data.extended) {
            $scheduleTimeWrapper.find(toDisable).prop('disabled', checked);
        }
    };

    var addRemoveButton = function ($entry) {
        if($entry.find('[data-action="remove-day"]').length !== 1){
            $entry.find('[data-action="add-another-day"]').parent().append($removeDayButton.clone());
        }
    };

    var setDisabledElements = function ($tableRow) {
        var dataActions = {
            'hour-checkbox-toggle': hourCheckboxToggle,
            'time-checkbox-toggle': timeCheckboxToggle
        };
        for (var key in dataActions) {
            $tableRow.find('input[data-action=' + key + ']').each(function () {
                dataActions[key].call({el: $(this)});
            });
        }
    };

    var cleanUpElemnets = function ($wrapper) {
        $wrapper.find('select2-container').remove();
        $wrapper.find('select').removeAttr('style');
        $wrapper.find('select').removeAttr('tabindex');
        $wrapper.find('select').removeAttr('title');
    };

    var createEmptyHourInput = function () {
        var $hourSelect = $('.schedule-time-wrapper.prime').clone();
        cleanUpElemnets($hourSelect);
        $hourSelect.removeClass('prime');
        return $hourSelect;
    };

    this.addInitializer('optionsChangeable', function () {
        if ($('#recipient-autoimport-csvdescription').length) {
            $('[data-action="options-availability"][value="ReferenceTable"]').prop('disabled', false);
        }
    });

    this.addAction({'click': 'add-schedule'}, function () {
        var $targetElement = this.el.parents('.schedule-time-wrapper'),
            $scheduleElement = $entryHourInput.clone(),
            $hourCheckbox = $targetElement.parents('tr').find('.hour-checkbox');
        $scheduleElement.find("#hours").parent().append("<span>"+t('time.interval')+" "+ t('time.60')+"</span>");
        $scheduleElement.find('.input-group').append($removeButton.clone());
        if ($targetElement.find('.btn-alert').length === 0) {
            $targetElement.find('.input-group').append($removeButton.clone());
        }
        $targetElement.parent().append($scheduleElement);
        hourCheckboxToggle.call({el: $hourCheckbox});
        AGN.runAll($scheduleElement);
    });

    this.addAction({'click': 'time-checkbox-toggle'}, timeCheckboxToggle);

    this.addAction({'click': 'hour-checkbox-toggle'}, hourCheckboxToggle);

    this.addAction({'click': 'add-another-day'}, function () {
        var targetElement = this.el.parents('tbody').find('tr').first(),
            $tableRow,
            $rootElement = this.el.parents('tbody');

        if($rootElement.find('tr').length === 1){
            addRemoveButton($entryRow);
            addRemoveButton($rootElement);
        }
        $tableRow = $entryRow.clone();
        targetElement.parent().append($tableRow);
        AGN.runAll($tableRow);
        saveToJson();
    });

    this.addAction({'click': 'remove-schedule'}, function () {
        var $targetElement = this.el.parents('.schedule-time-wrapper'),
            $tableRow = $targetElement.parents('tr');
        var $settingsWrapper = this.el.parents('.schedule-settings-wrapper');
        $targetElement.remove();
        setDisabledElements($tableRow);
        if ($settingsWrapper.find('.schedule-time-wrapper').length === 1) {
            $settingsWrapper.find('.btn-alert').parent().remove();
        }
        saveToJson();
    });

    this.addAction({'change': 'auto-import-export-validation'}, function () {
        var value = extractValue();
        var $timeWrapper = this.el;
        var $wrapperElement = $(this.el).parents('tr');
        var timeInput = this.el.find('.time-input');
        var day = $wrapperElement.find('select[name=dayOfTheWeek]').val();

        if (!scheduleValid.valid(value, day)) {
            $timeWrapper.addClass("has-alert has-feedback js-form-error");
            timeInput.after($errorInput.clone());
        } else {
            $timeWrapper.removeClass("has-alert has-feedback js-form-error");
            $timeWrapper.find('.js-form-error-msg').remove();
        }

        $('input[name=intervalAsJson]').val(JSON.stringify(value));
    });

    this.addAction({'click': 'remove-day'}, function () {
        this.el.parents('tr').remove();
        if($('tbody').find('tr').length === 1){
            $('tbody').find('[data-action="remove-day"]').remove();
        }
        saveToJson();
    });

    this.addAction({click: 'select-customer-fields'}, function() {
        AGN.Lib.Modal.createFromTemplate({}, 'modal-select-customer-fields');
        $('#modalSelectedProfileFields').select2('val', $('#selectedProfileFields').val());
    });

    this.addAction({click: 'save-selected-customer-fields'}, function() {
        $('#selectedProfileFields').select2('val', $('#modalSelectedProfileFields').val());
    });

    this.addDomInitializer('auto-import-export-init', function ($el) {
        data = $el.json();
        var jsonValue = JSON.parse($('input[name=intervalAsJson]').val());
        var $defaultEntry = $('tr.l-time-schedule-row');
        if(jsonValue.length > 1){
            $defaultEntry.find('[data-action="add-another-day"]').parent().append($removeDayButton.clone());
        }
        $entryRow = $defaultEntry.clone();
        $entryRow.removeClass('l-time-schedule-row');
        cleanUpElemnets($entryRow);
        $entryHourInput = $('.schedule-time-wrapper').clone();
        setValue(jsonValue || []);
    });


    this.addDomInitializer('mailing-options-attribute-resolver', function ($el) {
        exportTypeHandler.config($el, this.config);
    });


    this.addAction({'change' : 'change-mailing-type'}, function() {
        exportTypeHandler.setProperlyFieldState($(this.el).val());
    });

    this.addAction({'change' : 'export-type-resolver'}, function() {
        exportTypeHandler.setExportType($(this.el).find("[type=radio]:checked").val());
        exportTypeHandler.setProperlyFieldState($('#exportMailing select').val())
    });

    function ExportTypeHandler() {
        this.exportType = '';
        this.mailingType = -1;
        this.oneTimeOption = '';
        this.oneTimeOptionValues = {
            checked: false,
            disabled: false
        };
    };

    ExportTypeHandler.prototype.config = function(element, params) {
        if(element && params) {
            this.element = element;
            this.exportType = params.exportType || '';
            this.mailingType = params.mailingType >= 0 ? params.mailingType : -1;
            this.oneTimeOption = params.oneTimeOption || '';

            var oneTimeCheckbox = $('input[name="' + this.oneTimeOption + '"]');
            this.oneTimeOptionValues = {
                checked: oneTimeCheckbox.is(':checked'),
                disabled: oneTimeCheckbox.is(':disabled')
            };

            this.setOneTimeValue();
        }
    };

    ExportTypeHandler.prototype.setExportType = function(exportType) {
        this.exportType = exportType || '';
    };

    ExportTypeHandler.prototype.setMailingType = function(mailingType) {
        this.mailingType = mailingType || -1;
    };

    ExportTypeHandler.prototype.isAfterMailingDeliveryType = function() {
        return this.exportType == 'Mailing' && this.mailingType == 0;
    };

    ExportTypeHandler.prototype.setOneTimeValue = function() {
        var checked = this.isAfterMailingDeliveryType() || this.oneTimeOptionValues.checked;
        var disabled = this.isAfterMailingDeliveryType() || this.oneTimeOptionValues.disabled;
        var oneTimeCheckbox = $('input[name="' + this.oneTimeOption + '"]');
        oneTimeCheckbox.prop('checked', checked ? 'checked' : '');
        oneTimeCheckbox.prop('disabled', disabled ? 'checked' : '');
    };

    ExportTypeHandler.prototype.setProperlyFieldState = function (mailingType) {
        this.setMailingType(mailingType);
        this.setOneTimeValue();
    };

    function ScheduleValidation() {
    };

    ScheduleValidation.prototype.prepareTimeBy = function (scheduledTimes) {
        var times = [];
        scheduledTimes.forEach(function (scheduledTime) {
            if (scheduledTime.active) {
                times.push(parseInt(scheduledTime.time.replace(':', '')));
            }
        });

        return times;

    };

    ScheduleValidation.prototype.valid = function (values, currentDay) {
        var times = [];

        values.forEach(function (element) {
            if (element.weekDay === 0) {
                times = element.scheduledTime;
            }
            if (element.weekDay === currentDay) {
                element.scheduledTime.forEach(function (time) {
                    times.push(time);
                });
            }
        });

        times = this.prepareTimeBy(times);

        return times.every(this.hasCorrectInterval);
    };


    ScheduleValidation.prototype.hasCorrectInterval = function (time, index, times) {
        var isCorrect = true;
        times.splice(index, 1);
        times.forEach(function (element) {
            if (Math.abs(element - time) < 100) {// 100 is interval of one hour
                isCorrect = false;
            }
        });
        return isCorrect;
    };


    this.addDomInitializer('import-profile-mailing-lists', function() {
        var configs = this.config;
        isAutoImportNew = configs.isAutoImportNew;
        profileMailinglists = configs.profileMailinglists;
        var currentProfileId = $('[name="' + configs.profileIdPropertyName + '"]').val();
        setMailinglists(currentProfileId, isAutoImportNew);
    });

    this.addAction({'change': 'change-import-profile'}, function() {
        var selectedProfile = $(this.el).val();
        setMailinglists(selectedProfile, isAutoImportNew);
    });

    var setMailinglists = function(profileId, isNew) {
        if(isNew) {
            var mailingLists = profileMailinglists[profileId];
            $('input[type=checkbox][name^="mailinglist"]').prop('checked', false);
            _.each(mailingLists, function (id) {
                $('input[type=checkbox][name="mailinglist[' + id + ']"]').prop('checked', true);
            });
        }
    };
});