(() => {
  const XlCalendarLabel = AGN.Lib.Dashboard.XlCalendarLabel;

  class PushLabel extends XlCalendarLabel {

    static TYPE = 'push';

    constructor(data) {
      super(data);
    }

    get draggable() {
      return this.data.status !== PushLabel.pushStatusSent;
    }

    get templateParams() {
      return _.merge(super.templateParams, {
        link: AGN.url(`/push/${this.entityId}/view.action`),
        mediatype: 'status.push',
        status: 'mailing.status.' + this.data.status.toLowerCase(),
        shortname: this.data.name,
      });
    }

    get templateName() {
      return 'xl-calendar-mailing-label';
    }

    get date() {
      switch (this.data.status) {
        case PushLabel.pushStatusSent:
          return this.data.sendDate;
        case PushLabel.pushStatusScheduled:
          return this.data.startDate;
        default:
          return this.data.plannedFor;
      }
    }

    set date(date) {
      switch (this.data.status) {
        case PushLabel.pushStatusSent:
          this.data.sendDate = date;
          break;
        case PushLabel.pushStatusScheduled:
          this.data.startDate = date;
          break;
        default:
          this.data.plannedFor = date;
          break;
      }
    }

    get entityId() {
      return this.data.id;
    }

    get popoverOptions() {
      return {trigger: 'hover', text: this.data.name};
    }

    canMoveTo($day) {
      if (!super.canMoveTo($day)) {
        return false;
      }
      const date = $day.data('date');
      switch (this.data.status) {
        case PushLabel.pushStatusSent:
          return false;
        case PushLabel.pushStatusScheduled:
          return moment(date, 'DD-MM-YYYY').toDate() >= new Date();
        default:
          return true;
      }
    }

    moveTo($day) {
      const date = $day.data('date');
      this.$el.hide();
      $
        .post(AGN.url('/calendar/movePushNotification.action'), {pushId: this.entityId, date})
        .done(resp => {
          if (resp.success === true) {
            super.moveTo($day);
            this.date = date;
          }
      });
    }

    static getOrCreate(data) {
      const instance = XlCalendarLabel.getByEntityId(data.commentId, PushLabel.TYPE);
      if (instance) {
        return instance; // already initialized
      }
      return new PushLabel(data);
    }
  }

  AGN.Lib.Dashboard.XlCalendarPushLabel = PushLabel;
})();
