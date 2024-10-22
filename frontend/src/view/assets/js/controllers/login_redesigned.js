AGN.Lib.Controller.new('login', function () {

  const NOTIFICATION_DISMISS_EXPIRATION_DAYS = 30;
  const STORAGE_KEY = 'login-page';

  const Storage = AGN.Lib.Storage;
  const Messages = AGN.Lib.Messages;
  const Form = AGN.Lib.Form;

  this.addDomInitializer('login', function () {
    if (this.config.SHOW_TAB_HINT === true && isNotificationDismissExpired()) {
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

  this.addAction({keydown: 'password-change'}, function () {
    if (isCapsLockEnabled(this.event)) {
      Form.showFieldError$(this.el, t('login.error.capslock'));
    }
  });

  this.addAction({keyup: 'password-change'}, function () {
    if (!isCapsLockEnabled(this.event)) {
      Form.get(this.el).cleanFieldFeedback(this.el);
    }
  });

  function isCapsLockEnabled(event) {
    const e = event.originalEvent;
    return e.getModifierState && e.getModifierState('CapsLock');
  }

  this.addAction({click: 'showPasswordChangeForm'}, function() {
    $('#suggestion-view').addClass('hidden');
    $('#submission-view').removeClass('hidden');
  });

});
