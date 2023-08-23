(function () {

  var NotEscapedStringCellRenderer = function () {};

  NotEscapedStringCellRenderer.prototype.init = function (params) {
    this.gui = AGN.Lib.TableCellWrapper(params.data.show);

    var contentDiv = document.createElement("div");
    if (params.value) {
      contentDiv.innerHTML = params.value;
    }
    contentDiv.style.cssText =
      'text-overflow: ellipsis;' +
      'overflow: hidden;';
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
