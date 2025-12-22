AGN.Lib.Controller.new('dashboard-grid', function () {

  const LEGACY_TILES = ['xl-calendar'];

  const Dashboard = AGN.Lib.Dashboard;
  const Modal = AGN.Lib.Modal;
  const Def = Dashboard.Def;
  const TileSize = Def.TileSize;
  const EmptyTile = Dashboard.EmptyTile;
  const DraggableTile = Dashboard.DraggableTile;
  const controller = this;

  let tiles

  let placeholderTile; // tile with pressed Add/Replace button to be replaced with chosen one
  let grid;

  this.addDomInitializer('dashboard-grid', function () {
    tiles = initTiles().filter(tile => tile.allowed());

    const layout = JSON.parse(this.config.layout) || _.clone(Def.LAYOUT.DEFAULT);
    fixLegacyTiles(layout);

    drawTiles(schemaToTiles(layout.SCHEMA), layout.COLS_COUNT);
    infoIfDashboardEmpty();

    $(window).on("displayTypeChanged", (e, isMobileView) => {
      if (isMobileView) {
        toggleEditMode(false);
      }
    });
    toggleEditMode(false);
  });

  function initTiles() {
    return [
      new Dashboard.MailingsTile(controller),
      new Dashboard.PlanningTile(controller),
      new Dashboard.StatisticsTile(controller),
      new Dashboard.ClickersTile(controller),
      new Dashboard.OpenersTile(controller),
      new Dashboard.WorkflowsTile(controller),
      new Dashboard.ImportExportTile(controller),
      new Dashboard.AddOnsTile(controller),
      new Dashboard.NewsTile(controller),
      new Dashboard.CalendarTile(controller),
      new Dashboard.AnalysisTile(controller)
    ];
  }

  function fixLegacyTiles(layout) {
    layout.SCHEMA
      .filter(tile => LEGACY_TILES.includes(tile.id)).map(tile => tile.position)
      .forEach(pos => {
        pos.rows.forEach(row => {
          pos.cols.forEach(col => {
            layout.SCHEMA.push({id: Dashboard.EmptyTile.ID, positions: {rows: [row], cols: [col]}});
          });
        });
      });
    layout.SCHEMA = layout.SCHEMA.filter(tile => !LEGACY_TILES.includes(tile.id));
  }

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
    grid = new Dashboard.DashboardGrid(2, colsCount, tiles);
  }

  this.addAction({click: 'delete-tile'}, function () {
    const tileToDelete = DraggableTile.get(this.el.closest('.draggable-tile'));
    grid.replaceWithEmptyTile(tileToDelete);
  });

  this.addAction({click: 'replace-tile'}, function () {
    placeholderTile = DraggableTile.get(this.el.closest('.draggable-tile'));
    Modal.fromTemplate('dashboard-tiles-selection-modal', {tiles: getUnusedTiles()});
  });

  this.addAction({click: 'select-tile'}, function () {
    placeholderTile = DraggableTile.get(this.el.closest('.draggable-tile'));
    Modal.fromTemplate('dashboard-tiles-selection-modal', {tiles: getAllowedToAddTiles()})
  });

  function getAllowedToAddTiles() {
    return getUnusedTiles().map(tile => {
      tile.variants.forEach(variant => {
        const interferingTiles = grid.findInterferingTilesToReplace(placeholderTile, variant);
        // if at least one not empty interfering tile found, then block add of this variant
        variant.disabled = interferingTiles.some(tile => !tile.isEmpty);
      });
      return tile;
    });
  }

  function getUnusedTiles() {
    const displayedTiles = grid.visibleTiles.map(tile => tile.id);
    return tiles.filter(tile => !tile.isEmpty && !displayedTiles.includes(tile.id))
      .map(tile => {
        tile.variants.forEach(variant => variant.disabled = false);
        return tile;
      });
  }

  this.addAction({click: 'add-tile'}, function () {
    const modal = Modal.getInstance(this.el);
    const tileToAdd = getTileById(this.el.data('tile-id'));
    tileToAdd.size = TileSize.from(this.el.data('type')) || TileSize.REGULAR;

    const interferingTiles = grid.findInterferingTilesToReplace(placeholderTile, tileToAdd.size)
      .filter(tile => !tile.isEmpty);

    const callback = () => {
      grid.replaceTile(placeholderTile, tileToAdd);
      modal.hide();
    }

    if (interferingTiles.length > 1) {
      AGN.Lib.Confirm.from(
        'dashboard-replace-tiles-question-modal',
        {tileNames: interferingTiles.map(tile => tile.name).join(', ')}
      ).done(callback);
    } else {
      callback();
    }
  });

  this.addAction({change: 'redraw-mailing-statistics'}, function () {
    const $tile = this.el.closest('.draggable-tile');
    const tile = DraggableTile.get($tile);
    tile.updateChart();
  });

  this.addAction({click: 'open-mailing-statistics'}, function () {
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

  this.addAction({click: 'edit-dashboard'}, function () {
    toggleEditMode(true);
    const $infoPopup = this.el.closest('.popup');
    closePopupWithEditLink($infoPopup); // edit mode can be entered using popup link; see infoIfDashboardEmpty();
  });

  this.addAction({click: 'stop-editing'}, function () {
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
    }).done(() => AGN.Lib.Messages.defaultSaved());
  }

  function toggleEditMode(on) {
    $('#dashboard-start-edit-btn').closest('li').toggle(!on);
    $('body').toggleClass('edit-mode', !!on);
    grid.swapper.toggle(on);
  }

  this.addAction({click: 'select-layout'}, function () {
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
      .forEach(tile => grid.replaceWithEmptyTile(tile));

    const tiles = grid.tiles.map(tile => {
      if (tile.isWide && isGridLeftOver(tile, newColsCount)) {
        const emptyTile = new EmptyTile();
        emptyTile.position = {cols: [tile.beginCol], rows: [tile.beginRow]};
        return emptyTile;
      }
      return tile;
    });
    return tiles.filter(tile => !isGridLeftOver(tile, newColsCount));
  }

  function insertEmptyColumn(tiles, col) {
    for (let i = 0; i < grid.rows; i++) {
      const emptyTile = new EmptyTile();
      emptyTile.position = {rows: [i], cols: [col]};
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

  this.addAction({click: 'change-layout'}, function () {
    const newColsCount = this.el.data('cols-count');
    if (newColsCount === grid.cols) {
      return;
    }
    const newTiles = getTilesForChangedLayout(newColsCount);
    drawTiles(newTiles, newColsCount);
  });
});
