(() => {
  AGN.Lib.ChartUtils = {
    getTotal: values => values.reduce((acc, value) => acc + value, 0),
    calcExactPercentages: function (values) {
      const total = this.getTotal(values);
      return values.map(v => v / total * 100)
    }
  };
})();
