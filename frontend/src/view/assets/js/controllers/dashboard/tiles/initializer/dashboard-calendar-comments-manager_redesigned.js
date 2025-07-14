(() => {
  const Form = AGN.Lib.Form;
  const Modal = AGN.Lib.Modal;
  const Storage = AGN.Lib.Storage;
  const Label = AGN.Lib.Dashboard.DashboardCalendarLabel;
  const CommentLabel = AGN.Lib.Dashboard.DashboardCalendarCommentLabel;

  class DashboardCalendarCommentsManager {

    constructor(calendar) {
      this.calendar = calendar;
      $('#toggle-calendar-comments').prop('checked', this.isCommentsShown);
    }

    get isCommentsShown() {
      return Storage.get('dashboard.calendar.comments');
    }

    set isCommentsShown(show) {
      return Storage.set('dashboard.calendar.comments', show);
    }

    toggle(show) {
      this.calendar.$grid.find(CommentLabel.SELECTOR).toggle(show);
      if (show && !this.calendar.commentsLoaded) {
        const startDate = $(`${this.calendar.daySelector}:first`).data('date');
        const endDate = $(`${this.calendar.daySelector}:last`).data('date');
        this.#load(startDate, endDate);
      }
      this.isCommentsShown = show;
    }

    create(date) {
      CommentLabel.showModal({ date });
    }

    save(commentId) {
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

    remove(id) {
      Label.getByEntityId(id, CommentLabel.TYPE).remove();
    }

    #load(startDate, endDate) {
      $
        .get(AGN.url('/calendar/comments.action'), {startDate, endDate})
        .done(comments => _.each(comments, comment => CommentLabel.getOrCreate(comment)));
    }
  }

  AGN.Lib.Dashboard.DashboardCalendarCommentsManager = DashboardCalendarCommentsManager;
})();
