AGN.Lib.Controller.new('target-group-stat', function () {

  this.addDomInitializer('chart', function () {
    const data = this.config.statistics;
    if (!data?.length) {
      return;
    }

    const $chart = $('#target-statistic-chart');

    const colors = [
      $chart.css('--chart-dark-blue-color'),
      $chart.css('--chart-blue-color'),
      $chart.css('--chart-light-blue-color'),
      $chart.css('--chart-cyan-color'),
      $chart.css('--chart-light-cyan-color'),
      $chart.css('--chart-light-lime-color')
    ].splice(0, data.length);

    new Chart($chart[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: data.map(d => d.status),
        datasets: [{
          data: data.map(d => d.recipientsCount),
          backgroundColor: colors,
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
  });
});
