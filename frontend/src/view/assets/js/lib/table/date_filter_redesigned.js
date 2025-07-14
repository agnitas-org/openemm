class DateRangeFilter {

  init(params) {
    this.params = params;
    this.column = params.column.colId;
    this.$fromDate = this.#getDateRangeFilter(params, 'from');
    this.$toDate = this.#getDateRangeFilter(params, 'to');
  }

  #getDateRangeFilter(params, suffix) {
    let $filter = $(`${params.colDef[`filter${_.capitalize(suffix)}Input`]}`);
    if (!$filter.length) {
      $filter = $(`#${params.column.colId}-${suffix}-filter`)
    }
    return $filter;
  }

  #getIsoVal($dateInput) {
    return $dateInput.datepicker('getDate')?.toISOString();
  }

  isFilterActive() {
    return this.fromDate || this.toDate;
  }

  doesFilterPass(params) {
    if (!this.fromDate && !this.toDate) {
      return true;
    }

    const date = this.#getDate(params);
    if (!date) {
      return false;
    }

    if (this.fromDate && this.toDate) {
      return date >= this.fromDate && date <= this.toDate
    }
    if (this.fromDate) {
      return date >= this.fromDate
    }
    if (this.toDate) {
      return date <= this.toDate
    }
  }

  #getDate(params) {
    const value = params.data[this.column];
    if (typeof value === 'number' && value > 0) {
      return new Date(value);
    }

    return value?.date;
  }

  getModel() {
    return {
      fromDate: this.fromDate,
      toDate: this.toDate
    };
  }

  setModel(model) {
    if (this.$fromDate?.length) {
      const fromDateIso = this.#getIsoVal(this.$fromDate);
      this.fromDate = fromDateIso ? new Date(fromDateIso) : null;
    }
    if (this.$toDate?.length) {
      const toDateIso = this.#getIsoVal(this.$toDate);
      this.toDate = toDateIso ? new Date(toDateIso) : null;
      this.toDate?.setDate(this.toDate?.getDate() + 1);
    }
  }

  getGui() {
    return document.createElement('div'); // filters located in separate filter tile
  }
}
