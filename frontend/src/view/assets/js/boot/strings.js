(function() {
  /**
   * Polyfill for String.startsWith
   * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/startsWith
   */
  if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(search, pos) {
      return this.substr(!pos || pos < 0 ? 0 : +pos, search.length) === search;
    };
  }

  /**
   * Polyfill for String.endsWith
   * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/endsWith
   */
  if (!String.prototype.endsWith) {
    String.prototype.endsWith = function(search, len) {
      if (len === undefined || len > this.length) {
        len = this.length;
      }
      return this.substring(len - search.length, len) === search;
    };
  }

  /**
   * Polyfill for String.includes
   * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/includes
   */
  if (!String.prototype.includes) {
    String.prototype.includes = function(search, start) {
      'use strict';
      if (typeof start !== 'number') {
        start = 0;
      }

      if (start + search.length > this.length) {
        return false;
      } else {
        return this.indexOf(search, start) !== -1;
      }
    };
  }
})();
