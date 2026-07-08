(() => {
  AGN.Lib.ChartUtils = {
    getTotal(values) {
      return values.reduce((acc, value) => acc + value, 0);
    },
    calcExactPercentages(values) {
      const total = this.getTotal(values);
      if (total === 0) {
        return values.map(v => 0);
      }

      return values.map(v => v / total * 100)
    },
    getLightBlueColor() {
      return this.getChartVarColor('--chart-light-blue-color');
    },
    getDarkBlueColor() {
      return this.getChartVarColor('--chart-dark-blue-color');
    },
    getDarkestBlueColor() {
      return this.getChartVarColor('--chart-darkest-blue-color');
    },
    getBlueColor() {
      return this.getChartVarColor('--chart-blue-color');
    },
    getCyanColor() {
      return this.getChartVarColor('--chart-cyan-color');
    },
    getDarkestCyanColor() {
      return this.getChartVarColor('--chart-darkest-cyan-color');
    },
    getGreenColor() {
      return this.getChartVarColor('--chart-green');
    },
    getDarkGreenColor() {
      return this.getChartVarColor('--chart-dark-green');
    },
    getLimeColor() {
      return this.getChartVarColor('--chart-lime-color');
    },
    getDarkCyanColor() {
      return this.getChartVarColor('--chart-dark-cyan-color');
    },
    getLightVioletColor() {
      return this.getChartVarColor('--chart-light-violet');
    },
    getDarkYellowColor() {
      return this.getChartVarColor('--chart-dark-yellow-color');
    },
    getDarkestYellowColor() {
      return this.getChartVarColor('--chart-darkest-yellow-color');
    },
    getLightRedColor() {
      return this.getChartVarColor('--chart-light-red-color');
    },
    getDarkRedColor() {
      return this.getChartVarColor('--chart-dark-red-color');
    },
    getChartVarColor(cssVariableName) {
      return getComputedStyle(document.body).getPropertyValue(cssVariableName);
    }
  };
})();
