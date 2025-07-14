(() => {

  class MustacheTemplateCellRender {

    // gets called once before the renderer is used
    init(params) {
      const api = params.api;
      this.eGui = AGN.Lib.TableCellWrapper(
        api.getGridOption('viewLinkTemplate'),
        api.getGridOption('viewInModal'),
        api.getGridOption('isRestoreMode')(),
        params.data
      );
      this.eGui.innerHTML = AGN.Lib.Template.text(
        params.templateName,
        {value: params.value, entry: params.data}
      );

      AGN.Lib.CoreInitializer.run('tooltip', $(this.eGui));
    }

    // gets called once when grid ready to insert the element
    getGui() {
      return this.eGui;
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

  AGN.Opt.TableCellRenderers['MustacheTemplateCellRender'] = MustacheTemplateCellRender;

})();
