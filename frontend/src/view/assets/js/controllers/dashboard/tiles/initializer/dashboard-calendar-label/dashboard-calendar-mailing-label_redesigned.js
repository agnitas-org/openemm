(() => {
  const DashboardCalendarLabel = AGN.Lib.Dashboard.DashboardCalendarLabel;

  class MailingLabel extends DashboardCalendarLabel  {

    static TYPE = 'mailing';

    constructor(data, limit) {
      super(data);
      this.limit = limit;
    }

    get templateParams() {
      return _.merge(super.templateParams, {
        link: this.data.link,
        mediatype: 'mailing.mediatype.' + this.data.mediatype?.toLowerCase(),
        status: this.data.status,
        shortname: this.data.shortname,
      });
    }

    get draggable() {
      return !(this.data.inUnsentList && this.data.planned) && !this.data.isSent === true;
    }

    get date() {
      return this.data.sendDate;
    }

    set date(date) {
      return this.data.sendDate = date;
    }

    get entityId() {
      return this.data.id;
    }

    createPopover() {
      this.data.$el = this.$el;
      this.popover = new AGN.Lib.Dashboard.MailingPopover(this.data, true);
    }

    canMoveTo($container) {
      if (!super.canMoveTo($container)) {
        return false;
      }
      if ($container.is(this.calendar.$unsentList)) {
        return !this.$el.closest('#dashboard-calendar-unsent-tile').exists()
          && this.data.status !== 'mailing.status.scheduled';
      }
      const date = $container.data('date');
      return moment(date + this.data.sendTime, 'DD-MM-YYYY HH:mm').toDate() >= new Date();
    }

    moveTo($container) {
      if ($container.is(this.calendar.$unsentList)) {
        return this.moveToUnplannedList();
      } else {
        return this.moveToDay($container);
      }
    }

    moveToDay($container) {
      const date = $container.data('date');
      return $
        .post(AGN.url('/calendar/moveMailing.action'), {mailingId: this.entityId, date})
        .done(resp => {
          if (resp.success === true) {
            this.insertLabelInDay($container);
            this.date = date;
          }
        });
    }

    moveToUnplannedList() {
      return $
        .post(AGN.url(`/calendar/mailing/${this.entityId}/clearPlannedDate.action`))
        .done(success => {
          if (success) {
            this.date = '';
            this.calendar.$unsentList.append(this.$el);
          }
        })
    }

    insertLabelInDay($day) {
      const date = $day.data('date');
      let sendTime = this.data.time;
      const sendDateTime = moment(date + sendTime, 'DD-MM-YYYY HH:mm').toDate();
      const genDate = new Date(sendDateTime);

      // Generation is going to happen 3 hours earlier than the actual sending
      genDate.setHours(genDate.getHours() - 3);

      // Remove time if generation date in the past
      if (genDate < new Date()) {
        sendTime = null;
        this.data.time = null;
      }

      let inserted = false;
      if (sendTime) {
        this.calendar.getLabelsByType($day, MailingLabel.TYPE).reverse()
          .map(label => label.data.time < sendTime)
          .forEach(label => {
            label.$el.after(this.$el);
            inserted = true;
          });
      }

      if (!inserted) {
        super.moveTo($day);
      }
    }

    static getOrCreate(data) {
      const instance = DashboardCalendarLabel.getByEntityId(data.id, MailingLabel.TYPE);
      if (instance && !instance.data.planned) { // planed mailings shown in both unsent list and table
        return instance; // already initialized
      }
      return new MailingLabel(data);
    }
  }

  AGN.Lib.Dashboard.DashboardCalendarMailingLabel = MailingLabel;
})();
