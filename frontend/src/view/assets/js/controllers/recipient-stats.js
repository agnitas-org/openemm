AGN.Lib.Controller.new('recipient-stats', function () {
  const ProcessingLoader = AGN.Lib.ProcessingLoader;
  const StatsChart = AGN.Lib.StatsChart;
  const Messages = AGN.Lib.Messages;
  const Template = AGN.Lib.Template;
  const Helpers = AGN.Lib.Helpers;
  const Select = AGN.Lib.Select;
  const Form = AGN.Lib.Form;
  const Table = AGN.Lib.Table;
  const COLORS = AGN.Lib.Helpers.getStatColorsHexValues();

  const TABLE_OPTIONS = {
    pagination: false,
    showRecordsCount: "simple",
    domLayout: "autoHeight",
  };

  const TABLE_COLUMNS = {
    "progress": [
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
    ],
    "opt-ins": [
      {
        headerName: t('defaults.date'),
        field: "period",
      },
      {
        headerName: t('statistics.doubleOptIn'),
        headerColor: true,
        field: "byDoiConfirmation"
      },
      {
        headerName: t('statistics.imports'),
        headerColor: true,
        field: "byImports"
      },
      {
        headerName: t('statistics.byUser'),
        headerColor: true,
        field: "byUser"
      },
      {
        headerName: t('statistics.byAhv'),
        headerColor: true,
        field: "byAhv"
      },
      {
        headerName: t('statistics.other'),
        headerColor: true,
        field: "other"
      }
    ],
    "opt-outs": [
      {
        headerName: t('defaults.date'),
        field: "period",
      },
      {
        headerName: t('statistics.feedbackLoop'),
        headerColor: true,
        field: "byFeedbackLoop"
      },
      {
        headerName: t('statistics.byCsaLink'),
        headerColor: true,
        field: "byCsaLink"
      },
      {
        headerName: t('statistics.mailings'),
        headerColor: true,
        field: "byMailings"
      },
      {
        headerName: t('statistics.imports'),
        headerColor: true,
        field: "byImports"
      },
      {
        headerName: t('statistics.wsOrIndividual'),
        headerColor: true,
        field: "other"
      },
      {
        headerName: t('statistics.byUser'),
        headerColor: true,
        field: "byUser"
      }
    ],
    "status-info": [
      {
        headerName: t('statistics.statusInfo'),
        field: "name",
        cellStyle: {"user-select": "text"},
        cellRenderer: "NotEscapedStringCellRenderer"
      },
      {
        headerName: t('defaults.value'),
        field: "value",
        cellStyle: {"user-select": "text"},
        cellRenderer: "NotEscapedStringCellRenderer"
      }
    ],
    "mail-format": [
      {
        headerName: t('statistics.mailFormat'),
        type: "colorBadge",
        field: "name",
      },
      {
        headerName: '',
        field: "value",
        valueFormatter: ({value}) => `${value}%`,
      }
    ],
    "recipient-status": [
      {
        headerName: t('statistics.overallStatus'),
        field: "name",
      },
      {
        headerName: '',
        field: "value",
      },
      {
        colId: 'status',
        headerName: t('defaults.status'),
        field: "name",
        hide: true, // csv only
      },
      {
        colId: 'amount',
        headerName: t('statistics.amount'),
        field: "value",
        hide: true, // csv only
      }
    ],
    "doi": [
      {
        headerName: t('statistics.doi'),
        type: "colorBadge",
        field: "type",
      },
      {
        colId: 'amount',
        headerName: t('statistics.amount'),
        field: 'value',
        hide: true, // csv only
      },
      {
        colId: 'percent',
        headerName: t('statistics.percent'),
        field: 'percent',
        valueGetter: (params) => `${Math.floor(params.data.percent)}%`,
        hide: true, // csv only
      },
      {
        headerName: '',
        field: "value",
        suppressSizeToFit: true,
        valueGetter: (params) => {
          const percent = params.data.percent;
          return `${params.data.value} (${percent.toFixed(2)}%)`;
        }
      }
    ]
  }

  const getSelectedType = () => $('#stats-type').val()

  let config;

  this.addDomInitializer('recipient-stats', function () {
    config = this.config;
    loadStats();
  });

  this.addAction({change: 'change-stats'}, loadStats);

  this.addAction({click: 'reload-stats', change: 'reload-stats'}, loadStats);

  this.addAction({click: 'export-csv'}, exportCsv);

  function validateSelectedPeriod() {
    const $startDate = $('#startDate');
    const $endDate = $('#endDate');
    const startDate = $startDate.datepicker('getDate');
    const endDate = $endDate.datepicker('getDate');
    const selectedDays = startDate && endDate ? moment(endDate).diff(moment(startDate), 'days') : 0;
    if (selectedDays > config.daysPeriodLimit) {
      $endDate.datepicker('setDate', moment(startDate).add(config.daysPeriodLimit, 'days').toDate());
      Messages.warn(t("statistics.default.periodExceeded", config.daysPeriodLimit));
    }
  }

  const controlMailinglistSelect = () => {
    const mailinglistSelect = Select.get($('#mailinglist-select'));
    const isDoi = $('#stats-type').val() === 'doi';
    mailinglistSelect.disableOptionsByPredicate((option) => isDoi && $(option).val() === '0', true);
  }

  function loadStats() {
    validateSelectedPeriod();
    controlMailinglistSelect();

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

  function renderStats(stats) {
    switch (getSelectedType()) {
      case 'progress':
        renderProgressChart(stats);
        renderProgressTable(stats);
        break;
      case 'opt-ins':
        renderOptInsChart(stats);
        renderOptInsTable(stats);
        break;
      case 'opt-outs':
        renderOptOutsChart(stats);
        renderOptOutsTable(stats);
        break;
      case 'status-info':
        renderStatusInfoTables(stats);
        break;
      case 'mail-format':
        renderMailFormatChart(stats);
        stats.push({name: t('statistics.default.total'), value: 100, preventColor: true});
        renderTable('mail-format', stats);
        break;
      case 'recipient-status':
        renderRecipientStatusChart(stats);
        renderTable('recipient-status', stats);
        break;
      case 'doi':
        stats.at(-1).preventColor = true;
        stats.at(-1).csvSkip = true;
        renderTable('doi', stats);
        renderDoiChart(stats);
        break;
      default:
        console.error('Invalid statistic type.');
    }
  }

  function renderProgressChart(stats) {
    renderPeriodChart({
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

  function renderOptInsChart(stats) {
    renderPeriodChart({
      labels: _.map(stats, row => row.period),
      datasets: [
        {data: _.map(stats, row => row.byDoiConfirmation)},
        {data: _.map(stats, row => row.byImports)},
        {data: _.map(stats, row => row.byUser)},
        {data: _.map(stats, row => row.byAhv)},
        {data: _.map(stats, row => row.other)},
      ]
    });
  }

  function renderOptOutsChart(stats) {
    renderPeriodChart({
      labels: _.map(stats, row => row.period),
      datasets: [
        {data: _.map(stats, row => row.byFeedbackLoop)},
        {data: _.map(stats, row => row.byCsaLink)},
        {data: _.map(stats, row => row.byMailings)},
        {data: _.map(stats, row => row.byImports)},
        {data: _.map(stats, row => row.other)},
        {data: _.map(stats, row => row.byUser)},
      ]
    });
  }

  function renderPeriodChart(data) {
    StatsChart.create('line', $("#line-chart"), data);
  }

  function renderProgressTable(data) {
    data.push({
      period: t('statistics.sum'),
      optIns: _.sumBy(data, 'optIns'),
      optOuts: _.sumBy(data, 'optOuts'),
      bounced: _.sumBy(data, 'bounced'),
      optInWithoutConfirm: _.sumBy(data, 'optInWithoutConfirm'),
      blocklisted: _.sumBy(data, 'blocklisted'),
      csvSkip: true,
    })
    renderPeriodTable(TABLE_COLUMNS['progress'], data);
  }

  function renderOptInsTable(data) {
    data.push({
      period: t('statistics.sum'),
      other: _.sumBy(data, 'other'),
      byUser: _.sumBy(data, 'byUser'),
      byImports: _.sumBy(data, 'byImports'),
      byDoiConfirmation: _.sumBy(data, 'byDoiConfirmation'),
      byAhv: _.sumBy(data, 'byAhv'),
      csvSkip: true,
    })
    renderPeriodTable(TABLE_COLUMNS['opt-ins'], data);
  }

  function renderOptOutsTable(data) {
    data.push({
      period: t('statistics.sum'),
      other: _.sumBy(data, 'other'),
      byUser: _.sumBy(data, 'byUser'),
      byImports: _.sumBy(data, 'byImports'),
      byFeedbackLoop: _.sumBy(data, 'byFeedbackLoop'),
      byCsaLink: _.sumBy(data, 'byCsaLink'),
      byMailings: _.sumBy(data, 'byMailings'),
      csvSkip: true,
    })
    renderPeriodTable(TABLE_COLUMNS['opt-outs'], data);
  }

  function renderPeriodTable(columnDefs, data) {
    new Table(
      $('#period-table'),
      columnDefs,
      data,
      {
        ...TABLE_OPTIONS,
        autoHeight: true,
        domLayout: "normal"
      }
    );
  }

  function renderStatusInfoTables(data) {
    new Table($('#summed-table'),
      TABLE_COLUMNS['status-info'],
      data.filter(row => row.isSummed),
      TABLE_OPTIONS
    );
    new Table($('#not-summed-table'),
      TABLE_COLUMNS['status-info'],
      data.filter(row => !row.isSummed),
      TABLE_OPTIONS
    );
  }

  function renderMailFormatChart(stat) {
    StatsChart.create('doughnut', $(`#mail-format-chart`), {
      labels: stat.map(({name}) => name),
      datasets: [{
        data: stat.map(({value}) => value),
        backgroundColor: COLORS,
        borderWidth: 0,
      }]
    });
  }

  function renderDoiChart(data) {
    data.pop();
    StatsChart.create('doughnut', $(`#doi-chart`), {
      labels: data.map(({type}) => type),
      datasets: [{
        data: data.map(({value}) => value),
        backgroundColor: COLORS,
        borderWidth: 0,
      }]
    });
  }

  function renderRecipientStatusChart(stat) {
    new Chart($('#recipient-status-chart')[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: ['Count'],
        datasets: stat.map((row, index) => ({
          label: row.name,
          data: [row.value],
          backgroundColor: COLORS[index],
          categoryPercentage: 0.4,
          barPercentage: 0.8,
          maxBarThickness: 50,
        })),
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: true,
            position: 'bottom',
          },
        },
        scales: {
          x: {
            type: 'category',
            offset: true,
            grid: {
              display: false,
            },
            ticks: {
              display: false,
            },
            border: {
              display: true,
              color: Helpers.getColorByVar('--chart-axis-color'),
            },
          },
          y: {
            beginAtZero: true,
            suggestedMax: Math.max(...stat.map(s => s.value)) * 1.03,
            grid: {
              display: false,
            },
            ticks: {
              display: false,
            },
            border: {
              display: false,
            },
          },
        },
      },
    });
  }

  function renderTable(statsType, data) {
    new Table($(`#${statsType}-table`),
      TABLE_COLUMNS[statsType],
      data,
      TABLE_OPTIONS
    );
  }

  function exportCsv() {
    switch (getSelectedType()) {
      case 'progress':
        exportPeriodCsv('recipient_progress.csv');
        break;
      case 'opt-ins':
        exportPeriodCsv('recipient_optins.csv');
        break;
      case 'opt-outs':
        exportPeriodCsv('recipient_optouts.csv');
        break;
      case 'status-info':
        exportStatusInfoCsv();
        break;
      case 'recipient-status':
        exportRecipientStatusCsv();
        break;
      case 'mail-format':
        exportMailFormatCsv();
        break;
      case 'doi':
        exportDoiCsv();
        break;
      default:
        console.error('Invalid statistic type.');
    }
  }

  function exportPeriodCsv(fileName = 'export.csv') {
    Table.get($('#period-table')).exportUnsortedCsv({
      fileName,
      shouldRowBeSkipped: params => params.node.data?.csvSkip,
      columnSeparator: ';',
      suppressQuotes: true,
    });
  }

  function exportStatusInfoCsv() {
    const summedCsv = Table.get($(`#summed-table`)).getUnsortedDataAsCsv({
      columnSeparator: ';',
      suppressQuotes: true,
    });
    const notSummedCsv = Table.get($(`#not-summed-table`)).getUnsortedDataAsCsv({
      skipColumnHeaders: true,
      columnSeparator: ';',
      suppressQuotes: true,
    });
    const mergedCsv = summedCsv + (notSummedCsv ? '\n' + notSummedCsv + '\n' : '');
    Helpers.downloadCsv(mergedCsv, 'recipient_remarks.csv');
  }

  // result CSV looks strange because it was generated that way by the old BIRT
  // and some clients may still depend on this legacy format
  function exportMailFormatCsv() {
    const rows = [];
    Table.get($(`#mail-format-table`)).api.forEachNode(node => {
      rows.push(node.data);
    });
    const items = rows.filter(({ name, value }) => name !== 'Total' && Number(value) > 0);
    const reversed = [...items].reverse();

    const lines = [
      `${t('recipient.mailingType')};;`,
      ...reversed.map(({ name, value }) => `${name};${value}`),
      `${t('statistics.mailFormat')};in %`,
      ';',
    ];

    items.forEach(({ name, value }, index) => {
      lines.push(`;${name}`, String(value));
      if (index < items.length - 1) {
        lines.push(';');
      }
    });
    lines.push(';;', ';');
    Helpers.downloadCsv(lines.join('\n') + '\n', 'recipient_mailtype.csv');
  }

  function exportDoiCsv() {
    Table.get($(`#doi-table`)).exportUnsortedCsv({
      fileName: 'recipient_doi.csv',
      columnKeys: ['type', 'amount', 'percent'],
      columnSeparator: ';',
      suppressQuotes: true,
      shouldRowBeSkipped: params => params.node.data?.csvSkip,
    });
  }

  function exportRecipientStatusCsv() {
    Table.get($(`#recipient-status-table`)).exportUnsortedCsv({
      fileName: 'recipient_status.csv',
      columnKeys: ['status', 'amount'],
      columnSeparator: ';',
      suppressQuotes: true,
      shouldRowBeSkipped: params => params.node.data?.value <= 0,
    });
  }
});
