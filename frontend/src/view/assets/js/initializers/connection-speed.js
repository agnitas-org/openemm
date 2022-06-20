AGN.Lib.DomInitializer.new('connection-speed-test', function () {
  var SLOW_CONNECTION_THRESHOLD_KBPS = this.config.SLOW_CONNECTION_THRESHOLD_KBPS;
  var SPEED_TEST_RESOURCE_SIZE = this.config.SPEED_TEST_RESOURCE_SIZE; //bytes
  var ADDITIONAL_TRANSFER_DATA_SIZE = 300; //bytes
  var TEST_INTERVAL = 60000; // 1 min
  var anotherAjaxesStartedDuringTest = false;
  var anotherAjaxesRunning = false;
  var testRunning = false;
  var startTime;

  function isSpeedTestAjax(url) {
    return url.indexOf("/connectionSpeedTest") >= 0 || url.indexOf("/connectionSpeed") >= 0;
  }

  $(document).ajaxSend(function (e, jqxhr, settings) {
    if (!isSpeedTestAjax(settings.url)) {
      if (testRunning) {
        anotherAjaxesStartedDuringTest = true;
      }
      anotherAjaxesRunning = true;
      if (settings.url.indexOf('/lock.action') < 0) {
        runSpeedTestThrottled();
      }
    } else {
      testRunning = true;
      anotherAjaxesStartedDuringTest = false;
      startTime = Date.now();
    }
  });

  $(document).ajaxStop(function () {
    anotherAjaxesRunning = false;
  });

  function allImagesLoaded() {
    var result = true;
    $("img").each(function (index, element) {
      if (!element.complete) {
        result = false;
      }
    });
    return result;
  }

  function waitAnotherAjaxesAndRunTest() {
    if (anotherAjaxesRunning) {
      setTimeout(waitAnotherAjaxesAndRunTest, 2000);
    } else {
      if (allImagesLoaded()) {
        runSpeedTest();
      }
    }
  }

  var runSpeedTestThrottled = _.throttle(function () {
    waitAnotherAjaxesAndRunTest();
  }, TEST_INTERVAL);

  function runSpeedTest() {
    AGN.Lib.Loader.prevent();
    $.ajax({
      type: 'GET',
      url: AGN.url("/connectionSpeed/test.action") + "?cacheKiller=" + Date.now(),
      timeout: 10000
    }).done(function (response) {
      if (!anotherAjaxesStartedDuringTest) {
        checkSpeed();
      }
      testRunning = false;
    }).fail(function (jqXHR, textStatus) {
      if (textStatus === 'timeout') {
        changeInternetIndicatorState(0);
        testRunning = false;
      }
    });
  }

  function checkSpeed() {
    var duration = (Date.now() - startTime) / 1000; // seconds
    var bps = ((SPEED_TEST_RESOURCE_SIZE + ADDITIONAL_TRANSFER_DATA_SIZE) * 8 / duration);
    var kbits = bitsToKilobits(bps);
    changeInternetIndicatorState(kbits);
  }

  function changeInternetIndicatorState(kbits) {
    var qualityGood = kbits > SLOW_CONNECTION_THRESHOLD_KBPS;
    
    var $internetIndicator = $('#internet-indicator');
    $internetIndicator.data("tooltip", qualityGood ? t('defaults.ok') : kbits + ' Kbit/s');
    AGN.Lib.CoreInitializer.run('tooltip', $internetIndicator.parent());
    $internetIndicator.find('i').css('color', qualityGood ? '#109e4d': '#ffa300');
  }

  function bitsToKilobits(bps) {
    return (bps / 1000).toFixed(2);
  }

  window.addEventListener("offline", function () {
    changeInternetIndicatorState(0);
  });

  return false;
});