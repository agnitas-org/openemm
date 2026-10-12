(() => {

  class StatisticsTile extends AGN.Lib.Dashboard.BaseMailingStatisticsTile {

    static ID = 'statistics';

    getMetrics() {
      return {
        [t('statistics.default.sent')]: this._data.sent,
        [t('statistics.default.openers')]: this._data.openers,
        [t('statistics.default.clicker')]: this._data.clickers,
        [t('statistics.optOuts')]: this._data.optOuts,
        [t('statistics.default.hardBounces')]: this._data.hardBounces
      };
    }

    getChartOptions() {
      const isMobileView = this._isMobileView;
      const barColor = this.$el.css('--chart-blue-color');
      const metrics = this.getMetrics();

      return {
        type: 'bar',
        data: {
          labels: Object.keys(metrics),
          datasets: [{
            data: Object.values(metrics),
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
            datalabels: {
              offset: isMobileView ? 0 : -5 // make labels closer to the bar
            }
          }
        }
      };
    }

    needsRedrawOnMobile() {
      return true;
    }

    getStatisticsUrl() {
      return AGN.url('/dashboard/statistics.action');
    }

  }

  AGN.Lib.Dashboard.StatisticsTile = StatisticsTile;
})();
