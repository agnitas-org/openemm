AGN.Lib.DomInitializer.new('workflow-pause-timer', function() {
  const WORKFLOW_ID = this.config.workflowId;
  const PAUSE_TIME_MS = this.config.pauseTime;
  const DEFAULT_EXPIRATION_HOURS = this.config.defaultExpirationHours;
  const EXPIRATION_HOURS = this.config.expirationHours > 0 ? this.config.expirationHours : DEFAULT_EXPIRATION_HOURS;
  const EXPIRATION_MS = EXPIRATION_HOURS * 60 * 60 * 1000; // 2 hours * 60 min * 60 sec * 1000 = 7200000 ms

  const timeField = $('#workflow-pause-timer-text');
  const oneSecInterval = setInterval(function () {updateTimer()}, 1000);
  updateTimer(); // show timer without 1s delay

  function updateTimer() {
    timeField.html(getTimeStr());
  }

  function calculateTimeLeftMs() {
    return EXPIRATION_MS - (Date.now() - PAUSE_TIME_MS);
  }

  // If timer run out, send ajax call to unpause (auto reactivate) campaign
  function getTimeStr() {
    const timeLeftMs = calculateTimeLeftMs()
    if (timeLeftMs <= 0) {
      if (oneSecInterval) {
        clearInterval(oneSecInterval);
      }
      autoUnpause();
      return '<i class="icon icon-refresh icon-spin"></i> ' + t('workflow.activating.auto');
    }
    const time = new Date(timeLeftMs);
    return (time.getHours() > 0 ? d3.format('01')(time.getUTCHours()) + ':' : '')
      + (time.getMinutes() > 0 ? d3.format('02')(time.getUTCMinutes()) + ':' : '00:')
      + (time.getSeconds() > 0 ? d3.format('02')(time.getUTCSeconds()) : '00');
  }

  function autoUnpause() {
    return $.post(AGN.url('/workflow/' + WORKFLOW_ID + '/autoUnpause.action'))
      .done(function(resp) {
        if (resp.success === true) {
          AGN.Lib.Page.reload(AGN.url('/workflow/' + WORKFLOW_ID + '/view.action'));
        } else {
          AGN.Lib.JsonMessages(resp.popups);
        }
      })
      .fail(function() {
        AGN.Lib.Messages(t("Error"), t("defaults.error"), "alert");
      });
  }
  return false;
});
