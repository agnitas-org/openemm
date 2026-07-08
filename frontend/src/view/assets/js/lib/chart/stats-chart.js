(() => {

  class StatsChart {
    constructor($el, data) {
      this.$el = $el;
      this.data = data;
      this.$el.data('stat-chart', this);
      this.prepare();
      this.render();
    }

    get axisColor() {
      if (!this._axisColor) {
        this._axisColor = AGN.Lib.Helpers.getColorByVar('--chart-axis-color');
      }
      return this._axisColor;
    }

    prepare() {
      // can be overridden in a subclass
    }

    render() {
      throw new Error("Method 'render()' must be implemented by subclass");
    }

    static create(type, $el, data) {
      switch (type) {
        case 'line':
          new AGN.Lib.LineStatsChart($el, data);
          break;
        default:
          throw new Error(`${type} is not a valid StatsChart type`);
      }
    }
  }

  AGN.Lib.StatsChart = StatsChart;
})();
