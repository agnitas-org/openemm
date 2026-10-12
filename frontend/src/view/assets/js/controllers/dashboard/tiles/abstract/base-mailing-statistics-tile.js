(() => {

  class BaseMailingStatisticsTile extends AGN.Lib.Dashboard.DraggableTile {

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
      this._mailingId = 0;
      this._data = null;
      this._colors = AGN.Lib.Helpers.getStatColorsHexValues();
      this._cache = new AGN.Lib.InMemoryCache();
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
      const mailingIdBefore = this._mailingId;
      this._mailingId = this.$el.find('[data-statistics-mailing]').val();

      const mailingChanged = this._mailingId !== mailingIdBefore
      if (!mailingChanged && !forceUpdate) {
        return;
      }

      if (mailingChanged) {
        this._data = await this.#fetchData();
      }

      this.drawChart();
    }

    #fetchData() {
      const mailingId = this._mailingId;

      const cachedResult = this._cache.get(mailingId);
      if (cachedResult) {
        return cachedResult;
      }

      const promise = new Promise((resolve, reject) => {
        $.ajax({
          type: "GET",
          url: this.getStatisticsUrl(),
          data: { mailingId },
          success: data => resolve(data),
          error: error => reject(error)
        });
      });

      this._cache.set(mailingId, promise);
      return promise;
    }

    getStatisticsUrl() {
      throw new Error("#getStatisticsUrl function not implemented for statistics tile!");
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
