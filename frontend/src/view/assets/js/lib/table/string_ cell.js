(function () {

  var StringCellRenderer = function () {};

  StringCellRenderer.prototype.init = function (params) {
    this.gui = AGN.Lib.TableCellWrapper(params.data.show);

    var contentDiv = document.createElement("div");
    if (params.value) {
      contentDiv.appendChild(document.createTextNode(params.value));
    }
    contentDiv.style.cssText =
      'text-overflow: ellipsis;' +
      'overflow: hidden;';
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
