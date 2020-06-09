(function(){
  var DashboardStatisticsService;
  DashboardStatisticsService = {
    data: {
      chartsColors: [
        "#0071b9", "#009999", "#92d050", "#41641a", "#ffc000"
      ],

      statChartData: {
        columnTitles: [],
        c3data: {
          bindto: '.js-stat-chart',
          data: {
            columns: [],
            type: 'bar',
            labels: true
          },
          axis: {
            x: {
              tick: {
                centered: true,
                fit: true,
                format: function (columnIndex) {
                  return DashboardStatisticsService.data.statChartData.columnTitles[columnIndex];
                }
              }
            }
          },
          legend: {
            show: false
          },
          tooltip: {
            format: {
              title: function () {
                return null;  // Hide title
              },
              name: function (name, ratio, id, index) {
                return DashboardStatisticsService.data.statChartData.columnTitles[index];
              },
              value: function (value, ratio, id, index) {
                return value;
              }
            }
          },
          padding: {
            right: 25
          }
        },
        build: function (titles, values) {
          this.columnTitles = titles;

          var row = values.slice(0);
          row.unshift('values');
          this.c3data.data.columns = [row];

          return this.c3data;
        }
      },

      clickChartData: {
        c3data: {
          bindto: '.js-click-chart',
          data: {
            rows: [],
            type: 'donut'
          },
          size: {
            height: 150
          },
          donut: {
            title: '',
            label: {
              show: false
            },
            width: 15
          },
          legend: {
            position: 'right'
          },
          tooltip: {
            show: false
          }
        },
        build: function (title, titles, values) {
          this.c3data.donut.title = title;
          this.c3data.data.rows = [titles, values];
          var colors = {};
          for (var i = 0; i < titles.length; i++) {
            colors[titles[i]] = DashboardStatisticsService.data.chartsColors[i];
          }
          this.c3data.data.colors = colors;
          return this.c3data;
        }
      },

      viewChartData: {
        c3data: {
          bindto: '.js-view-chart',
          data: {
            rows: [],
            type: 'donut'
          },
          size: {
            height: 150
          },
          donut: {
            title: '',
            label: {
              show: false
            },
            width: 15
          },
          legend: {
            position: 'right'
          },
          tooltip: {
            show: false
          }
        },
        build: function (title, titles, values) {
          this.c3data.donut.title = title;
          this.c3data.data.rows = [titles, values];
          var colors = {};
          for (var i = 0; i < titles.length; i++) {
            colors[titles[i]] = DashboardStatisticsService.data.chartsColors[i];
          }
          this.c3data.data.colors = colors;
          return this.c3data;
        }
      }
    },

    updateCharts: function(mailingId) {
      jQuery.ajax({
        type: "POST",
        url: AGN.url("/dashboard/statistics.action"),
        data: {
          mailingId: mailingId
        },
        success: function(data) {
          var rowNames;
          var rowValues;
          var sumValue;

          data = _.merge({
            'common': [['no Data', '0']], // note: translate
            'clickers': [['no Data', '0']], // note: translate
            'clickersPercent': [0], // note: translate
            'openers': [['no Data', '0']], // note: translate
            'openersPercent': [0] // note: translate
          }, data);

          rowNames = [];
          rowValues = [];
          for (var i = 0; i < data['common'].length; i++) {
            var value = parseInt(data['common'][i][1]);
            rowNames.push(data['common'][i][0]);
            rowValues.push(value);
          }

          c3.generate(DashboardStatisticsService.data.statChartData.build(rowNames, rowValues));

          rowNames = [];
          rowValues = [];
          sumValue = 0;
          for (var i = 0; i < data['clickers'].length; i++) {
            var value = DashboardStatisticsService.roundTo(data['clickers'][i][1] * 100, 1);
            rowNames.push(data['clickers'][i][0]);
            rowValues.push(value);
            sumValue += value;
          }

          if (!isNaN(sumValue) && sumValue > 0) {
            for (var i = 0; i < rowValues.length; i++) {
              rowNames[i] += ' ' + DashboardStatisticsService.roundTo(100 * rowValues[i]/sumValue, 1) + '%';
            }
          }

          var clickChartTitle = DashboardStatisticsService.roundTo(data['clickersPercent'][0] * 100, 1) + '%*';
          c3.generate(DashboardStatisticsService.data.clickChartData.build(clickChartTitle, rowNames, rowValues));

          rowNames = [];
          rowValues = [];
          sumValue = 0;
          for (var i = 0; i < data['openers'].length; i++) {
            var value = DashboardStatisticsService.roundTo(data['openers'][i][1] * 100, 1);
            rowNames.push(data['openers'][i][0]);
            rowValues.push(value);
            sumValue += value;
          }

          if (!isNaN(sumValue) && sumValue > 0) {
            for (var i = 0; i < rowValues.length; i++) {
              rowNames[i] += ' ' + DashboardStatisticsService.roundTo(100 * rowValues[i]/sumValue, 1) + '%';
            }
          }

          var viewChartTitle = DashboardStatisticsService.roundTo(data['openersPercent'][0] * 100, 1) + '%*';
          c3.generate(DashboardStatisticsService.data.viewChartData.build(viewChartTitle, rowNames, rowValues));

          AGN.Lib.CoreInitializer.run('equalizer');
        }
      });
    },

    roundTo: function(number, fractionalDigits) {
      return (parseFloat(number).toFixed(fractionalDigits)) * 1.0;
    }
  };

  AGN.Lib.DashboardStatisticsService = DashboardStatisticsService;
})();
