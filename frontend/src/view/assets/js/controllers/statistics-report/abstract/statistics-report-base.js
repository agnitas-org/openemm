class StatisticsReportBaseController extends AGN.Lib.Controller {

  static MAX_COLUMNS_PER_TABLE = 3;
  static CHART_FONT_SIZE = 11;
  static MAX_BAR_THICKNESS = StatisticsReportBaseController.CHART_FONT_SIZE + 1;
  static MIN_BAR_LENGTH = 1;

  constructor() {
    super();

    Chart.defaults.font.size = StatisticsReportBaseController.CHART_FONT_SIZE;
    Chart.defaults.datasets.bar.maxBarThickness = StatisticsReportBaseController.MAX_BAR_THICKNESS;
    Chart.defaults.datasets.bar.minBarLength = StatisticsReportBaseController.MIN_BAR_LENGTH;

    Chart.register({
      id: 'customCanvasBackground',
      beforeDraw: (chart, args, options) => {
        const { ctx } = chart;
        ctx.save();

        let bgColor = 'transparent';
        if (typeof options.color === 'function') {
          bgColor = options.color(chart);
        } else if (typeof options.color === 'string') {
          bgColor = options.color;
        }

        ctx.fillStyle = bgColor;
        ctx.fillRect(0, 0, chart.width, chart.height);

        ctx.restore();
      }
    });

    const self = this;
    this.addDomInitializer('statistics-report', function () {
      self.config = this.config;
      self.$container = this.el;
      self.figures = self.config?.figures || [];
      self.render();

      window.matchMedia?.('print')?.addEventListener('change', event => {
        if (event.matches) {
          Object.values(Chart.instances).forEach(chart => chart.resize());
        }
      });

      window.waitStatus = self.config?.windowWaitStatus;
    });
  }

  render() {
    throw new Error('render() not implemented!');
  }

  get backgroundColorPlugin() {
    return {
      customCanvasBackground: {
        color: () => getComputedStyle(document.body).getPropertyValue('--bs-body-bg').trim()
      }
    };
  }

  chunkBalanced(items, maxPerChunk) {
    if (items.length === 0) {
      return [];
    }

    const chunkCount = Math.ceil(items.length / maxPerChunk);
    const baseSize = Math.floor(items.length / chunkCount);
    const remainder = items.length % chunkCount;

    const chunks = [];
    let offset = 0;
    for (let i = 0; i < chunkCount; i++) {
      const chunkSize = baseSize + (i < remainder ? 1 : 0);
      chunks.push(items.slice(offset, offset + chunkSize));
      offset += chunkSize;
    }
    return chunks;
  }

  renderChunkedTables($container, items, maxPerChunk, buildTemplateData) {
    this.chunkBalanced(items, maxPerChunk).forEach((chunk, idx) => {
      const [templateName, templateData] = buildTemplateData(chunk, idx);
      $container.append(AGN.Lib.Template.text(templateName, templateData));
    });
  }

  metricValue(raw) {
    if (raw == null) {
      return 0;
    }
    return (typeof raw === 'object') ? raw.value : raw;
  }

  formatRateCell(raw) {
    const metric = raw || {value: 0, rate: 0};
    return `${AGN.formatNumber(metric.value) ?? metric.value} (${metric.rate.toFixed(2)}%)`;
  }

  formatCountCell(raw) {
    return raw == null ? 0 : AGN.formatNumber(raw) ?? raw;
  }

  formatCurrencyCell(raw) {
    return AGN.formatNumber(raw ?? 0);
  }

  assignColors(metricDefs) {
    const colors = AGN.Lib.Helpers.getStatColorsHexValues();
    metricDefs.forEach((m, i) => m.color = colors[i % colors.length]);
  }

  toTargets(targetGroupsData) {
    return targetGroupsData.map(tgData => ({id: tgData.targetId, name: tgData.targetName}));
  }

  indexByTargetId(targetGroupsData) {
    const byTargetId = {};
    targetGroupsData.forEach(tgData => byTargetId[tgData.targetId] = tgData);
    return byTargetId;
  }

  appendSectionHeading($container, title, forceNewPage = true) {
    $container.append(AGN.Lib.Template.text('section-heading', {title, forceNewPage}));
  }

  renderDoughnutChart($canvas, chartMetrics, values) {
    return new Chart($canvas.getContext('2d'), {
      type: 'agnDoughnut',
      data: {
        labels: chartMetrics.map(m => m.label),
        datasets: [{
          data: values,
          backgroundColor: chartMetrics.map(m => m.color),
          borderWidth: 0
        }]
      },
      options: {
        plugins: {
          ...this.backgroundColorPlugin,
          legend: { display: false },
          datalabels: { display: false }
        }
      }
    });
  }

  renderStatSection($container, title, targets, metricDefs, getValue) {
    this.assignColors(metricDefs);

    const $section = AGN.Lib.Template.dom('stat-section', { title, forceNewPage: true });
    $container.append($section);

    const chartMetrics = metricDefs.filter(m => m.chartSlice !== false);
    const $charts = $section.find('[data-charts]');

    targets.forEach(tg => {
      const values = chartMetrics.map(m => this.metricValue(getValue(tg.id, m)));
      const hasData = values.some(v => v);

      const $tile = AGN.Lib.Template.dom('stat-donut-tile', { targetName: tg.name, hasData });
      $charts.append($tile);

      if (!hasData) {
        return;
      }

      this.renderDoughnutChart($tile.find('canvas')[0], chartMetrics, values);
    });

    this.renderLegendTable($section.find('[data-table]'), title, targets, metricDefs, getValue);
  }

  renderLegendTable($container, title, targets, metricDefs, getValue) {
    this.renderChunkedTables($container, targets, this.constructor.MAX_COLUMNS_PER_TABLE, (chunk, idx) => {
      const rows = metricDefs.map(m => ({
        label: m.label,
        color: m.noColor ? null : m.color,
        bold: !!m.bold,
        cells: chunk.map(target => this.formatRateCell(getValue(target.id, m)))
      }));
      return ['stat-legend-table', { title, targets: chunk, rows, idx }];
    });
  }

}

AGN.Lib.StatisticsReportBaseController = StatisticsReportBaseController;
