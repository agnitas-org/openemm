(function () {
  var SetFilter = function () {};

  SetFilter.prototype.init = function (params) {
    this.column = params.colDef.field
    this.values = _.sortBy(_.uniq(
      _.map(params.rowModel.rowsToDisplay, function(row) {
        return row.data[params.colDef.field]
      })
    ))

    this.eGui = document.createElement('div');
    this.eGui.innerHTML =
      "<div class=\"ag-filter\">" +
      "  <div>" +
      "    <div class=\"ag-filter-body-wrapper\">" +
               this.buildOptions() +
      "    </div>" +
      "    <div class=\"ag-filter-apply-panel\" id=\"applyPanel\">" +
      "      <a href=\"#\" id=\"clearFilter\" class=\"\">" + t('tables.clearFilter') + "</a>" +
      "    </div>" +
      "  </div>" +
      "</div>";

    this.checkBoxes = $(this.eGui).find('input');
    this.checkedBoxes = [];
    this.clearFilter = $(this.eGui).find('#clearFilter');
    this.checkBoxes.on('change', this.checkBoxesChanged.bind(this));
    this.clearFilter.on('click', this.filterClear.bind(this));

    this.filterActive = false;
    this.filterChangedCallback = params.filterChangedCallback;
    this.valueGetter = params.valueGetter;
  };

  SetFilter.prototype.buildOptions = function() {
    return _.map(this.values, function(value) {
      return "<div class=\"ag-filter-body\">" +
      "  <label class=\"\">" +
      "    <input class=\"\" type=\"checkbox\" value=\"" + value + "\">" +
      "    " + value +
      "  </label>" +
      "</div>"
    }).join("\n");
  }

  SetFilter.prototype.afterGuiAttached = function () {
  }

  SetFilter.prototype.destroy = function () {
    this.checkBoxes.off('change', this.checkBoxesChanged.bind(this));
    this.clearFilter.off('click', this.filterClear.bind(this));
  }

  SetFilter.prototype.checkBoxesChanged = function () {
    this.valuesFiltered = _.map(_.filter(this.checkBoxes, { checked: true }), function(checkbox) {
      return checkbox.value;
    })

    this.filterActive = this.valuesFiltered.length != 0;
    this.filterChangedCallback();
  };


  SetFilter.prototype.filterClear = function () {
    this.checkBoxes.removeAttr('checked');
    this.valuesFiltered = [];

    this.filterActive = false;
    this.filterChangedCallback();
  };

  SetFilter.prototype.getGui = function () {
    return this.eGui;
  };

  SetFilter.prototype.doesFilterPass = function (params) {
    var value = this.valueGetter(params.node);

    return _.contains(this.valuesFiltered, value)
  };

  SetFilter.prototype.isFilterActive = function () {
    return this.filterActive;
  };

  // this example isn't using getModel() and setModel(),
  // so safe to just leave these empty. don't do this in your code!!!
  SetFilter.prototype.getModel = function () {};
  SetFilter.prototype.setModel = function () {};


  AGN.Lib.TableSetFilter = SetFilter;
})()
