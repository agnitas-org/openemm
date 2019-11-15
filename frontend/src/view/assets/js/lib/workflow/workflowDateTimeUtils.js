(function() {

  var DateFormat = AGN.Lib.DateFormat,
    DateTimeUtils  = {};

  DateTimeUtils.toDateTime = function(date, hour, minute) {
    var value = null;
    if (!!date && date.getMonth) {
      value = new Date(date);

      if (!!hour && hour !== 0) {
        value.setHours(hour);
      }

      if (!!minute && minute !== 0) {
        value.setMinutes(minute);
      }
    }

    return value;
  };

  DateTimeUtils.getDateTimeValue = function(date, hour, minute) {
    var value = DateTimeUtils.toDateTime(date, hour, minute);
    return value != null ? value.getTime() : null;
  };


  DateTimeUtils.getDateTimeStr = function(date, hour, minute, datePattern) {
    if (!date || !date.getMonth()) {
      return "";
    }

    hour = hour || date.getHours();
    minute = minute || date.getMinutes();

    var dateTime = DateTimeUtils.toDateTime(date, hour, minute);

    if (!!dateTime) {
      return DateFormat.format(dateTime, datePattern)
    } else {
      return "";
    }
  };

  DateTimeUtils.getDateStr = function(date, datePattern) {
    return DateTimeUtils.getDateTimeStr(date, null, null, datePattern);
  };

  AGN.Lib.WM.DateTimeUtils = DateTimeUtils;
})();