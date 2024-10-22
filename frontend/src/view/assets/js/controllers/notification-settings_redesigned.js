AGN.Lib.Controller.new('notification-settings', function () {

  let $timelineHandler;
  let previewDisabledMsg;

  this.addDomInitializer("notification-settings", function () {
    previewDisabledMsg = this.config.previewDisabledMsg;
    if (Notification.permission == 'denied') {
      disablePreviewOption();
    }
  });

  this.addAction({click: 'preview'}, function () {
    if (Notification.permission != 'granted') {
      Notification.requestPermission(result => {
        if (result == 'granted') {
          showNotification();
        } else if (result == 'denied') {
          disablePreviewOption();
        }
      });
    } else {
      showNotification();
    }
  });

  function showNotification() {
    const notification = new Notification($('#pushTitle').val(), {
      body: $('#pushContent').val(),
      icon: $('#pushIcon').val()
    });

    notification.onclick = event => {
      event.preventDefault();
      window.open($('#pushLink').val(), '_blank')
    };
  }

  function disablePreviewOption() {
    const $previewOption = $('#preview-option');
    $previewOption.parent().attr('data-tooltip', previewDisabledMsg);
    $previewOption.addClass('disabled');
    AGN.runAll($previewOption.parent());
  }

  this.addDomInitializer("notification-stats", function () {
    $timelineHandler = $('.chart-timeline__handle');

    $.get(AGN.url(`/push/${this.config.pushId}/ajaxStatistic.action`)).done(data => {
      showCommonStatisticChart(data['common']);
      showProgressChart(data['viewsclicks']);
    });
  });

  function showCommonStatisticChart(data) {
    const $chart = $('#common-stat-chart');

    new Chart($chart[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: Object.keys(data),
        datasets: [{
          data: Object.values(data),
          backgroundColor: $chart.css('--chart-blue-color'),
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
            offset: -5 // make labels closer to the bar
          }
        }
      }
    });
  }

  function showProgressChart(data) {
    if (!data.length) {
      $('#progress-chart-block').addClass('hidden');
      return;
    }

    const labels = [];
    const data1 = [];
    const data2 = [];

    for (const index in data) {
      const item = data[index];
      for (const key in item) {
        if (item.hasOwnProperty(key)) {
          //set data row name
          if (index == 0 && key != 'time') {
            if (data1.length == 0) {
              data1.push(key);
            } else if (data2.length == 0) {
              data2.push(key);
            }
          }

          //set row value
          if (key == 'time') {
            labels.push(item[key]);
          } else if (data1[0] == key) {
            data1.push(item[key]);
          } else if (data2[0] == key) {
            data2.push(item[key]);
          }
        }
      }
    }

    const pointRadius = getPointRadius(labels.length);
    const $chart = $('#progress-chart');
    const $miniMapChart = $('#progress-chart-minimap');

    const datasets = [
      {
        label: data1[0],
        data: data1.slice(1),
        borderColor: $chart.css('--chart-very-dark-blue-color'),
        backgroundColor: $chart.css('--chart-very-dark-blue-color'),
        pointRadius: pointRadius
      },
      {
        label: data2[0],
        data: data2.slice(1),
        borderColor: $chart.css('--chart-violet-color'),
        backgroundColor: $chart.css('--chart-violet-color'),
        pointRadius: pointRadius
      }
    ];

    new Chart($chart[0].getContext('2d'), {
      type: 'line',
      data: {
        labels: labels,
        datasets: datasets
      },
      options: {
        scales: {
          x: {
            type: 'time',
            min: labels[0],
            max: labels[labels.length - 1],
            time: {
              unit: 'day',
              displayFormats: {day: 'yyyy-MM-dd hh:mm'},
              tooltipFormat: 'yyyy-MM-dd hh:mm'
            },
            ticks: {
              autoSkip: true,
              maxRotation: 0
            }
          },
          y: {min: 0}
        },
        interaction: {mode: 'index'},
        plugins: {
          legend: {position: 'bottom'},
          datalabels: {display: false},
          zoom: {
            pan: {
              enabled: true,
              onPan: handleZoomOrPanChange,
              mode: 'x'
            },
            limits: {
              x: {
                min: new Date(labels[0]).getTime(),
                max: new Date(labels[labels.length - 1]).getTime(),
                minRange: 1
              }
            },
            zoom: {
              enabled: true,
              wheel: {enabled: true,},
              pinch: {enabled: true},
              mode: 'x',
              onZoom: handleZoomOrPanChange
            }
          }
        }
      }
    });

    new Chart($miniMapChart[0].getContext('2d'), {
      type: 'line',
      data: {
        labels: labels,
        datasets: datasets.map(dataset => ({
          ...dataset,
          pointRadius: 0,
          borderWidth: 0.8
        })),
      },
      options: {
        scales: {
          x: {display: false},
          y: {display: false}
        },
        plugins: {
          legend: {display: false},
          datalabels: {display: false},
          tooltip: {external: null}
        }
      }
    });
  }

  function handleZoomOrPanChange({chart}) {
    if (!chart.timelineInitialized) {
      chart.timelineInitialized = true;
      initializeTimelineHandler(chart);
    }

    const {labels} = chart.config.data;
    const {min, max} = chart.scales['x'];

    const originScaleMin = new Date(labels[0]).getTime();
    const originScaleMax = new Date(labels[labels.length - 1]).getTime();

    const left = (min - originScaleMin) / (originScaleMax - originScaleMin) * 100;
    const right = (max - originScaleMin) / (originScaleMax - originScaleMin) * 100;

    const l = left > 0 ? Math.min(left, 98) : 0;
    const r = right < 100 ? Math.max(right, 2) : 100;

    $timelineHandler.prev().css('width', `${l}%`);
    $timelineHandler.css('left', `${l}%`);
    $timelineHandler.css('width', `${r - l}%`);
    $timelineHandler.next().css('width', `${100 - r}%`);
  }

  function initializeTimelineHandler(chart) {
    const hammerManager = new Hammer.Manager($timelineHandler[0]);
    hammerManager.add(new Hammer.Pinch());
    hammerManager.add(new Hammer.Pan({threshold: 10}));

    let currentDeltaX = 0;
    let currentDeltaY = 0;

    const handlePan = e => {
      const deltaX = e.deltaX - currentDeltaX;
      const deltaY = e.deltaY - currentDeltaY;
      currentDeltaX = e.deltaX;
      currentDeltaY = e.deltaY;
      const percent = Math.max(parseFloat($timelineHandler[0].style.width) / 100, 0.1);

      chart.pan({x: -deltaX / percent});
    };

    hammerManager.on('panstart', e => {
      currentDeltaX = 0;
      currentDeltaY = 0;
      handlePan(e);
    });
    hammerManager.on('panmove', handlePan);
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
});
