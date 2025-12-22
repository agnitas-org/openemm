class SetFilter {

  init(params) {
    this.params = params;
    this.column = params.column.colId;
    this.$el = $(`#${this.column}-filter`);
    this.filterValues = this.$el.val();
  }

  getGui() {
    return document.createElement('div'); // filters located in separate filter tile
  }

  doesFilterPass(params) {
    if (!this.filterValues.length) {
      return true;
    }
    let val = params.data[this.column];
    val = val?.name || val;

    if (val instanceof Array) {
      return this.filterValues.every(v => val.includes(v));
    }

    return this.filterValues.includes(val);
  }

  isFilterActive() {
    return this.filterValues.length > 0;
  }

  getModel() {
    return {values: this.filterValues};
  }

  setModel(model) {
    this.filterValues = this.$el.val();
  }
}
