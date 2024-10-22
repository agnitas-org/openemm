AGN.Lib.Controller.new('company-insights', function () {

  const ChartUtils = AGN.Lib.ChartUtils;

  const REVENUE_ICON_MAX_HEIGHT = 50; // px
  const MIN_REVENUE_BAR_LENGTH = REVENUE_ICON_MAX_HEIGHT + 10; // px

  let $typeOfReactorChart;
  let $rebuyRateChart;
  let $revenueChart;
  let $reactionDevicesChart;
  let $devicesProgressChart;

  let revenueImages = [];

  this.addAction({change: 'load-dynamic-statistic'}, function () {
    loadDynamicStatistic();
  });

  this.addDomInitializer('company-insights', function () {
    revenueImages = this.config.revenueImages;

    $typeOfReactorChart = $('#type-of-reactor-chart');
    $rebuyRateChart = $('#rebuy-rate-chart');
    $revenueChart = $('#revenue-chart');
    $reactionDevicesChart = $('#reaction-devices-chart');
    $devicesProgressChart = $('#devices-progress-chart');

    loadDynamicStatistic();

    const chartData = this.config.chartData;
    if (this.config.chartData) {
      showReactorChart(chartData['reactorType']);
      showRebuyRateChart(chartData['rebuyRate']);
      showRevenueChart(chartData['revenue']);
      showImpressionStatistics(chartData);
    } else {
      toggleNotDataAvailableMsg($typeOfReactorChart, true);
      toggleNotDataAvailableMsg($rebuyRateChart, true);
      toggleNotDataAvailableMsg($revenueChart, true);
    }
  });

  function showReactorChart(data) {
    if (Object.values(data).some(value => value <= 0)) {
      toggleNotDataAvailableMsg($typeOfReactorChart, true);
      return;
    }

    const colors = [
      $typeOfReactorChart.css('--chart-very-dark-blue-color'),
      $typeOfReactorChart.css('--chart-blue-color'),
      $typeOfReactorChart.css('--chart-light-cyan-color')
    ];

    new Chart($typeOfReactorChart[0].getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: Object.keys(data),
        datasets: [{
          data: Object.values(data),
          backgroundColor: colors,
          borderWidth: 0
        }]
      },
      options: {
        plugins: {
          'half-doughnut-label': false,
          revenueIcon: false,
          hideXAxisIfLabelsOverlaps: false
        }
      }
    });
  }

  function showRebuyRateChart(rate) {
    if (rate <= 0) {
      toggleNotDataAvailableMsg($rebuyRateChart, true);
      return;
    }

    rate = toPercents(rate);

    const colors = [
      $rebuyRateChart.css('--chart-blue-color'),
      $rebuyRateChart.css('--chart-light-grey-color')
    ];

    const halfDoughnutLabelColor = $rebuyRateChart.css('--chart-half-doughnut-label-color');

    Chart.register({
      id: 'half-doughnut-label',
      beforeDraw: (chart, option) => {
        const yCenter = chart.getDatasetMeta(0).data[0].y;
        const fontSize = Math.floor(chart.height * 0.10);

        chart.ctx.textBaseline = 'middle';
        chart.ctx.textAlign = 'center';
        chart.ctx.font = `600 ${fontSize}px ${Chart.defaults.font.family}`;
        chart.ctx.fillStyle = halfDoughnutLabelColor;
        chart.ctx.fillText(`${rate}%`, chart.width / 2, yCenter - (fontSize / 3));
      }
    });

    new Chart($rebuyRateChart[0].getContext('2d'), {
      type: 'doughnut',
      data: {
        datasets: [{
          data: [rate, 100 - rate],
          backgroundColor: colors,
          borderWidth: 0
        }]
      },
      options: {
        rotation: -90,
        circumference: 180,
        plugins: {
          legend: {
            display: false
          },
          datalabels: {
            display: false
          },
          tooltip: {
            external: null
          },
          revenueIcon: false,
          hideXAxisIfLabelsOverlaps: false
        }
      }
    });
  }

  function showRevenueChart(data) {
    const valuesSum = ChartUtils.getTotal(Object.values(data));

    if (valuesSum <= 0) {
      toggleNotDataAvailableMsg($revenueChart, true);
      return;
    }

    const colors = [
      $revenueChart.css('--chart-very-dark-blue-color'),
      $revenueChart.css('--chart-dark-blue-color'),
      $revenueChart.css('--chart-blue-color'),
      $revenueChart.css('--chart-light-blue-color')
    ];

    const images = revenueImages.map(src => {
      const image = new Image();
      image.src = src;
      return image;
    });

    Chart.register({
      id: 'revenueIcon',
      afterDraw: chart => {
        const bars = chart.getDatasetMeta(0).data;
        _.each(Object.keys(bars), (key, index) => {
          const image = images[index];

          if (image.complete) {
            const bar = bars[key];
            const startX = bar.x - (bar.width / 2);
            const endY = bar.y + bar.height;

            const aspectRatio = image.width / image.height;
            const maxWidth = (40 * bar.width) / 100; // 40% of bar width

            let imgWidth = Math.min(maxWidth, aspectRatio * REVENUE_ICON_MAX_HEIGHT);
            let imgHeight = imgWidth / aspectRatio;

            if (imgHeight > REVENUE_ICON_MAX_HEIGHT) {
              imgHeight = REVENUE_ICON_MAX_HEIGHT;
              imgWidth = imgHeight * aspectRatio;
            }

            const xPos = startX + ((bar.width - imgWidth) / 2);
            const yPos = endY - ((MIN_REVENUE_BAR_LENGTH - imgHeight) / 2) - imgHeight;

            chart.ctx.drawImage(image, xPos, yPos, imgWidth, imgHeight);
          } else {
            image.onload = () => chart.draw();
          }
        });
      }
    });

    Chart.register({
      id: 'hideXAxisIfLabelsOverlaps',
      afterRender: chart => {
        const bars = chart.getDatasetMeta(0).data;

        chart.options.scales.x.display = !existsOverlappedLabels(chart.data.labels, bars, chart.ctx);
        chart.update();
      }
    });

    new Chart($revenueChart[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: Object.keys(data),
        datasets: [{
          data: Object.values(data),
          backgroundColor: colors,
          categoryPercentage: 0.9,
          minBarLength: MIN_REVENUE_BAR_LENGTH
        }]
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
              maxRotation: 0
            }
          },
          y: {
            display: false
          }
        },
        plugins: {
          'half-doughnut-label': false,
          legend: {
            display: false
          },
          datalabels: {
            anchor: 'end',
            align: 'end',
            offset: -5, // make labels closer to the bar
            formatter: (value, context) => {
              const percent = (value * 100 / valuesSum).toFixed(1);
              return `${percent}%`;
            }
          }
        }
      }
    });
  }

  function showImpressionStatistics(data) {
    showFavDaysStatistic(data['favoriteOpeningDays']);
    showFavTimesStatistic(data['favoriteOpeningHours']);
    showCtr(data['ctr']);
    showConversion(data['conversion']);
    showPI(data['pageImpressions']);
    showPIPerVisit(data['pageImpressionsPerVisit']);
    showPIPerBuy(data['pageImpressionsPerBuy']);
  }

  function showFavDaysStatistic(data) {
    const $container = $('#fav-days-stat-block');
    if (Object.keys(data).length > 0) {
      $container.empty();
    }

    $.each(data, (day, value) => {
      $container.append(AGN.Lib.Template.dom('company-insights-fav-day-record', {day, value}));
    });
  }

  function showFavTimesStatistic(data) {
    const $container = $('#fav-time-stat-block');

    if (Object.keys(data).length > 0) {
      $container.empty();
    }

    $.each(data, (time, value) => {
      $container.append(AGN.Lib.Template.dom('company-insights-fav-time-record', {time, value}));
    });
  }

  function showCtr(ctr) {
    $('#ctr-val').text(ctr.toString().replace('.', ',') + ' %');
  }

  function showConversion(conversion) {
    if (conversion != 0) {
      $('#conversion-val').text(conversion.toString().replace('.', ',') + ' %');
    }
  }

  function showPI(pi) {
    if (pi != 0) {
      $('#pi-val').text(pi.toString().replace(',', '.') + ' Ø');
    }
  }

  function showPIPerVisit(piPerVisit) {
    if (piPerVisit != 0) {
      $('#pi-visit-val').text(piPerVisit.toString().replace(',', '.') + ' Ø');
    }
  }

  function showPIPerBuy(piPerBuy) {
    if (piPerBuy != 0) {
      $('#pi-buy-val').text(piPerBuy.toString().replace(',', '.') + ' Ø');
    }
  }

  function loadDynamicStatistic() {
    const periodType = $('#progressOfUsedPeriod').val();

    const data = {};
    if (periodType === 'MONTH') {
      data['byMonth'] = true;
    } else if (periodType == 'PERIOD') {
      data['from'] = $('#progressOfUsedFrom').val();
      data['till'] = $('#progressOfUsedTill').val();
    }

    $.get(AGN.url('/recipient/chart/progressOfUsedDevices.action'), data).done(data => {
      showReactionDevicesChart(data);
      showProgressOfUsedDevicesChart(data);
    });
  }

  function showReactionDevicesChart(data) {
    const chartData = data['usedDevices'];

    const categories = [
      {title: data['windowsTitle'], count: chartData['windows']},
      {title: data['macTitle'], count: chartData['mac']},
      {title: data['androidTitle'], count: chartData['android']},
      {title: data['iosTitle'], count: chartData['ios']}
    ];

    //Sort usage
    categories.sort((a, b) => b.count - a.count);

    const labels = categories.map(c => c.title);
    const values = categories.map(c => c.count);

    labels.push(data['otherTitle']);
    values.push(chartData['other']);

    if (ChartUtils.getTotal(values) === 0) {
      toggleNotDataAvailableMsg($reactionDevicesChart, true);
      return;
    }

    toggleNotDataAvailableMsg($reactionDevicesChart, false);

    const colors = [
      $reactionDevicesChart.css('--chart-very-dark-blue-color'),
      $reactionDevicesChart.css('--chart-dark-blue-color'),
      $reactionDevicesChart.css('--chart-blue-color'),
      $reactionDevicesChart.css('--chart-light-blue-color'),
      $reactionDevicesChart.css('--chart-light-cyan-color')
    ];

    Chart.getChart($reactionDevicesChart)?.destroy();

    new Chart($reactionDevicesChart[0].getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: labels,
        datasets: [{
          data: values,
          backgroundColor: colors,
          borderWidth: 0
        }]
      },
      options: {
        plugins: {
          'half-doughnut-label': false,
          revenueIcon: false,
          hideXAxisIfLabelsOverlaps: false
        }
      }
    });
  }

  function showProgressOfUsedDevicesChart(data) {
    const lineColor = $devicesProgressChart.css('--chart-black-color');

    const labels = [];
    const windowsValues = [];
    const macValues = [];
    const androidValues = [];
    const iosValues = [];
    const otherValues = [];
    const reactionsValues = [];

    for (let i = 0; i < data['usedDevicesByPeriods'].length; i++) {
      const usedDevices = data['usedDevicesByPeriods'][i];

      const {windows, mac, android, ios, other} = usedDevices;
      const reactions = windows + mac + android + ios + other;

      windowsValues.push(toPercents(windows / reactions));
      macValues.push(toPercents(mac / reactions));
      androidValues.push(toPercents(android / reactions));
      iosValues.push(toPercents(ios / reactions));
      otherValues.push(toPercents(other / reactions));

      reactionsValues.push(reactions);
      labels.push(usedDevices['date']);
    }

    const maxReaction = Math.max(...reactionsValues);

    if (maxReaction === 0) {
      toggleNotDataAvailableMsg($devicesProgressChart, true);
      return;
    } else {
      toggleNotDataAvailableMsg($devicesProgressChart, false);
    }

    Chart.getChart($devicesProgressChart)?.destroy();

    new Chart($devicesProgressChart[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: data['numberOfReactions'],
            data: reactionsValues,
            borderColor: lineColor,
            backgroundColor: lineColor,
            type: 'line',
            yAxisID: 'reactions-y'
          },
          {
            label: data['otherTitle'],
            backgroundColor: $devicesProgressChart.css('--chart-light-cyan-color'),
            data: otherValues
          },
          {
            label: data['iosTitle'],
            backgroundColor: $devicesProgressChart.css('--chart-light-blue-color'),
            data: iosValues
          },
          {
            label: data['androidTitle'],
            backgroundColor: $devicesProgressChart.css('--chart-blue-color'),
            data: androidValues
          },
          {
            label: data['macTitle'],
            backgroundColor: $devicesProgressChart.css('--chart-dark-blue-color'),
            data: macValues
          },
          {
            label: data['windowsTitle'],
            backgroundColor: $devicesProgressChart.css('--chart-very-dark-blue-color'),
            data: windowsValues
          }
        ]
      },
      options: {
        interaction: {
          mode: 'index' // to display all data from datasets in tooltip at once
        },
        plugins: {
          'half-doughnut-label': false,
          revenueIcon: false,
          hideXAxisIfLabelsOverlaps: false,
          datalabels: {
            display: false,
          },
          legend: {
            position: 'bottom',
            reverse: true // reverse order (to display 'Reactions' legend at the end)
          },
          tooltip: {
            callbacks: {
              label: function (context) {
                let value = context.raw;
                if (context.datasetIndex > 0) {
                  value += '%';
                }

                return `${context.dataset.label}: ${value}`;
              }
            },
            itemSort: (a, b) => b.datasetIndex - a.datasetIndex
          }
        },
        scales: {
          x: {
            stacked: true,
            ticks: {
              maxRotation: 0
            }
          },
          y: {
            stacked: true,
            min: 0,
            max: 100,
            ticks: {
              callback: (value, index, values) => `${value}%`
            }
          },
          'reactions-y': {
            type: 'linear',
            position: 'right',
            min: 0,
            max: maxReaction
          }
        }
      },
    });
  }

  /**
   * 0.253213 -> 25.3
   * @returns {number}
   */
  function toPercents(num) {
    return Math.round(1000 * num) / 10;
  }

  function existsOverlappedLabels(labels, bars, ctx) {
    if (labels.length < 2) {
      return false;
    }

    const PADDING_BETWEEN = 2;
    const labelsSizes = labels.map(l => ctx.measureText(l).width);

    for (let i = 1; i < labels.length; i++) {
      const startPos = bars[i].x - (labelsSizes[i] / 2);
      const prevEndPos = bars[i - 1].x + (labelsSizes[i - 1] / 2);

      if (startPos - PADDING_BETWEEN <= prevEndPos) {
        return true;
      }
    }

    return false;
  }

  function toggleNotDataAvailableMsg($chart, show) {
    $chart.toggleClass('hidden', show);
    $chart.next().toggleClass('hidden', !show);
  }

});
