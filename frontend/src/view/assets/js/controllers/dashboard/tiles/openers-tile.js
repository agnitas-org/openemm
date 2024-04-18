class OpenersTile extends BaseMailingDeviceStatisticsTile {

  constructor() {
    super();
  }

  extractStatisticsData(fetchedData) {
    return fetchedData['openers'];
  }

  extractPercent(fetchedData) {
    return fetchedData['openersPercent'][0];
  }

  drawChart() {
    super.drawChart();
    this.#updateOpenersPercent(this._chartData.percent);
  }

  #updateOpenersPercent(percent) {
    percent = this.roundTo(percent * 100, 1);
    this.$el.find('#statistics-openers-percent').text(`${percent}%`);
  }

  get id() {
    return DraggableTile.def.TILE.ID.OPENERS;
  }
}