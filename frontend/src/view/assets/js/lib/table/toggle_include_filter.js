class ToggleIncludeFilter {

  init(params) {
      this.column = params.column.colId;
      this.$el = $(`#${this.column}-filter`);
      this.checked = this.$el.prop('checked');
  }

  getGui() {
    return document.createElement('div'); // filters located in separate filter tile
  }

  doesFilterPass(params) {
    if (this.checked) {
      return true;
    }

    return params.data[this.column] === false;
  }

  isFilterActive() {
      return true;
  }

  getModel() {
      return this.checked;
  }

  setModel(model) {
    this.checked = this.$el.prop('checked');
  }
}
