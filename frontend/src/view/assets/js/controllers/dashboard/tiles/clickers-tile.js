(() => {

  class ClickersTile extends AGN.Lib.Dashboard.BaseMailingDeviceStatisticsTile {

    static ID = 'clickers';

    drawChart() {
      const totalValue = this.getTotalValue();
      const $canvas = this.findCanvas$();

      $canvas.toggleClass('hidden', totalValue === 0);
      $canvas.next().toggleClass('hidden', totalValue > 0);

      if (totalValue > 0) {
        super.drawChart();
      }
    }

    getStatisticsUrl() {
      return AGN.url('/dashboard/statistic/clickers.action');
    }

  }

  AGN.Lib.Dashboard.ClickersTile = ClickersTile;
})();