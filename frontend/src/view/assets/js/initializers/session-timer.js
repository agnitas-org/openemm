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

  /* session expired time in milliseconds */
  var sessionExpiredDateMs;

  /* flags for identification corresponding operations */
  var isSessionReallyExpired = false;
  var isFirstChecking = true;
  var isChecking = false;
  var timerLoader;

  if (config && creationTimeMs && lastAccessedTimeMs && maxInactiveIntervalS) {
    sessionExpiredDateMs = maxInactiveIntervalS * 1000 + Date.now();
    startTimer();
  }

  function startTimer() {
    setInterval(function () {
      var nowMs = Date.now();
      var timeLeftMs = (sessionExpiredDateMs > nowMs) ? (sessionExpiredDateMs - nowMs) : 0;
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
  var TimerGUI = function() {
    var self = this;
    self.timerLayout = $('#session-time-layout');
    self.timeField = $('#session-time-field');

    self.isSessionExpiredNotification = false;

    /* classes necessary for switching icon */
    self.refreshIconClass = 'icon icon-refresh icon-spin';
  };

  TimerGUI.prototype.showTimerBlock = function(timeLeftMs) {
    var self = this;
    self.timerLayout.show();
    self.printTimerText(timeLeftMs);
  };

  TimerGUI.prototype.showSessionExpired = function() {
    var self = this;

    self.printTimerText(0);

    if (!self.isSessionExpiredNotification) {
      $('body').append(AGN.Lib.Template.text('session-expired'));
      self.isSessionExpiredNotification = true;
    }

  };

  TimerGUI.prototype.showLoading = function() {
    var self = this;

    var loadingText = '<i class="' + self.refreshIconClass + '"></i>';
    self.changeTimeFieldContent(loadingText);
  };

  TimerGUI.prototype.printTimerText = function(timeMs) {
    var self = this;

    var timeString = '00:00';
    if (timeMs > 0) {
      var time = new Date(timeMs);
      timeString = time.getMinutes() > 0 ? d3.format('02')(time.getUTCMinutes()) + ':' : '00:';
      timeString += time.getSeconds() > 0 ? d3.format('02')(time.getUTCSeconds()) : '00';
    }

    self.changeTimeFieldContent(timeString);
  };

  TimerGUI.prototype.changeTimeFieldContent = function(content) {
    this.timeField.html(content);
  };

  return false;
});