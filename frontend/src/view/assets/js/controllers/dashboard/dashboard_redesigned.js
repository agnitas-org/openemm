AGN.Lib.Controller.new('dashboard', function() {

  const Modal = AGN.Lib.Modal;
  const GridUtils = AGN.Lib.Dashboard.GridUtils;
  const Def = AGN.Lib.Dashboard.Definitions;

  let tiles = [
    new MailingsTile(this),
    new PlanningTile(this),
    new StatisticsTile(this),
    new WorkflowsTile(this),
    new ImportExportTile(this),
    new AddOnsTile(this),
    new NewsTile(this),
    new CalendarTile(this)
  ];

  let placeholderTile; // tile with pressed + button to be replaced with chosen one

  this.addDomInitializer('dashboard-view', function() {
    Def.LAYOUT.CURRENT = JSON.parse(this.config.layout) || Def.LAYOUT.DEFAULT;
    tiles = tiles.filter(tile=> tile.allowed());

    Def.TILES_CONTAINER = $('#dashboard-tiles');
    drawTiles();
    preLoadImagesForChooseTileModal();
    infoIfDashboardEmpty();

    $(window).on("displayTypeChanged", (e, isMobileView) => {
      if (isMobileView) {
        toggleEditMode(false);
      }
    });
  });

  function infoIfDashboardEmpty() {
    if (!getVisibleTiles().length) {
      AGN.Lib.Messages(t('defaults.info'), t('dashboard.empty'), 'info');
    }
  }

  function getVisibleTiles() {
    return GridUtils.getTiles().filter(tile => tile.$el.css("visibility") !== "hidden");
  }

  function preLoadImagesForChooseTileModal() {
    const modal = _.template(AGN.Opt.Templates['dashboard-tiles-selection-modal'])({tiles: tiles});
    preloadImages($(modal));
  }

  function preloadImages($el) {
    $el.find('img').each(function () {
      const img = new Image();
      img.src = $(this).attr('src');
    });
  }

  function getUserTiles() {
    const schema = Def.LAYOUT.CURRENT.SCHEMA;
    if (schema === Def.LAYOUT.DEFAULT.SCHEMA) {
      return schemaToTiles(schema);
    }

    schema.sort((a, b) => {
      const aRow = a.position.rows[0];
      const aCol = a.position.cols[0];
      const bRow = b.position.rows[0];
      const bCol = b.position.cols[0];

       if (aRow !== bRow) {
         return aRow - bRow;
       }
       return aCol - bCol;
    });
    return schemaToTiles(schema);
  }

  function getTypeByTileDescription(tileDescr, defaultVal) {
    if (tileDescr.position?.rows?.length > 1) {
      return Def.TILE.TYPE.TALL;
    }
    if (tileDescr.position?.cols?.length > 1) {
      return Def.TILE.TYPE.WIDE;
    }
    return defaultVal;
  }

  function schemaToTiles(tilesDesr) {
    return tilesDesr.map(tileDescr => {
      const tile = getTileById(tileDescr.id)
      tile.type = getTypeByTileDescription(tileDescr, tile.type);
      return tile;
    })
  }

  function drawTiles() {
    GridUtils.removeAllTiles();
    updateGridColsCount(GridUtils.getColsCount());
    getUserTiles().forEach(tile => tile.displayOnScreen(Def.TILES_CONTAINER));
    GridUtils.saveTilesLayout();
  }

  function updateGridColsCount(colsCount) {
    Def.TILES_CONTAINER.removeClass(function(index, className) {
      return (className.match(/\bdashboard-grid-cols-\S+/g) || []).join(' ');
    });
    Def.TILES_CONTAINER.addClass(`dashboard-grid-cols-${colsCount}`);
  }

  this.addAction({click: 'delete-tile'}, function() {
    const $tileToDelete = this.el.closest('.draggable-tile');
    const tileToDelete = getTile($tileToDelete);

    if (tileToDelete.isTall) {
      createEmptyTileBeforeTallTileBottom(tileToDelete);
    }
    if (tileToDelete.isWide) {
      createEmptyTileAtRightOfWideTile(tileToDelete);
    }
    tileToDelete.replaceWith(new EmptyTile());
    tileToDelete.remove();

    GridUtils.saveTilesLayout();
  });

  function createEmptyTileBeforeTallTileBottom(tileToDelete) {
    const $secondPartContainer = $('<div class="draggable-tile">');
    const tileToDeleteCol = tileToDelete.beginCol;
    const emptyTile = new EmptyTile();
    const bottomPlaceholderPrev = GridUtils.getPreviousTileOfTallTileBottomPart(tileToDeleteCol);

    bottomPlaceholderPrev.$el.after($secondPartContainer);
    emptyTile.displayOnScreen($secondPartContainer);
  }

  function createEmptyTileAtRightOfWideTile(tileToDelete) {
    const $secondPartContainer = $('<div class="draggable-tile">');
    tileToDelete.$el.after($secondPartContainer);
    new EmptyTile().displayOnScreen($secondPartContainer);
  }

  this.addAction({click: 'select-tile'}, function() {
    placeholderTile = getTile(this.el.closest('.draggable-tile'));
    Modal.createFromTemplate({tiles: getAllowedToAddTiles()}, 'dashboard-tiles-selection-modal')
  });

  function getAllowedToAddTiles() {
    const displayedTiles = GridUtils.getTiles().map(tile => tile.id);
    return tiles.filter(function (tile) {
      return tile.id !== Def.TILE.ID.EMPTY && !displayedTiles.includes(tile.id);
    }).map(tile => {
      tile.variants.forEach(variant => variant.disabled = !allowedToAddTile(variant.type));
      return tile;
    });
  }

  this.addAction({click: 'add-tile'}, function() {
    const tileToAdd = getTileById(this.el.data('tile-id'));
    const tileType = this.el.data('type');
    if (tileType) {
      tileToAdd.type = tileType;
    }

    if (!allowedToAddTile(tileType)) {
      return;
    }
    if (tileToAdd.isTall) {
      removeSecondPlaceholderOfTallTile();
    }
    if (tileToAdd.isWide) {
      removeSecondPlaceholderOfWideTile();
    }
    placeholderTile.replaceWith(tileToAdd);
    GridUtils.saveTilesLayout();
  });

  function removeSecondPlaceholderOfTallTile() {
    const placeholderCol = placeholderTile.beginCol
    const topPlaceholderTile = getTileAtPosition(0, placeholderCol);
    const bottomPlaceholderTile = getTileAtPosition(1, placeholderCol);
    placeholderTile = topPlaceholderTile; // move placeholder to top part to be replaced later by a new tile
    bottomPlaceholderTile.remove();
  }

  function removeSecondPlaceholderOfWideTile() {
    const placeholderCol = placeholderTile.beginCol;
    const placeholderRow = placeholderTile.beginRow;
    const placeholderTileNext = getTileAtPosition(placeholderRow, placeholderCol + 1);
    const placeholderTilePrev = getTileAtPosition(placeholderRow, placeholderCol - 1);

    if (placeholderTileNext && placeholderTileNext.isEmpty) {
      placeholderTileNext.remove();
      return;
    }
    if (placeholderTilePrev && placeholderTilePrev.isEmpty) {
      placeholderTile.remove();
      placeholderTile = placeholderTilePrev;
    }
  }

  function allowedToAddTile(type) {
    switch (type) {
      case Def.TILE.TYPE.TALL:
        return isAllowedToAddTallTile();
      case Def.TILE.TYPE.WIDE:
        return isAllowedToAddWideTile();
      default:
        return true;
    }
  }

  function isAllowedToAddTallTile() {
    const placeholderCol = placeholderTile.beginCol;
    const topPlaceholderTile = getTileAtPosition(0, placeholderCol);
    const bottomPlaceholderTile = getTileAtPosition(1, placeholderCol);
    return topPlaceholderTile.isEmpty && bottomPlaceholderTile.isEmpty;
  }

  function isAllowedToAddWideTile() {
    const placeholderRow = placeholderTile.beginRow;
    const placeholderCol = placeholderTile.beginCol;
    const prevTile = getTileAtPosition(placeholderRow, placeholderCol - 1);
    const nextTile = getTileAtPosition(placeholderRow, placeholderCol + 1);

    return (prevTile && prevTile.isEmpty) || (nextTile && nextTile.isEmpty);
  }

  this.addAction({change: 'redraw-mailing-statistics'}, function() {
    const $tile = this.el.closest('.draggable-tile');
    const tile = getTile($tile);
    tile.updateChart();
  });

  function getTileById(id) {
    return tiles[tiles.findIndex((tile) => tile.id === id)] || new EmptyTile();
  }

  function getTile($tile) {
    return GridUtils.getTile($tile);
  }

  function getTileAtPosition(row, col, tiles) {
    return GridUtils.getTileAtPosition(row, col, tiles);
  }

  this.addAction({click: 'edit-dashboard'}, function() {
    toggleEditMode(true);
    const $popup = this.el.closest('.popup');
    if ($popup.length) {
      $(':focus', $popup).blur();
      toastr.clear($popup);
    }
  });

  this.addAction({click: 'stop-editing'}, function() {
    toggleEditMode(false);
    infoIfDashboardEmpty();
  });

  function toggleEditMode(on) {
    $('#dashboard-start-edit-btn').closest('li').toggle(!on);
    $('body').toggleClass('edit-mode', !!on);
  }

  this.addAction({click: 'select-layout'}, function() {
    Modal.createFromTemplate({}, 'dashboard-layout-selection-modal');
  });

  function isGridLeftOver(tile, newColsCount) {
    switch (newColsCount) {
      case 3: // skip last column
        return tile.position.cols.includes(3);
      case 2: // skip last two columns
        return tile.position.cols.includes(2) || tile.position.cols.includes(3);
      default:
        return false;
    }
  }

  function getTilesForSmallerLayout(newColsCount) {
    let tiles = GridUtils.getTiles().map(tile => {
      if (tile.isWide && isGridLeftOver(tile, newColsCount)) {
        const emptyTile = new EmptyTile();
        emptyTile.position = {cols: [tile.beginCol], rows: [tile.beginRow] };
        return emptyTile;
      }
      return tile;
    });
    return tiles.filter(tile => !isGridLeftOver(tile, newColsCount));
  }

  function insertEmptyColumn(tiles, col) {
    for (let i = 0; i < Def.LAYOUT.CURRENT.ROWS_COUNT; i++) {
      const emptyTile = new EmptyTile();
      emptyTile.position = {rows: [i], cols:[col]};
      tiles.push(emptyTile);
    }
  }

  function getTilesForBiggerLayout(newColsCount) {
    const currentColsCount = GridUtils.getColsCount();
    const displayedTiles = GridUtils.getTiles();

    for (let i = 2; i < 2 + newColsCount - currentColsCount; i++) { // 2 - minimum possible cols count
      insertEmptyColumn(displayedTiles, i);
    }
    return displayedTiles;
  }

  function getTilesForChangedLayout(newColsCount) {
    const currentColsCount = GridUtils.getColsCount();
    if (newColsCount < currentColsCount) {
      return getTilesForSmallerLayout(newColsCount);
    }
    return getTilesForBiggerLayout(newColsCount);
  }

  this.addAction({click: 'change-layout'}, function() {
    const newColsCount = this.el.data('cols-count');
    if (newColsCount === GridUtils.getColsCount()) {
      return;
    }

    const newTiles = getTilesForChangedLayout(newColsCount);

    Def.LAYOUT.CURRENT.COLS_COUNT = newColsCount;
    GridUtils.setCurrentSchema(newTiles);

    drawTiles();
  });
});
