(function(){

  var Table = AGN.Lib.Table,
      Confirm = AGN.Lib.Confirm,
      Page = AGN.Lib.Page,
      Tooltip = AGN.Lib.Tooltip,
      DeleteCellRenderer = function () {};

  function getValue(params) {
    return params.valueFormatted ? params.valueFormatted : params.value;
  }

  // gets called once before the renderer is used
  DeleteCellRenderer.prototype.init = function(params) {
    params.eGridCell.classList.add('ag-cell-table-actions');

    this.eGui = document.createElement('div');
    this.eGui.innerHTML = '<button type="button" class="btn btn-regular btn-alert"><i class="icon icon-trash-o"></i></button>';

    this.eButton = this.eGui.querySelector('.btn');

    if (params.colDef['button-tooltip']) {
      Tooltip.createTip($(this.eButton), params.colDef['button-tooltip']);
    }

    var value = getValue(params);
    if (value) {
      this.eButton.setAttribute('href', value);
    } else {
      // hide controls if value is not defined
      this.eGui.style.visibility = 'hidden';
    }

    // add event listener to button
    this.eventListener = function(e) {
      var self = this;

      $.get(getValue(params)).done(function(resp) {
        var $resp = $(resp),
            $modal;

        $modal = $resp.all('.modal');

        if ($modal.length == 1) {
          Confirm.create(resp).done(function() {
            Table.get($(self)).api.updateRowData({remove: [params.data]});
          });
        } else {
          Page.render(resp);
        }
      });

      e.preventDefault();
      return false;
    };
    this.eButton.addEventListener('click', this.eventListener);
  };

  // gets called once when grid ready to insert the element
  DeleteCellRenderer.prototype.getGui = function() {
    return this.eGui;
  };

  // gets called whenever the user gets the cell to refresh
  DeleteCellRenderer.prototype.refresh = function(params) {
    var value = getValue(params);

    // hide controls if value is not defined
    this.eGui.style.visibility = value ? 'visible' : 'hidden';

    // set value into cell again
    this.eButton.dataset.url = value;

    // return true to tell the grid we refreshed successfully
    return true;
  };

  // gets called when the cell is removed from the grid
  DeleteCellRenderer.prototype.destroy = function() {
    // do cleanup, remove event listener from button
    this.eButton.removeEventListener('click', this.eventListener);
  };

  AGN.Opt.TableCellRenderers['DeleteCellRenderer'] = DeleteCellRenderer;

})();
