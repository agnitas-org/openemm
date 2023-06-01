(function() {

  AGN.Lib.Breadcrumbs = {
    truncate: function($scope) {
      $.each($scope.find('.breadcrumbs.js-ellipsis'), function(index, e) {
        var $breadcrumb = $(e);
        var $items = $breadcrumb.children('li');

        $breadcrumb.find('*').removeClass('breadcrumb-element-small');
        $items.removeClass('abbreviated');
        $items.css({
          'min-width': '',
          'max-width': ''
        });
        $items.show();

        if (checkListItemsWidth($breadcrumb)){
          return;
        }

        $breadcrumb.find('*').addClass('breadcrumb-element-small');

        if (checkListItemsWidth($breadcrumb)){
          return;
        }

        var itemsSizes = [];
        var freeSpace = $breadcrumb.parent().width();

        $.each($items, function(index, e) {
          var $currentItem = $(e);

          if ($currentItem.hasClass('js-ellipsis')) {
            var content = $currentItem.html();

            $currentItem.html('&hellip;');
            var widthWithThreeDots = $currentItem.outerWidth(true);

            $currentItem.html(content);
            var widthWithContent = $currentItem.outerWidth(true);

            var minimumWidth = widthWithThreeDots < widthWithContent ? widthWithThreeDots : widthWithContent;

            $currentItem.addClass('abbreviated');
            var widthWithContentAbbreviated = $currentItem.outerWidth(true);
            $currentItem.removeClass('abbreviated');

            itemsSizes.unshift({
              $item: $currentItem,
              width: widthWithContent,
              minimumWidth: minimumWidth + 1,
              minimumWidthAbbreviated: minimumWidth + (widthWithContentAbbreviated - widthWithContent) + 1
            });
          } else {
            freeSpace -= $currentItem.outerWidth(true);
          }
        });

        var lastItemIndex = itemsSizes.length - 1;

        // Reversed order - right to left
        for (var i = 0; i <= lastItemIndex; i++) {
          var itemSize = itemsSizes[i];
          var $item = itemSize.$item;

          itemSize.width = $item.outerWidth(true);

          if (itemSize.width > freeSpace) {
            var minimumWidth;

            if (i == lastItemIndex) {
              minimumWidth = itemSize.minimumWidth;
            } else {
              $item.addClass('abbreviated');
              minimumWidth = itemSize.minimumWidthAbbreviated;
              for (var k = i + 1; k <= lastItemIndex; k++) {
                itemsSizes[k].$item.hide();
              }
            }

            if (minimumWidth > freeSpace && i > 0) {
              $item.hide();

              var previousItemSize = itemsSizes[i - 1];
              var $previousItem = previousItemSize.$item;
              $previousItem.addClass('abbreviated');

              if ($previousItem.outerWidth(true) - previousItemSize.width > freeSpace) {
                $previousItem.css('max-width', previousItemSize.width + freeSpace);
              }
            } else {
              $item.css('max-width', freeSpace);
            }
            break;
          } else {
            freeSpace -= itemSize.width;
          }
        }
      });
    }
  };

  /**
   * Checks if inner 'li' elements width less than container width
   * @param $container
   * @returns {boolean} true if elements width < container width
   */
  function checkListItemsWidth($container){
    var maximumWidth = $container.parent().width();

    var listItemsWidth = 0;
    $container.children('li').each(function (){
      listItemsWidth += $(this).outerWidth(true);
    });

    return Math.floor(listItemsWidth) <= Math.floor(maximumWidth);
  }
})();
