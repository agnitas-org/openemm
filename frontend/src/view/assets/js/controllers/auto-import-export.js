AGN.Lib.Controller.new('auto-import-export', function() {

    this.addInitializer('optionsChangeable', function () {
        if ($('#recipient-autoimport-csvdescription').length) {
            $('[data-action="options-availability"][value="ReferenceTable"]').prop('disabled', false);
        }
    });

    this.addAction({click: 'select-customer-fields'}, function() {
        AGN.Lib.Modal.createFromTemplate({}, 'modal-select-customer-fields');
        $('#modalSelectedProfileFields').select2('val', $('#selectedProfileFields').val());
    });

    this.addAction({click: 'save-selected-customer-fields'}, function() {
        $('#selectedProfileFields').select2('val', $('#modalSelectedProfileFields').val());
    });

    var profileMailinglists = {},
      isAutoImportNew = false;

    this.addDomInitializer('import-profile-mailing-lists', function() {
        var profileConfig = this.config;
        isAutoImportNew = profileConfig.isAutoImportNew;
        profileMailinglists = profileConfig.profileMailinglists;
        var currentProfileId = $('[name="' + profileConfig.profileIdPropertyName + '"]').val();
        setMailinglists(currentProfileId, isAutoImportNew);
    });

    this.addAction({'change': 'change-import-profile'}, function() {
        var selectedProfile = AGN.Lib.Select.get($(this.el)).getSelectedValue();
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

    //----------------------------- Auto Import/Export schedule table
    var ScheduleTimeTable = AGN.Lib.ScheduleTimeTable,
      DayRow = AGN.Lib.ScheduleTimeTable.DayRow;

    function getScheduleTimeTable($el) {
        var $controller = $el.closest('[data-controller]');
        return $controller.data('_schedule_table');
    }

    function setScheduleTimeTable($el, scheduleTable) {
        var $controller = $el.closest('[data-controller]');
        $controller.data('_schedule_table', scheduleTable);
    }

    this.addDomInitializer('auto-import-export-scheduler', function () {
        var $el = $(this.el);
        var config = this.config;
        var data = config.intervalAsJson || [];

        var scheduleTable = new ScheduleTimeTable($('#tile-importexport-schedule-time'));
        setScheduleTimeTable($el, scheduleTable);

        var period = [];
        if (!_.isEmpty(data)) {
            period = JSON.parse(data).map(function (object) {
                return DayRow.deserialize(object);
            });
        }

        period.forEach(function (day) {
            scheduleTable.addRow(day);
        });

        if (period.length === 0) {
            scheduleTable.addEmptyDayRow();
        }

        var form = AGN.Lib.Form.get($el);
        form.initFields();
    });

    this.addAction({click: 'add-day'}, function() {
        getScheduleTimeTable($(this.el)).addEmptyDayRow();
    });

    this.addAction({click: 'remove-day'}, function() {
        var $el = $(this.el);
        getScheduleTimeTable($el).deleteRow(DayRow.get($(this.el)));
    });

    this.addAction({click: 'add-schedule'}, function() {
        DayRow.get($(this.el)).addSchedule();
    });

    this.addAction({click: 'remove-schedule'}, function() {
        DayRow.get($(this.el)).removeSchedule($(this.el));
    });

    this.addAction({click: 'time-checkbox-toggle'}, function() {
        var $toggle = $(this.el);
        var row = DayRow.get($toggle);
        row.timeCheckboxToggle($toggle);
    });

    this.addAction({click: 'hour-checkbox-toggle'}, function() {
        var $toggle = $(this.el);
        var row = DayRow.get($toggle);
        row.hourCheckboxToggle($toggle);
    });
    this.addAction({change: 'validate-changes'}, function () {
        var $el = $(this.el);
        var row = DayRow.get($el);

        validateRow(row);
    });

    function validateRow(row) {
        if (row.isValidTimeIntervals()) {
            row.hideTimeScheduleErrors();
            return true;
        } else {
            row.showTimeScheduleErrors(t('error.delay.60'));
            return false;
        }
    }

    this.addAction({submission: 'save-scheduler-data'}, function () {
        var $el = $(this.el);
        var scheduleTable = getScheduleTimeTable($el);
        var valid = scheduleTable.getAllDayRows().every(function(row) {return validateRow(row)});

        if (valid) {
            var form = AGN.Lib.Form.get($(this.el));
            form.setValueOnce("intervalAsJson", scheduleTable.getSubmissionJson('day'));
            form.submit();
        }
    });


    //------------------------ Auto Export mailing options resolver
    function getExportTypeHandler($el) {
        var $controller = $el.closest('[data-controller]');
        return $controller.data('_export_type_handler');
    }

    function setExportTypeHandler($el, handler) {
        var $controller = $el.closest('[data-controller]');
        $controller.data('_export_type_handler', handler);
    }

    this.addDomInitializer('mailing-options-attribute-resolver', function () {
        var $el = $(this.el);
        setExportTypeHandler($el, new ExportTypeHandler($el, this.config));
    });

    function ExportTypeHandler($element, options) {
        if (!$element.exists()) {
            console.error('ExportTypeHandler could not instantiate, element is not defined');
            return false;
        }
        this.$element = $element;

        this.exportType = options.exportType || '';
        this.mailingType = options.mailingType || -1;
        var $oneTimeCheckbox = $('input[name="' + options.oneTimeOption + '"]');
        this.$oneTimeCheckbox = $oneTimeCheckbox;
        this.initialOneTimeValues = {
            checked: $oneTimeCheckbox.is(':checked'),
            disabled: $oneTimeCheckbox.is(':disabled')
        };

        this.toggleOneTimeCheckbox();
    }

    ExportTypeHandler.prototype.isAfterMailingDeliveryType = function() {
        return this.exportType == 'Mailing' && this.mailingType == 0;
    };

    ExportTypeHandler.prototype.resolve = function(exportType, mailingType) {
        this.exportType = exportType || '';
        this.mailingType = mailingType || -1;
        this.toggleOneTimeCheckbox();
    };

    ExportTypeHandler.prototype.toggleOneTimeCheckbox = function() {
        var isAfterMailingDeliveryType = this.isAfterMailingDeliveryType();
        var checked = this.initialOneTimeValues.checked;
        var disabled = this.initialOneTimeValues.disabled;

        this.$oneTimeCheckbox.prop('checked', isAfterMailingDeliveryType || checked);
        this.$oneTimeCheckbox.prop('disabled', isAfterMailingDeliveryType || disabled);
    };

    this.addAction({change : 'change-mailing-type'}, function() {
        var $el = $(this.el);
        var mailingType = AGN.Lib.Select.get($el).getSelectedValue();
        var exportType = $('#exportType [type=radio]:checked').val();
        getExportTypeHandler($el).resolve(exportType, mailingType);
    });

    this.addAction({change : 'export-type-resolver'}, function() {
        var $el = $(this.el);
        var exportType = $el.find("[type=radio]:checked").val();
        var mailingType = AGN.Lib.Select.get($('#exportMailing select')).getSelectedValue();
        getExportTypeHandler($el).resolve(exportType, mailingType);

    });
});