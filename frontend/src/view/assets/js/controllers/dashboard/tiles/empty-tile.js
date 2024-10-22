(() => {
  class EmptyTile extends AGN.Lib.Dashboard.DraggableTile {

    static ID = 'empty';

    getOverlay() {
      return AGN.Lib.Template.text('dashboard-tile-empty-overlay');
    }
  }

  AGN.Lib.Dashboard.EmptyTile = EmptyTile;
})();
