(function() {
  const Popover = AGN.Lib.Popover;

  function initializeRowClicks($row) {
    const $link = $row.find('.js-row-show');

    if ($link.length !== 1) {
      return
    }

    $row.data('link', $link.attr('href'));
    $link.remove();

    _.each($row.prop('cells'), function(cell) {
      const $cell = $(cell);

      if ($cell.find('a,input,select,textarea,button').length !== 0) {
        return;
      }

      $cell.addClass('table-link');

      if ($link.data('modal')) {
        passModalAttrsFromLinkToCell($link, $cell);
        return;
      }
      if ($cell.contents().length === 0) {
        $cell.html(`<a class="js-row-display" href="${$link.attr('href')}">&nbsp;</a>`)
      } else {
        $cell.contents().wrapAll(`<a class="js-row-display" href="${$link.attr('href')}"></a>`)
      }
    });
  }

  function passModalAttrsFromLinkToCell($link, $cell) {
    const dataModalAttr = 'data-modal';
    const dataModalSetAttr = 'data-modal-set';
    $cell
      .attr(dataModalAttr, $link.attr(dataModalAttr))
      .attr(dataModalSetAttr, $link.attr(dataModalSetAttr));
  }

  function initializeRowPopovers($row) {
    const $popover = $row.find('.js-row-popover');

    if ($popover.length !== 1) {
      return;
    }

    let deferred = false;
    let content = null;

    const getContent = function() {
      if (deferred === false) {
        deferred = $.Deferred();

        var $elements = $popover.children();

        if ($elements.length == 0) {
          var $container = $('<div></div>');
          $container.css({
            position: 'fixed',
            visibility: 'hidden'
          });
          $container.html($popover.html());
          $elements = $container.children();

          // Workaround for some browsers setting wrong img.complete when an image element is not attached to a document
          $(document.body).append($container);

          deferred.done(function() {
            $elements.detach();
            $container.remove();
          });
        }

        deferred.done(function() {
          content = $elements;
        });

        const images = $elements.find('img')
          .add($elements.filter('img'))
          .filter(function() {
            return this.complete ? null : this;
          });

        if (images.length > 0) {
          deferred.done(function() {
            const popover = Popover.get($row);
            if (popover) {
              popover.setContent();
              if ($row.is(':hover')) {
                $row.popover('show');
              }
            }
          });

          var checkLoadingComplete = function() {
            let complete = true;
            for (var i = 0; i < images.length; i++) {
              var image = images[i];
              if (image) {
                if (image.complete) {
                  $(image).off('load error');
                  images[i] = null;
                } else {
                  complete = false;
                }
              }
            }
            return complete;
          };

          for (let i = 0; i < images.length; i++) {
            $(images[i]).on('load error', function() {
              if (checkLoadingComplete()) {
                deferred.resolve();
              }
            });
          }
        } else {
          deferred.resolve();
        }
      }

      return content;
    };

    Popover.new($row, {
      trigger: 'hover',
      delay: {
        show: 300,
        hide: 100
      },
      html: true,
      content: getContent
    });
  }

  AGN.Lib.CoreInitializer.new('table-row-actions', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    $scope.find('.js-table').each(function() {
      _.each(this.rows, function(row) {
        const $row = $(row);
        initializeRowClicks($row);
        initializeRowPopovers($row);
      });
    });
  });

})();
