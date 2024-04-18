class ClickersTile extends BaseMailingDeviceStatisticsTile {

  constructor() {
    super();
  }

  extractStatisticsData(fetchedData) {
    return fetchedData['clickers'];
  }

  extractPercent(fetchedData) {
    return fetchedData['clickersPercent'][0];
  }

  drawChart() {
    super.drawChart();
    this.#updateClickerPercent(this._chartData.percent);
  }

  #updateClickerPercent(percent) {
    percent = this.roundTo(percent * 100, 1);
    this.$el.find('#statistics-clickers-percent').text(`${percent}%`);
  }

  get id() {
    return DraggableTile.def.TILE.ID.CLICKERS;
  }
}