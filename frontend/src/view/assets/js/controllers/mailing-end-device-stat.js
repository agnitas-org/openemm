AGN.Lib.Controller.new('mailing-end-device-stat', function () {

  const Template = AGN.Lib.Template;

  let config;
  let $container;
  let selectedTargets;

  const deviceClasses = {
    desktop: t('statistics.device.desktop'),
    mobile: t('statistics.device.mobile'),
    tablet: t('statistics.device.tablet'),
    smartTv: t('statistics.device.smarttv')
  }

  const metrics = {
    openings: t('statistics.default.openings'),
    clicks: t('statistics.default.openings')
  }

  const tableOptions = {
    pagination: false,
    domLayout: "autoHeight",
    showRecordsCount: "simple"
  }

  const colorsMap = new Map();

  this.addDomInitializer("mailing-end-device-stat", function () {
    config = this.config;
    $container = this.el;
    selectedTargets = getSelectedTargets();

    fetchData().done(resp => {
      render(resp);

      $('#stat-loader').addClass('hidden')
      $container.removeClass('invisible');
    });
  });

  function getSelectedTargets() {
    const targetGroupsMap = new Map(
      config.targetGroups.map(target => [target.id, target])
    );

    const result = config.selectedTargets
      .map(id => targetGroupsMap.get(parseInt(id)));

    result.unshift(getAllRecipientsTargetGroup());

    return result;
  }

  function fetchData() {
    return $.get(
      AGN.url(`/statistics/mailing/${(config.mailingId)}/endDevice.action`),
      {targetGroups: config.selectedTargets}
    );
  }

  function isEmptyResponse(resp) {
    return Object.keys(metrics).every(metricName =>
      Object.values(resp[metricName])
        .every(arr => !arr.length)
    );
  }

  function render(resp) {
    if (isEmptyResponse(resp)) {
      $container.append(Template.dom('no-results-found'));
      return;
    }

    initColorsMap(resp);

    Object.keys(metrics)
      .forEach(metricName => renderMetric(resp, metricName));
  }

  function initColorsMap(resp) {
    colorsMap.clear();

    const statColors = AGN.Lib.Helpers.getStatColorsHexValues();
    let colorIndex = 0;

    Object.keys(metrics).forEach(metricName => {
      const deviceNames = new Set(
        Object.values(resp[metricName])
          .flatMap(d => d)
          .map(d => d.deviceName)
      );

      deviceNames.forEach(deviceName => {
        if (!colorsMap.has(deviceName)) {
          colorsMap.set(deviceName, statColors[colorIndex % statColors.length]);
          colorIndex++;
        }
      });
    });
  }

  function renderMetric(resp, metricName) {
    const $row = Template.dom('end-device-metric', {
      metricName,
      metricLabel: metrics[metricName]}
    );

    $container.append($row);

    renderChart($(`#${metricName}-chart`), Object.values(resp[metricName]).flatMap(e => e));
    renderTables(resp, metricName);
  }

  function renderChart($chart, entries) {
    const data = prepareChartData(entries);
    if (!data.length) {
      $chart.closest('.tile').addClass('hidden');
      return;
    }

    new Chart($chart[0].getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: data.map(d => d.deviceName),
        datasets: [{
          data: data.map(d => d.value),
          backgroundColor: data.map(d => colorsMap.get(d.deviceName)),
          borderWidth: 0
        }]
      }
    });
  }

  function prepareChartData(entries) {
    const totals = new Map();

    entries.forEach(({ deviceName, allRecipients }) => {
      const current = totals.get(deviceName) || 0;
      totals.set(deviceName, current + allRecipients.value);
    });

    return Array.from(totals, ([deviceName, value]) => ({ deviceName, value }));
  }

  function renderTables(resp, metricName) {
    Object.entries(deviceClasses).forEach(([deviceName, deviceClassLabel]) => {
      const rows = prepareTableRows(resp[metricName][deviceName]);
      renderTableData(rows, metricName, deviceClassLabel);
    });
  }

  function prepareTableRows(entries) {
    return entries.map(e => {
      const row = {
        deviceName: e.deviceName,
        allRecipients: e.allRecipients,
        colorHex: colorsMap.get(e.deviceName)
      }

      selectedTargets.forEach(tg =>
        row[makeTargetGroupPropertyKey(tg.id)] = e.targetGroups[tg.id] ?? {value: 0, rate: 0});

      return row;
    });
  }

  function renderTableData(rows, metricName, deviceClassLabel) {
    if (!rows.length) {
      return;
    }

    const $table = Template.dom('metric-table-wrapper');
    $(`#${metricName}-table-container`).append($table);

    new AGN.Lib.Table($table, getTableColumnsDefinitions(metricName, deviceClassLabel), rows, tableOptions);
  }

  function getTableColumnsDefinitions(metricName, deviceClassName) {
    const columns = [
      {
        headerName: `${t(`statistics.default.${metricName}`)} (${deviceClassName})`,
        cellRenderer: 'MustacheTemplateCellRender',
        cellRendererParams: {templateName: 'device-name-cell'},
        type: 'textCaseInsensitiveColumn',
        field: 'deviceName',
        sortable: false
      }
    ];

    selectedTargets.forEach(tg => columns.push(getTargetGroupColumnDefinition(tg)));

    return columns;
  }

  function getTargetGroupColumnDefinition(targetGroup) {
    return {
      headerName: targetGroup.targetName,
      cellRenderer: 'MustacheTemplateCellRender',
      cellRendererParams: {'templateName': 'stat-metric-cell'},
      field: targetGroup.id == null ? 'allRecipients' : makeTargetGroupPropertyKey(targetGroup.id),
      type: 'textCaseInsensitiveColumn',
      sortable: false
    };
  }

  function getAllRecipientsTargetGroup() {
    return {
      id: null,
      targetName: t('statistics.default.allRecipients')
    }
  }

  function makeTargetGroupPropertyKey(targetId) {
    return `target${targetId}`;
  }

});
