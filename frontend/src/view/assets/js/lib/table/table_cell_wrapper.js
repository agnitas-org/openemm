(function () {
  
  AGN.Lib.TableCellWrapper = function (link) {
    if (link) {
      var cell = document.createElement('a');
      cell.href = link;
      cell.style.cssText =
        'display: block;' +
        'height: 33px;' +
        'margin:  -8px -6px;' +
        'padding: 8px 6px;';
      return cell;
    } else {
      return document.createElement('div');
    }
  };
})();
