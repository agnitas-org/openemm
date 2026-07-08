AGN.Lib.Controller.new('progress-stat', function () {

  const ChartUtils = AGN.Lib.ChartUtils;
  const Template = AGN.Lib.Template;

  const deviceClassColorsMap = new Map([
    [1, ChartUtils.getLimeColor()],
    [2, ChartUtils.getLightVioletColor()],
    [3, ChartUtils.getDarkCyanColor()],
    [4, ChartUtils.getDarkRedColor()],
    [null, ChartUtils.getBlueColor()],
  ]);

  const deviceNamesMap = new Map(
    [
      [1, 'desktop'],
      [2, 'mobile'],
      [3, 'tablet'],
      [4, 'smarttv'],
      [null, 'mixed']
    ].map(([id, key]) => [id, t(`statistics.device.${key}`)])
  );

  const openingsMetrics = [
    {
      title: t('statistics.progress.openersNet'),
      propName: 'unique'
    },
    {
      title: t('statistics.progress.openingsGross'),
      propName: 'total'
    },
    {
      title: t('statistics.progress.anonymousOpenings'),
      propName: 'anonymous'
    }
  ];

  const clicksMetrics = [
    {
      title: t('statistics.default.clicks'),
      propName: 'total'
    },
    {
      title: t('statistics.progress.anonymousClicks'),
      propName: 'anonymous'
    }
  ];

  const linkClicksMetrics = [
    {
      title: t('statistics.default.clicks'),
      propName: 'total'
    },
    {
      title: t('statistics.default.clickers'),
      propName: 'unique'
    }
  ]

  let config;
  let $container;
  let selectedTargetIds = [];
  let targetGroupsMap;

  this.addDomInitializer("mailing-progress-stat", function () {
    config = this.config;
    $container = this.el;
    selectedTargetIds = config.selectedTargets.map(tgId => parseInt(tgId));
    targetGroupsMap = getSelectedTargetGroupsMap(config.targetGroups);

    fetchData().done(resp => {
      drawDeliveryProgressChart($('#delivery-progress-chart'), resp.deliveries);
      drawEventProgressTiles(resp.targetGroupOpenings, openingsMetrics);
      drawEventProgressTiles(resp.targetGroupClicks, clicksMetrics);

      $('#stat-loader').addClass('hidden')
      $container.removeClass('invisible');
    });
  });

  this.addDomInitializer("link-clicks-progress-stat", function () {
    config = this.config;
    $container = this.el;
    selectedTargetIds = config.selectedTargets.map(tg => tg.id);
    targetGroupsMap = getSelectedTargetGroupsMap(config.selectedTargets);

    drawEventProgressTiles(config.data, linkClicksMetrics);
  });

  function getSelectedTargetGroupsMap(targetGroups) {
    const selectedTargets = targetGroups.filter(tg => selectedTargetIds.includes(tg.id));
    selectedTargets.unshift(getAllRecipientsTargetGroup());

    return new Map(
      selectedTargets.map(target => [target.id, target])
    );
  }

  function getAllRecipientsTargetGroup() {
    return {
      id: null,
      targetName: t('statistics.default.allRecipients')
    }
  }

  function fetchData() {
    return $.get(
      AGN.url(`/statistics/mailing/${(config.mailingId)}/progressStat.action`),
      {
        from: config.from,
        to: config.to,
        hourScale: config.isHourScale,
        targetGroups: selectedTargetIds
      });
  }

  function drawDeliveryProgressChart($canvas, entries) {
    const datasets = [{
      data: entries.map(e => ({
        x: e.timestamp,
        y: e.count
      })),
      backgroundColor: ChartUtils.getDarkestBlueColor(),
      categoryPercentage: 1
    }];

    drawProgressBarChart($canvas, datasets, getTimeBarChartOptions());
  }

  function drawEventProgressTiles(entries, metrics) {
    const deviceClassIds = collectDeviceClassIds(entries);

    metrics.forEach(m => {
      if (selectedTargetIds.length) {
        deviceClassIds.forEach(deviceClassId => {
          const datasets = collectDatasetsForTargetGroups(entries, deviceClassId, m.propName);
          drawMetricTile(m, datasets, deviceClassId);
        });
      } else {
        const datasets = collectDatasetsForDevices(entries[0].deviceClassSeries, m.propName);
        drawMetricTile(m, datasets);
      }
    });
  }

  function drawMetricTile(metric, datasets, deviceClassId) {
    const $tile = Template.dom('progress-metric-tile', {
      ...metric,
      deviceClassName: deviceNamesMap.get(deviceClassId) ?? ''
    });

    $container.append($tile);
    drawEventsProgressChart($tile.find('canvas'), datasets);
  }

  function collectDatasetsForDevices(series, propName) {
    return series.map(s => ({
      label: deviceNamesMap.get(s.deviceClassId),
      data: s.points.map(p => ({
        x: p.timestamp,
        y: p[propName]
      })),
      backgroundColor: deviceClassColorsMap.get(s.deviceClassId),
      categoryPercentage: 1
    }));
  }

  function collectDatasetsForTargetGroups(targetGroups, deviceClassId, propName) {
    const colors = AGN.Lib.Helpers.getStatColorsHexValues();
    const datasets = [];

    targetGroups.forEach((tg, idx) => {
      const series = tg.deviceClassSeries.find(s => s.deviceClassId === deviceClassId);
      if (!series) {
        return;
      }

      const dataPoints = series.points.map(p => ({
        x: p.timestamp,
        y: p[propName]
      }));

      datasets.push({
        label: targetGroupsMap.get(tg.targetGroupId).targetName,
        data: dataPoints,
        backgroundColor: colors[idx % colors.length],
        categoryPercentage: 1
      });
    });

    return datasets;
  }

  function drawEventsProgressChart($canvas, datasets) {
    const options = getTimeBarChartOptions(window.adminDateFormat,{
      plugins: {
        legend: {
          display: true,
          position: 'bottom'
        }
      }
    });

    drawProgressBarChart($canvas, datasets, options);
  }

  function collectDeviceClassIds(targetGroupEntries) {
    const deviceClassIds = new Set();
    targetGroupEntries.forEach(tg =>
      tg.deviceClassSeries.forEach(s => deviceClassIds.add(s.deviceClassId))
    );

    return deviceClassIds;
  }

  function drawProgressBarChart($canvas, datasets, options) {
    if (!datasets.length || datasets.every(({data}) => data.length === 0)) {
      $canvas.replaceWith(Template.dom('no-results-found'));
      return;
    }

    new Chart($canvas[0].getContext('2d'), {
      type: 'bar',
      data: { datasets },
      options
    });
  }

  function getTimeBarChartOptions(dateFormat = window.adminDateTimeFormat, customOptions = {}) {
    return _.merge({}, {
      layout: {
        padding: {
          top: 20,
          right: 0
        }
      },
      scales: {
        x: {
          type: 'time',
          time: {
            unit: config.isHourScale ? 'hour' : 'day',
            displayFormats: {
              day: config.isHourScale ? '' : dateFormat,
              hour: config.isHourScale ? 'hh:mm' : ''
            },
            tooltipFormat: dateFormat
          },
          ticks: {
            maxRotation: 0
          },
          min: config.from,
          max: config.to
        },
        y: {
          display: false
        }
      },
      plugins: {
        datalabels: {
          formatter: v => AGN.formatNumber(v.y)
        }
      }
    }, customOptions);
  }

});
