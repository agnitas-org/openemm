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
`getHeaderName()`                    |Returns name of request header                                                              |
`getCookieName()`                    |Returns name of cookie                                                                      |
`getParameterName()`                 |Returns name of request parameter                                                           |
`isProtectionEnabled()`              |Returns `true` if protection enabled                                                        |
`setTokenToReqHeader(jqxhr, reqType)`|Set actual token value to the request header                                                |
`updateTokenInDOM($scope)`           |Updates the CSRF related inputs inside the DOM to actual value. `$scope` can be a plain text|

*/

(() => {

  class CSRF {

    static HEADER_NAME = '';
    static COOKIE_NAME = '';
    static PARAM_NAME = '';

    static getParameterName() {
      return this.PARAM_NAME;
    }

    static getCookieName() {
      return this.COOKIE_NAME;
    }

    static getHeaderName() {
      return this.HEADER_NAME;
    }

    static setHeaderName(headerName) {
      this.HEADER_NAME = headerName;
    }

    static setParamName(paramName) {
      this.PARAM_NAME = paramName;
    }

    static setCookieName(cookieName) {
      this.COOKIE_NAME = cookieName;
    }

    static isProtectionEnabled() {
      return this.getHeaderName() && this.getParameterName() && this.getCookieName();
    }

    static readActualToken() {
      if (CSRF.isProtectionEnabled()) {
        return AGN.Lib.Storage.readCookie(this.getCookieName());
      }

      return '';
    }

    static setTokenToReqHeader(jqxhr, reqType) {
      if (reqType !== 'GET') {
        jqxhr.setRequestHeader(this.getHeaderName(), this.readActualToken());
      }
    }

    static updateTokenInDOM($scope = $(document)) {
      if (!this.isProtectionEnabled()) {
        return $scope;
      }

      const isPlainText = !($scope instanceof $);
      if (isPlainText) {
        $scope = $('<div>').append($scope);
      }

      const csrfToken = this.readActualToken();
      const $tokenInputs = $scope.find(`input[name="${this.getParameterName()}"]`);

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
