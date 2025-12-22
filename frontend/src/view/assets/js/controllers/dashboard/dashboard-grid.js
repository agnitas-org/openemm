(() => {
  const TileSize = AGN.Lib.Dashboard.Def.TileSize;

  class DashboardGrid {

    constructor(rows, cols, tiles) {
      this.rows = rows;
      this.cols = cols;
      this.swapper = new AGN.Lib.Dashboard.TileSwapper(this);
      this.$container = $('#dashboard-tiles');
      this.$container.on('agn:grid-modified', (e, tile) => this.#onGridTileModified(tile));
      this.#fillGrid(tiles);
    }

    #fillGrid(tiles) {
      this.#sortTilesByPosition(tiles)
      this.#removeAllTiles();
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

    #removeAllTiles() {
      this.$tiles.each((i, tile) => $(tile).data('tile').remove());
    }

    get visibleTiles() {
      return this.tiles.filter(tile => tile.$el.css("visibility") !== "hidden");
    }

    get schema() {
      return {
        COLS_COUNT: this.cols,
        SCHEMA: this.tiles.map(({id, position}) => ({id, position}))
      };
    }

    /**
     * Checks if the tile fits in the specified coordinates without going beyond the grid.
     * Does not check if the cells are occupied.
     */
    #canFitInBounds(tileSize, row, col) {
      if (row < 0 || col < 0) {
        return false;
      }

      const [tileRows, tileCols] = tileSize.dimensions;
      for (let rowOffset = 0; rowOffset < tileRows; rowOffset++) {
        for (let colOffset = 0; colOffset < tileCols; colOffset++) {
          let newRow = row + rowOffset;
          let newCol = col + colOffset;
          if (newRow >= this.rows || newCol >= this.cols) {
            return false;
          }
        }
      }
      return true;
    }

    #onGridTileModified(tile) {
      this.updateTilesPositions();
      if (tile) {
        this.swapper.initDragAndDrop(tile);
      }
    }

    #getInterferingTiles(height, width, row, col) {
      const tiles = _.flatMap(_.range(height), rowOffset => _.range(width).map(colOffset =>
          this.getTile(row + rowOffset, col + colOffset)
        ).filter(Boolean)
      )
      return _.uniq(tiles);
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

    replaceTile(tile, newTile) {
      const position = this.#findBestPositionToReplace(tile, newTile.size);
      this.#placeTileAtPos(newTile, position[0], position[1]);
    }

    /**
     * Finds best start position to place new tile when replacing another tile.
     * It compares positions by count of interfering tiles ignoring empty tiles
     */
    #findBestPositionToReplace(tile, newTileSize) {
      const positions = this.#getPossibleStartPositionsToReplace(tile, newTileSize);

      let bestPosition = null;
      let interferingTilesCount = 0;

      for (let i = 0; i < positions.length; i++) {
        const pos = positions[i];
        const interferingTiles = this.#findInterferingTiles(newTileSize, pos[0], pos[1])
          .filter(t => !t.isEmpty);

        if (interferingTiles.length < interferingTilesCount || bestPosition === null) {
          interferingTilesCount = interferingTiles.length;
          bestPosition = pos;
        }
      }

      return bestPosition;
    }

    #getPossibleStartPositionsToReplace(tile, newTileSize) {
      const positions = [];

      for (let row = tile.beginRow - newTileSize.dimensions[0] + 1; row <= tile.beginRow + tile.size.dimensions[0] - 1; row++) {
        for (let col = tile.beginCol - newTileSize.dimensions[1] + 1; col <= tile.beginCol + tile.size.dimensions[1] - 1; col++) {
          if (this.#canFitInBounds(newTileSize, row, col)) {
            positions.push([row, col]);
          }
        }
      }
      return positions;
    }

    findInterferingTilesToReplace(tile, newTileSize) {
      const position = this.#findBestPositionToReplace(tile, newTileSize);
      return this.#findInterferingTiles(newTileSize, position[0], position[1]);
    }

    #placeTileAtPos(tile, row, col) {
      this.#findInterferingTiles(tile.size, row, col)
        .filter(tile => tile.size !== TileSize.REGULAR)
        .forEach(tile => this.replaceWithEmptyTile(tile));

      this.#findInterferingTiles(tile.size, row, col)
        .forEach(tile => tile.remove());

      this.insertTile(tile, row, col);
    }

    #findInterferingTiles(tileSize, row, col) {
      const [height, width] = tileSize.dimensions;
      return this.#getInterferingTiles(height, width, row, col);
    }

    #createEmptyTile() {
      return new AGN.Lib.Dashboard.EmptyTile();
    }

    /**
     * Remove tile from the screen and replace it with empty tile(s)
     */
    replaceWithEmptyTile(tile) {
      if (!tile) {
        return;
      }
      tile.remove();

      const {rows, cols} = tile.position;
      rows.forEach(row => cols.forEach(col => this.insertTile(this.#createEmptyTile(), row, col)));
    }

    updateTilesPositions() {
      const tiles = this.tiles;
      const pointer = {row: 0, col: -1}
      tiles.forEach(tile => tile.position = {rows: [], cols: []}); // clear current positions
      tiles.forEach(tile => tile.position = this.#calculateTilePositionInGrid(tiles, tile.size, pointer)); // set new positions
    }

    #calculateTilePositionInGrid(tiles, tileSize, pointer) {
      pointer.col = pointer.col + 1;
      if (pointer.col >= this.cols) {
        pointer.row = pointer.row + 1;
        pointer.col = 0;
      }
      while (this.#isPositionAlreadyTakenByOtherExtendedTile(tiles, pointer.row, pointer.col)) {
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

    #isPositionAlreadyTakenByOtherExtendedTile(allTiles, row, column) {
      return allTiles
        .filter(tile => !tile.isRegular)
        .some(tile => tile.position.rows.includes(row) && tile.position.cols.includes(column));
    }
  }

  AGN.Lib.Dashboard.DashboardGrid = DashboardGrid;
})();
