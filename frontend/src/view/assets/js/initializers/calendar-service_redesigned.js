(function () {
  const DateFormat = AGN.Lib.DateFormat;
  const Template = AGN.Lib.Template;
  let CalendarService;

   CalendarService = function () {
//     let dayWidth = 0;
//
    let config;
    let localeDatePattern;
    let currentServerDateTime;
    let firstDayOfWeek;
    let curWeek;
//     let pushStatusSent;
//     let pushStatusScheduled;
//     let secondsBeforeWaitMessage;
//     let adminName;
//
//     let urlCalendarUnsent;
//     let urlCalendarPlanned;
    let urlCalendarMailingsList;
//     let urlCalendarMailingsMove;
//     let urlCalendarPushesList;
//     let urlCalendarPushesMove;
//     let urlCalendarAutoOptimization;
//     let urlCalendarCommentList;
//     let urlCalendarCommentSave;
//     let urlCalendarCommentRemove;
    let urlMailingView;
    let urlMailingStatisticsView;
    let isStatisticsViewPermitted;
//
//     let monthNames = t('date.monthsFull');
//
    let curDate = new Date();
    curDate.setHours(0, 0, 0, 0);
    let curMonth = curDate.getMonth();
    let curYear = curDate.getFullYear();
    let curDay = curDate.getDate();

//     let monthMode = true;
//     let showComments = false;
    let showCommentsByDay = {};
//     let viewMode = 'MONTH';
//     let weeksListArray = [];
//
//     let secondsAfterLastRequest = 0.0;
    let requestInProgress = false;
    let requestNumberActive = 0;

//     /*getters */
//     function getMonthMode() {
//       return monthMode;
//     }
//
//     this.getMonthMode = getMonthMode;
//
//     function getDayWidth() {
//       return dayWidth;
//     }
//
//     this.getDayWidth = getDayWidth;

    function getCurMonth() {
      return curMonth;
    }

    this.getCurMonth = getCurMonth;

    function getCurYear() {
      return curYear;
    }

    this.getCurYear = getCurYear;

//     function getCurDay() {
//       return curDay;
//     }
//
//     this.getCurDay = getCurDay;
//
//     /*setters*/
//     function setDayWidth(val) {
//       dayWidth = val;
//     }
//
//     this.setDayWidth = setDayWidth;

    // todo constructor?
    function setUp(conf) {
      localeDatePattern = conf.localeDatePattern;
      currentServerDateTime = new Date(conf.currentServerTime);
      firstDayOfWeek = conf.firstDayOfWeek;
      curWeek = getWeek(curDate, firstDayOfWeek);
      // pushStatusSent = conf.pushStatusSent;
      // pushStatusScheduled = conf.pushStatusScheduled;
      // adminName = conf.adminName;
      //
      // secondsBeforeWaitMessage = conf.secondsBeforeWaitMessage;
      // urlCalendarUnsent = conf.urls.CALENDAR_UNSENT_MAILINGS;
      // urlCalendarPlanned = conf.urls.CALENDAR_PLANNED_MAILINGS;
      urlCalendarMailingsList = conf.urls.CALENDAR_MAILINGS_LIST;
      // urlCalendarMailingsMove = conf.urls.CALENDAR_MAILINGS_MOVE;
      // urlCalendarPushesList = conf.urls.CALENDAR_PUSHES_LIST;
      // urlCalendarPushesMove = conf.urls.CALENDAR_PUSHES_MOVE;
      // urlCalendarAutoOptimization = conf.urls.CALENDAR_AUTO_OPTIMIZATION;
      // urlCalendarCommentList = conf.urls.CALENDAR_COMMENT_LIST;
      // urlCalendarCommentSave = conf.urls.CALENDAR_COMMENT_SAVE;
      // urlCalendarCommentRemove = conf.urls.CALENDAR_COMMENT_REMOVE;
      //
      urlMailingView = conf.urls.MAILING_VIEW;
      urlMailingStatisticsView = conf.urls.MAILING_STATISTICS_VIEW;
      isStatisticsViewPermitted = conf.isStatisticsViewPermitted;
      config = conf;
    }

    this.setUp = setUp;

//     function toggleDateSelectionMode(dateMode) {
//       if (dateMode == 'SELECT_MONTH') {
//         $('#linkSelectByMonth').toggleClass('active');
//         $('#linkSelectByWeek').toggleClass('active');
//
//         $("#dateSelectByMonth").css("display", "inline");
//         $("#dateSelectByWeek").css("display", "none");
//         changeCalendarMode(dateMode);
//         viewMode = 'MONTH';
//       }
//       if (dateMode == 'SELECT_WEEK') {
//         $('#linkSelectByMonth').toggleClass('active');
//         $('#linkSelectByWeek').toggleClass('active');
//
//         $("#dateSelectByMonth").css("display", "none");
//         $("#dateSelectByWeek").css("display", "inline");
//         createWeeksList($('#weeks_list_year')[0]);
//         changeCalendarMode(dateMode);
//         viewMode = 'WEEK';
//       }
//     }
//
//     this.toggleDateSelectionMode = toggleDateSelectionMode;
//
//     function checkProgress() {
//       if (requestInProgress) {
//         secondsAfterLastRequest += 0.5;
//         if (secondsAfterLastRequest >= secondsBeforeWaitMessage) {
//           $('#calendar-wait-notification').show();
//         }
//       }
//       else {
//         secondsAfterLastRequest = 0.0;
//       }
//     }
//
//     this.checkProgress = checkProgress;
//
//     function startProgress() {
//       requestInProgress = true;
//       requestNumberActive++;
//     }
//
    // this.startProgress = startProgress;
    //
    // function stopProgress() {
    //   requestNumberActive--;
    //   if (requestNumberActive <= 0) {
    //     requestNumberActive = 0;
    //     requestInProgress = false;
    //     $('#calendar-wait-notification').hide();
    //   }
    // }
    //
    // this.stopProgress = stopProgress;

    function generateMonthCalendar(month, year) {
      showCommentsByDay = {};

      let date = new Date(year, month, 1);
      let monthDaysCount = daysInMonth(month + 1, year);

      let offset = date.getDay() - firstDayOfWeek;
      if (offset < 0) {
        offset = 7 + offset;
      }

      let weeksCount = Math.ceil((monthDaysCount + offset) / 7);
      let startDate = new Date(year, month, 1 - offset);
      let endDate = new Date(year, month, 1 + weeksCount * 7);

      date = new Date(startDate);
      for (let weekIndex = 0; weekIndex < weeksCount; weekIndex++) {
        const trId = 'row-' + (weekIndex + 1);
        const weekNumber = getWeek(date, firstDayOfWeek);

        $('#calendar-container').append(`<tr id="${trId}"><td><div class="calendar-cell">${weekNumber}</div></td></tr>`);
        
        for (let index = 0; index < 7; index++) {
          const alienMonth = (date.getMonth() !== month);
          addDayToCalendar(date, trId, alienMonth);
          date.setDate(date.getDate() + 1);
        }
      }
      handleCalendarMailings(startDate, endDate);
      
//       getCalendarPushes(startDate, endDate);
//       getAutoOptimizations(startDate, endDate);
//
//       registerDayContainerHandlers();
//       setAllCommentsCheckState();
//
//       adjustSidebarHeight();
//
      synchronizeMonthsList(month, year);
    }

    this.generateMonthCalendar = generateMonthCalendar;
//
//     function generateWeekCalendar(month, year, day) {
//       let weekMilliseconds = 604800000;
//       showCommentsByDay = {};
//       let date = new Date(year, month, day);
//       let diff = date.getDay();
//       date.setDate(day - date.getDay() - 1 + firstDayOfWeek);
//       let startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
//       startDate.setDate(startDate.getDate() + 1);
//       curWeek = startDate.getWeek(firstDayOfWeek);
//       if (month == 0 && day == 1 && startDate.getWeek(firstDayOfWeek) != 1) {
//         startDate.setTime(startDate.getTime() + weekMilliseconds);
//         date.setTime(date.getTime() + weekMilliseconds);
//       }
//
//       $('#calendar-container').append('<tr id="week-row"></tr>');
//       $('#week-row').append('<td class="calendar-week-number-cell">' + startDate.getWeek(firstDayOfWeek) + '</td>');
//       for (let i = 0; i <= 6; i++) {
//         date.setDate(date.getDate() + 1);
//         addDayToCalendar(date, AGN.Lib.DateFormat.format(date, localeDatePattern), 'calendar-week-day-container', false, 'week-row', false);
//       }
//       $('#calendar-days').html(AGN.Lib.DateFormat.format(startDate, localeDatePattern) + ' - ' + AGN.Lib.DateFormat.format(date, localeDatePattern));
//
//       getComments(startDate, date);
//       getMailings(startDate, date);
//       getCalendarPushes(startDate, date);
//       getAutoOptimizations(startDate, date);
//
//       registerDayContainerHandlers();
//
//       adjustSidebarHeight();
//
//       synchronizeWeeksList(month, year, day);
//     }
//
//     this.generateWeekCalendar = generateWeekCalendar;
//
//     function addDragging($badge, stack) {
//       let $popup;
//
//       $badge.draggable({
//         stack: stack,
//         revert: 'invalid',
//         distance: 20,
//         start: function () {
//           $popup = $('#popup-' + $badge.prop('id'));
//           $popup.hide();
//           $popup.detach();
//           clearSelection();
//         },
//         stop: function () {
//           $('#calendar-popup-holder').append($popup);
//         }
//       });
//     }
//
//     function addMailingDragging($badge) {
//       addDragging($badge, '.calendar-mail-label');
//     }
//
//     this.addMailingDragging = addMailingDragging;
//
//     function addCommentDragging($badge) {
//       addDragging($badge, '.calendar-comment');
//     }
//
//     this.addCommentDragging = addCommentDragging;
//
//     function checkHideCommentIconsVisibility() {
//       $('.calendar-comments-toggle').css('visibility', 'hidden');
//       $('.calendar-day-container').has(".calendar-comment").each(function () {
//         $(this).find('.calendar-comments-toggle').css('visibility', 'visible');
//       });
//       adjustSidebarHeight();
//     }
//
//     this.checkHideCommentIconsVisibility = checkHideCommentIconsVisibility;
//
//     function generateUnsentMailingsTable() {
//       getUnsentMailings();
//     }
//
//     this.generateUnsentMailingsTable = generateUnsentMailingsTable;
//
//     function generatePlannedMailingsTable() {
//       getPlannedMailings();
//     }
//
//     this.generatePlannedMailingsTable = generatePlannedMailingsTable;
//
//     function clearSelection() {
//       let sel;
//       if (document.selection && document.selection.empty) {
//         document.selection.empty();
//       }
//       else if (window.getSelection) {
//         sel = window.getSelection();
//         if (sel && sel.removeAllRanges)
//           sel.removeAllRanges();
//       }
//     }
//
//     this.clearSelection = clearSelection;
//
    function daysInMonth(month, year) {
      return new Date(year, month, 0).getDate();
    }

    this.daysInMonth = daysInMonth;

//     function changeCalendarMode(dateMode) {
//       let curSelectedMonth = (dateMode == 'SELECT_MONTH');
//       if (curSelectedMonth && monthMode || !curSelectedMonth && !monthMode) {
//         if (monthMode) {
//           synchronizeMonthsList(curMonth, curYear)
//         } else {
//           synchronizeWeeksList(curMonth, curYear, curDay)
//         }
//         return;
//       }
//       removeCurrentCalendar();
//       if (curSelectedMonth) {
//         let testYears = new Date(curYear, curMonth, curDay);
//         testYears.setDate(testYears.getDate() + 7);
//         if (curWeek == 1 && curYear < testYears.getFullYear()) {
//           curMonth = 0;
//           curYear++;
//           curDay = 1;
//         }
//         generateMonthCalendar(curMonth, curYear);
//         monthMode = true;
//         showComments = false;
//       }
//       else {
//         generateWeekCalendar(curMonth, curYear, curDay);
//         monthMode = false;
//         showComments = true;
//       }
//     }
//
//     this.changeCalendarMode = changeCalendarMode;
//
    function removeCurrentCalendar() {
      $('#calendar-container tr:not(:first)').remove();
      // $('#calendar-popup-holder').empty();
      // $('.calendar-mail-label').remove();
    }

    this.removeCurrentCalendar = removeCurrentCalendar;
//
//     function prevEntry() {
//       if (monthMode) {
//         prevMonth();
//       }
//       else {
//         switchWeek(true);
//       }
//     }
//
//     this.prevEntry = prevEntry;
//
//     function nextEntry() {
//       if (monthMode) {
//         nextMonth();
//       }
//       else {
//         switchWeek(false);
//       }
//     }
//
//     this.nextEntry = nextEntry;
//
//     function switchWeek(back) {
//       let dayDiff = back ? -7 : 7;
//       removeCurrentCalendar();
//       let date = new Date(curYear, curMonth, curDay);
//       date.setDate(date.getDate() + dayDiff);
//       curDay = date.getDate();
//       curMonth = date.getMonth();
//       curYear = date.getFullYear();
//       date.setDate(date.getDate() + dayDiff);
//       let monthYears = $('#month_list_year option');
//       let minYear = Number(monthYears[monthYears.length - 1].value);
//       let maxYear = Number(monthYears[0].value);
//       if (date.getFullYear() < minYear) {
//         addMinYearControl();
//       }
//       if (date.getFullYear() > maxYear) {
//         addMaxYearControl();
//       }
//       generateWeekCalendar(curMonth, curYear, curDay);
//     }
//
//     this.switchWeek = switchWeek;
//
//     function prevMonth() {
//       removeCurrentCalendar();
//       curMonth--;
//       if (curMonth < 0) {
//         curMonth = 11;
//         curYear--;
//       }
//       let monthYears = $('#month_list_year option');
//       let minYear = Number(monthYears[monthYears.length - 1].value);
//       if (curYear < minYear) {
//         addMinYearControl();
//       }
//       generateMonthCalendar(curMonth, curYear);
//     }
//
//     this.prevMonth = prevMonth;
//
//     function nextMonth() {
//       removeCurrentCalendar();
//       curMonth++;
//       if (curMonth > 11) {
//         curMonth = 0;
//         curYear++;
//       }
//       let monthYears = $('#month_list_year option');
//       let maxYear = Number(monthYears[0].value);
//       if (curYear > maxYear) {
//         addMaxYearControl();
//       }
//       generateMonthCalendar(curMonth, curYear);
//     }
//
//     this.nextMonth = nextMonth;
//

    function addDayToCalendar(date, trId, alienMonth) {
      const today = !alienMonth && isToday(date);
      const dateStr = dateToServiceFormat(new Date(date.getFullYear(), date.getMonth(), date.getDate()));
      const newId = 'day-' + dateStr;
      $('#' + trId).append(`<td id="${newId}"></td>`);
      $('#' + newId).append(`<div class="calendar-cell calendar-day ${today ? 'calendar-day--today' : ''} ${alienMonth ? 'calendar-day--alien' : ''}" id="label-${newId}" data-date="${dateStr}">${date.getDate().toString().padStart(2, '0')}</div>`);
      // addDayPopup(newId);
    }

     this.addDayToCalendar = addDayToCalendar;
     
    function isToday(date) {
       const renderDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
       return renderDate.getTime() === curDate.getTime();
     }
//
//     function addCommentsShowButton(dayId) {
//       $('#label-' + dayId).append('<button type="button" class="calendar-comments-toggle" title="' + t("calendar.title.show_hide_comment") + '"><i class="icon icon-comments"></i></button>');
//       $('#' + dayId + ' .calendar-comments-toggle').css('visibility', 'hidden');
//     }
//
//     this.addCommentsShowButton = addCommentsShowButton;
//
//     function addDayPopup(dayId) {
//       $('#' + 'label-' + dayId).append('<button type="button" class="calendar-comments-add" title="' + t("calendar.title.new_comment") + '"><i class="icon icon-plus"></i></button>');
//       $('#' + 'label-' + dayId + ' .calendar-comments-add').css('visibility', 'hidden');
//       $('#' + dayId).mouseover(function () {
//         if (!$('#' + dayId).hasClass("calendar-another-month-day")) {
//           $('#' + 'label-' + dayId + ' .calendar-comments-add').css('visibility', 'visible');
//         }
//       });
//       $('#' + dayId).mouseout(function () {
//         $('#' + 'label-' + dayId + ' .calendar-comments-add').css('visibility', 'hidden');
//       });
//     }
//
//     this.addDayPopup = addDayPopup;
//
//     function toggleComment(dayId) {
//       if (showCommentsByDay['day-' + dayId]) {
//         $('#day-' + dayId + ' .calendar-comment').hide();
//       }
//       else {
//         $('#day-' + dayId + ' .calendar-comment').show();
//       }
//       showCommentsByDay['day-' + dayId] = !showCommentsByDay['day-' + dayId];
//       adjustSidebarHeight();
//     }
//
//     this.toggleComment = toggleComment;
//
//     function validateComment(commentText) {
//       if (commentText == '') {
//         $('#calendar-comment-error').html(t("calendar.error.empty_comment"));
//         return false;
//       }
//       if (byteCount(commentText) > 1000) {
//         $('#calendar-comment-error').html(t("calendar.error.long_comment"));
//         return false;
//       }
//       return true;
//     }
//
//     this.validateComment = validateComment;
//
//     function parseRecipients(recipients) {
//       if (recipients) {
//         let splitted = recipients.split(/[,; \t\n]+/);
//         let emails = [];
//
//         for (let i = 0; i < splitted.length; i++) {
//           if (splitted[i]) {
//             emails.push(splitted[i]);
//           }
//         }
//         return emails;
//       } else {
//         return [];
//       }
//     }
//
//     this.parseRecipients = parseRecipients;
//
//     function validateEmails(emails) {
//       if (!emails || emails.length == 0) {
//         $('#calendar-comment-error').html(t("calendar.error.empty_recipient_list"));
//         return false;
//       }
//       let recipients = emails.join(',');
//       if (byteCount(recipients) > 2000) {
//         $('#calendar-comment-error').html(t("calendar.error.long_recipient_list"));
//         return false;
//       }
//       let re = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i;
//       for (let i = 0; i < emails.length; i++) {
//         if (!re.test(emails[i])) {
//           $('#calendar-comment-error').html(t("calendar.error.invalid_email") + " " + emails[i]);
//           return false;
//         }
//       }
//       return true;
//     }
//
//     this.validateEmails = validateEmails;
//
//     function showCommentDialog(reminder) {
//       AGN.Lib.Modal.create($('#calendar-dialog-modal-template').html());
//
//       AGN.Lib.ReminderService.fillCommentDialog(reminder);
//       $('#calendar-comment-error').html('');
//       AGN.Lib.ReminderService.updateVisibility();
//     }
//
//     this.showCommentDialog = showCommentDialog;
//
//     function addComment(reminder) {
//       let commentId = 'comment-' + reminder.id;
//       let dayId = 'day-' + reminder.getDateAsString();
//       let text = reminder.text.replace(/\n/g, '<br>');
//
//       let $day = $('#' + dayId);
//       let $comment = $('#' + commentId);
//
//       if ($comment.exists() || !$day.exists()) {
//         return;
//       }
//
//       $comment = $(
//           '<div id="' + commentId + '" data-comment-id="' + reminder.id + '" class="calendar-mail-label calendar-comment">' +
//           '<span class="calendar-badge comment-badge">' +
//           '<span class="comment-text-content">' + text + '</span>' +
//           '</span>' +
//           '</div>'
//       );
//
//       if (!showCommentsByDay[dayId]) {
//         $comment.css('display', 'none');
//       }
//
//       $day.append($comment);
//
//       let $popupHolder = $('#calendar-popup-holder');
//
//       $popupHolder.append('<input type="hidden" id="deadline-' + commentId + '" name="' + commentId + '" value="' + reminder.isDeadline + '"/>');
//       $popupHolder.append('<input type="hidden" id="notified-' + commentId + '" name="' + commentId + '" value="' + reminder.isNotified + '"/>');
//       $popupHolder.append('<input type="hidden" id="emm-user-type-' + commentId + '" name="' + commentId + '" value="' + reminder.isEmmUserType + '"/>');
//       $popupHolder.append('<input type="hidden" id="notify-admin-id-' + commentId + '" name="' + commentId + '" value="' + reminder.notifyAdminId + '"/>');
//       $popupHolder.append('<input type="hidden" id="recipients-' + commentId + '" name="' + commentId + '" value="' + reminder.recipients + '"/>');
//       $popupHolder.append('<input type="hidden" id="plannedSendDate-' + commentId + '" name="' + commentId + '" value="' + reminder.getPlannedSendDateAsString() + '"/>');
//
//       $comment.dblclick(function () {
//         AGN.Lib.CalendarService.showCommentDialog(AGN.Lib.ReminderService.getReminder(commentId, reminder.currentAdminName));
//       });
//
//       let $removeButton = $('<button type="button" id="remove-' + commentId + '" class="calendar-comments-remove"><i class="icon icon-trash-o"></i></button>');
//
//       $comment.find('.comment-badge').append($removeButton);
//
//       $removeButton.attr('title', t("calendar.title.delete_comment"));
//       $removeButton.on("click", function () {
//         $('#deadline-' + commentId).remove();
//         $('#popup-' + commentId).remove();
//         $comment.remove();
//         removeCommentDB(commentId);
//         checkHideCommentIconsVisibility();
//         adjustSidebarHeight();
//         return false;
//       });
//
//       $comment.mouseover(function () {
//         $removeButton.css('display', 'block');
//       });
//       $comment.mouseout(function () {
//         $removeButton.css('display', 'none');
//       });
//
//       addCommentDragging($comment);
//       createCommentFullTextPopup(reminder.id, text);
//     }
//
//     this.addComment = addComment;
//
//     function createCommentFullTextPopup(commentId, commentText) {
//       $('#calendar-popup-holder').append('<div class="calendar-mail-popup" id="popup-comment-' + commentId + '"></div>');
//       $('#popup-comment-' + commentId).append('<div class="arrow"></div>');
//       $('#popup-comment-' + commentId).append('<div class="calendar-mail-popup-header">' + t("calendar.common.comment") + '</div>');
//       $('#popup-comment-' + commentId).append('<div class="calendar-mail-popup-content"><p class="comment-text-content">' + commentText + '</p></div>');
//
//       $('#popup-comment-' + commentId).css("display", "none");
//       $('#popup-comment-' + commentId).css("word-wrap", "break-word");
//       $('#comment-' + commentId).mouseover(function () {
//         let $popup = $('#popup-comment-' + commentId),
//             $trigger = $(this);
//
//         let offsetTop = $trigger.offset().top - $(window).scrollTop(),
//             triggerHeight = $trigger.outerHeight(),
//             popupHeight = $popup.outerHeight(),
//             margin = 15,
//             posTop = $trigger.offset().top + triggerHeight + margin;
//
//         if ((popupHeight + margin) < offsetTop) {
//           posTop = $trigger.offset().top - popupHeight - margin;
//
//           $popup.addClass('top');
//           $popup.removeClass('bottom');
//         } else {
//           $popup.addClass('bottom');
//           $popup.removeClass('top');
//         }
//         $popup.css({
//           top: posTop,
//           left: $trigger.offset().left + ($trigger.outerWidth() / 2) - ($popup.outerWidth() / 2)
//         });
//
//         $popup.css('z-index', '1500');
//         $popup.css('display', 'block');
//       });
//       $('#comment-' + commentId).mouseout(function () {
//         $('#popup-comment-' + commentId).css('display', 'none');
//       });
//     }
//
//     function doCommentDrop($droppable, $draggable, commentId) {
//       let date = $droppable.data('date');
//
//       if (!showCommentsByDay['day-' + date] && !$('.js-calendar-toggle-all-comments').prop('checked')) {
//         $draggable.hide();
//       }
//       $droppable.append($draggable);
//
//       saveCommentDB(AGN.Lib.ReminderService.getReminder("comment-" + commentId, adminName));
//       adjustSidebarHeight();
//
//       checkHideCommentIconsVisibility();
//     }
//
//     function doMailingDrop($droppable, $draggable, mailingId) {
//       let date = $droppable.data('date');
//
//       if ($draggable.attr('sent') != 'true') {
//         let sendTime = $draggable.data('time');
//         let sendDateTime = dateFromServiceFormat(date, sendTime);
//
//         if (sendDateTime >= currentServerDateTime) {
//           if ($draggable.attr('planned') == 'true') {
//             let $a = $draggable.find('a');
//             if ($a.hasClass('calendar-mailing-status-planned-in-past')) {
//               let statusClass = 'calendar-' + $draggable.attr('workstatus').replace(/\./g, '-');
//               $a.addClass(statusClass).removeClass('calendar-mailing-status-planned-in-past');
//             }
//           }
//
//           let genDate = new Date(sendDateTime);
//
//           // Generation is going to happen 3 hours earlier than the actual sending
//           genDate.setHours(genDate.getHours() - 3);
//
//           // Remove time if generation date in the past
//           if (genDate < currentServerDateTime) {
//             sendTime = null;
//             $draggable.data('time', null);
//           }
//
//           let $badges = $droppable.find('.calendar-mail-label');
//
//           // place in grid
//           let inserted = false;
//           if (sendTime) {
//             $($badges.get().reverse()).each(function () {
//               let $badge = $(this);
//               let foundTime = $badge.data('time');
//
//               if (foundTime && foundTime < sendTime) {
//                 $badge.after($draggable);
//                 inserted = true;
//               }
//             });
//           }
//
//           if (!inserted) {
//             let $lastMailingBadge = $badges.filter('[data-mailing-id]').last();
//             if ($lastMailingBadge.length) {
//               $lastMailingBadge.after($draggable);
//               inserted = true;
//             } else {
//               $droppable.append($draggable);
//             }
//           }
//
//           adjustSidebarHeight();
//
//           saveMailingMoveDB(mailingId, date);
//         }
//       }
//     }
//
//     function doPushDrop($droppable, $draggable, pushId) {
//       let date = $droppable.data('date');
//
//       switch ($draggable.data('status')) {
//         case pushStatusSent:
//           return;
//
//         case pushStatusScheduled:
//           if (dateFromServiceFormat(date, "00:00") < curDate) {
//             return;
//           }
//           break;
//       }
//
//       let $lastPushBadge = $droppable.find('.calendar-mail-label')
//           .filter('[data-push-id]')
//           .last();
//
//       if ($lastPushBadge.length) {
//         $lastPushBadge.after($draggable);
//       } else {
//         $droppable.append($draggable);
//       }
//
//       savePushMoveDB(pushId, date);
//     }
//
//     function registerDayContainerHandlers() {
//       $('.calendar-day-container').droppable({
//         hoverClass: 'calendar-day-hover',
//         drop: function (event, ui) {
//           let $this = $(this);
//
//           let entityType = 'UNKNOWN';
//           let entityId = ui.draggable.data('comment-id');
//           if (entityId) {
//             entityType = 'COMMENT';
//           } else {
//             entityId = ui.draggable.data('mailing-id');
//             if (entityId) {
//               entityType = 'MAILING';
//             } else {
//               entityId = ui.draggable.data('push-id');
//               if (entityId) {
//                 entityType = 'PUSH';
//               }
//             }
//           }
//
//           if (entityType == 'UNKNOWN') {
//             return;
//           }
//
//           ui.draggable.css({
//             top: '',
//             left: ''
//           });
//
//           switch (entityType) {
//             case 'COMMENT':
//               doCommentDrop($this, ui.draggable, entityId);
//               break;
//
//             case 'MAILING':
//               doMailingDrop($this, ui.draggable, entityId);
//               break;
//
//             case 'PUSH':
//               doPushDrop($this, ui.draggable, entityId);
//               break;
//           }
//         }
//       });
//     }
//
//     function adjustSidebarHeight() {
//       let height = $('#calendar-table').height();
//       $('.calendar-sidebar').css({'max-height': height + 'px'});
//     }
//
//     this.adjustSidebarHeight = adjustSidebarHeight;
//
//
//     // Ajax methods that work with EMM server - side
//
//     function saveMailingMoveDB(mailingId, date) {
//       return $.ajax({
//         type: 'POST',
//         url: urlCalendarMailingsMove,
//         data: {
//           mailingId: mailingId,
//           date: date
//         }
//       });
//     }
//
//     function savePushMoveDB(pushId, date) {
//       return $.ajax({
//         type: 'POST',
//         url: urlCalendarPushesMove,
//         data: {
//           pushId: pushId,
//           date: date
//         }
//       });
//     }
//
//     function updateComment(reminder) {
//       let id = 'comment-' + reminder.id;
//       let $comment = $('#' + id);
//
//       $('#deadline-' + id).val(reminder.isDeadline);
//       $('#emm-user-type-' + id).val(reminder.isEmmUserType);
//       $('#notify-admin-id-' + id).val(reminder.notifyAdminId);
//       $('#recipients-' + id).val(reminder.recipients);
//
//       let text = reminder.text.replace(/\n/g, '<br>');
//       addCommentDragging($comment);
//       $comment.find('.comment-text-content').html(text);
//       $('#popup-' + id + ' .comment-text-content').html(text);
//
//       $('#plannedSendDate-' + id).val(reminder.getPlannedSendDateAsString());
//     }
//
//     function saveCommentDB(reminder) {
//       $.ajax({
//         type: "POST",
//         url: urlCalendarCommentSave,
//         data: {
//           commentId: reminder.id,
//           date: reminder.getDateAsString(),
//           comment: reminder.text,
//           deadline: reminder.isDeadline,
//           sendNow: reminder.isSendNow,
//           emmUserType: reminder.isEmmUserType,
//           notifyAdminId: reminder.notifyAdminId,
//           recipients: reminder.recipients,
//           notified: reminder.isNotified,
//           plannedSendDate: reminder.getPlannedSendDateAsString()
//         },
//         success: function (data) {
//           if (typeof data.commentId == 'undefined') {
//             AGN.Lib.Page.render(data);
//           } else {
//             reminder.isSendNow = false;
//             if (reminder.id == 0) {
//               reminder.id = data.commentId;
//               addComment(reminder);
//               checkHideCommentIconsVisibility();
//             } else {
//               updateComment(reminder);
//             }
//           }
//         }
//       });
//     }
//
//     this.saveCommentDB = saveCommentDB;
//
//     function removeCommentDB(commentId) {
//       $.ajax({
//         type: "POST",
//         url: urlCalendarCommentRemove,
//         data: {
//           commentId: AGN.Lib.ReminderService.getReminderIdFromTag(commentId)
//         }
//       });
//     }
//
//     this.removeCommentDB = removeCommentDB;

     function handleCalendarMailings(startDate, endDate) {
       $.get(urlCalendarMailingsList, {
         startDate: dateToServiceFormat(startDate),
         endDate: dateToServiceFormat(endDate)
       }).done(data => assignMailingsToCalendarDays(data));
     }

     function assignMailingsToCalendarDays(mailings) {
       mailings.forEach(mailing => mailing.link = getMailingLink(mailing));
       const mailingsByDays = _.groupBy(mailings, 'sendDate');
       
       makeDaysSelectable(mailingsByDays);
       makeCurrentDayActive(mailingsByDays);
     }

     function makeCurrentDayActive(mailingsBySendDate) {
       const currentDateStr = dateToServiceFormat(new Date());
       $('#day-' + currentDateStr).click(() => changeSelectedDay(currentDateStr, mailingsBySendDate[currentDateStr]));
       changeSelectedDay(currentDateStr, mailingsBySendDate[currentDateStr]);
     }

     function makeDaysSelectable(mailingsByDays) {
       _.forEach($('.calendar-day'), calendarDay => {
         const $calendarDay = $(calendarDay);
         const dateStr = $calendarDay.data("date");
         const dayMailings = mailingsByDays[dateStr];
         if (!_.isEmpty(dayMailings)) {
           $('#label-day-' + dateStr).addClass('calendar-day--selectable');
         }
         $calendarDay.click(() => changeSelectedDay(dateStr, _.sortBy(dayMailings, 'sendTime')))
       });
     }
     
     function changeSelectedDay(dateStr, dayMailings) {
       $('#calendar-container').find('.calendar-day--selected').removeClass('calendar-day--selected');
       $('#label-day-' + dateStr).addClass('calendar-day--selected');
       const dayDate = DateFormat.parseFormat(dateStr, "dd-MM-yy");
       const dayOfWeek = dayDate.toLocaleString(window.adminLocale, {weekday: 'long'})
       dateStr = AGN.Lib.DateFormat.format(dayDate, window.adminDateFormat);
       $('#calendar-tile-day-mailings').html(Template.dom("dashboard-schedule-day", {dayOfWeek, dateStr, dayMailings}));
     }

     // function setMailings(d) {
    //   let $day = $('#day-' + d.sendDate);
    //   if (!$day.exists()) {
    //     return;
    //   }
    //   let mailingLink = getMailingLink(d);
    //   $day.data("mailings", d);
      
      
      // let $day = $('#day-' + d.sendDate);
      // let $badge = $('#' + d.mailingId);
      //
      // if ($day.exists() && !$badge.exists()) {
      //   let statusClass;
      //   let mailingLink = getMailingLink(d);
      //
      //   if (d.plannedInPast) {
      //     statusClass = 'calendar-mailing-status-planned-in-past';
      //   } else {
      //     statusClass = 'calendar-' + d.workstatus.replace(".", "-");
      //   }
      //
      //   let musData = {
      //     'sent': d.sent,
      //     'planned': d.planned,
      //     'mailingId': d.mailingId,
      //     'statusClass': statusClass,
      //     'mailingLink': mailingLink,
      //     'workstatus': d.workstatus,
      //     'shortname': d.shortname,
      //     'sendTime': d.sendTime
      //   };
      //
      //   $badge = AGN.Lib.Template.dom("calendar-mail-link", musData);
      //
      //   $day.append($badge);
      //
      //   musData.subject = d.subject;
      //   musData.mailsSent = d.mailsSent;
      //   // musData.previewImage = getPreviewImage(d.preview_component, d.isOnlyPostType);.
      //   // musData.popupClass = d.preview_component ? "" : "calendar-mail-popup-preview-missing";
      //   musData.emptyWorkstatus = !d.workstatus.trim();
      //   musData.openers = d.openers;
      //   musData.clickers = d.clickers;
      //   musData.workstatusIn = d.workstatusIn;
      //
      //   let $popup = AGN.Lib.Template.dom("calendar-mail-popup", musData);
      //
      //   $('#calendar-popup-holder').append($popup);
      //
      //   AGN.Lib.CalendarService.addMailingDragging($badge);
      // }
    // }

    function getMailingLink(d) {
      return d.sent && d.workstatus !== 'mailing.status.test' && isStatisticsViewPermitted
        ? urlMailingStatisticsView.replace(':mailingId:', d.mailingId)
        : urlMailingView.replace(':mailingId:', d.mailingId);
    }

//     function getPreviewImage(previewComp, isPostType) {
//       let link;
//
//       if (isPostType) {
//         link = AGN.url("/assets/core/images/facelift/post_thumbnail.jpg");
//       } else if (previewComp) {
//         link = AGN.url("/sc?compID=" + previewComp);
//       } else {
//         link = AGN.url("/assets/core/images/facelift/no_preview.svg");
//       }
//
//       return link;
//     }
//
//     function getCalendarDateForPushNotification(data) {
//       switch (data.status) {
//         case pushStatusSent:
//           return data.sendDate;
//
//         case pushStatusScheduled:
//           return data.startDate;
//
//         default:
//           return dateToServiceFormat(dateFromArray(data.plannedFor));
//       }
//     }
//
//     function getCalendarPushes(startDate, endDate) {
//       startProgress();
//       $.ajax({
//         type: "GET",
//         url: urlCalendarPushesList,
//         data: {
//           startDate: dateToServiceFormat(startDate),
//           endDate: dateToServiceFormat(endDate)
//         }
//       }).done(function (data) {
//         let createBadge = AGN.Lib.Template.prepare('calendar-planned-push-notification');
//         $.each(data, function (index, d) {
//           let $badge = $(createBadge(d));
//
//           $('#day-' + getCalendarDateForPushNotification(d)).append($badge);
//           addMailingDragging($badge);
//         });
//         adjustSidebarHeight();
//       }).always(stopProgress);
//     }
//
//     function getAutoOptimizations(startDate, endDate) {
//       startProgress();
//       $.ajax({
//         type: "GET",
//         url: urlCalendarAutoOptimization,
//         data: {
//           startDate: dateToServiceFormat(startDate),
//           endDate: dateToServiceFormat(endDate)
//         }, success: function (data) {
//           let createBadge = AGN.Lib.Template.prepare('calendar-auto-optimization');
//
//           $.each(data, function (index, d) {
//             let $badge = $(createBadge({
//               optimizationId: d.campaignID,
//               shortname: d.shortname,
//               linkUrl: getOptimizationLinkUrl(d),
//               status: d.autoOptimizationStatus
//             }));
//             $('#day-' + d.sendDate).append($badge);
//           });
//           adjustSidebarHeight();
//         }
//       }).always(stopProgress);
//     }
//
//     function getOptimizationLinkUrl(data) {
//       if (data.workflowId == 0) {
//         return "optimization/" + data.id + "/view.action";
//       } else {
//         return "workflow/" + data.workflowId + "/view.action";
//       }
//     }
//
//     function getComments(startDate, endDate) {
//       $.ajax({
//         type: "GET",
//         url: urlCalendarCommentList,
//         data: {
//           startDate: dateToServiceFormat(startDate),
//           endDate: dateToServiceFormat(endDate)
//         },
//         success: function(data) {
//           $.each(data, function(index, c) {
//             setComment(c);
//           });
//
//           AGN.Lib.CalendarService.checkHideCommentIconsVisibility();
//           setAllCommentsCheckState();
//           adjustSidebarHeight();
//         }
//       });
//     }
//
//     function setComment(c) {
//       let reminder = _.cloneDeep(AGN.Lib.ReminderService.reminder);
//       let recipientAdminId = c.recipients.adminId;
//
//       reminder.id = c.commentId;
//       reminder.setDate(c.date);
//       reminder.text = c.comment;
//       reminder.notifyAdminId = recipientAdminId;
//       reminder.isEmmUserType = recipientAdminId != 0;
//       reminder.currentAdminName = c.adminName;
//       reminder.isDeadline = c.deadline;
//       reminder.isNotified = c.notified;
//       reminder.setPlannedSendDate(c.plannedSendDates);
//       reminder.recipients = c.recipients.value;
//
//       AGN.Lib.CalendarService.addComment(reminder);
//     }
//
//     function getUnsentMailings() {
//       $.ajax({
//         type: "GET",
//         url: urlCalendarUnsent,
//         success: function (data) {
//           $(".calendar-sidebar-content").html(data);
//           adjustSidebarHeight();
//           AGN.Lib.CoreInitializer.run(['tooltip', 'scrollable'], $('.calendar-wrapper'));
//         }
//       });
//     }
//
//     function getPlannedMailings() {
//       $.ajax({
//         type: "GET",
//         url: urlCalendarPlanned,
//         success: function (data) {
//           $(".calendar-sidebar-content").html(data);
//           adjustSidebarHeight();
//           AGN.Lib.CoreInitializer.run(['tooltip', 'scrollable'], $('.calendar-wrapper'));
//         }
//       });
//     }
//
    function addMaxYearControl() {
      let monthYears = $('#month_list_year option');
      // let weekYears = $('#weeks_list_year option');
      let lastYear = Number(monthYears[monthYears.length - 1].value);
      for (let i = 0; i < monthYears.length; i++) {
        let currentYear = Number(monthYears[i].value) + 1;
        monthYears[i].text = currentYear;
        monthYears[i].value = currentYear;
        // weekYears[i].text = currentYear;
        // weekYears[i].value = currentYear;
      }
      monthYears[monthYears.length] = new Option(lastYear, lastYear);
      // weekYears[weekYears.length] = new Option(lastYear, lastYear);
    }

//     function addMinYearControl() {
//       let monthYears = $('#month_list_year option');
//       let weekYears = $('#weeks_list_year option');
//       let lastYear = Number(monthYears[monthYears.length - 1].value) - 1;
//       monthYears[monthYears.length] = new Option(lastYear, lastYear);
//       weekYears[weekYears.length] = new Option(lastYear, lastYear);
//     }
//
    function showDate() {
      let selectedIndex;
      // if (viewMode == 'MONTH') {
      const month = $('#month_list option:selected').val();
      const year = $('#month_list_year option:selected').val();
      const resultDate = new Date(year, month, 1);
      const strDate = DateFormat.format(resultDate, DateFormat.getLocaleDateFormat());
      selectedIndex = $('#month_list_year option:selected').index();
      // }
      // if (viewMode == 'WEEK') {
      //   let weekNumber = $('#weeks_list option:selected').val();
      //   for (let j in weeksListArray) {
      //     if (!weeksListArray.hasOwnProperty(j)) continue;
      //     if (weeksListArray[j].number == weekNumber) {
      //       strDate = AGN.Lib.DateFormat.format(weeksListArray[j].firstDay, getDateFormat());
      //       break;
      //     }
      //   }
      //   selectedIndex = $('#weeks_list_year option:selected').index();
      // }
      if (selectedIndex == 0) {
        addMaxYearControl()
      }
      jumpToDate(strDate);
    }

    this.showDate = showDate;

    function jumpToDate(strDate) {
      let date = DateFormat.parseFormat(strDate, DateFormat.getLocaleDateFormat());
      curMonth = date.getMonth();
      curYear = date.getFullYear();
      curDay = date.getDate();
      removeCurrentCalendar();
      // if (monthMode) {
        generateMonthCalendar(curMonth, curYear);
      // }
      // else {
      //   generateWeekCalendar(curMonth, curYear, curDay);
      // }
    }

    this.jumpToDate = jumpToDate;

//     function jumpToday() {
//       let todayDate = AGN.Lib.DateFormat.format(new Date(), getDateFormat())
//       jumpToDate(todayDate);
//     }
//
//     this.jumpToday = jumpToday;
//
//     function synchronizeWeeksList(month, year, day) {
//       let weekMilliseconds = 604800000;
//       let date = new Date(year, month, day);
//       date.setDate(day - date.getDay() - 1 + firstDayOfWeek);
//       let startWeekDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
//       startWeekDate.setDate(startWeekDate.getDate() + 1);
//
//       if (month == 0 && day == 1 && startWeekDate.getWeek(firstDayOfWeek) != 1) {
//         startWeekDate.setTime(startWeekDate.getTime() + weekMilliseconds);
//       }
//       let weekNumber = startWeekDate.getWeek(firstDayOfWeek);
//       if (weekNumber == 1) {
//         let endWeekDate = new Date(startWeekDate.valueOf() + weekMilliseconds);
//         if (startWeekDate.getFullYear() != endWeekDate.getFullYear()) {
//           year = endWeekDate.getFullYear();
//         }
//       }
//       $('#weeks_list_year')[0].selectedIndex = $('#weeks_list_year option[value = ' + year + ']')[0].index;
//       createWeeksList($('#weeks_list_year')[0]);
//       $('#weeks_list')[0].selectedIndex = $('#weeks_list option[value = ' + weekNumber + ']')[0].index;
//       $('#weeks_list_year').select2({minimumResultsForSearch: -1});
//       $('#weeks_list').select2({minimumResultsForSearch: -1});
//     }

    function synchronizeMonthsList(month, year) {
      $('#month_list_year')[0].selectedIndex = $('#month_list_year option[value = ' + year + ']')[0].index;
      $('#month_list')[0].selectedIndex = $('#month_list option[value = ' + month + ']')[0].index;
      AGN.runAll($('#month_list_year'));
      AGN.runAll($('#month_list'));
      // $('#month_list').select2({minimumResultsForSearch: -1});
      // $('#month_list_year').select2({minimumResultsForSearch: -1});
    }

//     function createWeeksList(pThis) {
//       weeksListArray = [];
//       let currentWeek;
//       let weekMilliseconds = 604800000;
//       let year = pThis.options[pThis.selectedIndex].value;
//       let lastYearDay = new Date(year, 11, 31);
//       let firstWeekDay = new Date(year, 0, 1);
//       firstWeekDay.setDate(1 - firstWeekDay.getDay() - 1 + firstDayOfWeek);
//       firstWeekDay.setDate(firstWeekDay.getDate() + 1);
//       let lastWeekDay = new Date(firstWeekDay.getFullYear(), firstWeekDay.getMonth(), firstWeekDay.getDate());
//       lastWeekDay.setTime(firstWeekDay.getTime() + weekMilliseconds - 86400000);
//       if (firstWeekDay.getWeek(firstDayOfWeek) != 1) {
//         firstWeekDay.setTime(firstWeekDay.getTime() + weekMilliseconds);
//         lastWeekDay.setTime(lastWeekDay.getTime() + weekMilliseconds);
//       }
//       do {
//         currentWeek = new Object();
//         currentWeek.number = firstWeekDay.getWeek(firstDayOfWeek);
//         currentWeek.firstDay = new Date(firstWeekDay.getFullYear(), firstWeekDay.getMonth(), firstWeekDay.getDate());
//         currentWeek.lastDay = new Date(lastWeekDay.getFullYear(), lastWeekDay.getMonth(), lastWeekDay.getDate());
//         weeksListArray.push(currentWeek);
//         firstWeekDay.setTime(firstWeekDay.getTime() + weekMilliseconds);
//         lastWeekDay.setTime(lastWeekDay.getTime() + weekMilliseconds);
//       } while ((firstWeekDay.getWeek(firstDayOfWeek) != 1) && (firstWeekDay.getFullYear() == year));
//       $('#weeks_list option').remove();
//       for (let j in weeksListArray) {
//         if (!weeksListArray.hasOwnProperty(j)) continue;
//         let optElem = document.createElement('option');
//         let firstDay = AGN.Lib.DateFormat.format(weeksListArray[j].firstDay, localeDatePattern);
//         let lastDay = AGN.Lib.DateFormat.format(weeksListArray[j].lastDay, localeDatePattern);
//         optElem.value = weeksListArray[j].number;
//         optElem.innerHTML = weeksListArray[j].number + ' : ' + firstDay + ' - ' + lastDay;
//         $('#weeks_list').append(optElem);
//       }
//     }
//
//     this.createWeeksList = createWeeksList;

    // helper methods
    function pad(num, size) {
      let s = num + "";
      while (s.length < size) s = "0" + s;
      return s;
    }

    function dateToServiceFormat(date) {
      let dd = pad(date.getDate(), 2);
      // Convert 0-based month number to 1-based
      let mm = pad((date.getMonth() + 1), 2);
      let yyyy = date.getFullYear();

      return dd + '-' + mm + '-' + yyyy;
    }

//     function dateFromArray(d) {
//       if (d) {
//         let yyyy = d[0];
//         // Convert 1-based month number to 0-based
//         let mm = d[1] - 1;
//         let dd = d[2];
//
//         return new Date(yyyy, mm, dd);
//       }
//       return null;
//     }
//
//     function dateFromServiceFormat(dateString, timeString) {
//       let res = dateString.match(/^(\d\d)-(\d\d)-(\d{4})$/);
//       if (res) {
//         let dd = res[1];
//         // Convert 1-based month number to 0-based
//         let mm = res[2] - 1;
//         let yyyy = res[3];
//
//         if (timeString) {
//           res = timeString.match(/^(\d\d):(\d\d)$/);
//           if (res) {
//             let hh = res[1];
//             let mi = res[2];
//             return new Date(yyyy, mm, dd, hh, mi);
//           }
//         }
//
//         return new Date(yyyy, mm, dd);
//       }
//       return null;
//     }
//
//     function setAllCommentsCheckState() {
//       if ($('.js-calendar-toggle-all-comments').prop('checked')) {
//         $('.calendar-table .calendar-comment').show();
//       }
//     }
//
//     this.setAllCommentsCheckState = setAllCommentsCheckState;
//
//     function byteCount(s) {
//       let escapedStr = encodeURI(s);
//       if (escapedStr.indexOf("%") != -1) {
//         let count = escapedStr.split("%").length - 1;
//         if (count == 0) count++;
//         let tmp = escapedStr.length - (count * 3);
//         count = count + tmp;
//       } else {
//         count = escapedStr.length;
//       }
//       return count;
//     }

     /**
      * Function returns the number of week for current date
      * @param firstWeekDay - the first day of week (0 = Sunday, 1 = Monday ...)
      */
     function getWeek(date, firstWeekDay) {
         // Create a copy of this date object
         var target = new Date(date.valueOf());
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
  };

  AGN.Lib.CalendarService = new CalendarService();

//   let reminderService = {
//
//     showDiv: function (id) {
//       $("div#calendar-dialog-modal div#" + id).css("display", "block")
//     },
//
//     hideDiv: function (id) {
//       $("div#calendar-dialog-modal div#" + id).css("display", "none")
//     },
//
//     updateVisibility: function () {
//       this.onScheduleChanged();
//       this.onDeadlineChanged();
//     },
//
//     onDeadlineChanged: function () {
//       if ($("#isDeadline:checked").val()) {
//         $("#remindDate").prop("disabled", false);
//         $("#remindTime").prop("disabled", false);
//         $(".js-open-datepicker").prop("disabled", false);
//         $(".js-open-timepicker").prop("disabled", false);
//       } else {
//         $("#remindDate").prop("disabled", true);
//         $("#remindTime").prop("disabled", true);
//         $(".js-open-datepicker").prop("disabled", true);
//         $(".js-open-timepicker").prop("disabled", true);
//       }
//       return false;
//
//     },
//
//     onUserTypeChanged: function (userType) {
//       if (userType == this.reminder.recipientEmm) {
//         $('#emmUsers').show();
//         $('#customRecipients').hide();
//       } else {
//         $('#customRecipients').show();
//         $('#emmUsers').hide();
//       }
//     },
//
//     onScheduleChanged: function () {
//       if ($("#scheduleReminder:checked").val()) {
//         $('#admin-for-notify').prop('disabled', false);
//         this.showDiv("reminderDetails");
//       } else {
//         $('#admin-for-notify').prop('disabled', true);
//         this.hideDiv("reminderDetails");
//       }
//       return false;
//     },
//
//     reminder: {
//       isDeadline: false,
//       isSendNow: false,
//       isEmmUserType: true,
//       recipientEmm: 1,
//       recipientCustom: 2,
//       defaultDate: {
//         date: "",
//         hours: 8,
//         minutes: 0
//       },
//
//       plannedSendDate: {
//         date: "",
//         hours: 8,
//         minutes: 0
//       },
//
//       getDateAsString: function () {
//         return $.datepicker.formatDate('dd-mm-yy', this.defaultDate.date);
//       },
//
//       setDate: function (date) {
//         this.defaultDate.date = $.datepicker.parseDate('dd-mm-yy', date);
//         this.plannedSendDate.date = $.datepicker.parseDate('dd-mm-yy', date);
//         this.plannedSendDate.hours = this.defaultDate.hours;
//         this.plannedSendDate.minutes = this.defaultDate.minutes;
//       },
//
//       getPlannedSendDateAsString: function () {
//         return $.datepicker.formatDate('dd-mm-yy', this.plannedSendDate.date) +
//             (this.plannedSendDate.hours < 10 ? ' 0' : ' ') + this.plannedSendDate.hours.toString() +
//             (this.plannedSendDate.minutes < 10 ? ':0' : ':') + this.plannedSendDate.minutes.toString();
//       },
//
//       setPlannedSendDate: function (dateTime) {
//         let dt = dateTime.split(' ');
//         this.plannedSendDate.date = $.datepicker.parseDate('dd-mm-yy', dt[0]);
//         let t = dt[1].split(':');
//         this.plannedSendDate.hours = parseInt(t[0]);
//         this.plannedSendDate.minutes = parseInt(t[1]);
//       },
//
//       getScheduleReminder: function () {
//         return this.isDeadline || this.isSendNow;
//       },
//
//       getRemindCalendarDate: function () {
//         if ((this.defaultDate.date.toString() == this.plannedSendDate.date.toString()) &&
//             (this.defaultDate.hours == this.plannedSendDate.hours) &&
//             (this.defaultDate.minutes == this.plannedSendDate.minutes)) {
//           return true;
//         } else {
//           return false;
//         }
//       },
//
//       onSave: function () {
//         reminderService.saveReminder(reminderService.reminder);
//       }
//     },
//
//     getNewReminder: function (date, conf) {
//       this.reminder.id = 0;
//       this.reminder.setDate(date);
//       this.reminder.text = "";
//       this.reminder.isEmmUserType = true;
//       this.reminder.notifyAdminId = conf.adminId;
//       this.reminder.recipients = '';
//       this.reminder.currentAdminName = conf.adminName;
//       this.reminder.isDeadline = false;
//       this.reminder.isNotified = false;
//       this.reminder.title = t("calendar.common.new_comment");
//       this.reminder.recipientEmm = conf.recipientEmm;
//       this.reminder.recipientCustom = conf.recipientCustom;
//       return this.reminder;
//     },
//
//     getReminder: function (commentId, adminName) {
//       let comment = $('#' + commentId + ' .comment-text-content').html() + '';
//       this.reminder.id = this.getReminderIdFromTag(commentId);
//       this.reminder.setDate(this.getDateFromTag($('#' + commentId).parent().attr('id')));
//       this.reminder.text = comment.replace(/<br>/gi, '\n');
//       this.reminder.isEmmUserType = ($('#emm-user-type-' + commentId).val() == 'true');
//       this.reminder.notifyAdminId = $('#notify-admin-id-' + commentId).val();
//       this.reminder.recipients = $('#recipients-' + commentId).val();
//       this.reminder.currentAdminName = adminName;
//       this.reminder.isDeadline = ($('#deadline-' + commentId).val() == 'true');
//       this.reminder.isNotified = ($('#notified-' + commentId).val() == 'true');
//       this.reminder.setPlannedSendDate($('#plannedSendDate-' + commentId).val());
//       this.reminder.title = t("calendar.common.edit_comment");
//       return this.reminder;
//     },
//
//     fillCommentDialog: function (reminder) {
//       let $modal = $('#calendar-dialog-modal');
//       $modal.find('.modal-title').html(reminder.title);
//       $modal.find('#comment-text').val(reminder.text);
//       $modal.find('#reminderDetails input[name="userType"][value=' + (reminder.isEmmUserType ? reminder.recipientEmm : reminder.recipientCustom) + ']').prop('checked', true).trigger('change');
//       $modal.find('#admin-for-notify').val(reminder.notifyAdminId);
//       AGN.Lib.Select.get($modal.find('#admin-for-notify')).selectValueOrSelectFirst(reminder.notifyAdminId);
//       $modal.find('#recipients-text').val(reminder.recipients);
//       $modal.find('#isDeadline').prop('checked', reminder.isDeadline);
//       $modal.find('#isSendNow').prop('checked', reminder.isSendNow);
//       $modal.find('#scheduleReminder').prop('checked', reminder.getScheduleReminder());
//       $modal.find('#remindDate').pickadate('picker').set('select', reminder.plannedSendDate.date);
//       $modal.find('#remindTime').val(reminder.plannedSendDate.hours + ':' + reminder.plannedSendDate.minutes);
//     },
//
//     saveReminder: function (reminder) {
//       let commentText = $('#comment-text').val();
//
//       if (AGN.Lib.CalendarService.validateComment(commentText)) {
//         let isEmmUserType = $('#isEmmUserType').prop('checked');
//         let recipients = AGN.Lib.CalendarService.parseRecipients($('#recipients-text').val());
//
//         if (isEmmUserType || AGN.Lib.CalendarService.validateEmails(recipients)) {
//           $('#calendar-dialog-modal').modal('hide');
//           reminder.text = commentText;
//
//           reminder.isEmmUserType = isEmmUserType;
//           if (isEmmUserType) {
//             reminder.notifyAdminId = $('#admin-for-notify').val();
//             reminder.recipients = '';
//           } else {
//             reminder.notifyAdminId = 0;
//             reminder.recipients = recipients.join(',');
//           }
//
//           reminder.isSendNow = $('#isSendNow').prop('checked');
//           reminder.isDeadline = $('#isDeadline').prop('checked');
//
//           if ($('#isDeadline').prop('checked') &&
//               $('#scheduleReminder').prop('checked')) {
//
//             let $datepicker = $('#remindDate').pickadate('picker'),
//                 $timepicker = $('#remindTime');
//
//             reminder.plannedSendDate.date = AGN.Lib.DateFormat.parseFormat($datepicker.get('select', 'dd-mm-yyyy'), 'dd-MM-yyyy');
//             reminder.plannedSendDate.hours = $timepicker.val().substring(0, 2);
//             reminder.plannedSendDate.minutes = $timepicker.val().substring(3, 5);
//
//           } else {
//             reminder.plannedSendDate.date = reminder.defaultDate.date;
//             reminder.plannedSendDate.hours = reminder.defaultDate.hours;
//             reminder.plannedSendDate.minutes = reminder.defaultDate.minutes;
//           }
//           AGN.Lib.CalendarService.saveCommentDB(reminder);
//         }
//       }
//     },
//
//     getDateFromTag: function (tag) {
//       return tag.substr(tag.indexOf('-') + 1);
//     },
//
//     getReminderIdFromTag: function (tag) {
//       return tag.substr(tag.indexOf('-') + 1);
//     }
//   };

  // AGN.Lib.ReminderService = reminderService
})();
