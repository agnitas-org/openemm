;(function(){

  AGN.Initializers.DropdownExpand = function($scope) {

    _.each($('[data-dropdown-expand]'), function(el) {
      var spaceLeft,
          offsetWidth,
          $el       = $(el),
          $dropdown = $el.find('.dropdown-menu'),
          target    = $el.data('dropdown-expand'),
          $target   = $(target),
          itemsToBeHidden = [];

      $el.removeClass('hidden');
      $dropdown.html('');

      spaceLeft = $target.parent().width();
      offsetWidth = $target.children().filter($el).outerWidth(true) + 15;

      _.each($target.siblings(), function(ch) {
        var $ch = $(ch);

        spaceLeft = spaceLeft - $ch.outerWidth(true);
      })

      $el.addClass('hidden');

      _.reduce($target.children().not($el), function(sum, ch) {
        var $ch = $(ch);

        $ch.removeClass('hidden');
        sum = sum + $ch.outerWidth(true);

        if (sum >= spaceLeft) {
          $el.removeClass('hidden');
          $dropdown.append($ch.clone())
          $ch.addClass('hidden');
        }

        return sum;

      }, offsetWidth);

    });
  }

})();
