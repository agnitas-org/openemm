(function() {
    function checkIntersection(rect1, rect2) {
        return (
            rect2.top >= rect1.top && rect2.top <= rect1.bottom ||
            rect1.top >= rect2.top && rect1.top <= rect2.bottom ||
            rect2.bottom >= rect1.top && rect2.bottom <= rect1.bottom ||
            rect1.bottom >= rect2.top && rect1.bottom <= rect2.bottom
          ) && (
            rect2.left >= rect1.left && rect2.left <= rect1.right ||
            rect1.left >= rect2.left && rect1.left <= rect2.right ||
            rect2.right >= rect1.left && rect2.right <= rect1.right ||
            rect1.right >= rect2.left && rect1.right <= rect2.right
          );
    }

    function uniteRectangles(rect1, rect2) {
        return {
            top: Math.min(rect1.top, rect2.top),
            bottom: Math.max(rect1.bottom, rect2.bottom),
            left: Math.min(rect1.left, rect2.left),
            right: Math.max(rect1.right, rect2.right)
        };
    }

    function uniteIntersectingRectangles(rects) {
        var newRects = [];

        rects = rects.slice(0);
        for (var i = 0; i < rects.length; i++) {
            var united = false;

            for (var k = i + 1; k < rects.length; k++) {
                if (checkIntersection(rects[i], rects[k])) {
                    rects[k] = uniteRectangles(rects[i], rects[k]);
                    united = true;
                    break;
                }
            }

            if (!united) {
                newRects.push(rects[i]);
            }
        }

        return newRects;
    }

    function checkNotEmptyRectangle(rectangle) {
        return rectangle.left !== rectangle.right && rectangle.top !== rectangle.bottom;
    }

    function getRectangles(link) {
      var elements = [].slice.call(link.children);
      elements.push(link);

      var rects = _.map(elements, function(el) {
        return el.getBoundingClientRect();
      });

      return _.filter(rects, checkNotEmptyRectangle);
    }

    function getRectangleArea(rectangle) {
        return (rectangle.bottom - rectangle.top) * (rectangle.right - rectangle.left);
    }

    function getLinkUid($link) {
      var href = $link.prop('href');
      var uid = -1;
      if (href && href.lastIndexOf('http') != -1) {
        if (href.lastIndexOf('uid=') > -1) {
          uid = new URL(href).searchParams.get('uid');
        } else if (href.lastIndexOf('/r/') > -1) {
          var part = href.substr(href.lastIndexOf('/r/') + 3);
          uid = part.replace('/', '');
        }
      }
      return uid || -1;
    }

    function showPopups() {
      // get ecs-frame document
      var $scope = $(document);

      var nullColorElement = $scope.find('#info-null-color');

      if (nullColorElement.length > 0) {
        var nullColor = nullColorElement.val();

        // iterate through all links of document
        var links = $scope.find('a');

        links.each(function (index) {
          var link = this;
          var $link = $(link);
          var uid = getLinkUid($link);

          if (uid > -1) {
            // get stats info for the URL from hidden field
            var linkInfo = $scope.find('#info-' + uid);

            // if there is stats for the URL - create stats-label and put it near link
            // in other case create default stat-label with zero-value
            var clickValue = "0 (0%)";
            var bgColor = nullColor;
            if (linkInfo.length > 0) {
              clickValue = linkInfo.val();
              bgColor = linkInfo.prop('name');
            }

            // Get optimized clickable areas
            var rectangles = getRectangles(link);
            if (rectangles.length) {
              rectangles = uniteIntersectingRectangles(rectangles);

              var biggestRectangle = rectangles[0];

              for (var k = 0; k < rectangles.length; k++) {
                var balloonId = createBalloon(bgColor, uid, index);
                adjustBalloon(balloonId, rectangles[k]);

                if (getRectangleArea(biggestRectangle) < getRectangleArea(rectangles[k])) {
                  biggestRectangle = rectangles[k];
                }
              }

              var tagId = createTag(bgColor, clickValue, uid, index);
              adjustTag(tagId, biggestRectangle);
            }
          }
        });
      }
    }

    function updatePopupsPositions() {
      $('.clicks-statistic-balloon').remove();
      $('.clicks-statistic-tag').remove();
      showPopups();
    }

    function createBalloon(color, uid, index) {
      var id = 'balloon_' + uid + '_' + index;
      var myDiv = document.createElement('div');
      myDiv.id = id;
      myDiv.style.backgroundColor = color;
      myDiv.className = 'clicks-statistic-balloon';
      myDiv.setAttribute('urlId', uid);
      myDiv.addEventListener('mouseover', highlightTag, false);
      myDiv.addEventListener('mouseout', removeTagHighlighting, false)
      document.body.appendChild(myDiv);
      return id;
    }

    function createTag(color, text, uid, index) {
      var id = 'tag_' + uid + '_' + index;
      var myDiv = document.createElement('div');
      myDiv.id = id;
      myDiv.innerHTML = text;
      myDiv.style.backgroundColor = color;
      myDiv.style.padding = '1px';
      myDiv.style.border = '1px solid #777777';
      myDiv.style.fontFamily = 'Tahoma, Arial, Helvetica, sans-serif';
      myDiv.style.fontSize = '11px';
      myDiv.style.zIndex = "10";
      myDiv.style.whiteSpace = 'nowrap';
      myDiv.style.opacity = '0.8';
      myDiv.setAttribute('urlId', uid);
      myDiv.className = 'clicks-statistic-tag';
      document.body.appendChild(myDiv);
      return id;
    }

    function adjustBalloon(id, rectangle) {
      var $balloon = $('#' + id);
      $balloon.css("position", "absolute");
      $balloon.css("opacity", "0.5");
      // fix for opacity in IE
      $balloon.css("filter", "progid:DXImageTransform.Microsoft.Alpha(opacity=50)");

      $balloon.css({
          left: rectangle.left,
          top: rectangle.top,
          width: rectangle.right - rectangle.left,
          height: rectangle.bottom - rectangle.top
      });
    }

    function adjustTag(id, rectangle) {
      var $tag = $('#' + id);
      $tag.css("position", "absolute");
      $tag.css("textAlign", "center");

      var leftValue = rectangle.right - $tag.outerWidth();
      if(leftValue < 0) {
         leftValue = 0;
      }
      $tag.css({
          left: leftValue,
          top: rectangle.bottom
      });
    }

    function setImagesLoadedEvent() {
      $(window.document.body).imagesLoaded()
        .progress(_.throttle(updatePopupsPositions, 100))
        .always(function() {
          updatePopupsPositions();
          window.waitStatus = "heatmapLoadFinished";
        });
    }

    function highlightTag(e) {
      var $tag = $('.clicks-statistic-tag[urlId="' +  $(e.target).attr('urlId') + '"]'),
        zIndex = $tag.css('z-index') || 0;
      $tag.css('z-index', 1000 + zIndex*1);
      $tag.css('opacity', '1.0');
    }

    function removeTagHighlighting(e) {
      var $tag = $('.clicks-statistic-tag[urlId="' + $(e.target).attr('urlId') + '"]'),
        zIndex = Math.max($tag.css('z-index') || 0, 1000);
      $tag.css('z-index', zIndex - 1000);
      $tag.css('opacity', '0.8');
    }

    if (window.addEventListener) {
        window.addEventListener('load', setImagesLoadedEvent, false);
    } else if (window.attachEvent) {
        window.attachEvent('onload', setImagesLoadedEvent);
    } else {
        window.onload = setImagesLoadedEvent;
    }
})();