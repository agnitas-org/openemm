(() => {

  const NotEscapedStringCellRenderer = function () {};

  NotEscapedStringCellRenderer.prototype.init = function (params) {
    this.gui = AGN.Lib.TableCellWrapper(params.api.gridOptionsService.gridOptions, params.data);

    const contentDiv = document.createElement("div");
    if (params.value) {
      contentDiv.innerHTML = params.value;
    }

    contentDiv.classList.add("text-truncate-table");
    this.gui.appendChild(contentDiv);
  };

  NotEscapedStringCellRenderer.prototype.getGui = function () {
    return this.gui;
  };

  NotEscapedStringCellRenderer.prototype.refresh = function () {
    return false;
  };

  AGN.Opt.TableCellRenderers['NotEscapedStringCellRenderer'] = NotEscapedStringCellRenderer;
})();
