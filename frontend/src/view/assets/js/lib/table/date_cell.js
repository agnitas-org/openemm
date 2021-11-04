(function(){

  var DateCellRenderer = function () {};

  function getFormattedValue(params, format) {
    if (params.value) {
      if (!params.valueFormatted) {
        params.valueFormatted = moment(params.value).format(format);
      }

      return params.valueFormatted;
    } else {
      return '';
    }
  }

  // gets called once before the renderer is used
  DateCellRenderer.prototype.init = function(params) {
    this.eGui = AGN.Lib.TableCellWrapper(params.data.show);

    if (params.optionDateFormat) {
      this.format = params.optionDateFormat;
    } else {
      this.format = "DD.MM.YYYY";

      if (params.optionShowTime) {
        if (params.optionShowSeconds) {
          this.format += ' HH:mm:ss';
        } else {
          this.format += ' HH:mm';
        }
      }
    }
    this.eGui.innerHTML = getFormattedValue(params, this.format);
  };

  // gets called once when grid ready to insert the element
  DateCellRenderer.prototype.getGui = function() {
    return this.eGui;
  };

  // gets called whenever the user gets the cell to refresh
  DateCellRenderer.prototype.refresh = function(params) {
    // set value into cell again
    this.eGui.innerHTML = getFormattedValue(params, this.format);
    // return true to tell the grid we refreshed successfully
    return true;
  };

  // gets called when the cell is removed from the grid
  DateCellRenderer.prototype.destroy = function() {
    // do cleanup, remove event listener from button
  };

  AGN.Opt.TableCellRenderers['DateCellRenderer'] = DateCellRenderer;

})();
