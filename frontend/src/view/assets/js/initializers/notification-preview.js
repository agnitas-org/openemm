AGN.Lib.CoreInitializer.new('push-notification-preview', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  _.each($scope.find('.push-notification-preview-button'), function(el) {
    function disablePreviewButton($element) {
      $element.prop("disabled", true);
      $element.parent().attr("data-tooltip", $element.parent().attr("data-tooltip-if-disabled"));
    }

    var $el = $(el);
    var formName = $el.data('notification-form');

    if (Notification.permission == 'denied') {
      disablePreviewButton($el);
    } else {
      $el.on("click", function() {
        function preview_push_notification() {
          var form = $('#' + formName);

          var titleElement = form.find('#pushTitle');
          var contentElement = form.find('#pushContent');
          var iconElement = form.find('#pushIcon');
          var linkElement = form.find('#pushLink');

          var pushTitle = (titleElement) ? titleElement.val() : "";
          var pushContent = (contentElement) ? contentElement.val() : "";
          var pushIcon = (iconElement) ? iconElement.val() : "";
          var pushLink = (linkElement) ? linkElement.val() : "";

          var opt = {body: pushContent, icon: pushIcon};

          var notification = new Notification(pushTitle, opt);
          notification.onclick = function(event) {
            event.preventDefault();
            window.open(pushLink, '_blank')
          };

        }

        if (Notification.permission != 'granted') {
          Notification.requestPermission(function(result) {
            if (result == 'granted') {
              preview_push_notification();
            } else if (result == 'denied') {
              disablePreviewButton($el);
            }
          });
        } else {
          preview_push_notification();
        }
      });
    }
  });
});
