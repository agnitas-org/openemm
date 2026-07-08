AGN.Lib.Controller.new('mailings-comparison-stat-view', function () {

  const BAR_HEIGHT = 20;

  this.addDomInitializer('mailings-comparison-stat-view', function () {
    const $loader = $('#stat-loader');

    $.ajax({
      url: AGN.url('/statistics/mailing/comparison/data.action'),
      data: {mailingIds: this.config.ids},
      method: 'GET',
      beforeSend: () => $loader.removeClass('hidden')
    })
      .done(mailings => {
        renderMailings(mailings, this.config);
        $('#mailings-data, #statistics-footer-info').removeClass('hidden');
      })
      .always(() => $loader.addClass('hidden'));
  });

  function renderMailings(mailings, config) {
    const statColors = AGN.Lib.Helpers.getStatColorsHexValues();
    mailings.forEach((m, idx) => m.colorHex = statColors[idx % statColors.length]);

    $('[data-table-cfg]').each(function () {
      const $table = $(this);
      const tableConfig = $($table.data('table-cfg')).json();

      new AGN.Lib.Table($table, tableConfig.columns, mailings, tableConfig.options);
    });

    config.barCharts.forEach(({label, property}) =>
      renderBarChart(mailings, label, property));
  }

  function renderBarChart(mailings, label, property) {
    mailings = mailings.filter(m => m[property] != null);
    if (!mailings.length) {
      return;
    }

    const height = `${mailings.length * BAR_HEIGHT}px`;
    const $el = AGN.Lib.Template.dom('bar-chart', {label, property, height});
    $('#charts-container').append($el);
    const $canvas = $el.find('canvas');

    new Chart($canvas[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: mailings.map(m => m.name),
        datasets: [{
          data: mailings.map(m => {
            const value = m[property];
            return isNumber(value) ? value : value.value;
          }),
          backgroundColor: mailings.map(m => m.colorHex),
          categoryPercentage: 1,
          minBarLength: 2
        }]
      },
      options: {
        indexAxis: 'y',
        layout: {
          padding: {
            top: 0,
            right: 100
          }
        },
        scales: {
          x: { display: false },
          y: { display: false }
        },
        plugins: {
          datalabels: {
            offset: 0, // make labels closer to the bar
            formatter: (value, context) => {
              const propertyValue = mailings[context.dataIndex][property];
              if (isNumber(propertyValue)) {
                return AGN.formatNumber(propertyValue);
              }

              return AGN.formatNumber(value) + ` (${propertyValue.rate.toFixed(1)} %)`;
            }
          }
        }
      }
    });
  }

  function isNumber(value) {
    return AGN.Lib.Helpers.isNumber(value);
  }

});
