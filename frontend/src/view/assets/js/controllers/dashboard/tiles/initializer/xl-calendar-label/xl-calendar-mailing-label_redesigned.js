(() => {
  const XlCalendarLabel = AGN.Lib.Dashboard.XlCalendarLabel;
  const Storage = AGN.Lib.Storage;

  class MailingLabel extends XlCalendarLabel  {

    static TYPE = 'mailing';

    constructor(data, limit) {
      super(data);
      this.limit = limit;
    }

    get templateParams() {
      return _.merge(super.templateParams, {
        link: this.data.link,
        mediatype: 'mailing.mediatype.' + this.data.mediatype?.toLowerCase(),
        status: this.data.workstatus,
        shortname: this.data.shortname,
      });
    }

    get draggable() {
      return !(this.data.inUnsentList && this.data.planned);
    }

    get date() {
      return this.data.sendDate;
    }

    set date(date) {
      return this.data.sendDate = date;
    }

    get entityId() {
      return this.data.mailingId;
    }

    get isUnsentPlannedListSelected() {
      return Storage.get(this.Calendar.UNSENT_MAILINGS_STORAGE_KEY) === 'planned';
    }

    createPopover() {
      this.data.$el = this.$elText;
      AGN.Lib.Dashboard.CalendarBase.createMailingPopover(this.data);
    }

    canMoveTo($container) {
      if (!super.canMoveTo($container)) {
        return false;
      }
      if (this.data.sent === true) {
        return false;
      }
      if ($container.is(this.unsentMailings$)) {
        return !this.isUnsentPlannedListSelected && this.data.workstatus !== 'mailing.status.scheduled';
      }
      const date = $container.data('date');
      return moment(date + this.data.sendTime, 'DD-MM-YYYY HH:mm').toDate() >= new Date();
    }

    moveTo($container) {
      if ($container.is(this.unsentMailings$)) {
        this.moveToUnsentList();
      } else {
        this.moveToDay($container);
      }
    }

    moveToDay($container) {
      const date = $container.data('date');
      $
        .post(AGN.url('/calendar/moveMailing.action'), {mailingId: this.entityId, date})
        .done(resp => {
          if (resp.success === true) {
            this.insertLabelInDay($container);
            this.date = date;
          }
          this.show();
        });
    }

    moveToUnsentList() {
      $
        .post(AGN.url(`/calendar/mailing/${this.entityId}/clearPlannedDate.action`))
        .done(success => {
          if (success) {
            this.date = '';
            this.unsentMailings$.append(this.$el);
          }
        })
        .always(() => this.show());
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
        this.Calendar.getLabelsByType($day, MailingLabel.TYPE).reverse()
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
      const instance = XlCalendarLabel.getByEntityId(data.mailingId, MailingLabel.TYPE);
      if (instance) {
        return instance; // already initialized
      }
      return new MailingLabel(data);
    }
  }

  AGN.Lib.Dashboard.XlCalendarMailingLabel = MailingLabel;
})();
