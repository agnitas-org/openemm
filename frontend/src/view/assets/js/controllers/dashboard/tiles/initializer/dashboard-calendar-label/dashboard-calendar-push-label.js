(() => {
  const DashboardCalendarLabel = AGN.Lib.Dashboard.DashboardCalendarLabel;
  const Def = AGN.Lib.Dashboard.Def;

  class PushLabel extends DashboardCalendarLabel {

    static TYPE = 'push';

    constructor(data) {
      if (_.isNumber(data.date)) {
        data.date = moment(data.date).tz(window.agnTimeZoneId).format(Def.DATE_FORMAT);
      }
      super(data);
    }

    get pushStatusSent() {
      return this.calendar.config.pushStatusSent;
    }

    get pushStatusScheduled() {
      return this.calendar.config.pushStatusScheduled;
    }

    get draggable() {
      return this.data.status !== this.pushStatusSent;
    }

    get templateParams() {
      return _.merge(super.templateParams, {
        link: AGN.url(`/push/${this.entityId}/view.action`),
        mediatype: 'status.push',
        status: 'mailing.status.' + this.data.status.toLowerCase(),
        shortname: this.data.name,
      });
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
        case this.pushStatusSent:
          return false;
        case this.pushStatusScheduled:
          return moment(date, Def.DATE_FORMAT).toDate() >= new Date();
        default:
          return true;
      }
    }

    moveTo($day) {
      const date = $day.data('date');
      return $
        .post(AGN.url('/calendar/movePush.action'), {pushId: this.entityId, date})
        .done(resp => {
          if (resp.success === true) {
            super.moveTo($day);
            this.sendDate = date;
          }
      });
    }

    static getOrCreate(data) {
      const instance = DashboardCalendarLabel.getByEntityId(data.id, PushLabel.TYPE);
      if (instance) {
        return instance; // already initialized
      }
      return new PushLabel(data);
    }
  }

  AGN.Lib.Dashboard.DashboardCalendarPushLabel = PushLabel;
})();
