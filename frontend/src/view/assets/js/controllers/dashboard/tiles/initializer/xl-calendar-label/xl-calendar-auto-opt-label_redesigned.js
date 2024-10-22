(() => {
  const XlCalendarLabel = AGN.Lib.Dashboard.XlCalendarLabel;

  class AutoOptLabel extends XlCalendarLabel {

    static TYPE = 'auto-opt';

    constructor(data) {
      super(data);
    }

    get templateParams() {
      return _.merge(super.templateParams, {
        link: this.link,
        mediatype: 'mailing.mediatype.email',
        status: 'mailing.status.' + this.adaptStatus(this.data.autoOptimizationStatus),
        shortname: this.data.shortname,
      });
    }

    get draggable() {
      return false;
    }

    get link() {
      if (!this.data.workflowId) {
        return "#";
      }
      return AGN.url("workflow/" + this.data.workflowId + "/view.action");
    }

    adaptStatus(status) {
      switch (status) {
        case 'NOT_STARTED':
          return 'new';
        case 'FINISHED':
          return 'sent';
        default:
          return status.toLowerCase();
      }
    }

    get templateName() {
      return 'xl-calendar-mailing-label';
    }

    get date() {
      return this.data.sendDate;
    }

    get entityId() {
      return this.data.id;
    }

    get popoverOptions() {
      return {trigger: 'hover', text: this.data.shortname};
    }

    static getOrCreate(data) {
      const instance = XlCalendarLabel.getByEntityId(data.id, AutoOptLabel.TYPE);
      if (instance) {
        return instance; // already initialized
      }
      return new AutoOptLabel(data);
    }
  }

  AGN.Lib.Dashboard.XlCalendarAutoOptLabel = AutoOptLabel;
})();
