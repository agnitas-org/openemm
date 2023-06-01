AGN.Lib.Controller.new('birt-reports', function() {

    var config = {},
        reportType = 0,
        ARCHIVE_LIST = [],
        MAILINGLIST_LIST = [],
        TARGETS_LIST = [],
        AFTER_MAILING_TYPE_ID,
        MAILING_ID;

    this.addDomInitializer("birt-reports", function() {
        config = this.config;
        MAILING_ID = config.constant.MAILING_ID;
        AFTER_MAILING_TYPE_ID = config.constant.AFTER_MAILING_TYPE_ID;
        ARCHIVE_LIST = config.filtered.FILTER_ARCHIVE;
        MAILINGLIST_LIST = config.filtered.FILTER_MAILINGLIST;
        TARGETS_LIST = config.filtered.FILTER_TARGET;
        reportType = config.reportType;
    });

    function disableTabs() {
        _.each($(config.reportSettingsId).find("[data-toggle-tab]"), function(tab) {
            var $tab = $(tab);
            var tabId = $tab.data("toggle-tab");
            if(tabId.endsWith(MAILING_ID)) {
                $tab.trigger("click");
                return;
            }

            $tab.parent("li").addClass("disabled");
            AGN.Lib.Tab.hide($(tabId));
        });
    }

    function enableTabs() {
        _.each($(config.reportSettingsId).find("[data-toggle-tab]"), function(tab) {
            $(tab).parent("li").removeClass("disabled");
        });
    }

    AGN.Lib.Action.new({change: "input[type=radio][name=type]"}, function(){
        var actualReportType = $(this.el).val();
        var isAfterMailingType = actualReportType == AFTER_MAILING_TYPE_ID;

        if(isAfterMailingType) {
            disableTabs();
        } else if(reportType == AFTER_MAILING_TYPE_ID) {
            enableTabs();
        }

        reportType = actualReportType;
        return false;
    });

    this.addDomInitializer("report-settings", function(){
        var el = this.el;
        var config = this.config;

        new ReportSettings(el, config);
    });

    var ReportSettings = function (container, config) {
        var self = this;
        this.container = container;
        this.config = config;
        this.settingsType = config.settingsType;
        this.select = AGN.Lib.Select.get(container.find(config.selectors.filterBlockId));
        this.mailingSelect = AGN.Lib.Select.get(container.find(config.selectors.normalMailing));

        self.init(config);

        container.find(config.selectors.mailingType).on('change', function(event) {
            self.handleMailingTypeChanges($(event.target));
        });
        container.find(config.selectors.mailingFilter).on('change', function (event) {
            self.handleMailingFilterChanges($(event.target));
        });
        container.find(config.selectors.filterBlockId).on('change', function(event) {
            self.handleMailingFilterValueChanges($(event.target));
        });

    };

    ReportSettings.prototype.init = function(config) {
        var mailingType = config.data.mailingType;
        var mailingFilter = config.data.mailingFilter;
        var predefineMailing = config.data.predefineMailing;
        var selectedMailings = config.data.selectedMailings;

        this.updateAllComparisonDependentFields(mailingType, mailingFilter, predefineMailing, selectedMailings);
    };

    ReportSettings.prototype.handleMailingTypeChanges = function(el) {
        var report = this;
        var mailingFilter = report.getMailingFilter();
        var mailingFilterValue = report.getMailingFilterValue();
        report.updateNormalMailingField(el.val(), mailingFilter, mailingFilterValue);
    };

    ReportSettings.prototype.handleMailingFilterChanges = function(el) {
        var report = this;
        var mailingType = report.getMailingType();
        report.updateAllComparisonDependentFields(mailingType, el.val());
    };

    ReportSettings.prototype.handleMailingFilterValueChanges = function(el) {
        var report = this;
        var mailingFilter = report.getMailingFilter();
        var mailingType = report.getMailingType();
        report.updateNormalMailingField(mailingType, mailingFilter, el.val());
    };

    ReportSettings.prototype.updateAllComparisonDependentFields = function(mailingType, mailingFilter, predefinedMailing, selectedMailings) {
        var self = this;
        self.select.resetOptions();

        if(mailingFilter > 0) {
            var data = [];

            if(mailingFilter == 1) {
                data = ARCHIVE_LIST;
            } else if(mailingFilter == 2) {
                data = MAILINGLIST_LIST;
            } else if(mailingFilter == 4) {
                data = TARGETS_LIST;
            }

            _.each(data, function(item) {
                self.select.addFormattedOption('<option value=\"' + item.id + '\">' + item.shortname + '</option>');
            });

           self.select.selectValueOrSelectFirst(predefinedMailing);
            predefinedMailing = self.select.getSelectedValue();
        }

        self.updateNormalMailingField(mailingType, mailingFilter, predefinedMailing, selectedMailings);
    };

    ReportSettings.prototype.updateNormalMailingField = function(mailingType, mailingFilter, predefinedMailing, selectedMailings) {
        var self = this;
        self.mailingSelect.resetOptions();

        if((self.settingsType == config.constant.COMPARISON_SETTINGS && mailingType == 2) ||
            (self.settingsType == config.constant.MAILING_SETTINGS && mailingType == 3)) {
            $.ajax({
                type: "GET",
                url: config.urls.FILTERED_MAILING_URL,
                data: {
                    type: mailingFilter,
                    value: predefinedMailing ? predefinedMailing : 0,
                    mailingType: config.constant.NORMAL_MAILING_TYPE
                }
            }).done(function (data) {
                _.each(data, function (item) {
                    self.mailingSelect.addFormattedOption('<option value=\"' + item.id + '\">' + item.shortname + '</option>');
                });

                self.mailingSelect.selectValue(selectedMailings ? selectedMailings : '');
            });
        }
    };

    ReportSettings.prototype.getMailingFilter = function() {
       var filter = this.container.find(this.config.selectors.mailingFilter);
       return parseInt(filter.val()) | 0;
    };

    ReportSettings.prototype.getMailingFilterValue = function() {
       var filterValue = this.select.getSelectedValue();
       return parseInt(filterValue) | 0;
    };

    ReportSettings.prototype.getMailingType = function() {
        var mailingType = this.container.find(this.config.selectors.mailingType + ':checked');
        return parseInt(mailingType.val()) | 0;
    };

    this.addAction({click: 'confirm-deactivate-deliveries'}, function() {
        AGN.Lib.Confirm.createFromTemplate({
            action: AGN.url("/statistics/report/" + config.reportId + "/deactivateAllDeliveries.action"),
            method: 'POST',
            title: t('birtreport.deactivateAll'),
            content: t('birtreport.deactivateAllQuestion')
        }, 'modal-yes-no-cancel')
            .done(function(resp) {
                AGN.Lib.Form.get($('#birtreportForm')).updateHtml(resp);
            })
    });
});
