AGN.Lib.Controller.new('target-group-view', function () {

  const Form = AGN.Lib.Form;
  const ScheduleTimeTable = AGN.Lib.ScheduleTimeTable;
  const PeriodRow = AGN.Lib.ScheduleTimeTable.PeriodRow;

  let scheduleTimeTable;

  AGN.Lib.Action.new({'qb:invalidrules': '#targetgroup-querybuilder'}, function () {
    AGN.Lib.Messages.alert('querybuilder.errors.general');
  });

  this.addDomInitializer('target-group-view', function () {
    if (this.config.errorPositionDetails) {
      handleEqlErrorDetails(this.config.errorPositionDetails);
    }
  });

  this.addAction({change: 'toggle-editor-tab'}, function () {
    const $el = this.el;
    const isChecked = $el.is(':checked');
    $el.prop('checked', !isChecked); // change switch to previous state (it will be changed after form submit)

    const form = Form.get($el);
    if (!form.validate({skip_empty: true})) {
      return;
    }

    form.setValueOnce('viewFormat', isChecked ? 'EQL' : 'QUERY_BUILDER');
    form.submit('', {skip_empty: true});
  });

  this.addAction({submission: 'save-target'}, function () {
    Form.get(this.el)
      .submit(this.el.data('submit-type'))
      .done(resp => {
        if (typeof resp == 'object' && !resp.success) {
          handleEqlErrorDetails(resp.data);
          AGN.Lib.JsonMessages(resp.popups);
        }
      });
  });

  function handleEqlErrorDetails(details) {
    AGN.Lib.Editor.get($('#eql')).goToLine(details.line, details.column);
  }

  // Scheduler

  this.addDomInitializer('target-scheduler-init', function () {
    scheduleTimeTable?.clean();
    scheduleTimeTable = new ScheduleTimeTable($('#target-scheduler-block'));

    let period = [];
    if (this.config.intervalAsJson) {
      period = JSON.parse(this.config.intervalAsJson)
        .map(object => PeriodRow.deserialize(object));
    }

    if (period.length === 0) {
      scheduleTimeTable.addEmptyPeriodRow();
    } else {
      period.forEach(day => scheduleTimeTable.addRow(day));
    }

    Form.get(this.el).initFields();
  });

  this.addAction({click: 'activate-schedule'}, function () {
    const url = this.el.attr('href');
    const intervalAsJson = scheduleTimeTable.getSubmissionJson('period');

    $.post(url, {intervalAsJson}).done(resp => AGN.Lib.Page.render(resp));
  });
});
