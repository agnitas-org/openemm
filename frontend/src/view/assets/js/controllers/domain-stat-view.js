AGN.Lib.Controller.new('domain-statistics-view', function () {

  this.addDomInitializer('domain-statistics-view', function () {
    const $chart = $('#domain-stat-chart');
    const { statistics } = this.config;
    const colors = AGN.Lib.Helpers.getStatColorsHexValues();

    new Chart($chart[0].getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: statistics.map(({ domainName }) => domainName),
        datasets: [{
          data: statistics.map(({ count }) => count),
          backgroundColor: colors,
          borderWidth: 0,
        }]
      }
    });
  });

});
