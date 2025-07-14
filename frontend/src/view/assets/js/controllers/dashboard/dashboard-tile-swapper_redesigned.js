(() => {
  const TileSize = AGN.Lib.Dashboard.Def.TileSize;

  class TileSwapper {

    constructor(grid) {
      this.grid = grid;
    }

    toggle(enable) {
      this.grid.$tiles.draggable("option", "disabled", !enable);
    }

    initDragAndDrop(tile) {
      this.setDraggable(tile);
      this.setDroppable(tile);
    }

    setDraggable(tile) {
      const self = this;

      tile.$el.draggable({
        containment: this.grid.$container,
        opacity: 0.7,
        zIndex: 1000,
        cursor: 'move',
        scroll: false,
        appendTo: 'body',
        helper: function (e) { // fix draggable clone dimensions
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
    }

    switchNotAllowedDroppables($draggable, on) {
      this._getNotAllowedDroppablesToSwitch(AGN.Lib.Dashboard.DraggableTile.get($draggable)).forEach(tile => tile.switchDroppable(on));
    }

    _getNotAllowedDroppablesToSwitch(draggableTile) {
      if (draggableTile.isRegular) {
        return this.grid.tiles.filter(tile => !tile.isRegular);
      }
      if (draggableTile.isTall) {
        return [...this.getTilesUnderOrAboveWideTiles(), ...this.grid.wideTiles, ...this.grid.xWideTiles];
      }
      if (draggableTile.isWide) {
        return [...this.getTilesLockedByTallTiles(), ...this.grid.tallTiles, ...this.grid.xWideTiles];
      }
      if (draggableTile.isXWide) {
        return [...this.grid.tallTiles, ...this.grid.xlTiles];
      }
      return [];
    }

    setDroppable(tile) {
      const self = this;
      tile.$el.droppable({
        accept: '.draggable-tile',
        tolerance: 'pointer',
        activeClass: 'draggable-accept',
        over: function (event, ui) {
          self.unhighlightAll();
          const draggableTile = AGN.Lib.Dashboard.DraggableTile.get(ui.draggable);
          const droppableTile = AGN.Lib.Dashboard.DraggableTile.get($(this));
          self.getReadyToSwapTiles(draggableTile, droppableTile).forEach(tile => tile.toggleHighlight(true));
        },
        drop: async function (event, ui) {
          const draggableTile = AGN.Lib.Dashboard.DraggableTile.get(ui.draggable);
          const droppableTile = AGN.Lib.Dashboard.DraggableTile.get($(this));
          await self.swapTiles(draggableTile, droppableTile);
          self.grid.updateTilesPositions();
        },
        deactivate: () => self.unhighlightAll(),
      });
    }

    getReadyToSwapTiles(draggableTile, droppableTile) {
      switch (draggableTile.size) {
        case TileSize.TALL:
        case TileSize.X_LARGE:
          return this.getTilesHoveredByTallTile(droppableTile);
        case TileSize.WIDE:
          return this.getTilesHoveredByWideTile(draggableTile, droppableTile);
          case TileSize.X_WIDE:
          return this.getTilesHoveredByExtraWideTile(draggableTile, droppableTile);
        default:
          return [droppableTile];
      }
    }

    getTilesHoveredByTallTile(droppableTile) {
      const { beginCol } = droppableTile;
      const tiles = [this.grid.getTile(0, beginCol)];
      const bottomTile = this.grid.getTile(1, beginCol);

      if (!tiles[0].isTall) {
        tiles.push(bottomTile);
      }
      return tiles;
    }

    getTilesHoveredByWideTile(draggableTile, droppableTile) {
      return draggableTile.beginRow === droppableTile.beginRow
        ? this.getTilesHoveredByWideTileAtSameRow(draggableTile, droppableTile)
        : this.getTilesHoveredByWideTileAtAnotherRow(draggableTile, droppableTile);
    }

    getTilesHoveredByWideTileAtSameRow(draggableTile, droppableTile) {
      const { beginCol: dragCol, beginRow: dragRow } = draggableTile;

      if (droppableTile.beginCol === dragCol - 2 || droppableTile.beginCol === dragCol + 3) {
        return [droppableTile, this.grid.getTile(dragRow, droppableTile.beginCol)];
      }
      return [droppableTile];
    }

    getTilesHoveredByWideTileAtAnotherRow(draggableTile, droppableTile) {
      const { beginCol: dropCol, beginRow: dropRow } = droppableTile;
      const nextTileOfDroppable = this.grid.getTile(dropRow, dropCol + 1);
      const prevTileOfDroppable = this.grid.getTile(dropRow, dropCol - 1);

      if (!this.isFirstCol(dropCol) && prevTileOfDroppable.isRegular) {
        return [droppableTile, prevTileOfDroppable];
      } else if (!this.isLastCol(dropCol) && nextTileOfDroppable.isRegular) {
        return [droppableTile, nextTileOfDroppable];
      }
      return [];
    }

    getTilesHoveredByExtraWideTile(draggableTile, droppableTile) {
      return draggableTile.beginRow === droppableTile.beginRow
        ? [droppableTile] // tile hovered by extra wide tile at same row
        : this.getTilesHoveredByExtraWideTileAtAnotherRow(draggableTile, droppableTile);
    }

    getTilesHoveredByExtraWideTileAtAnotherRow(draggableTile, droppableTile) {
      const { beginCol: dropCol, beginRow: dropRow } = droppableTile;
      const nextTileOfDroppable = this.grid.getTile(dropRow, dropCol + 1);
      const secondNextTileOfDroppable = this.grid.getTile(dropRow, dropCol + 2);
      const prevTileOfDroppable = this.grid.getTile(dropRow, dropCol - 1);
      const secondPrevTileOfDroppable = this.grid.getTile(dropRow, dropCol - 2);

      let hoveredTiles;
      if (this.isFirstCol(dropCol)) {
        hoveredTiles = [droppableTile, nextTileOfDroppable, secondNextTileOfDroppable]
      } else if (this.isLastCol(dropCol)) {
        hoveredTiles = [secondPrevTileOfDroppable, prevTileOfDroppable, droppableTile]
      } else {
        hoveredTiles = draggableTile.position.cols.map(col => this.grid.getTile(dropRow, col));
      }
      hoveredTiles = [...new Set(hoveredTiles)];
      const regularCount = hoveredTiles.filter(tile => tile.isRegular).length;
      const wideCount = hoveredTiles.filter(tile => tile.isWide).length;
      return regularCount === 3 || (regularCount === 1 && wideCount === 1) ? hoveredTiles : [];
    }

    unhighlightAll() {
      this.grid.tiles.forEach(tile => tile.toggleHighlight(false));
    }

    async swapTiles (draggableTile, droppableTile) {
      switch (draggableTile.size) {
        case TileSize.TALL:
          this.moveTallTile(draggableTile, droppableTile);
          break;
        case TileSize.WIDE:
          this.moveWideTile(draggableTile, droppableTile);
          break;
        case TileSize.X_WIDE:
          this.moveExtraWideTile(draggableTile, droppableTile);
          break;
        case TileSize.X_LARGE:
          this.moveXlTile(draggableTile, droppableTile);
          break;
        default:
          draggableTile.swap(droppableTile);
          break;
      }
    }

    moveTallTile(draggableTile, droppableTile) {
      const dragCol = draggableTile.beginCol;
      const dropCol = droppableTile.beginCol;
      const topTile = this.grid.getTile(0, dropCol);
      const bottomTile = this.grid.getTile(1, dropCol);
      const prevTileOfDraggableBottom = this.getPreviousTileOfTallTileBottomPart(dragCol);

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

    // [T][ ][ ][*]    [ ][ ][T][ ]    [T][T][ ][*]
    // [T][ ][ ][ ]    [ ][*][T][ ]    [T][T][ ][ ]
    //  ^                     ^            ^
    getPreviousTileOfTallTileBottomPart(tileCol) {
      if (this.isFirstCol(tileCol)) {
        return this.grid.getTile(0, this.grid.cols - 1); // get last tile in prev row
      }
      if (this.grid.getTile(1, tileCol - 1).isTall) {              // if prev tile is also tall
        return this.getPreviousTileOfTallTileBottomPart(tileCol - 1);   // select previous tile of this prev tall tile
      }
      return this.grid.getTile(1, tileCol - 1);
    }

    // [ ][*][*][ ]    [ ][ ][  W ]
    // [ ][  W ][ ]    [ ][ ][*][*]
    getTilesUnderOrAboveWideTiles() {
      return [...this.grid.wideTiles, ...this.grid.xWideTiles]
        .map(tile => {
          const row = tile.beginRow === 0 ? 1 : 0;
          return tile.position.cols.map(col => this.grid.getTile(row, col))
        }).flat();
    }

    // tiles in first or last column that can't be replaced with a wide tile
    // [*][T][ ][ ]    [ ][ ][T][*]
    // [*][T][ ][ ]    [ ][ ][T][*]
    getTilesLockedByTallTiles() {
      return this.grid.tiles
        .filter(tile => tile.isRegular &&
          (this.isFirstCol(tile.beginCol) && this.grid.getTile(tile.beginRow, 1).isTall)
          || (this.isLastCol(tile.beginCol) && this.grid.getTile(tile.beginRow, this.grid.cols - 2).isTall));
    }

    moveWideTile(draggableTile, droppableTile) {
      if (draggableTile.beginRow === droppableTile.beginRow) {
        this.moveWideTileAtSameRow(draggableTile, droppableTile);
      } else {
        this.moveWideTileToAnotherRow(draggableTile, droppableTile);
      }
    }

    moveWideTileAtSameRow(draggableTile, droppableTile) {
      const { beginCol: dragCol, beginRow: dragRow } = draggableTile;

      if (droppableTile.beginCol === dragCol - 2) {
        this.grid.getTile(dragRow, dragCol - 2).$el.before(draggableTile.$el);
      } else if (droppableTile.beginCol === dragCol + 3) {
        this.grid.getTile(dragRow, dragCol + 3).$el.after(draggableTile.$el);
      } else {
        draggableTile.swap(droppableTile);
      }
    }

    moveWideTileToAnotherRow(draggableTile, droppableTile) {
      const { beginCol: dropCol, beginRow: dropRow } = droppableTile;
      const nextTileOfDroppable = this.grid.getTile(dropRow, dropCol + 1);
      const prevTileOfDroppable = this.grid.getTile(dropRow, dropCol - 1);

      if (!this.isFirstCol(dropCol) && prevTileOfDroppable.isRegular) {
        this.replaceWideTileWithDroppableAndPrev(draggableTile, droppableTile, prevTileOfDroppable);
      } else if (!this.isLastCol(dropCol) && nextTileOfDroppable.isRegular) {
        this.replaceWideTileWithDroppableAndNext(draggableTile, droppableTile, nextTileOfDroppable);
      }
    }

    replaceWideTileWithDroppableAndNext(draggableTile, droppableTile, nextTileOfDroppable) {
      draggableTile.swap(droppableTile);
      droppableTile.$el.after(nextTileOfDroppable.$el);
    }

    replaceWideTileWithDroppableAndPrev(draggableTile, droppableTile, prevTileOfDroppable) {
      draggableTile.swap(prevTileOfDroppable);
      prevTileOfDroppable.$el.after(droppableTile.$el);
    }

    moveExtraWideTile(draggableTile, droppableTile) {
      if (draggableTile.beginRow === droppableTile.beginRow) {
        draggableTile.swap(droppableTile); // move extra wide tile at same row
      } else {
        this.moveExtraWideTileToAnotherRow(draggableTile, droppableTile);
      }
    }

    moveExtraWideTileToAnotherRow(draggableTile, droppableTile) {
      const [first, second, third] = this.getTilesHoveredByExtraWideTile(draggableTile, droppableTile);
      if (first) {
        draggableTile.swap(first);
      }
      if (second) {
        first.$el.after(second.$el);
      }
      if (third) {
        second.$el.after(third.$el);
      }
    }

    moveXlTile(draggableTile, droppableTile) {
      const dropCol = droppableTile.beginCol;
      const topTile = this.grid.getTile(0, dropCol);
      const bottomTile = this.grid.getTile(1, dropCol);

      if (this.isLastCol(dropCol)) {
        draggableTile.$el.before(topTile.$el); // swap top part
      } else {
        draggableTile.$el.after(topTile.$el); // swap top part
      }
      if (!topTile.isTall) {
        (this.isLastCol(dropCol) ? draggableTile.$el : topTile.$el).after(bottomTile.$el); // swap bottom part
      }
    }

    isFirstCol(col) {
      return col === 0;
    }

    isLastCol(col) {
      return col === this.grid.cols - 1;
    }
  }

  AGN.Lib.Dashboard.TileSwapper = TileSwapper;
})();
