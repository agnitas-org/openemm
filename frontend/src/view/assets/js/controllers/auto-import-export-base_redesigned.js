AGN.Lib.Controller.new('auto-import-export-base', function () {

  const ScheduleTimeTable = AGN.Lib.ScheduleTimeTable;
  const DayRow = AGN.Lib.ScheduleTimeTable.DayRow;

  let scheduleTable;

  this.addDomInitializer('auto-import-export-scheduler', function () {
    const $el = this.el;
    const config = this.config;
    const data = config.intervalAsJson || [];

    scheduleTable = new ScheduleTimeTable($el);
    setScheduleTimeTable($el, scheduleTable);

    let period = [];
    if (!_.isEmpty(data)) {
      period = JSON.parse(data).map(object => DayRow.deserialize(object)).filter(obj => obj);
    }

    period.forEach(day => scheduleTable.addRow(day));

    if (period.length === 0) {
      scheduleTable.addEmptyDayRow();
    }

    const form = AGN.Lib.Form.get($el);
    form.initFields();

    if (!form.editable) {
      $el.find(':input').prop('disabled', true);
      $el.find('a').addClass('disabled');
    }
  });

  this.addAction({click: 'add-day'}, function () {
    scheduleTable.addEmptyDayRow();
  });

  this.addAction({click: 'remove-day'}, function () {
    scheduleTable.deleteRow(DayRow.get(this.el));
  });

  this.addAction({click: 'add-schedule'}, function () {
    DayRow.get(this.el).addSchedule();
  });

  this.addAction({click: 'remove-schedule'}, function () {
    DayRow.get(this.el).removeSchedule(this.el);
  });

  this.addAction({click: 'time-checkbox-toggle'}, function () {
    DayRow.get(this.el).timeCheckboxToggle(this.el);
  });

  this.addAction({click: 'hour-checkbox-toggle'}, function () {
    DayRow.get(this.el).hourCheckboxToggle(this.el);
  });

  this.addAction({change: 'validate-changes'}, function () {
    DayRow.get(this.el).validateTimeIntervals();
  });

  function setScheduleTimeTable($el, scheduleTable) {
    $el.closest('[data-controller]').data('_schedule_table', scheduleTable);
  }

  this.addAction({click: 'check-connection'}, function () {
    const form = AGN.Lib.Form.get(this.el);

    $.ajax(this.el.attr('href'), {
      type: 'POST',
      dataType: 'html',
      enctype: 'multipart/form-data',
      processData: false,
      contentType: false,
      data: form.data()
    }).done(resp => AGN.Lib.Page.render(resp));
  });

});