class WorkflowStopFilter extends DateRangeFilter {

  init(params) {
    super.init(params);
    this.$filterType = $(`#${this.column}-filter`);
  }

  doesFilterPass(params) {
    const cellType = params.data[this.column]?.type;
    return (this.doesByDateFilterPass(cellType) && super.doesFilterPass(params))
      || this.doesAutomaticEndFilterPass(cellType)
  }

  doesByDateFilterPass(cellType) {
    return this.types.includes('stopDate') && cellType !== 'AUTOMATIC';
  }

  doesAutomaticEndFilterPass(cellType) {
    return this.types.includes('automaticEnd') && cellType === 'AUTOMATIC';
  }

  isFilterActive() {
    return this.types.length > 0;
  }

  getModel() {
    return { ...super.getModel(), types: this.types };
  }

  setModel(model) {
    super.setModel(model);
    this.types = [...this.$filterType.val()];
  }
}

AGN.Opt.Table['filters']['WorkflowStopFilter'] = WorkflowStopFilter;
