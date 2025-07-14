(() => {
  const Modal = AGN.Lib.Modal;
  const Form = AGN.Lib.Form;
  const DashboardCalendarLabel = AGN.Lib.Dashboard.DashboardCalendarLabel;

  class CommentLabel extends DashboardCalendarLabel {

    static TYPE = 'comment';
    static SELECTOR = DashboardCalendarLabel.SELECTOR + '--comment';

    constructor(data) {
      if (_.isNumber(data.date)) {
        data.date = moment(data.date).tz(window.agnTimeZoneId).format('DD-MM-YYYY');
      }
      if (_.isNumber(data.plannedSendDate)) {
        data.plannedSendDate = moment(data.plannedSendDate).tz(window.agnTimeZoneId).format(`${window.adminDateFormat.toUpperCase()} HH:mm`);
      }
      super(data);
    }

    attachToDay() {  // override of the base method
      this.calendar.$dayBody(this.$day).prepend(this.$el);
    }

    get templateParams() {
      return _.merge(super.templateParams, { comment: this.data.comment });
    }

    get entityId() {
      return this.data.commentId;
    }

    get form() {
      return Form.get($('#calendar-comment-form'));
    }

    get saveUrl() {
      return AGN.url('/calendar/saveComment.action');
    }

    createPopover() {
      // created with truncated-text-popover_redesigned.js
    }

    moveTo($day) {
      this.data.date = $day.data('date');
      return $.post(this.saveUrl, this.data).done(resp => {
        if (resp.commentId) {
          this.calendar.$dayBody($day).prepend(this.$el);
        }
      });
    }

    remove() {
      $
        .post(AGN.url('/calendar/removeComment.action'), {commentId: this.entityId})
        .done(() => super.remove());
    }

    updateComment(data) {
      this.data = data;
      this.$elText.text(this.data.comment);
      this.createPopover();
    }

    showModal() {
      CommentLabel.showModal(this.data);
    }

    static showModal(data) {
      data = _.extend(CommentLabel.createNewEmptyComment(data.date), data);
      data.comment = data.comment?.replace(/<br>/gi, '\n');
      if (!data.recipients) {
        data.recipients = [];
      }
      data.recipients = (Array.isArray(data.recipients) ? data.recipients : data.recipients.split(','))
        .map(email => email.trim())
        .filter(email =>  email !== "");
      data.isCustomRecipients = !!data.recipients.length;
      data.sendReminder = !!data.deadline;
      Modal.fromTemplate('calendar-comment-modal', data);
    }

    static createNewEmptyComment(date) {
      return {
        commentId: 0,
        comment: '',
        recipients: [],
        deadline: false,
        plannedSendDate: moment(date, 'DD-MM-YYYY').format(window.adminDateFormat.toUpperCase()) + ' 08:00'
      }
    }

    static getOrCreate(data) {
      const instance = DashboardCalendarLabel.getByEntityId(data.commentId, CommentLabel.TYPE);
      if (instance) {
        return instance; // already initialized
      }
      return new CommentLabel(data);
    }
  }
  AGN.Lib.Dashboard.DashboardCalendarCommentLabel = CommentLabel;
})();
