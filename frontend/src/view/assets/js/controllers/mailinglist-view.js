
class MailinglistViewController extends AGN.Lib.Controller {

  constructor() {
    super();
    const self = this;
    this.addDomInitializer('mailinglist-view', function () {
      self.onMailinglistViewInit(this.config)
    });
    this.addAction({click: 'reload-stats'}, () => this.reloadStats());
    this.addAction({click: 'export-csv'}, () => this.exportCsv());
  }

  onMailinglistViewInit(config) {
    this.config = config;
    this.reloadStats();
  }

  reloadStats() {
    const loader = new AGN.Lib.ProcessingLoader($('#mailinglist-stats'));
    loader.show();
    const params = { month: this.statsMonth, year: this.statsYear};
    $
      .get(AGN.url(`/mailinglist/${this.config.mailinglistId}/stats.action`), params)
      .done(data => {
        this.renderStatChart(data);
        this.renderStatTable(data);
        loader.hide();
      });
  }

  get tableColumns() {
    return [
      {
        headerName: t('defaults.date'),
        field: "period",
      },
      {
        headerName: t('statistics.optIns'),
        headerColor: true,
        field: "optIns"
      },
      {
        headerName: t('statistics.optOuts'),
        headerColor: true,
        field: "optOuts"
      },
      {
        headerName: t('statistics.bounced'),
        headerColor: true,
        field: "bounced"
      },
      {
        headerName: t('statistics.optInWithoutConfirm'),
        headerColor: true,
        field: "optInWithoutConfirm"
      },
      {
        headerName: t('statistics.blocklisted'),
        headerColor: true,
        field: "blocklisted"
      }
    ]
  }

  renderStatChart(stats) {
    AGN.Lib.StatsChart.create('line', $("#line-chart"), {
      labels: _.map(stats, row => row.period),
      datasets: [
        {data: _.map(stats, row => row.optIns)},
        {data: _.map(stats, row => row.optOuts)},
        {data: _.map(stats, row => row.bounced)},
        {data: _.map(stats, row => row.optInWithoutConfirm)},
        {data: _.map(stats, row => row.blocklisted)},
      ]
    });
  }

  renderStatTable(data) {
    data.push(this.getSumTableRow(data));

    new AGN.Lib.Table(
      $('#period-table').empty(),
      this.tableColumns,
      data,
      {
        pagination: false,
        showRecordsCount: "simple",
        domLayout: "autoHeight"
      }
    );
  }

  getSumTableRow(data) {
    return {
      period: t('statistics.sum'),
      optIns: _.sumBy(data, 'optIns'),
      optOuts: _.sumBy(data, 'optOuts'),
      bounced: _.sumBy(data, 'bounced'),
      optInWithoutConfirm: _.sumBy(data, 'optInWithoutConfirm'),
      blocklisted: _.sumBy(data, 'blocklisted'),
      csvSkip: true,
    }
  }

  get statsMonth() {
    return Number.parseInt($('#statMoth').val());
  }

  get statsYear() {
    return Number.parseInt($('#statYear').val());
  }

  exportCsv() {
    AGN.Lib.Table.get($('#period-table')).exportUnsortedCsv({
      fileName: 'recipient_progress.csv',
      shouldRowBeSkipped: params => params.node.data?.csvSkip,
      columnSeparator: ';',
      suppressQuotes: true,
    });
  }
}

AGN.Lib.Controller.new('mailinglist-view', MailinglistViewController);
