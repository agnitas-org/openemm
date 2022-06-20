AGN.Lib.Controller.new('notification-global-stat', function() {
    this.addAction({
        change: 'selectReportType'
    }, function() {
        var $stats = $('#globalStatInfo');

        switch (this.el.val()) {
          case 'STATUS':
            setChartsVisibility(false, true, false);
            $stats.show();
            break;

          case 'BROWSER':
            setChartsVisibility(false, false, true);
            $stats.hide();
            break;

          case 'PROGRESS':
            setChartsVisibility(true, false, false);
            $stats.show();
            break;
        }
    });

    this.addAction({
        click: 'close'
    }, function() {
        var notificationContainer = $('#notifications-container');
        notificationContainer.fadeOut('slow', function () {
            notificationContainer.remove();
        });
    });

    this.addAction({
        click: 'refreshStat'
    }, function() {
        updateProgressChart();
    });

    this.addAction({
        change: 'select-month'
    }, function() {
        updateProgressChart();
    });

    this.addAction({
        change: 'select-year'
    }, function() {
        updateProgressChart();
    });

    this.addAction({
        click: 'weekProgressStat'
    }, function() {
        $('#selectMode').val('WEEK');
        setDateControlsVisibility(false, false);
        updateProgressChart();

        //$('#refreshStat').click();
        //$('#refreshStat').hide();
        setDateSelectModeActivity(true, false, false);
    });

    this.addAction({
        click: 'monthProgressStat'
    }, function() {
        $('#selectMode').val('MONTH');
        setDateControlsVisibility(true, false);
        $('#refreshStat').show();

        setDateSelectModeActivity(false, true, false);
        updateProgressChart();
    });

    this.addAction({
        click: 'dateRangeProgressStat'
    }, function() {
        $('#selectMode').val('RANGE');
        setDateControlsVisibility(false, true);
        $('#refreshStat').show();

        setDateSelectModeActivity(false, false, true);
        updateProgressChart();
    });

    this.addAction({
        'change' : 'end-date-change'
    }, function() {
        var startDay = $('#startDay').val();
        var endDay = $('#endDay').val();
        $('#startDay').pickadate('picker').set('max', endDay);
        $('#endDay').pickadate('picker').set('min', startDay);
        updateProgressChart();
    });

    this.addAction({
        'change' : 'start-date-change'
    }, function() {
        var startDay = $('#startDay').val();
        var endDay = $('#endDay').val();
        $('#startDay').pickadate('picker').set('max', endDay);
        $('#endDay').pickadate('picker').set('min', startDay);
        updateProgressChart();
    });

    function setDateControlsVisibility(isMonthProgressStatVisible, isDateRangeProgressStatVisible) {
        $('#month-progress-stat').hide();
        $('#daterange-progress-stat').hide();
        if (isMonthProgressStatVisible) {
            $('#month-progress-stat').show();
        }
        if (isDateRangeProgressStatVisible) {
            $('#daterange-progress-stat').show();
        }
    }

    function setDateSelectModeActivity(isWeekActive, isMonthActive, isDateRangeActive) {
        $('#weekProgressStat').removeClass('active');
        $('#monthProgressStat').removeClass('active');
        $('#dateRangeProgressStat').removeClass('active');

        if (isWeekActive) {
            $('#weekProgressStat').addClass('active');
        }

        if (isMonthActive) {
            $('#monthProgressStat').addClass('active');
        }

        if (isDateRangeActive) {
            $('#dateRangeProgressStat').addClass('active');
        }
    }

    function setChartsVisibility(isProgressChartVisible, isStatusChartVisible, isBrowserChartVisible) {
        $('#notificationGlobalStatusStat').hide();
        $('#notificationGlobalBrowserStat').hide();
        $('#notificationGlobalProgressStat').hide();

        if (isProgressChartVisible) {
            AGN.Lib.DomInitializer.try('notification-global-progress-stat');
            $('#notificationGlobalProgressStat').show();
            $('#dateSelectMode').show();
            $('#dateModeControls').show();
        }

        if (isStatusChartVisible) {
            AGN.Lib.DomInitializer.try('notification-global-status-stat');
            $('#notificationGlobalStatusStat').show();
            $('#dateSelectMode').hide();
            $('#dateModeControls').hide();
        }

        if (isBrowserChartVisible) {
            AGN.Lib.DomInitializer.try('notification-global-browser-stat');
            $('#notificationGlobalBrowserStat').show();
            $('#dateSelectMode').hide();
            $('#dateModeControls').hide();
        }
    }

    function updateProgressChart() {
        $('#notifications-container').remove();

        if (isValidDatesRange()) {
            if ($('#monthProgressStat').attr('class') == 'active' || $('#dateRangeProgressStat').attr('class') == 'active') {
                AGN.Lib.DomInitializer.try('notification-global-progress-stat');
            }
        }
    }

    function isValidDatesRange() {
        if ($('#dateRangeProgressStat').attr('class') == 'active') {
            var startDay = $('#startDay').val();
            var endDay = $('#endDay').val();

            if (isEmptyDate(startDay) || isEmptyDate(endDay)) {
                showErrorPeriodFormat();
                return false;
            }

            if( (new Date(startDay).getTime() > new Date(endDay).getTime())) {
                showErrorPeriodFormat();
                return false;
            }
        }
        return true;
    }

    function isEmptyDate(date) {
        return typeof date === 'undefined' || date == '';
    }

    function showErrorPeriodFormat() {
        AGN.Lib.Messages(t('defaults.error'), t('error.statistic.period_format'), 'alert');

        setTimeout(function() {
            var notificationContainer = $('#notifications-container');

            notificationContainer.fadeOut('slow', function(){
                notificationContainer.remove();
            });
        }, 7000);
    }
});
