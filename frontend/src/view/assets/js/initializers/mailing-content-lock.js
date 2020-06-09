(function() {
  var LOCKING_UPDATE_INTERVAL_MILLISECONDS =  45000;  // 45 seconds
  var LOCKING_MAX_DURATION_MILLISECONDS = 3 * 60 * 60000;  // 3 hours

  AGN.Opt.Components.MailingContentLock = {
    manageLock: function (mailingId, isMailingExclusiveLockingAcquired) {
      if (mailingId > 0 && isMailingExclusiveLockingAcquired) {
        var ajaxLockingBeginTimestamp = Date.now();
        var ajaxLockingUpdateIntervalId;

        var ajaxUpdateLocking = function() {
          if (ajaxLockingBeginTimestamp + LOCKING_MAX_DURATION_MILLISECONDS > Date.now()) {
            $.ajax({
              url: AGN.url('/mailing/ajax/' + mailingId + '/lock.action'),
              type: 'POST',
              success: function(resp) {
                // If locking prolongation is failed for some reason (normally should never happen), just ignore that.
                if (resp.success !== true) {
                  clearInterval(ajaxLockingUpdateIntervalId);
                }
              },
              statusCode: {
                // Mailing is deleted or doesn't exist.
                404: function() {
                  clearInterval(ajaxLockingUpdateIntervalId);
                }
              }
            });
          } else {
            clearInterval(ajaxLockingUpdateIntervalId);

            $(document).trigger('ajax:unauthorized'); // for auto-save

            $.ajax({
              url: AGN.url('/logout.action'),
              type: 'POST',
              success: function() {
                $('body').append(AGN.Lib.Template.text('session-expired'));
              }
            });
          }
        };

        ajaxLockingUpdateIntervalId = setInterval(ajaxUpdateLocking, LOCKING_UPDATE_INTERVAL_MILLISECONDS);
        return true;
      } else {
        AGN.Lib.Modal.createFromTemplate({
          modalClass: '',
          title: t('defaults.warning'),
          content: t('error.mailing.exclusiveLockingFailed')
        }, 'modal');

        return false;
      }
    }
  };
})();