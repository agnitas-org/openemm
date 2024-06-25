(function() {
  const Popover = AGN.Lib.Popover;

  function initializeRowClicks($row) {
    const $link = $row.find('[data-view-row]');
    if ($link.length !== 1) {
      return
    }
    $row.data('link', $link.attr('href'));

    if ($link.data('view-row') === 'page') {    // detail view content is opened with reload
      $row.data('load-page', 'true');
      $row.wrap(`<a class="table-row-wrapper" href="${$link.attr('href')}"></a>`);
    } else if ($link.data('table-modal')) {     // content is loaded as a modal from the mustache template
      passModalAttrsFromLinkToRow($link, $row);
    }                                           // content is loaded as a modal from the server. default
    $link.remove();
  }

  function passModalAttrsFromLinkToRow($link, $row) {
    const dataModalAttr = 'data-table-modal';
    const dataModalOptionsAttr = 'data-table-modal-options';
    $row
      .attr(dataModalAttr, $link.attr(dataModalAttr))
      .attr(dataModalOptionsAttr, $link.attr(dataModalOptionsAttr));
  }

  function initializeRowPopovers($row) {
    const $popover = $row.find('.js-row-popover');

    if ($popover.length !== 1 || $row.closest('table').hasClass('table-preview')) {
      // destroy existing popovers when switch to 'Preview' mode
      Popover.remove($row);
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
      $(this).find('tr').each(function () {
        const $row = $(this);
        initializeRowClicks($row);
        initializeRowPopovers($row);
      });
    });
  });

})();
