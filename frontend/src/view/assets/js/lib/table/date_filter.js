(function() {
  var DateFilter = function() {};

  DateFilter.prototype.init = function (params) {
    this.eGui = document.createElement('div');
    this.eGui.innerHTML =
        "<div class=\"ag-filter\">" +
        "  <div>" +
        "    <div class=\"ag-filter-body-wrapper\">" +
        "      <div class=\"ag-filter-body\">" +
        "        <label class=\"\">" + t('tables.from') + "</label>" +
        "        <input class=\"ag-filter-filter form-control js-datepicker js-datepicker-right\" id=\"filterBegin\" type=\"text\" placeholder=\"\">" +
        "      </div>" +
        "      <div class=\"ag-filter-body\">" +
        "        <label class=\"\">" + t('tables.till') + "</label>" +
        "        <input class=\"ag-filter-filter form-control js-datepicker js-datepicker-right\" id=\"filterEnd\" type=\"text\" placeholder=\"\">" +
        "      </div>" +
        "    </div>" +
        "    <div class=\"ag-filter-apply-panel\" id=\"applyPanel\">" +
        "      <a href=\"#\" id=\"clearFilter\" class=\"\">" + t('tables.clearFilter') + "</a>" +
        "    </div>" +
        "  </div>" +
        "</div>";

    this.filterBegin = $(this.eGui).find('#filterBegin');
    this.filterEnd = $(this.eGui).find('#filterEnd');
    this.beginDate = undefined;
    this.endDate = undefined;
    this.clearFilter = $(this.eGui).find('#clearFilter');
    this.filterBegin.on('change', this.filterBeginChanged.bind(this));
    this.filterEnd.on('change', this.filterEndChanged.bind(this));
    this.clearFilter.on('click', this.filterClear.bind(this));
    
    this.filterActive = false;
    this.filterChangedCallback = params.filterChangedCallback;
    this.valueGetter = params.valueGetter;
  };

  DateFilter.prototype.afterGuiAttached = function() {
    AGN.Initializers.PickADate($(this.eGui));
  }

  DateFilter.prototype.destroy = function() {
    this.filterBegin.off('change', this.filterBeginChanged.bind(this));
    this.filterEnd.off('change', this.filterEndChanged.bind(this));
  }

  DateFilter.prototype.filterBeginChanged = function () {
    var apiMax = this.filterEnd.data('pickadate');
    apiMax.set('min', this.filterBegin.val());

    this.beginDate = this.filterBegin.data('pickadate').get('select').pick;

    this.filterActive = (this.beginDate || this.endDate);
    this.filterChangedCallback();
  };

  DateFilter.prototype.filterEndChanged = function () {
    var apiMin = this.filterBegin.data('pickadate');
    apiMin.set('max', this.filterEnd.val())

    this.endDate = this.filterEnd.data('pickadate').get('select').pick;

    this.filterActive = (this.beginDate || this.endDate);
    this.filterChangedCallback();
  };

  DateFilter.prototype.filterClear = function () {
    var apiMin, apiMax;
    
    this.filterEnd.val(undefined);
    this.filterBegin.val(undefined);
    this.beginDate = undefined;
    this.endDate = undefined;

    apiMax = this.filterEnd.data('pickadate');
    apiMax.set('min', false);
    apiMin = this.filterBegin.data('pickadate');
    apiMin.set('max', false)

    this.filterActive = false;
    this.filterChangedCallback();
  };

  DateFilter.prototype.getGui = function () {
    return this.eGui;
  };

  DateFilter.prototype.doesFilterPass = function (params) {
    var date = this.valueGetter(params.node);

    if (!this.beginDate && !this.endDate) { 
      return true;
    }

    if (!date) {
      return false;
    }

    if (this.beginDate && this.endDate) {
      return date >= this.beginDate && date <= this.endDate
    }

    if (this.beginDate) {
      return date >= this.beginDate
    }

    if (this.endDate) {
      return date <= this.endDate
    }
  };

  DateFilter.prototype.parseDate = function(date) {
    if (!date) {
      return;
    }
    return moment(date, t('date.format').toUpperCase()).valueOf();
  }

  DateFilter.prototype.isFilterActive = function () {
    return this.filterActive;
  };

  // this example isn't using getModel() and setModel(),
  // so safe to just leave these empty. don't do this in your code!!!
  DateFilter.prototype.getModel = function() {};
  DateFilter.prototype.setModel = function() {};


  AGN.Lib.TableDateFilter = DateFilter;
})()