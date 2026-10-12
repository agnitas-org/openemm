AGN.Lib.Controller.new('workflow-stat', function () {

  const Template = AGN.Lib.Template;
  const Helpers = AGN.Lib.Helpers;

  let config;
  let $container;

  const METRICS = [
    { name: 'sentEmails', label: t('statistics.default.sentEmails') },
    { name: 'acceptedEmails', label: t('statistics.default.acceptedEmails') },
    { name: 'measuredOpeners', label: t('statistics.default.measuredOpeners') },
    { name: 'invisibleOpeners', label: t('statistics.default.invisibleOpeners') },
    { name: 'totalOpeners', label: t('statistics.default.totalOpeners') },
    { name: 'anonymousOpenersCount', label: t('statistics.progress.anonymousOpenings') },
    { name: 'clickers', label: t('statistics.default.clickers') },
    { name: 'anonymousClickersCount', label: t('statistics.progress.anonymousClicks') },
    { name: 'optOuts', label: t('statistics.optOuts') },
    { name: 'hardBounces', label: t('statistics.default.hardBounces') },
    { name: 'softBounces', label: t('statistics.bounce.undelivered') },
    { name: 'revenue', label: t('statistics.default.revenue') },
    { name: 'avgMailingSize', label: t('statistics.default.avgMailSize'), tableTemplate: 'stat-size-cell', unit: 'KB' }
  ];

  const deviceTypeColors = Helpers.getStatColorsNames();

  const DEVICE_TYPES = [
    { key: 'TOTAL', label: t('statistics.default.total') },
    { key: 'PC', label: t('statistics.device.desktop') },
    { key: 'TABLET', label: t('statistics.device.tablet') },
    { key: 'MOBILE', label: t('statistics.device.mobile') },
    { key: 'SMART_TV', label: t('statistics.device.smarttv') },
    { key: 'MULTIPLE', label: t('statistics.device.mixed') }
  ].map((type, index) => ({ ...type, color: deviceTypeColors[index % deviceTypeColors.length] }));

  const OPENER_DEVICE_ROWS = DEVICE_TYPES.map(type => ({
    field: type.key === 'TOTAL' ? 'measuredOpeners' : null,
    mapField: type.key === 'TOTAL' ? null : 'openersByDeviceType',
    deviceKey: type.key,
    label: type.label,
    color: type.color,
    isTotalRow: type.key === 'TOTAL'
  }));

  const CLICKER_DEVICE_ROWS = DEVICE_TYPES.map(type => ({
    field: type.key === 'TOTAL' ? 'clickers' : null,
    mapField: type.key === 'TOTAL' ? null : 'clickersByDeviceType',
    deviceKey: type.key,
    label: type.label,
    color: type.color,
    isTotalRow: type.key === 'TOTAL'
  }));

  const REACTION_ROWS = [
    { field: 'openingsGross', label: t('statistics.default.openingsGross') },
    { field: 'clicksGross', label: t('statistics.default.clicksGross') },
    { field: 'activityRate', label: t('statistics.default.activityRate'), isPercent: true }
  ];

  const tableOptions = {
    pagination: false,
    domLayout: "autoHeight",
    showRecordsCount: "simple"
  }

  let availableMetrics = [];

  this.addDomInitializer("workflow-stat", function () {
    config = this.config;
    $container = this.el;

    fetchData().done(resp => {
      render(resp);

      $('#stat-loader').addClass('hidden')
      $container.removeClass('invisible');
    });
  });

  function fetchData() {
    const params = { targetGroups: config.selectedTargets };
    if (config.from) {
      params.from = config.from;
    }
    if (config.to) {
      params.to = config.to;
    }

    return $.get(
      AGN.url(`/workflow/${(config.workflowId)}/statistic/data.action`),
      params
    );
  }

  function render(resp) {
    const { mailings, total } = resp;

    if (!mailings.length) {
      $('#workflow-stat-content').html(Template.text('no-results-found'))
      return;
    }

    const colors = Helpers.getStatColorsHexValues();
    assignColors(mailings, colors);

    const rows = total ? [buildTotalRow(total, colors), ...mailings] : mailings;
    availableMetrics = getAvailableMetrics(rows);

    renderOptimizationInfo(resp);
    renderChart(rows);
    renderTable(rows);
    renderDevicesBlock(resp);
  }

  function renderOptimizationInfo(resp) {
    const info = resp.optimizationInfo;
    const $container = $('#auto-optimization-info');
    $container.toggleClass('hidden', !info);
    if (!info) {
      return;
    }

    $container.html(Template.text('auto-optimization-info-template', info));
    AGN.runAll($container);
  }

  function renderDevicesBlock(resp) {
    const hasData = !!(resp.openersByDevice || resp.clickersByDevice);
    $('#device-stat-block').toggleClass('hidden', !hasData);
    if (!hasData) {
      return;
    }

    $('#reactions-table-block').removeClass('hidden');

    const columnMailings = orderMailingsFinalFirst(resp.mailings, resp.optimizationInfo);

    renderDeviceChart('#openers-device-chart', resp.openersByDevice);
    renderDeviceTypeTable('#openers-device-table', columnMailings, OPENER_DEVICE_ROWS, t('statistics.default.openersByDevices'));
    renderDeviceChart('#clickers-device-chart', resp.clickersByDevice);
    renderDeviceTypeTable('#clickers-device-table', columnMailings, CLICKER_DEVICE_ROWS, t('statistics.default.clickersByDevices'));
    renderDeviceTypeTable('#reactions-table', columnMailings, REACTION_ROWS, t('statistics.default.reactions'), 'stat-reaction-value-cell', 'stat-label-cell');
  }

  function orderMailingsFinalFirst(mailings, optimizationInfo) {
    const finalMailingId = optimizationInfo?.groups?.find(group => group.finalMailing)?.mailingId;
    const finalMailing = mailings.find(mailing => mailing.mailingId === finalMailingId);
    if (!finalMailing) {
      return mailings;
    }

    return [finalMailing, ...mailings.filter(mailing => mailing.mailingId !== finalMailingId)];
  }

  function renderDeviceChart(canvasSelector, deviceData) {
    if (!deviceData) {
      return;
    }

    const slices = DEVICE_TYPES.filter(type => type.key !== 'TOTAL' && deviceData[type.key] != null);

    new Chart($(canvasSelector)[0].getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: slices.map(type => type.label),
        datasets: [{
          data: slices.map(type => deviceData[type.key].value),
          backgroundColor: slices.map(type => Helpers.getStatColorHexByName(type.color)),
          borderWidth: 0
        }]
      }
    });
  }

  function renderDeviceTypeTable(
    containerSelector,
    mailings,
    rowsConfig,
    cornerLabel,
    valueTemplate = 'stat-metric-cell',
    labelTemplate = 'device-type-label-cell'
  ) {
    const rows = rowsConfig.map(rowDef => {
      const row = { label: rowDef.label, color: rowDef.color, isPercent: !!rowDef.isPercent };
      mailings.forEach(mailing => {
        const metric = rowDef.field ? mailing.data[rowDef.field] : mailing.data[rowDef.mapField]?.[rowDef.deviceKey];
        // The "Total" row is the 100% baseline the device rows below it are a share of.
        row[mailing.mailingId] = rowDef.isTotalRow && metric != null
          ? { value: metric.value, rate: metric.value > 0 ? 100 : metric.rate }
          : metric;
      });
      return row;
    });

    const columns = [
      {
        headerName: cornerLabel,
        field: 'label',
        cellRenderer: 'MustacheTemplateCellRender',
        cellRendererParams: {templateName: labelTemplate},
        type: 'textCaseInsensitiveColumn',
        sortable: false
      },
      ...mailings.map(mailing => ({
        headerName: mailing.name,
        field: `${mailing.mailingId}`,
        cellRenderer: 'MustacheTemplateCellRender',
        cellRendererParams: {templateName: valueTemplate},
        sortable: false
      }))
    ];

    new AGN.Lib.Table($(containerSelector), columns, rows, tableOptions);
  }

  function assignColors(mailings, colors) {
    mailings.forEach((mailing, idx) => mailing.color = colors[(idx + 1) % colors.length]);
  }

  function buildTotalRow(total, colors) {
    return {
      mailingId: 0,
      name: t('statistics.default.total'),
      color: colors[0],
      data: total
    };
  }

  function renderChart(mailings) {
    new Chart($('#workflow-stat-chart')[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: availableMetrics.map(metric => metric.label),
        datasets: collectDataSets(mailings)
      },
      options: {
        indexAxis: 'x',
        layout: {
          padding: {
            top: 20,
            right: 0
          }
        },
        scales: {
          x: {
            ticks: {
              autoSkip: false,
              maxRotation: 90,
              minRotation: 0
            }
          },
          y: {
            display: false
          }
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: context => {
                const metric = availableMetrics[context.dataIndex];
                const unit = metric?.unit ? ` ${metric.unit}` : '';
                return `${context.dataset.label}: ${AGN.formatNumber(context.parsed.y)}${unit}`;
              }
            }
          },
          datalabels: {
            display: false
          }
        }
      }
    })
  }

  function collectDataSets(mailings) {
    return mailings.map(mailing => ({
      label: mailing.name,
      data: collectDataSetData(mailing),
      backgroundColor: mailing.color,
      categoryPercentage: 0.8,
      barPercentage: 0.9
    }));
  }

  function collectDataSetData(mailing) {
    return availableMetrics.map(metric => {
      const metricValue = mailing.data[metric.name];
      if (metricValue == null) {
        return 0;
      }

      return Helpers.isNumber(metricValue) ? metricValue : metricValue.value;
    });
  }

  function renderTable(mailings) {
    new AGN.Lib.Table($('#workflow-stat-table'), getTableColumnsDefinition(), mailings, tableOptions);
  }

  function getTableColumnsDefinition() {
    const columns = [
      {
        headerName: '',
        cellRenderer: 'MustacheTemplateCellRender',
        cellRendererParams: {"templateName": "workflow-mailing-cell"},
        field: 'name',
        type: 'textCaseInsensitiveColumn',
        sortable: false
      }
    ];

    availableMetrics
      .forEach(metric => {
        columns.push({
          headerName: metric.label,
          cellRenderer: 'MustacheTemplateCellRender',
          cellRendererParams: {templateName: metric.tableTemplate || 'stat-metric-cell'},
          field: `data.${metric.name}`,
          type: 'textCaseInsensitiveColumn',
          sortable: false
        });
      });

    return columns;
  }

  function getAvailableMetrics(mailings) {
    return METRICS.filter(metric =>
      mailings.some(mailing => mailing.data[metric.name] != null));
  }

});
