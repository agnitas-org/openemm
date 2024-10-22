(() => {

  class BaseMailingDeviceStatisticsTile extends AGN.Lib.Dashboard.BaseMailingStatisticsTile {

    constructor() {
      if (new.target === BaseMailingDeviceStatisticsTile) {
        throw new TypeError("Cannot construct Abstract BaseMailingDeviceStatisticsTile instances directly!");
      }

      super();
    }

    extractStatisticsData(fetchedData) {
      return {percent: this.extractPercent(fetchedData)};
    }

    extractPercent(fetchedData) {
      // No implementation
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
          this.$el.css('--chart-light-cyan-color')
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
  }

  AGN.Lib.Dashboard.BaseMailingDeviceStatisticsTile = BaseMailingDeviceStatisticsTile;
})();
