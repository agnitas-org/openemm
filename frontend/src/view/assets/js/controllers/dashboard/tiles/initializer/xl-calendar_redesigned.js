(() => {
  const Form = AGN.Lib.Form;
  const Template = AGN.Lib.Template;
  const Modal = AGN.Lib.Modal;
  const Storage = AGN.Lib.Storage;
  const Select = AGN.Lib.Select;
  const DateFormat = AGN.Lib.DateFormat;
  const Label = AGN.Lib.Dashboard.XlCalendarLabel;
  const CommentLabel = AGN.Lib.Dashboard.XlCalendarCommentLabel;
  const MailingLabel = AGN.Lib.Dashboard.XlCalendarMailingLabel;
  const AutoOptLabel = AGN.Lib.Dashboard.XlCalendarAutoOptLabel;
  const PushLabel = AGN.Lib.Dashboard.XlCalendarPushLabel;
  const CalendarBase = AGN.Lib.Dashboard.CalendarBase;

  class XlCalendar extends CalendarBase {

    static TABLE_SELECTOR = '#xl-calendar-table';
    static UNSENT_MAILINGS_LIST_SELECTOR = '#xl-calendar-unsent .tile-body';
    static DAY_CLASS = 'xl-calendar-day';
    static DAY_SELECTOR = `.${XlCalendar.DAY_CLASS}`;
    static UNSENT_MAILINGS_STORAGE_KEY = 'dashboard.xlCalendar.unsentType';
    static SHOW_COMMENTS_STORAGE_KEY = 'dashboard.xlCalendar.showComments';
    static MODE_STORAGE_KEY = 'dashboard.xlCalendar.mode';

    constructor($el, config) {
      super($el, config);

      this.config = config;
      this.dayMailingsLimit = config.showALlMailingsPerDay ? -1 : 5;
      this.initCalendarTable();
      this.initUnsentMailings();
    }

    get $table() {
      return $(XlCalendar.TABLE_SELECTOR);
    }

    get week() {
      return parseInt(this.$week.val());
    }

    get month() {
      return parseInt(this.$month.val());
    }

    get year() {
      return parseInt(this.$year.val());
    }

    get $week() {
      return $('#xl-calendar-week');
    }

    get $year() {
      return $('#xl-calendar-year');
    }

    get $month() {
      return $('#xl-calendar-month');
    }

    get $weeksColumn() {
      return $('#xl-calendar-weeks');
    }

    get weeksSelect() {
      return Select.get(this.$week);
    }

    get isCommentsShown() {
      return Storage.get(XlCalendar.SHOW_COMMENTS_STORAGE_KEY) !== false;
    }

    set isCommentsShown(shown) {
      Storage.set(XlCalendar.SHOW_COMMENTS_STORAGE_KEY, shown);
    }

    get isWeekMode() {
      return this.$el.hasClass('tile-x-wide')
        || Storage.get(XlCalendar.MODE_STORAGE_KEY) === 'week';
    }

    set mode(mode) {
      return Storage.set(XlCalendar.MODE_STORAGE_KEY, mode);
    }

    get unsentType() {
      return Storage.get(XlCalendar.UNSENT_MAILINGS_STORAGE_KEY) || 'unplanned';
    }

    set unsentType(type) {
      return Storage.set(XlCalendar.UNSENT_MAILINGS_STORAGE_KEY, type);
    }

    get currentDate() {
      if (this.isWeekMode) {
        return moment(Select.get(this.$week).$findOption(this.week).data('first-day'), 'DD-MM-YYYY').toDate();
      }

      const now = new Date();
      const date = new Date(this.year, this.month);

      if (date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()) {
        return now;
      }

      return date;
    }

    get unsentMailings$() {
      return $(XlCalendar.UNSENT_MAILINGS_LIST_SELECTOR);
    }

    $day(date) {
      return this.$table.find(`${XlCalendar.DAY_SELECTOR}[data-date="${date}"]`)
    }

    initCalendarTable() {
      const today = new Date();
      if (this.isWeekMode) {
        this.showWeek(today.getDate(), today.getMonth(), today.getFullYear());
      } else {
        this.showMonth(today.getMonth(), today.getFullYear());
      }
    }

    removeCurrentCalendar() {
      this.$table.empty();
      this.$weeksColumn.empty();
    }

    showWeek(day, month, year) {
      this.removeCurrentCalendar();
      var weekMilliseconds = 604800000;
      var date = new Date(year, month, day);
      date.setDate(day - date.getDay() - 1 + this.firstDayOfWeek);
      var startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
      startDate.setDate(startDate.getDate() + 1);
      if (month === 0 && day === 1 && this.getWeek(startDate) !== 1) {
        startDate.setTime(startDate.getTime() + weekMilliseconds);
        date.setTime(date.getTime() + weekMilliseconds);
      }
      for (let i = 0; i < 7; i++) {
        date.setDate(date.getDate() + 1);
        this.addDay(date, false);
      }
      const startDateStr = this.dateToServiceFormat(startDate);
      const endDateStr = this.dateToServiceFormat(date);
      this.fillCalendar(startDateStr, endDateStr);
      this.synchronizeWeeksList(day, month, year);
    }

    addWeekNumber(date) { // override
      this.$weeksColumn.append(`<div>${this.getWeek(date)}</div>`);
    }

    addDay(date, alien) {
      const dateStr = this.dateToServiceFormat(new Date(date.getFullYear(), date.getMonth(), date.getDate()));
      const $day = Template.dom('xl-calendar-day', { day: date.getDate(), date: dateStr})
        .addClass(this.isToday(date) ? `${XlCalendar.DAY_CLASS}--today` : '')
        .addClass(alien ? `${XlCalendar.DAY_CLASS}--alien` : '');
      this.$table.append($day);
      AGN.Lib.CoreInitializer.run(['scrollable', 'tooltip'], $day);
    }

    fillCalendar(startDateStr, endDateStr) {
      if (this.isCommentsShown) {
        this.getComments(startDateStr, endDateStr);
        this.commentsLoaded = true;
      }
      this.getMailings(startDateStr, endDateStr, this.dayMailingsLimit);
      this.getPushNotifications(startDateStr, endDateStr);
      this.getAutoOptimizations(startDateStr, endDateStr);
      this.initDroppables();
    }

    changeYear() {
      if (this.isWeekMode) {
        this.updateWeeksSelect();
      }
      this.flip();
    }

    flip() {
      this.jumpToDate(this.currentDate);
      if (this.lastYearSelected()) {
        this.addYearOption();
      }
    }

    toggleMode(isWeekMode) {
      const currentDate = this.currentDate;
      if (this.isWeekMode) {
        this.synchronizeMonthsList(currentDate.getMonth(), currentDate.getFullYear());
      } else {
        this.synchronizeWeeksList(currentDate.getDate(), currentDate.getMonth(), currentDate.getFullYear());
      }
      this.mode = isWeekMode ? 'week' : 'month';
      this.flip();
    }

    jumpToDate(date) {
      if (this.isWeekMode) {
        this.showWeek(date.getDate(), date.getMonth(), date.getFullYear());
      } else {
        this.showMonth(date.getMonth(), date.getFullYear());
      }
    }

    jumpToday() {
      this.jumpToDate(new Date());
    }

    initDroppables() {
      $(XlCalendar.DAY_SELECTOR).droppable({
        accept: Label.SELECTOR,
        drop: this.dropLabel
      });
      this.unsentMailings$.droppable({
        accept: '[id^="xl-calendar-mailing-"]',
        drop: this.dropLabel
      });
    }

    dropLabel(event, ui) {
      const $label = ui.draggable;
      Label.get($label).drop($(this));
    }

    getComments(startDate, endDate) {
      $
        .get(AGN.url('/calendar/comments.action'), {startDate, endDate})
        .done(comments => _.each(comments, comment => CommentLabel.getOrCreate(comment)));
    }

    initUnsentMailings() {
      $('[name="not-sent-mailings-list-type"]').on('change', (e) => {
        this.unsentType = $(e.target).val();
        this.loadUnsentMailings();
      })
      this.loadUnsentMailings();
      $(`#${this.unsentType}-mailings-btn`).prop('checked', true);
    }

    loadUnsentMailings() {
      $
        .get(AGN.url(`/calendar/unsent-mailings/${this.unsentType}.action`))
        .done(unsentMails => this.displayUnsentMailings(unsentMails));
    }

    displayUnsentMailings(unsentMails) {
      this.unsentMailings$.empty();
      unsentMails
        .map(mailing => this.asUnsentMailingData(mailing))
        .forEach(mailing => new MailingLabel(mailing));
    }

    asUnsentMailingData(mailing) {
      return {
        ...mailing,
        link: AGN.url(`/mailing/${mailing.mailingId}/settings.action`),
        mediatype: mailing.mediatype?.toLowerCase(),
        workstatus: 'mailing.status.' + mailing.status.toLowerCase(),
        labelId: `xl-calendar-unsent-${mailing.mailingId}`,
        inUnsentList: true,
        planned: this.unsentType === 'planned',
        sent: false,
        sendTime: false,
        sentCount: false
      };
    }

    showMoreMailings($btn) {
      const date = $btn.closest(XlCalendar.DAY_SELECTOR).data('date');
      this.getMailings(date, date, -1);
      $btn.remove();
    }

    getMailings(startDate, endDate, limit) {
      $
        .get(AGN.url('/calendar/mailings.action'), {startDate, endDate, limit: this.dayMailingsLimit + 1}) // +1 in order to detect if more mailings are present
        .done(mailings => _.each(mailings, mailing => this.addMailingLabel(mailing, limit)));
    }

    addMailingLabel(mailing, limit) {
      const $day = this.$day(mailing.sendDate);
      if (limit > 0 && $day.find('[id^="xl-calendar-mailing-"]').length >= limit) {
        if (!$day.find('[data-action="load-more-day-mailings"]').exists()) {
          $day.find(`${XlCalendar.DAY_SELECTOR}__body`).append(Template.text('xl-calendar-showMore-btn'));
        }
        return;
      }
      mailing.link = this.getMailingLink(mailing);
      mailing.sentCount = mailing.mailsSent;
      mailing.subject = mailing.subject || '';
      mailing.thumbnailComponentId = mailing.preview_component;

      MailingLabel.getOrCreate(mailing);
    }

    getAutoOptimizations(startDate, endDate) {
      $
        .get(AGN.url('/calendar/autoOptimization.action'), {startDate, endDate})
        .done(autoOpts => _.each(autoOpts, autoOpt => AutoOptLabel.getOrCreate(autoOpt)));
    }

    getPushNotifications(startDate, endDate) {
      PushLabel.pushStatusSent = this.config.pushStatusSent;
      PushLabel.pushStatusScheduled = this.config.pushStatusScheduled;
      $
        .get(AGN.url('/calendar/pushes.action'), {startDate, endDate})
        .done(pushes => _.each(pushes, push => PushLabel.getOrCreate(push)));
    }

    toggleComments(show) {
      this.$table.find(CommentLabel.SELECTOR).toggle(show);
      this.isCommentsShown = show;
      if (show && !this.commentsLoaded) {
        const startDate = $(`${XlCalendar.DAY_SELECTOR}:first`).data('date');
        const endDate = $(`${XlCalendar.DAY_SELECTOR}:last`).data('date');
        this.getComments(startDate, endDate);
      }
    }

    createComment(date) {
      CommentLabel.showModal({ date });
    }

    saveComment(commentId) {
      const commentForm = Form.get($('#calendar-comment-form'));
      if (!commentForm.validate()) {
        return;
      }
      const data = commentForm.data();
      $.post(AGN.url('/calendar/saveComment.action'), data).done(resp => {
        if (!resp.commentId) {
          AGN.Lib.Page.render(resp);
          return;
        }
        if (!commentId) {
          data.commentId = resp.commentId;
          CommentLabel.getOrCreate(data).$el.toggle(this.isCommentsShown);
        } else {
          Label.getByEntityId(commentId, CommentLabel.TYPE).updateComment(data);
        }
        Modal.getInstance(commentForm.$form).hide();
      });
    }

    removeComment(id) {
      Label.getByEntityId(id, CommentLabel.TYPE).remove();
    }

    updateWeeksSelect() {
      const weeksListArray = [];
      var currentWeek;
      var weekMilliseconds = 604800000;
      var firstWeekDay = new Date(this.year, 0, 1);
      firstWeekDay.setDate(1 - firstWeekDay.getDay() - 1 + this.firstDayOfWeek);
      firstWeekDay.setDate(firstWeekDay.getDate() + 1);
      var lastWeekDay = new Date(firstWeekDay.getFullYear(), firstWeekDay.getMonth(), firstWeekDay.getDate());
      lastWeekDay.setTime(firstWeekDay.getTime() + weekMilliseconds - 86400000);
      if (this.getWeek(firstWeekDay) != 1) {
        firstWeekDay.setTime(firstWeekDay.getTime() + weekMilliseconds);
        lastWeekDay.setTime(lastWeekDay.getTime() + weekMilliseconds);
      }
      do {
        currentWeek = {};
        currentWeek.number = this.getWeek(firstWeekDay);
        currentWeek.firstDay = new Date(firstWeekDay.getFullYear(), firstWeekDay.getMonth(), firstWeekDay.getDate());
        currentWeek.lastDay = new Date(lastWeekDay.getFullYear(), lastWeekDay.getMonth(), lastWeekDay.getDate());
        weeksListArray.push(currentWeek);
        firstWeekDay.setTime(firstWeekDay.getTime() + weekMilliseconds);
        lastWeekDay.setTime(lastWeekDay.getTime() + weekMilliseconds);
      } while ((this.getWeek(firstWeekDay) !== 1) && (firstWeekDay.getFullYear() === this.year));

      this.weeksSelect.resetOptions();

      for (var j in weeksListArray) {
        if (!weeksListArray.hasOwnProperty(j)) continue;
        var firstDay = DateFormat.format(weeksListArray[j].firstDay, window.adminDateFormat);
        var lastDay = DateFormat.format(weeksListArray[j].lastDay, window.adminDateFormat);
        this.weeksSelect.addOption(weeksListArray[j].number, weeksListArray[j].number + ' : ' + firstDay + ' - ' + lastDay);
        this.weeksSelect.$findOption(weeksListArray[j].number).data('first-day', this.dateToServiceFormat(weeksListArray[j].firstDay));
      }
    }

    synchronizeWeeksList(day, month, year) {
      var weekMilliseconds = 604800000;
      var date = new Date(year, month, day);
      date.setDate(day - date.getDay() - 1 + this.firstDayOfWeek);
      var startWeekDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
      startWeekDate.setDate(startWeekDate.getDate() + 1);

      if (month == 0 && day == 1 && this.getWeek(startWeekDate) != 1) {
        startWeekDate.setTime(startWeekDate.getTime() + weekMilliseconds);
      }
      var weekNumber = this.getWeek(startWeekDate);
      if (weekNumber == 1) {
        var endWeekDate = new Date(startWeekDate.valueOf() + weekMilliseconds);
        if (startWeekDate.getFullYear() != endWeekDate.getFullYear()) {
          year = endWeekDate.getFullYear();
        }
      }
      this.$year[0].selectedIndex = this.$year.find(`option[value=${year}]`)[0].index;
      AGN.runAll(this.$year);
      this.updateWeeksSelect();
      this.$week[0].selectedIndex = this.$week.find(`option[value=${weekNumber}]`)[0].index;
      AGN.runAll(this.$week);
    }

    static getLabelsByType($day, type) {
      return $day
        .find(Label.SELECTOR).map(label => Label.get($(label)))
        .filter(label => label.type === type);
    }

    static getLastLabelOfType($day, type) {
      return XlCalendar.getLabelsByType($day, type).slice(-1)[0];
    }
  }

  AGN.Lib.Dashboard.XlCalendar = XlCalendar;
})();
