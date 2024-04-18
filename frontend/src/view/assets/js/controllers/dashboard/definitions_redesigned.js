AGN.Lib.Dashboard.Definitions = {
  // TILES_CONTAINER defined in initializer
  LAYOUT: {
    DEFAULT: {
      COLS_COUNT: 3,
      ROWS_COUNT: 2,
      SCHEMA: [{id: "mailings"}, {id: "statistics"}, {id: "imports-exports"}, {id: "calendar"}, {id: "news"}]
    }
  },
  TILE: AGN.Lib.Helpers.deepFreeze({
    TYPE: {REGULAR: 'regular', TALL: 'tall', WIDE: 'wide'},
    ID: {
      EMPTY: 'empty',
      CALENDAR: 'calendar',
      MAILINGS: 'mailings',
      STATISTICS: 'statistics',
      CLICKERS: 'clickers',
      OPENERS: 'openers',
      NEWS: 'news',
      ADD_ONS: 'add-ons',
      ANALYSIS: 'analysis',
      IMPORTS_EXPORTS: 'imports-exports',
      PLANNING: 'planning',
      WORKFLOWS: 'workflows',
    },
  })
}
