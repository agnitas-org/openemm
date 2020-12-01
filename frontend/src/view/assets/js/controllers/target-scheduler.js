AGN.Lib.Controller.new('target-scheduler', function () {

    var ScheduleTimeTable = AGN.Lib.ScheduleTimeTable,
      PeriodRow = AGN.Lib.ScheduleTimeTable.PeriodRow;

    var scheduleTimeTable;

    this.addAction({submission: 'save-scheduler-data'}, function () {
        var form = AGN.Lib.Form.get($(this.el));
        form.setValueOnce("intervalAsJson", scheduleTimeTable.getSubmissionJson('period'));
        form.submit();
    });

    this.addDomInitializer('target-scheduler-init', function ($el) {
        var data = $el.json();

        if (scheduleTimeTable) {
            scheduleTimeTable.clean();
        }

        scheduleTimeTable = new ScheduleTimeTable($('#tile-schedule-time'));

        var period = [];
        if (data.intervalAsJson) {
            period = JSON.parse(data.intervalAsJson).map(function (object) {
                return PeriodRow.deserialize(object);
            });
        }

        period.forEach(function (day) {
            scheduleTimeTable.addRow(day);
        });

        if (period.length === 0) {
            scheduleTimeTable.addEmptyPeriodRow();
        }

        var form = AGN.Lib.Form.get($el);
        form.initFields();
    });

});