(function () {
  const Def = AGN.Lib.Dashboard.Definitions;

  const GridUtils = {

    setCurrentSchema: function (tiles) {
      if (!tiles) {
        tiles = this.getTiles();
      }

      Def.LAYOUT.CURRENT.SCHEMA = tiles.map(({ id, position }) => ({ id, position }));
    },

    getColsCount: function() {
      return Def.LAYOUT.CURRENT.COLS_COUNT;
    },

    $getTiles: function () {
      return $('.draggable-tile');
    },

    getTiles: function () {
      return this.$getTiles().toArray().map($tile => $($tile).data('tile'));
    },

    removeAllTiles: function () {
      this.$getTiles().each((i, tile) => $(tile).data('tile').remove());
    },

    getTileAtPosition: function (row, col, tiles) {
      if (!tiles) {
        tiles = this.getTiles();
      }
      return tiles.find(tile => tile.position.rows.includes(row) && tile.position.cols.includes(col));
    },

    updateTilesPositions: function () {
      const displayedTiles = this.getTiles();
      const pointer = {row: 0, col: -1}

      displayedTiles.forEach(tile => tile.position = {rows: [], cols: []}); // clear current positions
      displayedTiles.forEach(tile => tile.position = calculateTilePositionInGrid(displayedTiles, tile.type, pointer)); // set new positions
    },

    getTile: function ($tile) {
      return $tile.data('tile');
    },

    switchNotAllowedDroppables: function ($draggable, on) {
      this._getNotAllowedDroppablesToSwitch(this.getTile($draggable)).forEach(tile => tile.switchDroppable(on));
    },

    _getNotAllowedDroppablesToSwitch: function(draggableTile) {
      if (draggableTile.isRegular) {
        return this.getTallTiles();
      }
      if (draggableTile.isTall) {
        return this.getTilesUnderOrAboveWideTiles();
      }
      if (draggableTile.isWide) {
        return [...this.getTilesLockedByTallTiles(), ...this.getTallTiles()];
      }
    },

    setDraggable: function (tile) {
      const self = this;

      tile.$el.draggable({
        containment: Def.TILES_CONTAINER,
        opacity: 0.7,
        zIndex: 1000,
        snap: true,
        snapMode: "inner",
        revert: 'invalid',
        cursor: 'move',
        scroll: false,
        helper: function (e) { // fix draggable clone dimentions
          const $target = $(e.currentTarget);
          return $target.clone(true).css({'animation': 'none', 'width': $target.width(), 'height': $target.height()});
        },
        start() {
          $(this).css({opacity: 0})   // hide original tile. clone is displayed while dragging
          self.switchNotAllowedDroppables($(this), false);
        },
        stop() {
          $(this).css({opacity: 1});  // show original tile back
          self.switchNotAllowedDroppables($(this), true);
        }
      });
    },

    enableDraggable: function (enable) {
      this.$getTiles().draggable("option", "disabled", !enable);
    },

    setDroppable: function (tile) {
      if (tile.$el.is('.tile-wide')) {
        return;
      }
      const self = this;
      tile.$el.droppable({
        tolerance: 'pointer',
        activeClass: 'draggable-accept',
        drop: async function (event, ui) {
          const draggableTile = self.getTile(ui.draggable);
          const droppableTile = self.getTile($(this));
          await self.swapTiles(draggableTile, droppableTile);
          self.updateTilesPositions();
          self.setCurrentSchema();
        }
      });
    },

    swapTiles: async function (draggableTile, droppableTile) {
      switch (draggableTile.type) {
        case Def.TILE.TYPE.TALL:
          moveTallTile(draggableTile, droppableTile);
          break;
        case Def.TILE.TYPE.WIDE:
          moveWideTile(draggableTile, droppableTile);
          break;
        default:
          draggableTile.swap(droppableTile);
          break;
      }
    },

    // [T][ ][ ][*]    [ ][ ][T][ ]    [T][T][ ][*]
    // [T][ ][ ][ ]    [ ][*][T][ ]    [T][T][ ][ ]
    //  ^                     ^            ^
    getPreviousTileOfTallTileBottomPart: function(tileCol) {
      if (tileCol === 0) {                                              // if it's first column
        return this.getTileAtPosition(0, GridUtils.getColsCount() - 1); // get last tile in prev row
      }
      if (this.getTileAtPosition(1, tileCol - 1).isTall) {              // if prev tile is also tall
        return this.getPreviousTileOfTallTileBottomPart(tileCol - 1);   // select previous tile of this prev tall tile
      }
      return this.getTileAtPosition(1, tileCol - 1);
    },

    // [ ][*][*][ ]    [ ][ ][  W ]
    // [ ][  W ][ ]    [ ][ ][*][*]
    getTilesUnderOrAboveWideTiles: function () {
      return this.getTiles()
        .filter(tile => tile.isWide)
        .map(tile => {
          const row = tile.beginRow === 0 ? 1 : 0;
          return [
            this.getTileAtPosition(row, tile.beginCol),
            this.getTileAtPosition(row, tile.endCol)
          ]
        })
        .flat();
    },

    // tiles in first or last column that can't be replaced with a wide tile
    // [*][T][ ][ ]    [ ][ ][T][*]
    // [*][T][ ][ ]    [ ][ ][T][*]
    getTilesLockedByTallTiles: function () {
      return this.getTiles()
        .filter(tile => tile.isRegular &&
          (tile.beginCol === 0 && this.getTileAtPosition(tile.beginRow, 1).isTall)
          || (tile.beginCol === this.getColsCount() - 1 && this.getTileAtPosition(tile.beginRow, this.getColsCount() - 2).isTall));
    },

    getTallTiles: function() {
      return this.getTiles().filter(tile => tile.isTall);
    }
  }

  function moveTallTile(draggableTile, droppableTile) {
    const dragCol = draggableTile.beginCol;
    const dropCol = droppableTile.beginCol;
    const topTile = GridUtils.getTileAtPosition(0, dropCol);
    const bottomTile = GridUtils.getTileAtPosition(1, dropCol);
    const prevTileOfDraggableBottom = GridUtils.getPreviousTileOfTallTileBottomPart(dragCol);

    if (topTile.$el.is(prevTileOfDraggableBottom.$el)) {
      prevTileOfDraggableBottom.$el.after(bottomTile.$el); // swap bottom part
      draggableTile.swap(topTile); // swap top part
    } else {
      draggableTile.swap(topTile); // swap top part
      if (!topTile.isTall) {
        prevTileOfDraggableBottom.$el.after(bottomTile.$el); // swap bottom part
      }
    }
  }

  function moveWideTile(draggableTile, droppableTile) {
    if (draggableTile.beginRow === droppableTile.beginRow) {
      moveWideTileAtSameRow(draggableTile, droppableTile);
    } else {
      moveWideTileToAnotherRow(draggableTile, droppableTile);
    }
  }

  function moveWideTileAtSameRow(draggableTile, droppableTile) {
    const dragCol = draggableTile.beginCol;
    const dragRow = draggableTile.beginRow;

    if (droppableTile.beginCol === dragCol - 2) {
      GridUtils.getTileAtPosition(dragRow, dragCol - 2).$el.before(draggableTile.$el);
    } else if (droppableTile.beginCol === dragCol + 3) {
      GridUtils.getTileAtPosition(dragRow, dragCol + 3).$el.after(draggableTile.$el);
    } else {
      draggableTile.swap(droppableTile);
    }
  }

  function replaceWideTileWithDroppableAndNext(draggableTile, droppableTile, nextTileOfDroppable) {
    draggableTile.swap(droppableTile);
    droppableTile.$el.after(nextTileOfDroppable.$el);
  }

  function replaceWideTileWithDroppableAndPrev(draggableTile, droppableTile, prevTileOfDroppable) {
    draggableTile.swap(prevTileOfDroppable);
    prevTileOfDroppable.$el.after(droppableTile.$el);
  }

  function isFirstCol(col) {
    return col === 0;
  }

  function isLastCol(col) {
    return col === GridUtils.getColsCount() - 1;
  }

  function moveWideTileToAnotherRow(draggableTile, droppableTile) {
    const dropCol = droppableTile.beginCol;
    const dropRow = droppableTile.beginRow;
    const nextTileOfDroppable = GridUtils.getTileAtPosition(dropRow, dropCol + 1);
    const prevTileOfDroppable = GridUtils.getTileAtPosition(dropRow, dropCol - 1);

    if (!isFirstCol(dropCol) && prevTileOfDroppable.isRegular) {
      replaceWideTileWithDroppableAndPrev(draggableTile, droppableTile, prevTileOfDroppable);
    } else if (!isLastCol(dropCol) && nextTileOfDroppable.isRegular) {
      replaceWideTileWithDroppableAndNext(draggableTile, droppableTile, nextTileOfDroppable);
    }
  }

  function calculateTilePositionInGrid(allTiles, tileType, pointer) {
    pointer.col = pointer.col + 1;
    if (pointer.col >= GridUtils.getColsCount()) {
      pointer.row = pointer.row + 1;
      pointer.col = 0;
    }
    while (isPositionAlreadyTakenByOtherExtendedTile(allTiles, tileType, pointer.row, pointer.col)) {
      pointer.col = pointer.col + 1;
    }

    switch (tileType) {
      case Def.TILE.TYPE.TALL:
        return {rows: [0, 1], cols: [pointer.col]};
      case Def.TILE.TYPE.WIDE:
        pointer.col = pointer.col + 1;
        return {rows: [pointer.row], cols: [pointer.col - 1, pointer.col]};
      default:
        return {rows: [pointer.row], cols: [pointer.col]};
    }
  }

  function isPositionAlreadyTakenByOtherExtendedTile(allTiles, tileType, row, column) {
    if (tileType !== Def.TILE.TYPE.TALL) {
      return allTiles
        .filter(tile => tile.isTall)
        .some(tile => tile.position.rows.includes(row) && tile.position.cols.includes(column));
    }
    if (tileType !== Def.TILE.TYPE.WIDE) {
      return allTiles
        .filter(tile => tile.isWide)
        .some(tile => tile.position.rows.includes(row) && tile.position.cols.includes(column));
    }
    return false;
  }

  AGN.Lib.Dashboard.GridUtils = GridUtils;
})();
