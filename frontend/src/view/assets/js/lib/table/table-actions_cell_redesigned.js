(() => {

  class TableActionsCellRenderer {

    // gets called once before the renderer is used
    init(params) {
      this.$eGui = $('<div class="d-flex gap-2 justify-content-end"></div>')

      const buttonsCfg = params.colDef['buttons'];
      if (!buttonsCfg) {
        return;
      }
      params.data.rowActions.forEach(action => {
        const btnCfg = buttonsCfg.find(cfg => cfg.name === action);
        if (btnCfg) {
          this.$eGui.append(AGN.Lib.Template.text(btnCfg.template, params.data));
        }
      })
      AGN.Lib.CoreInitializer.run('tooltip', this.$eGui);
    }

    // gets called once when grid ready to insert the element
    getGui() {
      return this.$eGui.get(0);
    }

    // gets called whenever the user gets the cell to refresh
    refresh() {
      return true; // return true to tell the grid we refreshed successfully
    }

  }

  AGN.Opt.TableCellRenderers['TableActionsCellRenderer'] = TableActionsCellRenderer;

})();
