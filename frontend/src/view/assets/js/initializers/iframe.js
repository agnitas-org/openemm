(function(){

  AGN.Initializers.Iframe = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    $scope.find('iframe:not(.js-fixed-iframe)').each(function() {
      var el = this, $el = $(this);

      if ($el.hasClass('js-simple-iframe')) {
        var $wrapper = $el.parent(),
          $page    = $wrapper.parent(),
          maxWidth = parseInt($el.data('max-width')),
          heightSafety = parseInt($el.data('height-extra')) || 10,
          mediaQuery = $el.data('media-query'),
          isFF = !!window.sidebar,
          updateIframe,
          resizeArea;

        updateIframe = function() {
          var doc = el.contentDocument ? el.contentDocument : el.contentWindow.document,
            body = doc.body, html = doc.documentElement,
            height, width;

          if (mediaQuery) {
            width = maxWidth;
          } else {
            width = Math.max(
              body.scrollWidth,
              body.offsetWidth,
              html.clientWidth,
              html.scrollWidth,
              html.offsetWidth
            );
          }

          height = Math.max(
            body.scrollHeight,
            body.offsetHeight,
            html.clientHeight,
            html.scrollHeight,
            html.offsetHeight
          );

          height+= heightSafety;

          $el.height(height);
          $el.width(width);

          if (mediaQuery && isFF) {
            $el.contents().find('body').css('overflow', 'hidden');
          }

          resizeArea();
          $(window).off('viewportChanged', resizeArea);
          $(window).on('viewportChanged', resizeArea);
        };

        resizeArea = function() {
          $page.width('auto');

          if ($el.width() > maxWidth && maxWidth > $page.width()) {
            $wrapper.width($el.width());
          } else {
            $wrapper.width(Math.min(maxWidth, $page.width()));
            // $wrapper.parent().width(maxWidth);
          }

          if (!mediaQuery) {
            $wrapper.doubleScroll();
            $wrapper.parent().doubleScroll();
            $('.doubleScroll-scroll-wrapper').css('margin', '0 auto');
          }

        };

        $el.off('load');
        $el.on('load', updateIframe);
      } else {
        $el.on('load', function() {
          window.setTimeout(function() {
            $el.iFrameResize({heightCalculationMethod: 'grow', enablePublicMethods: true});
          }, 50);

        });
      }
    });

  }

})();
