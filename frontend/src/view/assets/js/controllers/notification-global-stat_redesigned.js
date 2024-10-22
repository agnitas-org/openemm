AGN.Lib.Controller.new('notification-global-stat', function () {

  let $statusChart;
  let $browserChart;
  let $progressChart;

  this.addDomInitializer("notification-global-stat", function () {
    $statusChart = $('#global-status-chart');
    $browserChart = $('#global-browser-chart');
    $progressChart = $('#global-progress-chart');

    updateStatusChart();
    updateBrowserChart();
    updateProgressChart();
  });

  this.addAction({
    change: 'update-progress-chart'
  }, function () {
    updateProgressChart();
  });

  function updateStatusChart() {
    $.get(AGN.url('/push/ajaxGetGlobalStatusStatistic.action')).done(data => {
      if (data && data.warning && data.warning.length > 0) {
        AGN.Lib.JsonMessages(data, true);
      } else {
        new Chart($statusChart[0].getContext('2d'), {
          type: 'bar',
          data: {
            labels: Object.keys(data),
            datasets: [{
              data: Object.values(data),
              backgroundColor: $statusChart.css('--chart-blue-color'),
              categoryPercentage: 1.08,
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
                offset: -5 // make labels closer to the bar
              }
            }
          }
        });
      }
    });
  }

  function updateBrowserChart() {
    $.get(AGN.url('/push/ajaxGetGlobalBrowserStatistic.action')).done(data => {
      if (data && (data.warning && data.warning.length > 0)) {
        AGN.Lib.JsonMessages(data, true);
      } else {
        const titles = [];
        const values = [];
        for (const key in data) {
          titles.push(key);
          values.push(roundTo(data[key] * 100, 1));
        }

        const colors = [
          $browserChart.css('--chart-very-dark-blue-color'),
          $browserChart.css('--chart-dark-blue-color'),
          $browserChart.css('--chart-blue-color'),
          $browserChart.css('--chart-light-blue-color'),
          $browserChart.css('--chart-light-cyan-color')
        ];

        new Chart($browserChart[0].getContext('2d'), {
          type: 'agnDoughnut',
          data: {
            labels: titles,
            datasets: [{
              data: values,
              backgroundColor: colors,
              borderWidth: 0
            }]
          }
        });
      }
    });
  }

  function updateProgressChart() {
    if (!$progressChart.exists() || !isValidDateRanges()) {
      return;
    }

    Chart.getChart($progressChart)?.destroy();

    const labels = [];
    const data1 = [];
    const data2 = [];
    const data3 = [];

    $.get(AGN.url('/push/ajaxGetGlobalProgressStatistic.action'), {
      dateMode: $('#periodType').val(),
      selectMonth: $('#selectMonth').val(),
      selectYear: $('#selectYear').val(),
      startDay: $('#startDay').val(),
      endDay: $('#endDay').val()
    }).done(data => {
      if (data && data.warning && data.warning.length > 0) {
        AGN.Lib.JsonMessages(data, true);
      } else {
        for (const dataIndex in data) {
          const item = data[dataIndex];
          for (const key in item) {
            //set row name
            if (dataIndex == 0 && key != 'time') {
              if (data1.length == 0) {
                data1.push(key);
              } else if (data2.length == 0) {
                data2.push(key);
              } else if (data3.length == 0) {
                data3.push(key);
              }
            }

            //set row value
            const value = item[key];
            if (key == 'time') {
              labels.push(value);
            } else if (key == data1[0]) {
              data1.push(value);
            } else if (key == data2[0]) {
              data2.push(value);
            } else if (key == data3[0]) {
              data3.push(value);
            }
          }
        }

        const pointRadius = getPointRadius(labels.length);

        new Chart($progressChart[0].getContext('2d'), {
          type: 'line',
          data: {
            labels: labels,
            datasets: [
              {
                label: data1[0],
                data: data1.slice(1),
                borderColor: $progressChart.css('--chart-very-dark-blue-color'),
                backgroundColor: $progressChart.css('--chart-very-dark-blue-color'),
                pointRadius: pointRadius
              },
              {
                label: data2[0],
                data: data2.slice(1),
                borderColor: $progressChart.css('--chart-blue-color'),
                backgroundColor: $progressChart.css('--chart-blue-color'),
                pointRadius: pointRadius
              },
              {
                label: data3[0],
                data: data3.slice(1),
                borderColor: $progressChart.css('--chart-light-cyan-color'),
                backgroundColor: $progressChart.css('--chart-light-cyan-color'),
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
                min: 0
              }
            },
            interaction: {
              mode: 'index',
              intersect: false
            },
            stacked: false,
            plugins: {
              legend: {
                position: 'bottom'
              },
              datalabels: {
                display: false
              }
            }
          }
        });
      }
    });
  }

  function isValidDateRanges() {
    if ($('#periodType').val() !== 'RANGE') {
      return true;
    }

    const $startDate = $('#startDay');
    const $endDate = $('#endDay');

    if (!$startDate.val() || !$endDate.val() || getDate($startDate).getTime() > getDate($endDate).getTime()) {
      AGN.Lib.Messages.alert('error.statistic.period_format');
      return false;
    }

    return true;
  }

  function getDate($input) {
    const format = $input.datepicker("option", "dateFormat");
    return $.datepicker.parseDate(format, $input.val());
  }

  function getPointRadius(pointsCount) {
    if (pointsCount < 60) {
      return 5;
    }

    if (pointsCount < 100) {
      return 4;
    }

    return 3;
  }

  function roundTo(number, fractionalDigits) {
    return (parseFloat(number).toFixed(fractionalDigits)) * 1.0;
  }
});
