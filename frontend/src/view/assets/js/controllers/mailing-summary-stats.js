AGN.Lib.Controller.new('mailing-summary-stats', function () {

  const Table = AGN.Lib.Table;
  const Helpers = AGN.Lib.Helpers;
  const ProcessingLoader = AGN.Lib.ProcessingLoader;
  const Form = AGN.Lib.Form;
  const Template = AGN.Lib.Template;

  let data;
  let metadata;
  let config;
  let form;
  let loader;

  const toAgGridRows = (data) => {
    const targetIds = _.keys(data);
    const rootMetricKeys = _.keys(data[targetIds[0]]);

    return rootMetricKeys.map(metricKey => {
      const row = { metric: metricKey };

      _.each(data, (targetMetrics, id) => {
        row[id] = targetMetrics[metricKey];
      });

      return row;
    });
  };

  this.addDomInitializer("mailing-summary-stats", function () {
    form = Form.get($('#stat-form'));
    loader = new ProcessingLoader($('#overview-tile .tile-body'), '#stat-form');
    config = this.config;
    loadStatistic();
  });

  this.addAction({click: 'reload-summary-stat'}, loadStatistic);

  function loadStatistic() {
    const $container = $('#overview-tile > .tile-body');
    $container.empty();
    loader.show();
    form.setActionOnce(AGN.url(`/statistics/mailing/${config.mailingId}/summary.action`));

    form.submit().done(report => {
      metadata = report.metadata;
      data = report.data;
      $container.append(Template.dom('summary-stats-template', ({ generalInfo, reactions } = data)));
      renderCharts();
      renderTables();
      loader.hide();
    });
  }

  function renderTables() {
    renderKeyFiguresTable(false);
    renderEventByDeviceTypeTable(data.openersByDevice, 'openers')
    renderEventByDeviceTypeTable(data.clickersByDevice, 'clickers')
  }

  function renderCharts() {
    renderKeyFiguresChart();
    renderEventByDeviceTypeChart(data.openersByDevice, 'openers');
    renderEventByDeviceTypeChart(data.clickersByDevice, 'clickers');
  }

  function renderKeyFiguresChart() {
    const $chart = $('#key-figures-chart').empty();
    const firstTargetId = _.keys(data.keyFigures)[0];
    const metricsForChart = data.keyFigures[firstTargetId];
    const metricKeys = _.keys(metricsForChart);

    const labels = [];
    const mainVals = [];
    const mainColors = [];
    const subVals = [];
    const subColors = [];

    _.each(metricKeys, (key) => {
      const statValue = metricsForChart[key];
      const meta = metadata.keyFigures[key];

      labels.push(meta ? meta.label : key);
      mainColors.push(meta ? meta.color : '#ccc');

      const partialEntries = _.toPairs(statValue.partialValues);

      if (partialEntries.length > 0) {
        const [subKey, subStat] = partialEntries[0];
        const subMeta = metadata.keyFigures[subKey];
        const total = Number(statValue.val || 0);
        const subVal = Number(subStat.val || 0);

        mainVals.push(total - subVal);
        subVals.push(subVal);
        subColors.push(subVal > 0 && subMeta ? subMeta.color : 'rgba(0,0,0,0)');
      } else {
        mainVals.push(Number(statValue.val || 0));
        subVals.push(0);
        subColors.push('rgba(0,0,0,0)');
      }
    });

    Chart.getChart($chart[0])?.destroy();
    new Chart($chart[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            data: mainVals,
            backgroundColor: mainColors.map(c => Helpers.getStatColorHexByName(c)),
            barThickness: 12,
          },
          {
            data: subVals,
            backgroundColor: subColors.map(c => Helpers.getStatColorHexByName(c)),
            barThickness: 12,
          },
        ]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        scales: {
          x: {
            stacked: true,
            beginAtZero: true,
            display: false,
            ticks: { font: { size: 12 } },
          },
          y: {
            stacked: true,
            ticks: { font: { size: 12 } },
          }
        },
        plugins: {
          datalabels: { display: false, },
          legend: { display: false, },
          tooltip: {
            callbacks: {
              label: function(context) {
                const metricKey = metricKeys[context.dataIndex];
                const originalData = metricsForChart[metricKey];

                if (context.datasetIndex === 0) {
                  const rate = showNetto() ? originalData.deliveredRate : originalData.rate;
                  return `Total: ${originalData.val} (${rate.toFixed(1)}%)`;
                } else {
                  const subKey = _.keys(originalData.partialValues)[0];
                  const subMeta = metadata.keyFigures[subKey];
                  const subData = originalData.partialValues[subKey];
                  return `${subMeta ? subMeta.label : 'Sub'}: ${subData.val} (${subData.rate.toFixed(1)}%)`;
                }
              }
            }
          }
        }
      }
    });
  }

  function renderKeyFiguresTable() {
    new Table(
      $('#key-figures-table').empty(),
      getKeyFiguresColumnDefinitions(),
      toAgGridRows(data.keyFigures),
      getDefaultTableOptions()
    );
  }

  function getKeyFiguresColumnDefinitions() {
    return [
      {
        headerName: t('statistics.default.keyFigures'),
        field: "metric",
        type: "textCaseInsensitiveColumn",
        cellRenderer: "MustacheTemplateCellRender",
        sortable: true,
        comparator: labelComparator(metadata.keyFigures),
        cellRendererParams: {
          "templateName": "metric-label-cell",
          "data": {
            "meta": metadata.keyFigures,
            "data": data.keyFigures
          },
        }
      },
      ..._.toPairs(metadata.targets).map(([id, name]) => ({
        headerName: name,
        field: id,
        sortable: true,
        comparator: valueComparator,
        cellRenderer: "MustacheTemplateCellRender",
        cellRendererParams: {
          "templateName": "metric-value-cell",
          "data": {
            "showNetto": showNetto(),
          },
        },
      }))
    ];
  }

  function renderEventByDeviceTypeTable(data, metric) {
    new Table(
      $(`#${metric}-by-device-table`),
      getEventByDeviceTypeColumnDefinitions(data, metric),
      toAgGridRows(data),
      getDefaultTableOptions()
    );
  }

  function renderEventByDeviceTypeChart(stat, metirc) {
    new Chart($(`#${metirc}-by-device-chart`)[0].getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: _.values(metadata.deviceTypes).map(({ label }) => label),
        datasets: [{
          data: _.values(stat[1]).map(({ val }) => val),
          backgroundColor: _.values(metadata.deviceTypes).map(({ color }) => Helpers.getStatColorHexByName(color)),
          borderWidth: 0,
        }]
      },
      options: {
        plugins: {
          legend: {
            labels: { font: { size: 12 } }
          }
        },
      }
    })
  }

  function getEventByDeviceTypeColumnDefinitions(data, metric) {
    return [
      {
        headerName: t(`statistics.default.${metric}ByDevices`),
        field: "metric",
        type: "textCaseInsensitiveColumn",
        cellRenderer: "MustacheTemplateCellRender",
        sortable: true,
        comparator: labelComparator(metadata.deviceTypes),
        cellRendererParams: {
          "templateName": "metric-label-cell",
          "data": {
            "meta": metadata.deviceTypes,
            "data": data
          },
        }
      },
      ..._.toPairs(metadata.targets).map(([id, name]) => ({
        headerName: name,
        field: id,
        sortable: true,
        comparator: valueComparator,
        cellRenderer: "MustacheTemplateCellRender",
        cellRendererParams: {
          "templateName": "metric-value-cell",
        },
      }))
    ];
  }

  function getDefaultTableOptions() {
    return {
      pagination: false,
      showRecordsCount: "simple",
      domLayout: "autoHeight"
    }
  }

  this.addAction({click: 'export-csv'}, function () {
    const table = AGN.Lib.Table.get($('#key-figures-table'));
    if (table && table.api) {
      table.api.exportDataAsCsv({ fileName: 'mailing-stats.csv' });
    }
  });

  function showNetto() {
    return $('#showNetto').prop('checked');
  }

  this.addAction({click: 'show-netto'}, function () {
    renderKeyFiguresChart()
    renderKeyFiguresTable();
  });

  function valueComparator(valueA, valueB) {
    const a = (valueA && valueA.val != null) ? Number(valueA.val) : 0;
    const b = (valueB && valueB.val != null) ? Number(valueB.val) : 0;
    return a - b;
  }

  function labelComparator(metrics) {
    return (valueA, valueB) => {
      const labelA = metrics[valueA]?.label || valueA;
      const labelB = metrics[valueB]?.label || valueB;
      return labelA.localeCompare(labelB, undefined, {sensitivity: 'base'});
    };
  }
});
