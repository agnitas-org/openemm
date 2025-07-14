class WildcardFilter {

  init(params) {
    this.params = params;
    this.column = params.column.colId;
    this.$el = $(`#${this.column}-filter`);
    this.searchText = this.$el.val();
  }

  getGui() {
    return document.createElement('div'); // filters located in separate filter tile
  }

  doesFilterPass(params) {
    if (!this.isFilterActive()) {
      return true;
    }
    const cellValue = params.data[this.column] || '';
    const regexPattern = this.searchText
      .replace(/[%*]/g, '.*')
      .replace(/[_?]/g, '.');

    const regex = new RegExp(`${regexPattern}`, 'i');
    return regex.test(cellValue);
  }

  isFilterActive() {
    return this.searchText?.trim().length > 0;
  }

  getModel() {
    return {values: this.searchText};
  }

  setModel(model) {
    this.searchText = this.$el.val();
  }
}
