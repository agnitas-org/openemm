class DeliveryDateFilter extends DateRangeFilter {

  init(params) {
    super.init(params);
    this.$filterType = $(`#${this.column}-filter`);
  }

  doesFilterPass(params) {
    const value = params.data[this.column];
    return (this.doesDateFilterPass(value) && super.doesFilterPass(params))
      || this.doesSoftBounceFilterPass(value)
      || this.doesHardBounceFilterPass(value)
      || this.doesNoFeedbackFilterPass(value);
  }

  doesDateFilterPass(value) {
    return value && this.types.includes('date') && !['soft-bounce', 'hard-bounce'].includes(value);
  }

  doesSoftBounceFilterPass(value) {
    return this.types.includes('soft-bounce') && value === 'soft-bounce';
  }

  doesHardBounceFilterPass(value) {
    return this.types.includes('hard-bounce') && value === 'hard-bounce';
  }

  doesNoFeedbackFilterPass(value) {
    return this.types.includes('no-feedback') && !value;
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

AGN.Opt.Table['filters']['DeliveryDateFilter'] = DeliveryDateFilter;
