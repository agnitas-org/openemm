(function ($) {

  const $root = $('html');
  let labelColor;

  Chart.register(ChartDataLabels);

  Chart.defaults.color = () => {
    if (!labelColor) {
      // necessary cuz css variable value differs for darkmode and .dark-theme class is set for 'body' element
      labelColor = $('body').css('--chart-label-color');
    }

    return labelColor;
  };
  Chart.defaults.maintainAspectRatio = false;
  Chart.defaults.font.size = $root.css('--chart-font-size');
  Chart.defaults.font.family = $root.css('--chart-font-family');

  Chart.defaults.scale.border.display = false;
  Chart.defaults.scale.grid.display = false;

  Chart.controllers.bar.defaults.barPercentage = 1;

  Chart.defaults.plugins.legend.labels.usePointStyle = true;
  Chart.defaults.plugins.legend.labels.pointStyle = 'rectRounded';
  Chart.defaults.plugins.legend.position = 'right';

  Chart.defaults.plugins.tooltip.enabled = false;
  Chart.defaults.plugins.tooltip.external = (context) => {
    const {chart, tooltip} = context;
    const $tooltip = $getOrCreateTooltip(chart);

    // Hide if no tooltip
    if (tooltip.opacity === 0) {
      $tooltip.css('opacity', 0);
      return;
    }

    if (tooltip.body) {
      const titleLines = tooltip.title || [];
      const bodyLines = tooltip.body.map(b => b.lines);

      const $tooltipHeader = $('<div class="tooltip-header">');
      $tooltipHeader.append($(`<span>${titleLines[0]}</span>`));

      const $tooltipBody = $('<div class="tooltip-body">');

      bodyLines.forEach((body, i) => {
        const colors = tooltip.labelColors[i];

        const $div = $('<div>');
        const $icon = $('<i class="icon icon-square">').css('color', colors.backgroundColor);
        const span = $('<span class="text-truncate">').text(body);

        $div.append($icon);
        $div.append(span);
        $tooltipBody.append($div);
      });

      $($tooltip).empty();

      $($tooltip).append($tooltipHeader);
      $($tooltip).append($tooltipBody);
    }

    const position = detectTooltipPosition($tooltip, chart);

    $tooltip.css({
      opacity: 1,
      left: position.left + 'px',
      top: position.top + 'px'
    });
  };

  function detectTooltipPosition($tooltip, chart) {
    const {chartArea, tooltip} = chart;
    const chartWidth = chartArea.right - chartArea.left;
    const chartHeight = chartArea.bottom - chartArea.top;
    const {offsetLeft: positionX, offsetTop: positionY} = chart.canvas;

    // Calculate the right and bottom boundaries of the chart
    const chartRightBoundary = positionX + chartWidth;
    const chartBottomBoundary = positionY + chartHeight;

    // Adjust tooltip position to stay within chart boundaries
    let left = positionX + tooltip.caretX;
    let top = positionY + tooltip.caretY;

    if (left + $tooltip.outerWidth() > chartRightBoundary) {
      left = chartRightBoundary - $tooltip.outerWidth();
    }
    if (top + $tooltip.outerHeight() > chartBottomBoundary) {
      top = chartBottomBoundary - $tooltip.outerHeight();
    }

    return {
      left: Math.max(positionX, left),
      top: Math.max(positionY, top)
    };
  }

  function $getOrCreateTooltip(chart) {
    let $tooltipEl = $(chart.canvas).parent().find('.chart-tooltip');

    if (!$tooltipEl.exists()) {
      $tooltipEl = $('<div class="chart-tooltip">');
      $(chart.canvas).parent().append($tooltipEl);
    }

    return $tooltipEl;
  };

  // Works like a standard doughnut chart. makes visible pieces on the graph for small percentage values that fall between 0% - 1%.
  class AgnDoughnutController extends Chart.DoughnutController {
    initialize() {
      this._actualRealValues = this.chart.data.datasets[0].data;
      this._exactPercentages = AGN.Lib.ChartUtils.calcExactPercentages(this._actualRealValues);
      this.chart.data.datasets[0].data = calcVisiblePercentages(this._actualRealValues)
      super.initialize();
    }
  }

  AgnDoughnutController.id = 'agnDoughnut';
  AgnDoughnutController.defaults = Chart.DoughnutController.defaults;
  AgnDoughnutController.overrides = _.merge(_.cloneDeep(Chart.DoughnutController.overrides), {
    plugins: {
      legend: {
        // Updates exact percentages and visual pieces size when hide/show some data
        onClick: function (e, legendItem, legend) {
          const controller = getChartController(legend.chart);
          const actualValues = legend.legendItems.map((item, i) => {
            if (legendItem.index === i) {
              return item.hidden ? controller._actualRealValues[i] : 0;
            }

            return item.hidden ? 0 : controller._actualRealValues[i];
          });

          controller._exactPercentages = AGN.Lib.ChartUtils.calcExactPercentages(actualValues);
          legend.chart.data.datasets[0].data = calcVisiblePercentages(actualValues);

          const originHandler = Chart.DoughnutController.overrides.plugins.legend.onClick;
          originHandler(e, legendItem, legend);
        }
      },
      datalabels: {
        // Gets label color by dougnut piece
        color: data => {
          const bgColor = data.dataset.backgroundColor[data.dataIndex];
          return AGN.Lib.Helpers.getColorLuminance(bgColor) < 0.57 ? 'white' : '#4B4B4B';
        },
        // Shows exact percentages
        formatter: (value, context) => {
          const exactPercentages = getChartController(context.chart)._exactPercentages;
          const percent = exactPercentages[context.dataIndex];
          return percent > 0 ? `${percent.toFixed(1)}%` : '';
        }
      },
      tooltip: {
        callbacks: {
          // Shows real values
          label: context => getChartController(context.chart)._actualRealValues[context.dataIndex]
        }
      }
    }
  });

  function getChartController(chart) {
    return chart.getDatasetMeta(0).controller;
  }

  // Rounds percentage values between 0% and 1% to 1%, making them visually visible.
  function calcVisiblePercentages(values) {
    const total = AGN.Lib.ChartUtils.getTotal(values);
    return values.map(v => {
      if (v <= 0) {
        return 0;
      }

      return Math.max(v / total * 100, 1);
    });
  }

  Chart.register(AgnDoughnutController);

})(window.jQuery);