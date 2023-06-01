(function(){
    var NotificationStatService = {
        data: {
            chartsColors: ["#0071b9"],
            statChartData: {
                columnTitles: [],
                c3data: {
                    bindto: '#barchart-notification-stats',
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
                                    return NotificationStatService.data.statChartData.columnTitles[columnIndex];
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
                                return null;
                            },
                            name: function (name, ratio, id, index) {
                                return NotificationStatService.data.statChartData.columnTitles[index];
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

            progressChartData: {
                c3data: {
                    bindto: '#progresschart-notification-stats',
                    data: {
                        x: 'x',
                        columns: []
                    },
                    axis: {
                        x: {
                            type: 'category',
                            tick: {
                                fit: false
                            }
                        }
                    },
                    zoom: {
                        enabled: true,
                        rescale: true
                    },
                    subchart: {
                        show: true
                    },
                    padding: {
                        right: 25
                    }
                },
                build: function (times, data1, data2) {
                    this.c3data.data.columns = [times, data1, data2];
                    return this.c3data;
                }
            }
        },

        updateCharts: function(pushId, url) {
            $.ajax({
                type: "GET",
                url: url,
                data: {
                    pushID: pushId
                },
                success: function (data) {
                    generateCommonStatisticChart(data['common']);
                    generateProgressChart(data['viewsclicks']);

                    AGN.Lib.CoreInitializer.run('equalizer');
                }
            });
        }
    };

    AGN.Lib.NotificationStatService = NotificationStatService;

    AGN.Lib.DomInitializer.new('notification-stats', function($elem) {
        var config = AGN.Lib.Helpers.objFromString($elem.data('config'));
        if (config && config.pushId && config.url) {
            AGN.Lib.NotificationStatService.updateCharts(config.pushId, config.url);
        }
    });

    function generateCommonStatisticChart(jsonCommonStatisticData) {
        var rowNames = [];
        var rowValues = [];

        for(var key in jsonCommonStatisticData) {
            if (jsonCommonStatisticData.hasOwnProperty(key)){
                rowNames.push(key);
                rowValues.push(jsonCommonStatisticData[key]);
            }
        }

        c3.generate(NotificationStatService.data.statChartData.build(rowNames, rowValues));
    }

    function generateProgressChart(jsonProgressData) {
        var times = ['x'];
        var data1 = [];
        var data2 = [];
        for(objIndex in jsonProgressData) {
            var jsonProgressItem = jsonProgressData[objIndex];
            for (var jsonProgressObjectKey in jsonProgressItem) {
                if (jsonProgressItem.hasOwnProperty(jsonProgressObjectKey)){
                    //set data row name
                    if (objIndex == 0 && jsonProgressObjectKey != 'time') {
                        if (data1.length == 0) {
                            data1.push(jsonProgressObjectKey);
                        } else if (data2.length == 0) {
                            data2.push(jsonProgressObjectKey);
                        }
                    }

                    //set row value
                    if (jsonProgressObjectKey == 'time') {
                        times.push(jsonProgressItem[jsonProgressObjectKey]);
                    } else if (data1[0] == jsonProgressObjectKey) {
                        data1.push(jsonProgressItem[jsonProgressObjectKey]);
                    } else if (data2[0] == jsonProgressObjectKey) {
                        data2.push(jsonProgressItem[jsonProgressObjectKey]);
                    }
                }
            }

        }
        c3.generate(NotificationStatService.data.progressChartData.build(times, data1, data2));
    }
})();