AGN.Lib.Dashboard.Def = {
  LAYOUT: {
    DEFAULT: {
      COLS_COUNT: 3,
      ROWS_COUNT: 2,
      SCHEMA: [{id: "mailings"}, {id: "statistics"}, {id: "imports-exports"}, {id: "calendar"}, {id: "news"}]
    }
  },

  TileSize: {
    REGULAR: {dimensions: [1, 1], name: 'regular'},
    TALL: {dimensions: [2, 1], name: 'tall'},
    WIDE: {dimensions: [1, 2], name: 'wide'},
    X_WIDE: {dimensions: [1, 3], name: 'x-wide'},
    // LARGE: {dimensions: [2, 2], name: 'lg'}, keep for future
    X_LARGE: {dimensions: [2, 3], name: 'xl'},

    from: function(name) {
      for (let key in this) {
        if (this[key].name === name) {
          return this[key];
        }
      }
      return null;
    }
  },
  DATE_FORMAT: 'DD-MM-YYYY',
  TIME_FORMAT: 'HH:mm',
  DATE_TIME_FORMAT: 'DD-MM-YYYY HH:mm',
};
