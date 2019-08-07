AGN.Lib.DomInitializer.new('session-timer', function () {
  var BLOCK_VISIBLE_TIME_MILLISECONDS = 10 * 60 * 1000;
  var DELAY_TIME_MILLISECONDS = 5 * 1000;
  var UNAUTHORIZED_STATUS = 401;
  var NOT_IMPLEMENTED_STATUS = 501;

  /* data transferred from the backend side */
  var config = this.config;
  var sessionInfoUrl = this.config.sessionInfoUrl;
  var creationTimeMs = this.config.creationTime;
  var lastAccessedTimeMs = this.config.lastAccessedTime;
  var maxInactiveIntervalS = this.config.maxInactiveInterval;

  /* elements of timer layout */
  var $sessionTimerLayout = $('#session-time-layout');
  var $sessionTimeField = $('#session-time-field');
  var $sessionTimeIcon = $('#session-time-icon');

  /* classes necessary for switching icon */
  var hourglassIconClasses = 'menu-item-logo icon icon-hourglass-end';
  var refreshIconClasses = 'icon icon-refresh icon-spin';

  /* session expired time in milliseconds */
  var sessionExpiredDateMs;

  /* flags for identification corresponding operations */
  var isSessionReallyExpired = false;
  var isFirstChecking = true;
  var isChecking = false;

  if (config && creationTimeMs && lastAccessedTimeMs && maxInactiveIntervalS) {
    sessionExpiredDateMs = maxInactiveIntervalS * 1000 + Date.now();
    startTimer();
  }

  function startTimer() {
    setInterval(function () {
      var nowMs = Date.now();
      var timeLeftMs = (sessionExpiredDateMs > nowMs) ? (sessionExpiredDateMs - nowMs) : 0;

      if ((!isFirstChecking || (timeLeftMs <= BLOCK_VISIBLE_TIME_MILLISECONDS)) && !isSessionReallyExpired && !isChecking) {
        $sessionTimerLayout.show();
        printTimeMs(timeLeftMs);
      }

      if (!isChecking && !isSessionReallyExpired
          && (sessionExpiredDateMs + DELAY_TIME_MILLISECONDS > nowMs) && (sessionExpiredDateMs < nowMs)) {
        showLoading();
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
    maxInactiveIntervalS = data.maxInactiveInterval;
    sessionExpiredDateMs = Date.now() + data.maxInactiveInterval * 1000;
    isSessionReallyExpired = false;
  }

  function errorResponse(response) {
    if (response.status === UNAUTHORIZED_STATUS) {
      isSessionReallyExpired = true;
      showSessionExpired();
    }
  }

  function afterResponse() {
    isChecking = false;
    isFirstChecking = false;
  }

  function showSessionExpired() {
    $sessionTimeIcon.attr('class', hourglassIconClasses);
    $sessionTimeField.text(t('logon.session.expired'));
  }

  function printTimeMs(timeMs) {
    $sessionTimeIcon.attr('class', hourglassIconClasses);

    if (timeMs > 0) {
      var time = new Date(timeMs);
      var timeString = '';
      timeString += time.getMinutes() > 0 ? d3.format('02')(time.getUTCMinutes()) + ':' : '00:';
      timeString += time.getSeconds() > 0 ? d3.format('02')(time.getUTCSeconds()) : '00';
      timeString += ' ' + t('time.min') + ' ' + t('logon.session.remaining');

      $sessionTimeField.text(timeString)
    }
  }

  function showLoading() {
    $sessionTimeIcon.attr('class', refreshIconClasses);
    $sessionTimeField.text(t('logon.session.checking'));
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
});