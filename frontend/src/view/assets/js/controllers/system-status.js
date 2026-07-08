AGN.Lib.Controller.new('system-status', function () {

  const Template = AGN.Lib.Template;

  const SYSTEM_VERSION_ATTR = 'system-version';
  const SYSTEM_UPDATE_DISMISS_KEY = 'system-update-dismissed-version';

  this.addDomInitializer('system-status', function () {
    showSystemNotificationIfNotDismissed('system-eol-msg');
    showSystemNotificationIfNotDismissed('system-update-available-msg');
  });

  function showSystemNotificationIfNotDismissed(templateName) {
    if (Template.exists(templateName)) {
      const $notification = Template.dom(templateName);
      const closedVersion = sessionStorage.getItem(SYSTEM_UPDATE_DISMISS_KEY);
      const version = $notification.data(SYSTEM_VERSION_ATTR);

      if (closedVersion === null || version > closedVersion) {
        $('#system-notifications').append($notification);
      }
    }
  }

  this.addAction({click: 'cancel-imports'}, function () {
    AGN.Lib.Confirm.from('cancel-running-imports-modal')
      .done(() => {
        $.post(AGN.url('/serverstatus/killRunningImports.action'));
      });
  });

  this.addAction({click: 'close-system-update-msg'}, function () {
    const $panel = this.el.closest('.panel');
    const version = $panel.data(SYSTEM_VERSION_ATTR);

    sessionStorage.setItem(SYSTEM_UPDATE_DISMISS_KEY, version);
    $panel.remove();
  });

});