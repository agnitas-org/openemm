(function() {

  var isFF = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;

  var  addEventListener = function(el, evt, func) {
    if ("addEventListener" in window) {
      el.addEventListener(evt, func, false);
    } else if ("attachEvent" in window) {
      el.attachEvent("on" + evt, func);
    }
  };

  var onLoadHandler = function() {
    birtCommunicationManager.postProcess = function() {
      BirtCommunicationManager.prototype.postProcess.call(this);

      if ( isFF ) {
        window.setInterval(function() {
          window.parentIFrame.size();
        }, 500)
      }

    }
  };

  addEventListener(window, 'load', onLoadHandler);
})();
