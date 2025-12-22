(() => {
  const DashboardCalendarLabel = AGN.Lib.Dashboard.DashboardCalendarLabel;
  const Def = AGN.Lib.Dashboard.Def;

  class AutoOptLabel extends DashboardCalendarLabel {

    static TYPE = 'auto-opt';

    constructor(data) {
      if (_.isNumber(data.sendDate)) {
        data.sendDate = moment(data.sendDate).tz(window.agnTimeZoneId).format(Def.DATE_FORMAT);
      }
      super(data);
    }

    get templateParams() {
      return _.merge(super.templateParams, {
        link: this.link,
        mediatype: 'mailing.mediatype.email',
        status: 'mailing.status.' + this.adaptStatusForIcon(this.data.autoOptimizationStatus),
        tooltip: 'mailing.status.' + this.adaptStatusForTooltip(this.data.autoOptimizationStatus),
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

    adaptStatusForIcon(status) {
      switch (status) {
        case 'NOT_STARTED':
          return 'new';
        case 'TEST_SEND':
          return 'ao_test_send';
        case 'EVAL_IN_PROGRESS':
          return 'in-generation';
        case 'FINISHED':
          return 'sent';
        default:
          return status.toLowerCase();
      }
    }

    adaptStatusForTooltip(status) {
      switch (status) {
        case 'NOT_STARTED':
          return 'new';
        case 'TEST_SEND':
          return 'test';
        case 'EVAL_IN_PROGRESS':
          return 'inGeneration';
        case 'FINISHED':
          return 'sent';
        default:
          return status.toLowerCase();
      }
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
      const instance = DashboardCalendarLabel.getByEntityId(data.id, AutoOptLabel.TYPE);
      if (instance) {
        return instance; // already initialized
      }
      return new AutoOptLabel(data);
    }
  }

  AGN.Lib.Dashboard.DashboardCalendarAutoOptLabel = AutoOptLabel;
})();
