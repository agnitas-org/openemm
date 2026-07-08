AGN.Lib.Controller.new('recipient-stats', function () {

  const ProcessingLoader = AGN.Lib.ProcessingLoader;
  const StatsChart = AGN.Lib.StatsChart;
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;
  const Form = AGN.Lib.Form;
  const Table = AGN.Lib.Table;

  this.addDomInitializer('recipient-stats', loadStats);

  this.addAction({change: 'change-stats'}, loadStats);

  this.addAction({click: 'reload-stats'}, loadStats);

  this.addAction({click: 'export-csv'}, exportCsv);

  function loadStats() {
    const mailinglistSelect = Select.get($('#mailinglist-select'));

    if (Select.get($('#stats-type')).$selectedOption.is('[data-mailinglist-required]') && mailinglistSelect.getSelectedValue() === '0') {
      mailinglistSelect.selectNext();
    }

    const $container = Template.dom(`${getSelectedType()}-stats-template`, {});
    $('#overview-tile > .tile-body').empty().append($container);
    const form = Form.get($('#stat-form'));
    const loader = new ProcessingLoader($container, '#stat-form');
    loader.show();
    form.setActionOnce(AGN.url(`/statistics/recipient/${getSelectedType()}.action`));
    form
      .submit()
      .done(stats => renderStats(stats))
      .always(() => loader.hide());
  }

  function getSelectedType() {
    return $('#stats-type').val();
  }

  function renderStats(stats) {
    switch (getSelectedType()) {
      case 'progress':
        renderProgressChart(stats);
        renderProgressTable(stats);
        break;
      default:
        logger.error('Invalid statistic type.');
    }
  }

  function renderProgressChart(data) {
    const months = {}; // group by month

    _.forEach(data, (progress, dateStr) => {
      const [, month, year] = dateStr.split('.').map(Number);
      const monthKey = `${year}-${String(month).padStart(2, '0')}`;

      if (!months[monthKey]) {
        months[monthKey] = {optIns: 0, optOuts: 0, bounced: 0, doubleOptIn: 0, blocklisted: 0};
      }
      months[monthKey].optIns += progress.optIns;
      months[monthKey].optOuts += progress.optOuts;
      months[monthKey].bounced += progress.bounced;
      months[monthKey].doubleOptIn += progress.doubleOptIn;
      months[monthKey].blocklisted += progress.blocklisted;
    });

    const labels = _.keys(months).sort(); // sorted months
    const datasets = [
      {data: labels.map(m => months[m].optIns)},
      {data: labels.map(m => months[m].optOuts)},
      {data: labels.map(m => months[m].bounced)},
      {data: labels.map(m => months[m].doubleOptIn)},
      {data: labels.map(m => months[m].blocklisted)},
    ];
    new StatsChart('line', $("#progress-chart"), {labels, datasets});
  }

  function renderProgressTable(data) {
    new AGN.Lib.Table(
      $('#table').empty(),
      // getTableColumnDefinitions(),
      data,
      getDefaultTableOptions()
    );
  }

  // function getTableColumnDefinitions() {
  //   return [
  //     {
  //       headerName: metrics['month'],
  //       field: "month",
  //       cellRenderer: "MustacheTemplateCellRender",
  //       cellRendererParams: {"templateName": "month-cell-template"},
  //     },
  //     {
  //       headerName: metrics['active'],
  //       headerColor: true,
  //       field: "active"
  //     },
  //     {
  //       headerName: metrics['unsubscribed'],
  //       headerColor: true,
  //       field: "unsubscribed"
  //     },
  //     {
  //       headerName: metrics['bounced'],
  //       headerColor: true,
  //       field: "bounced"
  //     },
  //     {
  //       headerName: metrics['waitForDoi'],
  //       headerColor: true,
  //       field: "waitForDoi"
  //     },
  //     {
  //       headerName: metrics['blacklisted'],
  //       headerColor: true,
  //       field: "blacklisted"
  //     }
  //   ];
  // }

  function getDefaultTableOptions() {
    return {
      pagination: false,
      showRecordsCount: "simple",
      autoHeight: true
    }
  }

  function exportCsv() {
    return Table.get($('#progress-table'))?.api.exportDataAsCsv({fileName: 'recipient_progress.csv'});
  }
});
