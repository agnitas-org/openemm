(() => {

  class LineStatsChart extends AGN.Lib.StatsChart {
    constructor($el, data) {
      super($el, data);
    }

    prepare() {
      this.data.datasets.forEach((ds, i) => ds.borderColor = this.lineColors[i]);
    }

    get lineColors() {
      if (!this._lineColors) {
        this._lineColors = AGN.Lib.Helpers.getStatColorsHexValues()
      }
      return this._lineColors;
    }

    render() {
      new Chart(this.$el[0].getContext('2d'), {
        type: 'line',
        data: this.data,
        options: {
          responsive: true,
          maintainAspectRatio: false,
          layout: {
            padding: {top: 15, right: 15, bottom: 15, left: 15}
          },
          plugins: {
            legend: {display: false},
          },
          scales: {
            x: {
              offset: true,
              border: {
                display: true,
                color: this.axisColor,
              },
              grid: {
                display: true,
                drawOnChartArea: false,
                drawTicks: true,
                tickLength: 5,
                color: this.axisColor,
              },
              ticks: {
                font: {size: 12},
              }
            },
            y: {
              beginAtZero: true,
              border: {
                display: true,
                color: this.axisColor,
              },
              grid: {
                display: true,
                drawOnChartArea: false,
                drawTicks: true,
                tickLength: 5,
                color: this.axisColor,
              },
              ticks: {
                font: {size: 12},
              }
            }
          },
          elements: {
            line: {
              borderWidth: 2,
            },
            point: {
              radius: 5,
              hoverRadius: 10,
              backgroundColor: (ctx) => ctx.dataset.borderColor
            }
          }
        }
      });
    }
  }
  AGN.Lib.LineStatsChart = LineStatsChart;
})();
