jQuery.extend( jQuery.fn.pickadate.defaults, {
  monthsFull:       t('date.monthsFull'),
  monthsShort:      t('date.monthsShort'),
  weekdaysFull:     t('date.weekdaysFull'),
  weekdaysShort:    t('date.weekdaysShort'),
  labelMonthNext:   t('date.nextMonth'),
  labelMonthPrev:   t('date.prevMonth'),
  labelMonthSelect: t('date.selectMonth'),
  labelYearSelect:  t('date.selectYear'),
  today:            t('defaults.today'),
  clear:            t('defaults.delete'),
  close:            t('defaults.close'),
  firstDay:         t('date.firstDayOfWeek'),
  format:           t('date.format'),
  hiddenName: true
});

jQuery.extend( jQuery.fn.pickatime.defaults, {
  clear:  t('defaults.delete'),
  format: t('time.format'),
  hiddenName: true
});
