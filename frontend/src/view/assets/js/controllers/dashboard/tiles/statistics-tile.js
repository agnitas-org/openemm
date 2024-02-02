class StatisticsTile extends DraggableTile {

  constructor() {
    super();
    this.#addUiHandlers();

    this._chart = null;
    this._isMobileView = false;
    this._chartData = {names: [], values: [], mailingId: 0};
  }

  #addUiHandlers() {
    $(window).on("displayTypeChanged", (e, isMobileView) => {
      this._isMobileView = isMobileView;

      if (this._chart) {
        this.updateChart(true);
      }
    });
  }

  displayOnScreen($place) {
    super.displayOnScreen($place);
    if (this.#$findCanvas().exists()) {
      this.updateChart();
    }
  }

  async updateChart(forceUpdate = false) {
    const mailingIdBefore = this._chartData.mailingId;
    this._chartData.mailingId = $('#mailing-statistics-item').val();

    const mailingChanged = this._chartData.mailingId !== mailingIdBefore
    if (!mailingChanged && !forceUpdate) {
      return;
    }

    if (mailingChanged) {
      const chartData = await this.#fetchChartData();
      this._chartData.names = chartData.names;
      this._chartData.values = chartData.values;
    }

    this.#drawChart();
  }

  #fetchChartData() {
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

  #drawChart() {
    if (this._chart) {
      this._chart.destroy();
    } else {
      Chart.register(ChartDataLabels); // register plugin of chart js
    }

    const ctx = this.#$findCanvas()[0].getContext('2d');
    this._chart = new Chart(ctx, this.#getChartOptions());
  }

  #$findCanvas() {
    return this.$el.find('#statistics-chart');
  }

  #getChartOptions() {
    const isMobileView = this._isMobileView;

    const barColor = this.$el.css('--chart-bar-color');
    const labelColor = this.$el.css('--chart-label-color');
    const fontSize = parseInt(this.$el.css('--chart-font-size'));
    const fontFamily = this.$el.css('--chart-font-family');

    return {
      type: 'bar',
      data: {
        labels: this._chartData.names,
        datasets: [{
          data: this._chartData.values,
          backgroundColor: barColor,
          borderWidth: 0,
          barPercentage: 1,
          categoryPercentage: isMobileView ? 0.7 : 0.9
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
        maintainAspectRatio: false, // take all height
        scales: {
          x: {
            display: !isMobileView,
            border: {
              display: false,
            },
            grid: {
              display: false // Hide the vertical grid lines
            },
            ticks: {
              autoSkip: false,
              maxRotation: 90,
              minRotation: 0,
              color: labelColor,
              font: {
                size: fontSize,
                family: fontFamily
              }
            }
          },
          y: {
            display: isMobileView,
            border: {
              display: false,
            },
            grid: {
              display: false // Hide the vertical grid lines
            },
            ticks: {
              color: labelColor,
              mirror: true,
              labelOffset: -37.5,
              font: {
                size: fontSize,
                family: fontFamily
              }
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
            color: labelColor,
            offset: isMobileView ? 0 : -5, // make labels closer to the bar
            font: {
              size: fontSize,
              lineHeight: '132%',
              family: fontFamily
            }
          }
        }
      }
    };
  }

  get id() {
    return DraggableTile.def.TILE.ID.STATISTICS;
  }
}
