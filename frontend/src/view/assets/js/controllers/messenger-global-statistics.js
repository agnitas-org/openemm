(function () {
    var GlobalMessengerTypeStatistics = {
        data: {
            chartsColors: [
                "#0071b9", "#009999", "#92d050", "#41641a", "#ffc000"
            ],
            browserChartData: {
                c3data: {
                    bindto: '#typeStatistics',
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
                        colors[titles[i]] = GlobalMessengerTypeStatistics.data.chartsColors[i];
                    }
                    this.c3data.data.colors = colors;

                    return this.c3data;
                }
            }
        },

        roundTo: function (number, fractionalDigits) {
            return (parseFloat(number).toFixed(fractionalDigits)) * 1.0;
        },

        updateChart: function (url) {
            $.ajax({
                type: "GET",
                url: url,
                success: function (data) {
                    var titles = [];
                    var values = [];
                    var sumValue = 0;
                    for (var key in data) {
                        var value = GlobalMessengerTypeStatistics.roundTo(data[key] * 100, 1);
                        titles.push(key);
                        values.push(value);
                        sumValue += value;
                    }

                    if (!isNaN(sumValue) && sumValue > 0) {
                        for (var i = 0; i < values.length; i++) {
                            titles[i] += ' ' + GlobalMessengerTypeStatistics.roundTo(100 * values[i] / sumValue, 1) + '%';
                        }
                    }

                    c3.generate(GlobalMessengerTypeStatistics.data.browserChartData.build(titles, values));
                    AGN.Lib.CoreInitializer.run('equalizer');
                }
            });
        }
    };

    var GlobalMessengerStatusStatistics = {
        data: {
            chartsColors: ["#0071b9", "#92D050"],
            statusChartData: {
                columnTitles: [],
                c3data: {
                    bindto: '#statusStatistics',
                    data: {
                        columns: [],
                        type: 'bar',
                        labels: true,
                        color: function (color, d) {
                            return GlobalMessengerStatusStatistics.data.chartsColors[d.index];
                        }
                    },
                    axis: {
                        x: {
                            tick: {
                                centered: true,
                                fit: true,
                                format: function (columnIndex) {
                                    return GlobalMessengerStatusStatistics.data.statusChartData.columnTitles[columnIndex];
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
                                return GlobalMessengerStatusStatistics.data.statusChartData.columnTitles[index];
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
        updateChart: function (url) {
            $.ajax({
                type: "GET",
                url: url,
                success: function (data) {
                    var titles = [];
                    var values = [];
                    for (var key in data) {
                        titles.push(key);
                        values.push(data[key]);
                    }

                    c3.generate(GlobalMessengerStatusStatistics.data.statusChartData.build(titles, values));
                    AGN.Lib.CoreInitializer.run('equalizer');
                }
            });
        }
    };

    var GLobalMessengerProgressStatistics = {
        data: {
            chartColors: ["#0071b9", "#006600", "#b30000"],
            globalProgressChartData: {
                columnTitles: [],
                c3data: {
                    bindto: '#progressStatistics',
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
                build: function (times, data1, data2, data3) {
                    this.c3data.data.columns = [times, data1, data2, data3];

                    var colors = {};
                    colors[data1[0]] = GLobalMessengerProgressStatistics.data.chartColors[0];
                    colors[data2[0]] = GLobalMessengerProgressStatistics.data.chartColors[1];
                    colors[data3[0]] = GLobalMessengerProgressStatistics.data.chartColors[2];
                    this.c3data.data.colors = colors;
                    return this.c3data;
                }
            }
        },
        updateChart: function (url, data) {
            GLobalMessengerProgressStatistics.data.globalProgressChartData.c3data.bindto = data.bindto;
            $.ajax({
                type: data.requestType,
                url: url,
                data: {
                    month: data.month,
                    year: data.year,
                    start: data.start,
                    end: data.end
                },
                success: progressCallBack
            });

        }
    };

    function progressCallBack(data) {
        var time = ['x'];
        var data1 = [];
        var data2 = [];
        var data3 = [];
        for (var objIndex in data) {
            var jsonItem = data[objIndex];
            for (var objKey in jsonItem) {
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

        c3.generate(GLobalMessengerProgressStatistics.data.globalProgressChartData.build(time, data1, data2, data3));
        AGN.Lib.CoreInitializer.run('equalizer');
    }

    function updateVisability($select) {
        var selectedValue = $select.val();

        if (selectedValue === 'STATUS') {
            AGN.Lib.DomInitializer.try('messenger-global-status-stat');
        }

        if (selectedValue === "TYPE") {
            AGN.Lib.DomInitializer.try('messenger-global-type-stat');
        }

        if (selectedValue === "PROGRESS") {
            AGN.Lib.DomInitializer.try('messenger-global-week-progress-stat');
        }
    }

    function display(showType, showStatus, showProgress, progressType) {
        var type = $('#typeStatistics'),
            $status = $('#statusStatistics'),
            $progress = $('#progressStatistics'),
            $navbar = $('#dateSelectMode');

        var isWeek, isMonth, isPeriod;

        showType ? type.show() : type.hide();
        showStatus ? $status.show() : $status.hide();
        showProgress ? $progress.show() : $progress.hide();
        showProgress ? $navbar.show() : $navbar.hide();

        if (progressType) {
            isWeek = progressType === 'week';
            isMonth = progressType === 'month';
            isPeriod = progressType === 'period';
        }

        displayDifferentProgressType(isWeek, isMonth, isPeriod);
    }

    function displayDifferentProgressType(isWeek, isMonth, isPeriod) {

        if (isWeek) {
            $('#weekProgressStat').addClass('active');
            $('#monthProgressStat').removeClass('active');
            $('#periodProgressStat').removeClass('active');
            $('#weekTab').show();
            $('#monthTab').hide();
            $('#periodTab').hide();
        }
        if (isMonth) {
            $('#weekProgressStat').removeClass('active');
            $('#monthProgressStat').addClass('active');
            $('#periodProgressStat').removeClass('active');
            $('#weekTab').hide();
            $('#monthTab').show();
            $('#periodTab').hide();
        }
        if (isPeriod) {
            $('#weekProgressStat').removeClass('active');
            $('#monthProgressStat').removeClass('active');
            $('#periodProgressStat').addClass('active');
            $('#weekTab').hide();
            $('#monthTab').hide();
            $('#periodTab').show();
        }

    };

    function generate($elem, chart) {
        var config = AGN.Lib.Helpers.objFromString($elem.data('config'));
        if (config && config.url) {
            chart.updateChart(config.url);
        }
    }

    function generateProgressChart($elem, chart, data) {
        var month = $("#selectMonth").val(),
            year = $("#selectYear").val(),
            startDate = $("#startDay").val(),
            endDate = $("#endDay").val();

        data.month = month;
        data.year = year;
        data.start = startDate;
        data.end = endDate;

        var config = AGN.Lib.Helpers.objFromString($elem.data('config'));
        if (config && config.url) {
            chart.updateChart(config.url, data);
        }
    }

    AGN.Lib.Controller.new('messenger-global-statistics', function () {

        this.addDomInitializer('messenger-global-type-stat', function ($e) {
            generate($e, GlobalMessengerTypeStatistics);
            display(true, false, false);
        });

        this.addDomInitializer('messenger-global-status-stat', function ($e) {
            generate($e, GlobalMessengerStatusStatistics);
            display(false, true, false);
        });

        this.addDomInitializer('messenger-global-week-progress-stat', function ($e) {
            generateProgressChart($e, GLobalMessengerProgressStatistics, {
                bindto: '#weekProgressStatistics',
                requestType: 'GET'
            });
            display(false, false, true, 'week');
        });

        this.addDomInitializer('messenger-global-month-progress-stat', function ($e) {
            generateProgressChart($e, GLobalMessengerProgressStatistics, {
                bindto: '#monthProgressStatistics',
                requestType: 'POST'
            });
            display(false, false, true, 'month');
        });

        this.addDomInitializer('messenger-global-period-progress-stat', function ($e) {
            generateProgressChart($e, GLobalMessengerProgressStatistics, {
                bindto: '#periodProgressStatistics',
                requestType: 'POST'
            });
            display(false, false, true, 'period');
        });

        this.addAction({'change': 'choose-stats'}, function () {
            updateVisability(this.el);
        });

        this.addAction({'change': 'month-statistic'}, function () {
            AGN.Lib.DomInitializer.try('messenger-global-month-progress-stat');
        });

        this.addAction({'change': 'period-statistic'}, function () {
            AGN.Lib.DomInitializer.try('messenger-global-period-progress-stat');
        });

        this.addAction({'click': 'choose-week'}, function () {
            AGN.Lib.DomInitializer.try('messenger-global-week-progress-stat');
        });

        this.addAction({'click': 'choose-month'}, function () {
            AGN.Lib.DomInitializer.try('messenger-global-month-progress-stat');
        });

        this.addAction({'click': 'choose-period'}, function () {
            AGN.Lib.DomInitializer.try('messenger-global-period-progress-stat');
        });


    });

})();