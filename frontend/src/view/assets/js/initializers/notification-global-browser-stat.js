(function(){
    var GLobalNotificationBrowserStatService = {
        data: {
            chartsColors: [
                "#0071b9", "#009999", "#92d050", "#41641a", "#ffc000"
            ],
            browserChartData: {
                c3data: {
                    bindto: '#notificationGlobalBrowserStat',
                    data: {
                        rows: [],
                        type: 'donut'
                    },
                    donut: {
                        label: {
                            show: true
                        },
                    },
                    legend: {
                        position: 'right'
                    },
                    tooltip: {
                        show: false
                    }
                },

                build: function (titles, values) {
                    this.c3data.data.rows = [titles, values];
                    var colors = {};
                    for (var i = 0; i < titles.length; i++) {
                        colors[titles[i]] = GLobalNotificationBrowserStatService.data.chartsColors[i];
                    }
                    this.c3data.data.colors = colors;

                    return this.c3data;
                }
            }
        },

        roundTo: function(number, fractionalDigits) {
            return (parseFloat(number).toFixed(fractionalDigits)) * 1.0;
        },

        updateChart: function(url) {
            $.ajax({
                type: "POST",
                url: url,
                success: function (data) {
                    if (data && (data.warning && data.warning.length > 0)) {
                        AGN.Lib.JsonMessages(data, true);
                    } else {
                        var titles = [];
                        var values = [];
                        var sumValue = 0;
                        for(key in data) {
                            var value = GLobalNotificationBrowserStatService.roundTo(data[key] * 100, 1);
                            titles.push(key);
                            values.push(value);
                            sumValue += value;
                        }

                        if (!isNaN(sumValue) && sumValue > 0) {
                            for (var i = 0; i < values.length; i++) {
                                titles[i] += ' ' + GLobalNotificationBrowserStatService.roundTo(100 * values[i]/sumValue, 1) + '%';
                            }
                        }

                        c3.generate(GLobalNotificationBrowserStatService.data.browserChartData.build(titles, values));
                        AGN.Lib.CoreInitializer.run('equalizer');
                    }
                }
            });
        }
    };

    AGN.Lib.GLobalNotificationBrowserStatService = GLobalNotificationBrowserStatService;

    AGN.Lib.DomInitializer.new('notification-global-browser-stat', function($elem) {
        var config = AGN.Lib.Helpers.objFromString($elem.data('config'));
        if (config && config.url) {
            AGN.Lib.GLobalNotificationBrowserStatService.updateChart(config.url)
        }
    });
})();
