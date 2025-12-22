(() => {

  const LOCKING_UPDATE_INTERVAL_MILLISECONDS =  45000;  // 45 seconds
  const LOCKING_MAX_DURATION_MILLISECONDS = 3 * 60 * 60000;  // 3 hours

  AGN.Opt.Components.MailingContentLock = {
    manageLock: function (mailingId, isMailingExclusiveLockingAcquired, anotherLockingUserName) {
      if (mailingId > 0 && isMailingExclusiveLockingAcquired) {
        const ajaxLockingBeginTimestamp = Date.now();
        let ajaxLockingUpdateIntervalId;

        const ajaxUpdateLocking = function() {
          if (ajaxLockingBeginTimestamp + LOCKING_MAX_DURATION_MILLISECONDS > Date.now()) {
            $.ajax({
              url: AGN.url(`/mailing/ajax/${mailingId}/lock.action`),
              type: 'POST',
              success: resp => {
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
              success: () => {
                const $modal = AGN.Lib.Modal.fromTemplate('session-expired');
                $modal.on('modal:close', () => AGN.Lib.Page.reload(AGN.url('/logon.action')));
              }
            });
          }
        };

        ajaxLockingUpdateIntervalId = setInterval(ajaxUpdateLocking, LOCKING_UPDATE_INTERVAL_MILLISECONDS);
        return true;
      }

      AGN.Lib.Modal.fromTemplate('mailing-locked', {username: anotherLockingUserName})
      return false;
    }
  };
})();