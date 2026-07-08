AGN.Lib.Controller.new('recipient-distribution-stats', function() {

  const StatsChart = AGN.Lib.StatsChart;

  let data;
  let metrics;

  this.addDomInitializer("recipient-distribution-stats", function () {
    data = this.config.data;
    metrics = this.config.metrics;
    renderChart();
    renderTable();
  });

  function renderTable() {
    new AGN.Lib.Table(
      $('#table').empty(),
      getTableColumnDefinitions(),
      data,
      getDefaultTableOptions()
    );
  }

  function renderChart() {
    const labels = data.map(r => r.month);
    const datasets = [
      {
        data: data.map(r => r.active),
      },
      {
        data: data.map(r => r.unsubscribed)
      },
      {
        data: data.map(r => r.bounced)
      },
      {
        data: data.map(r => r.waitForDoi)
      },
      {
        data: data.map(r => r.blacklisted)
      }
    ];
    StatsChart.create('line', $("#chart"), { labels, datasets });
  }

  function getTableColumnDefinitions() {
    return [
      {
        headerName: metrics['month'],
        field: "month",
        cellRenderer: "MustacheTemplateCellRender",
        cellRendererParams: {"templateName": "month-cell-template"},
      },
      {
        headerName: metrics['active'],
        headerColor: true,
        field: "active"
      },
      {
        headerName: metrics['unsubscribed'],
        headerColor: true,
        field: "unsubscribed"
      },
      {
        headerName: metrics['bounced'],
        headerColor: true,
        field: "bounced"
      },
      {
        headerName: metrics['waitForDoi'],
        headerColor: true,
        field: "waitForDoi"
      },
      {
        headerName: metrics['blacklisted'],
        headerColor: true,
        field: "blacklisted"
      }
    ];
  }

  function getDefaultTableOptions() {
    return {
      pagination: false,
      showRecordsCount: "simple",
      autoHeight: true
    }
  }

  this.addAction({click: 'export-csv'}, function () {
    const table = AGN.Lib.Table.get($('#table'));
    if (table?.api) {
      table.api.exportDataAsCsv({ fileName: 'recipient_distribution.csv' });
    }
  });
});
