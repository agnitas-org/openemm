(function () {
    var MessageStatisticsService = {
        data: {
            chartsColors: ["#26b98a"],
            statChartData: {
                columnTitles: [],
                c3data: {
                    bindto: '#barchart-message-stats',
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
                                    return MessageStatisticsService.data.statChartData.columnTitles[columnIndex];
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
                                return MessageStatisticsService.data.statChartData.columnTitles[index];
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
                build: function (data) {
                    this.columnTitles.push(data.shift());
                    data.unshift('values');
                    this.c3data.data.columns = [data];
                    return this.c3data;
                }
            }
        },

        updateChart: function ( url) {

            $.ajax({
                type: "GET",
                url: url,
                success: function (data) {
                    c3.generate(MessageStatisticsService.data.statChartData.build(data));
                    AGN.Lib.CoreInitializer.run('equalizer');
                }
            });
        }

    };

    AGN.Lib.MessageStatisticsService = MessageStatisticsService;

    AGN.Lib.DomInitializer.new('message-stat', function ($e) {
        var config = AGN.Lib.Helpers.objFromString($e.data('config'));
        if (config && config.url) {
            MessageStatisticsService.updateChart(config.url);
        }
    });
})();