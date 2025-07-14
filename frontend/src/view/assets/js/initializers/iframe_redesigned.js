/*doc
---
title: iFrame
name: iframe
category: Components - iFrame
---
*/

AGN.Lib.CoreInitializer.new('iframe', function ($scope = $(document)) {
  $scope.find('iframe:not(.js-fixed-iframe)').each(function () {
    const el = this;
    const $el = $(this);

    if ($el.hasClass('js-simple-iframe')) {
      const $wrapper = $el.parent();
      const $page = $wrapper.parent();

      const maxWidthVal = $el.data('max-width');
      const shouldUpdateWidth = maxWidthVal !== '100%';
      const maxWidth = parseInt(maxWidthVal);

      const mediaQuery = $el.data('media-query');

      const detectHeight = doc => {
        const body = doc.body;
        const html = doc.documentElement;

        return Math.max(
          body.scrollHeight,
          body.offsetHeight,
          html.clientHeight,
          html.scrollHeight,
          html.offsetHeight
        );
      }

      const detectWidth = doc => {
        const body = doc.body;
        const html = doc.documentElement;

        return Math.max(
          body.scrollWidth,
          body.offsetWidth,
          html.clientWidth,
          html.scrollWidth,
          html.offsetWidth
        );
      }

      const updateIframe = () => {
        const doc = el.contentDocument || el.contentWindow?.document;

        if (doc) {
          let scale = 1;

          if (shouldUpdateWidth) {
            const contentWidth = detectWidth(doc);

            if (mediaQuery) {
              if (contentWidth > maxWidth) {
                scale = maxWidth / contentWidth;
                $(doc.documentElement).css('zoom', `calc(${scale})`);
              }

              $el.width(maxWidth);
            } else {
              $el.width(contentWidth);
            }
          }

          // Do it to fix adding height-extra many times if this function called more than once
          $el.height(detectHeight(doc) * scale);

          const $body = $el.contents().find('body');
          $body.css('overflow-y', 'hidden');

          if (mediaQuery) {
            $body.css('overflow-x', 'hidden');
          }
        }

        if (shouldUpdateWidth) {
          $page.width('auto');

          if ($el.width() > maxWidth && maxWidth > $page.width()) {
            $wrapper.width($el.width());
          } else {
            $wrapper.width(Math.min(maxWidth, $page.width()));
          }
        }
      };

      $el.off('load.iframe');
      $el.on('load.iframe', function () {
        window.setTimeout(() => {
          updateIframe();

          const $body = $(el.contentDocument.body);
          if (typeof $body.imagesLoaded === "function") {
            $body.imagesLoaded().progress(_.throttle(updateIframe, 100)).always(updateIframe);
          }

          $(window).off('viewportChanged.iframe', updateIframe);
          $(window).on('viewportChanged.iframe', updateIframe);
        }, 50);
      });
    } else {
      $el.on('load.iframe', function () {
        window.setTimeout(() => {
          $el.iFrameResize({heightCalculationMethod: 'grow', enablePublicMethods: true});
        }, 50);
      });
    }
  });
});
