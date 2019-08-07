(function(){

  var DateCellRenderer = function () {};
  
  // gets called once before the renderer is used
  DateCellRenderer.prototype.init = function(params) {
    this.eGui = document.createElement('div');

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

    if (params.value) {
      if (!params.valueFormatted) {
        params.valueFormatted = moment(params.value).format(this.format);
      }
      this.eGui.innerHTML = params.valueFormatted;
    }

  };

  // gets called once when grid ready to insert the element
  DateCellRenderer.prototype.getGui = function() {
    return this.eGui;
  };

  // gets called whenever the user gets the cell to refresh
  DateCellRenderer.prototype.refresh = function(params) {
    // set value into cell again
    if (params.value) {
      params.valueFormatted = moment(params.value).format(this.format);
      this.eGui.innerHTML = params.valueFormatted;
    } else { 
      this.eGui.innerHTML = '';
    }
    // return true to tell the grid we refreshed successfully
    return true;
  };

  // gets called when the cell is removed from the grid
  DateCellRenderer.prototype.destroy = function() {
    // do cleanup, remove event listener from button
  };

  AGN.Opt.TableCellRenderers['DateCellRenderer'] = DateCellRenderer;

})();
