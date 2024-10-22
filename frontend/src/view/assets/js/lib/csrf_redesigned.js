/*doc
---
title: CSRF
name: csrf
category: Javascripts - CSRF
---

For various manipulations related to the csrf token, you can use `AGN.Lib.CSRF`.

Below are some methods you can call:

method                               |description                                                                                 |
-------------------------------------|--------------------------------------------------------------------------------------------|
`readActualToken()`                  |Returns actual token from cookies                                                           |
`getParameterName()`                 |Returns name of request parameter                                                           |
`isProtectionEnabled()`              |Returns `true` if protection enabled                                                        |
`setTokenToReqHeader(jqxhr, reqType)`|Set actual token value to the request header                                                |
`updateTokenInDOM($scope)`           |Updates the CSRF related inputs inside the DOM to actual value. `$scope` can be a plain text|

*/

(function () {

  class CSRF {
    static readActualToken() {
      if (CSRF.isProtectionEnabled()) {
        return AGN.Lib.Storage.readCookie(window.csrfCookieName);
      }

      return '';
    }

    static getParameterName() {
      return window.csrfParameterName;
    }

    static isProtectionEnabled() {
      return window.csrfHeaderName && CSRF.getParameterName() && window.csrfCookieName;
    }

    static setTokenToReqHeader(jqxhr, reqType) {
      if (CSRF.isProtectionEnabled() && reqType !== 'GET') {
        jqxhr.setRequestHeader(window.csrfHeaderName, CSRF.readActualToken());
      }
    }

    static updateTokenInDOM($scope = $(document)) {
      if (!CSRF.isProtectionEnabled()) {
        return $scope;
      }

      const isPlainText = !($scope instanceof $);
      if (isPlainText) {
        $scope = $('<div>').append($scope);
      }

      const csrfToken = CSRF.readActualToken();
      const $tokenInputs = $scope.find(`input[name="${CSRF.getParameterName()}"]`);

      _.each($tokenInputs, input => {
        if (isPlainText) {
          $(input).attr('value', csrfToken);
        } else {
          $(input).val(csrfToken);
        }
      });

      if (isPlainText) {
        return $scope.html();
      }
    }
  }

  AGN.Lib.CSRF = CSRF;
})();
