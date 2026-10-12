(() => {

  class OpenersTile extends AGN.Lib.Dashboard.BaseMailingDeviceStatisticsTile {
  
    static ID = 'openers';

    getStatisticsUrl() {
      return AGN.url('/dashboard/statistic/openers.action');
    }

    drawChart() {
      const totalValue = this.getTotalValue();
      const $canvas = this.findCanvas$().parent();

      $canvas.toggleClass('hidden', totalValue === 0);
      $canvas.next().toggleClass('hidden', totalValue === 0);
      $canvas.next().next().toggleClass('hidden', totalValue > 0);

      if (totalValue > 0) {
        super.drawChart();
        this.#updateOpenersPercent(this._data.percent);
      }
    }

    #updateOpenersPercent(percent) {
      this.$el.find('#statistics-openers-percent').text(`${percent.toFixed(1)}%`);
    }

  }

  AGN.Lib.Dashboard.OpenersTile = OpenersTile;
})();
