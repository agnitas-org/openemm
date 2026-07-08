AGN.Lib.Controller.new('mailing-bounce-stats', function () {

  const Table = AGN.Lib.Table;
  const ProcessingLoader = AGN.Lib.ProcessingLoader;
  const Form = AGN.Lib.Form;
  const Select = AGN.Lib.Select;
  const Template = AGN.Lib.Template;

  let data;
  let config;

  const toAgGridRows = (data) => {
    const targetIds = _.keys(data);
    const bounces = _.keys(data[targetIds[0]]);

    const rows = bounces.map(detail => {
      const row = { detail };

      _.each(data, (targetMetrics, id) => {
        row[id] = targetMetrics[detail] || '';
      });

      return row;
    });
    return rows;
  };

  this.addDomInitializer("mailing-bounce-stats", function () {
    config = this.config;
    loadStatistic();
  });

  this.addAction({click: 'reload-bounce-stats'}, loadStatistic);

  function loadStatistic() {
    const $container = Template.dom('bounce-stats-template', {});
    $('#overview-tile > .tile-body').empty().append($container);
    const form = Form.get($('#stat-form'));
    const loader = new ProcessingLoader($container, '#stat-form');
    loader.show();
    form.setActionOnce(AGN.url(`/statistics/mailing/${config.mailingId}/bounces.action`));
    form
      .submit()
      .done(stats => {
        data = stats;
        renderChartsAndTables();
      })
      .always(() => loader.hide());
  }

  function renderChartsAndTables() {
    renderChart('softBounces')
    renderTable('softBounces');
    renderChart('hardBounces')
    renderTable('hardBounces');
    renderUndeliveredTable();
  }

  function renderTable(statName) {
    new Table(
      $(`#${statName}-table`),
      getTableColumnDefinitions(statName),
      toAgGridRows(data[statName]),
      getDefaultTableOptions()
    );
  }

  function renderUndeliveredTable() {
    if (!config.isMailtrackingActive) {
      return;
    }
    const row = Object.fromEntries(
      Object.entries(data['undelivered']).map(([key, val]) => [
        key + "",
        val || ""
      ])
    );
    row.detail = t('statistics.bounce.undelivered');

    new Table(
      $(`#undelivered-table`),
      [
        {
          headerName: t('statistics.default.softBounces'),
          field: "detail",
        },
        ..._.toPairs(getSelectedTargets()).map(([id, name]) => ({
          headerName: name,
          field: id,
          sortable: true,
          cellRenderer: "MustacheTemplateCellRender",
          cellRendererParams: { "templateName": "table-value-cell" },
        }))
      ],
      [row],
      getDefaultTableOptions()
    );
  }

  function renderChart(statName) {
    new Chart($(`#${statName}-chart`)[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: _.keys(data[statName][1]),
        datasets: [
          {
            data: _.values(data[statName][1]).map(({ value }) => value),
            backgroundColor: AGN.Lib.Helpers.getStatColorsHexValues(),
            barThickness: parseFloat(getComputedStyle(document.documentElement).fontSize),
          },
        ]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        scales: {
          x: { display: false },
        },
        plugins: {
          datalabels: { display: false },
          legend: { display: false },
        }
      }
    });
  }

  function getTableColumnDefinitions(statName) {
    return [
      {
        headerName: t(`statistics.default.${statName}`),
        field: "detail",
        type: "colorBadge",
        sortable: true
      },
      ..._.toPairs(getSelectedTargets()).map(([id, name]) => ({
        headerName: name,
        field: id,
        sortable: true,
        comparator: valueComparator,
        cellRenderer: "MustacheTemplateCellRender",
        cellRendererParams: {
          "templateName": "table-value-cell",
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

  function valueComparator(valueA, valueB) {
    const a = (valueA && valueA.value != null) ? Number(valueA.value) : 0;
    const b = (valueB && valueB.value != null) ? Number(valueB.value) : 0;
    return a - b;
  }

  function getSelectedTargets() {
    return { 1: t('statistics.default.allRecipients'), ...Select.get($('#targetGroupSelect')).selectedValues() };
  }
});
