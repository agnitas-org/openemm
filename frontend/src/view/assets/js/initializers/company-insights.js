(function(){
    var recipientStatsData = {
        URL: undefined,
        reactorChartData: {
            bindto: '#reactor-type-chart',
            data: {
                columns: [],
                type: 'donut',
                order: null
            },
            color: {
                pattern: ['#008F00', '#92D050', '#CCCDCD', '#F43536'] //dark green, green, grey, red
            },
            legend: {
                position: 'right'
            }
        },
        rebuyChartData: {
            bindto: '#rebuy-rate-chart',
            data: {
                columns: [],
                type: 'gauge'
            },
            color: {
                pattern: ['#0071B9']
            }
        },
        revenueChartData: {
            bindto: '#revenue-chart',
            data: {
                columns: [],
                type: 'bar',
                labels: {
                    show: true,
                    format: function (v, id, i, j) {
                        return v;
                    }
                },
                color: function (color, d) {
                    return color;
                }
            },
            tooltip: {
                show: false
            },
            axis: {
                rotated: true,
                x: {
                    show: false
                },
                y: {
                    show: false,
                    min: 0,
                    max: 150
                }
            },
            legend: {
                show: false
            },
            bar: {
                width: {
                    ratio: 0.7
                }
            }
        },
        progressChartData: {
            bindto: '#progress-of-used-devices-chart',
            onrendered: function() {
                  // all ticks starts from gradation in progress-of-used-devices-chart.
                  d3.selectAll("#progress-of-used-devices-chart")
                      .selectAll(".c3-axis.c3-axis-x")
                      .selectAll(".tick text")
                      .style("text-anchor", "start");
            },
            data: {
                columns: [],
                groups: [[
                    'windows',
                    'mac',
                    'android',
                    'ios',
                    'other'
                ]],
                types: {
                    reactions: 'spline'
                },
                axes: {
                    reactions: 'y2'
                },
                names: {},
                type: 'bar',
                x: 'date',
                xFormat: '%m/%d/%Y',
                order: null
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        culling: true,
                        format: '%m/%d/%Y'
                    }
                },
                y: {
                    padding: {
                        top:10
                    },
                    tick: {
                        format: function (value) {
                            return value + " %";
                        }
                    }
                },
                y2: {
                    show: true,
                    min: 0
                }
            },
            bar: {
                width: {
                    ratio: 0.5
                }
            },
            tooltip: {
                format: {
                    value: function (value, ratio, id) {
                        if (id === 'reactions') {
                            return value;
                        }
                        return value + ' %';
                    }
                }
            },
            color: {
                pattern: ['#0071B9', '#4C9BCE', '#99C6E3', '#CCE3F1', '#CCCDCD', '#555555'] //dark blue, blue, light blue, lighter blue, grey
            }
        },
        devicesChartData: {
            bindto: '#devices-reacted-on-chart',
            data: {
                columns: [],
                type: 'donut',
                order: null,
                names: {}
            },
            legend: {
                position: 'right'
            },
            color: {
                pattern: ['#0071B9', '#4C9BCE', '#99C6E3', '#CCE3F1', '#CCCDCD'] //dark blue, blue, light blue, lighter blue, grey
            }
        }
    };

    var chartSizesForWideScreen = {
        reactorChartData: {
            size: {
                width: 400,
                height: 350
            }
        },
        rebuyChartData: {
            size: {
                width: 400,
                height: 250
            }
        },
        revenueChartData: {
            size: {
                width: 350,
                height: 275
            }
        },
        progressChartData: {
            size: {
                width: 860,
                height: 350
            }
        },
        devicesChartData: {
            size: {
                width: 400,
                height: 350
            }
        }
    };

    var chartSizes = {
        reactorChartData: {
            size: {
                width: 350,
                height: 250
            }
        },
        rebuyChartData: {
            size: {
                width: 250,
                height: 200
            }
        },
        revenueChartData: {
            size: {
                width: 300,
                height: 250
            }
        },
        progressChartData: {
            size: {
                width: 550,
                height: 250
            }
        },
        devicesChartData: {
            size: {
                width: 350,
                height: 250
            }
        }
    };

    var chartsToRender = [];

    var resizeRenderTimer = null;
    var isLargePreviousScreen = false;

    AGN.Lib.RecipientStatsData = recipientStatsData;

    AGN.Lib.DomInitializer.new('company-insights', function() {
        loadStoredStatistics();
        loadDynamicStatistic();
        $('input[type=radio][name=progressOfUsedPeriod]').change(function () {
            if (this.value == 'dates') {
                $('#progressOfUsedDates').removeClass('hidden');
            } else {
                $('#progressOfUsedDates').addClass('hidden');
            }
        });
        $('#refreshProgressOfUsedDevices').on("click", function () {
            loadDynamicStatistic();
        });
        isLargePreviousScreen = isLargeScreen();
        $(window).resize(function () {
            if (resizeRenderTimer != null) clearTimeout(resizeRenderTimer);
            resizeRenderTimer = setTimeout(function () {
                var largeScreen = isLargeScreen();
                if (isLargePreviousScreen != largeScreen) {
                    isLargePreviousScreen = largeScreen;
                    renderCharts();
                }
            }, 200);
        });
    });

    /**
     * Load saved statistics from DB
     */
    function loadStoredStatistics() {
        $.ajax({
            type: "GET",
            url: AGN.url("/recipient/chart/ajaxStatistic.action"),
            success: function (data) {
                if (data) {
                  updateReactorChart(data['reactorType'], data['notAvailable']);
                  updateRebuyChart(data['rebuyTitle'], data['rebuyRate'], data['notAvailable']);
                  updateRevenueChart(data['revenue'], data['notAvailable']);
                  loadImpressionStatistics(data);
                  AGN.Lib.CoreInitializer.run('equalizer');
                }
            }
        });
    }

    /**
     * Calculate and load new statistics data
     */
    function loadDynamicStatistic() {
        var data = {};
        var period = $('input[name=progressOfUsedPeriod]:checked').val();
        if (period == 'month') {
            data['byMonth'] = true;
        } else if (period == 'dates') {
            data['from'] = $('#progressOfUsedFrom').val();
            data['till'] = $('#progressOfUsedTill').val();
        }
        $.ajax({
            type: "GET",
            url: AGN.url("/recipient/chart/progressOfUsedDevices.action"),
            data: data,
            success: function (data) {
                updateUsedDevicesCharts(data);
                AGN.Lib.CoreInitializer.run('equalizer');
            }
        });
    }

    /**
     * Shows and render chart.
     * Adds chart to rerender queue to rerender it after screen resizing
     * @param chartName id of chart holder div, chartName + "Data" - chart data key in recipientStatsData, chartSizes and chartSizesForWideScreen
     */
    function renderChart(chartName) {
        var chartData = recipientStatsData[chartName + "Data"];
        if (chartData == undefined) {
            return;
        }
        var settings = isLargeScreen() ? chartSizesForWideScreen : chartSizes;
        var chartSize = settings[chartName + "Data"];
        $.extend(true, chartData, chartSize);

        $('#' + chartName).removeClass('hidden');

        c3.generate(chartData);
        if (chartsToRender.indexOf(chartName) == -1) {
            chartsToRender.push(chartName);
        }
    }

    function renderCharts() {
        chartsToRender.forEach(function (chartName) {
            renderChart(chartName);
        })
    }

    function updateReactorChart(reactorData, notAvailableMessage) {
        if (reactorData === undefined) {
            return;
        }
        var values = [];
        var hasAnyValue = false;
        for (var key in reactorData) {
            if (reactorData.hasOwnProperty(key)) {
                values.push([key, reactorData[key]]);
                if (reactorData[key] > 0) {
                    hasAnyValue = true;
                }
            }
        }
        if (!hasAnyValue) {
            values = [[notAvailableMessage, 100]];
            recipientStatsData.reactorChartData.color.pattern = ['#CCCDCD'];
        } else {
            if (values.length == 4) {
                recipientStatsData.reactorChartData.color.pattern = ['#008F00', '#92D050', '#CCCDCD', '#F43536'];
            } else {
                recipientStatsData.reactorChartData.color.pattern = ['#92D050', '#CCCDCD', '#F43536'];
            }
        }
        recipientStatsData.reactorChartData.data.columns = values;

        renderChart("reactorChart");
    }

    function updateRebuyChart(title, rate, notAvailableMessage) {
        if (rate === undefined) {
            return;
        }
        if (rate > 0) {
            recipientStatsData.rebuyChartData.data.columns = [[title, toPercents(rate)]];
        } else {
            recipientStatsData.rebuyChartData.gauge = {};
            recipientStatsData.rebuyChartData.gauge.label = {};
            recipientStatsData.rebuyChartData.gauge.label.format = function (value, ratio) {
                return notAvailableMessage;
            };
            recipientStatsData.rebuyChartData.gauge.label.show = false;
            recipientStatsData.rebuyChartData.data.columns = [[notAvailableMessage, 0]];
        }

        renderChart("rebuyChart");
    }

    function updateRevenueChart(revenueData, notAvailableMessage) {
        if (revenueData === undefined) {
            return;
        }
        var colors = ['#0071B9', '#4C9BCE', '#99C6E3', '#CCCDCD'];
        var titles = [];
        var columns = [''];
        var values = [];
        var percents = [];
        var valuesSum = 0;
        var percentSum = 0;
        var isDataAvailable = false;
        for (var key in revenueData) {
            if (revenueData.hasOwnProperty(key)) {
                var value = revenueData[key];
                titles.push(key);
                if (value > 0) {
                    isDataAvailable = true;
                    values.push(value);
                    valuesSum += value;
                } else {
                    values.push(0);
                }
            }
        }
        if (isDataAvailable) {
            var maximumLengthInPercents = isLargeScreen() ? 70 : 60;
            var barIsTooLong = false; //there is no place for label
            for (var i = 0; i < values.length; i++) {
                percents[i] = Math.round((values[i] / valuesSum * 100));
                percentSum += percents[i];
                if (percents[i] >= 70) {
                    barIsTooLong = true;
                }
            }
            if (percentSum != 100) {
                percents[percents.length - 1] += (100 - percentSum);
            }
            for (i = 0; i < percents.length; i++) {
                //Add 25% -> place on bar for symbol
                columns.push(25 + (barIsTooLong ? percents[i] * maximumLengthInPercents / 100 : percents[i]));
            }
            recipientStatsData.revenueChartData.data.labels.format = function (value, id, i, j) {
                return percents[i] + '% ' + titles[i];
            };
        } else {
            for (i = 0; i < titles.length; i++) {
                //25% -> place for symbol on bar + 25% -> 100/4 = 50% to show
                columns.push(50);
                percents[i] = 25;
            }
            recipientStatsData.revenueChartData.data.labels.format = function (value, id, i, j) {
                return notAvailableMessage;
            };
        }
        recipientStatsData.revenueChartData.data.columns = [columns];
        recipientStatsData.revenueChartData.data.color = function (color, d) {
            if (d.value != undefined) {
                return colors[d.index];
            }
            return color;
        };

        renderChart("revenueChart");
    }

    function updateUsedDevicesCharts(usedDevicesData) {
        if (usedDevicesData === undefined || usedDevicesData['usedDevicesByPeriods'] === undefined || usedDevicesData['usedDevices'] === undefined) {
            return;
        }
        var columnNames = {
            windows: usedDevicesData['windowsTitle'],
            mac: usedDevicesData['macTitle'],
            android: usedDevicesData['androidTitle'],
            ios: usedDevicesData['iosTitle'],
            other: usedDevicesData['otherTitle'],
            reactions: usedDevicesData['numberOfReactions']
        };

        //Sort usage
        var donutUsedDevices = usedDevicesData['usedDevices'];
        var donutColumns = [];
        var categoriesSortedByCount = [];
        categoriesSortedByCount.push({name: 'windows', count: donutUsedDevices['windows']});
        categoriesSortedByCount.push({name: 'mac', count: donutUsedDevices['mac']});
        categoriesSortedByCount.push({name: 'android', count: donutUsedDevices['android']});
        categoriesSortedByCount.push({name: 'ios', count: donutUsedDevices['ios']});
        categoriesSortedByCount.sort(function (a, b) {
            return b.count - a.count;
        });

        //Prepare data for donut chart and draw
        for (var j = 0; j < categoriesSortedByCount.length; j++) {
            donutColumns.push([columnNames[categoriesSortedByCount[j].name], categoriesSortedByCount[j].count]);
        }
        donutColumns.push([usedDevicesData['otherTitle'], donutUsedDevices['other']]);
        recipientStatsData.devicesChartData.data.columns = donutColumns;
        recipientStatsData.devicesChartData.data.names = columnNames;
        renderChart("devicesChart");

        //prepare translation map: 'category' -> 'order'
        var columns = [];
        var translationMap = [];
        for (var t = 0; t < categoriesSortedByCount.length; t++) {
            translationMap[categoriesSortedByCount[t].name] = t;
            columns.push([categoriesSortedByCount[t].name]);
        }
        columns.push(['other'], ['reactions'], ['date']);

        //Transform data from server
        for (var i = 0; i < usedDevicesData['usedDevicesByPeriods'].length; i++) {
            var usedDevices = usedDevicesData['usedDevicesByPeriods'][i];
            var windows = usedDevices['windows'];
            var mac = usedDevices['mac'];
            var android = usedDevices['android'];
            var ios = usedDevices['ios'];
            var other = usedDevices['other'];
            var reactions = windows + mac + android + ios + other;
            columns[translationMap['windows']].push(toPercents(windows / reactions));
            columns[translationMap['mac']].push(toPercents(mac / reactions));
            columns[translationMap['android']].push(toPercents(android / reactions));
            columns[translationMap['ios']].push(toPercents(ios / reactions));
            columns[4].push(toPercents(other / reactions));
            columns[5].push(reactions);
            columns[6].push(usedDevices['date']);
        }

        //Prepare and draw stacked bars chart
        recipientStatsData.progressChartData.data.columns = columns;
        recipientStatsData.progressChartData.data.names = columnNames;
        recipientStatsData.progressChartData.data.xFormat = usedDevicesData['dateFormat'];
        recipientStatsData.progressChartData.axis.x.min = usedDevicesData['min'];
        recipientStatsData.progressChartData.axis.x.max = usedDevicesData['max'];
        recipientStatsData.progressChartData.axis.x.tick.format = usedDevicesData['dateFormat'];

        renderChart("progressChart");
    }

    /**
     * 0.253213 -> 25.3
     * @returns {number}
     */
    function toPercents(num) {
        return Math.round(1000 * num) / 10;
    }

    function isLargeScreen(){
        return $(window).width() >= 1600;
    }

    function loadImpressionStatistics(data) {
        loadFavDaysStatistic(data['favoriteOpeningDays']);
        loadFavTimesStatistic(data['favoriteOpeningHours']);
        loadCtr(data['ctr']);
        loadConversion(data['conversion']);
        loadPI(data['pageImpressions']);
        loadPIPerVisit(data['pageImpressionsPerVisit']);
        loadPIPerBuy(data['pageImpressionsPerBuy']);
    }

    function loadCtr(ctr) {
        $('#ctr-val').text(ctr.toString().replace('.', ','));
    }

    function loadConversion(conversion) {
        if (conversion != 0) {
            $('#conversion-val').text(conversion.toString().replace('.', ','));
        } else {
            $('#conversion-val').text("---");
            $("#conversion-val").parent().find('.l-stat-sign').text("");
        }
    }

    function loadPI(pi) {
        if (pi != 0) {
            $('#pi-val').text(pi.toString().replace(',', '.'));
        } else {
            $('#pi-val').text("---");
            $("#pi-val").parent().find('.l-stat-sign').text("");
        }
    }

    function loadPIPerVisit(piPerVisit) {
        if (piPerVisit != 0) {
            $('#pi-visit-val').text(piPerVisit.toString().replace(',', '.'));
        } else {
            $('#pi-visit-val').text("---");
            $("#pi-visit-val").parent().find('.l-stat-sign').text("");
        }
    }

    function loadPIPerBuy(piPerBuy) {
        if (piPerBuy != 0) {
            $('#pi-buy-val').text(piPerBuy.toString().replace(',', '.'));
        } else {
            $('#pi-buy-val').text("---");
            $("#pi-buy-val").parent().find('.l-stat-sign').text("");
        }
    }

    function loadFavDaysStatistic(favDaysData) {
        var divForFavDay = $('#favOpeningDays').find('.stat-fav-day');
        setDataToTheDiv(favDaysData, divForFavDay);
    }

    function loadFavTimesStatistic(favHoursData) {
        var divForFavTimes = $('#favOpeningHours').find('.stat-fav-time');
        setDataToTheDiv(favHoursData, divForFavTimes);
    }

    function setDataToTheDiv(statData, divForFavoriteStatistic) {
        var hr =  divForFavoriteStatistic.closest('.l-stat-content').find('hr');
        var index = 0;
        var mapSize = getSize(statData);
        $.each(statData, function(key, value) {
            divForFavoriteStatistic.find(".l-stat-name").text(key);
            divForFavoriteStatistic.find(".l-stat-val").text(value);
            index++;
            if (index < mapSize) {
                var nextDivForFavDay = divForFavoriteStatistic.clone();
                nextDivForFavDay.insertAfter(hr);
                divForFavoriteStatistic = nextDivForFavDay;
                if (index < mapSize - 1) {
                    hr = hr.clone();
                    hr.insertAfter(divForFavoriteStatistic);
                }
            }
        });
    }

    function getSize(map) {
        var size = 0;
        $.each(map, function(key, value) {
            size++;
        });
        return size;
    }
})();