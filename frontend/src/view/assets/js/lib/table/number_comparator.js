(function() {

  function compare(valueA, valueB, nodeA, nodeB, isInverted) {
    valueA = parseFloat(valueA);
    valueB = parseFloat(valueB);

    // NaNs last for any sort direction.
    if (isNaN(valueA) || isNaN(valueB)) {
      if (!isNaN(valueA)) {
        return isInverted ? +1 : -1;
      }

      if (!isNaN(valueB)) {
        return isInverted ? -1 : +1;
      }

      return 0;  // Both are NaN, keep order unchanged.
    }

    return valueA - valueB;
  }

  AGN.Lib.TableNumberComparator = compare;

})();
