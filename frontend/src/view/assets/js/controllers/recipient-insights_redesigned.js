AGN.Lib.Controller.new('recipient-insights', function () {

  const MIN_VALUE_THRESHOLD = 0.01;
  const LOW_VALUE_MARK = '< 1';
  const LOW_VALUE = 0.001;  // Any value lower that threshold.

  const REACTION_GRADE_COLORS = {
    'HIGH_PERFORMER': '#4FB102',
    'OPPORTUNITY': '#0ACD70',
    'LEAD': '#C2C2C2',
    'SLEEPER': '#E12E59'
  };

  let customerId;
  let reactionInProgressChart;

  this.addDomInitializer('recipient-insights', function () {
    customerId = this.config.customerId;
    const data = this.config.insightsData;
    $.i18n.load(this.config.translations);

    showWeekdayReactionRankChart(data.weekdayReactionRankData);
    showHourReactionRankChart(data.hourReactionRankData);
    showReactionDeviceRankChart(data.reactionDeviceRankData);
    showReactionsInProgressChart(data.reactionsInProgressData);

    showMailingReactionPercentage(data.mailingReactionPercentageData);
    showPageImpressions(data.pageImpressionsData);
    showCustomerRevenue(data.customerRevenueData);
    showCustomerPerformance(data.customerPerformanceData);
  });

  this.addAction({change: 'selectReactionProgressPeriod'}, function () {
    const $el = this.el;
    const $option = $el.find('option:selected');
    let min, max;

    switch ($el.val()) {
      case 'YEAR':
      case 'MONTH':
        min = $option.data('min');
        max = $option.data('max');
        break;

      case 'CUSTOM':
        const periodDates = getSelectedPeriodDates();
        min = periodDates.min;
        max = periodDates.max;
        break;

      default:
        console.error(`Unexpected period type: '${$el.val()}'`);
        return;
    }

    updateReactionInProgressChart(min, max);
  });

  this.addAction({change: 'period-select'}, function () {
    const periodDates = getSelectedPeriodDates();
    updateReactionInProgressChart(periodDates.min, periodDates.max);
  });

  function updateReactionInProgressChart(min, max) {
    if (min && max) {
      $.ajax(AGN.url('/insights/reactionsInProgress.action'), {
        type: 'GET',
        data: {
          customerId: customerId,
          min: min,
          max: max
        }
      }).done(data => showReactionsInProgressChart(data));
    }
  }

  function getSelectedPeriodDates() {
    return {
      min: $('#reactionProgressMinDate').val(),
      max: $('#reactionProgressMaxDate').val()
    }
  }

  function showWeekdayReactionRankChart(data) {
    data = asRankChartData(data);

    const maxValue = Math.max(...data.values);
    const $chart = $('#days-reaction-chart');

    new Chart($chart[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: t('date.weekdaysShort'),
        datasets: [{
          data: data.values,
          backgroundColor: chartData => {
            const isMaxValue = data.values[chartData.dataIndex] === maxValue;
            return $chart.css(isMaxValue ? '--chart-very-dark-blue-color' : '--chart-blue-color')
          },
          categoryPercentage: 0.9,
          minBarLength: 2
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
              maxRotation: 90
            }
          },
          y: {
            display: false
          }
        },
        plugins: {
          legend: {
            display: false
          },
          datalabels: {
            anchor: 'end',
            align: 'end',
            offset: -5, // make labels closer to the bar
            formatter: (value, context) => data.percentages[context.dataIndex] + '%'
          },
          tooltip: {
            position: 'nearest',
            callbacks: {
              label: context => t('daysWithReactions') + ': ' + context.formattedValue
            }
          }
        }
      }
    });
  }

  function showHourReactionRankChart(data) {
    data = asRankChartData(data);
    const maxValue = Math.max(...data.values);

    const $chart = $('#reaction-hour-chart');

    new Chart($chart[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24],
        datasets: [{
          data: data.values,
          backgroundColor: chartData => {
            const isMaxValue = data.values[chartData.dataIndex] === maxValue;
            return $chart.css(isMaxValue ? '--chart-very-dark-blue-color' : '--chart-blue-color')
          },
          categoryPercentage: 0.5,
          minBarLength: 2
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
              autoSkip: true,
              maxRotation: 0
            }
          },
          y: {
            display: false
          }
        },
        plugins: {
          legend: {
            display: false
          },
          datalabels: {
            anchor: 'end',
            align: 'end',
            offset: -5, // make labels closer to the bar
            display: 'auto', // prevents overlap
            formatter: (value, context) => data.percentages[context.dataIndex] + '%'
          },
          tooltip: {
            callbacks: {
              label: context => t('reactionTimes') + ': ' + context.formattedValue
            }
          }
        }
      }
    });
  }

  function asRankChartData(data) {
    const values = Object.values(data.values);
    const percentages = Object.entries(data.percentages).map(([key, value]) => value);

    return {values, percentages};
  }

  function showReactionDeviceRankChart(data) {
    const $chart = $('#reaction-device-chart');

    const colors = [
      $chart.css('--chart-very-dark-blue-color'),
      $chart.css('--chart-dark-blue-color'),
      $chart.css('--chart-blue-color'),
      $chart.css('--chart-light-blue-color'),
      $chart.css('--chart-light-cyan-color')
    ];
    const values = [];
    const names = [];

    $.each(data, function (index, d) {
      values.push(d.value);
      names.push(t(d.deviceClass));
    });

    new Chart($chart[0].getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: names,
        datasets: [{
          data: values,
          backgroundColor: colors,
          borderWidth: 0
        }]
      }
    });
  }

  function showReactionsInProgressChart(data) {
    if (reactionInProgressChart) {
      reactionInProgressChart.destroy();
    }

    const $chart = $('#reaction-progress-chart');
    const categories = [];
    const reactions = [];
    const impressions = [];
    const revenues = [];

    let isImpressionAvailable = false;
    let isRevenueAvailable = false;

    $.each(data.values, (date, d) => {
      categories.push(date);
      reactions.push(d.reactions);
      impressions.push(d.impressions);
      revenues.push(d.revenues);

      if (d.impressions > 0) {
        isImpressionAvailable = true;
      }
      if (d.revenues > 0) {
        isRevenueAvailable = true;
      }
    });

    const pointsCount = categories.length;
    let pointRadius = 3;

    if (pointsCount < 60) {
      pointRadius = 5;
    } else if (pointsCount < 100) {
      pointRadius = 4;
    }

    const options = {
      type: 'line',
      data: {
        labels: categories,
        datasets: [
          {
            label: t('statistic.reactions'),
            data: reactions,
            borderColor: $chart.css('--chart-very-dark-blue-color'),
            backgroundColor: $chart.css('--chart-very-dark-blue-color'),
            pointRadius: pointRadius
          }
        ]
      },
      options: {
        scales: {
          x: {
            ticks: {
              maxRotation: 0
            }
          },
          y: {
            display: false
          }
        },
        interaction: {
          mode: 'index',
          intersect: false,
        },
        stacked: false,
        plugins: {
          legend: {
            position: 'bottom'
          },
          datalabels: {
            display: false,
          },
          tooltip: {
            callbacks: {
              title: function (tooltipItems) {
                const date = tooltipItems[0].label;

                const mailingIds = data.values[date].mailingIds;
                if (mailingIds.length) {
                  return data.mailingNames[mailingIds[0]] + ' ' + date;
                }

                return date;
              }
            }
          }
        }
      },
    };

    if (isImpressionAvailable) {
      options.data.datasets.push({
        label: t('statistic.impressions'),
        data: impressions,
        borderColor: $chart.css('--chart-blue-color'),
        backgroundColor: $chart.css('--chart-blue-color'),
        pointRadius: pointRadius
      });
    }

    if (isRevenueAvailable) {
      options.data.datasets.push({
        label: t('statistic.revenue'),
        data: revenues,
        borderColor: $chart.css('--chart-dark-blue-color'),
        backgroundColor: $chart.css('--chart-dark-blue-color'),
        pointRadius: pointRadius
      });
    }

    reactionInProgressChart = new Chart($chart[0].getContext('2d'), options);
  }

  function showMailingReactionPercentage(data) {
    showKpiValue('#reactionTypeKpi', data.percentage.percentage);
    $('#reactionGradeIcon').css('color', getActivityGradeColor(data.grade));
  }

  function getActivityGradeColor(grade) {
    return REACTION_GRADE_COLORS[grade] || 'inherit';
  }

  function showPageImpressions(data) {
    showKpiValue('#impressionsByClickKpi', data.perClick);
    showKpiValue('#impressionsByVisitKpi', data.perVisit);
    showKpiValue('#impressionsByBuyKpi', data.perBuy);
  }

  function showCustomerRevenue(data) {
    showKpiValue('#revenueSumKpi', data.value);
    showKpiValue('#revenueAvgKpi', data.average);
  }

  function showCustomerPerformance(data) {
    if (data.clicks.percentage > 0) {
      showKpiValue('#performanceClicksKpi', data.clicks.percentage);
    } else {
      showKpiValue('#performanceClicksKpi', data.clicks.value > 0 ? LOW_VALUE : 0);
    }
    showKpiValue('#performanceRevenueKpi', data.revenue);
  }

  function showKpiValue(selector, value) {
    const $content = $(selector);
    const $value = $content.find('[data-kpi-value]');

    if (value > 0) {
      if (value < MIN_VALUE_THRESHOLD) {
        $value.text(LOW_VALUE_MARK);
      } else {
        let valueSign = '';
        if ($value.is('[data-kpi-sign]')) {
          valueSign = ' ' + $value.data('kpi-sign');
        }

        $value.text(`${value.toFixed(2)}${valueSign}`);
      }
    }
  }

});