(() => {

  class StringCellRenderer {

    // gets called once before the renderer is used
    init(params) {
      const api = params.api;
      this.gui = AGN.Lib.TableCellWrapper(
        api.getGridOption('viewLinkTemplate'),
        api.getGridOption('viewInModal'),
        api.getGridOption('isRestoreMode')(),
        params.data
      );

      const contentDiv = document.createElement("div");
      if (params.value) {
        contentDiv.appendChild(this.getContent(params.value));
      }

      contentDiv.classList.add("text-truncate-table");
      this.gui.appendChild(contentDiv);
    }

    getContent(value) {
      return document.createTextNode(value)
    }

    // gets called once when grid ready to insert the element
    getGui() {
      return this.gui;
    }

    // gets called whenever the user gets the cell to refresh
    refresh() {
      return false;
    }

    // gets called when the cell is removed from the grid
    destroy() {
      // do cleanup, remove event listener from button
    }
  }

  AGN.Opt.TableCellRenderers['StringCellRenderer'] = StringCellRenderer;

  class ClickToCopyCellRenderer extends StringCellRenderer {

    getContent(value) {
      const $el = $('<span>', {
        'class': 'click-to-copy',
        'text': value
      });
      AGN.Lib.CoreInitializer.run('copyable', $el);
      return $el[0];
    }
  }

  AGN.Opt.TableCellRenderers['ClickToCopyCellRenderer'] = ClickToCopyCellRenderer;
})();
