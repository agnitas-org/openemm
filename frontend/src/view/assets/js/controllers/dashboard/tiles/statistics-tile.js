class StatisticsTile extends BaseMailingStatisticsTile {

  constructor() {
    super();
  }

  fetchChartData() {
    const mailingId = this._chartData.mailingId;
    return new Promise(function(resolve, reject) {
      $.ajax({
        type: "GET",
        url: AGN.url('/dashboard/statistics.action'),
        data: {
          mailingId: mailingId
        },
        success: function(data) {
          const rowNames = [];
          const rowValues = [];

          for (let i = 0; i < data['common'].length; i++) {
            rowNames.push(data['common'][i][0]);
            rowValues.push(parseInt(data['common'][i][1]));
          }

          resolve({
            names: rowNames,
            values: rowValues
          });
        },
        error: function(error) {
          reject(error);
        }
      });
    });
  }

  getChartOptions() {
    const isMobileView = this._isMobileView;

    const barColor = this.$el.css('--chart-blue-color');

    return {
      type: 'bar',
      data: {
        labels: this._chartData.names,
        datasets: [{
          data: this._chartData.values,
          backgroundColor: barColor,
          categoryPercentage: isMobileView ? 0.7 : 0.9,
          minBarLength: 2
        }]
      },
      options: {
        indexAxis: isMobileView ? 'y' : 'x',
        layout: {
          padding: {
            top: isMobileView ? 0 : 20,
            right: isMobileView ? 25 : 0
          }
        },
        scales: {
          x: {
            display: !isMobileView,
            ticks: {
              autoSkip: false,
              maxRotation: 90,
              minRotation: 0
            }
          },
          y: {
            display: isMobileView,
            ticks: {
              mirror: true,
              labelOffset: -37.5
            }
          }
        },
        plugins: {
          legend: {
            display: false
          },
          datalabels: {
            anchor: 'end',
            align: 'end',
            offset: isMobileView ? 0 : -5 // make labels closer to the bar
          }
        }
      }
    };
  }

  needsRedrawOnMobile() {
    return true;
  }

  get id() {
    return DraggableTile.def.TILE.ID.STATISTICS;
  }
}
