// Added to support old code
(function() {
  jQuery.browser = getBrowserInformation().browser;

  function getBrowserInformation() {
    var userAgent = navigator.userAgent.toLowerCase();
    var regexMsie = /(msie) ([\w.]+)/;
    var regexWebkit = /(webkit)[ \/]([\w.]+)/;
    var regexOpera = /(opera)(?:.*version)?[ \/]([\w.]+)/;
    var regexMozilla = /(mozilla)(?:.*? rv:([\w.]+))?/;

    var match = regexWebkit.exec(userAgent) ||
      regexOpera.exec(userAgent) ||
      regexMsie.exec(userAgent) ||
      userAgent.indexOf("compatible") < 0 && regexMozilla.exec(userAgent) || [];
    return { browser: match[1] || "", version: match[2] || "0" };
  }

})(jQuery);