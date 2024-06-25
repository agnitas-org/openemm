AGN.Lib.Controller.new('birt-reports', function () {

  let config = {},
    reportType = 0,
    ARCHIVE_LIST = [],
    MAILINGLIST_LIST = [],
    TARGETS_LIST = [],
    AFTER_MAILING_TYPE_ID,
    MAILING_ID,
    $form;

  const SWITCH_SELECTOR = '.form-check-input';

  this.addDomInitializer("birt-reports", function () {
    config = this.config;
    MAILING_ID = config.constant.MAILING_ID;
    AFTER_MAILING_TYPE_ID = config.constant.AFTER_MAILING_TYPE_ID;
    ARCHIVE_LIST = config.filtered.FILTER_ARCHIVE;
    MAILINGLIST_LIST = config.filtered.FILTER_MAILINGLIST;
    TARGETS_LIST = config.filtered.FILTER_TARGET;
    reportType = config.reportType;

    $form = this.el;
  });

  this.addDomInitializer("report-settings", function () {
    $('#extended-settings-tile').find('[data-toggle-tab]').on('click', function (e) {
      if (!$(e.target).is(SWITCH_SELECTOR)) {
        const checked = $(this).find(SWITCH_SELECTOR).is(':checked');
        getMobileActivateDeliveryCheckbox$().prop('checked', checked);
      }
    });

    updateTabsState();
    new ReportSettings(this.el, this.config);
  });

  this.addAction({click: 'confirm-deactivate-deliveries'}, function () {
    AGN.Lib.Confirm.createFromTemplate({}, 'report-deactivate-deliveries');
  });

  this.addAction({click: 'activate-delivery'}, function () {
    const checked = this.el.is(':checked');

    this.event.stopPropagation();
    window.setTimeout(() => this.el.prop('checked', checked), 0);

    const isActiveTab = this.el.closest('.btn').is('.active');
    if (isActiveTab) {
      getMobileActivateDeliveryCheckbox$().prop('checked', checked);
    }
  });

  function getMobileActivateDeliveryCheckbox$() {
    return $('[data-action="activate-delivery-for-active-tab"]');
  }

  this.addAction({click: 'activate-delivery-for-active-tab'}, function () {
    const checked = this.el.is(':checked');
    this.event.stopPropagation();
    window.setTimeout(() => this.el.prop('checked', checked), 0);
    $('#extended-settings-tile').find('[data-toggle-tab].active').find(SWITCH_SELECTOR).prop('checked', checked);
  });

  this.addAction({change: 'interval-change'}, function () {
    updateTabsState();
  });

  function updateTabsState() {
    const actualReportType = $('#interval').val();

    if (actualReportType == AFTER_MAILING_TYPE_ID) {
      disableTabs();
    } else if (reportType == AFTER_MAILING_TYPE_ID) {
      enableTabs();
    }

    reportType = actualReportType;
  }

  function disableTabs() {
    $('#extended-settings-tile').find('[data-toggle-tab]').each(function () {
      const $tab = $(this);
      const tabId = $tab.data("toggle-tab");

      if (tabId.endsWith(MAILING_ID)) {
        $tab.trigger("click");
        return;
      }

      $tab.addClass('disabled');
      AGN.Lib.Tab.hide($(tabId));
    });
  }

  function enableTabs() {
    $('#extended-settings-tile').find('[data-toggle-tab]').each(function () {
      $(this).removeClass('disabled');
    });
  }

  const ReportSettings = function ($container, config) {
    this.$container = $container;
    this.config = config;
    this.settingsType = config.settingsType;
    this.select = AGN.Lib.Select.get($container.find(config.selectors.filterBlockId));
    this.mailingSelect = AGN.Lib.Select.get($container.find(config.selectors.normalMailing));

    this.init(config);

    $container.find(config.selectors.mailingType).on('change', event => {
      this.handleMailingTypeChanges($(event.target));
    });
    $container.find(config.selectors.mailingFilter).on('change', event => {
      this.handleMailingFilterChanges($(event.target));
    });
    $container.find(config.selectors.filterBlockId).on('change', event => {
      this.handleMailingFilterValueChanges($(event.target));
    });
  };

  ReportSettings.prototype.init = function (config) {
    const mailingType = config.data.mailingType;
    const mailingFilter = config.data.mailingFilter;
    const predefineMailing = config.data.predefineMailing;
    const selectedMailings = config.data.selectedMailings;

    const jqXHR = this.updateAllComparisonDependentFields(mailingType, mailingFilter, predefineMailing, selectedMailings);
    jqXHR.done(() => $form.dirty('setAsClean'));
  };

  ReportSettings.prototype.handleMailingTypeChanges = function ($el) {
    const mailingFilter = this.getMailingFilter();
    const mailingFilterValue = this.getMailingFilterValue();
    this.updateNormalMailingField($el.val(), mailingFilter, mailingFilterValue);
  };

  ReportSettings.prototype.handleMailingFilterChanges = function ($el) {
    const mailingType = this.getMailingType();
    this.updateAllComparisonDependentFields(mailingType, $el.val());
  };

  ReportSettings.prototype.handleMailingFilterValueChanges = function ($el) {
    const mailingFilter = this.getMailingFilter();
    const mailingType = this.getMailingType();
    this.updateNormalMailingField(mailingType, mailingFilter, $el.val());
  };

  ReportSettings.prototype.updateAllComparisonDependentFields = function (mailingType, mailingFilter, predefinedMailing, selectedMailings) {
    this.select.resetOptions();

    if (mailingFilter > 0) {
      let data = [];

      if (mailingFilter == 1) {
        data = ARCHIVE_LIST;
      } else if (mailingFilter == 2) {
        data = MAILINGLIST_LIST;
      } else if (mailingFilter == 4) {
        data = TARGETS_LIST;
      }

      _.each(data, item => this.select.addFormattedOption(`<option value="${item.id}">${item.shortname}</option>`));

      this.select.selectValueOrSelectFirst(predefinedMailing);
      predefinedMailing = this.select.getSelectedValue();
    }

    return this.updateNormalMailingField(mailingType, mailingFilter, predefinedMailing, selectedMailings);
  };

  ReportSettings.prototype.updateNormalMailingField = function (mailingType, mailingFilter, predefinedMailing, selectedMailings) {
    const deferred = $.Deferred();
    this.mailingSelect.resetOptions();

    if ((this.settingsType == config.constant.COMPARISON_SETTINGS && mailingType == 2) ||
      (this.settingsType == config.constant.MAILING_SETTINGS && mailingType == 3)) {
      $.ajax({
        type: "GET",
        url: AGN.url('/statistics/report/getFilteredMailing.action'),
        data: {
          type: mailingFilter,
          value: predefinedMailing ? predefinedMailing : 0,
          mailingType: config.constant.NORMAL_MAILING_TYPE
        }
      }).done(data => {
        _.each(data, item => this.mailingSelect.addFormattedOption(`<option value="${item.id}">${item.shortname}</option>`));

        this.mailingSelect.selectValue(selectedMailings ? selectedMailings : '');
        deferred.resolve();
      });
    }

    return deferred.promise();
  };

  ReportSettings.prototype.getMailingFilter = function () {
    const filter = this.$container.find(this.config.selectors.mailingFilter);
    return parseInt(filter.val()) | 0;
  };

  ReportSettings.prototype.getMailingFilterValue = function () {
    const filterValue = this.select.getSelectedValue();
    return parseInt(filterValue) | 0;
  };

  ReportSettings.prototype.getMailingType = function () {
    const mailingType = this.$container.find(`${this.config.selectors.mailingType}:checked`);
    return parseInt(mailingType.val()) | 0;
  };

});
