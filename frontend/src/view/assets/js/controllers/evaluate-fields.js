AGN.Lib.Controller.new('evaluate-fields', function () {

  const Helpers = AGN.Lib.Helpers;

  this.addDomInitializer('evaluate-fields', function () {
    const $chart = $('#evaluate-fields-chart');
    const stat = this.config.stat;
    const colors = Helpers.getStatColorsHexValues();

    new Chart($chart[0].getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: stat.map(({ value }) => value),
        datasets: [{
          data: stat.map(({ count }) => count),
          backgroundColor: colors,
          borderWidth: 0,
        }]
      }
    });
  });
});
