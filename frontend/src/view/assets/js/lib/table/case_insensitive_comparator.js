(function() {

  function compare(valueA, valueB) {
    if (typeof valueA === "string" && typeof valueB === "string") {
      return valueA.toLowerCase().localeCompare(valueB.toLowerCase());
    }

    return valueA.localeCompare(valueB);
  }

  AGN.Lib.TableCaseInsensitiveComparator = compare;

})();
