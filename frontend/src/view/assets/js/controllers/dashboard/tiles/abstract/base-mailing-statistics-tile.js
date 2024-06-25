class BaseMailingStatisticsTile extends DraggableTile {

  constructor() {
    if (new.target === BaseMailingStatisticsTile) {
      throw new TypeError("Cannot construct Abstract BaseMailingStatisticsTile instances directly!");
    }

    super();

    if (this.needsRedrawOnMobile()) {
      this._isMobileView = false;
      this.#setupDisplayTypeChangeHandler();
    }

    this._chart = null;
    this._chartData = {names: [], values: [], mailingId: 0};
  }

  #setupDisplayTypeChangeHandler() {
    $(window).on("displayTypeChanged", (e, isMobileView) => {
      this._isMobileView = isMobileView;

      if (this._chart) {
        this.updateChart(true);
      }
    });
  }

  displayOnScreen($place) {
    super.displayOnScreen($place);
    if (this.#$findCanvas().exists()) {
      this.updateChart();
    }
  }

  async updateChart(forceUpdate = false) {
    const mailingIdBefore = this._chartData.mailingId;
    this._chartData.mailingId = this.$el.find('[data-statistics-mailing]').val();

    const mailingChanged = this._chartData.mailingId !== mailingIdBefore
    if (!mailingChanged && !forceUpdate) {
      return;
    }

    if (mailingChanged) {
      const chartData = await this.fetchChartData();
      this.initChartData(chartData);
    }

    this.drawChart();
  }

  fetchChartData() {
    throw new Error("Fetch function not implemented for statistics tile!");
  }

  initChartData(fetchedData) {
    this._chartData.names = fetchedData.names;
    this._chartData.values = fetchedData.values;
  }

  drawChart() {
    if (this._chart) {
      this._chart.destroy();
    }

    const ctx = this.#$findCanvas()[0].getContext('2d');
    this._chart = new Chart(ctx, this.getChartOptions());
  }

  #$findCanvas() {
    return this.$el.find('canvas');
  }

  getChartOptions() {
    throw new Error("Chart options not defined for statistics tile!");
  }

  needsRedrawOnMobile() {
    return false;
  }
}
