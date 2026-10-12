(() => {

  class DoughnutChart extends AGN.Lib.StatsChart {
    constructor($el, data) {
      const defaultDatasetConfig = {
        backgroundColor: AGN.Lib.Helpers.getStatColorsHexValues(),
        barThickness: Number.parseFloat(getComputedStyle(document.documentElement).fontSize),
        borderWidth: 0,
      };
      super($el, {
        ...data,
        datasets: data?.datasets?.map(dataset => ({
          ...defaultDatasetConfig,
          ...dataset,
        })) || []
      });
    }

    render() {
      new Chart(this.$el[0].getContext('2d'), {
        type: 'agnDoughnut',
        data: this.data
      });
    }
  }

  AGN.Lib.DoughnutChart = DoughnutChart;
})();
