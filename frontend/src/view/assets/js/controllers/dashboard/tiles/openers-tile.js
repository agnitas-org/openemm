(() => {

  class OpenersTile extends AGN.Lib.Dashboard.BaseMailingDeviceStatisticsTile {
  
    static ID = 'openers';

    extractStatisticsData(fetchedData) {
      const names = [];
      const values = [];
      const data = fetchedData['openers'];

      for (let i = 0; i < data.length; i++) {
        const value = parseFloat(data[i][1]);
        names.push(data[i][0]);
        values.push(value);
      }

      return _.extend(super.extractStatisticsData(fetchedData), {names, values});
    }

    extractPercent(fetchedData) {
      return fetchedData['openersPercent'][0];
    }

    drawChart() {
      const totalValue = AGN.Lib.ChartUtils.getTotal(this._chartData.values);
      const $canvas = this.findCanvas$().parent();

      $canvas.toggleClass('hidden', totalValue === 0);
      $canvas.next().toggleClass('hidden', totalValue === 0);
      $canvas.next().next().toggleClass('hidden', totalValue > 0);

      if (totalValue > 0) {
        super.drawChart();
        this.#updateOpenersPercent(this._chartData.percent);
      }
    }

    #updateOpenersPercent(percent) {
      percent = this.#roundTo(percent * 100, 1);
      this.$el.find('#statistics-openers-percent').text(`${percent}%`);
    }

    #roundTo(number, fractionalDigits) {
      return (parseFloat(number).toFixed(fractionalDigits)) * 1.0;
    }
  }

  AGN.Lib.Dashboard.OpenersTile = OpenersTile;
})();
