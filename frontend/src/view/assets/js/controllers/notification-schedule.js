AGN.Lib.Controller.new('notification-schedule', function() {
  var $beginDate, $endDate, $beginTime, $endTime;

  this.addDomInitializer('notification-schedule', function($elem) {
    $beginDate = $elem.find('#startDate');
    $endDate = $elem.find('#endDate');
    $beginTime = $elem.find('#startTime');
    $endTime = $elem.find('#endTime');

    $beginDate.pickadate('picker').on('set', calculateEndDateAndConstraints);
    $beginTime.on('change', calculateEndDateAndConstraints);

    $endDate.pickadate('picker').on('set', disableEndDateCalculation);
    $endTime.on('change', disableEndDateCalculation);
  });

  function getBeginDate() {
    var date = $beginDate.pickadate('get', 'select');
    return date && new Date(date.obj);
  }

  function calculateEndDateAndConstraints() {
    var picker = $endDate.pickadate('picker');
    var begin = getBeginDate();

    picker.off('set');
    if (begin) {
      var end = getMaxExpiredDate(begin);
      assignEndDateConstraints(begin, end);
      assignEndDate(end);
      assignEndTime($beginTime.val());
    } else {
      assignEndDateConstraints(true, false);
      assignEndDate(null);
      assignEndTime(null);
    }
    picker.on('set', disableEndDateCalculation);
  }

  function calculateConstraints() {
    var begin = getBeginDate();

    if (begin) {
      assignEndDateConstraints(begin, getMaxExpiredDate(begin));
    } else {
      assignEndDateConstraints(true, false);
    }
  }

  function assignEndDate(date) {
    var picker = $endDate.pickadate('picker');
    if (date) {
      picker.set('select', date);
    } else {
      picker.clear();
    }
  }

  function assignEndDateConstraints(min, max) {
    var picker = $endDate.pickadate('picker');

    picker.set('min', min);
    picker.set('max', max);

    picker.set('select', picker.get('select'));
  }

  function assignEndTime(time) {
    $endTime.val(time);
  }

  // An implementation compatible with java's code.
  function getMaxExpiredDate(startDate) {
    var expiredDate = new Date(startDate);
    expiredDate.setDate(expiredDate.getDate() + 28);
    return expiredDate;
  }

  function disableEndDateCalculation() {
    $endDate.removeClass('enableable');
    $endTime.removeClass('enableable');

    $endDate.pickadate('off', 'set');
    $endTime.unbind('change');

    $beginDate.pickadate('off', 'set');
    $beginDate.pickadate('picker').on('set', calculateConstraints);
    $beginTime.unbind('change');
    $beginTime.on('change', calculateConstraints);
  }
});
