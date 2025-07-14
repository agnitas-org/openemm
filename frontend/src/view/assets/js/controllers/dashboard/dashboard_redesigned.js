AGN.Lib.Controller.new('dashboard', function() {

  const Modal = AGN.Lib.Modal;
  const Def = AGN.Lib.Dashboard.Def;
  const TileSize = Def.TileSize;
  const EmptyTile = AGN.Lib.Dashboard.EmptyTile;
  const DraggableTile = AGN.Lib.Dashboard.DraggableTile;

  let tiles = [
    new AGN.Lib.Dashboard.MailingsTile(this),
    new AGN.Lib.Dashboard.PlanningTile(this),
    new AGN.Lib.Dashboard.StatisticsTile(this),
    new AGN.Lib.Dashboard.ClickersTile(this),
    new AGN.Lib.Dashboard.OpenersTile(this),
    new AGN.Lib.Dashboard.WorkflowsTile(this),
    new AGN.Lib.Dashboard.ImportExportTile(this),
    new AGN.Lib.Dashboard.AddOnsTile(this),
    new AGN.Lib.Dashboard.NewsTile(this),
    new AGN.Lib.Dashboard.XlCalendarTile(this),
    new AGN.Lib.Dashboard.CalendarTile(this),
    new AGN.Lib.Dashboard.WeekCalendarTile(this),
    new AGN.Lib.Dashboard.AnalysisTile(this)
  ];

  let placeholderTile; // tile with pressed + button to be replaced with chosen one
  let grid;

  this.addDomInitializer('dashboard-view', function() {
    tiles = tiles.filter(tile=> tile.allowed());
    const layout = JSON.parse(this.config.layout) || _.clone(Def.LAYOUT.DEFAULT);
    drawTiles(schemaToTiles(layout.SCHEMA), layout.COLS_COUNT);
    infoIfDashboardEmpty();

    $(window).on("displayTypeChanged", (e, isMobileView) => {
      if (isMobileView) {
        toggleEditMode(false);
      }
    });
    toggleEditMode(false);
    new AGN.Lib.Dashboard.News();
  });

  function infoIfDashboardEmpty() {
    if (!grid.visibleTiles.length) {
      AGN.Lib.Messages.info('dashboard.empty');
    }
  }

  function getSizeByTileDescription(tileDescr, defaultVal) {
    const rowsOccupied = tileDescr.position?.rows?.length;
    const colsOccupied = tileDescr.position?.cols?.length;
    if (rowsOccupied === 2 && colsOccupied === 1) {
      return TileSize.TALL;
    }
    if (rowsOccupied === 1 && colsOccupied === 2) {
      return TileSize.WIDE;
    }
    if (rowsOccupied === 2 && colsOccupied === 3) {
      return TileSize.X_LARGE;
    }
    return defaultVal;
  }

  function schemaToTiles(schema) {
    return schema.map(tileDescr => {
      const tile = getTileById(tileDescr.id)
      tile.size = getSizeByTileDescription(tileDescr, tile.size);
      return tile;
    })
  }

  function drawTiles(tiles, colsCount) {
    grid = new AGN.Lib.Dashboard.DashboardGrid(2, colsCount, tiles);
  }

  this.addAction({click: 'delete-tile'}, function() {
    const $tileToDelete = this.el.closest('.draggable-tile');
    const tileToDelete = DraggableTile.get($tileToDelete);
    grid.removeTile(tileToDelete);
  });

  this.addAction({click: 'select-tile'}, function() {
    placeholderTile = DraggableTile.get(this.el.closest('.draggable-tile'));
    Modal.fromTemplate('dashboard-tiles-selection-modal', {tiles: getAllowedToAddTiles()})
  });

  function getAllowedToAddTiles() {
    const displayedTiles = grid.visibleTiles.map(tile => tile.id);
    return tiles
      .filter(tile => tile.id !== EmptyTile.ID && !displayedTiles.includes(tile.id))
      .map(tile => {
        tile.variants.forEach(variant => variant.disabled = grid.getPossibleStartPositions(variant, placeholderTile.beginRow, placeholderTile.beginCol).length <= 0);
        return tile;
      });
  }

  this.addAction({click: 'add-tile'}, function() {
    const tileToAdd = getTileById(this.el.data('tile-id'));
    const tileSize = TileSize.from(this.el.data('type'));
    if (tileSize) {
      tileToAdd.size = tileSize;
    }
    grid.placeTileAroundPos(tileToAdd, placeholderTile.beginRow, placeholderTile.beginCol);
  });

  this.addAction({change: 'redraw-mailing-statistics'}, function() {
    const $tile = this.el.closest('.draggable-tile');
    const tile = DraggableTile.get($tile);
    tile.updateChart();
  });

  this.addAction({click: 'open-mailing-statistics'}, function() {
    const mailingId = this.el.closest('.draggable-tile').find('[data-statistics-mailing]').val();
    AGN.Lib.Page.reload(AGN.url(`/statistics/mailing/${mailingId}/view.action`));
  });

  function getTileById(id) {
    return tiles[tiles.findIndex((tile) => tile.id === id)] || new EmptyTile();
  }

  function closePopupWithEditLink($popup) {
    if (!$popup.length) {
      return;
    }
    $(':focus', $popup).blur();
    toastr.clear($popup);
  }

  this.addAction({click: 'edit-dashboard'}, function() {
    toggleEditMode(true);
    const $infoPopup = this.el.closest('.popup');
    closePopupWithEditLink($infoPopup); // edit mode can be entered using popup link; see infoIfDashboardEmpty();
  });

  this.addAction({click: 'stop-editing'}, function() {
    toggleEditMode(false);
    infoIfDashboardEmpty();
    saveTilesLayout();
  });

  function saveTilesLayout() {
    $.ajax({
      type: 'POST',
      url: AGN.url('/dashboard/layout/save.action'),
      data: {layout: JSON.stringify(grid.schema)},
      async: false,
    }).done(() => AGN.Lib.Messages.defaultSaved())
      .fail(() => AGN.Lib.Messages.defaultError());
  }

  function toggleEditMode(on) {
    $('#dashboard-start-edit-btn').closest('li').toggle(!on);
    $('body').toggleClass('edit-mode', !!on);
    grid.swapper.toggle(on);
  }

  this.addAction({click: 'select-layout'}, function() {
    Modal.fromTemplate('dashboard-layout-selection-modal');
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
    grid.tiles
      .filter(tile => (tile.isXl || tile.isXWide) && isGridLeftOver(tile, newColsCount))
      .forEach(tile => grid.removeTile(tile));

    const tiles = grid.tiles.map(tile => {
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
    for (let i = 0; i < grid.rows; i++) {
      const emptyTile = new EmptyTile();
      emptyTile.position = {rows: [i], cols:[col]};
      tiles.push(emptyTile);
    }
  }

  function getTilesForBiggerLayout(newColsCount) {
    const displayedTiles = grid.tiles;

    for (let i = 0; i < newColsCount - grid.cols; i++) {
      insertEmptyColumn(displayedTiles, grid.cols + i);
    }
    return displayedTiles;
  }

  function getTilesForChangedLayout(newColsCount) {
    if (newColsCount < grid.cols) {
      return getTilesForSmallerLayout(newColsCount);
    }
    return getTilesForBiggerLayout(newColsCount);
  }

  this.addAction({click: 'change-layout'}, function() {
    const newColsCount = this.el.data('cols-count');
    if (newColsCount === grid.cols) {
      return;
    }
    const newTiles = getTilesForChangedLayout(newColsCount);
    drawTiles(newTiles, newColsCount);
  });
});
