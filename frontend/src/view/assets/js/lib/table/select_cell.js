(function(){

  var Table = AGN.Lib.Table,
      SelectCellRenderer = function () {};

  // gets called once before the renderer is used
  SelectCellRenderer.prototype.init = function(params) {
    var self = this;

    this.eGui = document.createElement('div');
    this.eGui.innerHTML = '<label><input type="checkbox" name="" value="on"</label>';

    this.eInput = this.eGui.querySelector('input');
    this.eInput.setAttribute("name", params.valueFormatted ? params.valueFormatted : params.value);

    // add event listener to button
    this.eventListener = function() {
      if (this !== self.eInput) {
        self.eInput.checked = !!!self.eInput.checked
      }
    };

    this.eGui.addEventListener('click', this.eventListener);
  };

  // gets called once when grid ready to insert the element
  SelectCellRenderer.prototype.getGui = function() {
    return this.eGui;
  };

  SelectCellRenderer.prototype.refresh = function(params) {
    this.eInput.setAttribute("name", params.valueFormatted ? params.valueFormatted : params.value);
    return true;
  };

  // gets called when the cell is removed from the grid
  SelectCellRenderer.prototype.destroy = function() {
    // do cleanup, remove event listener from button
    this.eGui.removeEventListener('click', this.eventListener);
  };

  AGN.Lib.SelectCellRenderer = SelectCellRenderer;

})();