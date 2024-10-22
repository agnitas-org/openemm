(() => {
  const TileSize = AGN.Lib.Dashboard.Def.TileSize;

  class DashboardGrid {

    constructor(rows, cols, tiles) {
      this.rows = rows;
      this.cols = cols;
      this.swapper = new AGN.Lib.Dashboard.TileSwapper(this);
      this.$container = $('#dashboard-tiles');
      this.$container.on('agn:grid-modified', (e, tile) => this.onGridTileModified(tile));
      this.fillGrid(tiles);
    }

    fillGrid(tiles) {
      this.#sortTilesByPosition(tiles)
      this.removeAllTiles();
      tiles.forEach(tile => tile.displayIn(this.$container));

      this.$container.removeClass((index, className) => (className.match(/\bdashboard-grid-cols-\S+/g) || []).join(' '));
      this.$container.addClass(`dashboard-grid-cols-${this.cols}`);
    }

    #sortTilesByPosition(tiles) {
      tiles.sort((a, b) => {
        const aRow = a.position.rows[0];
        const aCol = a.position.cols[0];
        const bRow = b.position.rows[0];
        const bCol = b.position.cols[0];

        if (aRow !== bRow) {
          return aRow - bRow;
        }
        return aCol - bCol;
      });
    }

    removeAllTiles() {
      this.$tiles.each((i, tile) => $(tile).data('tile').remove());
    }

    get visibleTiles() {
      return this.tiles.filter(tile => tile.$el.css("visibility") !== "hidden");
    }

    get schema() {
      return {COLS_COUNT: this.cols, SCHEMA: this.tiles.map(({id, position}) => ({id, position}))};
    }

    isCellFree(row, col) {
      const tile = this.getTile(row, col);
      return !tile || tile.isEmpty;
    }

    canPlaceTile(tileSize, row, col) {
      const [tileRows, tileCols] = tileSize.dimensions;
      for (let rowOffset = 0; rowOffset < tileRows; rowOffset++) {
        for (let colOffset = 0; colOffset < tileCols; colOffset++) {
          let newRow = row + rowOffset;
          let newCol = col + colOffset;
          if (newRow >= this.rows || newCol >= this.cols || !this.isCellFree(newRow, newCol)) {
            return false;
          }
        }
      }
      return true;
    }

    onGridTileModified(tile) {
      this.updateTilesPositions();
      if (tile) {
        this.swapper.initDragAndDrop(tile);
      }
    }

    placeTileAtPos(tile, row, col) {
      if (!this.canPlaceTile(tile.size, row, col)) {
        return;
      }
      const [height, width] = tile.size.dimensions;
      this.getInterferingTiles(height, width, row, col).forEach(tile => tile?.remove());
      this.insertTile(tile, row, col);
    }

    getInterferingTiles(height, width, row, col) {
      return _
        .flatMap(_.range(height), rowOffset => _.range(width).map(colOffset =>
          this.getTile(row + rowOffset, col + colOffset)
        ).filter(Boolean)
      );

    }

    insertTile(tile, row, col) {
      const currentTileAtPos = this.getTile(row, col);
      const prevTile = this.getPreviousTileOf(currentTileAtPos);
      if (prevTile) {
        tile.insertAfter(prevTile);
      } else if (currentTileAtPos) {
        tile.insertBefore(currentTileAtPos);
      } else {
        tile.displayIn(this.$container);
      }
    }

    getPreviousTileOf(tile) {
      if (!tile) {
        return null;
      }
      const tiles = this.tiles;
      const index = tiles.findIndex(t => t === tile);
      return index > 0 ? tiles[index - 1] : null;
    }

    get tiles() {
      return this.$tiles.toArray().map($tile => $($tile).data('tile'));
    }

    get tallTiles() {
      return this.tiles.filter(tile => tile.isTall);
    }

    get xlTiles() {
      return this.tiles.filter(tile => tile.isXl);
    }

    get wideTiles() {
      return this.tiles.filter(tile => tile.isWide);
    }

    get xWideTiles() {
      return this.tiles.filter(tile => tile.isXWide);
    }

    get $tiles() {
      return $('.draggable-tile');
    }

    getTile(row, col) {
      return this.tiles.find(tile => tile.position.rows.includes(row) && tile.position.cols.includes(col));
    }

    getPossibleStartPositions(tileSize, clickedRow, clickedCol) {
      const [tileRows, tileCols] = tileSize.dimensions;
      const startPositions = [];

      for (let row = clickedRow - tileRows + 1; row <= clickedRow; row++) {
        for (let col = clickedCol - tileCols + 1; col <= clickedCol; col++) {
          if (row >= 0 && col >= 0 && this.canPlaceTile(tileSize, row, col)) {
            startPositions.push([row, col]);
          }
        }
      }
      return startPositions;
    }

    placeTileAroundPos(tile, row, col) {
      const startPositions = this.getPossibleStartPositions(tile.size, row, col);
      if (startPositions.length > 0) {
        const [startRow, startCol] = startPositions[0];
        this.placeTileAtPos(tile, startRow, startCol);
      }
    }

    createEmptyTile() {
      return new AGN.Lib.Dashboard.EmptyTile();
    }

    removeTile(tile) {
      if (!tile) {
        return;
      }
      tile.remove();

      const {rows, cols} = tile.position;
      rows.forEach(row => cols.forEach(col => this.insertTile(this.createEmptyTile(), row, col)));
    }

    updateTilesPositions() {
      const tiles = this.tiles;
      const pointer = {row: 0, col: -1}
      tiles.forEach(tile => tile.position = {rows: [], cols: []}); // clear current positions
      tiles.forEach(tile => tile.position = this.calculateTilePositionInGrid(tiles, tile.size, pointer)); // set new positions
    }


    calculateTilePositionInGrid(tiles, tileSize, pointer) {
      pointer.col = pointer.col + 1;
      if (pointer.col >= this.cols) {
        pointer.row = pointer.row + 1;
        pointer.col = 0;
      }
      while (this.isPositionAlreadyTakenByOtherExtendedTile(tiles, pointer.row, pointer.col)) {
        pointer.col = pointer.col + 1;
      }

      switch (tileSize) {
        case TileSize.TALL:
          return {rows: [0, 1], cols: [pointer.col]};
        case TileSize.WIDE:
          pointer.col = pointer.col + 1;
          return {rows: [pointer.row], cols: [pointer.col - 1, pointer.col]};
        case TileSize.X_WIDE:
          pointer.col = pointer.col + 2;
          return {rows: [pointer.row], cols: [pointer.col - 2, pointer.col - 1, pointer.col]};
        case TileSize.X_LARGE:
          pointer.col = pointer.col + 2;
          return {rows: [0, 1], cols: [pointer.col - 2, pointer.col - 1, pointer.col]};
        default:
          return {rows: [pointer.row], cols: [pointer.col]};
      }
    }

    isPositionAlreadyTakenByOtherExtendedTile(allTiles, row, column) {
      return allTiles
        .filter(tile => !tile.isRegular)
        .some(tile => tile.position.rows.includes(row) && tile.position.cols.includes(column));
    }
  }

  AGN.Lib.Dashboard.DashboardGrid = DashboardGrid;
})();
