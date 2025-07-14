(() => {
  const Modal = AGN.Lib.Modal;
  const Form = AGN.Lib.Form;
  const XlCalendarLabel = AGN.Lib.Dashboard.XlCalendarLabel;

  class CommentLabel extends XlCalendarLabel {

    static TYPE = 'comment';
    static SELECTOR = XlCalendarLabel.SELECTOR + '--comment';

    constructor(data) {
      super(data);
    }

    attachToDay() {  // override of the base method
      this.$day.find(`${this.Calendar.DAY_SELECTOR}__body`).prepend(this.$el);
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

    get popoverOptions() {
      return {
        trigger: 'hover',
        html: true,
        text: this.data.comment.replace(/\n/g, '<br>')
      }
    }

    moveTo($day) {
      this.data.date = $day.data('date');
      $.post(this.saveUrl, this.data).done(resp => {
        if (resp.commentId) {
          $day.find(`${this.Calendar.DAY_SELECTOR}__body`).prepend(this.$el);
        }
        this.show();
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
      const instance = XlCalendarLabel.getByEntityId(data.commentId, CommentLabel.TYPE);
      if (instance) {
        return instance; // already initialized
      }
      return new CommentLabel(data);
    }
  }
  AGN.Lib.Dashboard.XlCalendarCommentLabel = CommentLabel;
})();
