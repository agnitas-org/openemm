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
        return rectangle.left != rectangle.right && rectangle.top != rectangle.bottom;
    }

    function getRectangles(a) {
        var rectangle = a.getBoundingClientRect();
        var rects = $.map(a.children, getRectangles);
        rects.push(rectangle);
        return $.map(rects, function(rectangle) {
            return checkNotEmptyRectangle(rectangle) ? rectangle : null;
        });
    }

    function getRectangleArea(rectangle) {
        return (rectangle.bottom - rectangle.top) * (rectangle.right - rectangle.left);
    }

    function showPopups() {
      // get ecs-frame document
      var frameDocument = document;

      var nullColorElement = document.getElementById('info-null-color');
      if (nullColorElement == null) {
        return;
      }
      var nullColor = nullColorElement.value;

      // iterate through all links of document
      var links = document.getElementsByTagName('a');

      if (links != null && links.length > 0) {
        for(var i = 0; i < links.length; i++) {
          var a = links[i];

          var linkUrl = a.getAttribute('href');
          if (!linkUrl) {
            continue;
          } else if (linkUrl.lastIndexOf('http') == -1) {
            continue;
          } else if (linkUrl.lastIndexOf('uid=') > -1) {
            var uid = linkUrl.substr(linkUrl.lastIndexOf('uid=') + 4);
            if (uid.indexOf('&') != -1) {
              uid = uid.substr(0, uid.indexOf('&'));
            }
            var codedUrlId = uid;
          } else if (linkUrl.lastIndexOf('/r/') > -1) {
            var uid = linkUrl.substr(linkUrl.lastIndexOf('/r/') + 3);
            if (uid.indexOf('/') != -1) {
              uid = uid.substr(0, uid.indexOf('/'));
            }
            var codedUrlId = uid;
          } else {
            continue;
          }

          // get stats info for the URL from hidden field
          var infoElId = "info-" + codedUrlId;
          var linkInfo = document.getElementById(infoElId);

          // if there is stats for the URL - create stats-label and put it near link
          // in other case create default stat-label with zero-value
          var clickValue = "0 (0%)";
          var bgColor = nullColor;
          if (linkInfo != null) {
            clickValue = linkInfo.value;
            bgColor = linkInfo.name;
          }

          // Get optimized clickable areas
          var rectangles = getRectangles(a);
          if (rectangles.length) {
            rectangles = uniteIntersectingRectangles(rectangles);

            var biggestRectangle = rectangles[0];

            for(var k = 0; k < rectangles.length; k++) {
              var balloon = createBalloon(bgColor);
              adjustBalloon(balloon, rectangles[k]);

              if (getRectangleArea(biggestRectangle) < getRectangleArea(rectangles[k])) {
                biggestRectangle = rectangles[k];
              }
            }

            var tag = createTag(bgColor, clickValue);
            adjustTag(tag, biggestRectangle);
          }
        }
      }
    }

    function updatePopopsPositions() {
      $('.clicks-statistic-balloon').remove();
      $('.clicks-statistic-tag').remove();
      showPopups();
    }

    function createBalloon(color) {
        var myDiv = document.createElement('div');
        myDiv.style.backgroundColor = color;
        myDiv.className = 'clicks-statistic-balloon';
        document.body.appendChild(myDiv);
        return myDiv;
    }

    function createTag(color, text) {
        var myDiv = document.createElement('div');
        myDiv.innerHTML = text;
        myDiv.style.backgroundColor = color;
        myDiv.style.padding = '1px';
        myDiv.style.border = '1px solid #777777';
        myDiv.style.fontFamily = 'Tahoma, Arial, Helvetica, sans-serif';
        myDiv.style.fontSize = '11px';
        myDiv.style.zIndex = "10";
        myDiv.style.whiteSpace = 'nowrap';
        myDiv.className = 'clicks-statistic-tag';
        document.body.appendChild(myDiv);
        return myDiv;
    }

    function adjustBalloon(e, rectangle) {
        $(e).css("position", "absolute");
        $(e).css("opacity", "0.5");
        // fix for opacity in IE
        $(e).css("filter", "progid:DXImageTransform.Microsoft.Alpha(opacity=50)");

        $(e).css({
            left: rectangle.left,
            top: rectangle.top,
            width: rectangle.right - rectangle.left,
            height: rectangle.bottom - rectangle.top
        });
    }

    function adjustTag(e, rectangle) {
        $(e).css("position", "absolute");
        $(e).css("textAlign", "center");

        $(e).css({
            left: rectangle.right - $(e).outerWidth(),
            top: rectangle.bottom
        });
    }

    function setImagesLoadedEvent() {
      $(window.document.body).imagesLoaded().progress(_.throttle(updatePopopsPositions, 100)).always(updatePopopsPositions);
    }

    if (window.addEventListener) {
        window.addEventListener('load', setImagesLoadedEvent, false);
    } else if (window.attachEvent) {
        window.attachEvent('onload', setImagesLoadedEvent);
    } else {
        window.onload = setImagesLoadedEvent;
    }
})();
