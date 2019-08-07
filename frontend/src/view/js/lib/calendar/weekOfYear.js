/**
 * Function returns the number of week for current date
 * @param firstWeekDay - the first day of week (0 = Sunday, 1 = Monday ...)
 */
Date.prototype.getWeek = function (firstWeekDay) {

    // Create a copy of this date object
    var target = new Date(this.valueOf());
    var year = target.getFullYear();
    var month = target.getMonth();
    var day = target.getDate();
    // Make sure weeks won't be displayed wrong for US week setting
    if (firstWeekDay == 0) {
        day = day + 1;
    }
    month = month + 1;
    var a = Math.floor((14 - month) / 12);
    var y = year + 4800 - a;
    var m = month + 12 * a - 3;
    var J = day + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - Math.floor(y / 100) + Math.floor(y / 400) - 32045;
    var d4 = (((J + 31741 - (J % 7)) % 146097) % 36524) % 1461;
    var L = Math.floor(d4 / 1460);
    var d1 = ((d4 - L) % 365) + L;
    var week = Math.floor(d1 / 7) + 1;
    return week;

}