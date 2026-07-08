AGN.Lib.Controller.new('monthly-overview-stat', function () {

  this.addDomInitializer('monthly-overview-stat', function () {
    displayData();
  });

  this.addAction({click: 'refresh-data'}, function () {
    displayData();
  });

  function displayData() {
    const $container = $('#stat-content');
    $container.addClass('hidden');

    getData().then(resp => {
      assignColors(resp);

      $container.html(AGN.Lib.Template.text('monthly-overview-content', {
        ... resp,
        type: $('#stat-type').val()
      }));

      renderChart(resp.amountRows);

      AGN.runAll($container);
      $container.removeClass('hidden');
    });
  }

  function getData() {
    const $loader = $('#stat-loader');
    $loader.removeClass('hidden');

    return new Promise((resolve, reject) => {
      $.get(AGN.url('/statistics/monthly/data.action'), $('#stat-form').serialize())
        .done(resolve)
        .fail(reject)
        .always(() => $loader.addClass('hidden'));
    });
  }

  function assignColors(data) {
    const statColors = AGN.Lib.Helpers.getStatColorsHexValues();
    const colorMap = new Map();

    const uniqueIds = new Set([
      ...data.detailRows.map(row => row.id),
      ...data.amountRows.map(row => row.id),
    ]);

    uniqueIds.values().forEach((id, idx) => {
      colorMap.set(id, statColors[idx % statColors.length]);
    });

    data.detailRows.forEach(row => row.colorHex = colorMap.get(row.id));
    data.amountRows.forEach(row => row.colorHex = colorMap.get(row.id));
  }

  function renderChart(rows) {
    if (!rows.length) {
      return;
    }

    new Chart(($('#amount-chart'))[0].getContext('2d'), {
      type: 'bar',
      data: {
        labels: rows.map(r => r.shortname),
        datasets: [{
          data: rows.map(r => r.amount),
          backgroundColor: rows.map(r => r.colorHex),
          minBarLength: 2
        }]
      },
      options: {
        indexAxis: 'x',
        layout: {
          padding: {
            top: 20,
            right: 0
          }
        },
        scales: {
          x: {
            ticks: {
              autoSkip: false,
              maxRotation: 90,
              minRotation: 0
            }
          },
          y: {
            display: false
          }
        }
      }
    });
  }

});
