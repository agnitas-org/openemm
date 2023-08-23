AGN.Lib.Controller.new('mailing-overview', function() {
  var adminDateFormat = 'dd.MM.yyyy';
  
  this.addDomInitializer('mailing-overview', function() {
    adminDateFormat = this.config.adminDateFormat;
  });

  this.addAction({click: 'send-date-filter-period'}, function () {
    controlSendDatePeriodSelect(this.el);
    $('#filterSendDateBegin').val(getSendDateBeginVal());
    $('#filterSendDateEnd').val(getSendDateEndVal());
    toggleSendDateInputsReadonly($("input[name='sendDatePeriod']:checked").length > 0)
  });

  this.addAction({click: 'reset-send-date-filter'}, function () {
    $('#filterSendDateBegin').val('');
    $('#filterSendDateEnd').val('');
    $('input[name="sendDatePeriod"]:checked').removeAttr('checked');
    toggleSendDateInputsReadonly(false);
  });
  
  function toggleSendDateInputsReadonly(readonly) {
    const $sendDateBegin = $('#filterSendDateBegin');
    const $sendDateEnd = $('#filterSendDateEnd');
    $sendDateBegin.get(0).toggleAttribute('readonly', readonly);
    $sendDateEnd.get(0).toggleAttribute('readonly', readonly);
    $sendDateBegin.toggleClass('js-datepicker', !readonly);
    $sendDateEnd.toggleClass('js-datepicker', !readonly);
  }

  function getSendDateBeginVal() {
    const period = $("input[name='sendDatePeriod']:checked").val();
    if (!period) {
      return '';
    }
    const date = new Date();
    date.setDate(date.getDate() - period);
    return AGN.Lib.DateFormat.format(date, adminDateFormat);
  }

  function getSendDateEndVal() {
    return $("input[name='sendDatePeriod']:checked").length
      ? AGN.Lib.DateFormat.format(new Date(), adminDateFormat)
      : '';
  }
  
  function controlSendDatePeriodSelect($periodCheckbox) {
    if ($periodCheckbox.is(":checked")) {
      const group = "input:checkbox[name='" + $periodCheckbox.attr("name") + "']";
      $(group).prop("checked", false);
      $periodCheckbox.prop("checked", true);
    } else {
      $periodCheckbox.prop("checked", false);
    }
  }
});
