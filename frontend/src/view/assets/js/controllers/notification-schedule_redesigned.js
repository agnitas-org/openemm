AGN.Lib.Controller.new('notification-schedule', function () {

  let $beginDate;
  let $endDate;
  let $beginTime;
  let $endTime;

  this.addDomInitializer('notification-schedule', function () {
    const $scope = this.el.closest('.tile');

    $beginDate = $scope.find('#startDate');
    $endDate = $scope.find('#endDate');
    $beginTime = $scope.find('#startTime');
    $endTime = $scope.find('#endTime');

    $beginDate.on('change', calculateEndDateAndConstraints)
    $beginTime.on('change', calculateEndDateAndConstraints);

    $endDate.on('change', disableEndDateCalculation);
    $endTime.on('change', disableEndDateCalculation);
  });

  function calculateEndDateAndConstraints() {
    const begin = getBeginDate();
    $endDate.unbind('change');

    if (begin) {
      const end = getMaxExpiredDate(begin);
      assignEndDateConstraints(begin, end);
      assignEndDate(end);
      assignEndTime($beginTime.val());
    } else {
      assignEndDateConstraints(0, null);
      assignEndDate(null);
      assignEndTime(null);
    }
    $endDate.on('change', disableEndDateCalculation);
  }

  function calculateConstraints() {
    const begin = getBeginDate();

    if (begin) {
      assignEndDateConstraints(begin, getMaxExpiredDate(begin));
    } else {
      assignEndDateConstraints(0, null);
    }
  }

  function getBeginDate() {
    return $beginDate.datepicker('getDate');
  }

  function assignEndDate(date) {
    $endDate.datepicker('setDate', date);
  }

  function assignEndDateConstraints(min, max) {
    $endDate.datepicker('option', {
      minDate: min,
      maxDate: max
    });
  }

  function assignEndTime(time) {
    $endTime.val(time);
  }

  // An implementation compatible with java's code.
  function getMaxExpiredDate(startDate) {
    const expiredDate = new Date(startDate);
    expiredDate.setDate(expiredDate.getDate() + 28);
    return expiredDate;
  }

  function disableEndDateCalculation() {
    $endDate.unbind('change');
    $endTime.unbind('change');

    $beginDate.unbind('change');
    $beginDate.on('change', calculateConstraints);
    $beginTime.unbind('change');
    $beginTime.on('change', calculateConstraints);
  }
});
