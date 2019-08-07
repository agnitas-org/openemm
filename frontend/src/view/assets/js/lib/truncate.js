(function(){

  AGN.Lib.Truncate = {
    run: function($container, truncateSelector) {
      var width,
          spaceOccupied;

      width = $container.width();

      // space occupied by other elements
      spaceOccupied = _.reduce($container.children().not(truncateSelector), function(total, el) {
        return total + $(el).outerWidth(true);
      }, 0);

      // margins of the truncated element
      spaceOccupied = spaceOccupied + ($(truncateSelector).outerWidth(true) - $(truncateSelector).outerWidth())

      // error margin of 5% or at least 5px
      spaceOccupied = (spaceOccupied * 0.05 > 5) ? 1.05 * spaceOccupied : spaceOccupied + 5

      // check if any space is left at all
      spaceOccupied = (spaceOccupied > width) ? width : spaceOccupied

      $container.children(truncateSelector).css({
        "max-width": width - spaceOccupied  + 'px',
        "overflow": "hidden"
      });
    }
  }


})();
