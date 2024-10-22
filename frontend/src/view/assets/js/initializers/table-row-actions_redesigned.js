(() => {

  const Popover = AGN.Lib.Popover;

  function initializeRowClicks($row) {
    const $link = $row.find('[data-view-row]');
    if ($link.length !== 1) {
      return
    }
    $row.data('link', $link.attr('href'));

    if ($link.data('view-row') === 'page') { // detail view content is opened with reload
      $row.data('load-page', 'true');
      $row.wrap(`<a class="table-row-wrapper" href="${$link.attr('href')}"></a>`);
    } else if ($link.data('modal')) { // content is loaded as a modal from the mustache template
      passDataAttrsFromLinkToRow($link, $row);
    } // content is loaded as a modal from the server. default
    $link.remove();
  }

  function passDataAttrsFromLinkToRow($link, $row) {
    const DATA_PREFIX = 'data-';

    $.each($link[0].attributes, (index, attr) => {
      if (attr.name.startsWith(DATA_PREFIX) && !attr.name !== 'data-view-row') {
        const attrName = attr.name.substring(DATA_PREFIX.length);
        $row.attr(`data-${attrName}`, attr.value);
      }
    });
  }

  function initializeRowPopovers($row) {
    const $popover = $row.find('.js-row-popover');

    if ($popover.length !== 1 || $row.closest('table').hasClass('table--preview')) {
      // destroy existing popovers when switch to 'Preview' mode
      Popover.remove($row);
      return;
    }

    let deferred = false;
    let content = null;

    const getContent = function() {
      if (deferred === false) {
        deferred = $.Deferred();

        let $elements = $popover.children();

        if (!$elements.exists()) {
          const $container = $('<div></div>');
          $container.css({
            position: 'fixed',
            visibility: 'hidden'
          });
          $container.html($popover.html());
          $elements = $container.children();

          // Workaround for some browsers setting wrong img.complete when an image element is not attached to a document
          $(document.body).append($container);

          deferred.done(() => {
            $elements.detach();
            $container.remove();
          });
        }

        deferred.done(() => {
          content = $elements;
        });

        const images = $elements.find('img')
          .add($elements.filter('img'))
          .filter(function() {
            return this.complete ? null : this;
          });

        if (images.length > 0) {
          deferred.done(() => {
            const popover = Popover.get($row);
            if (popover) {
              popover.setContent();
              if ($row.is(':hover')) {
                $row.popover('show');
              }
            }
          });

          const checkLoadingComplete = function () {
            let complete = true;
            for (let i = 0; i < images.length; i++) {
              const image = images[i];
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

    Popover.create($row, {
      delay: {
        show: 300,
        hide: 100
      },
      html: true,
      content: getContent
    });
  }

  AGN.Lib.CoreInitializer.new('table-row-actions', function($scope = $(document)) {
    $scope.find('.js-table').each(function() {
      $(this).find('tr').each(function () {
        const $row = $(this);
        initializeRowClicks($row);
        initializeRowPopovers($row);
      });
    });
  });

})();
