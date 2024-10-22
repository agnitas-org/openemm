AGN.Lib.Controller.new('mailing-overview', function () {

  const Form = AGN.Lib.Form;
  const DateFormat = AGN.Lib.DateFormat;

  const BADGES_FILTERS = [
    {key: 'isgrid', text: t('mailing.default.grid'), class: 'mailing.status.emc'},
    {key: 'isCampaignManager', text: t('workflow.icon'), class: 'mailing.status.cm'}
  ];

  this.addDomInitializer('mailing-overview', function () {
    clearFormSubmitHandlers(Form.getWrapper(this.el));
    handleBadgesFiltersOptions(this.config.badgesFilters);
  });

  function handleBadgesFiltersOptions(selectedBadges) {
    if (!selectedBadges) {
      selectedBadges = [];
    }

    const $statusSelect = $('#status-filter');
    const statusSelect = AGN.Lib.Select.get($statusSelect);

    _.each(BADGES_FILTERS, badge => {
      $statusSelect.append(`<option value="${badge.key}" data-badge-class="${badge.class}">${badge.text}</option>`);

      if (selectedBadges.includes(badge.key)) {
        statusSelect.selectOption(badge.key);
      }
    });

    const form = Form.get($statusSelect);
    form.get$().on('form:submit', () => {
      let badgesAdded = 0;

      _.each(BADGES_FILTERS, badge => {
        if (statusSelect.isOptionSelected(badge.key)) {
          form.setValueOnce(`filterBadges[${badgesAdded++}]`, badge.key);
          statusSelect.$findOption(badge.key).attr('disabled', true);
        }
      });
    });

    form.get$().on('submitted', () => {
      BADGES_FILTERS.forEach(badge => {
        statusSelect.$findOption(badge.key).attr('disabled', false);
      });
    });
  }

  function clearFormSubmitHandlers($form) {
    $form.off('form:submit');
    $form.off('submitted');
  }

  this.addAction({'table-column-manager:add': 'update-columns'}, function () {
    const form = Form.get(this.el);
    form.setValueOnce('selectedFields', this.data.columns);
    form.setValueOnce('inEditColumnsMode', true);
    form.submit();
  });

  this.addAction({'table-column-manager:apply': 'update-columns'}, function () {
    const selectedFields = this.data.columns;

    $.ajax({
      type: 'POST',
      url: AGN.url('/mailing/setSelectedFields.action'),
      traditional: true,
      data: {selectedFields}
    }).done(resp => {
      if (resp.success) {
        AGN.Lib.WebStorage.extend('mailing-overview', {'fields': selectedFields});
      }
      AGN.Lib.JsonMessages(resp.popups);
    });
  });

  this.addAction({click: 'send-date-filter-period'}, function () {
    controlSendDatePeriodSelect(this.el);
    setSendDatesRange(getSendDateBeginVal(), getSendDateEndVal());
    toggleSendDateInputsReadonly($("input[name='sendDatePeriod']:checked").length > 0)
  });

  this.addAction({click: 'reset-send-date-filter'}, function () {
    setSendDatesRange('', '');
    $('input[name="sendDatePeriod"]:checked').removeAttr('checked');
    toggleSendDateInputsReadonly(false);
  });

  function setSendDatesRange(startDate, endDate) {
    $('#filterSendDateBegin').val(startDate).trigger('change');
    $('#filterSendDateEnd').val(endDate).trigger('change');
  }

  function toggleSendDateInputsReadonly(readonly) {
    $('#filterSendDateBegin').get(0).toggleAttribute('readonly', readonly);
    $('#filterSendDateEnd').get(0).toggleAttribute('readonly', readonly);
  }

  function getSendDateBeginVal() {
    const period = $("input[name='sendDatePeriod']:checked").val();
    if (!period) {
      return '';
    }
    const date = new Date();
    date.setDate(date.getDate() - period);
    return DateFormat.format(date, window.adminDateFormat);
  }

  function getSendDateEndVal() {
    return $("input[name='sendDatePeriod']:checked").length
      ? DateFormat.format(new Date(), window.adminDateFormat)
      : '';
  }

  function controlSendDatePeriodSelect($periodCheckbox) {
    if ($periodCheckbox.is(":checked")) {
      const group = `input:checkbox[name="${$periodCheckbox.attr("name")}"]`;
      $(group).prop("checked", false);
      $periodCheckbox.prop("checked", true);
    } else {
      $periodCheckbox.prop("checked", false);
    }
  }
});
