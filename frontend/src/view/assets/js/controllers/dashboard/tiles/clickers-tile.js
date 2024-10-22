(() => {

  class ClickersTile extends AGN.Lib.Dashboard.BaseMailingDeviceStatisticsTile {

    static ID = 'clickers';

    extractStatisticsData(fetchedData) {
      const names = [];
      const values = [];
      const data = fetchedData['clickers'];

      for (let i = 0; i < data.length; i++) {
        const value = parseFloat(data[i][1]);
        names.push(data[i][0]);
        values.push(value);
      }

      return _.extend(super.extractStatisticsData(fetchedData), {names, values});
    }

    drawChart() {
      const totalValue = AGN.Lib.ChartUtils.getTotal(this._chartData.values);
      const $canvas = this.findCanvas$();

      $canvas.toggleClass('hidden', totalValue === 0);
      $canvas.next().toggleClass('hidden', totalValue > 0);

      if (totalValue > 0) {
        super.drawChart();
      }
    }
  }

  AGN.Lib.Dashboard.ClickersTile = ClickersTile;
})();