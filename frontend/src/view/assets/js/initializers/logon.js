(function() {
    var NOTIFICATION_DISMISS_EXPIRATION_DAYS = 30;

    function getSettings() {
        return AGN.Lib.Storage.get('login-page') || {};
    }

    function setSettings(settings) {
        AGN.Lib.Storage.set('login-page', settings);
    }

    function isNotificationDismissExpired() {
        var settings = getSettings();

        if (settings) {
            if (settings.notificationDismissExpiration) {
                return settings.notificationDismissExpiration <= Date.now();
            }
        }

        /* Compatible mode */
        /* To be removed 30 days later */
        var closedDate = localStorage.getItem('closedDate');
        if (closedDate) {
            var currentDate = new Date();
            var requiredDate = new Date(closedDate);
            requiredDate.setDate(requiredDate.getDate() + NOTIFICATION_DISMISS_EXPIRATION_DAYS);

            return currentDate - requiredDate > 0;

        }
        /* Compatible mode */

        return true;
    }

    function onNotificationDismiss() {
        var settings = getSettings();
        var expirationDate = new Date();

        expirationDate.setDate(expirationDate.getDate() + NOTIFICATION_DISMISS_EXPIRATION_DAYS);
        settings.notificationDismissExpiration = expirationDate.getTime();

        setSettings(settings);
    }

    function isFrameHidden() {
        // By default it's shown.
        return getSettings().isFrameShown == false;
    }

    AGN.Lib.DomInitializer.new('logon', function($e) {
        $e.toggleClass('l-hide-frame', isFrameHidden());
        $e.removeClass('hidden');

        if (isNotificationDismissExpired() && this.config.SHOW_TAB_HINT === true) {
            var head = t('defaults.info');
            var content = t('logon.info.multiple_tabs');

            AGN.Lib.Messages(head, content, 'info', onNotificationDismiss, false);
        }
    });

})();
