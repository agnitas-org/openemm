(() => {

  function compare(valueA, valueB) {
    if (typeof valueA === "string" && typeof valueB === "string") {
      return valueA.toLowerCase().localeCompare(valueB.toLowerCase());
    }

    if (valueA === null || valueA === undefined) {
      return 1;
    }

    return valueA.localeCompare(valueB);
  }

  AGN.Lib.TableCaseInsensitiveComparator = compare;

})();
