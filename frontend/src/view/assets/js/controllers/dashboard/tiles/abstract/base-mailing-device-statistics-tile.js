class BaseMailingDeviceStatisticsTile extends BaseMailingStatisticsTile {

  constructor() {
    if (new.target === BaseMailingDeviceStatisticsTile) {
      throw new TypeError("Cannot construct Abstract BaseMailingDeviceStatisticsTile instances directly!");
    }

    super();
  }

  fetchChartData() {
    const mailingId = this._chartData.mailingId;
    return new Promise((resolve, reject) => {
      $.ajax({
        type: "GET",
        url: AGN.url('/dashboard/statistics.action'),
        data: {
          mailingId: mailingId
        },
        success: resp => {
          const labels = [];
          const values = [];

          const data = this.extractStatisticsData(resp);

          for (let i = 0; i < data.length; i++) {
            const value = this.roundTo(data[i][1] * 100, 1);
            labels.push(data[i][0]);
            values.push(value);
          }

          resolve({
            names: labels,
            values: values,
            percent: this.extractPercent(resp)
          });
        },
        error: error => reject(error)
      });
    });
  }

  extractStatisticsData(fetchedData) {
    throw new Error("Function not implemented for statistics tile!");
  }

  extractPercent(fetchedData) {
    throw new Error("Function not implemented for statistics tile!");
  }

  initChartData(fetchedData) {
    super.initChartData(fetchedData);
    this._chartData.percent = fetchedData.percent;
  }

  getChartOptions() {
    if (!this._chartData.colors) {
      this._chartData.colors = [
        this.$el.css('--chart-very-dark-blue-color'),
        this.$el.css('--chart-dark-blue-color'),
        this.$el.css('--chart-blue-color'),
        this.$el.css('--chart-light-blue-color'),
        this.$el.css('--chart-cyan-color')
      ];
    }

    return {
      type: 'agnDoughnut',
      data: {
        labels: this._chartData.names,
        datasets: [{
          data: this._chartData.values,
          backgroundColor: this._chartData.colors,
          borderWidth: 0
        }]
      }
    };
  }

  roundTo(number, fractionalDigits) {
    return (parseFloat(number).toFixed(fractionalDigits)) * 1.0;
  }
}
