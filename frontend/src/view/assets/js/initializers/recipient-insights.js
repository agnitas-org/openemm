(function() {
  var MISSING_VALUE_MARK = '---';
  var MIN_VALUE_THRESHOLD = 0.01;
  var LOW_VALUE_MARK = '< 1';
  var LOW_VALUE = 0.001;  // Any value lower that threshold.

  var REACTION_GRADE_COLORS = {
    'HIGH_PERFORMER': '#008F00',
    'OPPORTUNITY': '#92D050',
    'LEAD': '#CCCDCD',
    'SLEEPER': '#F43536'
  };

  var REVENUE_GRADE_CASH_COW = 500;
  var REVENUE_GRADE_RISING_STAR = 150;

  var REVENUE_ICON_POOR_DOG = 'poor-dog';
  var REVENUE_ICON_QUESTION_MARK = 'question-mark';
  var REVENUE_ICON_RISING_STAR = 'rising-star';
  var REVENUE_ICON_CASH_COW = 'cash-cow';



  AGN.Lib.DomInitializer.new('recipient-insights', function() {
    var config = this.config;
    var data = this.config.insightsData;

    $.i18n.load(config.translations);

    showWeekdayReactionRankChart(data.weekdayReactionRankData);
    showHourReactionRankChart(data.hourReactionRankData);
    showMailingsRevenueRatingChart(data.mailingsRevenueRatingData, config.mailingUriPattern, config.localeLanguage);
    showReactionsInProgressChart(data.reactionsInProgressData);
    showReactionDeviceRankChart(data.reactionDeviceRankData);

    showMailingReactionPercentage(data.mailingReactionPercentageData);
    showCustomerRevenue(data.customerRevenueData);
    showCustomerPerformance(data.customerPerformanceData);
    showPageImpressions(data.pageImpressionsData);

    this.addAction({change: 'selectReactionProgressPeriodType'}, function() {
      $('#reactionProgressPeriodDates').toggleClass('hidden', 'CUSTOM' !== this.el.val());
    });

    this.addAction({click: 'selectReactionProgressPeriod'}, function() {
      var $type = $('input[name="reactionProgressPeriod"]:checked');
      var min, max;

      switch ($type.val()) {
        case 'YEAR':
        case 'MONTH':
          min = $type.data('min');
          max = $type.data('max');
          break;

        case 'CUSTOM':
          min = $('#reactionProgressMinDate').val();
          max = $('#reactionProgressMaxDate').val();
          break;

        default:
          console.error('Unexpected period type: `' + $type.val() + '`');
          return;
      }

      if (min && max) {
        $.ajax(config.urls.reactionsInProgress, {
          type: 'GET',
          data: {
            customerId: config.customerId,
            min: min,
            max: max
          }
        }).done(function(data) {
          showReactionsInProgressChart(data);
        });
      } else {
        AGN.Lib.Messages(t('defaults.error'), t('defaults.error'), 'alert');
      }
    });
  });

  function asRankChartData(data) {
    var values = ['data1'];
    var percentages = [];
    var maxValue = 0;

    $.each(data.values, function(key, value) {
      if (value > maxValue) {
        maxValue = value;
      }

      values.push(value);
      percentages.push(data.percentages[key]);
    });

    return {
      values: values,
      percentages: percentages,
      maxValue: maxValue
    };
  }

  function setPaddingForAxisX($$, extraPadding) {
    $($$.axes.x[0]).find('.tick text').attr('y', function(index, value) {
      return parseInt(value || 0) + extraPadding;
    });
  }

  function showWeekdayReactionRankChart(data) {
    data = asRankChartData(data);

    var chartTitle = t('recipient.reaction.day');
    var categories = t('date.weekdaysShort');

    c3.generate({
      bindto: '#reaction-weekday-chart',
      onrendered: function() {
        setPaddingForAxisX(this, 10);
      },
      size: {
        height: 200
      },
      data: {
        type: 'bar',
        columns: [data.values],
        labels: {
          format: {
            data1: function(v, id, index) {
              return data.percentages[index] + '%';
            }
          }
        },
        color: function(color, d) {
          return d.value === data.maxValue ? '#0071b9' : '#eaeaea';
        }
      },
      bar: {
        width: {
          ratio: 0.88
        }
      },
      legend: {
        show: false
      },
      tooltip: {
        format: {
          name: function() {
            return chartTitle;
          }
        }
      },
      axis: {
        x: {
          type: 'category',
          categories: categories,
          tick: {
            outer: false
          }
        },
        y: {
          show: false,
          padding: {
            top: 20,
            bottom: 20
          }
        }
      }
    });
  }

  function showHourReactionRankChart(data) {
    data = asRankChartData(data);

    var categories = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24];

    c3.generate({
      bindto: '#reaction-hour-chart',
      onrendered: function() {
        setPaddingForAxisX(this, 10);
      },
      size: {
        height: 200
      },
      data: {
        type: 'bar',
        columns: [data.values],
        labels: {
          format: {
            data1: function(v, id, index) {
              return data.percentages[index] + '%';
            }
          }
        },
        color: function(color, d) {
          return d.value === data.maxValue ? '#0071b9' : '#eaeaea';
        },
        onmouseover: function(d) {
          this.internal.mainText
            .style('visibility', function(label) { return d.index === label.index ? 'visible' : 'hidden'; });
        },
        onmouseout: function(d) {
          this.internal.mainText
            .filter(function(label) { return d.index === label.index; })
            .style('visibility', 'hidden');
        }
      },
      bar: {
        width: {
          ratio: 0.7
        }
      },
      legend: {
        show: false
      },
      tooltip: {
        show: false
      },
      axis: {
        x: {
          type: 'category',
          categories: categories,
          tick: {
            multiline: false,
            centered: true,
            outer: false
          }
        },
        y: {
          show: false,
          padding: {
            top: 20,
            bottom: 20
          }
        }
      }
    });
  };

  function extractRowDataWithLocale(locale)  {
    var format;
    if(locale === 'de') {
        var d3_locale_deDe = d3.locale({
            decimal: ",",
            thousands: ".",
            grouping: [3],
            currency: [""],
            dateTime: "%a %b %e %X %Y",
            date: "%m/%d/%Y",
            time: "%H:%M:%S",
            periods: ["AM", "PM"],
            days: ["Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"],
            shortDays: ["So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"],
            months: ["Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"],
            shortMonths: ["Jan", "Feb", "Mar", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
        });
        format = d3_locale_deDe.numberFormat;
    } else {
      format = d3.format;
    }
    return function (d) {
        return [
            {text: d.date, classes: 'l-revenue-date'},
            {text: format(",.2f")(d.revenue), classes: 'l-revenue-value'},
            {text: d.name, mailingId: d.mailingId, classes: 'l-revenue-name'}
        ];
    }
  };

  function showMailingsRevenueRatingChart(data, mailingUriPattern, locale) {
    var headersData = [
      {title: t('mailing.senddate'), criterion: 'timestamp', sorting: false},
      {title: t('statistic.revenue'), criterion: 'revenue', sorting: false},
      {title: t('Mailing'), criterion: 'name', sorting: false}
    ];

    var extractRowData = extractRowDataWithLocale(locale);

    var getSortingIconClasses = function(d) {
      if (d.sorting === 'asc') {
        return 'icon icon-arrow-down';
      } else if (d.sorting === 'desc') {
        return 'icon icon-arrow-up';
      }
      return 'hidden';
    };

    var changeSorting = $.noop;

    var table = d3.select('#mailings-revenue-rating-table')
      .append('table');

    var headers = table.append('thead')
      .append('tr')
      .selectAll('th')
      .data(headersData)
      .enter()
      .append('th');

    headers.append('span')
      .on('click', function(d) { changeSorting(d); })
      .attr('class', 'clickable unselectable')
      .html(function(d) { return d.title + '&nbsp;'; });

    var sortingIcons = headers.append('i')
      .attr('class', getSortingIconClasses);

    if (data.length) {
      var rows = table.append('tbody')
        .selectAll('tr')
        .data(data)
        .enter()
        .append('tr');

      var cells = rows.selectAll('td')
        .data(extractRowData)
        .enter()
        .append('td')
        .attr('class', function(d) { return d.classes; });

      cells.filter(function(d) { return !d.mailingId; })
        .text(function(d) { return d.text; });

      cells.filter(function(d) { return d.mailingId > 0; })
        .append('a')
        .attr('href', function(d) { return mailingUriPattern.replace(':mailing-id:', d.mailingId); })
        .attr('title', function(d) { return d.text; })
        .text(function(d) { return d.text; });

      changeSorting = function(chosen) {
        sortingIcons.attr('class', function(d) {
          if (d === chosen) {
            d.sorting = (d.sorting === 'asc') ? 'desc' : 'asc';
          } else {
            d.sorting = false;
          }
          return getSortingIconClasses(d);
        });

        var compare = chosen.sorting === 'desc' ? d3.descending : d3.ascending;
        rows.sort(function(a, b) {
          return compare(a[chosen.criterion], b[chosen.criterion]);
        });
      };
    } else {
      rows = table.append('tbody')
        .append('tr')
        .append('td')
        .attr('colspan', 3)
        .attr('class', 'l-revenue-unavailable')
        .text(t('recipient.chart.not_available'));
    }
  }

  function showReactionsInProgressChart(data) {
    var reactionsKey = t('statistic.reactions');
    var impressionsKey = t('statistic.impressions');
    var revenuesKey = t('statistic.revenue');

    var colorsMap = {};

    colorsMap[reactionsKey] = '#99c6e3';
    colorsMap[impressionsKey] = '#f43536';
    colorsMap[revenuesKey] = '#0071b9';

    var categories = [];

    var reactions = [reactionsKey];
    var impressions = [impressionsKey];
    var revenues = [revenuesKey];

    var isImpressionAvailable = false;
    var isRevenueAvailable = false;

    $.each(data.values, function(date, d) {
      categories.push(date);

      reactions.push(d.reactions);
      impressions.push(d.impressions);
      revenues.push(d.revenues);

      if (d.impressions > 0) {
        isImpressionAvailable = true;
      }

      if (d.revenues > 0) {
        isRevenueAvailable = true;
      }
    });

    var columns = [reactions];
    var types = {reactions: 'area'};

    if (isImpressionAvailable) {
      columns.push(impressions);
      types.impressions = 'area';
    }

    if (isRevenueAvailable) {
      columns.push(revenues);
      types.revenues = 'bar';
    }

    var mailingNames = data.mailingNames;
    var count = categories.length;
    var pointSize = 3;

    if (count < 60) {
      pointSize = 5;
    } else if (count < 100) {
      pointSize = 4;
    }

    c3.generate({
      bindto: '#reaction-progress-chart',
      onrendered: function() {
        setPaddingForAxisX(this, 10);
      },
      size: {
        height: 200
      },
      data: {
        columns: columns,
        types: types,
        color: function(color, d) {
          return colorsMap[d.id ? d.id : d] || color;
        }
      },
      tooltip: {
        format: {
          title: function(index) {
            var date = categories[index];
            var mailingIds = data.values[date].mailingIds;
            if (mailingIds.length) {
              return mailingNames[mailingIds[0]];
            } else {
              return date;
            }
          }
        }
      },
      bar: {
        width: 10
      },
      point: {
        r: pointSize
      },
      axis: {
        x: {
          type: 'category',
          categories: categories,
          height: 40,
          tick: {
            multiline: false,
            centered: true,
            outer: false,
            culling: true
          }
        },
        y: {
          show: false
        }
      }
    });
  }

  function showReactionDeviceRankChart(data) {
    var palette = ['#0071B9', '#009BFF', '#64C3FF', '#00558A', '#92D050'];

    var columns = [];
    var names = {};
    var percentages = {};
    var colors = {};

    $.each(data, function(index, d) {
      var id = 'data' + index;

      columns.push([id, d.value]);
      names[id] = t(d.deviceClass);
      percentages[id] = d.percentage;
      colors[id] = palette[index];
    });

    c3.generate({
      bindto: '#reaction-device-chart',
      size: {
        height: 200
      },
      data: {
        columns: columns,
        type: 'donut',
        names: names,
        color: function(color, d) {
          return colors[d.id ? d.id : d] || color;
        }
      },
      donut: {
        label: {
          format: function(value, fraction, id) {
            return percentages[id] + '%';
          }
        }
      },
      legend: {
        position: 'right'
      },
      tooltip: {
        format: {
          value: function(value, fraction, id) {
            return value + ' (' + percentages[id] + '%)';
          }
        }
      }
    });
  }

  function showKpiValue(selector, value) {
    var $content = $(selector);
    var $value = $content.find('.l-kpi-value');
    var $sign = $content.find('.l-kpi-sign');

    if (value > 0) {
      if (value < MIN_VALUE_THRESHOLD) {
        $value.text(LOW_VALUE_MARK);
      } else {
        $value.text(d3.round(value, 2));
      }
    } else {
      $value.text(MISSING_VALUE_MARK);
    }

    $sign.toggle(value > 0);
  }

  function showMailingReactionPercentage(data) {
    showKpiValue('#reactionTypeKpi', data.percentage.percentage);

    $('#reactionGradeIcon').css('color', getActivityGradeColor(data.grade));
  }

  function showCustomerRevenue(data) {
    showKpiValue('#revenueSumKpi', data.value);
    showKpiValue('#revenueAvgKpi', data.average);

    $('#revenueIcon').addClass(getRevenueGradeIcon(data.value));
  }

  function showCustomerPerformance(data) {
    if (data.clicks.percentage > 0) {
      showKpiValue('#performanceClicksKpi', data.clicks.percentage);
    } else {
      showKpiValue('#performanceClicksKpi', data.clicks.value > 0 ? LOW_VALUE : 0);
    }
    showKpiValue('#performanceRevenueKpi', data.revenue);
  }

  function showPageImpressions(data) {
    showKpiValue('#impressionsByClickKpi', data.perClick);
    showKpiValue('#impressionsByVisitKpi', data.perVisit);
    showKpiValue('#impressionsByBuyKpi', data.perBuy);
  }

  function getActivityGradeColor(grade) {
    return REACTION_GRADE_COLORS[grade] || 'inherit';
  }

  function getRevenueGradeIcon(value) {
    if (value >= REVENUE_GRADE_CASH_COW) {
      return REVENUE_ICON_CASH_COW;
    } else {
      if (value >= REVENUE_GRADE_RISING_STAR) {
        return REVENUE_ICON_RISING_STAR;
      } else {
        if (value > 0) {
          return REVENUE_ICON_QUESTION_MARK;
        } else {
          return REVENUE_ICON_POOR_DOG;
        }
      }
    }
  }
})();
