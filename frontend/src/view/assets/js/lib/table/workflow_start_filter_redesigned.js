class WorkflowStartFilter extends DateRangeFilter {

  init(params) {
    super.init(params);
    this.$filterType = $(`#${this.column}-filter`);
  }

  doesFilterPass(params) {
    const cellType = params.data[this.column]?.type;
    return (this.doesByDateFilterPass(cellType) && super.doesFilterPass(params))
      || this.doesActionBasedFilterPass(cellType)
      || this.doesDateBasedFilterPass(cellType);
  }

  doesByDateFilterPass(cellType) {
    return this.types.includes('startDate') && cellType !== 'EVENT_REACTION' && cellType !== 'EVENT_DATE';
  }

  doesActionBasedFilterPass(cellType) {
    return this.types.includes('actionBased') && cellType === 'EVENT_REACTION';
  }

  doesDateBasedFilterPass(cellType) {
    return this.types.includes('dateBased') && cellType === 'EVENT_DATE';
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

AGN.Opt.Table['filters']['WorkflowStartFilter'] = WorkflowStartFilter;
