AGN.Lib.Controller.new('auto-import-export-new', function() {

    var checkConnectionUrl;
    var isWorkflowDriven;

    this.addDomInitializer('auto-export', function () {
        checkConnectionUrl = this.config.urls.checkConnection;
    });

    this.addDomInitializer('auto-import', function () {
        checkConnectionUrl = this.config.urls.checkConnection;
        isWorkflowDriven = this.config.isWorkflowDriven;
        if (contentSourceTypeExists()) {
            selectContentSource(this.config.contentSourceIdentifier);
        }
    });

    function selectContentSource(identifier) {
        const contentSourceSelect = AGN.Lib.Select.get($('#contentSourceId'));

        if (contentSourceSelect.hasOption(identifier)) {
            contentSourceSelect.selectValue(identifier);
        }
    }

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

    this.addAction({click: 'check-connection'}, function() {
        const form = AGN.Lib.Form.get(this.el);

        $.ajax(checkConnectionUrl, {
            type: 'POST',
            dataType: 'html',
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            data: form.data()
        }).done(function (resp) {
            AGN.Lib.Page.render(resp);
        });
    });

    var profileMailinglists = {},
        isAutoImportNew = false;

    this.addDomInitializer('import-profile-mailing-lists', function() {
        const profileConfig = this.config;
        isAutoImportNew = profileConfig.isAutoImportNew;
        profileMailinglists = profileConfig.profileMailinglists;
        const currentProfileId = $('[name="importProfileId"]').val();
        setMailinglists(currentProfileId, isAutoImportNew);
    });

    this.addAction({'change': 'change-import-profile'}, function() {
        const selectedProfile = AGN.Lib.Select.get($(this.el)).getSelectedValue();
        setMailinglists(selectedProfile, isAutoImportNew);
    });

    const setMailinglists = function(profileId, isNew) {
        if (isNew) {
            const mailingLists = profileMailinglists[profileId];
            $('input[type=checkbox][name^="mailinglists"]').prop('checked', false);
            _.each(mailingLists, function (id) {
                $('input[type=checkbox][name="mailinglists[' + id + ']"]').prop('checked', true);
            });
        }
    };

    //----------------------------- Auto Import/Export schedule table
    const ScheduleTimeTable = AGN.Lib.ScheduleTimeTable,
        DayRow = AGN.Lib.ScheduleTimeTable.DayRow;

    function getScheduleTimeTable($el) {
        const $controller = $el.closest('[data-controller]');
        return $controller.data('_schedule_table');
    }

    function setScheduleTimeTable($el, scheduleTable) {
        const $controller = $el.closest('[data-controller]');
        $controller.data('_schedule_table', scheduleTable);
    }

    this.addDomInitializer('auto-import-export-scheduler', function () {
        const $el = $(this.el);
        const config = this.config;
        const data = config.intervalAsJson || [];

        const scheduleTable = new ScheduleTimeTable($('#tile-importexport-schedule-time'));
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

        const form = AGN.Lib.Form.get($el);
        form.initFields();
    });

    this.addAction({click: 'add-day'}, function() {
        getScheduleTimeTable($(this.el)).addEmptyDayRow();
    });

    this.addAction({click: 'remove-day'}, function() {
        const $el = $(this.el);
        getScheduleTimeTable($el).deleteRow(DayRow.get($(this.el)));
    });

    this.addAction({click: 'add-schedule'}, function() {
        DayRow.get($(this.el)).addSchedule();
    });

    this.addAction({click: 'remove-schedule'}, function() {
        DayRow.get($(this.el)).removeSchedule($(this.el));
    });

    this.addAction({click: 'time-checkbox-toggle'}, function() {
        const $toggle = $(this.el);
        const row = DayRow.get($toggle);
        row.timeCheckboxToggle($toggle);
    });

    this.addAction({click: 'hour-checkbox-toggle'}, function() {
        const $toggle = $(this.el);
        const row = DayRow.get($toggle);
        row.hourCheckboxToggle($toggle);
    });
    this.addAction({change: 'validate-changes'}, function () {
        const $el = $(this.el);
        const row = DayRow.get($el);

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
        const $el = $(this.el);
        const scheduleTable = getScheduleTimeTable($el);
        const valid = scheduleTable.getAllDayRows().every(function(row) {
            return validateRow(row)
        });

        if (valid) {
            const form = AGN.Lib.Form.get($(this.el));
            form.setValueOnce("intervalAsJson", scheduleTable.getSubmissionJson('day'));
            form.submit();
        }
    });

    this.addAction({click: 'save-static-import'}, function () {
        const $form = $('#recipient-autoimport-form');
        const form = AGN.Lib.Form.get($form);
        const contentSourceDataParts = getContentSourceDataParts();

        if (contentSourceDataParts.length) {
            form.setValue('contentSourceID', contentSourceDataParts[0]);
            form.setValue('contentSourceType', contentSourceDataParts[1]);
        }

        form.submit();
    });

    this.addAction({submission: 'save-scheduler-data-autoimport'}, function () {
        const $el = $(this.el);
        const scheduleTable = getScheduleTimeTable($el);
        const valid = scheduleTable.getAllDayRows().every(function(row) {
            return validateRow(row)
        });

        if (valid) {
            const form = AGN.Lib.Form.get($(this.el));
            const contentSourceDataParts = getContentSourceDataParts();

            form.setValueOnce("intervalAsJson", scheduleTable.getSubmissionJson('day'));
            if (contentSourceDataParts.length) {
                form.setValueOnce('contentSourceID', contentSourceDataParts[0]);
                form.setValueOnce('contentSourceType', contentSourceDataParts[1]);
            }

            form.submit();
        }
    });

    this.addAction({click: 'save-auto-import'}, function() {
        const $form = $('#recipient-autoimport-form');

        const scheduleTable = getScheduleTimeTable($form);
        const valid = scheduleTable.getAllDayRows().every(function(row) {
            return validateRow(row)
        });

        if (valid) {
            const form = AGN.Lib.Form.get($form);
            const intervalJson = scheduleTable.getSubmissionJson('day');
            const contentSourceDataParts = getContentSourceDataParts();

            if (isWorkflowDriven) {
                form.setValue("intervalAsJson", intervalJson);

                if (contentSourceDataParts.length) {
                    form.setValue('contentSourceID', contentSourceDataParts[0]);
                    form.setValue('contentSourceType', contentSourceDataParts[1]);
                }
            } else {
                form.setValueOnce("intervalAsJson", intervalJson);

                if (contentSourceDataParts.length) {
                    form.setValueOnce('contentSourceID', contentSourceDataParts[0]);
                    form.setValueOnce('contentSourceType', contentSourceDataParts[1]);
                }
            }

            form.submit();
        }
    });

    function getContentSourceDataParts() {
        if (contentSourceTypeExists()) {
            return $('#contentSourceId').val().split('/');
        }

        return [];
    }

    function contentSourceTypeExists() {
        const $contentSourceId = $('#contentSourceId');
        return $contentSourceId.exists();
    }


    //------------------------ Auto Export mailing options resolver
    function getExportTypeHandler($el) {
        const $controller = $el.closest('[data-controller]');
        return $controller.data('_export_type_handler');
    }

    function setExportTypeHandler($el, handler) {
        const $controller = $el.closest('[data-controller]');
        $controller.data('_export_type_handler', handler);
    }

    this.addDomInitializer('mailing-options-attribute-resolver', function () {
        const $el = $(this.el);
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
        const $oneTimeCheckbox = $('input[name="' + options.oneTimeOption + '"]');
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
        const isAfterMailingDeliveryType = this.isAfterMailingDeliveryType();
        const checked = this.initialOneTimeValues.checked;
        const disabled = this.initialOneTimeValues.disabled;

        this.$oneTimeCheckbox.prop('checked', isAfterMailingDeliveryType || checked);
        this.$oneTimeCheckbox.prop('disabled', isAfterMailingDeliveryType || disabled);
    };

    this.addAction({change : 'change-mailing-type'}, function() {
        const $el = $(this.el);
        const mailingType = AGN.Lib.Select.get($el).getSelectedValue();
        var exportType;
        if ($('#exportType').is("select")) {
            exportType = AGN.Lib.Select.get($('#exportType select')).getSelectedValue();
        } else {
            exportType = $('#exportType [type=radio]:checked').val();
        }
        getExportTypeHandler($el).resolve(exportType, mailingType);
    });

    this.addAction({change : 'export-type-resolver'}, function() {
        const $el = $(this.el);
        var exportType;
        if ($('#exportType').is("select")) {
            exportType = AGN.Lib.Select.get($('#exportType select')).getSelectedValue();
        } else {
            exportType = $el.find("[type=radio]:checked").val();
        }
        const mailingType = AGN.Lib.Select.get($('#exportMailing select')).getSelectedValue();
        getExportTypeHandler($el).resolve(exportType, mailingType);

    });
});