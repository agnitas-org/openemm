(function () {

  const NOTIFICATION_DISMISS_EXPIRATION_DAYS = 30;
  const STORAGE_KEY = 'login-page';
  const Storage = AGN.Lib.Storage;
  const Messages = AGN.Lib.Messages;

  function getSettings() {
    return Storage.get(STORAGE_KEY) || {};
  }

  function setSettings(settings) {
    Storage.set(STORAGE_KEY, settings);
  }

  function isNotificationDismissExpired() {
    const settings = getSettings();
    if (settings?.notificationDismissExpiration) {
      return settings.notificationDismissExpiration <= Date.now();
    }

    /* Compatible mode */
    /* To be removed 30 days later */
    const closedDate = localStorage.getItem('closedDate');
    if (closedDate) {
      const currentDate = new Date();
      const requiredDate = new Date(closedDate);
      requiredDate.setDate(requiredDate.getDate() + NOTIFICATION_DISMISS_EXPIRATION_DAYS);

      return currentDate - requiredDate > 0;

    }
    /* Compatible mode */
    return true;
  }

  function onNotificationDismiss() {
    const settings = getSettings();
    const expirationDate = new Date();

    expirationDate.setDate(expirationDate.getDate() + NOTIFICATION_DISMISS_EXPIRATION_DAYS);
    settings.notificationDismissExpiration = expirationDate.getTime();

    setSettings(settings);
  }

  function isFrameHidden() {
    // By default it's shown.
    return getSettings().isFrameShown == false;
  }

  AGN.Lib.DomInitializer.new('logon', function () {
    if (isNotificationDismissExpired() && this.config.SHOW_TAB_HINT === true) {
      Messages(
        t('defaults.info'),
        t('logon.info.multiple_tabs'),
        'info',
        onNotificationDismiss,
        false
      );
    }

    if (this.config.logoutMsg) {
      Messages.infoText(this.config.logoutMsg);
    }
  });

})();
