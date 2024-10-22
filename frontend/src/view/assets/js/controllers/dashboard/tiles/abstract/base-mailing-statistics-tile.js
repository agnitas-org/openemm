(() => {

  class BaseMailingStatisticsTile extends AGN.Lib.Dashboard.DraggableTile {

    static FETCHED_DATA_CACHE = new AGN.Lib.InMemoryCache();

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

    displayIn($place) {
      super.displayIn($place);
      this.updateChartIfCanvasExists();
    }

    insertAfter(tile) {
      super.insertAfter(tile);
      this.updateChartIfCanvasExists();
    }

    insertBefore(tile) {
      super.insertBefore(tile);
      this.updateChartIfCanvasExists();
    }

    updateChartIfCanvasExists() {
      if (this.findCanvas$().exists()) {
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
        const chartData = await this.#fetchChartData();
        this.initChartData(this.extractStatisticsData(chartData));
      }

      this.drawChart();
    }

    #fetchChartData() {
      const mailingId = this._chartData.mailingId;

      const cachedResult = BaseMailingStatisticsTile.FETCHED_DATA_CACHE.get(mailingId);
      if (cachedResult) {
        return cachedResult;
      }

      const dataExtractionPromise = new Promise((resolve, reject) => {
        $.ajax({
          type: "GET",
          url: AGN.url('/dashboard/statistics.action'),
          data: {mailingId},
          success: data => resolve(data),
          error: error => reject(error)
        });
      });

      BaseMailingStatisticsTile.FETCHED_DATA_CACHE.set(mailingId, dataExtractionPromise);
      return dataExtractionPromise;
    }

    extractStatisticsData(data) {
      throw new Error("Extract statistics function not implemented for statistics tile!");
    }

    initChartData(fetchedData) {
      this._chartData.names = fetchedData.names;
      this._chartData.values = fetchedData.values;
    }

    drawChart() {
      this._chart?.destroy();

      const ctx = this.findCanvas$()[0].getContext('2d');
      this._chart = new Chart(ctx, this.getChartOptions());
    }

    findCanvas$() {
      return this.$el.find('canvas');
    }

    getChartOptions() {
      throw new Error("Chart options not defined for statistics tile!");
    }

    needsRedrawOnMobile() {
      return false;
    }
  }

  AGN.Lib.Dashboard.BaseMailingStatisticsTile = BaseMailingStatisticsTile;
})();
