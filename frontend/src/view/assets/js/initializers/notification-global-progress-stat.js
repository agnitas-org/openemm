(function(){
    var GLobalNotificationProgressStatService = {
        data: {
            chartColors: ["#0071b9", "#006600", "#b30000"],
            globalProgressChartData: {
                columnTitles: [],
                c3data: {
                    bindto: '#notificationGlobalProgressStat',
                    data: {
                        x: 'x',
                        columns: []
                    },
                    axis: {
                        x: {
                            type: 'category',
                            tick: {
                                centered: true,
                                fit: false
                            }
                        }
                    },
                    padding: {
                        right: 25
                    }
                },
                build: function(times, data1, data2, data3) {
                    this.c3data.data.columns = [times, data1, data2, data3];

                    var colors = {};
                    colors[data1[0]] = GLobalNotificationProgressStatService.data.chartColors[0];
                    colors[data2[0]] = GLobalNotificationProgressStatService.data.chartColors[1];
                    colors[data3[0]] = GLobalNotificationProgressStatService.data.chartColors[2];
                    this.c3data.data.colors = colors;
                    return this.c3data;
                }
            }
        },
        updateChart: function(url) {
            var time = ['x'];
            var data1 = [];
            var data2 = [];
            var data3 = [];

            var dateMode = $('#selectMode').val();

            var selectMonth = $('#selectMonth').val();
            var selectYear = $('#selectYear').val();

            var startDayFromDatePicker = $('#startDay').val();
            var endDayFromDatePicker = $('#endDay').val();

            $.ajax({
                type: "POST",
                url: url,
                data: {
                    dateMode: dateMode,
                    selectMonth: selectMonth,
                    selectYear: selectYear,
                    startDay: startDayFromDatePicker,
                    endDay: endDayFromDatePicker
                },
                success: function (data) {
                    if (data && data.warning && data.warning.length > 0) {
                        AGN.Lib.JsonMessages(data, true);
                    } else {
                        for (objIndex in data) {
                            var jsonItem = data[objIndex];
                            for (objKey in jsonItem) {
                                //set row name
                                if (objIndex == 0 && objKey != 'time') {
                                    if (data1.length == 0) {
                                        data1.push(objKey);
                                    } else if (data2.length == 0) {
                                        data2.push(objKey);
                                    } else if (data3.length == 0) {
                                        data3.push(objKey);
                                    }
                                }

                                //set row value
                                var value = jsonItem[objKey];
                                if (objKey == 'time') {
                                    time.push(value);
                                } else if (objKey == data1[0]) {
                                    data1.push(value);
                                } else if (objKey == data2[0]) {
                                    data2.push(value);
                                } else if (objKey == data3[0]) {
                                    data3.push(value);
                                }
                            }
                        }

                        c3.generate(GLobalNotificationProgressStatService.data.globalProgressChartData.build(time, data1, data2, data3));
                        AGN.Lib.CoreInitializer.run('equalizer');
                    }
                }
            });

        }
    };

    AGN.Lib.GLobalNotificationProgressStatService = GLobalNotificationProgressStatService;

    AGN.Lib.DomInitializer.new('notification-global-progress-stat', function($elem) {
        var config = AGN.Lib.Helpers.objFromString($elem.data('config'));
        if (config && config.url) {
            AGN.Lib.GLobalNotificationProgressStatService.updateChart(config.url)
        }
    });

})();
