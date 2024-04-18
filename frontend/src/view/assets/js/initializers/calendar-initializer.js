// TODO: EMMGUI-714: check usage and remove when old design will be removed
AGN.Lib.DomInitializer.new("calendar-table", function () {
  var conf = this.config;

  AGN.Lib.CalendarService.setUp(conf);

  window.setInterval(AGN.Lib.CalendarService.checkProgress, 500);

  if (AGN.Lib.CalendarService.getMonthMode()) {
    AGN.Lib.CalendarService.generateMonthCalendar(AGN.Lib.CalendarService.getCurMonth(), AGN.Lib.CalendarService.getCurYear());
  } else {
    AGN.Lib.CalendarService.generateWeekCalendar(AGN.Lib.CalendarService.getCurMonth(), AGN.Lib.CalendarService.getCurYear(), AGN.Lib.CalendarService.getCurDay());
  }

  AGN.Lib.CalendarService.generateUnsentMailingsTable();

  var Action = AGN.Lib.Action;

  Action.new({'click': '#dateTimePicker'}, function() {
    this.event.stopPropagation();
  });

  Action.new({'click': '#dateTimePickerButton'}, function() {
    this.event.stopPropagation();
  });

  Action.new({'click': '.js-calendar-navigate-previous'}, function() {
    AGN.Lib.CalendarService.prevEntry();

    this.event.preventDefault();
    return false;
  });

  Action.new({'click': '.js-calendar-navigate-next'}, function() {
    AGN.Lib.CalendarService.nextEntry();

    this.event.preventDefault();
    return false;
  });

  Action.new({'click': '.js-calendar-navigate-today'}, function() {
    AGN.Lib.CalendarService.jumpToday();

    this.event.preventDefault();
    return false;
  });

  Action.new({'click': '.js-calendar-view-month'}, function() {
    AGN.Lib.CalendarService.toggleDateSelectionMode('SELECT_MONTH');

    this.event.preventDefault();
    return false;
  });

  Action.new({'click': '.js-calendar-view-week'}, function() {
    AGN.Lib.CalendarService.toggleDateSelectionMode('SELECT_WEEK');

    this.event.preventDefault();
    return false;
  });

  Action.new({'click': '.js-calendar-list-unsent'}, function() {
    AGN.Lib.CalendarService.generateUnsentMailingsTable();

    this.event.preventDefault();
    return false;
  });

  Action.new({'click': '.js-calendar-list-planned'}, function() {
    AGN.Lib.CalendarService.generatePlannedMailingsTable();

    this.event.preventDefault();
    return false;
  });

  Action.new({'click': '#comment-save-button'}, function() {
    AGN.Lib.ReminderService.reminder.onSave();
  });

  Action.new({'click': '.calendar-comments-add'}, function() {
    var $el = this.el,
        date;

    date = $el.parents('.calendar-day-container').data('date');

    AGN.Lib.CalendarService.showCommentDialog(AGN.Lib.ReminderService.getNewReminder(date, conf));

    return false;
  });

  Action.new({'click': '.calendar-comments-toggle'}, function() {
    var $el = this.el,
        date;

    date = $el.parents('.calendar-day-container').data('date');
    AGN.Lib.CalendarService.toggleComment(date);

    return false;
  });

  Action.new({'change': '.js-calendar-toggle-all-comments'}, function() {
    if (this.el.prop('checked')) {
      $('.calendar-table').find('.calendar-comment').show();
    } else {
      $('.calendar-table').find('.calendar-comment').hide();
    }

    AGN.Lib.CalendarService.adjustSidebarHeight();
  });

  Action.new({'change': '#scheduleReminder'}, function() {
    AGN.Lib.ReminderService.onScheduleChanged();
  });

  Action.new({'change': '#isDeadline'}, function() {
    AGN.Lib.ReminderService.onDeadlineChanged();
  });

  Action.new({'change': '#month_list, #month_list_year, #weeks_list'}, function() {
    AGN.Lib.CalendarService.showDate();
  });

  Action.new({'change': '#weeks_list_year'}, function() {
    AGN.Lib.CalendarService.createWeeksList(this.el[0]);
  });

  Action.new({'change': '#reminderDetails input[name="userType"]'}, function() {
    var $el = this.el[0];
    AGN.Lib.ReminderService.onUserTypeChanged($el.value);
  });

  this.addAction({'mouseover' : 'mailing-popup'}, function () {
    var $badge = this.el,
        $popup = $('#popup-' + $badge.data('mailing-id'));

    if ($popup.exists()) {
      var viewportTop = $(window).scrollTop();
      var popupHeight = $popup.outerHeight();

      var margin = 15;
      var badgeTop = $badge.offset().top;
      var badgeBottom = badgeTop + $badge.outerHeight();

      var spaceAbove = badgeTop - viewportTop - margin;
      var spaceBelow = viewportTop + $(window).outerHeight() - badgeBottom;
      var positionTop;

      if (popupHeight <= spaceAbove || spaceAbove > spaceBelow) {
        positionTop = badgeTop - margin - popupHeight;
        $popup.addClass('top');
        $popup.removeClass('bottom');
      } else {
        positionTop = badgeBottom + margin;
        $popup.addClass('bottom');
        $popup.removeClass('top');
      }

      $popup.css({
        top: positionTop,
        left: $badge.offset().left + ($badge.outerWidth() / 2) - ($popup.outerWidth() / 2)
      });

      $popup.css('z-index', '1500');
      $popup.css('display', 'block');
    }
  });

  this.addAction({'mouseout' : 'mailing-popup'}, function () {
    var mailingId = this.el.data('mailing-id');
    $('#popup-' + mailingId).css('display', 'none');
  });

});