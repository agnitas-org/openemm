(function(){
    var GLobalNotificationStatusStatService = {
        data: {
            chartsColors: ["#0071b9", "#92D050", "#41641A"],
            statusChartData: {
                columnTitles: [],
                c3data: {
                    bindto: '#notificationGlobalStatusStat',
                    data: {
                        columns: [],
                        type: 'bar',
                        labels: true,
                        color: function(color, d) {
                            return GLobalNotificationStatusStatService.data.chartsColors[d.index];
                        }
                    },
                    axis: {
                        x: {
                            tick: {
                                centered: true,
                                fit: true,
                                format: function (columnIndex) {
                                    return GLobalNotificationStatusStatService.data.statusChartData.columnTitles[columnIndex];
                                }
                            }
                        },
                        y: {
                            show: false
                        }
                    },
                    legend: {
                        show: false
                    },
                    tooltip: {
                        format: {
                            title: function () {
                                return null;
                            },
                            name: function (name, ratio, id, index) {
                                return GLobalNotificationStatusStatService.data.statusChartData.columnTitles[index];
                            },
                            value: function (value, ratio, id, index) {
                                return value;
                            }
                        }
                    },
                    padding: {
                        right: 25,
                        left: 25
                    }
                },

                build: function (titles, values) {
                    this.columnTitles = titles;

                    var row = values.slice(0);
                    row.unshift('values');
                    this.c3data.data.columns = [row];
                    return this.c3data;
                }
            }
        },
        updateChart: function(url) {
            $.ajax({
                type: "POST",
                url: url,
                success: function (data) {
                    var titles = [];
                    var values = [];
                    for(key in data) {
                        titles.push(key);
                        values.push(data[key]);
                    }

                    c3.generate(GLobalNotificationStatusStatService.data.statusChartData.build(titles, values));
                    AGN.Initializers.Equalizer();
                }
            });
        }
    };

    AGN.Lib.GLobalNotificationStatusStatService = GLobalNotificationStatusStatService;

    AGN.Lib.DomInitializer.new('notification-global-status-stat', function($elem) {
        var config = AGN.Lib.Helpers.objFromString($elem.data('config'));
        if (config && config.url) {
            AGN.Lib.GLobalNotificationStatusStatService.updateChart(config.url);
        }
    });
})();
