(function () {

  const StringCellRenderer = function () {};

  StringCellRenderer.prototype.init = function (params) {
    this.gui = AGN.Lib.TableCellWrapper(params.api.gridOptionsService.gridOptions, params.data);

    const contentDiv = document.createElement("div");
    if (params.value) {
      contentDiv.appendChild(document.createTextNode(params.value));
    }

    contentDiv.classList.add("text-truncate-table");
    this.gui.appendChild(contentDiv);
  };

  StringCellRenderer.prototype.getGui = function () {
    return this.gui;
  };

  StringCellRenderer.prototype.refresh = function () {
    return false;
  };

  AGN.Opt.TableCellRenderers['StringCellRenderer'] = StringCellRenderer;
})();
