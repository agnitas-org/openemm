class EmptyTile extends DraggableTile {
  
  get id() {
    return DraggableTile.def.TILE.ID.EMPTY;
  }

  getOverlay() {
    return AGN.Lib.Template.text('dashboard-tile-empty-overlay');
  }
}
