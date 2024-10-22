(() => {

  class StatisticsTile extends AGN.Lib.Dashboard.BaseMailingStatisticsTile {

    static ID = 'statistics';

    extractStatisticsData(data) {
      const names = [];
      const values = [];

      for (let i = 0; i < data['common'].length; i++) {
        names.push(data['common'][i][0]);
        values.push(parseInt(data['common'][i][1]));
      }

      return {names, values};
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
  }

  AGN.Lib.Dashboard.StatisticsTile = StatisticsTile;
})();
