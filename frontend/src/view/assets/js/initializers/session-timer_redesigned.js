AGN.Lib.DomInitializer.new('session-timer', function () {
  const BLOCK_VISIBLE_TIME_MILLISECONDS = 10 * 60 * 1000;
  const DELAY_TIME_MILLISECONDS = 5 * 1000;
  const UNAUTHORIZED_STATUS = 401;
  const NOT_IMPLEMENTED_STATUS = 501;

  /* data transferred from the backend side */
  const config = this.config;
  const sessionInfoUrl = this.config.sessionInfoUrl;
  const creationTimeMs = this.config.creationTime;
  const lastAccessedTimeMs = this.config.lastAccessedTime;
  let maxInactiveIntervalS = this.config.maxInactiveInterval;

  /* session expired time in milliseconds */
  let sessionExpiredDateMs;

  /* flags for identification corresponding operations */
  let isSessionReallyExpired = false;
  let isFirstChecking = true;
  let isChecking = false;
  let timerLoader;

  if (config && creationTimeMs && lastAccessedTimeMs && maxInactiveIntervalS) {
    sessionExpiredDateMs = maxInactiveIntervalS * 1000 + Date.now();
    startTimer();
  }

  function startTimer() {
    setInterval(function () {
      const nowMs = Date.now();
      const timeLeftMs = (sessionExpiredDateMs > nowMs) ? (sessionExpiredDateMs - nowMs) : 0;
      if (!timerLoader) {
        timerLoader = new TimerGUI();
      }

      if ((!isFirstChecking || (timeLeftMs <= BLOCK_VISIBLE_TIME_MILLISECONDS)) && !isSessionReallyExpired && !isChecking) {
        timerLoader.showTimerBlock(timeLeftMs);
      }

      if (!isChecking && !isSessionReallyExpired
          && (sessionExpiredDateMs + DELAY_TIME_MILLISECONDS > nowMs) && (sessionExpiredDateMs < nowMs)) {
        timerLoader.showLoading();
      }

      if (!isSessionReallyExpired && !isChecking && (sessionExpiredDateMs + DELAY_TIME_MILLISECONDS < nowMs)) {
        checkIsReallyExpired();
      }
    }, 1000);
  }

  /**
   * Checking current session.
   * If session really have already died, server return 401 status
   * in other cases returns time to live and update current sessionExpiredDateMs
   * WARNING: The request may extend session time.
   */
  function checkIsReallyExpired() {
    $.ajax({
      url: sessionInfoUrl,
      global: false,
      beforeSend: beforeRequest,
      success: successResponse,
      error: errorResponse,
      complete: afterResponse
    })
  }

  function beforeRequest() {
    isChecking = true;
  }

  function successResponse(data) {
    if (!!data) {
      maxInactiveIntervalS = data.maxInactiveInterval;
      sessionExpiredDateMs = Date.now() + data.maxInactiveInterval * 1000;
      isSessionReallyExpired = false;
    } else {
      isSessionReallyExpired = true;
      timerLoader.showSessionExpired();
    }
  }

  function errorResponse(response) {
    if (response.status === UNAUTHORIZED_STATUS) {
      isSessionReallyExpired = true;
      timerLoader.showSessionExpired();
    }
  }

  function afterResponse() {
    isChecking = false;
    isFirstChecking = false;
  }

  /**
   * Ajax response listener.
   * Necessary for catching any ajax responses and restart the timer.
   * preventSessionTimer variable need for preventing multiple processing the same event.
   * WARNING: Current listener catches AJAX responses only.
   */
  $(document).ajaxComplete(function (event, xhr) {
    if (!event.preventSessionTimer && !isSessionReallyExpired && !isChecking
        && (xhr.status !== UNAUTHORIZED_STATUS && xhr.status < NOT_IMPLEMENTED_STATUS)) {
      sessionExpiredDateMs = maxInactiveIntervalS * 1000 + Date.now();
    }
    event.preventSessionTimer = true;
  });

  /**
   * TimerGui is helper for timer block overview
   *
   * @constructor
   */

  class TimerGUI {

    constructor() {
      this.timerBlock = $('#session-timer-block');
      this.timeLabel = $('#session-time-label');
      this.isSessionExpiredNotification = false;

      /* classes necessary for switching icon */
      this.refreshIconClass = 'icon icon-spinner icon-pulse';
    }

    showTimerBlock(timeLeftMs) {
      this.timerBlock.removeClass('hidden');
      this.printTimerText(timeLeftMs);
    }

    showSessionExpired() {
      this.printTimerText(0);

      if (!this.isSessionExpiredNotification) {
        const $modal = AGN.Lib.Modal.fromTemplate('session-expired');
        $modal.on('modal:close', () => AGN.Lib.Page.reload(AGN.url('/logon.action')));

        this.isSessionExpiredNotification = true;
      }
    }

    showLoading() {
      const loadingText = `<i class="${this.refreshIconClass}"></i>`;
      this.changeTimeFieldContent(loadingText);
    }

    printTimerText(timeMs) {
      let timeString = '00:00';
      if (timeMs > 0) {
        const time = new Date(timeMs);
        timeString = time.getMinutes() > 0 ? d3.format('02')(time.getUTCMinutes()) + ':' : '00:';
        timeString += time.getSeconds() > 0 ? d3.format('02')(time.getUTCSeconds()) : '00';
      }

      this.changeTimeFieldContent(timeString);
    }

    changeTimeFieldContent(content) {
      this.timeLabel.html(content);
    }
  }

  return false;
});